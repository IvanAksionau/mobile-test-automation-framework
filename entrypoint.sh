#!/bin/bash
set -euo pipefail

# Global variables and environment setup
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
export ANDROID_HOME=$HOME/.local/android_sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
export PATH=$ANDROID_HOME/emulator:$PATH
export PATH=$ANDROID_HOME/platform-tools:$PATH
export PATH=/usr/local/bin:$PATH
export PATH=/opt/homebrew/bin:$PATH

# Appium configuration
APPIUM_PORT="${APPIUM_PORT:-4723}"
APPIUM_HOST="${APPIUM_HOST:-127.0.0.1}"

# Global variables
AVD_NAME=testAVD
ANDROID_PLATFORM="platforms;android-33"
ANDROID_SYSTEM_IMAGE="system-images;android-33;google_apis;arm64-v8a"
FALLBACK_SYSTEM_IMAGE="system-images;android-33;google_apis_playstore;arm64-v8a"
BUILD_TOOLS="build-tools;34.0.0"
EMULATOR_GPU_MODE="auto"
EMULATOR_PID=""
APPIUM_PID=""
TAIL_PID=""
DEVICE_MODEL="pixel_6"
DEVICE_ID=""
TEST_RESULT=0
TAGS="${CUCUMBER_FILTER_TAGS:-@smoke}"
ENV="${ENVIRONMENT:-stg}"
BUILD_NUMBER="${LATEST_BUILD:-"build number is not defined"}"
RETRY_COUNT="2"

# SDKs to install array
SDK_TO_INSTALL=(
    "platform-tools"
    "emulator"
    "$BUILD_TOOLS"
    "$ANDROID_PLATFORM"
    "$ANDROID_SYSTEM_IMAGE"
)

#--------------------------------------
# Check required tools
#--------------------------------------
check_tools() {
    echo "🔧 Checking essential tools..."
    for tool in sdkmanager avdmanager emulator adb mvn appium; do
        if ! command -v "$tool" &>/dev/null; then
            echo "❌ $tool not found in PATH"
            exit 1
        fi
    done
    echo "✅ All required tools are available"
}

#--------------------------------------
# Check disk space
#--------------------------------------
check_disk_space() {
    echo "🔍 Checking disk space..."
    FREE_GB=$(df -g . | awk 'NR==2 {print $4}')
    if [ "$FREE_GB" -lt 10 ]; then
        echo "⚠️  Warning: Less than 10GB free disk space ($FREE_GB GB). Emulator may fail to start."
    else
        echo "✅ Sufficient disk space: $FREE_GB GB free."
    fi
}

#--------------------------------------
# Install Android SDKs and system images
#--------------------------------------
install_sdks() {
    echo "📦 Installing Android SDKs..."
    sdkmanager --update

    # Install each SDK from the array
    for sdk in "${SDK_TO_INSTALL[@]}"; do
        echo "Installing SDK: $sdk"
        echo y | sdkmanager "$sdk"
    done

    echo "✅ Android SDKs installation completed"
}

#--------------------------------------
# Setup AVD (with fallback)
#--------------------------------------
setup_avd() {
    echo "🔧 Setting up AVD: $AVD_NAME"
    avdmanager create avd \
        -n "$AVD_NAME" \
        -k "$ANDROID_SYSTEM_IMAGE" \
        --device "$DEVICE_MODEL" \
        --force || {
        echo "⚠️  Failed to create AVD with $ANDROID_SYSTEM_IMAGE, trying fallback image..."
        sdkmanager --install "$FALLBACK_SYSTEM_IMAGE"
        avdmanager create avd \
            -n "$AVD_NAME" \
            -k "$FALLBACK_SYSTEM_IMAGE" \
            --device "$DEVICE_MODEL" \
            --force
    }
    avdmanager list devices
    echo "✅ AVD setup completed"
}

#--------------------------------------
# Start emulator (no -no-skin, configurable GPU)
#--------------------------------------
start_emulator() {
    echo "🚀 Starting emulator..."
    # Pre-emulator cleanup
    pkill -f emulator || true
    adb kill-server || true
    emulator \
        -avd "$AVD_NAME" \
        -no-window \
        -wipe-data \
        -no-audio \
        -no-boot-anim \
        -gpu $EMULATOR_GPU_MODE \
        -camera-back none \
        -camera-front none \
        -no-snapshot \
        -memory 8192 \
        -partition-size 8192 \
        -cores 4 \
        -accel on \
        -verbose \
        2>&1 | tee emulator.log &
    EMULATOR_PID=$!
    echo "✅ Emulator started with PID: $EMULATOR_PID"
}

#--------------------------------------
# Wait for emulator to be ready
#--------------------------------------
wait_for_emulator() {
    echo "⏳ Waiting for emulator to connect via ADB..."
    adb wait-for-device

    DEVICE_ID=$(adb devices | awk 'NR==2 {print $1}')

    echo "⏳ Waiting for emulator to boot (max 500s)..."
    boot_timeout=500
    elapsed=0
    until [[ "$(adb -s "$DEVICE_ID" shell getprop sys.boot_completed | tr -d '\r')" == "1" ]]; do
      sleep 5
      elapsed=$((elapsed + 5))
      if [ "$elapsed" -ge "$boot_timeout" ]; then
        echo "❌ Emulator failed to boot within ${boot_timeout}s"
        adb -s "$DEVICE_ID" shell getprop
        exit 1
      fi
    done
    echo "✅ Emulator boot_completed in ${elapsed}s"

    # Wait for boot animation to stop
    echo "⏳ Waiting for boot animation to stop..."
    anim_elapsed=0
    until [[ "$(adb -s "$DEVICE_ID" shell getprop init.svc.bootanim | tr -d '\r')" == "stopped" ]]; do
      sleep 2
      anim_elapsed=$((anim_elapsed + 2))
      if [ "$anim_elapsed" -ge "$boot_timeout" ]; then
        echo "❌ Emulator boot animation did not stop within ${boot_timeout}s"
        exit 1
      fi
    done
    echo "✅ Boot animation stopped after $((elapsed + anim_elapsed))s"

    # Check System UI is running
    if ! adb -s "$DEVICE_ID" shell pidof com.android.systemui | grep -qE '\\S'; then
      echo "⚠️  System UI process is not running! You may see UI errors."
    else
      echo "✅ System UI process is running."
    fi
}

#--------------------------------------
# Wait for emulator to be ready
#--------------------------------------
start_appium_server() {
  echo "🚀 Starting Appium server on port ${APPIUM_PORT}…"
  # Stream output to both console and optional file (in case you want to persist it too)
  appium --port "${APPIUM_PORT}" --address "${APPIUM_HOST}" --allow-insecure '*:adb_shell' --log-timestamp > appium.log 2>&1 &
  APPIUM_PID=$!
  tail -F appium.log &
  TAIL_PID=$!

  # wait up to 60s for Appium to be "ready":
  n=0
  until curl -s "http://${APPIUM_HOST}:${APPIUM_PORT}/status" | grep -q '"ready":true' || [ $n -ge 300 ]; do
    echo "⏳ Waiting for Appium… ($n)"
    sleep 1
    n=$((n+1))
  done

  if [ $n -ge 300 ]; then
    echo "❌ Appium failed to start"
    exit 1
  fi

  echo "✅ Appium is ready."
}

#--------------------------------------
# Run Maven tests
#--------------------------------------
run_tests() {
# Detect platform version
    PLATFORM_VERSION=$(adb -s "$DEVICE_ID" shell getprop ro.build.version.release | tr -d '\r')
    echo "📱 Detected platform version: $PLATFORM_VERSION"

    echo "🧪 Running Maven tests using Cucumber tags: $TAGS in $ENV environment"
    TEST_RESULT=0
    mvn clean test \
      -Dframework.mobile.hub.url=http://${APPIUM_HOST}:${APPIUM_PORT}/ \
      -Dframework.platform.name=Android \
      -Dcucumber.filter.tags="$TAGS" \
      -Denvironment="$ENV" \
      -Dframework.platform.version="$PLATFORM_VERSION" \
      -Dframework.device.name="$DEVICE_MODEL" \
      -Dframework.mobile.app="./FIFA.apk" \
      -Dframework.mobile.app.version="$BUILD_NUMBER" \
      -Dframework.device.udid="$DEVICE_ID" \
      -Dframework.failure.retry.test="$RETRY_COUNT" || TEST_RESULT=$?
    
    if [ "$TEST_RESULT" -eq 0 ]; then
        echo "✅ Tests completed successfully"
    else
        echo "❌ Tests failed with exit code: $TEST_RESULT"
    fi
}

#--------------------------------------
# Cleanup emulator
#--------------------------------------
cleanup_emulator() {
    echo "🧼 Cleaning up emulator ($AVD_NAME)"
    
    # Kill Appium process if running
    if [[ -n "$APPIUM_PID" ]]; then
        echo "🔪 Killing Appium process: $APPIUM_PID"
        kill "$APPIUM_PID" || true
    fi
    
    # Kill tail process if running
    if [[ -n "$TAIL_PID" ]]; then
        echo "🔪 Killing tail process: $TAIL_PID"
        kill "$TAIL_PID" || true
    fi
    
    # Kill emulator process if running
    if [[ -n "$EMULATOR_PID" ]]; then
        echo "🔪 Killing emulator process: $EMULATOR_PID"
        kill "$EMULATOR_PID" || true
    fi
    
    # Delete AVD
    avdmanager delete avd -n "$AVD_NAME" || echo "⚠️ Failed to delete AVD or AVD doesn't exist"
    echo "✅ Cleanup completed"
}

#--------------------------------------
# Generate reports
#--------------------------------------
generate_reports() {
    echo "📊 Generating reports..."

    if [ "$TEST_RESULT" -ne 0 ]; then
        echo "⚠️ Tests failed. Showing logs..."
        find target/surefire-reports -name "*.txt" -exec cat {} \; || echo "⚠️ No test logs found"
    fi

    if command -v allure &>/dev/null && [ -d target/allure-results ]; then
        echo "📊 Generating Allure report..."
        allure generate target/allure-results --clean -o allure-report
        GEN_EXIT=$?
        if [ "$GEN_EXIT" -ne 0 ]; then
            echo "⚠️ Allure report generation failed"
            exit $GEN_EXIT
        fi
    else
        echo "⚠️ Allure CLI not found or no results to generate"
    fi

    echo "✅ Report generation completed"
}

#--------------------------------------
# Show usage information
#--------------------------------------
show_usage() {
    echo "Usage: $0 [OPERATION]"
    echo ""
    echo "Available operations:"
    echo "  check-tools        - Check if required tools are available"
    echo "  install-sdks       - Install Android SDKs and system images"
    echo "  setup-avd          - Create and setup AVD"
    echo "  start-emulator     - Start the emulator"
    echo "  wait-emulator      - Wait for emulator to be ready"
    echo "  run-tests          - Run Maven tests"
    echo "  cleanup            - Cleanup emulator and AVD"
    echo "  generate-reports   - Generate test reports"
    echo "  full-run           - Run complete test suite (default)"
    echo "  full-run-ci        - Run test suite for CI (no cleanup/reports)"
    echo ""
    echo "Examples:"
    echo "  $0                 # Run complete test suite"
    echo "  $0 full-run        # Run complete test suite"
    echo "  $0 full-run-ci     # Run test suite for CI without cleanup"
    echo "  $0 install-sdks    # Only install Android SDKs"
    echo "  $0 setup-avd       # Only setup AVD"
    echo "  $0 start-emulator  # Only start emulator"
    echo "  $0 run-tests       # Only run tests (assumes emulator is ready)"
}

#--------------------------------------
# Main function
#--------------------------------------
main() {
    local operation="${1:-full-run}"
    
    case "$operation" in
        "check-tools")
            check_tools
            ;;
        "check-disk-space")
            check_disk_space
            ;;
        "install-sdks")
            install_sdks
            ;;
        "setup-avd")
            setup_avd
            ;;
        "start-emulator")
            start_emulator
            ;;
        "wait-emulator")
            wait_for_emulator
            ;;
        "start-appium-server")
            start_appium_server
            ;;
        "run-tests")
            run_tests
            ;;
        "cleanup")
            cleanup_emulator
            ;;
        "generate-reports")
            generate_reports
            ;;
        "full-run")
            echo "🚀 Starting full test run..."
            check_tools
            setup_avd
            start_emulator
            wait_for_emulator
            start_appium_server
            run_tests
            cleanup_emulator
            generate_reports
            echo "✅ Full test run completed"
            ;;
        "run-ci")
            echo "🚀 Starting CI test run..."
            setup_avd
            start_emulator
            wait_for_emulator
            start_appium_server
            run_tests
            echo "✅ CI test run completed"
            ;;
        "full-run-ci")
            echo "🚀 Starting CI full test run..."
            install_sdks
            check_tools
            check_disk_space
            setup_avd
            start_emulator
            wait_for_emulator
            start_appium_server
            run_tests
            echo "✅ CI test run completed"
            ;;
        "help"|"-h"|"--help")
            show_usage
            exit 0
            ;;
        *)
            echo "❌ Unknown operation: $operation"
            echo ""
            show_usage
            exit 1
            ;;
    esac
    
    exit "$TEST_RESULT"
}

# Call main function with all arguments
main "$@"
