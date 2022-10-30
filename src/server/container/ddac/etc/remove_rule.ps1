# Delete a CAR
$server = $args[0]
$rulename = $args[1]
$username = $args[2]
$password = $args[3]

$securepassword = ConvertTo-SecureString -AsPlainText -Force $password

$error.clear();
$credential = New-Object System.Management.Automation.PSCredential ($username,$securepassword)

if (!$error) {
  $error.clear()
  Remove-ADCentralAccessRule -Identity:"$rulename" -Server:"$server" -Credential:$credential -Confirm:$false
}
