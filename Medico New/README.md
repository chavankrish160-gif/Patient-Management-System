# Patient Record Management System

A small patient-record demonstration application with a Core Java backend serving an HTML/CSS/JavaScript frontend. The frontend reads and writes patient records through the Java HTTP API. The backend persists them to `data/patients.json`; no framework, Maven, database, or browser `localStorage` is used.

> This is a college demonstration project. Use fictional patient information only.

## Run
Compile

 `  javac -d target src/patientrecord/*.java   `
 
Run 

 `  java -cp target patientrecord.PatientRecordsApplication `
 
  

The app supports adding, listing, searching, viewing, editing, and deleting patient records. To clear records, stop the application and remove `data/patients.json`.


