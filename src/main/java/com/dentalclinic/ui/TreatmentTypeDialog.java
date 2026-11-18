package com.dentalclinic.ui;

import com.dentalclinic.model.TreatmentType;
import com.dentalclinic.service.TreatmentService;
import com.dentalclinic.utils.Fmt;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.math.RoundingMode;

/** Create / edit form for an entry of the price list. */
final class TreatmentTypeDialog {
    private TreatmentTypeDialog() {
    }

    /** @return true when the treatment was saved */
    static boolean show(TreatmentType existing) {
        TreatmentService service = new TreatmentService();
        TextField name = new TextField();
        TextField description = new TextField();
        TextField price = new TextField();
        CheckBox active = new CheckBox("Active (can be selected when recording treatments)");
        active.setSelected(true);

        if (existing != null) {
            name.setText(existing.getName());
            description.setText(Fmt.nullToEmpty(existing.getDescription()));
            price.setText(existing.getDefaultPrice().setScale(2, RoundingMode.HALF_UP).toPlainString());
            active.setSelected(existing.isActive());
        }

        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "Name *", name);
        Ui.addRow(grid, 1, "Description", description);
        Ui.addRow(grid, 2, "Default price *", price);
        Ui.addRow(grid, 3, "", active);
        grid.setPrefWidth(460);

        return Ui.showForm(existing == null ? "New treatment" : "Edit treatment", "Save", grid, () -> {
            TreatmentType t = existing == null ? new TreatmentType() : existing;
            String previousName = t.getName();
            t.setName(name.getText().trim());
            t.setDescription(Fmt.blankToNull(description.getText()));
            t.setActive(active.isSelected());
            try {
                t.setDefaultPrice(Fmt.parseMoney(price.getText()));
                service.saveType(t);
            } catch (RuntimeException e) {
                t.setName(previousName); // keep the list consistent if saving failed
                throw e;
            }
        });
    }
}
