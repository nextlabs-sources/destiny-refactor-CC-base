#include "include/uninstall_hash.h"
#include "stdio.h"
#include <TCHAR.h>
#include <time.h>
#include <Wincrypt.h>

#define SHARED_SECRET_PREFIX _T("i*/747")
#define SHARED_SECRET_SUFFIX _T("A380-?")
#define SHARED_SECRET_LENGTH 12

/////////////////////////////////////////////////////////////////////
// Takes a challenge and calculates a hash based on it
// challenge [in] : challenge string
// respBuf [out] : challenge response buffer (the challenge response will
//                 be stored in that buffer
// respBufSize [out] : size of the challenge response.
// returns : ERROR_SUCCESS if success
//           HASH_BUFFER_TOO_SMALL if buffer is too small (respBufSize will give the correct size)
//           HASH_BUFFER_ERROR if another error occured.
/////////////////////////////////////////////////////////////////////
UINT WINAPI  hashChallenge (LPCTSTR challenge, LPTSTR respBuf, size_t &respBufSize) 
{
	if (respBuf == NULL || challenge == NULL) 
	{
		return HASH_BUFFER_ERROR;
	}
	_tcscpy(respBuf, _T(""));
	size_t challengeLen = _tcslen(challenge);
	TCHAR* innerChallenge = new TCHAR[challengeLen + SHARED_SECRET_LENGTH + 1];
	_tcscpy(innerChallenge, SHARED_SECRET_PREFIX); 
	_tcscat(innerChallenge, challenge);
	_tcscat(innerChallenge, SHARED_SECRET_SUFFIX);

	BYTE* pbContent = (BYTE*) innerChallenge;
	DWORD cbContent = (DWORD) _tcslen((TCHAR*) pbContent)+1;  
	DWORD HashAlgSize;
	CRYPT_ALGORITHM_IDENTIFIER HashAlgorithm;
	CMSG_HASHED_ENCODE_INFO HashedEncodeInfo;
	DWORD cbEncodedBlob = 0;
	BYTE *pbEncodedBlob = NULL;
	HCRYPTMSG hMsg;
	HCRYPTMSG hDupMsg;
	HCRYPTPROV hCryptProv;

	DWORD cbData = sizeof(DWORD);

	if(!CryptAcquireContext(&hCryptProv, NULL, NULL, PROV_RSA_FULL, CRYPT_NEWKEYSET))
	{
		DWORD err = GetLastError();
		if (err == NTE_EXISTS)
		{
			CryptReleaseContext(hCryptProv, 0);
			if (!CryptAcquireContext(&hCryptProv, NULL, NULL, PROV_RSA_FULL, 0))
			{		
				err = GetLastError();
				return HASH_BUFFER_ERROR;
			}
		} 
		else
		{
			return HASH_BUFFER_ERROR;
		}
	}

	HashAlgSize = sizeof(HashAlgorithm);
	memset(&HashAlgorithm, 0, HashAlgSize);
	HashAlgorithm.pszObjId = szOID_RSA_MD5;

	memset(&HashedEncodeInfo, 0, sizeof(CMSG_HASHED_ENCODE_INFO));
	HashedEncodeInfo.cbSize = sizeof(CMSG_HASHED_ENCODE_INFO);
	HashedEncodeInfo.hCryptProv = hCryptProv;
	HashedEncodeInfo.HashAlgorithm = HashAlgorithm;
	HashedEncodeInfo.pvHashAuxInfo = NULL;

	cbEncodedBlob = CryptMsgCalculateEncodedLength(PKCS_7_ASN_ENCODING | X509_ASN_ENCODING, 0, CMSG_HASHED, &HashedEncodeInfo, NULL, cbContent);
	pbEncodedBlob = (BYTE *) malloc(cbEncodedBlob);
	hMsg = CryptMsgOpenToEncode(PKCS_7_ASN_ENCODING | X509_ASN_ENCODING, 0, CMSG_HASHED, &HashedEncodeInfo, NULL, NULL);

	if(!CryptMsgUpdate(hMsg, pbContent, cbContent, TRUE))
	{
		return HASH_BUFFER_ERROR;
	}

	hDupMsg = CryptMsgDuplicate(hMsg);
	if(!CryptMsgGetParam(hDupMsg, CMSG_CONTENT_PARAM, 0, pbEncodedBlob, &cbEncodedBlob))
	{
		return HASH_BUFFER_ERROR;
	}
	
	//Each integer will not have more than 4 digits
	TCHAR* buf = new TCHAR[4*cbEncodedBlob+1];
	_tcscpy(buf, _T(""));
	for (DWORD i=0; i<cbEncodedBlob; i++)
	{
		swprintf(buf, _T("%s%i"), buf, (int) pbEncodedBlob[i]);
	}

	size_t bufLen = _tcslen(buf);
	int result = ERROR_SUCCESS;
	if (respBufSize < bufLen)
	{
		respBufSize = bufLen+1;
		result = HASH_BUFFER_TOO_SMALL;
	} 
	else
	{
		_tcscpy(respBuf, buf);
	}
	delete [] buf;
	buf = NULL;
	return result;
}