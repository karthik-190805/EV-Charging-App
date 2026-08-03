EV Charging App – Android Application

This folder contains the complete Android Studio application for the EV Charging App project.

The application is designed to help electric vehicle users locate charging stations, view station and connector details, monitor charging-related information, and manage user and charging-station operator workflows.

Project Structure

Application/
├── app/                    # Main Android application module
├── gradle/                 # Gradle wrapper and dependency configuration
├── .gitattributes          # Git attribute settings
├── .gitignore              # Files excluded from Git
├── build.gradle.kts        # Project-level Gradle configuration
├── gradle.properties       # Gradle project properties
├── gradlew                 # Gradle wrapper for Linux/macOS
├── gradlew.bat             # Gradle wrapper for Windows
└── settings.gradle.kts     # Project and module settings

Main Application Features

User and charging-station operator role selection

User registration and login

Password recovery and OTP verification

EV charging-station listing

Station details and charging-port information

Station availability information

Charging-related feature display

User and server-side activity flows

Local JSON-based vehicle and charging-station datasets

Technology Used

Android Studio

Java

XML

Gradle Kotlin DSL

Android SDK

JSON datasets

Opening the Project

Download or clone the repository.

Open Android Studio.

Select Open.

Choose the Application folder.

Wait for Gradle synchronization to complete.

Connect an Android device or start an emulator.

Click Run.

Open the complete Application folder, not only the app folder.

Important Source Locations

app/src/main/java/        # Java source code
app/src/main/res/         # Layouts, drawables, values and resources
app/src/main/assets/      # JSON and other application data
app/src/main/AndroidManifest.xml

Java Package Organization

com.example.evapp/
├── model/                 # Application data models
├── server/                # Charging-station operator screens
├── user/                  # EV-user screens
├── util/                  # Utility and helper classes
└── RoleSelectionActivity.java

Build Requirements

Android Studio

Java 11 or a compatible JDK

Android SDK matching the project configuration

Internet connection for initial Gradle dependency download

Running the Application

Using Android Studio:

Allow Gradle to finish syncing.

Select an available emulator or connected Android device.

Click the green Run button.

Using the terminal on Windows:

gradlew.bat assembleDebug

Using the terminal on Linux or macOS:

./gradlew assembleDebug

Notes

Do not delete the gradle folder, gradlew, gradlew.bat, or Gradle configuration files.

Keep application datasets inside app/src/main/assets/.

Do not commit generated folders such as .gradle/, .idea/, build/, or app/build/.

Review API keys, passwords, and sensitive configuration before publishing the repository.

Author

Karthik

GitHub: @karthik-190805

Project Purpose

This application was developed as an educational and project-development initiative to demonstrate an EV charging-station discovery and management solution.
