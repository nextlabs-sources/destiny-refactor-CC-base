COMPILER=VS2008
PLATFORMSDK=7.0

# Define the target
SUBTARGET = IPCJNI

# Define the objects of the target module
SRC = stdafx.cpp           \
      oswrapper.cpp        \
	  IPCJNI.cpp    \
	  SessionInfo.cpp 

INCPATH += \
  ${EXTERNALSDIR}/j2sdk/jdk1.7.0_71/include/ \
  ${EXTERNALSDIR}/j2sdk/jdk1.7.0_71/include/win32 \
  $(NLENFORCERSDIR)/platforms/win32/modules/content/client/include \
  $(NLENFORCERSDIR)/platforms/win32/modules/content/analyze/include \
  $(NLENFORCERSDIR)/platforms/win32/modules/content/framework/include \
  ${EXTERNALSDIR}/boost/boost_1_43_0 \
  $(NLENFORCERSDIR)/platforms/common/include \
  $(NLENFORCERSDIR)/sdk/include \
  ../../../../../../build

LIBPATH += \
  ../Shared/$(BUILDTYPE)_$(TARGETENV) \

ifeq ($(TARGETENVARCH), x86)
  LIBPATH += \
   ${EXTERNALSDIR}/boost/boost_1_43_0/stage-win32/lib
else
  LIBPATH += \
   ${EXTERNALSDIR}/boost/boost_1_43_0/stage-x64/lib
endif

LIBS += Shared_static Imagehlp Netapi32 secur32 Crypt32 mpr Psapi

DFLAGS += -DIPCJNI_EXPORTS -DPSAPI_VERSION=1
