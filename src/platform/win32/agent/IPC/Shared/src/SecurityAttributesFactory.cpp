// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., 
// Redwood City CA, Ownership remains with Blue Jungle Inc, 
// All rights reserved worldwide. 

/**
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 *  
 */

#include "StdAfx.h"
#include "globals.h"
#include "NotQuiteNullDacl.h"
#include "securityattributesfactory.h"

SecurityAttributesFactory::SecurityAttributesFactory(void)
{
}

SecurityAttributesFactory::~SecurityAttributesFactory(void)
{
}

SECURITY_ATTRIBUTES* SecurityAttributesFactory::m_pSecurityAttributes = NULL;

SECURITY_ATTRIBUTES* SecurityAttributesFactory::GetSecurityAttributes ()
{
    if (m_pSecurityAttributes == NULL)
    {
        // create the not-quite-null-dacl and security descriptor to 
        // be used for creating all events/filemappings/mutexes
        NotQuiteNullDacl* pDacl = new NotQuiteNullDacl ();
        pDacl->Create();
        // declare and initialize a security attributes structure
        m_pSecurityAttributes = new SECURITY_ATTRIBUTES();
        ZeroMemory( m_pSecurityAttributes, sizeof(*m_pSecurityAttributes) );
        m_pSecurityAttributes->nLength = sizeof( *m_pSecurityAttributes );
        m_pSecurityAttributes->bInheritHandle = FALSE; // object uninheritable

        // declare and initialize a security descriptor
        SECURITY_DESCRIPTOR* pSD = new SECURITY_DESCRIPTOR();
        InitializeSecurityDescriptor( pSD,
            SECURITY_DESCRIPTOR_REVISION );
        // assign the dacl to it
        SetSecurityDescriptorDacl( pSD,
            TRUE,
            pDacl->GetPDACL(),
            FALSE );
        // Make the security attributes point to the security descriptor
        m_pSecurityAttributes->lpSecurityDescriptor = pSD;
    }

    return (m_pSecurityAttributes);

}
