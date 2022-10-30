/*
* Actions.h
* Author: Fuad Rashid
* All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., 
* Redwood City CA, Ownership remains with Blue Jungle Inc, 
* All rights reserved worldwide. 
*/

#ifndef _ACTIONS_H_
#define _ACTIONS_H_

#define MAX_ACTION_NAME_SIZE	32

#define OPEN_NAME               L"OPEN"
#define EDIT_NAME               L"EDIT"
#define DELETE_NAME             L"DELETE"
#define READ_NAME               L"READ"
#define WRITE_NAME              L"WRITE"
#define CLOSE_NAME              L"CLOSE"
#define RENAME_NAME             L"RENAME"
#define CREATE_NEW_NAME         L"EDIT"
#define CHANGE_PROPERTIES_NAME  L"CHANGE_ATTRIBUTES"
#define CHANGE_SECURITY_NAME    L"CHANGE_SECURITY"
#define EDIT_COPY_NAME          L"EDIT_COPY"
#define SEND_IM_NAME            L"IM"
#define CUT_PASTE_NAME          L"CUT_PASTE"
#define COPY_PASTE_NAME         L"PASTE"
#define BATCH_NAME              L"BATCH"
#define BURN_NAME               L"BURN"
#define PRINT_NAME              L"PRINT"
#define COPY_NAME               L"COPY"
#define MOVE_NAME               L"MOVE"
#define SHARE_NAME              L"SHARE"
#define EMAIL_NAME              L"EMAIL"
#define EMBED_NAME              L"EMBED"
#define STOP_AGENT_NAME         L"STOP_AGENT"
#define EXECUTE_NAME			L"EXECUTE"
#define UNKNOWN_NAME            L"UNKNOWN"

#define OPEN_READ_ACTION               0x0001
#define OPEN_READ_WRITE_ACTION         0x0002
#define DELETE_ACTION                  0x0004
#define READ_ACTION                    0x0008
#define WRITE_ACTION                   0x0010
#define CLOSE_ACTION                   0x0020
#define RENAME_ACTION                  0x0040
#define CREATE_NEW_ACTION              0x0080
#define CHANGE_PROPERTIES_ACTION       0x0100
#define CHANGE_SECURITY_ACTION         0x0200
#define SAVE_AS_ACTION                 0x0400
#define SEND_IM_ACTION                 0x0800
#define CUT_PASTE_ACTION               0x1000
#define COPY_PASTE_ACTION              0x2000
#define BATCH_ACTION                   0x4000
#define BURN_ACTION                    0x8000
#define PRINT_ACTION                   0x10000
#define COPY_ACTION                    0x20000
#define MOVE_ACTION                    0x40000
#define SHARE_ACTION                   0x80000
#define EMAIL_ACTION                   0x100000
#define OPEN_DIR_ACTION                0x200000
#define EMBED_ACTION                   0x400000
#define STOP_AGENT_ACTION              0x800000
#define EXECUTE_ACTION				   0x1000000

//for ulAllow
#define DENY                    1
#define ALLOW                   2

//for ulAllowType
#define NOT_WATCHED             1 //allow all operations on file for all users/all actions
#define WATCH_NEXT_OP           2 // send next op to policy engine
#define ALLOW_UNTIL_CLOSE       3 // ignore all subsequent actions of the same type for the open file

#endif
