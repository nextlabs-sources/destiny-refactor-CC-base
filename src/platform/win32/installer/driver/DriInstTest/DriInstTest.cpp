// DriInstTest.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "Windows.h"
#include "..\\DriInst\\DriInst.h"

int _tmain(int argc, _TCHAR* argv[])
{
	printf("press enter to install core driver");
	getchar();
	UINT ret = InstallDriver(SERVICE_DEMAND_START,"dscore",
	"C:\\Program Files\\Compliant Enterprise\\Compliance Enforcer\\bin\\dscore.sys", NULL);
	printf("install core driver ret = %d\n", ret);
	
	printf("press enter to install IFS driver");
	getchar();
	ret = InstallDriver(SERVICE_DEMAND_START,"dsifsflt",
	"C:\\Program Files\\Compliant Enterprise\\Compliance Enforcer\\bin\\dsifsflt.sys", "dscore");
	printf("install IFS driver ret = %d\n", ret);

	printf("press enter to start core driver");
	getchar();
	ret = StartDriver("dscore");
	printf("start core driver ret = %d\n", ret);

	printf("press enter to start IFS driver");
	getchar();
	ret = StartDriver("dsifsflt");
	printf("start IFS driver ret = %d\n", ret);

	printf("press enter to stop IFS driver");
	getchar();
	ret = StopDriver("dsifsflt");
	printf("stop IFS driver ret = %d\n", ret);

	printf("press enter to stop core driver");
	getchar();
	ret = StopDriver("dscore");
	printf("stop core driver ret = %d\n", ret);

	printf("press enter to remove IFS driver");
	getchar();
	ret = RemoveDriver("dsifsflt");
	printf("remove IFS driver ret = %d\n", ret);

	printf("press enter to remove core driver");
	getchar();
	ret = RemoveDriver("dscore");
	printf("remove driver ret = %d\n", ret);

	return 0;
}

