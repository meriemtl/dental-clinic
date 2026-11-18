package com.dentalclinic.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Small helpers shared by all screens (alerts, tables, forms, dialogs). */
final class Ui {
    static final String CSS = Ui.class.getResource("/styles.css").toExternalForm();

    private Ui() {
    }

    // ------------------------------------------------------------------ messages

    static void info(String message) {
        alert(Alert.AlertType.INFORMATION, "Information", message);
    }

    static void warn(String message) {
        alert(Alert.AlertType.WARNING, "Attention", message);
    }

    static void error(String message) {
        alert(Alert.AlertType.ERROR, "Error", message);
    }

    private static void alert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getDialogPane().getStylesheets().add(CSS);
        a.showAndWait();
    }

    static boolean confirm(String title, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getDialogPane().getStylesheets().add(CSS);
        Optional<ButtonType> result = a.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    static String message(Throwable t) {
        String m = t.getMessage();
        return m == null || m.isBlank() ? t.getClass().getSimpleName() : m;
    }

    /** Runs an action; problems are shown in an alert instead of breaking the screen. */
    static void run(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            warn(message(e));
        } catch (RuntimeException e) {
            error(message(e));
        }
    }

    // ------------------------------------------------------------------ small controls

    static Button button(String text, String styleClass, Runnable action) {
        Button b = new Button(text);
        if (styleClass != null) {
            b.getStyleClass().add(styleClass);
        }
        b.setOnAction(e -> run(action));
        return b;
    }

    static Label pageTitle(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("page-title");
        return l;
    }

    static Label sectionTitle(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-title");
        return l;
    }

    static Label muted(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        return l;
    }

    static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    static TextArea textArea(int rows) {
        TextArea t = new TextArea();
        t.setPrefRowCount(rows);
        t.setWrapText(true);
        return t;
    }

    static <T> StringConverter<T> converter(Function<T, String> label) {
        return new StringConverter<T>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : label.apply(value);
            }

            @Override
            public T fromString(String text) {
                return null;
            }
        };
    }

    static <T> ComboBox<T> combo(List<T> items, Function<T, String> label) {
        ComboBox<T> box = new ComboBox<>();
        box.getItems().addAll(items);
        box.setConverter(converter(label));
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    /** NO_SHOW becomes "No show". */
    static String pretty(Enum<?> value) {
        if (value == null) {
            return "";
        }
        String text = value.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    static <T> ObservableList<T> items(List<T> list) {
        return FXCollections.observableArrayList(list);
    }

    // ------------------------------------------------------------------ tables

    static <T> TableView<T> table() {
        TableView<T> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("Nothing to display."));
        return t;
    }

    /** Adds a text column to the table; the value is computed from the row object. */
    static <T> void addColumn(TableView<T> table, String title, double width, Function<T, String> value) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        table.getColumns().add(col);
    }

    // ------------------------------------------------------------------ forms and dialogs

    static GridPane form() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(10);
        g.setPadding(new Insets(4));
        return g;
    }

    static void addRow(GridPane grid, int row, String label, Node field) {
        Label l = new Label(label);
        l.getStyleClass().add("form-label");
        grid.add(l, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    /** Places a label at (col,row) and the field right of it, spanning "span" columns. */
    static void addField(GridPane grid, int col, int row, int span, String label, Node field) {
        Label l = new Label(label);
        l.getStyleClass().add("form-label");
        grid.add(l, col, row);
        grid.add(field, col + 1, row, span, 1);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    /**
     * Shows a Save / Cancel dialog around a form. The onSave action runs when the user clicks
     * Save; if it throws, the message is displayed in the dialog and the dialog stays open.
     *
     * @return true when the data was saved
     */
    static boolean showForm(String title, String saveText, Node form, Runnable onSave) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(CSS);
        ButtonType saveType = new ButtonType(saveText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Label error = new Label();
        error.getStyleClass().add("error-text");
        error.setWrapText(true);
        error.setMaxWidth(520);
        VBox box = new VBox(12, form, error);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);

        boolean[] saved = {false};
        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                onSave.run();
                saved[0] = true;
            } catch (RuntimeException ex) {
                error.setText(message(ex));
                event.consume(); // keep the dialog open
            }
        });
        dialog.showAndWait();
        return saved[0];
    }

    /** Shows read-only content with a Close button. */
    static void showContent(String title, Node content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(CSS);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
}
