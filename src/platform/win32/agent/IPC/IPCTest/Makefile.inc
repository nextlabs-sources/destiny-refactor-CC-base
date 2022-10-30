COMPILER=VS2008
PLATFORMSDK=7.0

# Define the target
TARGET = IPCTest

# Define the objects of the target module
SRC = stdafx.cpp           \
      IPCTest.cpp            

INCPATH += \
#  ${EXTERNALSDIR}/j2sdk/jdk1.6.0_18/include/ \
#  ${EXTERNALSDIR}/j2sdk/jdk1.6.0_18/include/win32 \

LIBPATH += \
  ../Shared/$(BUILDTYPE)_$(TARGETENV) \
  ../IPCProxy/$(BUILDTYPE)_$(TARGETENV) 
#  ${EXTERNALSDIR}\boost\boost_1_38_0\libs\regex\build\vc90 \
#  $(NLENFORCERSDIR)\platforms\win32\modules\content\libs

LIBS += Shared IPCProxy_static
