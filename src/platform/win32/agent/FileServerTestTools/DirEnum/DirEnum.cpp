// DirEnum.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <windows.h>
#include "dsntprocioctl.h"	//Use for NTProcDrv
#include "dsfileinfo.h"		
#include "dsifsioctls.h"	//Use for dsifsflt
#include "dsifsinterface.h"	//Use for dsifsflt
#include <lm.h>

BOOL gfFileAgentMode = TRUE;	//FALSE will be DesktopAgentMode

BOOL EnumerateDirs(HANDLE hDriver, PDIRS_RECORD pDirRecord, PWCHAR pwzCurrentDir, BOOL IsFileAgentMode)
{
	PDIR_LINKS		pDirLink = NULL;
	PDIR_LINKS		pTempNextDirLink;
	PDIR_LINKS		pTempRightDirLink;
	ULONG			ulIOCtl;
	ULONG			ulBytesReturn;
	HANDLE			hFileHandle = NULL;
	WCHAR			wzTempName[MAX_NAME_LENGTH];
	WIN32_FIND_DATAW	strFindFileData;

	if (hDriver == NULL || pDirRecord == NULL || pwzCurrentDir == NULL)
	{
		printf("EnumerateDirs<ERROR> NULL parameter(s) received\n");
		return FALSE;
	}

	ulIOCtl = IsFileAgentMode == TRUE ? BJ_IFS_GET_FILE_INFO : IOCTL_NTPROCDRV_GET_FILE_INFO;

	pDirLink = (PDIR_LINKS)malloc(sizeof(DIR_LINKS));
	if (pDirLink == NULL)
	{
		printf("EnumerateDirs<ERROR>cannot malloc PDIR_LINK\n");
		return FALSE;
	}

	pDirLink->strFileInfo.hFileHandle = CreateFileW(pwzCurrentDir, 
												  FILE_READ_DATA, 
												  FILE_SHARE_READ,  
												  NULL, 
												  OPEN_EXISTING, 
												  FILE_FLAG_BACKUP_SEMANTICS, 
												  NULL);

	if (pDirLink->strFileInfo.hFileHandle == INVALID_HANDLE_VALUE)
	{
		printf("EnumerateDirs<ERROR>Cannot open file handle\n");
		goto EnumerateDirsError1;
	}

	pDirLink->ulNumDuplicate = 0;
	pDirLink->pNext = pDirLink->pRight = NULL;
	
	if (!DeviceIoControl(hDriver, 
						 ulIOCtl, 
						 &pDirLink->strFileInfo, sizeof(FILE_INFO),
						 &pDirLink->strFileInfo, sizeof(FILE_INFO),
						 &ulBytesReturn, NULL))
	{
		printf("EnumerateDirs<ERROR>DeviceIoControl FAILED LastError = 0x%x\n",GetLastError());
		goto EnumerateDirsError2;
	}

	if (pDirLink->strFileInfo.fDirectory != 1)
	{
		//Could be a zero length dir, ignore it.
		goto EnumerateDirsError2;
	}

	if (pDirRecord->pDirLinks == NULL)
	{
		pDirRecord->ulNumOfUniqueDirs = 1;
		pDirRecord->pDirLinks = pDirLink;
	}
	else	//Traverse the tree!!! TBD
	{
		pTempNextDirLink = pDirRecord->pDirLinks;
		while(pTempNextDirLink)	//Now traverse down the linklist
		{
			//Ah hah, found a duplicate directory now traverse to the right
			if (pDirLink->strFileInfo.ulHighUniqueID == pTempNextDirLink->strFileInfo.ulHighUniqueID &&
				pDirLink->strFileInfo.ulLowUniqueID == pTempNextDirLink->strFileInfo.ulLowUniqueID)
			{
				ULONG ulNumDuplicate = ++pTempNextDirLink->ulNumDuplicate;
				pTempRightDirLink = pTempNextDirLink;
				while (pTempRightDirLink)
				{
					if (pTempRightDirLink->pRight == NULL)	//Stick this duplicate to the right
					{
						pDirLink->ulNumDuplicate = ulNumDuplicate;
						pTempRightDirLink->pRight = pDirLink;
						goto EnumerateDirsOutOfWhileLoops;
					}
					pTempRightDirLink = pTempRightDirLink->pRight;
					pTempRightDirLink->ulNumDuplicate = ulNumDuplicate;

				}//while (pTempRightDirLink)

			}
			
			if (pTempNextDirLink->pNext == NULL)//Unique Dir, stick this to the bottom of the linklist
			{
				pTempNextDirLink->pNext = pDirLink;
				pDirRecord->ulNumOfUniqueDirs++;
				goto EnumerateDirsOutOfWhileLoops;
			}
			pTempNextDirLink = pTempNextDirLink->pNext;
		}	//while(pTempNextDirLink)
	}
EnumerateDirsOutOfWhileLoops:
	//We don't free pDirLink, the calling application 
	//responsible for free it.  We only allocate.
	CloseHandle(pDirLink->strFileInfo.hFileHandle);

	wcscpy(wzTempName, pwzCurrentDir);
	wcscat(wzTempName, L"\\*");
	hFileHandle = FindFirstFileW(wzTempName, &strFindFileData);

	if (hFileHandle == INVALID_HANDLE_VALUE)
	{
		printf("EnumerateDirs<WARNING>Failed on FindFirstFile Last Error = 0x%x\n",GetLastError());
		return TRUE;
	}

	while(TRUE)
	{
		//Note:  Do not use the flag FILE_ATTRIBUTE_DIRECTORY as it is not reliable at all.  Instead,
		//use our kernelmode driver to find out if the file is the directory or not.
		//Note2: strFindFileData.nFileSizeHigh == 0 && strFindFileData.nFileSizeLow == 0 is not guarantee 
		//to be a directory as we may have zero length file.  However with the next swoop in the recursive,
		//our kernel driver will find out the zero length file is not a directory and not keeping it around
		if (wcscmp(strFindFileData.cFileName, L".") != 0 &&		//don't keep track . and .. directories
			wcscmp(strFindFileData.cFileName, L"..") != 0 &&
			strFindFileData.nFileSizeHigh == 0 &&
			strFindFileData.nFileSizeLow == 0)
		{
			wcscpy(wzTempName, pwzCurrentDir);
			wcscat(wzTempName, L"\\");
			wcscat(wzTempName, strFindFileData.cFileName);
			EnumerateDirs(hDriver, pDirRecord, wzTempName, IsFileAgentMode);
		}

		if (FindNextFileW(hFileHandle, &strFindFileData) == FALSE)
		{
			FindClose(hFileHandle);
			break;
		}
	}
	
	return TRUE;

EnumerateDirsError2:
	if (pDirLink && pDirLink->strFileInfo.hFileHandle != INVALID_HANDLE_VALUE)
		CloseHandle(pDirLink->strFileInfo.hFileHandle);
EnumerateDirsError1:
	if (pDirLink) free(pDirLink);
	return FALSE;
}


BOOL DoFileAgentDirEnum()
{
	return TRUE;
}

PDIRS_RECORD DoDesktopAgentDirEnum(LPTSTR lpszServerName, PULONG pulError)
{
	HANDLE			hDriver = NULL;
	ULONG			ulCount;
	PDIRS_RECORD	pDirRecord = NULL;
	NET_API_STATUS	napiStatus;
	WCHAR			wzServerName[MAX_NAME_LENGTH];
	WCHAR			wzPathName[MAX_NAME_LENGTH];
	DWORD			dwSharesRead, dwTotalShares;
	PSHARE_INFO_1	pSharedInfo1 = NULL;
	PSHARE_INFO_1	pSharedInfo1Temp = NULL;	

	if (lpszServerName == NULL)
	{
		printf("DoDeskTopAgentDirEnum<ERROR>NULL lpszServerName received\n");
		if (pulError)
			*pulError = ERROR_INVALID_PARAMETER;
		return NULL;
	}

	if (pulError == NULL)
	{
		printf("DoDeskTopAgentDirEnum<ERROR>NULL pulError received\n");
		return NULL;
	}

    hDriver = CreateFileW( L"\\\\.\\NTProcDrv",
						   GENERIC_READ | GENERIC_WRITE,
						   0,
						   NULL,
						   OPEN_EXISTING,
						   FILE_ATTRIBUTE_NORMAL,
						   NULL);
	if (hDriver == INVALID_HANDLE_VALUE)
	{
		*pulError = GetLastError();
		printf("DoDeskTopAgentDirEnum<ERROR>Cannot connect to driver status = 0x%x\n", *pulError);		
		goto DoDesktopAgentDirEnumExit1;
	}

	pDirRecord = (PDIRS_RECORD)malloc(sizeof(DIRS_RECORD));
	if (pDirRecord == NULL)
	{
		*pulError = ERROR_NO_SYSTEM_RESOURCES;
		printf("DoDeskTopAgentDirEnum<ERROR>Cannot allocated pDirRecord\n");
		goto DoDesktopAgentDirEnumExit2;
	}

	//Initialize pDirRecord
	pDirRecord->ulNumOfUniqueDirs = 0;
	pDirRecord->pDirLinks = NULL;

	wcscpy(wzPathName, L"\\\\");
	mbstowcs(wzServerName, lpszServerName, MAX_NAME_LENGTH);	
	
	do // begin do
	{
		napiStatus = NetShareEnum ((LPSTR)(LPWSTR)wzServerName, 
								    1, 
									(LPBYTE *) &pSharedInfo1, 
									MAX_PREFERRED_LENGTH, 
									&dwSharesRead, 
									&dwTotalShares, 
									NULL);
		//
		// If the call succeeds,
		//
		if(napiStatus == ERROR_SUCCESS || napiStatus == ERROR_MORE_DATA)
		{
			pSharedInfo1Temp = pSharedInfo1;

			//
			// Loop through the entries;
			//  print retrieved data.
			//
			for(ulCount = 1; ulCount <= dwSharesRead; ulCount++)
			{
				if (pSharedInfo1Temp->shi1_type == STYPE_DISKTREE)
				{
					//printf("Server %S has shared dir = %S\n", wzServerName, pSharedInfo1Temp->shi1_netname);
					wcscat(wzPathName, wzServerName);
					wcscat(wzPathName, L"\\");
					wcscat(wzPathName, (WCHAR *)pSharedInfo1Temp->shi1_netname);
					EnumerateDirs(hDriver, pDirRecord, wzPathName, gfFileAgentMode);
					wcscpy(wzPathName, L"\\\\");
				}
 
				pSharedInfo1Temp++;
			}
		}
		else 
		{
			printf("DoDeskTopAgentDirEnum<ERROR>NetShareEnum status = %ld\n",napiStatus);
			free(pDirRecord);
			pDirRecord = NULL;
			*pulError = napiStatus;
			goto DoDesktopAgentDirEnumExit2;
		}

		//
		// Free the allocated buffer even if call failed
		//
		if (pSharedInfo1)
			NetApiBufferFree(pSharedInfo1);

	}
	// Continue to call NetShareEnum while 
	//  there are more entries. 
	// 
	while (napiStatus == ERROR_MORE_DATA); // end do
DoDesktopAgentDirEnumExit2:
	if (hDriver) CloseHandle(hDriver);
DoDesktopAgentDirEnumExit1:
	return pDirRecord;
}

BOOL DisplayDirs(PDIRS_RECORD pDirsRecord)
{
	PDIR_LINKS	pTempNextDirLink;
	PDIR_LINKS	pTempRightDirLink;
	if (pDirsRecord == NULL)
	{
		printf("DisplayDirs<ERROR>NULL pDirsRecord received\n");
		return FALSE;
	}
	printf("Total of unique directories is: %d\n", pDirsRecord->ulNumOfUniqueDirs);
	pTempNextDirLink = pDirsRecord->pDirLinks;

	while (pTempNextDirLink)
	{
		if (pTempNextDirLink->ulNumDuplicate > 0)
		{
			printf("   --Duplicate--: Num Of Dir=%d\n", pTempNextDirLink->ulNumDuplicate + 1);
			pTempRightDirLink = pTempNextDirLink;
			while (pTempRightDirLink)
			{
				printf("   Dir=%S  IDHi=0x%x   IDLo=0x%x\n",
						pTempRightDirLink->strFileInfo.wzFileName,
						pTempRightDirLink->strFileInfo.ulHighUniqueID,
						pTempRightDirLink->strFileInfo.ulLowUniqueID);

				pTempRightDirLink = pTempRightDirLink->pRight;
			}
		}
		else
		{
			printf("Dir=%S  IDHi=0x%x   IDLo=0x%x\n",
					pTempNextDirLink->strFileInfo.wzFileName,
					pTempNextDirLink->strFileInfo.ulHighUniqueID,
					pTempNextDirLink->strFileInfo.ulLowUniqueID);
		}

		pTempNextDirLink = pTempNextDirLink->pNext;
	}
	return TRUE;
}

int _tmain(int argc, _TCHAR* argv[])
{
	PDIRS_RECORD pDirRecord = NULL;

	ULONG ulErrorCode;
	if (argc > 1)
	{
		gfFileAgentMode = FALSE;	//DesktopAgent mode
	}

	if (gfFileAgentMode == TRUE)
		DoFileAgentDirEnum();
	else
	{
		pDirRecord = DoDesktopAgentDirEnum(argv[1], &ulErrorCode);
	}
	
	if (pDirRecord == NULL)
	{
		printf("pDirRecord returning is NULL, error = 0x%x\n",ulErrorCode);
		return 1;
	}
	DisplayDirs(pDirRecord);
	return 0;
}

