#!/bin/bash
# Helper script to find JDK 21 installation on macOS
# This helps configure org.gradle.java.home in local.properties

echo "Searching for JDK 21 installations..."

# Check common Homebrew location
if [ -d "/opt/homebrew/opt/openjdk@21" ]; then
    echo "Found Homebrew OpenJDK 21:"
    echo "  /opt/homebrew/opt/openjdk@21"
    echo ""
    echo "Add to local.properties:"
    echo "  org.gradle.java.home=/opt/homebrew/opt/openjdk@21"
fi

# Check Intel Homebrew location
if [ -d "/usr/local/opt/openjdk@21" ]; then
    echo "Found Homebrew OpenJDK 21 (Intel):"
    echo "  /usr/local/opt/openjdk@21"
    echo ""
    echo "Add to local.properties:"
    echo "  org.gradle.java.home=/usr/local/opt/openjdk@21"
fi

# Check SDKMAN
if [ -d "$HOME/.sdkman/candidates/java" ]; then
    echo "Found SDKMAN Java installations:"
    ls -d "$HOME/.sdkman/candidates/java"/*/ 2>/dev/null | grep -E "(21|17)" | while read dir; do
        echo "  $dir"
    done
    echo ""
    echo "Add to local.properties (replace with actual version):"
    echo "  org.gradle.java.home=\$HOME/.sdkman/candidates/java/21.0.x"
fi

# Check /Library/Java/JavaVirtualMachines
if [ -d "/Library/Java/JavaVirtualMachines" ]; then
    echo "Found system Java installations:"
    ls -d "/Library/Java/JavaVirtualMachines"/*/Contents/Home 2>/dev/null | while read dir; do
        version=$(java -version 2>&1 | head -1)
        echo "  $dir"
    done
fi

# Check if JAVA_HOME is set
if [ -n "$JAVA_HOME" ]; then
    echo ""
    echo "JAVA_HOME is currently set to:"
    echo "  $JAVA_HOME"
    if [ -f "$JAVA_HOME/bin/jlink" ]; then
        echo "  ✓ jlink found - this JDK should work"
        echo ""
        echo "Add to local.properties:"
        echo "  org.gradle.java.home=$JAVA_HOME"
    else
        echo "  ✗ jlink not found - this JDK is incomplete"
    fi
fi

echo ""
echo "To fix the 'jlink executable does not exist' error:"
echo "1. Copy local.properties.example to local.properties (if it doesn't exist)"
echo "2. Add the org.gradle.java.home line with one of the paths above"
echo "3. Run ./gradlew clean build"
