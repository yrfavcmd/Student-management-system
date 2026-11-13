# Student Management System

A Java Swing desktop application for managing students, courses, enrollments, and grades. It includes a console fallback for headless environments.

## Features

- Add, edit, search, and delete students
- Add and delete courses
- Enroll students in courses and prevent duplicate enrollments
- Record and update grades from 0 to 100
- View student reports with course grades and average grade
- Automatically remove related enrollments when a student or course is deleted

## Requirements

- Java Development Kit (JDK) 8 or later

## Run

From the project directory in PowerShell:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out src\Main.java src\Student.java src\Course.java src\StudentManagementFrame.java
java -cp out Main
```

The application opens its desktop interface when a graphical environment is available. Otherwise, it starts in the console.

## Data

Student, course, enrollment, and grade data is stored in memory and is cleared when the application exits.
