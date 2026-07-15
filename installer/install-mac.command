#!/bin/bash
# ExaCraft installer for macOS. Double-click me!
# Installs Fabric Loader (creates a launcher profile), then downloads
# Fabric API and the ExaCraft mod jar into the mods folder.
set -e

MC_VERSION="26.2"
LOADER_VERSION="0.19.3"
FABRIC_INSTALLER_URL="https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.1.1/fabric-installer-1.1.1.jar"
FABRIC_API_URL="https://cdn.modrinth.com/data/P7dR8mSH/versions/Kr4WG5mG/fabric-api-0.154.2%2B26.2.jar"
FABRIC_API_FILE="fabric-api-0.154.2+26.2.jar"
MOD_URL="https://raw.githubusercontent.com/Exarcun/ExaCraft/main/dist/exacraft-latest.jar"

MINECRAFT="$HOME/Library/Application Support/minecraft"
if [ ! -d "$MINECRAFT" ]; then
    echo "Could not find $MINECRAFT"
    echo "Start Minecraft (vanilla) once, close it, then run this installer again."
    exit 1
fi

# Find Java: PATH first, then the runtime bundled with the Minecraft launcher.
JAVA=""
if command -v java >/dev/null 2>&1 && java -version >/dev/null 2>&1; then
    JAVA="java"
else
    JAVA=$(find "$MINECRAFT/runtime" -type f -name java -path "*/bin/java" 2>/dev/null | head -n 1)
fi
if [ -z "$JAVA" ]; then
    echo "Java was not found on this Mac."
    echo "Install it from https://adoptium.net (any recent version), then run this installer again."
    exit 1
fi
echo "Using Java: $JAVA"

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

echo "Step 1/3: Installing Fabric Loader..."
curl -fL -o "$TMP/fabric-installer.jar" "$FABRIC_INSTALLER_URL"
"$JAVA" -jar "$TMP/fabric-installer.jar" client -dir "$MINECRAFT" -mcversion "$MC_VERSION" -loader "$LOADER_VERSION"

MODS="$MINECRAFT/mods"
mkdir -p "$MODS"

echo "Step 2/3: Downloading Fabric API..."
curl -fL -o "$MODS/$FABRIC_API_FILE" "$FABRIC_API_URL"

echo "Step 3/3: Downloading ExaCraft..."
# Remove older copies of the mod so only one version loads.
rm -f "$MODS"/examinecraft-*.jar "$MODS"/exacraft-*.jar
curl -fL -o "$MODS/exacraft-latest.jar" "$MOD_URL"

echo ""
echo "================================================================"
echo " Done! Open the Minecraft launcher and pick the"
echo " 'fabric-loader-$MC_VERSION' profile, then join the server."
echo "================================================================"
