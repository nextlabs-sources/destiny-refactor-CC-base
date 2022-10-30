# Create/Update a CAR
$server = $args[0]
$rulename = $args[1]
$identity = $args[2]
$description = $args[3]
$useracls = $args[4]
$resourcecondition = $args[5]
$username = $args[6]
$password = $args[7]
$whichacl = $args[8]

$securepassword = ConvertTo-SecureString -AsPlainText -Force $password

$error.clear();
$credential = New-Object System.Management.Automation.PSCredential ($username,$securepassword)

if (!$error) {
  $error.clear()
  $ignore = Get-ADCentralAccessRule -Identity:"$rulename" -Server:"$server" -Credential:$credential
  
  if ($error) {
    New-ADCentralAccessRule -Name:"$rulename" -Server:"$server" -Credential:$credential
    Set-ADObject -Identity:"$identity" -Server:"$server" -Credential:$credential
  }
  
  if ($whichacl -eq "proposed") {
    if ($resourcecondition -eq "(1==1)") {
      Set-ADCentralAccessRule -CurrentAcl:"O:SYG:SYD:AR(A;;FA;;;WD)" -ProposedAcl:"O:SYG:SYD:AR(A;;FA;;;OW)(A;;FA;;;BA)(A;;FA;;;SY)$useracls" -Description:"$description" -Identity:"$rulename" -ResourceCondition:$null -Server:"$server" -Credential:$credential
    } else {
      Set-ADCentralAccessRule -CurrentAcl:"O:SYG:SYD:AR(A;;FA;;;WD)" -ProposedAcl:"O:SYG:SYD:AR(A;;FA;;;OW)(A;;FA;;;BA)(A;;FA;;;SY)$useracls" -Description:"$description" -Identity:"$rulename" -ResourceCondition:"$resourcecondition" -Server:"$server" -Credential:$credential
    }
  } else {
    if ($resourcecondition -eq "(1==1)") {
      Set-ADCentralAccessRule -CurrentAcl:"O:SYG:SYD:AR(A;;FA;;;OW)(A;;FA;;;BA)(A;;FA;;;SY)$useracls" -Description:"$description" -Identity:"$rulename" -ProposedAcl:$null -ResourceCondition:$null -Server:"$server" -Credential:$credential
    } else {
      Set-ADCentralAccessRule -CurrentAcl:"O:SYG:SYD:AR(A;;FA;;;OW)(A;;FA;;;BA)(A;;FA;;;SY)$useracls" -Description:"$description" -Identity:"$rulename" -ProposedAcl:$null -ResourceCondition:"$resourcecondition" -Server:"$server" -Credential:$credential
    }
  }
}
