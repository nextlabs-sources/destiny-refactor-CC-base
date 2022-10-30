/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
* Redwood City, CA.
* Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
*    
* @author Sasha Vladimirov
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/platform/win32/agent/FileServerTestTools/ModifiedShasa/MadUser.cpp#1 $
*/ 

#include "stdafx.h"

using namespace std;

#define USAGE " directory reqs_per_sec"
#define NUMREPEATS 1

#define CM "C:\\batch\\agent.bat"
#define CM_ARGS ""

#define NUMTHREADS 3
#define NUM_RUNS 1

#define REQS_PER_FILE 3

#define READ_SIZE 8192

//#define DEBUG 1

typedef vector<string> stringVector;


long getSize(string filename) {
	size_t sizeStart = filename.find("_", filename.rfind("\\"));
	size_t sizeEnd = filename.find("_", sizeStart + 1);
	string sizeStr = filename.substr(sizeStart + 1, sizeEnd-sizeStart-1);
	return atol(sizeStr.c_str());
}


ULONGLONG makeSane(const FILETIME ft) {
	return ((ULONGLONG) ft.dwHighDateTime << 32) | ft.dwLowDateTime;
}

HANDLE hProcess = NULL;
LARGE_INTEGER frequency;
LONGLONG counterPeriod;

ULONGLONG cycles[NUMTHREADS];
LPCSTR *filenames;

size_t checkNumReqs;

// lock to use around checkNumReqs
CRITICAL_SECTION cnrLock;

size_t filesPerThread;
char bitBucket[READ_SIZE];

DWORD WINAPI exercise(LPVOID arg) {
	int threadNum = (int) arg;
	LPCSTR *myNames = filenames + (threadNum * filesPerThread);

	FILETIME startTime, endTime;
	FILETIME kernelTimeBefore, userTimeBefore;
	FILETIME kernelTimeAfter, userTimeAfter;

	if (hProcess != NULL) {
		//	HANDLE hProcess = OpenProcess ( PROCESS_QUERY_INFORMATION, false, _getpid ( ) );
		BOOL bBefore = GetProcessTimes(hProcess, &startTime, &endTime, &kernelTimeBefore, &userTimeBefore);

		for (size_t i = 0; i < filesPerThread; i++) {
			HANDLE hFile = CreateFile(myNames[i], FILE_WRITE_DATA, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
			if (hFile == INVALID_HANDLE_VALUE)
			{
				printf("exercise<ERROR>First CreateFile Failed LastError = 0x%x\n", GetLastError());
			}
			else
			{
				CloseHandle(hFile);
			}
		}
		BOOL bAfter = GetProcessTimes(hProcess, &startTime, &endTime, &kernelTimeAfter, &userTimeAfter);

		if (!bBefore || !bAfter) {
			cerr << "error getting thread times" << endl;
			exit(-1);
		}

		ULONGLONG kBefore = makeSane(kernelTimeBefore);
		ULONGLONG kAfter = makeSane(kernelTimeAfter);	


		ULONGLONG uBefore = makeSane(userTimeBefore);
		ULONGLONG uAfter = makeSane(userTimeAfter);

	}

	LARGE_INTEGER periodStart, periodEnd;
	LARGE_INTEGER before, after;
	DWORD bytesRead = 0;

	LONGLONG thisPeriod = 0;
	QueryPerformanceCounter(&periodStart);
	for (size_t i = 0; i < filesPerThread; i++) {
		QueryPerformanceCounter(&periodEnd);
		thisPeriod = periodEnd.QuadPart - periodStart.QuadPart;
		while (thisPeriod < counterPeriod) {
			Sleep(1);
			QueryPerformanceCounter(&periodEnd);
			thisPeriod = periodEnd.QuadPart - periodStart.QuadPart;
		}
		QueryPerformanceCounter(&before);
		HANDLE hFile = CreateFile(myNames[i], FILE_READ_DATA, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
		if (hFile != INVALID_HANDLE_VALUE)
		{
			if (ReadFile(hFile, bitBucket, READ_SIZE, &bytesRead, NULL) == FALSE)
				printf("exercise<ERROR>ReadFile Failed LastError = 0x%x\n",GetLastError());
			if (CloseHandle(hFile) == 0)
				printf("exercise<ERROR>CloseHandle Failed LastError = 0x%x\n",GetLastError());
		}
		else
		{
			printf("exercise<ERROR>Second CreateFile Failed LastError = 0x%x\n", GetLastError());
		}
		QueryPerformanceCounter(&after);

#if 0	//take this out
		if (bytesRead != READ_SIZE) {
			DWORD error = GetLastError();
			EnterCriticalSection(&cnrLock);
			cerr << "Couldn't read " << READ_SIZE << " bytes.  Error: " << error << endl;
			LeaveCriticalSection(&cnrLock);
			exit(-1);
		}
#endif	

#ifdef DEBUG		
		EnterCriticalSection(&cnrLock);
		checkNumReqs += REQS_PER_FILE;
		cout << myNames[i] << endl;
		LeaveCriticalSection(&cnrLock);
#endif
		/*
		if ((thisPeriod - counterPeriod) > (counterPeriod / 2)) {
		cout << "Lagging behind, looped: " << counter << " times" << endl;
		cout << "counterPeriod: " << counterPeriod << " thisPeriod: " << thisPeriod << endl;
		}
		*/

		periodStart.QuadPart = before.QuadPart;
		cycles[threadNum] += after.QuadPart - before.QuadPart;
	}

	return 0;
}


DWORD findControlModule(void) {

	HANDLE hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	if (hSnapshot == INVALID_HANDLE_VALUE) {
		cerr << "Unable to get processes information" << endl;
		exit(-1);
	}
	PROCESSENTRY32 procEntry;
	procEntry.dwSize = sizeof(procEntry);
	if (!(Process32First(hSnapshot, &procEntry))) {
		return NULL;
	}
	do {
		string exeName = string(procEntry.szExeFile);
		if (exeName.find("controlmodule.exe") < exeName.size() || 
			exeName.find("HookService.exe") < exeName.size()) {
				CloseHandle(hSnapshot);
				return procEntry.th32ProcessID;
			}
	} while (Process32Next(hSnapshot, &procEntry));

	CloseHandle(hSnapshot);
	return NULL;
}




int _tmain(int argc, _TCHAR* argv[]) {
	if (argc != 3) {
		cerr << argv[0] << USAGE;
		return -1;
	}
	int desiredRPS;
	if (!(desiredRPS = atoi(argv[2]))) {
		cerr << argv[0] << USAGE;
		return -1;
	}

	HANDLE hThisProcess = GetCurrentProcess();
	SetPriorityClass(hThisProcess, HIGH_PRIORITY_CLASS);


	InitializeCriticalSection(&cnrLock);
	checkNumReqs = 0;

	QueryPerformanceFrequency(&frequency);
	counterPeriod = frequency.QuadPart / desiredRPS * NUMTHREADS * REQS_PER_FILE;

	WIN32_FIND_DATA FindFileData;
	HANDLE hFind;

	string dirName = string(argv[1]);
	if (dirName.at(dirName.length() - 1) != '\\') {
		dirName += '\\';
	}

	hFind = FindFirstFile((dirName + "*.test").c_str(), &FindFileData);
	if (hFind == INVALID_HANDLE_VALUE) 
	{
		cerr << "invalid directory specified: " << argv[1];
		return -1;
	} 

	DWORD cmProcId = findControlModule();
	if (cmProcId == NULL) {
		cout << "controlmodule.exe is not running, assuming FSA tests" << endl;
		hProcess = NULL;
	} else {

		hProcess = OpenProcess(PROCESS_ALL_ACCESS, false, cmProcId);
		if (hProcess == NULL) {
			cerr << "Cannot open controlmodule process" << endl;
			exit(-1);
		}
	}

	map<long, stringVector> sizeFilesMap;
	map<long, ULONGLONG> sizeCyclesMap;
	BOOL found;

	size_t numFiles = 0;
	do {
		if (!(FindFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
			string filename = (dirName + FindFileData.cFileName);
			long size = getSize(filename);
			sizeFilesMap[size].push_back(filename);
			sizeCyclesMap[size] = 0;
			numFiles++;
		}

		found = FindNextFile(hFind, &FindFileData);

	} while(found);

	FindClose(hFind);

	ofstream result("result.csv", ios::out);
	if (!result) {
		cerr << "unable to create result file" << endl;
		return -1;
	}	

	ULONGLONG totalCycles = 0;
	ULONGLONG runCycles = 0;
	LARGE_INTEGER before, after;

	long numIterations = numFiles * NUM_RUNS;
	long numCompleted = 0;

	for (int run = 0; run < NUM_RUNS; run++) {

		map<long, stringVector>::iterator iter = sizeFilesMap.begin();
		for (size_t i = sizeFilesMap.size(); i > 0; i--) {
			pair<long, stringVector> entry = *iter++;
			stringVector strNames = entry.second;
			size_t numFiles = strNames.size();
			filenames = new LPCSTR[numFiles];
			stringVector::iterator namesIter = strNames.begin();
			for (size_t j = 0; j < numFiles; j++) {
				filenames[j] = (*namesIter++).c_str();
			}

			filesPerThread = numFiles / NUMTHREADS;
			if (numFiles % NUMTHREADS) {
				cerr << "the number of files of size " << entry.first;
				cerr << " is not a multiple of " << NUMTHREADS;
				cerr << " can't handle that."  << endl;
				exit(-1);
			}

			for (int j = 0; j < NUMTHREADS; j++) {
				cycles[j] = 0;
			}
			HANDLE hThreads[NUMTHREADS];
			for (int j = 0; j < NUMTHREADS; j++) {
				hThreads[j] = CreateThread(NULL, 0, exercise, (LPVOID) j, CREATE_SUSPENDED, NULL);
				if (hThreads[j] == NULL) {
					cerr << "failed to create a thread" << endl;
					exit(-1);
				}
			}
			QueryPerformanceCounter(&before);
			for (int j = 0; j < NUMTHREADS; j++) {
				if (ResumeThread(hThreads[j]) == -1) {
					cerr << "failed to resume a thread" << endl;
					exit(-1);
				}
			}
			WaitForMultipleObjects(NUMTHREADS, hThreads, true, INFINITE);
			QueryPerformanceCounter(&after);
			runCycles += after.QuadPart - before.QuadPart;
			
			for (int j = 0; j < NUMTHREADS; j++) {
				CloseHandle(hThreads[j]);
			}

			for (int j = 0; j < NUMTHREADS; j++) {
				sizeCyclesMap[entry.first] += cycles[j];
			}
			totalCycles += sizeCyclesMap[entry.first];

			delete [] filenames;
			numCompleted += numFiles;
			cout << numCompleted * 100 / numIterations << "% complete" << endl;
		}
	}

#ifdef DEBUG
	if (checkNumReqs != (numFiles * REQS_PER_FILE)) {
		cerr << "WTF?" << endl;
		exit(-1);
	}
#endif

	map<long, ULONGLONG>::iterator cyclesIter = sizeCyclesMap.begin();
	for (size_t i = 0; i < sizeCyclesMap.size(); i++) {
		pair<long, ULONGLONG> entry = *cyclesIter++;
		size_t numFiles = sizeFilesMap[entry.first].size();
		FLOAT aveTime = (FLOAT) (entry.second * 1000000) / (frequency.QuadPart * numFiles * REQS_PER_FILE * NUM_RUNS);
		result << entry.first << ", " << aveTime << endl;

		cout << "size: " << entry.first << ", elapsed time: " << aveTime << "us" << endl;
	}



	FLOAT aveLatency = (FLOAT) (totalCycles * 1000000) / (frequency.QuadPart * numFiles * REQS_PER_FILE * NUM_RUNS);
	cout << "average response latency: " << aveLatency << endl;
	FLOAT reqsPerSec = (FLOAT) (numFiles * REQS_PER_FILE * frequency.QuadPart * NUM_RUNS) / runCycles;
	cout << "average requests per second: " << reqsPerSec << endl;

	result << "threads, " << NUMTHREADS << endl;
	result << "aveLatency, " << aveLatency << endl;
	result << "actualRPS, " << reqsPerSec << endl;
	result << "desiredRPS, " << desiredRPS << endl;

	result.close();
	CloseHandle( hProcess );
	DeleteCriticalSection(&cnrLock);

	return 0;
}


