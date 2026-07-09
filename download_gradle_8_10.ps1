[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$out = 'C:\workspace\gradle-8.10-bin.zip'
$dest = 'C:\workspace\gradle-8.10'

if (-Not (Test-Path $out)) {
    Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.10-bin.zip' -OutFile $out
}

if (-Not (Test-Path $dest)) {
    Expand-Archive -Path $out -DestinationPath 'C:\workspace' -Force
    Rename-Item -Path 'C:\workspace\gradle-8.10' -NewName 'gradle-8.10' -ErrorAction SilentlyContinue
}

Get-ChildItem "$dest\bin\gradle.bat" | Select-Object FullName
