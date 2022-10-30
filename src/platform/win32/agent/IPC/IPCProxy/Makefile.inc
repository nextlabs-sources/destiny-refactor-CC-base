COMPILER=VS2008
PLATFORMSDK=7.0

# Define the target
SUBTARGET = IPCProxy
SUBTARGET_STATIC = yes

# Define the objects of the target module
SRC = stdafx.cpp           \
      IPCProxy.cpp            

INCPATH += \
  ${EXTERNALSDIR}/j2sdk/jdk1.7.0_71/include/ \
  ${EXTERNALSDIR}/j2sdk/jdk1.7.0_71/include/win32 \
  $(NLENFORCERSDIR)/sdk/include 

LIBPATH += \
  ../Shared/$(BUILDTYPE)_$(TARGETENV) 

LIBS += Shared
