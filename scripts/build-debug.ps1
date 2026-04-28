. "$PSScriptRoot\android-env.ps1"

Set-Location (Resolve-Path "$PSScriptRoot\..")
gradle --no-daemon assembleDebug
