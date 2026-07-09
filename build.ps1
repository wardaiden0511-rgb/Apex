# build.ps1 - helper to create Gradle wrapper (if Gradle installed) and build the project

$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $root

if (Test-Path ./gradlew.bat) {
    Write-Host "Found gradlew.bat, running build..."
    & .\gradlew.bat build
    exit $LASTEXITCODE
}

# Try to run 'gradle wrapper' if gradle is installed
$gradle = Get-Command gradle -ErrorAction SilentlyContinue
if ($gradle) {
    Write-Host "Gradle found. Generating wrapper..."
    gradle wrapper
    if (Test-Path ./gradlew.bat) {
        Write-Host "Wrapper generated. Running build..."
        & .\gradlew.bat build
        exit $LASTEXITCODE
    } else {
        Write-Error "Failed to generate wrapper."
        exit 2
    }
} else {
    Write-Error "No Gradle wrapper found and 'gradle' not in PATH. Install Gradle or generate wrapper on another machine."
    exit 1
}
