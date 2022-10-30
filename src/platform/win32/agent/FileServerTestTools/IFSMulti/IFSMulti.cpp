// user.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <windows.h>

#define MAX_STRING	256
#define  MAX_FILES	256

ULONG gulBufSize = 1024;
ULONG gulNumThreads = 10;
ULONG gulIterations = 1000;
char  gszFile[MAX_STRING] = "T:\\asdf.txt";
BOOL  gfWriteMode = FALSE;
BOOL  gfListFileMode = FALSE;

//global variable used by mygetopt function and get_parameters
static char* myoptarg;

class cIFSMULTI
{
	friend IFSWorkerThread(PVOID pData);
public:
	BOOL RunThreads(void);
	cIFSMULTI();
	~cIFSMULTI();
private:
	ULONG	m_ulBufSize;
	PVOID	m_pBuf;
	BOOL	m_fWriteMode;
	ULONG	m_ulThreads;	
	ULONG	m_ulIterations;
	HANDLE	*m_phThreadWaitEvents;
	ULONG	m_ulStartTime;
	ULONG	m_ulEndTime;
	char	*m_pFileNameList[MAX_FILES];
	ULONG	m_ulNumOfFiles;
};
cIFSMULTI *g_pcIFSMulti = NULL;

DWORD GetUserInput (void)
{
	DWORD	err = 0;
	int		c = 0;
	while (1) 
	{
		c = toupper(c);		// upcase it 
		if (c == 'S' || c == 'X' || c == 'Q' || c == EOF) 
		{
			printf("Got stop request, shutting down...\n");
			break;
		} 
		c = getchar();
	}
	return err; 
}


cIFSMULTI::cIFSMULTI(void)
{
	FILE *fFileListHandle = NULL;
	char szTemp[MAX_STRING];
	m_fWriteMode = gfWriteMode;
	m_ulThreads = gulNumThreads;
	m_ulIterations = gulIterations;
	m_ulBufSize = gulBufSize;
	m_pBuf = NULL;
	memset(m_pFileNameList, 0, MAX_FILES);
	m_ulNumOfFiles = 0;

	if (gfListFileMode == FALSE)
	{
		m_ulNumOfFiles = 1;
		m_pFileNameList[0] = (char *)malloc(MAX_STRING);
		strcpy(m_pFileNameList[0], gszFile);
		printf("File %s\n",m_pFileNameList[0]);
	}
	else
	{
		if ((fFileListHandle = fopen(gszFile, "r")) == NULL)
		{
			printf("Cannot open FileName %s in filelist mode\n");
			return;
		}

		while (m_ulNumOfFiles < MAX_FILES)
		{
			memset(szTemp, 0, MAX_STRING);
			if (fscanf(fFileListHandle, "%s", szTemp) == EOF)
				break;			
			m_pFileNameList[m_ulNumOfFiles] = (char *)malloc(strlen(szTemp) + 1);
			strcpy(m_pFileNameList[m_ulNumOfFiles], szTemp);
			printf("File %s\n",m_pFileNameList[m_ulNumOfFiles]);
			m_ulNumOfFiles++;		
		}
	}
	return;
}

cIFSMULTI::~cIFSMULTI(void)
{
	int i;
	for (i = 0; i < (int)m_ulNumOfFiles; i++)
		free(m_pFileNameList[i]);
	if (m_pBuf)
		free(m_pBuf);
	return;
}


int IFSWorkerThread(PVOID pData)
{
	int i;
	HANDLE hFile;
	ULONG ulMemRead, ulMemWrote;
	ULONG ulDesireAccess;
	ULONG ulShareMode;
	printf("IFSMulti thread = %d\n",*(PULONG)pData);
	ulDesireAccess = g_pcIFSMulti->m_fWriteMode ? GENERIC_READ|GENERIC_WRITE : GENERIC_READ;
	ulShareMode = g_pcIFSMulti->m_fWriteMode ? FILE_SHARE_READ|FILE_SHARE_WRITE : FILE_SHARE_READ;

	for (i = 0; i < (int)g_pcIFSMulti->m_ulIterations; i++)
	{
		if ((hFile=CreateFile(g_pcIFSMulti->m_pFileNameList[i % g_pcIFSMulti->m_ulNumOfFiles], 
							  ulDesireAccess, 
							  ulShareMode,
							  NULL, OPEN_EXISTING, 
							  FILE_ATTRIBUTE_NORMAL, NULL)) != INVALID_HANDLE_VALUE)
		{
			if (!ReadFile(hFile, g_pcIFSMulti->m_pBuf, g_pcIFSMulti->m_ulBufSize, &ulMemRead, NULL))
				printf("Cannot Read File.......Thread = %d file = %s\n",
						*(PULONG)pData, g_pcIFSMulti->m_pFileNameList[i % g_pcIFSMulti->m_ulNumOfFiles]);
			else if(g_pcIFSMulti->m_fWriteMode &&
			   !WriteFile(hFile, g_pcIFSMulti->m_pBuf, min(ulMemRead, g_pcIFSMulti->m_ulBufSize), &ulMemWrote, NULL))
				printf("Cannot Write File......\n");

			CloseHandle(hFile);
		}
		else
			printf("CreateFile FAILED thread = %d\n", *(PULONG)pData);
	}

	SetEvent((HANDLE)g_pcIFSMulti->m_phThreadWaitEvents[*(PULONG)pData]);
	return 0;
}


BOOL cIFSMULTI::RunThreads()
{
	int i;
	BOOL fReturn = TRUE;
	ULONG ulThreadIDArray[256];
	HANDLE hThreadHandle[256];
	ULONG ulThreadID;

	if (m_ulNumOfFiles == 0)
	{
		printf("cIFSMULTI::RunThreads Cannot run because m_ulNumOfFiles is zero\n");
		return FALSE;
	}

	if ((m_pBuf = malloc(m_ulBufSize)) == NULL)
	{
		printf("cIFSMULTI::RunThreads<ERROR>Cannot allocate buffer\n");
		return FALSE;
	}

	m_phThreadWaitEvents = (HANDLE *)malloc(m_ulThreads * sizeof(HANDLE));

	if (m_phThreadWaitEvents == NULL)
	{
		fReturn = FALSE;
		goto RunThreadsExit0;
	}

	for (i = 0; i < (int)m_ulThreads; i++)
	{
		m_phThreadWaitEvents[i] = CreateEvent(NULL, FALSE, FALSE, NULL);
	}

	m_ulStartTime = timeGetTime();
	for (i = 0; i < (int)m_ulThreads; i++)
	{
		ulThreadIDArray[i] = i;
		hThreadHandle[i] = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)IFSWorkerThread, 
									   &ulThreadIDArray[i], 0, &ulThreadID);
	}

	WaitForMultipleObjects(m_ulThreads, m_phThreadWaitEvents, TRUE, INFINITE);
	m_ulEndTime = timeGetTime();
	printf("Time executed is %d miliseconds\n", m_ulEndTime - m_ulStartTime);
	free(m_phThreadWaitEvents);
RunThreadsExit0:
	free(m_pBuf);
	m_pBuf = NULL;
	return fReturn;
}

//*****************************************************************
// Name: printUsage
//
// Description: Function that displays the program usage
//
// Inputs:
//   None
//
// Returns:
//   Success: void
//   Failure: void
//*****************************************************************

void printUsage(void)
{
    printf("\n"
           "Usage:\n"
           "-h ................................. Display program usage\n"
           "-? ................................. Display program usage\n"
		   "-t <number of threads>.............. default is 10\n"
		   "-f <File Name>...................... default is T:\\asdf.txt\n"
		   "-l ................................. list of files in the file, default is off\n"
		   "-i <Iterations>..................... default is 5000\n"
		   "-w ................................. read/write mode, default is read only"
           );
	exit(0);
}

/*********************************************
 * mygetopt
 *
 * Windows version of UNIX function that parses
 * commandline args 
 *
 * PARAM Argc current arg number
 * PARAM Argv array of command line args
 * PARAM Opts list of valid arguments and whether they take parameters
 *
 * RETURN the next argument
 *********************************************/
WCHAR mygetopt (ULONG Argc, char* Argv[], char* Opts)
{
    static ULONG  optind=1;
    static ULONG  optcharind;
    static ULONG  hyphen=0;


    char  ch;
    char* indx;

    do {
        if (optind >= Argc) {
            return EOF;
        }

        ch = Argv[optind][optcharind++];
         //printf("optind=%d, char=%c\n", optind, ch);
        if (ch == '\0') {
            optind++; optcharind=0;
            hyphen = 0;
            continue;
        }

        if ( hyphen || (ch == '-') || (ch == '/')) {
            if (!hyphen) {
                ch = Argv[optind][optcharind++];
                if (ch == '\0') {
                    optind++;
                    return EOF;
                }
            } else if (ch == '\0') {
                optind++;
                optcharind = 0;
                continue;
            }
            indx = strchr(Opts, ch);
            if (indx == NULL) {
                continue;
            }
            if (*(indx+1) == ':') {
                if (Argv[optind][optcharind] != '\0'){
                    myoptarg = &Argv[optind][optcharind];
                } else {
                    if ((optind + 1) >= Argc ||
                        (Argv[optind+1][0] == '-' ||
                         Argv[optind+1][0] == '/' )) {
                        return 0;
                    }
                    myoptarg = Argv[++optind];
                }
                optind++;
                hyphen = optcharind = 0;
                //printf("optind=%d, char=%c\n", optind, ch);
                 return ch;
            }
            hyphen = 1;
           return ch;
        } else {
            return EOF;
        }
    } while (1);
}

/*********************************************
 * get_parameters
 *
 * parses command line arguments setting appropriate
 * values.
 *
 * PARAM argc - number of command line arguments
 * PARAM argv - command line arguments
 *
 *********************************************/
void get_parameters(int argc, char* argv[])
{
    WCHAR c, eof;
	char *stopString;
	ULONG ulThread = 0;
    eof = EOF;

    //a list of default attributes.
	while ((c = mygetopt(argc, argv, "?ht:i:f:wl")) != eof)
    {
        switch (c)
        {
		case '?':
        case 'h':
			printUsage();
            break;
		case 't':
			if ((gulNumThreads = (ULONG)strtol(myoptarg, &stopString, 0)) > 50) {
				printf("ERROR number of threads cannot exceed 50\n");
				exit(1);
			}			
			break;
		case 'f':
			strcpy(gszFile, myoptarg);
			break;
		case 'i':
			gulIterations = (ULONG)strtol(myoptarg, &stopString, 0);
			break;
		case 'w':
			gfWriteMode = TRUE;
			break;
		case 'l':
			gfListFileMode = TRUE;
			break;
        default:
            printf("ERROR unknown option: %x\n",c);
            exit(1);
        }
    }
}

int _tmain(int argc, _TCHAR* argv[])
{
	get_parameters(argc, argv);
	g_pcIFSMulti = new cIFSMULTI;
	g_pcIFSMulti->RunThreads();
	delete g_pcIFSMulti;
	//printf("Press Q to quit application\n");
	//GetUserInput();	
	return 0;
}

