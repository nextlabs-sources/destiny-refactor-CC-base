// EvalPerfTH.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"

using namespace std;

#define USAGE " directory"

void exercise(const TCHAR *filename) {
  for (int i = 0; i < 10; i++) {
    cout << "Exercising " << filename << endl;
    HANDLE hFile = CreateFile(filename, FILE_READ_DATA, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
    if (hFile == INVALID_HANDLE_VALUE) {
      cout << "deny" << endl;
    } else {
      cout << "allow" << endl;
      if (!CloseHandle(hFile)) {
	cerr << "Could not close " << filename << ". Exiting";
	exit(-1);
      }
    }
  }
}
    


int _tmain(int argc, _TCHAR* argv[])
{

  if (argc != 2) {
    cerr << argv[0] << USAGE;
    return -1;
  }


  WIN32_FIND_DATA FindFileData;
  HANDLE hFind;

  string dirName = string(argv[1]);
  if (dirName.at(dirName.length() - 1) != '\\') {
	  dirName += '\\';
  }
  
  hFind = FindFirstFile((dirName + "*").c_str(), &FindFileData);
  if (hFind == INVALID_HANDLE_VALUE) 
    {
      cerr << "invalid directory specified: " << argv[1];
      return -1;
    } 

  BOOL found;
  do {
    if (!(FindFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
      exercise((dirName + "\\" + FindFileData.cFileName).c_str());
    }

    found = FindNextFile(hFind, &FindFileData);

  } while(found);

  FindClose(hFind);
    
  
  return 0;
}


