
# Universal master makefile for all modules 
# by Dominic Lam

# Including the OS dependent environment 

OS = $(shell uname -s)

include $(PLATFORMDIR)/build/env.${OS}

ifeq ($(BUILDTYPE), release)
  CFLAGS   += -O2
  CXXFLAGS += -O2
  LDFLAGS  +=
  BINDIR    = release_${BINBASE}
  LIBDIR    = release_${LIBBASE}
else
  CFLAGS   += -g -Wall
  CXXFLAGS += -g -Wall 
  LDFLAGS  += -g -Wall
  DFLAGS   += -DDEBUG
  BUILDTYPE = debug
  BINDIR    = ${BINBASE}
  LIBDIR    = ${LIBBASE}
endif

# Always include Makefile.inc

include Makefile.inc

# Phony target to make sure they will get built
.PHONY: subdirs $(SUBDIRS)
.PHONY: clean
.PHONY: $(TARGET)

# Go to subdirectory and do recursive make 
ifneq ($(strip $(SUBDIRS)),)
subdirs: $(SUBDIRS)
  for dir in $(SUBDIRS); do $(MAKE) -C $$dir; done
endif

SRCDIR = src

OBJS = $(addprefix $(BINDIR)/, $(addsuffix .o, $(basename $(notdir $(SRC)))))

INCP = $(addprefix -I, $(INCPATH))

LIBP = $(addprefix -L, $(LIBPATH))

RUNP = $(addprefix -Xlinker -R, $(LIBPATH))


# Computing the files to be cleaned
CLEANFILES = $(OBJS)

ifneq ($(TARGET), )
  CLEANFILES += $(BINDIR)/$(TARGET)
endif

ifneq ($(SUBTARGET), )
  CLEANFILES += $(BINDIR)/lib$(SUBTARGET)
endif

# Rule for building C files
$(BINDIR)/%.o: $(SRCDIR)/%.c
	@if [ ! -d $(BINDIR) ] ; then               \
	  mkdir -p $(BINDIR) ;                      \
	fi
	$(CC) $(CFLAGS) $(INCP) $(DFLAGS) -c $< -o $@

# Rule for building C++ files
$(BINDIR)/%.o: $(SRCDIR)/%.cpp
	@if [ ! -d $(BINDIR) ] ; then               \
	  mkdir -p $(BINDIR) ;                      \
	fi
	$(CXX) $(CXXFLAGS) $(INCP) $(DFLAGS) -c $< -o $@

# Rule for building Target executables
$(TARGET): $(OBJS)
	@if [ ! -d $(BINDIR) ] ; then               \
	  mkdir -p $(BINDIR) ;                      \
	fi
	$(CXX) $(LDFLAGS) $^ -o $(BINDIR)/$@ $(LIBP) $(RUNP) $(LIBS)
	@if [ $(BUILDTYPE) == "release" ] ; then    \
	  $(STRIP) -s $(BINDIR)/$@ ;                \
	fi
	@if [ ! -d ${BJROOT}/$(BINDIR) ] ; then     \
	  mkdir -p ${BJROOT}/$(BINDIR) ;            \
	fi
	cp -f $(BINDIR)/$@ ${BJROOT}/$(BINDIR)

# Rule for building subtarget share library
$(SUBTARGET): $(OBJS)
	@if [ ! -d $(BINDIR) ] ; then               \
	  mkdir -p $(BINDIR) ;                      \
	fi
	$(CXX) $(LDFLAGS) -shared $^ -o $(BINDIR)/lib$@
	@if [ $(BUILDTYPE) == "release" ] ; then    \
	  $(STRIP) -s $(BINDIR)/lib$@ ;             \
	fi
	@if [ ! -d ${BJROOT}/$(LIBDIR) ] ; then     \
	  mkdir -p ${BJROOT}/$(LIBDIR)  ;           \
	fi
	cp -f $(BINDIR)/lib$@ ${BJROOT}/$(LIBDIR) 

# Rule for cleaning up
clean:
	rm -f $(CLEANFILES)

