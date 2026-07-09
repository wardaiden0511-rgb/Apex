[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

Write-Output '--- loom versions ---'
$loomUrl = 'https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml'
$loomText = Invoke-WebRequest -Uri $loomUrl -UseBasicParsing | Select-Object -ExpandProperty Content
[xml]$loomXml = $loomText
$loomXml.versioning.versions.version | Select-Object -Last 40 | ForEach-Object { Write-Output $_ }

Write-Output '--- yarn 1.21.11 entries ---'
$yarnUrl = 'https://maven.fabricmc.net/net/fabricmc/yarn/'
$yarnText = Invoke-WebRequest -Uri $yarnUrl -UseBasicParsing | Select-Object -ExpandProperty Content
$yarnText -split "`n" | Where-Object { $_ -match '1\.21\.11' } | ForEach-Object { Write-Output $_ }
