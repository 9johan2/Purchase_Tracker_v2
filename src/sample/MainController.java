package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import sample.datamodel.DataSource;
import sample.datamodel.Item;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController {

    private final DataSource dataSource = DataSource.getInstance();
    private final Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
    private final Alert errorAlert = new Alert(Alert.AlertType.ERROR);

    @FXML
    private BorderPane mainWindow;
    @FXML
    private DatePicker filterFrom;
    @FXML
    private DatePicker filterTo;
    @FXML
    private ListView<Item> leftListView;
    @FXML
    private ListView<Item> rightListView;
    @FXML
    private Label leftLabel;
    @FXML
    private Label rightLabel;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label statusLabel;

    public void initialize() {
        infoAlert.setHeaderText(null);
        errorAlert.setHeaderText(null);

        // Setting up the choice of item category filters
        List<String> categories = new ArrayList<>();
        categories.add("No filter");
        categories.addAll(Item.getCategories());
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        addContextMenu();

    }

    private void addContextMenu() {
        ContextMenu leftListMenu = new ContextMenu();
        ContextMenu rightListMenu = new ContextMenu();

        MenuItem deleteLeftItem = new MenuItem("Delete item");
        deleteLeftItem.setOnAction((e) -> deleteItem(leftListView));

        MenuItem deleteRightItem = new MenuItem("Delete item");
        deleteRightItem.setOnAction((e) -> deleteItem(rightListView));

        leftListMenu.getItems().add(deleteLeftItem);
        rightListMenu.getItems().add(deleteRightItem);

        leftListView.setContextMenu(leftListMenu);
        rightListView.setContextMenu(rightListMenu);
    }

    @FXML
    public void refreshLists() {
        statusLabel.setText("");
            // If no dates are selected the range will be from the first day of this year up to current day
        if (filterFrom.getValue() == null || filterTo.getValue() == null) {

            filterFrom.setValue(LocalDate.of(LocalDate.now().getYear(), 1, 1));
            filterTo.setValue(LocalDate.now());
        }
            // Converting dates to integers to match the database structure
        int from = Integer.parseInt(filterFrom.getValue().toString().replaceAll("-", ""));
        int to = Integer.parseInt(filterTo.getValue().toString().replaceAll("-", ""));

        if (from > to) {
            infoAlert.setTitle("Invalid dates");
            infoAlert.setContentText("Please reverse the order of the dates");
            infoAlert.showAndWait();
            return;
        }
        if (from < 20000000) {
            infoAlert.setTitle("Invalid dates");
            infoAlert.setContentText("Sorry, only transactions performed in the 21st century will be shown");
            infoAlert.showAndWait();
            return;
        }

        Task<ObservableList<Item>> leftListTask = new Task<>() {
            @Override
            protected ObservableList<Item> call() {
                return FXCollections.observableArrayList(dataSource.filteredQuery(Item.Buyer.LEFT.getName(), from, to, categoryComboBox.getValue()));
            }
        };

        Task<ObservableList<Item>> rightListTask = new Task<>() {
            @Override
            protected ObservableList<Item> call() {
                return FXCollections.observableArrayList(dataSource.filteredQuery(Item.Buyer.RIGHT.getName(), from, to, categoryComboBox.getValue()));
            }
        };

        leftListView.itemsProperty().bind(leftListTask.valueProperty());
        rightListView.itemsProperty().bind(rightListTask.valueProperty());

        progressBar.progressProperty().bind(rightListTask.progressProperty());
        progressBar.setVisible(true);

        leftListTask.setOnSucceeded(e -> {
            List<Item> leftList = leftListView.getItems();
            updateLabelSum(leftLabel, leftList);
        });
        rightListTask.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            List<Item> rightList = rightListView.getItems();
            updateLabelSum(rightLabel, rightList);
        });
        rightListTask.setOnFailed(e -> progressBar.setVisible(false));

        new Thread(rightListTask).start();
        new Thread(leftListTask).start();

    }

    @FXML
    private void updateLabelSum(Label label, List<Item> list) {
        double sum = 0;

        if (list != null && !list.isEmpty()) {
            for (Item item : list) {
                sum += item.getPrice();
            }
        }
        label.setText("Sum: " + String.format("%.2f", sum));
    }


    @FXML
    public void addItem() {
        System.out.println("add item called");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainWindow.getScene().getWindow());
        dialog.setTitle("New purchase");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("addItem.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException e) {
            System.out.println("Could not load dialog: " + e.getMessage());
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        AddItemController controller = loader.getController();
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            Item item = controller.createItem();
            try {
                dataSource.insertItem(item);
                refreshLists();
                statusLabel.setText("Item added successfully.");

            } catch (SQLException e) {
                errorAlert.setTitle("Unexpected error");
                errorAlert.setContentText("Error while creating item");
                errorAlert.showAndWait();
            }
        }
    }

    @FXML
    public void deleteItem(ListView<Item> listView) {
        Item item = listView.getSelectionModel().getSelectedItem();
        if (item == null) {
            infoAlert.setTitle("No item selected");
            infoAlert.setContentText("Please select an item to delete");
            infoAlert.showAndWait();
        } else {
            if (dataSource.deleteItem(item.getId())) {
                refreshLists();
                statusLabel.setText("Item deleted");
            } else {
                errorAlert.setTitle("Unexpected error");
                errorAlert.setContentText("An unexpected error occurred");
                errorAlert.showAndWait();
                statusLabel.setText("Error while deleting item");
            }
        }
    }

    @FXML
    public void exportData() {
        try {
            dataSource.exportData();
        } catch (FileNotFoundException e) {
            errorAlert.setTitle("File not found");
            errorAlert.setContentText("The file you specified can not be found");
            errorAlert.showAndWait();
        } catch (IOException e) {
            errorAlert.setTitle("Error");
            errorAlert.setContentText("An unexpected error occurred");
            errorAlert.showAndWait();
        }
    }

    @FXML
    public void importData() {
        try {
            dataSource.importData();
        } catch (FileNotFoundException e) {
            errorAlert.setTitle("File not found");
            errorAlert.setContentText("The file you specified can not be found");
            errorAlert.showAndWait();
        } catch (Exception e) {
            errorAlert.setTitle("Error");
            errorAlert.setContentText("An unexpected error occurred");
            errorAlert.showAndWait();
        }

    }

    @FXML
    public void exit() {
        Platform.exit();
    }
}
