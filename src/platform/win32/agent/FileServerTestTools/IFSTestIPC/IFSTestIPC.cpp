// IFSTestIPC.cpp : Defines the entry point for the console application.
//
#include "stdafx.h"
#include <stdio.h>
#include <windows.h>
#include <winioctl.h>
#include <tchar.h>
#define SECURITY_WIN32
#include <sddl.h>
#include "dsifsinterface.h"
#include "dsifsioctls.h"
#include "dsipc.h"
#include "actions.h"

#define MAX_THREADS		50

//global variable used by mygetopt function and get_parameters
static char* optarg;

//global variables for program
ULONG	gulNumThreads = 10;
ULONG	gulSlotSize = 8192;
ULONG	gulIterations = 5000;
BOOL	gfExitThread = FALSE;

class cIFSIPC
{
friend IFSWorkerThread(PVOID pData);

public:		
	~cIFSIPC();
	cIFSIPC(ULONG ulNumThreads, ULONG ulSlotSize, ULONG ulIterations);
	BOOL InitAndRun(void);	
private:
	IPC_SETUP_INFO	m_strIPCInfo;
	HANDLE  m_hThreads[MAX_THREADS];
	HANDLE	m_hIFSDriver;
	ULONG	m_ulIterations;
};

cIFSIPC *gpIFSIPC = NULL;

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

void PrintAction(ULONG ulAction)
{
	switch (ulAction)
	{	
		case OPEN_READ_ACTION: printf("(OPEN_READ_ACTION)\n");break;  
		case OPEN_READ_WRITE_ACTION: printf("(OPEN_READ_WRITE_ACTION)\n");break;
		case DELETE_ACTION: printf("(DELETE_ACTION)\n");break;         
		case READ_ACTION: printf("(READ_ACTION)\n");break;           
		case WRITE_ACTION: printf("(WRITE_ACTION)\n");break;          
		case CLOSE_ACTION: printf("(CLOSE_ACTION)\n");break;          
		case RENAME_ACTION: printf("(RENAME_ACTION)\n");break;         
		case CREATE_NEW_ACTION: printf("(CREATE_NEW_ACTION)\n");break;     
		case CHANGE_PROPERTIES_ACTION: printf("(CHANGE_PROPERTIES_ACTION)\n");break;
		case CHANGE_SECURITY_ACTION: printf("(CHANGE_SECURITY_ACTION)\n");break;  
		case SAVE_AS_ACTION: printf("(SAVE_AS_ACTION)\n");break;        
		case SEND_IM_ACTION: printf("(SEND_IM_ACTION)\n");break;           
		case CUT_PASTE_ACTION: printf("(CUT_PASTE_ACTION)\n");break;        
		case COPY_PASTE_ACTION: printf("(COPY_PASTE_ACTION)\n");break;       
		case BATCH_ACTION: printf("(BATCH_ACTION)\n");break;            
		case BURN_ACTION: printf("(BURN_ACTION)\n");break;             
		case PRINT_ACTION: printf("(PRINT_ACTION)\n");break;            
		case COPY_ACTION: printf("(COPY_ACTION)\n");break;             
		case MOVE_ACTION: printf("(MOVE_ACTION)\n");break;             
		case SHARE_ACTION: printf("(SHARE_ACTION)\n");break;            
		case EMAIL_ACTION: printf("(EMAIL_ACTION)\n");break;            
		case OPEN_DIR_ACTION: printf("(OPEN_DIR_ACTION)\n");break;		
		case EMBED_ACTION: printf("(EMBED_ACTION)\n");break;           
		default: printf("UNKNOWN_ACTION\n");break;
	}
	return;
}

int IFSWorkerThread(PVOID pData)
{
	PIPC_SLOT pIPCSlot = (PIPC_SLOT)pData;
	PIPC_POLICY_REQUEST	pIPCPolicy;
        TCHAR* pszUSID = NULL;

	if (!pIPCSlot)
	{
		fprintf(stderr,"IFSTESTIPC.EXE:IFSWorkerThread<ERROR>pData is NULL\n");
		return 1;
	}

	printf("Thread %d spawned...\n",pIPCSlot->ulSlotNumber);
	pIPCPolicy = (PIPC_POLICY_REQUEST)((PBYTE)gpIFSIPC->m_strIPCInfo.pShareMem +
										(pIPCSlot->ulSlotNumber * gpIFSIPC->m_strIPCInfo.ulSlotSize));

	while(TRUE)
	{
		WaitForSingleObject((HANDLE)pIPCSlot->pRecvEvent, INFINITE);
		printf("Thread %d got unblock...\n", pIPCSlot->ulSlotNumber);
		if (gfExitThread)
		{
			printf("Thread %d terminate...\n", pIPCSlot->ulSlotNumber);
			return 0;
		}

                if (pIPCPolicy->bIgnoreObligations)
                {
                    printf("**************IGNORE*******************");
                }
		//print everything else
		printf("ulAction = 0x%x ", pIPCPolicy->ulAction);
		PrintAction(pIPCPolicy->ulAction);
		printf("ulSize = %d szwMethodName = %S\n",pIPCPolicy->ulSize, pIPCPolicy->szwMethodName);
		printf("FileName = %S\n", pIPCPolicy->szwFileName);
                if (ConvertSidToStringSid((PSID) pIPCPolicy->strFile.aUserSID, &pszUSID))
                {
                    printf("SID = %s\n", pszUSID);
                    LocalFree (pszUSID);
                }
                else
                {
                    printf ("SID: ?????????\n");
                }
		printf("RemoteIP = 0x%x  TID = 0x%x  PID = 0x%x  ",pIPCPolicy->ulRemoteAddress, pIPCPolicy->ulTID, pIPCPolicy->ulPID);

		if (wcsstr(pIPCPolicy->szwFileName, L"donttouch.txt"))
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		} 
		else if (wcsstr(pIPCPolicy->szwFileName, L"noread.txt") && pIPCPolicy->ulAction == OPEN_READ_ACTION)
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}
		else if (wcsstr(pIPCPolicy->szwFileName, L"readonly.txt") && pIPCPolicy->ulAction == OPEN_READ_WRITE_ACTION)
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}
		else if (wcsstr(pIPCPolicy->szwFileName, L"norename.txt") && pIPCPolicy->ulAction == RENAME_ACTION)
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}
		else if (wcsstr(pIPCPolicy->szwFileName, L"nodelete.txt") && pIPCPolicy->ulAction == DELETE_ACTION)
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}
		else if (wcsstr(pIPCPolicy->szwFileName, L"noproperty.txt") && pIPCPolicy->ulAction == CHANGE_PROPERTIES_ACTION)
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}
		else if (wcsstr(pIPCPolicy->szwFileName, L"nosecurity.txt") && pIPCPolicy->ulAction == CHANGE_SECURITY_ACTION)
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}
		else if (wcsstr(pIPCPolicy->szwFileName, L"nonew.txt") && pIPCPolicy->ulAction == CREATE_NEW_ACTION)
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}
		else if (wcsstr(pIPCPolicy->szwFileName, L"nodir") && pIPCPolicy->ulAction == OPEN_DIR_ACTION)
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}
		else if ( wcsstr(pIPCPolicy->szwFileName, L"nowrite.txt") && 
			      (pIPCPolicy->ulAction == WRITE_ACTION || pIPCPolicy->ulAction == RENAME_ACTION || 
				   pIPCPolicy->ulAction == DELETE_ACTION || pIPCPolicy->ulAction == CLOSE_ACTION ||
				   pIPCPolicy->ulAction == CHANGE_SECURITY_ACTION || pIPCPolicy->ulAction == CHANGE_PROPERTIES_ACTION) ) 
		{
			pIPCPolicy->ulAllow = DENY;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}
		else
		{
			pIPCPolicy->ulAllow = ALLOW;
			pIPCPolicy->ulAllowType = WATCH_NEXT_OP;
		}

		if (pIPCPolicy->ulAllow == ALLOW) printf("  ALLOW\n\n"); else printf("   DENY\n\n");
                fflush (stdout);

		SetEvent((HANDLE)pIPCSlot->pSendEvent);
	}
	return 0;
}


cIFSIPC::cIFSIPC(ULONG ulThreads, ULONG ulSlotSize, ULONG ulIterations)
{
	m_hIFSDriver = NULL;
	m_ulIterations = ulIterations;
	m_strIPCInfo.ulNumSlot = ulThreads;
	m_strIPCInfo.ulSlotSize = ulSlotSize;
	m_strIPCInfo.ulMemSize = ulSlotSize * ulThreads;
	memset ((void *)m_hThreads, 0, MAX_THREADS * sizeof(HANDLE));
}

cIFSIPC::~cIFSIPC()
{
	int i;
	ULONG ulBytesReturn;
	ULONG ulExitThreadCode;
	PIPC_SLOT pIPCSlot;

	if (m_hIFSDriver != INVALID_HANDLE_VALUE)
	{
		DeviceIoControl(m_hIFSDriver, BJ_IFS_DEINIT_KERNEL_IPC, NULL, 0, NULL, 0, &ulBytesReturn, NULL);
		CloseHandle(m_hIFSDriver);
	}

	pIPCSlot = (PIPC_SLOT)m_strIPCInfo.pIPCSlot;
	gfExitThread = TRUE;
	if ( pIPCSlot )
	{
		for (i = 0; i < (int)m_strIPCInfo.ulNumSlot; i++)
		{
			SetEvent((HANDLE)pIPCSlot->pRecvEvent);
			SetEvent((HANDLE)pIPCSlot->pSendEvent);
			//Nah, get lazy here
			Sleep(10);	
			CloseHandle((HANDLE)pIPCSlot->pRecvEvent);
			CloseHandle((HANDLE)pIPCSlot->pSendEvent);
			pIPCSlot++;
			if (GetExitCodeThread(m_hThreads[i],&ulExitThreadCode))
				TerminateThread(m_hThreads[i],ulExitThreadCode);
			else
				fprintf(stderr,"IFSIPC::~IFSIPC<ERROR>Cannot get thead %d exit code LastError0x%x\n",i,GetLastError());
			CloseHandle(m_hThreads[i]);
		}
	}
	if (m_strIPCInfo.pIPCSlot)
		free(m_strIPCInfo.pIPCSlot);
	if (m_strIPCInfo.pShareMem)
		free(m_strIPCInfo.pShareMem);
	return;
}

BOOL cIFSIPC::InitAndRun()
{
	int			i;	
	ULONG		ulThreadID;
	ULONG		ulBytesReturn;
	PIPC_SLOT	pIPCSlots;
    m_hIFSDriver = CreateFileW( DSIFS_W32_DEVICE_NAME,
								GENERIC_READ | GENERIC_WRITE,
								0,
								NULL,
								OPEN_EXISTING,
								FILE_ATTRIBUTE_NORMAL,
								NULL);
	if (m_hIFSDriver == NULL)
	{
		fprintf(stderr,"IFSIPC::Initialize<ERROR>Cannot connect to IFS Driver\n");
		return FALSE;
	}

	if ((m_strIPCInfo.pShareMem = malloc(m_strIPCInfo.ulNumSlot * m_strIPCInfo.ulSlotSize)) == NULL)
	{
		fprintf(stderr,"IFSIPC::Initialize<ERROR>Cannot allocate shared memory\n");
		goto InitializeError1;
	}

	if ((m_strIPCInfo.pIPCSlot = malloc(m_strIPCInfo.ulNumSlot * sizeof(IPC_SLOT))) == NULL)
	{
		fprintf(stderr,"IFSIPC::Initialize<ERROR>Cannot allocate memory for shared events\n");
		goto InitializeError2;
	}
	pIPCSlots = (PIPC_SLOT)m_strIPCInfo.pIPCSlot;
	for (i = 0; i < (int)m_strIPCInfo.ulNumSlot; i++)
	{
		//Let someone else cleanup this code with defensive programming.  
		pIPCSlots->pRecvEvent = (PVOID)CreateEvent(NULL, FALSE, FALSE, NULL);
		pIPCSlots->pSendEvent = (PVOID)CreateEvent(NULL, FALSE, FALSE, NULL);
		pIPCSlots->ulSlotNumber = (ULONG)i;
		pIPCSlots++;
	}

	pIPCSlots = (PIPC_SLOT)m_strIPCInfo.pIPCSlot;

	for (i = 0; i < (int)m_strIPCInfo.ulNumSlot; i++)
	{
		m_hThreads[i] = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)IFSWorkerThread, 
									 (PVOID)pIPCSlots, 0, &ulThreadID);
		if (m_hThreads[i] == NULL)
			fprintf(stderr,"IFSIPC::Initialize:CreateThread error, thread=%d LastError=0x%x\n",i,GetLastError());
		pIPCSlots++;
	}
	m_strIPCInfo.ulInstalledDirLength;
	memset(m_strIPCInfo.wzInstalledDir, 0, MAX_NAME_LENGTH * 2);

	if ((m_strIPCInfo.ulInstalledDirLength = 
		GetCurrentDirectoryW(MAX_NAME_LENGTH, m_strIPCInfo.wzInstalledDir)) == 0)
	{
		fprintf(stderr,"GetCurrentDirectoryW FAILED, Last Error = 0x%x\n", GetLastError());
	}

        m_strIPCInfo.ulShortInstalledDirLength = 0;
	memset(m_strIPCInfo.wzShortInstalledDir, 0, MAX_NAME_LENGTH * 2);
         
	if ((m_strIPCInfo.ulShortInstalledDirLength = 
		GetShortPathNameW (m_strIPCInfo.wzInstalledDir, m_strIPCInfo.wzShortInstalledDir, MAX_NAME_LENGTH)) == 0)
	{
		fprintf(stderr,"GetShortPathName FAILED, Last Error = 0x%x\n", GetLastError());
	}

	m_strIPCInfo.ulProcessID = ::GetCurrentProcessId();

	//Lazy here, sleep a bit to let the threads all started before
	//calling the kernel mode driver.  Some rookies can fix this.
	Sleep(10);

	if (!DeviceIoControl(m_hIFSDriver, BJ_IFS_INIT_KERNEL_IPC, 
						 &m_strIPCInfo, sizeof(IPC_SETUP_INFO),
						 &m_strIPCInfo, sizeof(IPC_SETUP_INFO),
						 &ulBytesReturn, NULL))
	{
		fprintf(stderr,"IFSIPC::Initialize:Calling BJ_IFS_INIT_KERNEL_IPC failed LastError=0x%x\n",GetLastError());
		goto InitializeError3;
	}

	return TRUE;
InitializeError3:
	free(m_strIPCInfo.pIPCSlot);
	m_strIPCInfo.pIPCSlot = NULL;
InitializeError2:
	free(m_strIPCInfo.pShareMem);
	m_strIPCInfo.pShareMem = NULL;
InitializeError1:
	CloseHandle(m_hIFSDriver);
	m_hIFSDriver = INVALID_HANDLE_VALUE;
	return FALSE;
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

void PrintUsage(void)
{
    printf("\n"
           "Usage:\n"
           "-h ................................. Display program usage\n"
           "-? ................................. Display program usage\n"
		   "-t <number of threads>.............. default is 10\n"
		   "-i <Iterations>..................... default is 5000\n"
		   "-s <Slot Size .......................default is 512"
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
                    optarg = &Argv[optind][optcharind];
                } else {
                    if ((optind + 1) >= Argc ||
                        (Argv[optind+1][0] == '-' ||
                         Argv[optind+1][0] == '/' )) {
                        return 0;
                    }
                    optarg = Argv[++optind];
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
			PrintUsage();
            break;
		case 't':
			if ((gulNumThreads = (ULONG)strtol(optarg, &stopString, 0)) > MAX_THREADS) {
				printf("ERROR number of threads cannot exceed 50\n");
				exit(1);
			}			
			break;
		case 'i':
			gulIterations = (ULONG)strtol(optarg, &stopString, 0);
			break;
		case 's':
			gulSlotSize = (ULONG)strtol(optarg, &stopString, 0);
			break;
        default:
            printf("ERROR unknown option: %x\n",c);
            exit(1);
        }
    }
}
#ifdef _WIN64
int main(int argc, char *argv[])
#else	//WIN32
int _tmain(int argc, _TCHAR* argv[])
#endif
{
    int retVal = 0;
	get_parameters(argc, argv);
	gpIFSIPC = new cIFSIPC(gulNumThreads, gulSlotSize, gulIterations);
	
	if (!gpIFSIPC)
	{
		fprintf(stderr,"Cannot allocate new cIFSIPC class, program terminate\n");
		return 1;
	}

	if (gpIFSIPC->InitAndRun() == FALSE)
	{
		fprintf(stderr,"Cannot initialize cIFSIPC, program terminate\n");
		delete gpIFSIPC;
		return 1;
	}

	printf("Program running...\n");
	printf("Please type Q and enter to stop program\n");

	GetUserInput();

	delete gpIFSIPC;

	return retVal;
}


