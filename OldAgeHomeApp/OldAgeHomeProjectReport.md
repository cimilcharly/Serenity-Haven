# Old Age Home Management System (Serenity Haven)

---

## 1. Abstract
The **Old Age Home Management System** is an Android-based application designed to streamline the operations of an old age home, "Serenity Haven". This application bridges the communication gap between administrators, staff, doctors, and residents. It provides a centralized platform for managing resident profiles, medical records, medication schedules, doctor appointments, and daily activities. The system enhances the efficiency of caregiving by ensuring timely medication reminders, easy access to medical assistance, and a structured approval process for new resident registrations. Additionally, it incorporates features like a health chatbot and direct communication channels to improve the quality of life for the elderly residents.

## 2. List of Figures
*(Please insert the list of figures used in this report here. Examples include the System Architecture Diagram, Use Case Diagram, and Screenshots.)*
- Figure 1: System Architecture Diagram
- Figure 2: Use Case Diagram
- Figure 3: User Login Screen
- Figure 4: Resident Dashboard
- Figure 5: Admin Dashboard
- Figure 6: Medicine Reminder Notification

## 3. Introduction
With the increasing elderly population, the need for well-organized and efficient care facilities has grown significantly. Traditional methods of managing old age homes often rely on manual record-keeping, which can be prone to errors, especially regarding medical schedules and health tracking. This project introduces a digital solution to modernize the management of old age homes. By leveraging mobile technology, the system ensures that critical information is accessible to authorized personnel and that residents receive the care and attention they need promptly.

## 4. Background about the Topic
Current management systems in many care facilities are either non-existent or fragmented. Paper-based records are difficult to maintain and search. Existing digital solutions are often too complex or expensive for smaller facilities. This project focuses on creating a user-friendly, cost-effective mobile application tailored to the specific needs of an old age home environment, focusing on ease of use for elderly residents and streamlined workflows for staff.

## 5. Problem Statement
The primary challenges addressed by this project include:
- **Inefficient Record Keeping**: Difficulty in maintaining and retrieving physical files for each resident.
- **Medication Errors**: Risk of missed or incorrect medication doses due to lack of automated reminders.
- **Communication Gaps**: Delays in communication between residents, staff, and doctors.
- **Appointment Management**: complexities in scheduling and tracking doctor visits.
- **Safety Concerns**: Need for a reliable system to track resident activities and health status.

## 6. Objectives
The main objectives of the project are:
- To develop an Android application for managing resident data, staff shifts, and doctor schedules.
- To implement a secure login and registration system with role-based access control (Admin, Staff, Doctor, Resident).
- To provide a medicine reminder system to ensure timely medication adherence.
- To facilitate easy appointment booking with doctors.
- To enable real-time communication and notifications for critical updates.
- To include an AI-powered chatbot for basic health queries and assistance.

## 7. Scope of the Project
The scope of the project includes:
- **User Modules**: Admin, Staff, Doctor, and Resident.
- **Data Management**: Secure storage of user profiles, medical history, and daily logs.
- **Core Functionalities**:
    - Resident registration and approval workflow.
    - Medicine inventory and dosage tracking.
    - Doctor appointment scheduling.
    - Emergency notifications and alerts.
    - Chatbot integration for resident assistance.
- **Platform**: Android Mobile Application.

## 8. Community Profile
*(Describe the target community for this project here. This section should detail the demographics of the old age home residents, staff, and the specific facility where the project is implemented or modeled after.)*

**Target Audience:**
- **Residents**: Elderly individuals aged 60+, potentially with various health conditions requiring regular monitoring and medication.
- **Administrators**: Facility managers responsible for overall operations.
- **Medical Staff**: Doctors and nurses providing healthcare services.
- **Caregivers**: Staff members assisting residents with daily activities.

*(Insert specifics about the community you interacted with, e.g., "The project focuses on 'XYZ Old Age Home' located in [City], housing approximately 50 residents...")*

## 9. Need Analysis
Through interaction with old age home administrators and residents, several key needs were identified:
- **Automated Alerts**: Staff requested automated alerts for medication timings due to high workload.
- **Simplified Interface**: Residents needed a very simple, large-text interface for easy navigation.
- **Centralized Data**: Doctors emphasized the need for quick access to a resident's medical history before consultations.
- **Remote Monitoring**: Family members (future scope) and admins need to monitor the well-being of residents remotely.

## 10. Methodology
The project followed the **Agile Development Methodology**:
1.  **Requirement Gathering**: Interviews with potential users to understand needs.
2.  **Design**: creating wireframes and UI designs focused on accessibility.
3.  **Development**: Iterative coding of modules (Authentication, Dashboard, Medical Records) using Android Studio and Java.
4.  **Testing**: Unit testing and user acceptance testing with a small group of users.
5.  **Deployment**: Installing the app on test devices for feedback.

## 11. Architecture / Flow Diagram
**(Insert Image: System Architecture Diagram)**
*Description:* The system follows a client-server architecture.
- **Client**: Android Application (Java/XML).
- **Backend/Database**: Firebase Realtime Database / Firestore for storing user data, schedules, and chat logs.
- **Authentication**: Firebase Authentication for secure login.

**(Insert Image: User Flow Diagram)**
*Description:* The flow starts with a Login/Signup screen. Based on the role (Admin/Resident/Doctor), the user is navigated to their respective dashboard. Residents can view reminders and book appointments; Admins can approve requests and manage staff.

## 12. Components
### Software Components
1.  **Frontend**: XML for UI layouts (Activities and Fragments).
2.  **Logic**: Java for business logic and event handling.
3.  **Database**: Firebase Realtime Database for data persistence.
4.  **Authentication Service**: Firebase Auth.
5.  **APIs**: Integration with external APIs (if any, e.g., for chatbot).

### Modules
-   **Authentication Module**: Login, Signup, Forgot Password.
-   **Dashboard Module**: Role-specific home screens.
-   **Resident Management**: Add, Edit, View Resident profiles.
-   **Medical Module**: Medicine list, dosage, reminders.
-   **Appointment Module**: Book and view doctor appointments.
-   **Notification Module**: Push notifications for reminders and approvals.
-   **Chatbot Module**: AI assistant for queries.

## 13. Software and Hardware Requirements
### Software Requirements
-   **Operating System**: Windows 10/11 or macOS.
-   **IDE**: Android Studio (Latest Version).
-   **Language**: Java (JDK 8+).
-   **Database**: Firebase.
-   **Design Tools**: Figma / Adobe XD (for UI prototyping).

### Hardware Requirements
-   **Development Machine**: Functioning Laptop/PC with minimum 8GB RAM.
-   **Testing Device**: Android Smartphone (Android 6.0 Marshmallow or higher) or Android Emulator.

## 14. Description of the Project
The "Serenity Haven" app is a comprehensive solution designed to improve the quality of life in old age homes.

**Key Features:**
1.  **Role-Based Access**: Secure login for Admins, Staff, Doctors, and Residents, ensuring data privacy and appropriate feature access.
2.  **Resident Registration & Approval**: New residents can sign up effortlessly. To maintain security, all registrations go through an 'Admin Approval' process (`PendingApprovalsActivity`) before activation.
3.  **Medicine Management**: The `MedicineActivity` allows staff/doctors to prescribe medicines. Residents receive timely notifications (`NotificationActivity`) to take their medication.
4.  **Doctor Appointments**: Residents can browse a list of available doctors (`DoctorListActivity`) and book appointments. Doctors have a dedicated dashboard (`DoctorDashboardActivity`) to view their schedule.
5.  **Health Chatbot**: An integrated AI chatbot (`ChatbotActivity`) assists residents with common health questions and provides companionship.
6.  **Staff & Admin Dashboards**: Dedicated interfaces (`MainStaffDashboardActivity`, `AdminDashboardActivity`) for managing operations, viewing resident lists (`ResidentListActivity`), and handling approvals.

## 15. Results and Screenshots
*(This section requires actual screenshots of the application running. Please take screenshots of the following screens and insert them here.)*

-   **Screenshot 1**: Login Page
-   **Screenshot 2**: Signup Page
-   **Screenshot 3**: Admin Dashboard showing Pending Approvals
-   **Screenshot 4**: Resident Dashboard with Medicine Reminders
-   **Screenshot 5**: Doctor Appointment Booking
-   **Screenshot 6**: Chatbot Interface

*Description:* The application successfully allows users to register, log in, and access their respective dashboards. The medication reminder system triggers notifications accurately, and the admin approval workflow functions as intended.

## 16. Conclusion
The "Serenity Haven" / Old Age Home Management System successfully addresses the core challenges of managing an elderly care facility. By digitizing record-keeping, medication schedules, and communication, the system reduces manual errors and improves the overall efficiency of the home. The user-centric design ensures that even elderly residents with limited technical knowledge can navigate the app comfortably.

## 17. Learning Outcome
Developing this project provided valuable insights into:
-   **Mobile App Development Life Cycle**: From ideation to deployment.
-   **User Experience (UX) Design**: Design principles for elderly users (accessibility, readability).
-   **Database Management**: integrating real-time databases (Firebase) for live updates.
-   **Java Programming**: Advanced concepts in Android development (Adapters, Activities, Fragments, Services).
-   **Project Management**: Balancing features, time constraints, and user requirements.

## 18. Limitations and/or Challenges
-   **Internet Dependency**: The app requires an active internet connection for real-time data synchronization.
-   **Device Compatibility**: Older Android devices may not support all animations or features smoothly.
-   **Digital Literacy**: Some residents may still struggle with using smartphones despite the simplified interface.

## 19. Future Scope
-   **Offline Mode**: Implementing local caching to allow basic functionality without internet.
-   **IoT Integration**: Integrating with wearable health bands to automatically track vitals (heart rate, BP).
-   **Voice Commands**: Adding voice support for navigation and commands to assist visually impaired residents.
-   **Family Portal**: A separate module for family members to track the health and status of their loved ones.

## 20. References
1.  Android Developer Documentation: [https://developer.android.com/](https://developer.android.com/)
2.  Firebase Documentation: [https://firebase.google.com/docs](https://firebase.google.com/docs)
3.  Material Design Guidelines: [https://material.io/design](https://material.io/design)

---

## 21. Appendices

### A. Community Profile
*(Attach detailed documents, surveys, or profiles of the specific community you worked with. e.g., "Demographic Report of XYZ Old Age Home".)*

### B. Confirmation Letter
*(Attach the official confirmation letter from the community/organization/institution granting permission for the project.)*
[Insert Letter Here]

### C. Relevant Documents of Meeting/Interacting with Community
*(Attach proof of interaction. This creates credibility.)*
-   **Photographs**: [Insert photos of team meetings with residents/staff]
-   **Mail Communications**: [Insert screenshots of email exchanges]
-   **Letters of Appreciation/Feedback**: [Insert copies of any feedback letters received]

## 22. Project File Structure
The following is a verified list of all source code components (Java Classes and XML Layouts) developed for this project.

### 22.1 Java Source Files (`app/src/main/java/com/example/oldagehome`)

**Activities (UI Controllers)**
- `AddResidentActivity.java`: Handles adding/editing resident details (Staff/Admin).
- `AdminDashboardActivity.java`: Main dashboard for Administrators.
- `AppointmentActivity.java`: Handles booking appointments.
- `ChatbotActivity.java`: AI Chatbot interface for residents.
- `DoctorDashboardActivity.java`: Dashboard for Doctors to view schedules.
- `DoctorListActivity.java`: Lists available doctors for booking.
- `LoginActivity.java`: Entry point for user authentication.
- `MainStaffDashboardActivity.java`: Primary dashboard for Main Staff.
- `MedicineActivity.java`: Manages medicine prescriptions and reminders.
- `NotificationActivity.java`: Displays system notifications.
- `PendingApprovalsActivity.java`: Admin screen to approve new signups.
- `ResidentDashboardActivity.java`: Main dashboard for Residents.
- `ResidentListActivity.java`: Displays list of all residents.
- `ResidentRegistrationActivity.java`: Signup screen for new residents.
- `SignupActivity.java`: General user registration.
- `SplashActivity.java`: Initial launch screen.
- `StaffDashboardActivity.java`: Dashboard for general staff.

**Adapters (Data Binding)**
- `AppointmentAdapter.java`: Binds appointment data to lists.
- `ChatAdapter.java`: Handles chat message display in Chatbot.
- `DoctorAdapter.java`: Usage for doctor lists.
- `MedicineAdapter.java`: Binds medicine schedules to views.
- `NotificationAdapter.java`: Displays notification items.
- `PendingResidentAdapter.java`: Handles the list of pending approvals.
- `ResidentAdapter.java`: Binds resident profile data to lists.

**Models (Data Structures)**
- `AppointmentModel.java`: Data class for appointments.
- `ChatMessage.java`: Data class for chat messages.
- `DoctorModel.java`: Data class for doctor profiles.
- `MedicalRecordModel.java`: Data class for medical history.
- `MedicineModel.java`: Data class for medicine details.
- `NotificationModel.java`: Data class for notifications.
- `ResidentModel.java`: Extended user model for residents.
- `RoomModel.java`: Data class for room management.
- `UserModel.java`: Base user class (common fields).

**Utilities & Helpers**
- `AlarmReceiver.java`: Handles scheduled alarms for reminders.
- `EmailSender.java`: Helper for sending emails (if implemented).
- `MemoryManager.java`: Manages local storage/context for AI.
- `MyFirebaseMessagingService.java`: Handles Firebase Cloud Messaging.
- `RecommendationAlgorithm.java`: Algorithm for health suggestions.
- `RoleManager.java`: Constants and helpers for user roles.

**Network**
- `GrokApiService.java`: Interface for AI API.
- `RetrofitClient.java`: HTTP client setup.

### 22.2 XML Layout Files (`app/src/main/res/layout`)

**Activity Layouts**
- `activity_add_resident.xml`: Form for adding residents.
- `activity_admin_dashboard.xml`: Admin home screen layout.
- `activity_appointment.xml`: Appointment booking screen.
- `activity_chatbot.xml`: Chat interface layout.
- `activity_doctor_dashboard.xml`: Doctor dashboard layout.
- `activity_doctor_list.xml`: List of doctors.
- `activity_login.xml`: Login screen UI.
- `activity_main_staff_dashboard.xml`: Main staff home screen.
- `activity_medicine.xml`: Medicine management screen.
- `activity_notification.xml`: Notification history screen.
- `activity_pending_approvals.xml`: Layout for approval list.
- `activity_resident_dashboard.xml`: Resident home screen.
- `activity_resident_list.xml`: Layout for resident directory.
- `activity_resident_registration.xml`: Registration form.
- `activity_signup.xml`: Signup form.
- `activity_splash.xml`: Splash screen layout.
- `activity_staff_dashboard.xml`: Staff dashboard layout.

**Dialogs & Items**
- `dialog_add_medicine.xml`: Popup for adding new medicine.
- `item_appointment.xml`: Single row for appointment list.
- `item_chat_message.xml`: Bubble layout for chat messages.
- `item_doctor.xml`: Single row for doctor list.
- `item_medicine.xml`: Single row for medicine list.
- `item_medicine_time.xml`: Layout for specific medicine timings.
- `item_notification.xml`: Single row for notifications.
- `item_pending_resident.xml`: Single row for approval list.
- `item_resident.xml`: Single row for resident list.
