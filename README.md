# KuraKani

KuraKani is an Android messaging app for chatting with friends. The project includes account registration and sign-in, user discovery, text and media messages, push notifications, and Agora-powered calling.

## Features

- Sign up and sign in with Firebase Authentication.
- Find users and start conversations.
- Send text, image, and recorded audio messages.
- Receive Firebase Cloud Messaging notifications.
- Start audio/video calls using Agora RTC.
- Upload and download message media through Firebase Storage.

## Tech stack

- Kotlin and Android Views with View Binding
- Firebase Authentication, Realtime Database, Storage, and Cloud Messaging
- Agora RTC
- Koin dependency injection
- AndroidX Navigation, Lifecycle, WorkManager, and Coroutines

## Requirements

- Android Studio with Android SDK 33 or later configured.
- JDK compatible with the included Gradle wrapper.
- A Firebase project configured for this app.
- Agora credentials for call functionality.

## Local configuration

The app reads these values from `local.properties` and injects them into the Android build:

```properties
AGORA_APP_ID=your_agora_app_id
AGORA_TOKEN=your_agora_token
FCM_AUTHENTICATION_TOKEN=your_fcm_authentication_token
```

Use your own Firebase configuration in `app/google-services.json`. Keep service credentials and tokens private; do not publish real secrets in source control. Firebase and Agora services must be configured for their related features to work.

## Build and run

Open this directory in Android Studio and sync Gradle, or build a debug APK from the project root:

```bash
./gradlew :app:assembleDebug
```

Install the debug build on a device or emulator with:

```bash
./gradlew :app:installDebug
```

Camera and microphone permissions are needed for calls; network access is required for account, messaging, and media features.
