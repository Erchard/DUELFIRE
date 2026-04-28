$env:JAVA_HOME = "D:\AndroidTools\jdk17"
$env:ANDROID_SDK_ROOT = "D:\AndroidTools\android-sdk"
$env:ANDROID_HOME = "D:\AndroidTools\android-sdk"
$env:GRADLE_USER_HOME = "D:\AndroidTools\gradle-cache"

$paths = @(
    "$env:JAVA_HOME\bin",
    "$env:ANDROID_SDK_ROOT\cmdline-tools\latest\bin",
    "$env:ANDROID_SDK_ROOT\platform-tools",
    "D:\AndroidTools\gradle\bin"
)

$env:Path = ($paths -join ";") + ";" + $env:Path
