
As of March, 2005, here is how we compile AMD64 application.
-------------------------------------------------------------------------------------------

Require software for compile 64Bits application
	1.  Microsoft Visual Studio 6.
	2.  Microsoft Platform SDK Windows2003-SP1 or later (This CD will have executable,
	     include, lib for AMD64.

1.  Create an empty "Win32 Console Application" project
2.  Add the cpp files, 
3.  In the C/C++ section, add your private include path
4.  In the C/C++ section, Debug info: select "Program Database"
5.  In the C/C++ section, Preprocessor, add WIN64, _WIN64, AMD64, _AMD64_
6.  In the C/C++ section, add /Zi, remove /XY, change /GX to /EHsc, add /GS,
     remove /GZ
7.  In Link section, change /machine:i386 to /machine:AMD64
8.  In Link section,  remove /pdbtype:sept     	
9.  In Tools, Options, Directories, Select "Include files" add these two paths
     on top of it:
	a.  C:\Program Files\Microsoft Platform SDK\include
	b.  C:\PROGRAM FILES\MICROSOFT PLATFORM SDK\INCLUDE\CRT
10. In Tools, Options, Directories, Select "Executable files" add these two paths
     on top of it:
	a.  C:\PROGRAM FILES\MICROSOFT PLATFORM SDK\BIN\WIN64\AMD64
	b.  C:\PROGRAM FILES\MICROSOFT PLATFORM SDK\BIN\WIN64\X86\AMD64
11. In Tools, Options, Directories, Select "Library files" add these paths on top of it:
	C:\PROGRAM FILES\MICROSOFT PLATFORM SDK\LIB\AMD64
	C:\PROGRAM FILES\MICROSOFT PLATFORM SDK\LIB\AMD64\MFC


SPECIAL NOTE:
A.  File IFSTESTIPC.VCPROJ and IFSTESTIPC.SLN is for Win32 compilation.
B.  File IFSTestIPC.DSP and IFSTestIPC.DSW is for Win64 compilation


