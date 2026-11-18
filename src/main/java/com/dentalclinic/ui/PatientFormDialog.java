package com.dentalclinic.ui;

import com.dentalclinic.model.Patient;
import com.dentalclinic.model.Patient.Gender;
import com.dentalclinic.service.PatientService;
import com.dentalclinic.utils.Fmt;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.List;

/** Create / edit form for a patient. */
final class PatientFormDialog {
    private PatientFormDialog() {
    }

    /** @return true when the patient was saved */
    static boolean show(Patient existing) {
        PatientService service = new PatientService();

        TextField first = new TextField();
        TextField last = new TextField();
        DatePicker dob = new DatePicker();
        ComboBox<Gender> gender = Ui.combo(List.of(Gender.values()), Ui::pretty);
        TextField phone = new TextField();
        TextField email = new TextField();
        ComboBox<String> blood = Ui.combo(
                List.of("", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
                s -> s.isEmpty() ? "Unknown" : s);
        TextField address = new TextField();
        TextArea allergies = Ui.textArea(2);
        TextArea history = Ui.textArea(3);

        first.setPrefWidth(220);
        last.setPrefWidth(220);

        if (existing != null) {
            first.setText(existing.getFirstName());
            last.setText(existing.getLastName());
            dob.setValue(existing.getDateOfBirth());
            gender.setValue(existing.getGender());
            phone.setText(Fmt.nullToEmpty(existing.getPhoneNumber()));
            email.setText(Fmt.nullToEmpty(existing.getEmail()));
            blood.setValue(Fmt.nullToEmpty(existing.getBloodType()));
            address.setText(Fmt.nullToEmpty(existing.getAddress()));
            allergies.setText(Fmt.nullToEmpty(existing.getAllergies()));
            history.setText(Fmt.nullToEmpty(existing.getMedicalHistory()));
        } else {
            blood.setValue("");
        }

        GridPane grid = Ui.form();
        Ui.addField(grid, 0, 0, 1, "First name *", first);
        Ui.addField(grid, 2, 0, 1, "Last name *", last);
        Ui.addField(grid, 0, 1, 1, "Date of birth *", dob);
        Ui.addField(grid, 2, 1, 1, "Gender *", gender);
        Ui.addField(grid, 0, 2, 1, "Phone", phone);
        Ui.addField(grid, 2, 2, 1, "E-mail", email);
        Ui.addField(grid, 0, 3, 1, "Blood type", blood);
        Ui.addField(grid, 2, 3, 1, "Address", address);
        Ui.addField(grid, 0, 4, 3, "Allergies", allergies);
        Ui.addField(grid, 0, 5, 3, "Medical history", history);

        String title = existing == null ? "New patient" : "Edit patient";
        return Ui.showForm(title, "Save", grid, () -> {
            Patient p = new Patient();
            if (existing != null) {
                p.setPatientId(existing.getPatientId());
            }
            p.setFirstName(first.getText().trim());
            p.setLastName(last.getText().trim());
            p.setDateOfBirth(dob.getValue());
            p.setGender(gender.getValue());
            p.setPhoneNumber(Fmt.blankToNull(phone.getText()));
            p.setEmail(Fmt.blankToNull(email.getText()));
            p.setBloodType(Fmt.blankToNull(blood.getValue()));
            p.setAddress(Fmt.blankToNull(address.getText()));
            p.setAllergies(Fmt.blankToNull(allergies.getText()));
            p.setMedicalHistory(Fmt.blankToNull(history.getText()));
            service.savePatient(p);
        });
    }
}
