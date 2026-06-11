# Old Age Home Management System - Android App

This is a Native Android Application generated based on your requirements. 

## Project Setup

1. **Open in Android Studio**:
   - Launch Android Studio.
   - Select "Open".
   - Navigate to `c:\Users\dell\OneDrive\Desktop\Serenity\OldAgeHomeApp` and select it.

2. **Sync Gradle**:
   - Android Studio should automatically detect the `build.gradle` files.
   - Allow it to download dependencies.

3. **Firebase Configuration**:
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create a new project.
   - Add an Android App with package name `com.example.oldagehome`.
   - Download the `google-services.json` file.
   - Place `google-services.json` in the `app/` directory (next to `app/build.gradle`).
   - Enable **Authentication** (Email/Password).
   - Enable **Firestore Database** (Start in Test Mode).
   - Enable **Cloud Messaging** (FCM).

## Features Implemented

- **Authentication**: Login and Signup with Role selection.
- **Roles**: Admin, Doctor, Staff, Receptionist.
- **Dashboards**: Separate dashboards for Admin, Doctor, and Staff.
- **Resident Management**: View list and add new residents.
- **Doctor Management**: View list of doctors.
- **Appointments**: View and Book appointments.
- **Notifications**: System for viewing notifications.

## Next Steps

- Implement `Edit` and `Delete` functionality for Residents/Doctors.
- Implement the Medical Records module (similar to Appointments).
- Set up Firestore Security Rules in the Firebase Console.
- Test on a real device or emulator.
