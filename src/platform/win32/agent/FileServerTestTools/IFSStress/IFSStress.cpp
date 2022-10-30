// IFSStress.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <windows.h>
#include <io.h>
#include <time.h>
#include <winbase.h>

#define MAX_STRING	256
#define  MAX_FILES	256
#define WRITE_STRING 25

LONG  gulBufSize = 1024;
ULONG gulNumThreads = 10;
ULONG gulIterations = 1000;
ULONG gulNumFiles = 0;
ULONG gulDelay = 200;
ULONG gulCollide = 0;
ULONG gulSuccessfulRead = 0;
ULONG gulSuccessfulWrite = 0;
ULONG gulOpenFileFail = 0;
char  gszDir[MAX_STRING] = "";
char  *gpszFileList;
char  gszFile[MAX_STRING] = "";
BOOL  gfReadMode = TRUE;
BOOL  gfWriteMode = FALSE;
BOOL  gfListFileMode = FALSE;
BOOL  gfFlushMode = FALSE;
BOOL  gfDirExist = FALSE;
BOOL  *gpfFileLocks;


//global variable used by mygetopt function and get_parameters
static char* myoptarg;

class cIFSStress
{
	friend IFSWorkerThread(PVOID pData);
	friend FileOperation(ULONG openattempt, PVOID pData, PVOID pBuf, char *szThreadID, int iIter);
public:
	BOOL RunThreads(void);
	cIFSStress();
	~cIFSStress();
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
cIFSStress *g_pcIFSStress = NULL;

cIFSStress::cIFSStress(void)
{
	m_fWriteMode = gfWriteMode;
	m_ulThreads = gulNumThreads;
	m_ulIterations = gulIterations;
	m_ulBufSize = gulBufSize;
	m_pBuf = NULL;
	memset(m_pFileNameList, 0, MAX_FILES);
	m_ulNumOfFiles = 0;
    m_phThreadWaitEvents = NULL;
	m_ulStartTime = 0;
	m_ulEndTime = 0;
}

cIFSStress::~cIFSStress(void)
{
	int i;
	for (i = 0; i < (int)m_ulNumOfFiles; i++)
		free(m_pFileNameList[i]);
	if (m_pBuf)
		free(m_pBuf);
	return;
}

//*****************************************************************
// Name: FileOperation
//
// Description: Each thread will call this function during each 
//              iteration to perform file read/write
//
// Inputs:
//   int numfiles
//
// Returns:
//   Success: void
//   Failure: void
//*****************************************************************
int FileOperation(ULONG openattempt, PVOID pData, PVOID pBuf, char *szThreadID, int iIter){
	
	ULONG	ulOpenAttempt = openattempt;
	ULONG	ulRandomFile;
	ULONG	ulMemRead, ulMemWrote;
	HANDLE	hFile;
	ULONG	ulDesireAccess = g_pcIFSStress->m_fWriteMode ? GENERIC_READ|GENERIC_WRITE : GENERIC_READ;
	ULONG	ulShareMode = g_pcIFSStress->m_fWriteMode ? FILE_SHARE_READ|FILE_SHARE_WRITE : FILE_SHARE_READ;
	BOOL	fObtainFile = FALSE; //this is used to keep track of if the thread obtained the fil
	char	szWriteString[WRITE_STRING]; //this is the buffer used to write ID to file.
	DWORD	dw = 0;
	TCHAR	szBuf[80]; 
    LPVOID	lpMsgBuf = NULL;

	for (int i = 0; i < (int)openattempt; i++){

		//reset the string to write to the file
		memset(szWriteString, '\0', sizeof(szWriteString));
		sprintf(szWriteString, "thread %d iteration %d", *(PULONG)pData, iIter);

		//generate a random file number
		srand((unsigned)timeGetTime());	
		ulRandomFile = (rand()%gulNumFiles);
		
		fObtainFile = FALSE;
		//need to obatin the lock first
		if (InterlockedCompareExchangePointer((PVOID *)&gpfFileLocks[ulRandomFile],
			(PVOID)TRUE, (PVOID)fObtainFile)){
			//did not get the lock
			printf("file %d is locked by another thread, cannot read/write to it\n", ulRandomFile);
			gulCollide++;
			Sleep(10); //delay so that the random number seed will be different.
		} else {
			fObtainFile = TRUE;
			if ((hFile=CreateFile((gpszFileList+ulRandomFile*MAX_STRING), 
								  ulDesireAccess, 
								  ulShareMode,
								  NULL, OPEN_EXISTING, 
								  FILE_ATTRIBUTE_NORMAL, NULL)) != INVALID_HANDLE_VALUE)
			{
				if (gfReadMode)
				{
					if (ReadFile(hFile, pBuf, g_pcIFSStress->m_ulBufSize, &ulMemRead, NULL))
					{
						gulSuccessfulRead++;
						if(g_pcIFSStress->m_fWriteMode)
						{
							SetFilePointer(hFile, 0, NULL, FILE_BEGIN);
							if (!WriteFile(hFile, pBuf,
								min(ulMemRead, g_pcIFSStress->m_ulBufSize) /*sizeof(szWriteString)*/,
								&ulMemWrote, NULL))
							{
								printf("Cannot Write File......\n");
							}
							gulSuccessfulWrite++;
						}
					}
					else 
					{
						printf("Cannot Read File.......Thread = %d file = %s\n", *(PULONG)pData,
							gpszFileList+ulRandomFile*MAX_STRING);
					}
				}
				//delay before releasing the lock
				Sleep(gulDelay*10);
				CloseHandle(hFile);

				InterlockedExchangePointer((PVOID *)&gpfFileLocks[ulRandomFile], (PVOID)FALSE);
				break;
			}
			else 
			{
				printf("CreateFile FAILED thread = %d file = %s\n",
					*(PULONG)pData, gpszFileList+ulRandomFile*MAX_STRING);
				dw = GetLastError();
				FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
					NULL,
					dw,
					MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
					(LPTSTR) &lpMsgBuf,
					0, NULL );
				printf("failed with error %d: %s", dw, lpMsgBuf); 
				fObtainFile = FALSE;
				gulOpenFileFail++;
			}
		}
	}
	return 0;
}

int IFSWorkerThread(PVOID pData)
{
	PVOID	pBuf = NULL;
	char	*szThreadID = (char *)pData;
	
	if ((pBuf = malloc(g_pcIFSStress->m_ulBufSize)) == NULL){
		printf("cIFSStress::FileOperation<ERROR>Cannot allocate buffer\n");
		return FALSE;
	}

	for (int i = 0; i < (int)g_pcIFSStress->m_ulIterations; i++){
		memset(pBuf, '\0', sizeof(pBuf));
		FileOperation(gulNumFiles, pData, pBuf, szThreadID, i);
	}

	SetEvent((HANDLE)g_pcIFSStress->m_phThreadWaitEvents[*(PULONG)pData]);
	return 0;
}


BOOL cIFSStress::RunThreads()
{	
	BOOL fReturn = TRUE;
	ULONG ulThreadIDArray[256];
	HANDLE hThreadHandle[256];
	ULONG ulThreadID;

	if ((m_phThreadWaitEvents = (HANDLE *)malloc(m_ulThreads * sizeof(HANDLE))) == NULL){
		fReturn = FALSE;
		goto RunThreadsExit0;
	}

	for (int i = 0; i < (int)m_ulThreads; i++)
	{
		m_phThreadWaitEvents[i] = CreateEvent(NULL, FALSE, FALSE, NULL);
	}

	m_ulStartTime = timeGetTime();
	for (int i = 0; i < (int)m_ulThreads; i++)
	{
		ulThreadIDArray[i] = i;
		hThreadHandle[i] = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)IFSWorkerThread, 
									   &ulThreadIDArray[i], 0, &ulThreadID);
	}

	WaitForMultipleObjects(m_ulThreads, m_phThreadWaitEvents, TRUE, INFINITE);
	m_ulEndTime = timeGetTime();
	printf("Time executed is %d miliseconds\n", m_ulEndTime - m_ulStartTime);
	free(m_phThreadWaitEvents);
	fReturn = TRUE;
RunThreadsExit0:
	free(m_pBuf);
	m_pBuf = NULL;
	return fReturn;
}

//*****************************************************************
// Name: CreateLocks
//
// Description: Creates a list of locks, one for each file
//              
//
// Inputs:
//   char *dirname
//
// Returns:
//   Success: void
//   Failure: void
//*****************************************************************
void CreateLocks(void) {
	
	gpfFileLocks = (BOOL *)malloc(gulNumFiles * sizeof(BOOL));
	
	// populate each entry with false
	for (int i = 0; i < (int)gulNumFiles; i++){
		gpfFileLocks[i] = FALSE;
	}
}

//*****************************************************************
// Name: ScanDir
//
// Description: Scans and lists all files in a directory and its 
//              subdirectories
//
// Inputs:
//   char *dirname
//
// Returns:
//   Success: void
//   Failure: void
//*****************************************************************

void ScanDir(char *dirname) {
    
	BOOL            fFinished;
    HANDLE          hList;
    TCHAR           szDir[MAX_PATH+1];
    TCHAR           szSubDir[MAX_PATH+1];
    WIN32_FIND_DATA FileData;
	ULONG			ulFileIndex = 0;

    // Get the proper directory path
    sprintf(szDir, _T("%s\\*"), dirname);

    // Get the first file
    hList = FindFirstFile(szDir, &FileData);
	gpszFileList = (char *)malloc(gulNumFiles * MAX_STRING);
    if (hList == INVALID_HANDLE_VALUE) { 
        printf("No files found\n\n");
		exit(1);
    }
    else {
		// Traverse through the directory structure
        fFinished = FALSE;
        while (!fFinished) {
            // Check the object is a directory or not
            if (FileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
				if ((strcmp(FileData.cFileName, ".") != 0) &&
					(strcmp(FileData.cFileName, "..") != 0)) {
                    // Get the full path for sub directory
                    sprintf(szSubDir, "%s\\%s", dirname, FileData.cFileName);
                    ScanDir(szSubDir);
                }
            }
			else { // need to read the file name into the global pointer gszFileList
				sprintf((gpszFileList + ulFileIndex*MAX_STRING), "%s\\%s \0", dirname, FileData.cFileName);
				ulFileIndex++;
			}
            if (!FindNextFile(hList, &FileData)) {
                if (GetLastError() == ERROR_NO_MORE_FILES) {
                    fFinished = TRUE;
                }
            }
        }
    }
    FindClose(hList);
}

//*****************************************************************
// Name: CountFiles
//
// Description: Counts the number of files 
//
// Inputs:
//   char *dirname
//
// Returns:
//   Success: void
//   Failure: void
//*****************************************************************

void CountFiles(char *dirname) {
    
	BOOL            fFinished;
    HANDLE          hList;
    TCHAR           szDir[MAX_PATH+1];
    TCHAR           szSubDir[MAX_PATH+1];
    WIN32_FIND_DATA FileData;

    // Get the proper directory path
    sprintf(szDir, _T("%s\\*"), dirname);

    // Get the first file
    hList = FindFirstFile(szDir, &FileData);
    if (hList == INVALID_HANDLE_VALUE) { 
        printf("No files found\n\n");
		exit(1);
    }
    else {
		// Traverse through the directory structure
        fFinished = FALSE;
        while (!fFinished) {
            // Check the object is a directory or not
            if (FileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {	
				if ((strcmp(FileData.cFileName, ".") != 0) &&
					(strcmp(FileData.cFileName, "..") != 0)) {
                    ScanDir(szSubDir);
                }
            }
			else { 
				gulNumFiles++;
			}
            if (!FindNextFile(hList, &FileData)) {
                if (GetLastError() == ERROR_NO_MORE_FILES) {
                    fFinished = TRUE;
                }
            }
        }
    }
    FindClose(hList);
	ScanDir(dirname);
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
		   "-f ................................. flush mode, default is off\n"
		   "-i <Iterations>..................... default is 1000\n"
		   "-w ................................. read/write mode, default is read only\n"
		   "-d <Directory>...................... This option is mandatory\n"
		   "-y <num of centiseconds>............ Delay time\n"
		   "-b <size of buffer>................. default is 1024\n"
           );
	exit(0);
}

//*****************************************************************
// Name: printStat
//
// Description: Function that displays some of the statistics
//
// Inputs:
//   None
//
// Returns:
//   Success: void
//   Failure: void
//*****************************************************************

void printStat(void)
{
    printf("\n"
           "Stats:\n"
           "Successful Reads  -  %d\n"
           "Successful Writes -  %d\n"
		   "Open File Failure -  %d\n"
		   "Collisions        -  %d\n", gulSuccessfulRead, gulSuccessfulWrite, gulOpenFileFail,
		   gulCollide);
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
	char szBufSize[MAX_STRING] = "";

    //a list of default attributes.
	while ((c = mygetopt(argc, argv, "?ht:i:fwd:y:b:")) != eof)
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
			if (!gulNumThreads){
				printf("improper use of option t\n");
				exit(1);
			}
			printf("number of threads = %d\n", gulNumThreads);
			break;
		case 'f':
			gfFlushMode = TRUE;
			printf("flush mode is turned on\n");
			break;
		case 'i':
			if (!(gulIterations = (ULONG)strtol(myoptarg, &stopString, 0))){
				printf("improper use of option i\n");
				exit(1);
			}
			printf("number of iterations = %d\n", gulIterations);
			break;
		case 'w':
			printf("write mode = true\n");
			gfWriteMode = TRUE;
			break;
		case 'd':
			strcpy(gszDir, myoptarg);
			printf("directory is %s\n", gszDir);
			gfDirExist = TRUE;
			break;
		case 'y':
			if (!(gulDelay = (ULONG)strtol(myoptarg, &stopString, 0))){
				printf("improper use of option y\n");
				exit(1);
			}
			printf("delay is %d centiseconds\n", gulDelay);
			break;
		case 'b':
			strcpy(szBufSize, myoptarg);
			if (szBufSize[0] == '0'){
				gulBufSize = 0;
			} else{
				if (!(gulBufSize = strtol(myoptarg, &stopString, 0))){
					printf("improper use of option b\n");
					exit(1);
				}
			}	
			printf("buffer size changed to %d\n", gulBufSize);
			break;
        default:
            printf("ERROR unknown option: %c\n",c);
            exit(1);
        }
    }
}

int _tmain(int argc, _TCHAR* argv[])
{
	get_parameters(argc, argv);

	if (!gfDirExist){
		printf("must supply a directory using -d option\n");
		exit(1);
	}

	CountFiles(gszDir);
	
	/*
	if (gulNumThreads > gulNumFiles){
		printf("ERROR: number of threads %d is greater than the number of files %d\n", 
				gulNumThreads, gulNumFiles);
		exit(1);
	}
	*/
	
	for (int i = 0; (ULONG)i < gulNumFiles; i++){
		printf("files inside the buffer: %s\n", (gpszFileList+i*MAX_STRING));
	}
	
	if (gulBufSize <= 0){
		gfReadMode = FALSE;
		gfWriteMode = FALSE;
	}

	CreateLocks();
	
	g_pcIFSStress = new cIFSStress;
	g_pcIFSStress->RunThreads();
	delete g_pcIFSStress;

	printStat();
	printf("This is a placeholder: Program executed successfully\n");
	return 0;
}

