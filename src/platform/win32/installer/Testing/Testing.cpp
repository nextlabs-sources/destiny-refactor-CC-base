#include "stdafx.h"
#include "..\common\installercommon.h"
#include "..\..\..\common\include\uninstall_hash.h"
/*
	Main function (for testing purpose only)
*/
int _tmain(int argc, _TCHAR* argv[])
{
    //TCHAR name[30] = _T("STBARTS:5432");
    //TCHAR result[30];
    //int iResult = getHostName(name, result);
    //int port = getPortNumber(name, 333);
    //LPTSTR result = DiscoverPolicyServers(NULL);
    //TCHAR domainControllers[500];
    //iResult = findDomainControllers(domainControllers);
    //TCHAR file[500];
    //TCHAR domainName[100];
    //getDomainName(domainName, TRUE);
    //getDomainName(domainName, FALSE);
    //browseForFiles (file);
    /*TCHAR mgmtServer[101];
    discoverMgmtServers(mgmtServer, 100);
	MessageBox(GetForegroundWindow(), _T("OK"), _T("OK"), MB_OK);*/
    /*int i=0;
    TCHAR users[5000];
    iResult = findRegularUsers(users);
    */
    //iResult = validateServiceAccount (_T("iannis"), _T("STBARTS"), _T("123blue!"));
    //TCHAR errMsg[500];
    //testLDAPConnection(_T("PUKAPUKA"), 389, _T("ktong@bluejungle.com"), _T("KKKKkkkk1111"), _T("BLUEJUNGLE"), errMsg);

	/*UINT result = testLDAPConnection(_T("CUBA"), 389, _T("zachary.taylor"), _T("zachary.taylor"), _T("yahoo.com"), errMsg);
	result = testLDAPConnection(_T("CUBA"), 389, _T("zachary.taylor"), _T("zachary.taylor"), _T("BLUEJUNGLE"), errMsg);*/
	//UINT result = testLDAPConnection(_T("CUBA"), 389, _T("iannis"), _T("123blue!"), _T("dc=test,dc=bluejungle,dc=com"), _T("TEST"), errMsg);
	//UINT result = testLDAPConnection(_T("CUBA"), 389, _T("iannis"), _T("123blue!"), NULL, _T("TEST"), errMsg);
    //int yesNo = showYesNoDialog(_T("DialogTitle"), _T("Dialog message"));
    /*TCHAR icenetServers[31];
    discoverIcenetServers(icenetServers, 30);*/
	//LPTSTR err = new TCHAR[500];
	//int result = testPostgreSQLConnection(_T("localhost"), 5432, _T("root"), _T("123blue"), err);
	//LPTSTR dbName = new TCHAR[500];
	//dbName = _T("activity");
	//result = createPGDatabase (_T("localhost"), 5432, _T("root"), _T("123blue!"), dbName, true, err);
	//result = dropPGDatabase (_T("localhost"), 5432, _T("root"), _T("123blue!"), _T("activity"), err);
	//result = isPostgreSQLDatabaseCreated(_T("localhost"), 5432, _T("root"), _T("123blue!"), _T("activity"), err);
	//testOracleConnection();
	//findAvailablePort(389);
	//TCHAR buf [31];
	//size_t size = 30;
	//TCHAR challenge [16];
	//size_t challengeLen = 15;
	//int result = hashChallenge(challenge, buf, size);
	//if (result = -1)
	//{
	//	TCHAR* newBuf = new TCHAR[size];
	//	result = hashChallenge(challenge, newBuf, size);
	//	delete newBuf;
	//	newBuf = NULL;
	//}
	

	/*TCHAR password[100];
	TCHAR realLogin[100];
	autoCreateServiceUser(L"ceuser", L"This is a new CE User", realLogin, password);
	deleteServiceUserAccount(realLogin);
	int i=0;

	TCHAR name[30] = _T("PEMBA");
    TCHAR result[30];
	int iResult = getHostSID(name, result);*/

	/*
	TCHAR firstName[257];
	int firstNameSize = 256;
	int result = getCurrentUserFirstName(firstName, firstNameSize);
	int i=0;

	TCHAR lastName[257];
	int lastNameSize = 256;
	result = getCurrentUserLastName(lastName, lastNameSize);
	i=0;

	TCHAR principalName[257];
	int principalNameSize = 256;
	result = getCurrentUserPrincipalName(principalName, principalNameSize);
	i=0;

	TCHAR loginName[257];
	int loginNameSize = 256;
	result = getCurrentUserLoginName(loginName, loginNameSize);
	i=0;
	*/

	waitForService(0);
	printf("This is the end of the story");
}
