// DriInst.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "TCHAR.h"
#include "DriInst.h"
BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
    return TRUE;
}

/////////////////////////////////////////////////////////////////////////////////
//
// dwStartType : type of service startup
// szServiceName : Name of the driver service
// szPathName : Path to the driver file
// szDependencies : name of the dependent service (NULL if none)
// lpLoadOrderGroup : load ordering group
// return 0: success
// return 1: has exist
// return 2: fail
//
/////////////////////////////////////////////////////////////////////////////////
UINT	WINAPI	 InstallDriver(int dwStartType, LPCTSTR szServiceName, LPCTSTR szPathName, LPCTSTR szDependency, LPCTSTR lpLoadOrderGroup)
{
	SC_HANDLE   schService = NULL;
	SC_HANDLE   schSCManager = NULL;
	
	UINT ret = 2;
	
	if (_tcslen(szDependency) == 0) {
		szDependency = NULL;
	}
	
	schSCManager = OpenSCManager(
		NULL,                   // machine (NULL == local)
		NULL,                   // database (NULL == default)
		SC_MANAGER_CONNECT | SC_MANAGER_CREATE_SERVICE
		);
	if ( schSCManager )
	{
        schService = OpenService(schSCManager, szServiceName, SERVICE_ALL_ACCESS);
        if (schService)
		{			
			ret = 1;
			//MessageBox(GetForegroundWindow(), szServiceName, NULL, MB_OK | MB_ICONERROR);
		}
		else
		{
			schService = CreateService(
				schSCManager,               // SCManager database
				szServiceName,				// name of service
				szServiceName,				// name to display
				SERVICE_ALL_ACCESS,			// desired access
				SERVICE_KERNEL_DRIVER,		// service type
				dwStartType,				// start type
				SERVICE_ERROR_NORMAL,       // error control type
				szPathName,                 // service's binary
				lpLoadOrderGroup,           // load ordering group
				NULL,                       // no tag identifier
				szDependency,				// dependencies
				NULL,                       // LocalSystem account
				NULL);                      // no password

			if (schService)
			{
				ret = 0;
				//MessageBox(GetForegroundWindow(), szServiceName, NULL, MB_OK | MB_ICONINFORMATION);
			}
			else
			{
				ret = 2;
				//MessageBox(GetForegroundWindow(), szServiceName, NULL, MB_OK | MB_ICONERROR);
			}
		}
	}
	else
	{
		ret = 2;
		//MessageBox(GetForegroundWindow(), szServiceName, NULL, MB_OK | MB_ICONERROR);
	}

	if(schService) 
	{
		CloseServiceHandle(schService);
	}
	if(schSCManager) 
	{
		CloseServiceHandle(schSCManager);
	}
	
	return ret;
}

// return 0: success
// return 1: no exist
// return 2: fail
// return 3: can't stop
UINT	WINAPI	RemoveDriver(LPCTSTR szServiceName)
{
	SC_HANDLE		schService;
	SC_HANDLE		schSCManager;
	SERVICE_STATUS	gssStatus;
	UINT			ret = 2;

	schSCManager = OpenSCManager(
		NULL,                   // machine (NULL == local)
		NULL,                   // database (NULL == default)
		SC_MANAGER_CONNECT		// access required
		);
	if ( schSCManager )
	{
		schService = OpenService(schSCManager, 
			szServiceName, SERVICE_ALL_ACCESS);

		if (schService)
		{
			if ( ControlService( schService, SERVICE_CONTROL_STOP, &gssStatus ) )
			{
				Sleep( 500 );

				while ( QueryServiceStatus( schService, &gssStatus ) )
				{
					if ( gssStatus.dwCurrentState == SERVICE_STOP_PENDING )
					{
						Sleep( 1000 );
					}
					else
						break;
				}
			}

			if ( gssStatus.dwCurrentState != SERVICE_STOPPED )
			{
				ret = 3;
				//MessageBox(GetForegroundWindow(), szServiceName, NULL, MB_OK | MB_ICONERROR);
				goto EXIT;
			}

			// now remove the service
			if ( DeleteService(schService) )
			{
				//MessageBox(GetForegroundWindow(), szServiceName, NULL, MB_OK | MB_ICONINFORMATION);
				ret = 0;
			}
			else
			{
				//MessageBox(GetForegroundWindow(), szServiceName, NULL, MB_OK | MB_ICONERROR);
				ret = 2;
			}
		}
		else
		{
			//MessageBox(GetForegroundWindow(), szServiceName, NULL, MB_OK | MB_ICONINFORMATION);
			ret = 0;
		}
	}
	else
	{
		//MessageBox(GetForegroundWindow(), szServiceName, NULL, MB_OK | MB_ICONERROR);
		ret = 2;
	}

EXIT:
	if(schService) 
	{
		CloseServiceHandle(schService);
	}
	if(schSCManager) 
	{
		CloseServiceHandle(schSCManager);	
	}
	return ret;
}

// return 0: success
// return 1: not exist
// return 2: fail
UINT	WINAPI	StartDriver(LPCTSTR szServicename)
{
    SC_HANDLE				schService = NULL;
    SC_HANDLE				schSCManager = NULL;
	SERVICE_STATUS          ssStatus;       

	UINT					ret = 2;

	do 
	{
		schSCManager = OpenSCManager(
							NULL,                   // machine (NULL == local)
							NULL,                   // database (NULL == default)
							SC_MANAGER_ALL_ACCESS   // access required
							);
		if ( schSCManager )
		{
			schService = OpenService(schSCManager, szServicename, SERVICE_ALL_ACCESS);

			if (schService)
			{
				// try to start the service
				if( StartService(schService,0,NULL) )	
				{
					Sleep( 100 );

					while( QueryServiceStatus( schService, &ssStatus ) )
					{
						if ( ssStatus.dwCurrentState == SERVICE_START_PENDING )
						{
							Sleep( 200 );
						}
						else	break;
					}

					if ( ssStatus.dwCurrentState != SERVICE_RUNNING )	
					{
						break;
					}
					//MessageBox(GetForegroundWindow(), L"Driver started", NULL, MB_OK | MB_ICONINFORMATION);
					ret = 0;
				}
			}
			else
			{
				//MessageBox(GetForegroundWindow(), szServicename, NULL, MB_OK | MB_ICONERROR);
				ret = 1;
			}
		}
		else
		{
			//MessageBox(GetForegroundWindow(), szServicename, NULL, MB_OK | MB_ICONERROR);
			ret = 2;
		}
	}while(FALSE);

	if(schService)	
	{
		CloseServiceHandle(schService);
	}

	if(schSCManager)	
	{
		CloseServiceHandle(schSCManager);
	}

	return ret;
}

// return 0: success
// return 1: not exist
// return 2: fail
// return 3: can't stop
UINT WINAPI	StopDriver(LPCTSTR servicename)
{
	SC_HANDLE				schService = NULL;
	SC_HANDLE				schSCManager = NULL;
	SERVICE_STATUS          gssStatus;
	UINT					ret = 2;

	schSCManager = OpenSCManager(
		NULL,                   // machine (NULL == local)
		NULL,                   // database (NULL == default)
		SC_MANAGER_ALL_ACCESS   // access required
		);

	if ( schSCManager )
	{
		schService = OpenService(schSCManager, servicename, SC_MANAGER_ALL_ACCESS);

		if (schService)
		{
			if ( ControlService( schService, SERVICE_CONTROL_STOP, &gssStatus ) )
			{
				Sleep( 10000 );

				while ( QueryServiceStatus( schService, &gssStatus ) )
				{
					if ( gssStatus.dwCurrentState == SERVICE_STOP_PENDING )
					{
						Sleep( 10000 );
					}
					else
					{
						ret =4;
						break;
					}
				}

				if ( gssStatus.dwCurrentState == SERVICE_STOPPED )
				{
					//MessageBox(GetForegroundWindow(), L"Driver stopped", NULL, MB_OK | MB_ICONINFORMATION);
					ret = 0;
				}
				else
				{
					//MessageBox(GetForegroundWindow(), servicename, NULL, MB_OK | MB_ICONERROR);
					ret = 3;
				}
			}
			else
			{
				//ControlService failed
				ret = GetLastError();
			}
		}
		else
		{
			//MessageBox(GetForegroundWindow(), servicename, NULL, MB_OK | MB_ICONERROR);
			ret = 1;
		}
	}
	else
	{
		MessageBox(GetForegroundWindow(), servicename, NULL, MB_OK | MB_ICONERROR);
		ret = 2;
	}

	if(schService) 
	{
		CloseServiceHandle(schService);
	}
	if(schSCManager) 
	{
		CloseServiceHandle(schSCManager);	
	}
	return ret;
}
