COMPILER=VS2008
PLATFORMSDK=7.0

# Define the target
TARGET = LoggingInUserTest

# Define the objects of the target module
SRC = stdafx.cpp           \
      LoggedInUserTest.cpp            

LIBPATH += \
  ../Shared/$(BUILDTYPE)_$(TARGETENV) 

LIBS += Shared Netapi32
