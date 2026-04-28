$ErrorActionPreference = "Stop"
. "$PSScriptRoot\android-env.ps1"

Set-Location (Resolve-Path "$PSScriptRoot\..")
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
