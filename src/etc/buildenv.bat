subst /d p:
subst /d q:
subst /d x:
subst /d r:
subst /d o:
subst p: %1
subst q: %2
IF EXIST "C:/Program Files (x86)/Microsoft Visual Studio 9.0\VC\bin\cl.exe" (subst x: "C:/Program Files (x86)/Microsoft Visual Studio 9.0") ELSE (subst x: "C:/Program Files/Microsoft Visual Studio 9.0")
IF EXIST "X:\VC/bin/cl.exe" (echo "Setup of Visual Studio complete")
subst r: "C:/Program Files/Microsoft SDKs/Windows/v6.0A"
subst o: "C:/Program Files/Microsoft SDKs/Windows/v7.0"
subst /d i:
IF EXIST "C:\Program Files (x86)\InstallShield\2010 StandaloneBuild\System\IsCmdBld.exe" (subst i: "C:\Program Files (x86)\InstallShield\2010 StandaloneBuild" ) ELSE (subst i: "C:\Program Files\InstallShield\2010 StandaloneBuild")
IF EXIST "i:\System\IsCmdBld.exe" (echo "Setup of Install Shield 2010 complete")

