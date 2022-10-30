COMPILER=VS2008
PLATFORMSDK=7.0

# Define the target
SUBTARGET = IPCStubTest

# Define the objects of the target module
SRC = stdafx.cpp           \
      IPCStubTest.cpp \
	  StubTestRequestHandler.cpp            

LIBPATH += \
  ../IPCStub/$(BUILDTYPE)_$(TARGETENV) 

LIBS += IPCStub
