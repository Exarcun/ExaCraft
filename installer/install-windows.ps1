# ExaCraft installer for Windows.
# Installs Fabric Loader (creates a launcher profile), then downloads
# Fabric API and the ExaCraft mod jar into the mods folder.
$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$McVersion = '26.2'
$LoaderVersion = '0.19.3'
$FabricInstallerUrl = 'https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.1.1/fabric-installer-1.1.1.jar'
$FabricApiUrl = 'https://cdn.modrinth.com/data/P7dR8mSH/versions/Kr4WG5mG/fabric-api-0.154.2%2B26.2.jar'
$FabricApiFile = 'fabric-api-0.154.2+26.2.jar'
$ModUrl = 'https://raw.githubusercontent.com/Exarcun/ExaCraft/main/dist/exacraft-latest.jar'

$minecraft = Join-Path $env:APPDATA '.minecraft'
if (-not (Test-Path $minecraft)) {
    Write-Host "Could not find $minecraft"
    Write-Host 'Start Minecraft (vanilla) once, close it, then run this installer again.'
    exit 1
}
if (-not (Test-Path (Join-Path $minecraft 'launcher_profiles.json'))) {
    Write-Host 'Minecraft is installed but the launcher has never been run.'
    Write-Host 'Start Minecraft (vanilla) once, close it, then run this installer again.'
    exit 1
}

function Find-Java {
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    # Fall back to the Java runtimes bundled with the Minecraft launcher.
    $roots = @(
        (Join-Path $env:LOCALAPPDATA 'Packages\Microsoft.4297127D64EC6_8wekyb3d8bbwe\LocalCache\Local\runtime'),
        'C:\Program Files (x86)\Minecraft Launcher\runtime',
        (Join-Path $env:LOCALAPPDATA 'Minecraft Launcher\runtime')
    )
    foreach ($root in $roots) {
        if (Test-Path $root) {
            $java = Get-ChildItem $root -Recurse -Filter 'java.exe' -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($java) { return $java.FullName }
        }
    }
    return $null
}

$java = Find-Java
if (-not $java) {
    Write-Host 'Java was not found on this PC.'
    Write-Host 'Install it from https://adoptium.net (any recent version), then run this installer again.'
    exit 1
}
Write-Host "Using Java: $java"

$tmp = Join-Path $env:TEMP 'exacraft-install'
New-Item -ItemType Directory -Force $tmp | Out-Null
$installerJar = Join-Path $tmp 'fabric-installer.jar'

Write-Host 'Step 1/3: Installing Fabric Loader...'
Invoke-WebRequest -UseBasicParsing $FabricInstallerUrl -OutFile $installerJar
& $java -jar $installerJar client -dir $minecraft -mcversion $McVersion -loader $LoaderVersion
if ($LASTEXITCODE -ne 0) {
    Write-Host 'The Fabric installer reported an error (see above).'
    exit 1
}

$mods = Join-Path $minecraft 'mods'
New-Item -ItemType Directory -Force $mods | Out-Null

Write-Host 'Step 2/3: Downloading Fabric API...'
Invoke-WebRequest -UseBasicParsing $FabricApiUrl -OutFile (Join-Path $mods $FabricApiFile)

Write-Host 'Step 3/3: Downloading ExaCraft...'
# Remove older copies of the mod so only one version loads.
Get-ChildItem $mods -Filter 'examinecraft-*.jar' -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem $mods -Filter 'exacraft-*.jar' -ErrorAction SilentlyContinue | Remove-Item -Force
Invoke-WebRequest -UseBasicParsing $ModUrl -OutFile (Join-Path $mods 'exacraft-latest.jar')

Write-Host ''
Write-Host '================================================================'
Write-Host " Done! Open the Minecraft launcher and pick the"
Write-Host " 'fabric-loader-$McVersion' profile, then join the server."
Write-Host '================================================================'
