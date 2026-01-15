#!/bin/bash
set -euo pipefail

#--------------------------------------
# iOS Local Simulator Entrypoint for Jenkins
# - Assumes tools are preinstalled on the macOS agent (Appium/XCUITest)
# - Uses Xcode Command Line Tools (xcrun/xcodebuild)
# - Creates and boots an iOS Simulator
# - Starts local Appium server
# - Runs tests and generates reports
#--------------------------------------

# Global variables and environment setup
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21}"
export PATH=/usr/local/bin:$PATH
export PATH=/opt/homebrew/bin:$PATH

# Inputs (can be overridden via environment variables or CLI)
APPIUM_URL="${APPIUM_URL:-http://127.0.0.1:4725/}"
APPIUM_PORT="${APPIUM_PORT:-4725}"
APPIUM_HOST="${APPIUM_HOST:-127.0.0.1}"
DEVICE_NAME="${DEVICE_NAME:-iPhone 16 Pro}"
PLATFORM_VERSION="${PLATFORM_VERSION:-latest}"
UDID="${UDID:-}"
TAGS="${CUCUMBER_FILTER_TAGS:-@smoke}"
ENV="${ENVIRONMENT:-stg}"
BUILD_NUMBER="${LATEST_BUILD:-"build number is not defined"}"
IOS_APP_PATH="${IOS_APP_PATH:-./FIFA.zip}"
SIM_NAME="${SIM_NAME:-}"
RETRY_COUNT="2"
APPIUM_PID=""
TAIL_PID=""

# Internal state
TEST_RESULT=0

#--------------------------------------
# Check required tools
#--------------------------------------
check_tools() {
    echo "🔧 Checking essential tools..."
    for tool in mvn curl xcrun xcodebuild; do
        if ! command -v "$tool" &>/dev/null; then
            echo "❌ $tool not found in PATH"
            exit 1
        fi
    done
    echo "✅ Core tools are available"
}
## Tools are expected to be preinstalled on the remote macOS agent (Homebrew/Node/Appium/XCUITest).
## This script only verifies presence via check_tools() and will fail fast otherwise.

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
# Detect latest installed iOS runtime version
#--------------------------------------
get_latest_platform_version() {
   xcrun simctl list runtimes \
     | grep '^iOS [0-9]' \
     | sed -E 's/^iOS ([0-9]+(\.[0-9]+)?).*/\1/' \
     | sort -V | tail -1
}

#--------------------------------------
# Create and boot iOS Simulator
#--------------------------------------
setup_simulator() {
    if [[ -z "$SIM_NAME" ]]; then
        SIM_NAME="ci-sim-$(date +%Y%m%d%H%M%S)"
    fi
    if [[ -z "$PLATFORM_VERSION" || "$PLATFORM_VERSION" == "latest" ]]; then
        detected=$(get_latest_platform_version)
        if [[ -z "$detected" ]]; then
            echo "❌ Failed to detect latest iOS runtime version on this agent"
            xcrun simctl list runtimes || true
            exit 1
        fi
        PLATFORM_VERSION="$detected"
        echo "🧭 Detected latest iOS runtime: $PLATFORM_VERSION"
    fi
    echo "🔧 Setting up iOS Simulator: $SIM_NAME ($DEVICE_NAME, iOS $PLATFORM_VERSION)"

    # Find matching runtime identifier for requested iOS version
    local runtime_line
    runtime_line=$(xcrun simctl list runtimes | grep -E "iOS $PLATFORM_VERSION" || true)
    if [[ -z "$runtime_line" ]]; then
        echo "❌ iOS runtime $PLATFORM_VERSION not found on this agent"
        xcrun simctl list runtimes
        exit 1
    fi
    local runtime_id
    runtime_id=$(echo "$runtime_line" | sed -n 's/.*(.*) - \(com\.apple\.CoreSimulator\.SimRuntime\.iOS[-0-9]*\).*/\1/p')
    if [[ -z "$runtime_id" ]]; then
        # Alternative parsing
        runtime_id=$(echo "$runtime_line" | awk -F" - " '{print $NF}')
    fi
    echo "🧭 Using runtime: $runtime_id"

    # Delete existing simulator with same name
    local existing
    existing=$(xcrun simctl list devices | grep -F "($SIM_NAME)" | awk -F'[()]' '{print $2}' || true)
    if [[ -n "$existing" ]]; then
        echo "🗑️  Deleting existing simulator $SIM_NAME ($existing)"
        xcrun simctl delete "$existing" || true
    fi

    # Create simulator
    UDID=$(xcrun simctl create "$SIM_NAME" "$DEVICE_NAME" "$runtime_id")
    echo "✅ Created simulator with UDID: $UDID"

    # Boot and wait until ready
    echo "🚀 Booting simulator..."
    xcrun simctl boot "$UDID" || true
    xcrun simctl bootstatus "$UDID" -b
    echo "✅ Simulator booted"
}

#--------------------------------------
# Start Appium server locally and wait
#--------------------------------------
start_appium_server() {
    echo "🚀 Starting Appium server on port ${APPIUM_PORT}…"
    appium --port "${APPIUM_PORT}" --address "${APPIUM_HOST}" --log-timestamp > appium.log 2>&1 &
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
# Run Maven tests (remote iOS)
#--------------------------------------
run_tests() {
    echo "🧪 Running Maven tests for iOS using Cucumber tags: $TAGS in $ENV environment"
    TEST_RESULT=0
    mvn clean test \
      -Dframework.mobile.hub.url=http://${APPIUM_HOST}:${APPIUM_PORT}/ \
      -Dframework.platform.name=iOS \
      -Dcucumber.filter.tags="$TAGS" \
      -Denvironment="$ENV" \
      -Dframework.platform.version="$PLATFORM_VERSION" \
      -Dframework.device.name="$DEVICE_NAME" \
      -Dframework.mobile.app="./FIFA.zip" \
      -Dframework.mobile.app.version="$BUILD_NUMBER" \
      -Dframework.device.udid="$UDID" \
      -Dframework.failure.retry.test="$RETRY_COUNT" || TEST_RESULT=$?

    if [ "$TEST_RESULT" -eq 0 ]; then
        echo "✅ Tests completed successfully"
    else
        echo "❌ Tests failed with exit code: $TEST_RESULT"
    fi
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
        allure generate target/allure-results --clean -o allure-report || echo "⚠️ Allure report generation failed"
    else
        echo "⚠️ Allure CLI not found or no results to generate"
    fi

    echo "✅ Report generation completed"
}

#--------------------------------------
# Cleanup simulator and background processes
#--------------------------------------
cleanup_simulator() {
    echo "🧼 Cleaning up iOS simulator ($SIM_NAME)"

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

    # Shutdown and delete simulator
    if [[ -n "${UDID:-}" ]]; then
        echo "🛑 Shutting down simulator: $UDID"
        xcrun simctl shutdown "$UDID" || true
        echo "🗑️  Deleting simulator: $UDID"
        xcrun simctl delete "$UDID" || true
    fi
    echo "✅ Cleanup completed"
}

#--------------------------------------
# Show usage information
#--------------------------------------
show_usage() {
    echo "Usage: $0 [OPERATION]"
    echo ""
    echo "Available operations:"
    echo "  check-tools        - Check if required tools are available"
    # tools are expected to be preinstalled on the agent
    echo "  check-disk-space   - Check if enough disk space is available"
    echo "  setup-simulator    - Create and boot iOS simulator"
    echo "  start-appium       - Start local Appium server"
    echo "  run-tests          - Run Maven tests"
    echo "  generate-reports   - Generate test reports"
    echo "  cleanup            - Shutdown and delete simulator"
    echo "  full-run           - End-to-end local simulator run (default)"
    echo "  run-ci             - CI run without cleanup/reports"
    echo "  full-run-ci        - CI run including installs and setup"
    echo ""
    echo "Environment variables:"
    echo "  APPIUM_URL            Appium server URL (default: http://127.0.0.1:4723/)"
    echo "  DEVICE_NAME           iOS device type (default: iPhone 14)"
    echo "  PLATFORM_VERSION      iOS platform version (default: 16.4)"
    echo "  UDID                  iOS simulator UDID (set automatically on create)"
    echo "  IOS_APP_PATH          Path to .ipa/.app/.zip (default: ./FIFA.zip)"
    echo "  CUCUMBER_FILTER_TAGS  Cucumber tags (default: @smoke)"
    echo "  ENVIRONMENT           Test environment (default: stg)"
    echo "  LATEST_BUILD          App version label (default: latest_<env>_build)"
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
        "setup-simulator")
            setup_simulator
            ;;
        "start-appium")
            start_appium_server
            ;;
        "run-tests")
            run_tests
            ;;
        "generate-reports")
            generate_reports
            ;;
        "cleanup")
            cleanup_simulator
            ;;
        "full-run")
            echo "🚀 Starting full iOS local simulator test run..."
            check_tools
            check_disk_space
            setup_simulator
            start_appium_server
            run_tests
            cleanup_simulator
            generate_reports
            echo "✅ Full iOS test run completed"
            ;;
        "run-ci")
            echo "🚀 Starting CI iOS test run..."
            setup_simulator
            start_appium_server
            run_tests
            echo "✅ CI iOS test run completed"
            ;;
        "full-run-ci")
            echo "🚀 Starting CI iOS full test run..."
            check_tools
            check_disk_space
            setup_simulator
            start_appium_server
            run_tests
            echo "✅ CI iOS full test run completed"
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


