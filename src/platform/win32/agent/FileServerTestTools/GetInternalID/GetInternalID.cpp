// GetInternalID.cpp : Defines the entry point for the console application.
//
#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <windows.h>
#include "dsntprocioctl.h"	//Use for NTProcDrv
#include "dsfileinfo.h"		
#include "dsifsioctls.h"	//Use for dsifsflt
#include "dsifsinterface.h"	//Use for dsifsflt

int _tmain(int argc, _TCHAR* argv[])
{
	HANDLE hFile;
	FILE_INFO strFileInfo;
	HANDLE hDriver;
	DWORD ulBytesReturn;

	if (argc <= 1)
	{
		printf("GetInternalID, must enter a sharename/directory/file\n");
		return 1;
	}

	printf("File/Directory/ShareName to open = %s\n", argv[1]);

	hFile = CreateFileA(argv[1], FILE_READ_DATA, FILE_SHARE_READ,  
					   NULL, OPEN_EXISTING, 
					   FILE_FLAG_BACKUP_SEMANTICS, NULL);

	if (hFile == INVALID_HANDLE_VALUE)
	{
		printf("Cannot Open %s , program terminate LastError = 0x%x\n", argv[1], GetLastError());
		return 1;
	}

	hDriver =  CreateFileW( /*DSIFS_W32_DEVICE_NAME*/DSNTPROC_W32_DEVICE_NAME,
							GENERIC_READ | GENERIC_WRITE,
							0,
							NULL,
							OPEN_EXISTING,
							FILE_ATTRIBUTE_NORMAL,
							NULL);
	if (hDriver == NULL)
	{
		printf("Cannot Open NTProcDrv driver, program terminate\n");
		CloseHandle(hFile);
		return 1;
	}
	strFileInfo.hFileHandle = hFile;

	if (!DeviceIoControl(hDriver, 
						 /*BJ_IFS_GET_FILE_INFO*/IOCTL_NTPROCDRV_GET_FILE_INFO, 
						 &strFileInfo, sizeof(FILE_INFO),
						 &strFileInfo, sizeof(FILE_INFO),
						 &ulBytesReturn, NULL))
	{
		printf("DeviceIoControl FAILED, program terminate\n");
		CloseHandle(hFile);
		CloseHandle(hDriver);
		return 1;
	}
	

	printf("SUCCESS::Can open %s, Info on File/Dir/ShareName\n", argv[1]);

	printf("HighUniqueID = 0x%x  LowUniqueID = 0x%x \n", strFileInfo.ulHighUniqueID, strFileInfo.ulLowUniqueID);
	printf("HighCreationTime = 0x%x  LowCreationTime = 0x%x \n",strFileInfo.ulHighCreationTime,strFileInfo.ulLowCreationTime);
	printf("HighLastAccessTime = 0x%x  LowLastAccessTime = 0x%x\n",strFileInfo.ulHighLastAccessTime,strFileInfo.ulLowLastAccessTime);
	printf("HighLastWriteTime = 0x%x  LowLastWriteTime = 0x%x\n",strFileInfo.ulHighLastWriteTime,strFileInfo.ulLowLastWriteTime);
	printf("HighAllocationSize = 0x%x  LowAllocationSize = 0x%x\n",strFileInfo.ulHighAllocationSize,strFileInfo.ulLowAllocationSize);
	printf("HighEndOfFile = 0x%x  LowEndOfFile = 0x%x\n",strFileInfo.ulHighEndOfFile,strFileInfo.ulLowEndOfFile);
	printf("HighCurrentByteOffset = 0x%x  LowCurrentByteOffset = 0x%x\n",strFileInfo.ulHighCurrentByteOffset,strFileInfo.ulLowCurrentByteOffset);
	printf("AccessMaskFlag = 0x%x  Mode = 0x%x\n",strFileInfo.ulAccessMaskFlag,strFileInfo.ulMode);	
	printf("NumOfLinks = %d  Attribute = 0x%x\n",strFileInfo.ulNumOfLinks, strFileInfo.ulAttribute);
	printf("IsDir = %d  IsDeletePending = %d\n",strFileInfo.fDirectory, strFileInfo.fDeletePending);
	printf("FileNameLength = %d  FileName = %S\n",strFileInfo.ulFileNameLength / 2,strFileInfo.wzFileName);

	CloseHandle(hFile);
	CloseHandle(hDriver);

	return 0;
}

