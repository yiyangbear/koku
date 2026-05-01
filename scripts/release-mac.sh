#!/bin/bash
set -e

APP_NAME="Koku"
JAR_NAME="koku-1.0-SNAPSHOT.jar"
MAIN_CLASS="com.example.koku.app.KokuLauncher"
PACKAGE_ID="com.yiyangbear.koku"
ICON_PNG="src/main/resources/icons/koku.png"
ICON_ICNS="build/koku.icns"

VERSION="$1"

if [ -z "$VERSION" ]; then
  echo "Usage: ./scripts/release-mac.sh <version>"
  echo "Example: ./scripts/release-mac.sh 1.0.1"
  exit 1
fi

echo "======================================"
echo "Packaging $APP_NAME version $VERSION"
echo "======================================"

echo ""
echo "Step 1: Checking project root..."
if [ ! -f "pom.xml" ]; then
  echo "Error: pom.xml not found."
  echo "Please run this script from the project root."
  exit 1
fi

echo ""
echo "Step 2: Checking tools..."
command -v java >/dev/null 2>&1 || { echo "Error: java not found"; exit 1; }
command -v javac >/dev/null 2>&1 || { echo "Error: javac not found"; exit 1; }
command -v jpackage >/dev/null 2>&1 || { echo "Error: jpackage not found"; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "Error: mvn not found"; exit 1; }

java -version
jpackage --version
mvn -version | head -n 1

echo ""
echo "Step 3: Checking launcher source..."
if [ ! -f "src/main/java/com/example/koku/app/KokuLauncher.java" ]; then
  echo "KokuLauncher.java not found. Creating it..."
  mkdir -p src/main/java/com/example/koku/app
  cat > src/main/java/com/example/koku/app/KokuLauncher.java <<'LAUNCHER'
package com.example.koku.app;

public class KokuLauncher {
    public static void main(String[] args) {
        AppLauncher.main(args);
    }
}
LAUNCHER
fi

echo ""
echo "Step 4: Cleaning old build..."
rm -rf build
mkdir -p build/package-input
mkdir -p build/dist

echo ""
echo "Step 5: Building JAR..."
mvn clean package

echo ""
echo "Step 6: Checking main JAR..."
if [ ! -f "target/$JAR_NAME" ]; then
  echo "Error: target/$JAR_NAME not found."
  echo "Check the actual JAR name in target/ and update JAR_NAME in this script."
  exit 1
fi

echo ""
echo "Step 7: Checking KokuLauncher in JAR..."
if ! jar tf "target/$JAR_NAME" | grep -q "com/example/koku/app/KokuLauncher.class"; then
  echo "Error: KokuLauncher.class not found inside target/$JAR_NAME"
  echo "Check src/main/java/com/example/koku/app/KokuLauncher.java"
  exit 1
fi

echo ""
echo "Step 8: Preparing package input..."
cp "target/$JAR_NAME" build/package-input/

mvn dependency:copy-dependencies \
  -DincludeScope=runtime \
  -DoutputDirectory=build/package-input

echo ""
echo "Step 9: Checking JavaFX dependencies..."
if ! ls build/package-input | grep -q "javafx"; then
  echo "Error: JavaFX dependencies were not copied into build/package-input."
  echo "The app would fail with: JavaFX runtime components are missing."
  exit 1
fi

echo ""
echo "Step 10: Preparing icon..."
if [ -f "$ICON_PNG" ]; then
  rm -rf build/icon.iconset
  mkdir -p build/icon.iconset

  sips -z 16 16     "$ICON_PNG" --out build/icon.iconset/icon_16x16.png
  sips -z 32 32     "$ICON_PNG" --out build/icon.iconset/icon_16x16@2x.png
  sips -z 32 32     "$ICON_PNG" --out build/icon.iconset/icon_32x32.png
  sips -z 64 64     "$ICON_PNG" --out build/icon.iconset/icon_32x32@2x.png
  sips -z 128 128   "$ICON_PNG" --out build/icon.iconset/icon_128x128.png
  sips -z 256 256   "$ICON_PNG" --out build/icon.iconset/icon_128x128@2x.png
  sips -z 256 256   "$ICON_PNG" --out build/icon.iconset/icon_256x256.png
  sips -z 512 512   "$ICON_PNG" --out build/icon.iconset/icon_256x256@2x.png
  sips -z 512 512   "$ICON_PNG" --out build/icon.iconset/icon_512x512.png
  sips -z 1024 1024 "$ICON_PNG" --out build/icon.iconset/icon_512x512@2x.png

  iconutil -c icns build/icon.iconset -o "$ICON_ICNS"
else
  echo "Warning: icon PNG not found at $ICON_PNG"
  echo "The app will be packaged without a custom icon."
  ICON_ICNS=""
fi

echo ""
echo "Step 11: Creating app image for testing..."
rm -rf build/dist
mkdir -p build/dist

if [ -f "$ICON_ICNS" ]; then
  jpackage \
    --type app-image \
    --name "$APP_NAME" \
    --app-version "$VERSION" \
    --input build/package-input \
    --main-jar "$JAR_NAME" \
    --main-class "$MAIN_CLASS" \
    --dest build/dist \
    --icon "$ICON_ICNS" \
    --mac-package-name "$APP_NAME" \
    --mac-package-identifier "$PACKAGE_ID"
else
  jpackage \
    --type app-image \
    --name "$APP_NAME" \
    --app-version "$VERSION" \
    --input build/package-input \
    --main-jar "$JAR_NAME" \
    --main-class "$MAIN_CLASS" \
    --dest build/dist \
    --mac-package-name "$APP_NAME" \
    --mac-package-identifier "$PACKAGE_ID"
fi

echo ""
echo "Step 12: Testing app launcher..."
if [ ! -f "build/dist/$APP_NAME.app/Contents/MacOS/$APP_NAME" ]; then
  echo "Error: app launcher not found."
  exit 1
fi

echo ""
echo "App image created successfully:"
echo "build/dist/$APP_NAME.app"

echo ""
echo "Now testing the app briefly..."
"build/dist/$APP_NAME.app/Contents/MacOS/$APP_NAME" &
APP_PID=$!

sleep 3

if ps -p $APP_PID > /dev/null 2>&1; then
  echo "App seems to be running. Closing test process..."
  kill $APP_PID >/dev/null 2>&1 || true
else
  echo "The app process ended quickly."
  echo "If this is unexpected, run this manually to see the error:"
  echo "build/dist/$APP_NAME.app/Contents/MacOS/$APP_NAME"
  exit 1
fi

echo ""
echo "Step 13: Creating DMG..."
rm -rf build/dist
mkdir -p build/dist

if [ -f "$ICON_ICNS" ]; then
  jpackage \
    --type dmg \
    --name "$APP_NAME" \
    --app-version "$VERSION" \
    --input build/package-input \
    --main-jar "$JAR_NAME" \
    --main-class "$MAIN_CLASS" \
    --dest build/dist \
    --icon "$ICON_ICNS" \
    --mac-package-name "$APP_NAME" \
    --mac-package-identifier "$PACKAGE_ID"
else
  jpackage \
    --type dmg \
    --name "$APP_NAME" \
    --app-version "$VERSION" \
    --input build/package-input \
    --main-jar "$JAR_NAME" \
    --main-class "$MAIN_CLASS" \
    --dest build/dist \
    --mac-package-name "$APP_NAME" \
    --mac-package-identifier "$PACKAGE_ID"
fi

echo ""
echo "======================================"
echo "Done!"
echo "======================================"
echo ""
echo "Release files:"
ls -lh build/dist
echo ""
echo "Open the DMG with:"
echo "open build/dist/*.dmg"
