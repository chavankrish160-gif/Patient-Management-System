"use strict";

// Get references to HTML elements used by the application.
const rows = document.querySelector("#patient-rows");
const tableState = document.querySelector("#table-state");
const countElement = document.querySelector("#patient-count");
const searchInput = document.querySelector("#patient-search");
const notice = document.querySelector("#notice");
const patientDialog = document.querySelector("#patient-dialog");
const detailsDialog = document.querySelector("#details-dialog");
const patientForm = document.querySelector("#patient-form");
const formTitle = document.querySelector("#form-title");
const saveButton = document.querySelector("#save-patient");
const formError = document.querySelector("#form-error");

// Store all patient records received from the backend.
let patients = [];

// Store the ID of the patient currently being edited.
// null means that the form is being used to add a new patient.
let editingId = null;

// Store the notification timeout identifier.
let noticeTimer;


// Send HTTP requests to the Java backend.
async function request(path, options = {}) {
    const response = await fetch(path, {
        ...options,

        // Set the content type for JSON requests.
        headers: {
            "Content-Type": "application/json",
            ...options.headers
        }
    });

    // Throw an error if the server returns an unsuccessful status.
    if (!response.ok) {
        const message = await response.text();
        throw new Error(
            message || `Request failed (${response.status}).`
        );
    }

    // A 204 response has no body; otherwise, parse the JSON response.
    return response.status === 204 ? null : response.json();
}


// Fetch all patient records and refresh the table.
async function refreshPatients() {
    // Display the loading state.
    tableState.hidden = false;
    tableState.replaceChildren(
        createSpinner(),
        textNode("Loading patient records…")
    );

    try {
        // Request all patients from the backend API.
        patients = await request("/api/patients");

        // Display the total number of patients.
        countElement.textContent = String(patients.length);

        // Render the patient table.
        renderPatients();

    } catch {
        // Show an error message if loading fails.
        countElement.textContent = "—";
        tableState.replaceChildren();

        const title = document.createElement("p");
        title.className = "empty-title";
        title.textContent = "Patient records could not be loaded";

        const message = document.createElement("p");
        message.className = "empty-copy";
        message.textContent =
            "Check that the Java backend is running, then reload.";

        tableState.append(title, message);
        tableState.hidden = false;
    }
}


// Display patient records and apply the search filter.
function renderPatients() {
    // Read the search text and convert it to lowercase.
    const term = searchInput.value.trim().toLocaleLowerCase();

    // Filter patients by name, ID, or phone number.
    const filtered = patients.filter((patient) =>
        [
            patient.patientName,
            patient.id,
            patient.phone
        ].some((value) =>
            String(value ?? "").toLocaleLowerCase().includes(term)
        )
    );

    // Create and display a table row for each matching patient.
    rows.replaceChildren(...filtered.map(createPatientRow));

    // Hide the empty-state message when records are available.
    if (filtered.length > 0) {
        tableState.hidden = true;
        return;
    }

    // Prepare a message for an empty result.
    tableState.replaceChildren();

    const title = document.createElement("p");
    title.className = "empty-title";

    const message = document.createElement("p");
    message.className = "empty-copy";

    // Display a different message depending on the situation.
    if (patients.length === 0) {
        title.textContent = "No patient records yet";
        message.textContent =
            "Add a patient to begin building your directory.";
    } else {
        title.textContent = "No matching patients";
        message.textContent =
            "Try a different name, patient ID, or phone number.";
    }

    tableState.append(title, message);
    tableState.hidden = false;
}


// Create a table row containing one patient's information.
function createPatientRow(patient) {
    const row = document.createElement("tr");

    // Add the patient's details to the row.
    row.append(
        cell(patient.id, "patient-id"),
        cell(patient.patientName, "patient-name"),
        cell(patient.age),
        cell(patient.gender),
        cell(patient.phone),
        cell(patient.medicalProblem || "—", "problem-cell"),
        cell(formatDate(patient.admissionDate))
    );

    // Create a cell containing the action buttons.
    const actionCell = document.createElement("td");
    const actions = document.createElement("div");
    actions.className = "row-actions";

    // Add buttons to view, edit, or delete the patient.
    actions.append(
        actionButton("View", "view", patient.id),
        actionButton("Edit", "edit", patient.id),
        actionButton("Delete", "delete", patient.id)
    );

    actionCell.append(actions);
    row.append(actionCell);

    return row;
}


// Create a table cell and safely display its value.
function cell(value, className = "") {
    const element = document.createElement("td");

    // Use a dash when the value is missing.
    element.textContent = value ?? "—";

    // Apply a CSS class when one is provided.
    if (className) {
        element.className = className;
    }

    return element;
}


// Create a View, Edit, or Delete button.
function actionButton(label, action, id) {
    const button = document.createElement("button");

    button.type = "button";

    // Apply a special CSS class to the Delete button.
    button.className =
        `row-action${action === "delete" ? " delete" : ""}`;

    // Store the action and patient ID in data attributes.
    button.dataset.action = action;
    button.dataset.id = id;

    // Set the visible button text.
    button.textContent = label;

    // Provide an accessible description for screen readers.
    button.setAttribute(
        "aria-label",
        `${label} patient ${id}`
    );

    return button;
}


// Create a span element containing the supplied text.
function textNode(text) {
    const element = document.createElement("span");
    element.textContent = text;
    return element;
}


// Create a loading spinner element.
function createSpinner() {
    const spinner = document.createElement("span");

    spinner.className = "spinner";

    // Hide the decorative spinner from screen readers.
    spinner.setAttribute("aria-hidden", "true");

    return spinner;
}


// Format an admission date for display in the table.
function formatDate(value) {
    // Return a dash if no date is available.
    if (!value) {
        return "—";
    }

    // Parse the date without changing its local calendar day.
    const parsed = new Date(`${value}T00:00:00`);

    // Return the original value if the date is invalid.
    return Number.isNaN(parsed.getTime())
        ? value
        : new Intl.DateTimeFormat(undefined, {
            year: "numeric",
            month: "short",
            day: "numeric"
        }).format(parsed);
}


// Display a notification message to the user.
function showNotice(message, isError = false) {
    // Cancel the previous notification timer.
    window.clearTimeout(noticeTimer);

    // Update the notification text and error styling.
    notice.textContent = message;
    notice.classList.toggle("error", isError);
    notice.hidden = false;

    // Hide the notification after six seconds.
    noticeTimer = window.setTimeout(() => {
        notice.hidden = true;
    }, 6000);
}


// Open the form for adding a new patient.
function openCreateForm() {
    // Clear the editing ID because this is a new record.
    editingId = null;

    // Reset the form and remove previous validation messages.
    patientForm.reset();
    clearFieldErrors();

    // Set the form title and save button text.
    formTitle.textContent = "Add patient";
    saveButton.textContent = "Save patient";

    // Set today's date as the default admission date.
    patientForm.elements.admissionDate.value = localDateString();

    // Open the dialog and focus the patient name field.
    patientDialog.showModal();
    patientForm.elements.patientName.focus();
}


// Return today's date in YYYY-MM-DD format using local time.
function localDateString() {
    const today = new Date();

    // Adjust the time by the local timezone offset.
    today.setMinutes(
        today.getMinutes() - today.getTimezoneOffset()
    );

    return today.toISOString().slice(0, 10);
}


// Clear validation errors from the form.
function clearFieldErrors() {
    // Remove all field-specific error messages.
    patientForm.querySelectorAll(".field-error").forEach((element) => {
        element.textContent = "";
    });

    // Remove invalid-state attributes from inputs.
    patientForm.querySelectorAll("[aria-invalid='true']").forEach(
        (element) => {
            element.removeAttribute("aria-invalid");
        }
    );

    // Clear the general form error message.
    formError.hidden = true;
    formError.textContent = "";
}


// Display validation errors returned by the backend.
function displayFieldErrors(fieldErrors) {
    let firstInvalidField = null;

    // Process each field name and its corresponding error message.
    for (const [name, message] of Object.entries(fieldErrors)) {
        const input = patientForm.elements.namedItem(name);

        // Find the element assigned to display this field's error.
        const error = patientForm.querySelector(
            `[data-error-for="${CSS.escape(name)}"]`
        );

        if (input && error) {
            // Mark the input as invalid and display its message.
            input.setAttribute("aria-invalid", "true");
            error.textContent = message;

            // Remember the first invalid input.
            firstInvalidField ??= input;
        }
    }

    // Move focus to the first invalid field.
    if (firstInvalidField) {
        firstInvalidField.focus();
    }
}


// Fetch a patient and open the form for editing.
async function openEditForm(id) {
    clearFieldErrors();

    let patient;

    try {
        // Request the selected patient's details from the backend.
        patient = await request(
            `/api/patients/${encodeURIComponent(id)}`
        );

    } catch (error) {
        showNotice(error.message, true);
        return;
    }

    // Store the ID so the form submits an update.
    editingId = patient.id;

    // Fill the form with the existing patient information.
    patientForm.elements.patientName.value = patient.patientName;
    patientForm.elements.age.value = patient.age;
    patientForm.elements.gender.value = patient.gender;
    patientForm.elements.phone.value = patient.phone;
    patientForm.elements.address.value = patient.address ?? "";
    patientForm.elements.medicalProblem.value =
        patient.medicalProblem ?? "";
    patientForm.elements.admissionDate.value =
        patient.admissionDate ?? "";

    // Change the form labels for editing.
    formTitle.textContent = "Edit patient";
    saveButton.textContent = "Save changes";

    // Open the form and focus the patient name field.
    patientDialog.showModal();
    patientForm.elements.patientName.focus();
}


// Fetch and display the complete details of a patient.
async function showDetails(id) {
    let patient;

    try {
        // Get the selected patient's information.
        patient = await request(
            `/api/patients/${encodeURIComponent(id)}`
        );

    } catch (error) {
        showNotice(error.message, true);
        return;
    }

    // Prepare the labels and values shown in the details dialog.
    const entries = [
        ["Patient ID", patient.id],
        ["Patient name", patient.patientName],
        ["Age", patient.age],
        ["Gender", patient.gender],
        ["Phone", patient.phone],
        ["Residential address", patient.address],
        ["Medical problem", patient.medicalProblem],
        ["Registration date", formatDate(patient.admissionDate)],
        ["Created", formatTimestamp(patient.createdAt)],
        ["Last updated", formatTimestamp(patient.updatedAt)]
    ];

    // Find the container where the patient details will be displayed.
    const list = document.querySelector("#patient-details");

    // Create and display one detail item for every field.
    list.replaceChildren(
        ...entries.map(([label, value]) => detailItem(label, value))
    );

    // Open the patient details dialog.
    detailsDialog.showModal();
}


// Create one label-value item for the patient details dialog.
function detailItem(label, value) {
    const wrapper = document.createElement("div");

    // Make long fields span the full width of the dialog.
    wrapper.className =
        `detail-item${
            label === "Patient ID"
            || label === "Residential address"
            || label === "Medical problem"
                ? " wide"
                : ""
        }`;

    // Create the field label.
    const term = document.createElement("dt");
    term.textContent = label;

    // Create the field value.
    const description = document.createElement("dd");

    // Display a dash for empty or missing values.
    description.textContent =
        value === null || value === undefined || value === ""
            ? "—"
            : String(value);

    // Add the label and value to the container.
    wrapper.append(term, description);

    return wrapper;
}


// Format timestamps for the Created and Last updated fields.
function formatTimestamp(value) {
    // Return a dash if the timestamp is missing.
    if (!value) {
        return "—";
    }

    const parsed = new Date(value);

    // Return the original timestamp if it cannot be parsed.
    return Number.isNaN(parsed.getTime())
        ? value
        : new Intl.DateTimeFormat(undefined, {
            year: "numeric",
            month: "short",
            day: "numeric",
            hour: "numeric",
            minute: "2-digit"
        }).format(parsed);
}


// Delete a patient after asking for confirmation.
async function deletePatient(id) {
    // Find the patient's name for the confirmation message.
    const patient = patients.find((record) => record.id === id);

    const name = patient
        ? `“${patient.patientName}”`
        : "this patient";

    // Ask the user to confirm the deletion.
    if (!window.confirm(
        `Delete ${name}? This action cannot be undone.`
    )) {
        return;
    }

    try {
        // Send the DELETE request to the backend.
        await request(
            `/api/patients/${encodeURIComponent(id)}`,
            { method: "DELETE" }
        );

        // Display success and reload the patient list.
        showNotice("Patient record deleted.");
        await refreshPatients();

    } catch (error) {
        // Display an error if deletion fails.
        showNotice(error.message, true);
    }
}


// Handle form submission for adding or updating a patient.
patientForm.addEventListener("submit", async (event) => {
    // Prevent the browser from reloading the page.
    event.preventDefault();

    // Remove old validation messages.
    clearFieldErrors();

    // Check the HTML form's built-in validation rules.
    if (!patientForm.reportValidity()) {
        return;
    }

    // Collect the submitted form values.
    const formData = new FormData(patientForm);
    const payload = Object.fromEntries(formData.entries());

    // Convert the age field from text to a number.
    payload.age = Number(payload.age);

    // Determine whether the user is adding or editing a patient.
    const wasEditing = Boolean(editingId);

    try {
        // Use PUT for updates and POST for new patient records.
        await request(
            wasEditing
                ? `/api/patients/${encodeURIComponent(editingId)}`
                : "/api/patients",
            {
                method: wasEditing ? "PUT" : "POST",
                body: JSON.stringify(payload)
            }
        );

        // Close the form after a successful save.
        patientDialog.close();

        // Clear the editing ID.
        editingId = null;

        // Display the appropriate success message.
        showNotice(
            wasEditing
                ? "Patient record updated."
                : "Patient record added."
        );

        // Reload the table to show the latest data.
        await refreshPatients();

    } catch (error) {
        // Display the error returned by the backend.
        formError.textContent = error.message;
        formError.hidden = false;
    }
});


// Handle clicks on the View, Edit, and Delete buttons.
// One listener handles all table rows using event delegation.
rows.addEventListener("click", (event) => {
    // Find the action button that was clicked.
    const button = event.target.closest("button[data-action]");

    // Ignore clicks that did not originate from an action button.
    if (!button) {
        return;
    }

    // Read the action and patient ID stored on the button.
    const { action, id } = button.dataset;

    // Call the function corresponding to the selected action.
    if (action === "view") {
        showDetails(id);

    } else if (action === "edit") {
        openEditForm(id);

    } else if (action === "delete") {
        deletePatient(id);
    }
});


// Update the table whenever the user changes the search text.
searchInput.addEventListener("input", renderPatients);

// Open the Add Patient form when the add button is clicked.
document.querySelector("#add-patient")
    .addEventListener("click", openCreateForm);

// Attach close handlers to buttons marked with data-close.
document.querySelectorAll("[data-close]").forEach((button) => {
    button.addEventListener("click", () => {
        // Find and close the dialog specified by the button.
        document.getElementById(button.dataset.close).close();
    });
});


// Load patient records automatically when the page starts.
refreshPatients();