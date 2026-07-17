# Group 24 - MotorPH Employee Application

## Terminal Assessment: Finalized MotorPH Employee Application
This repository contains the source code and documentation for the MotorPH Employee Application, developed as part of the IT103 course requirements. The application streamlines employee record management, attendance tracking, and payroll processing through a role-based interface.

---

## **Program details**
The MotorPH Payroll System is a Java program that calculates employee payroll using employee information and attendance records.
The program reads data from CSV files and performs the following tasks:
- Displays all employee information
- Calculates total hours worked
- Calculates gross salary based on hourly rate
- Calculates deductions (SSS, PhilHealth, Pag-IBIG, Tax)
- Calculates net salary
Payroll can be processed for one employee or all employees.
The payroll records are calculated for cutoff periods from June to December.

---

### **How the system works**
**Login System**
The program starts with a login screen where the user enters a username and password.

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

**Employee Access**

If the user logs in as employee, they can:
- Enter their employee number
- View their employee details, including:
  - Employee Number
  - Employee Name
  - Birthday
- View their payslip

If the employee number does not exist, the program displays an error message.

**Payroll Staff Access**

If the user logs in as payroll_staff, they can:
- Process payroll for one employee
- Process payroll for all employees
- Generate payroll summary

The system calculates:
- Total Hours Worked
- Gross Salary
- Government Deductions
- Net Salary

**Admin Access**

If the user logs in as admin, they can:
- View all employee records
- Add new employees
- Edit existing employee records
- Delete employee records

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

---

## Quick Links
Homework 1 Docs:(https://docs.google.com/spreadsheets/d/15dP33snFRs8yTzUYBFgnr9rBPT60C8DrKaGoaPqU1JM/edit?usp=sharing) 

---

## Group Members
France Arielle Plandaño lr.faplandano@mmdc.mcl.edu.ph

Jefferson Viray lr.jviray@mmdc.mcl.edu.ph

Marie Anne Omagap lr.maomagap@mmdc.mcl.edu.ph

Marjorie Mellan Ragot lr.mmragot@mmdc.mcl.edu.ph

Charles Darwin Maraña lr.cdmarana@mmdc.mcl.edu.ph
