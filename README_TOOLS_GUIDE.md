# Mobile Test Automation Environment Setup (macOS)

This document describes how to set up a complete mobile test automation environment on **macOS** using **Java, Maven, Appium, Android Studio, Xcode, and Appium Inspector**.

---

## 1. Java Setup

### Install Java via Homebrew (Recommended)

Homebrew is a package manager that makes installing Java easy and manageable.

### Install Homebrew

/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

Verify installation:

brew --version

### Install OpenJDK

brew install openjdk

Verify:

java -version

### Set Java Path

Apple Silicon:

export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"

Intel Macs:

export PATH="/usr/local/opt/openjdk/bin:$PATH"

---

## 2. Maven Setup

brew install maven

Verify:

mvn -version

---

## 3. Appium Setup

brew install node
npm install -g appium

Install drivers:

appium driver install uiautomator2
appium driver install xcuitest

---

## 4. Android Studio Setup

Install Android Studio from the official website and install SDK, Emulator, and Command-line tools.

Set environment variables:

export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

---

## 5. Xcode Setup

Install Xcode from App Store and download iOS simulator.

Verify:

xcrun simctl list | grep Booted

---

## 6. Appium Inspector Setup

Install Appium Inspector from GitHub releases and start Appium server:

appium --allow-insecure=adb_shell
