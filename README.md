# Group 24 - MotorPH Employee Application

## Terminal Assessment: Finalized MotorPH Employee Application
This repository contains the source code and documentation for the MotorPH Employee Application, developed as part of the MO-IT103 course requirements. The application streamlines employee record management, attendance tracking, and payroll processing through a role-based interface.

---
## Features

### Login System
- Secure login page
- Role-based access
- Supports:
  - Employee
  - Payroll Staff
  - Administrator

---

### Employee Module

Employees can:

- View their personal information
- View payroll details
- Check salary breakdown

---

### Payroll Module

Payroll staff can:

- Process payroll for a single employee
- Process payroll for all employees
- Automatically calculate:
  - Hours worked
  - Gross salary
  - SSS Contribution
  - PhilHealth Contribution
  - Pag-IBIG Contribution
  - Withholding Tax
  - Total Deductions
  - Net Salary

Payroll calculations follow the MotorPH payroll rules.

---

### Admin Module

Administrators can:

- View all employee records
- Add new employees
- Edit employee information
- Delete employee records
- Automatically generate employee numbers
- Save all changes directly to the Employee CSV file

---

**Files Used**
- Employee Data File: MotorPH_Employee_Details.csv
- Attendance File: Attendance_Record.csv

These files contain employee information and attendance records used by the program.

**Generated Files**
- Payroll_Report.csv

This file contains the summary of the automated payroll calculation.

---

## Running the Application

1. Clone or download this repository.
2. Ensure that the required CSV files are located in the application's root directory.
3. Open the project in your preferred Java IDE (such as IntelliJ IDEA or NetBeans).
4. Build and run the Main.java file.

**Available accounts:**
- Employee

  Username: employee
  
  Password: 12345

- Payroll Staff

  Username: payroll_staff
  
  Password: 12345

- Admin

  Username: admin

  Password: 12345

---

## Group Members
France Arielle Plandaño lr.faplandano@mmdc.mcl.edu.ph

Jefferson Viray lr.jviray@mmdc.mcl.edu.ph

Marie Anne Omagap lr.maomagap@mmdc.mcl.edu.ph

Marjorie Mellan Ragot lr.mmragot@mmdc.mcl.edu.ph

Charles Darwin Maraña lr.cdmarana@mmdc.mcl.edu.ph
