package com.dentalclinic.ui;

import com.dentalclinic.model.Patient;
import com.dentalclinic.model.TreatmentRecord;
import com.dentalclinic.model.TreatmentType;
import com.dentalclinic.model.User;
import com.dentalclinic.service.TreatmentService;
import com.dentalclinic.service.UserService;
import com.dentalclinic.utils.Fmt;
import com.dentalclinic.utils.Session;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** Records a treatment performed on a patient. */
final class TreatmentRecordDialog {
    private TreatmentRecordDialog() {
    }

    /** @return true when the treatment was saved */
    static boolean show(Patient patient) {
        TreatmentService treatments = new TreatmentService();
        List<TreatmentType> types = treatments.findActiveTypes();
        List<User> dentists = new UserService().findActiveDentists();
        if (types.isEmpty()) {
            throw new IllegalStateException(
                    "The price list is empty. Add treatments in the Treatments screen first.");
        }
        if (dentists.isEmpty()) {
            throw new IllegalStateException(
                    "No active dentist found. An administrator must create one in the Staff screen.");
        }

        ComboBox<TreatmentType> type = Ui.combo(types, TreatmentType::getName);
        TextField tooth = new TextField();
        tooth.setPromptText("e.g. 16");
        ComboBox<User> dentist = Ui.combo(dentists, User::getFullName);
        User current = Session.getUser();
        dentist.setValue(dentists.stream()
                .filter(d -> current != null && d.getUserId() == current.getUserId())
                .findFirst().orElse(dentists.get(0)));
        DatePicker date = new DatePicker(LocalDate.now());
        TextField price = new TextField();
        TextArea notes = Ui.textArea(3);

        type.setOnAction(e -> {
            TreatmentType t = type.getValue();
            if (t != null) {
                price.setText(t.getDefaultPrice().setScale(2, RoundingMode.HALF_UP).toPlainString());
            }
        });

        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "Treatment *", type);
        Ui.addRow(grid, 1, "Tooth number", tooth);
        Ui.addRow(grid, 2, "Dentist *", dentist);
        Ui.addRow(grid, 3, "Date *", date);
        Ui.addRow(grid, 4, "Price *", price);
        Ui.addRow(grid, 5, "Notes", notes);
        grid.setPrefWidth(460);

        return Ui.showForm("Record a treatment - " + patient.getFullName(), "Save", grid, () -> {
            TreatmentType t = type.getValue();
            User d = dentist.getValue();
            if (t == null) {
                throw new IllegalArgumentException("Please choose a treatment.");
            }
            if (d == null) {
                throw new IllegalArgumentException("Please choose a dentist.");
            }
            TreatmentRecord r = new TreatmentRecord();
            r.setPatientId(patient.getPatientId());
            r.setUserId(d.getUserId());
            r.setTypeId(t.getTypeId());
            r.setTreatmentName(t.getName());
            r.setToothNumber(Fmt.blankToNull(tooth.getText()));
            r.setNotes(Fmt.blankToNull(notes.getText()));
            r.setPrice(Fmt.parseMoney(price.getText()));
            r.setPerformedOn(date.getValue());
            treatments.saveRecord(r);
        });
    }
}
