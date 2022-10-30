// The following ifdef block is the standard way of creating macros which make exporting 
// from a DLL simpler. All files within this DLL are compiled with the DRIINST_EXPORTS
// symbol defined on the command line. this symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see 
// DRIINST_API functions as being imported from a DLL, whereas this DLL sees symbols
// defined with this macro as being exported.
#ifdef DRIINST_EXPORTS
#define DRIINST_API __declspec(dllexport)
#else
#define DRIINST_API __declspec(dllimport)
#endif

UINT	WINAPI	InstallDriver(int dwStartType, LPCTSTR szServiceName, LPCTSTR szPathName, LPCTSTR szDependency, LPCTSTR lpLoadOrderGroup);
UINT	WINAPI	RemoveDriver(LPCTSTR szServiceName);
UINT	WINAPI	StartDriver(LPCTSTR szServicename);
UINT	WINAPI	StopDriver(LPCTSTR servicename);