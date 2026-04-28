$ErrorActionPreference = "Stop"
. "$PSScriptRoot\android-env.ps1"

Set-Location (Resolve-Path "$PSScriptRoot\..")
.\gradlew.bat --no-daemon clean assembleDebug
