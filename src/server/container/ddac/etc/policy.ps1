# Create/Update a CAP
$name = $args[0]
$server = $args[1]
$members = $args[2]
$username = $args[3]
$password = $args[4]

$securepassword = ConvertTo-SecureString -AsPlainText -Force $password

$error.clear()
$credential = New-Object System.Management.Automation.PSCredential ($username,$securepassword)

if (!$error) {
  $error.clear()
  $ignore = Get-ADCentralAccessPolicy -Identity:"$name" -Server:"$server" -Credential:$credential
  if ($error) {
      New-ADCentralAccessPolicy -Name:"$name" -Server:"$server" -Credential:$credential
  }
  
  $currentmembers = (Get-ADCentralAccessPolicy -Identity:"$name" -Server:"$server" -Credential:$credential).members
  if ($currentmembers.count -gt 0) {
      Remove-ADCentralAccessPolicyMember -Identity:"$name" -Members:$currentmembers -Server:"$server" -Credential:$credential -Confirm:$false
  }
  
  foreach ($member in $members.split(",")) {
      Add-ADCentralAccessPolicyMember -Identity:"$name" -Members:$member -Server:"$server" -Credential:$credential
  }
}
