# Base for Cursor cloud/background agents. Do not COPY code; the agent clones the repo.
# Runtime secrets (e.g. GEMINI_API_KEY) are injected by Cursor from Settings → Cloud Agents → Secrets.
FROM ubuntu:24.04

# Avoid interactive prompts
ENV DEBIAN_FRONTEND=noninteractive

# Dev tools: Java (for Android), Node.js, Git
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jdk-headless \
    ca-certificates \
    curl \
    git \
    unzip \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Android SDK: install command-line tools + platform (compileSdk 36) and build-tools
ARG ANDROID_HOME=/home/ubuntu/android-sdk
ENV ANDROID_HOME=${ANDROID_HOME}
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

RUN useradd -m -s /bin/bash ubuntu \
    && mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && curl -fsSL -o /tmp/cmdline-tools.zip \
        "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" \
    && unzip -q /tmp/cmdline-tools.zip -d /tmp \
    && mv /tmp/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip \
    && yes | sdkmanager --sdk_root=${ANDROID_HOME} --licenses 2>/dev/null || true \
    && sdkmanager --sdk_root=${ANDROID_HOME} \
        "platform-tools" \
        "platforms;android-36" \
        "build-tools;36.0.0" \
    && chown -R ubuntu:ubuntu ${ANDROID_HOME}

# Playwright: install system deps (as root), then browsers (as ubuntu)
RUN npm install -g playwright \
    && npx playwright install-deps

USER ubuntu
WORKDIR /home/ubuntu

RUN npx playwright install
