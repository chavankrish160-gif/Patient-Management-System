# Product Requirements Document: Patient Record Management System

**Status:** Ready for implementation and acceptance testing  
**Version:** 1.0  
**Audience:** College project stakeholders, implementers, and testers

## 1. Overview

The Patient Record Management System is a small web application for maintaining patient registration records. It enables a staff user to register a patient, find and review records, correct patient information, and remove a record after confirmation.

The product is limited to patient records. It is not a full hospital management system and does not provide clinical decision support.

### Project Layout and Run Instructions

- Java source files are stored in `src/patientrecord/` and use the `patientrecord` package.
- Frontend files are stored in `src/public/`: `index.html`, `css/style.css`, and `js/app.js`.
- Patient records are stored locally in `data/patients.json`.
- Compiled Java classes are written to `target/patientrecord/`.
- With JDK 17 or later, compile with `javac -d target src/patientrecord/*.java` and start the app with `java -cp target patientrecord.PatientRecordsApplication`. The app serves the frontend at `http://localhost:8081` by default.

## 2. Problem and Goals

Patient information should be easy to register, find, review, and maintain in one place. The system must prevent incomplete or malformed records, communicate outcomes clearly, and retain saved records between application restarts.

### Goals

- Provide reliable create, read, update, and delete workflows for patient records.
- Let a staff user quickly find a record by patient name, patient ID, or phone number.
- Show an accurate total patient count and useful empty, loading, success, and error states.
- Protect record integrity through validation and explicit confirmation before deletion.

### Out of Scope

- Appointments, billing, pharmacy, doctor management, prescriptions, laboratory management, insurance, or hospital operations.
- User accounts, roles, access-control administration, or multi-tenant workflows.
- Medical advice, diagnosis, treatment recommendations, or emergency response.
- Claims of healthcare regulatory certification or production suitability.

## 3. Users

**Primary user — Patient-record staff member:** Registers patient information and maintains the patient directory.

**Project operator — College project demonstrator:** Starts the application and demonstrates record workflows using fictional information.

## 4. Scope Baseline

- **Discovery method:** The supplied Patient Record Management System master prompt and the existing project feature description.
- **Items discovered:** 5 core capabilities: registration, directory/search, record details, editing, and deletion; plus shared dashboard, validation, persistence, and error-handling requirements.
- **Items in scope:** All discovered patient-record capabilities.
- **Scope boundary:** Patient registration and record maintenance only; unrelated hospital-management modules are excluded.

## 5. User Scenarios and Acceptance Tests

### US-01 — Register a patient (Priority: P1)

As a staff member, I want to add a patient's registration information so that the patient appears in the directory and can be maintained later.

**Why P1:** Record creation is the foundation of the system and enables all subsequent record workflows.

**Independent test:** Submit a valid fictional patient record and verify that a generated patient ID and saved record appear in the directory.

**Acceptance scenarios:**

1. **Given** the registration form is open, **when** the user submits all required fields with valid values, **then** the record is saved, a unique ID and creation/update timestamps are assigned, a success message is shown, and the directory and total count refresh without a manual page reload.
2. **Given** a required field is empty or the age, gender, phone, or registration date is invalid, **when** the user submits the form, **then** the record is not saved and an understandable field-specific validation message is displayed.
3. **Given** the local patient data file cannot be read or written, **when** the user submits a valid form, **then** the user sees a safe error message and no success state is shown.

### US-02 — Browse and search the patient directory (Priority: P1)

As a staff member, I want to see patient records and search them by name, ID, or phone so that I can locate the correct record.

**Why P1:** The directory is the main entry point for finding and maintaining records.

**Independent test:** Load a directory containing known records, verify the total count, and search using each supported search key.

**Acceptance scenarios:**

1. **Given** patient records exist, **when** the directory loads, **then** each record shows its ID, name, age, gender, phone, medical problem, registration date, and actions; the displayed total matches the number of saved records.
2. **Given** patient records exist, **when** the user enters a full or partial patient name, ID, or phone number, **then** matching records are shown and non-matching records are hidden.
3. **Given** no patient records exist, **when** the directory loads, **then** a helpful empty state is shown and the count is zero.
4. **Given** a search has no matching records, **when** the search is applied, **then** a distinct no-match message is shown without implying that the underlying directory is empty.

### US-03 — Review patient details (Priority: P1)

As a staff member, I want to open one patient's details so that I can review the full saved registration information.

**Why P1:** The directory cannot show every field; staff need a full view before making changes.

**Independent test:** Open a known patient's details and compare every displayed field with that saved record.

**Acceptance scenarios:**

1. **Given** a patient is listed, **when** the user selects View, **then** the details display the patient's ID, name, age, gender, phone, address, medical problem, registration date, creation timestamp, and last-updated timestamp.
2. **Given** a valid patient ID does not exist, **when** the detail view is requested, **then** the user receives a clear not-found message.

### US-04 — Edit a patient record (Priority: P1)

As a staff member, I want to update an existing patient's information so that corrections do not create duplicate records.

**Why P1:** Existing patient information must remain maintainable and accurate.

**Independent test:** Edit a known record, reload it, and verify updated values, an unchanged patient ID/creation time, and a changed last-updated time.

**Acceptance scenarios:**

1. **Given** a patient is listed, **when** the user selects Edit, **then** the form is populated with that patient's saved information.
2. **Given** the user submits valid changes, **when** the update completes, **then** the same record is updated, its ID and creation timestamp remain unchanged, its last-updated timestamp advances, and the list/count refresh.
3. **Given** updated values are invalid, **when** the user submits the form, **then** changes are rejected and field-specific validation messages are shown.
4. **Given** the patient was removed by another operation, **when** an update is requested, **then** the user receives a not-found message and no new patient is created.

### US-05 — Delete a patient record (Priority: P2)

As a staff member, I want to delete an obsolete patient record after confirming my choice so that the directory remains accurate.

**Why P2:** Deletion is important for record maintenance but must not undermine the primary registration and lookup workflows.

**Independent test:** Confirm deletion of a known fictional test patient, verify it is absent from the list and local data file, and verify the count decreases.

**Acceptance scenarios:**

1. **Given** a patient is listed, **when** the user selects Delete, **then** a confirmation dialog identifies the action before deletion is attempted.
2. **Given** the user cancels the confirmation, **when** the dialog closes, **then** the record and count remain unchanged.
3. **Given** the user confirms deletion, **when** deletion succeeds, **then** the record is removed, the directory and count refresh, and a success message is shown.
4. **Given** a valid patient ID does not exist, **when** deletion is requested, **then** a not-found response is handled clearly and the application remains usable.

## 6. Functional Requirements

- **REQ-001:** The system MUST allow a staff user to create a patient record with a required full name, positive age, gender (Male, Female, or Other), valid mobile number, and registration date.
- **REQ-002:** The system MUST allow address and medical problem to be recorded as optional patient information.
- **REQ-003:** The system MUST assign a unique patient ID and creation and last-updated timestamps to each saved record.
- **REQ-004:** The system MUST persist patient records so that they remain available after an application restart.
- **REQ-005:** The system MUST display a directory containing each patient's ID, name, age, gender, phone, medical problem, registration date, and View, Edit, and Delete actions.
- **REQ-006:** The system MUST calculate the displayed total patient count from saved records and update it after successful creation or deletion.
- **REQ-007:** The system MUST allow the directory to be searched by full or partial patient name, ID, or phone number.
- **REQ-008:** The system MUST display all recorded fields for one selected patient, including ID and timestamps.
- **REQ-009:** The system MUST load a selected patient's existing information into the edit form and save changes to that existing record rather than creating a duplicate.
- **REQ-010:** The system MUST retain a patient's ID and creation timestamp when editing and update the last-updated timestamp after a successful change.
- **REQ-011:** The system MUST request confirmation before deletion and leave the record unchanged when deletion is cancelled.
- **REQ-012:** The system MUST validate patient data on submission and reject missing, malformed, or out-of-range values with understandable errors.
- **REQ-013:** The system MUST distinguish empty-directory, no-search-match, loading, success, service-error, invalid-ID, and not-found states in user-facing feedback.
- **REQ-014:** The system MUST use appropriate success, validation, not-found, conflict, and service-failure outcomes for record operations.
- **REQ-015:** The system MUST NOT return stack traces or sensitive local configuration in error responses.
- **REQ-016:** The system MUST present a responsive, keyboard-accessible interface with labels for form fields and a clear confirmation interaction for deletion.
- **REQ-017:** The system MUST limit its scope to patient-record management and MUST NOT add appointments, billing, pharmacy, doctor management, prescriptions, laboratory management, or unrelated modules.
- **REQ-018:** The system MUST identify itself and its documentation as a demonstration project and MUST NOT claim healthcare regulatory certification.

## 7. Key Entities

- **Patient record:** One registered person; includes unique ID, patient name, age, gender, phone, optional address, optional medical problem, registration date, creation timestamp, and last-updated timestamp.
- **Validation error:** A safe, understandable explanation of an invalid submitted field or request.
- **Patient directory:** The searchable collection of saved patient records and its total-record count.

## 8. Non-Functional Requirements

- **Privacy:** Demonstrations and acceptance tests use fictional patient information. Patient records are stored in the local JSON data file and are not included in frontend assets.
- **Reliability:** A failed read or write must not be presented as successful; a failed operation must provide a user-visible error and leave the application usable.
- **Accessibility:** All form inputs have programmatic labels, actions are keyboard-operable, and status/errors are announced to assistive technology.
- **Responsive design:** Directory and forms remain usable on desktop, tablet, and mobile-sized screens.
- **Data integrity:** Updates target one existing unique ID; deletes require explicit confirmation; invalid IDs and missing records are handled without crashing.

## 9. Test Plan

Use fictional test records only. Compile and start the application using the commands in Section 1, then run the acceptance scenarios manually. The application persists records in `data/patients.json`; no database service or automated test framework is configured. Each test has an explicit expected result.

| Test ID | Requirement(s) | Test case and steps | Expected result |
| --- | --- | --- | --- |
| T-001 | REQ-001–004, REQ-006 | Start with an empty `data/patients.json` (or no data file); create a valid fictional patient. | Create succeeds; generated ID and timestamps are present; directory shows the patient; count is one. |
| T-002 | REQ-001, REQ-012 | Submit with each required field omitted in turn. | Each request is rejected; field-specific error is returned; no record is created. |
| T-003 | REQ-001, REQ-012 | Submit age `0`, a negative age, an age over 130, non-numeric age, an unsupported gender, and malformed/too-short phone values. | Invalid values are rejected with understandable errors; no invalid record is saved. |
| T-004 | REQ-002, REQ-003 | Create a patient with blank optional address and medical problem. | Record is accepted; optional values are empty; ID and timestamps are generated. |
| T-005 | REQ-004 | Create a patient, restart the application, and retrieve the patient. | The same patient data and ID remain available after restart. |
| T-006 | REQ-005–007, REQ-013 | Seed multiple records; inspect directory, count, and search by partial name, exact/partial ID, and phone. | Required columns/actions are present; each query returns the matching patient(s); count reflects saved records. |
| T-007 | REQ-013 | Load with no patient data file, then search for a non-matching value after creating a record. | Empty directory and no-match state are distinct and accurate. |
| T-008 | REQ-008, REQ-013 | Open details for an existing patient and for a syntactically valid but missing ID. | Existing record shows every field and timestamp; missing record yields a clear not-found result. |
| T-009 | REQ-009, REQ-010 | Edit name, age, and phone; save; read again; compare ID and timestamps. | Updated values persist on the same ID; creation time is unchanged; updated time advances; no duplicate is created. |
| T-010 | REQ-009, REQ-012 | Edit a patient with an invalid phone or missing required field and submit. | Update is rejected with field-specific errors; original record remains unchanged. |
| T-011 | REQ-011, REQ-013 | Select Delete and cancel the confirmation. | No delete request is committed; record and count remain unchanged. |
| T-012 | REQ-006, REQ-011 | Select Delete and confirm for a fictional test record. | Record is absent from the local data file and directory; count decreases; success feedback appears. |
| T-013 | REQ-013, REQ-014 | Request an invalid ID and a valid ID that does not exist for read, update, and delete. | Invalid ID is reported as a client error; missing record is reported as not found; app remains available. |
| T-014 | REQ-013–015 | Make the local patient data file unreadable or invalid, then attempt a list and a write. | The application reports an error without claiming success or returning a stack trace to the browser. |
| T-015 | REQ-016 | Navigate forms and dialogs using keyboard only; inspect labels, focus indicators, and mobile viewport layout. | Inputs and actions are operable and labeled; focus is visible; layout remains usable at narrow width. |
| T-016 | REQ-017–018 | Review product screens and user-facing documentation. | No out-of-scope hospital modules or compliance claims are present; fictional-data demonstration warning is present. |

### Test Automation

No automated test framework or test suite is currently configured. The cases above are manual acceptance scenarios and run against the local JSON file.

## 10. Success Criteria

- **SC-001:** A staff user can register a valid patient and see that record and the incremented count in the directory without manually reloading the browser.
- **SC-002:** A staff user can find a seeded patient using name, ID, and phone searches; each search returns the correct matching records.
- **SC-003:** Every valid test patient can be viewed, edited, re-read with changes persisted, and deleted after confirmation.
- **SC-004:** Invalid required fields and invalid age, gender, or phone inputs are rejected in all tested cases without creating or corrupting a record.
- **SC-005:** A saved test record remains readable after an application restart.
- **SC-006:** All 16 manual acceptance cases pass before project acceptance using the local JSON data file.
- **SC-007:** The test data set contains only fictional patient information, and no checked error response exposes stack traces or sensitive local configuration.

## 11. Assumptions and Constraints

- The primary user is a trusted staff member of the demonstration; authentication and user roles are not in scope.
- Gender values are limited to Male, Female, or Other, as specified in the product brief.
- Age is a whole number from 1 through 130 inclusive.
- Phone validation accepts a 7–20 character phone value containing digits and common formatting characters, with an optional leading plus sign.
- Registration date is required; address and medical problem are optional.
- The application uses a local JSON file for persistence; no database service is required.
- This project is educational and is not certified for handling real patient information.

