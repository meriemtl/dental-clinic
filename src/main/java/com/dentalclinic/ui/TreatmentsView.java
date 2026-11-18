package com.dentalclinic.ui;

import com.dentalclinic.model.TreatmentType;
import com.dentalclinic.service.TreatmentService;
import com.dentalclinic.utils.Fmt;
import com.dentalclinic.utils.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** The clinic's price list. */
final class TreatmentsView extends BorderPane {
    private final TreatmentService service = new TreatmentService();
    private final TableView<TreatmentType> table = Ui.table();

    TreatmentsView() {
        setPadding(new Insets(24));

        HBox header = new HBox(12, Ui.pageTitle("Treatments and prices"));
        header.setAlignment(Pos.CENTER_LEFT);
        VBox top = new VBox(6, header, Ui.muted("The price list used when recording treatments for a patient."));
        top.setPadding(new Insets(0, 0, 14, 0));
        setTop(top);

        Ui.addColumn(table, "Treatment", 220, TreatmentType::getName);
        Ui.addColumn(table, "Description", 320, t -> Fmt.nullToEmpty(t.getDescription()));
        Ui.addColumn(table, "Default price", 130, t -> Fmt.money(t.getDefaultPrice()));
        Ui.addColumn(table, "Status", 90, t -> t.isActive() ? "Active" : "Inactive");
        setCenter(table);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(14, 0, 0, 0));
        if (Session.canManageCatalog()) {
            actions.getChildren().addAll(
                    Ui.button("+ New treatment", "primary", () -> {
                        if (TreatmentTypeDialog.show(null)) {
                            refresh();
                        }
                    }),
                    Ui.button("Edit", "secondary", () -> {
                        if (TreatmentTypeDialog.show(selected())) {
                            refresh();
                        }
                    }),
                    Ui.button("Activate / deactivate", "secondary", () -> {
                        TreatmentType t = selected();
                        t.setActive(!t.isActive());
                        service.saveType(t);
                        refresh();
                    }));
        } else {
            actions.getChildren().add(Ui.muted("Only dentists and administrators can edit the price list."));
        }
        setBottom(actions);

        refresh();
    }

    private void refresh() {
        Ui.run(() -> table.setItems(Ui.items(service.findAllTypes())));
    }

    private TreatmentType selected() {
        TreatmentType t = table.getSelectionModel().getSelectedItem();
        if (t == null) {
            throw new IllegalStateException("Please select a treatment in the list first.");
        }
        return t;
    }
}
