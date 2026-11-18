# Dental Clinic Management System

JavaFX desktop application (Java 17+) with a MySQL database, for managing a dental clinic:
patients, appointments, treatments, billing and staff accounts.

## Features

| Screen | What it does |
|---|---|
| **Login** | Secure sign-in (BCrypt-hashed passwords), three roles: Administrator, Dentist, Staff |
| **Dashboard** | Patients count, appointments today / next 7 days, revenue this month, outstanding balance, today's agenda |
| **Patients** | Add / edit / delete, live search (name, phone, e-mail), full **patient file** (info, appointments, treatments, invoices) |
| **Appointments** | Day view with previous / next day, all-dates view, dentist filter, create / reschedule / complete / cancel / no-show / delete, **overlap detection** per dentist |
| **Treatments** | Price list (consultation, filling, crown...) editable by dentists and administrators; treatments recorded per patient with tooth number |
| **Billing** | Invoices built from unbilled treatments + extra lines, partial payments (cash / card / cheque / transfer), status Unpaid / Partial / Paid |
| **Staff** (admin only) | Create users, change role, activate / deactivate, reset passwords |
| **Settings** | Change my password, configuration summary |

Permissions: administrators can do everything (including deleting patients / invoices); dentists can record
treatments and edit the price list; staff handle patients, appointments and billing.

## Requirements

1. **JDK 17 or newer** - check with `java -version` (JDK 21 recommended)
2. **Apache Maven 3.8+** - check with `mvn -version`
   (Windows: `winget install Apache.Maven`, then open a new terminal)
3. **A MySQL 8 (or MariaDB) server** that is running - see "MySQL" below

JavaFX, the MySQL driver and jBCrypt are downloaded automatically by Maven (internet needed the first time).

## Quick start

```bat
cd Dental-Clinic-folder
:: 1. edit config.properties -> db.password must be YOUR MySQL root password
:: 2. start MySQL
:: 3. run:
mvn javafx:run
```
(or double-click `run.bat`).

On the **first start** the application creates the database `dental_clinic`, all tables, the default
accounts, a price list and a few demo patients/appointments (set `app.sample-data=false` in
`config.properties` for an empty database). You do **not** need to run any SQL script.

### Default accounts (change the passwords!)

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Administrator |
| `dentist` | `dentist123` | Dentist |
| `reception` | `reception123` | Staff |

Change them in **Staff -> Reset password** (administrator) or **Settings** (each user).

## MySQL

Pick one:

* **XAMPP / WAMP / MySQL Installer**: start MySQL, then put the root password in `config.properties`
  (`db.password=` empty for a default XAMPP install).
* **Docker**: `docker compose up -d` in this folder gives a MySQL with user `root` / password `password`
  (the defaults of `config.properties`).

### config.properties

```properties
db.host=localhost
db.port=3306
db.name=dental_clinic
db.user=root
db.password=password
clinic.name=Dental Clinic
currency=DA
agenda.start=08:00
agenda.end=18:00
agenda.slot=15
app.sample-data=true
```
Restart the application after editing it. The file must be in the folder you start the application from.

## Other ways to run

* **VS Code**: open the folder, install "Extension Pack for Java", let it import the Maven project,
  then use Run -> *Dental Clinic* (`.vscode/launch.json`, class `com.dentalclinic.Launcher`) or run
  `mvn javafx:run` in the terminal.
* **Without Maven**: put the two jars described in `lib/README.txt` in `lib/`, edit `JAVAFX_LIB` in
  `run-without-maven.bat`, and run it.
* **Old database**: if you already created `dental_clinic` with the original script, it still works (missing
  columns are added automatically). If login fails or you want a clean start: `DROP DATABASE dental_clinic;`
  in MySQL, then start the application again.

## Project structure

```
pom.xml                         Maven build (JavaFX 21, MySQL driver, jBCrypt)
config.properties               Settings (database, clinic name, currency, agenda hours)
database/dental_clinic.sql      Optional manual copy of the schema
src/main/resources/schema.sql   Schema used by the application (single source of truth)
src/main/resources/styles.css   JavaFX stylesheet
src/main/java/com/dentalclinic/
  Main, Launcher                Entry points
  model/                        Patient, Appointment, User, TreatmentType, TreatmentRecord, Invoice, ...
  dao/                          JDBC data access (interfaces + implementations)
  service/                      Business rules and validation
  utils/                        Config, database connection/initialisation, JDBC helper, formatting, session
  ui/                           JavaFX screens and dialogs (built in code)
```

## What was wrong in the original project (and what changed)

* **It could not compile or start**: `AppointmentDAO` lacked an import, `MainController` lacked its JavaFX
  imports, `PatientListController` used properties that `Patient` does not have, and `styles.css`,
  the images and the dashboard / appointment views referenced by the FXML files did not exist
  (`PatientFormView.fxml` was cut off).
* **Database access broke after the first query**: every DAO closed the single shared connection.
  Each operation now opens and closes its own connection (`utils/Jdbc.java`).
* **Errors were swallowed** (`printStackTrace`, empty lists). Failures are now reported to the user.
* **Missing dependencies**: `src/lib` was empty (MySQL driver and jBCrypt). Maven manages them now.
* **The admin password hash in the SQL script was unverified**. Default accounts are now created by the
  application with freshly generated BCrypt hashes.
* **Missing functionality**: treatments, billing/payments, dashboard, staff management, roles, patient
  file, appointment overlap detection (cancelled appointments free their slot).
* The FXML screens were replaced by screens built in Java code (same layered architecture:
  model / DAO / service / UI), which removes the id-mismatch errors the FXML files had.

## Troubleshooting

| Message | Fix |
|---|---|
| *Cannot connect to MySQL...* | Start MySQL; check `db.host`, `db.port`, `db.user`, `db.password` |
| *Access denied for user 'root'* | Wrong `db.password` in `config.properties` |
| *Communications link failure* | MySQL is not running or the port is wrong |
| *JavaFX runtime components are missing* | Start with `mvn javafx:run` or the `Launcher` class, not `Main` |
| `mvn` not recognised | Install Maven and open a new terminal; `JAVA_HOME` must point to a JDK 17+ |
| Invalid login after using an old database | `DROP DATABASE dental_clinic;` and restart the application |

## Testing status

The data layer SQL, the business rules (billing, payments, overlap detection, authentication, validation)
and the compilation of all classes were verified automatically during development. The JavaFX screens
could not be executed in the environment used to produce this project, so they have not been
tested on a real display: if you find a visual or behavioural problem, please report it.
