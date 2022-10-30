COMPILER=VS2008
PLATFORMSDK=7.0

# Define the target
SUBTARGET = Shared
SUBTARGET_STATIC = yes

# Define the objects of the target module
SRC = stdafx.cpp           \
      ILog.cpp            \
	  LogFactory.cpp \
	  NotQuiteNullDacl.cpp \
	  SecurityAttributesFactory.cpp \
	  UserUtils.cpp \
	  Shared.cpp \
	  SimpleLog.cpp

LIBS += Netapi32

DFLAGS += -DSHARED_EXPORTS
