# Mobile Test Automation Framework

## Overview

This is a **Mobile Test Automation Framework** designed for testing **mobile native applications** using **Appium,
Selenium, and Cucumber**. The framework is built with **Kotlin** and **Maven**, ensuring modularity, scalability, and
maintainability for test automation.

## Tech Stack

- **Kotlin** – Programming language for test scripts
- **Maven** – Dependency and build management
- **Selenium** – WebDriver for UI automation
- **Appium** – Mobile application automation (Android & iOS)
- **Cucumber** – BDD framework for test execution
- **TestNG** – Test execution and reporting
- **Allure** – Reporting and test logs

## Project Structure

```
src
├── main
│   └── kotlin
│       └── com
│           └── ivan
│               └── automation
│                   └── core
│                       └── mobile
│                           ├── allure           # Allure logging helpers
│                           ├── api              # Backend API models/services/wrappers
│                           ├── assertions       # Soft assertions utilities
│                           ├── driver           # Appium driver management (SOLID refactored)
│                           │   ├── capabilities # Platform-specific capabilities (SRP)
│                           │   ├── factory      # Driver factory implementations (OCP)
│                           │   ├── interfaces   # Driver abstractions (DIP)
│                           │   └── manager      # Driver lifecycle management (SRP)
│                           ├── environment      # Environment/config management
│                           ├── hooks            # Test lifecycle management (SOLID refactored)
│                           │   ├── interfaces   # Hook abstractions (ISP)
│                           │   └── lifecycle    # Specific lifecycle hooks (SRP)
│                           ├── listener         # Test event listeners
│                           ├── screen           # Base screen abstractions
│                           └── utils            # Utilities (properties, waits, context)
└── test
    ├── kotlin
    │   └── com
    │       └── ivan
    │           └── automation
    │               └── tests
    │                   ├── questions           # Assertions/verification layer
    │                   ├── runner              # TestNG + Cucumber runner
    │                   ├── screens             # Page/screen objects per feature
    │                   ├── stepsdefinitions    # Cucumber steps
    │                   └── tasks               # Business flows/tasks
    └── resources
        ├── features           # Cucumber feature files
        ├── properties         # Environments (common, stg, int-2)
        └── suites             # TestNG suite(s) → runner
```

### Key Files and Classes

#### Test Execution
- [`TestRunner.kt`](src/test/kotlin/com/ivan/automation/tests/runner/TestRunner.kt) — Cucumber/TestNG runner (tags, plugins, glue, Allure integration)
- [`TestListener.kt`](src/main/kotlin/com/ivan/automation/core/mobile/listener/TestListener.kt) — TestNG listener for reporting and events

#### Driver Management (SOLID Refactored)
- **Interfaces** (Dependency Inversion Principle):
  - [`IDriverFactory`](src/main/kotlin/com/ivan/automation/core/mobile/driver/interfaces/IDriverFactory.kt) — Driver creation abstraction
  - [`IDriverManager`](src/main/kotlin/com/ivan/automation/core/mobile/driver/interfaces/IDriverManager.kt) — Driver lifecycle management
  - [`ICapabilitiesProvider`](src/main/kotlin/com/ivan/automation/core/mobile/driver/interfaces/ICapabilitiesProvider.kt) — Platform capabilities abstraction

- **Implementations** (Single Responsibility Principle):
  - [`AppiumDriverFactory`](src/main/kotlin/com/ivan/automation/core/mobile/driver/factory/AppiumDriverFactory.kt) — Creates Appium drivers
  - [`ThreadLocalDriverManager`](src/main/kotlin/com/ivan/automation/core/mobile/driver/manager/ThreadLocalDriverManager.kt) — Manages driver lifecycle
  - [`AndroidCapabilitiesProvider`](src/main/kotlin/com/ivan/automation/core/mobile/driver/capabilities/AndroidCapabilitiesProvider.kt) — Android-specific capabilities
  - [`IOSCapabilitiesProvider`](src/main/kotlin/com/ivan/automation/core/mobile/driver/capabilities/IOSCapabilitiesProvider.kt) — iOS-specific capabilities
  - [`BrowserStackOptionsProvider`](src/main/kotlin/com/ivan/automation/core/mobile/driver/capabilities/BrowserStackOptionsProvider.kt) — BrowserStack configuration

- **Legacy Support** (Backward Compatibility):
  - [`Device.kt`](src/main/kotlin/com/ivan/automation/core/mobile/driver/Device.kt) — Delegates to new factory pattern
  - [`Capabilities.kt`](src/main/kotlin/com/ivan/automation/core/mobile/driver/Capabilities.kt) — Legacy capabilities (deprecated)

#### Test Lifecycle Hooks (SOLID Refactored)
- **Interfaces** (Interface Segregation Principle):
  - [`ITestLifecycleHook`](src/main/kotlin/com/ivan/automation/core/mobile/hooks/interfaces/ITestLifecycleHook.kt) — Test lifecycle abstraction

- **Implementations** (Single Responsibility Principle):
  - [`DriverLifecycleHook`](src/main/kotlin/com/ivan/automation/core/mobile/hooks/lifecycle/DriverLifecycleHook.kt) — Driver initialization/cleanup
  - [`TestContextLifecycleHook`](src/main/kotlin/com/ivan/automation/core/mobile/hooks/lifecycle/TestContextLifecycleHook.kt) — Test context management
  - [`DeviceRegistrationLifecycleHook`](src/main/kotlin/com/ivan/automation/core/mobile/hooks/lifecycle/DeviceRegistrationLifecycleHook.kt) — Device registration

#### Other Core Components
- [`AllureLogger.kt`](src/main/kotlin/com/ivan/automation/core/mobile/allure/AllureLogger.kt) — Logging helpers for Allure steps/attachments
- [`MobileBaseScreen.kt`](src/main/kotlin/com/ivan/automation/core/mobile/screen/MobileBaseScreen.kt) — Base screen abstractions for Page Object implementations
- [`TestDataManager.kt`](src/main/kotlin/com/ivan/automation/core/mobile/utils/TestDataManager.kt) — Facade for test data operations
- [`TestContextManager.kt`](src/main/kotlin/com/ivan/automation/core/mobile/utils/TestContextManager.kt) — Thread-safe test context management

#### Configuration
- `src/test/resources/properties/common.properties` — Local execution defaults (platform, app path, device/udid)
- `src/test/resources/properties/stg.properties`, `int-2.properties` — Environment-specific overrides
- `src/test/resources/suites/suite.xml` — TestNG suite configuration

#### Scripts
- `entrypoint.sh` — Android: SDK/AVD/emulator/Appium orchestration + Maven test execution
- `entrypoint_ios.sh` — iOS: Simulator/Appium orchestration + Maven test execution

## Getting Started

### Prerequisites

Ensure the following are installed:

- **Java (JDK 17 or later)**
- **Maven**
- **Node.js & npm** (for Appium)
- **Appium Server**
- **Android SDK / Xcode** (for mobile testing)
- **Device / Emulator / Simulator**

### macOS Setup for Jenkins Pipeline (local agent)

The following commands are required for macOS setup to support the Jenkins pipeline:

```sh
brew install openjdk@21
sudo ln -sfn $(brew --prefix)/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk
brew install android-commandlinetools
brew install node
npm install -g appium
brew install maven
brew install allure
```

### Installation

```sh
mvn clean install -U -DskipTests
```

## Configuration

### Properties files

Base config lives in `src/test/resources/properties`:
- `common.properties` – main local overrides (platform, device, app path, timeouts)
- `stg.properties`, `int-2.properties` – environment-specific API/app settings

Example from `common.properties`:
```properties
framework.platform.name=Android       # Android or iOS
framework.platform.version=15         # Platform version
framework.device.name=iPhone 14 Plus  # Cosmetic for remote; used in reports
framework.device.udid=<device-udid>   # iOS Simulator/real device UDID
framework.mobile.app=/path/to/app     # .apk/.ipa/.zip
framework.mobile.hub.url=http://127.0.0.1:4723/
```

### Switching environments

Use `-Denvironment=<name>` or set env var `ENVIRONMENT`:
```sh
mvn test -Denvironment=stg
mvn test -Denvironment=int-2
```
You can also override any property via `-D` at runtime:
```sh
mvn test \
  -Dframework.platform.name=iOS \
  -Dframework.platform.version=16.4 \
  -Dframework.device.name="iPhone 14" \
  -Dframework.device.udid=<SIM-UDID> \
  -Dframework.mobile.app="/path/to/IVAN.zip"
```

## Running Tests

### 1) Run a specific feature file
```sh
mvn test -Dcucumber.features=src/test/resources/features/home_screen_test.feature
```

### 2) Run with tags
```sh
mvn test -Dcucumber.filter.tags="@smoke"
```

### 3) Run all tests
```sh
mvn test
```

### 4) Generate static code analysis report (Detekt)
```sh
mvn detekt:check
```

## Android Entrypoint (local emulator)

Script: `./entrypoint.sh`

Operations:
- `install-sdks` – install Android SDK components and images
- `setup-avd` – create emulator AVD (with fallback)
- `start-emulator` – boot emulator headless
- `wait-emulator` – wait until fully booted
- `start-appium-server` – start local Appium server on 4723
- `run-tests` – run Maven tests with detected platform version
- `cleanup` – shutdown and delete AVD; stop background processes
- `generate-reports` – generate Allure report if results exist
- `full-run` – full local run (default)
- `full-run-ci` – CI-oriented full run (includes installs)
- `run-ci` – CI run without cleanup/reports

Examples:
```sh
# Full local run
./entrypoint.sh

# CI full run
./entrypoint.sh full-run-ci

# Only run tests assuming emulator/appium ready
./entrypoint.sh run-tests
```

Environment overrides used by the script:
- `CUCUMBER_FILTER_TAGS` (default: `@smoke`)
- `ENVIRONMENT` (default: `stg`)
- `LATEST_BUILD` (default: `latest_<env>_build`)

The script auto-detects Android platform version via `adb` and passes
`framework.mobile.hub.url`, `framework.platform.name`, `framework.device.udid`, and the app path `./IVAN.apk` to Maven.

## iOS Entrypoint (local simulator)

Script: `./entrypoint_ios.sh`

Operations:
- `setup-simulator` – create and boot an iOS Simulator
- `start-appium` – start local Appium server on 4723
- `run-tests` – run Maven tests for iOS
- `generate-reports` – generate Allure report if results exist
- `cleanup` – shutdown & delete the simulator
- `full-run` – end-to-end iOS local run (default)
- `run-ci` / `full-run-ci` – CI variants

Configurable via env vars:
- `APPIUM_URL` (default: `http://127.0.0.1:4723/`)
- `DEVICE_NAME` (default: `iPhone 14`)
- `PLATFORM_VERSION` (default: latest detected)
- `UDID` (autofilled after create)
- `IOS_APP_PATH` (default: `./IVAN.zip`)
- `CUCUMBER_FILTER_TAGS` (default: `@smoke`)
- `ENVIRONMENT` (default: `stg`)
- `LATEST_BUILD` (default: `latest_<env>_build`)

Examples:
```sh
# Full iOS local run
./entrypoint_ios.sh

# Specify device and version
DEVICE_NAME="iPhone 14 Plus" PLATFORM_VERSION=16.4 ./entrypoint_ios.sh full-run
```

## Reports & Logs

- **Allure Reports** (local):
```sh
mvn allure:serve
```
- If using the entrypoint scripts, an HTML report can be generated into `./allure-report` when Allure CLI is installed.

## Architecture & Design Principles

This framework follows **SOLID principles** for better maintainability, testability, and extensibility:

- **Single Responsibility Principle (SRP)**: Each class has one well-defined purpose
- **Open/Closed Principle (OCP)**: Extensible without modifying existing code
- **Liskov Substitution Principle (LSP)**: Implementations are interchangeable
- **Interface Segregation Principle (ISP)**: Focused, minimal interfaces
- **Dependency Inversion Principle (DIP)**: Depends on abstractions, not concretions

For detailed information about the SOLID refactoring, see [`docs/SOLID_PRINCIPLES_REFACTORING.md`](docs/SOLID_PRINCIPLES_REFACTORING.md).

### Key Design Patterns

- **Factory Pattern**: [`AppiumDriverFactory`](src/main/kotlin/com/ivan/automation/core/mobile/driver/factory/AppiumDriverFactory.kt) for driver creation
- **Strategy Pattern**: [`ICapabilitiesProvider`](src/main/kotlin/com/ivan/automation/core/mobile/driver/interfaces/ICapabilitiesProvider.kt) for platform-specific capabilities
- **Facade Pattern**: [`TestDataManager`](src/main/kotlin/com/ivan/automation/core/mobile/utils/TestDataManager.kt) for test data operations
- **Template Method**: [`MobileBaseScreen`](src/main/kotlin/com/ivan/automation/core/mobile/screen/MobileBaseScreen.kt) for page objects

## CI / Jenkins

- `Jenkinsfile` uses a shared library step `AndroidMobileTestRun` with Slack notifications.
- Agents should have prerequisites preinstalled (JDK 21, Android/Xcode toolchains, Node/Appium, Maven, Allure).
- Suggested Jenkins parameters/env:
  - `CUCUMBER_FILTER_TAGS` (e.g., `@smoke`)
  - `ENVIRONMENT` (`stg`, `int-2`)
  - `LATEST_BUILD` (label for app version)
- Artifacts: archive `target/allure-results` and publish Allure report via Jenkins Allure plugin.

## Notes

- Update `src/test/resources/properties/common.properties` to match your local device/emulator/simulator.
- For Android, ensure an emulator or real device is connected and Appium server is reachable.
- For iOS, ensure Xcode Command Line Tools and iOS runtimes are installed on the agent.
- The framework maintains backward compatibility - existing tests continue to work while new code can use the refactored architecture.