package sample;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sample.datamodel.Item;

import java.time.LocalDate;

public class AddItemController {
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> categoryBox;
    @FXML
    private TextField priceField;
    @FXML
    private CheckBox currencyChoice;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> buyerBox;

    public void initialize() {
        categoryBox.setItems(FXCollections.observableList(Item.getCategories()));
        buyerBox.setItems(FXCollections.observableList(Item.getBuyers()));
    }

    public Item createItem() {
        String name = nameField.getText();
        String category;
        if (categoryBox.getValue() != null) {
            category = categoryBox.getValue();
        } else {
            category = Item.Category.OTHER.getDescription();
        }

        double price;
        try {
            String temp = priceField.getText();
            if (!temp.matches("[0-9]+\\.[0-9]+") || !temp.matches("[0-9]+")) {
                temp = temp.replaceAll(",","\\.");
            }
            price = Double.parseDouble(temp);
        } catch (NumberFormatException e) {
            price = 0.0;
        }
        if (currencyChoice.isSelected()) {
            price *= 2.5;
        }
        LocalDate dateOfPurchase = datePicker.getValue();
        if (dateOfPurchase == null) {
            dateOfPurchase = LocalDate.now();
        }

        String buyer = Item.Buyer.LEFT.getName();
        if (buyerBox.getValue() != null) {
            buyer = buyerBox.getValue();
        }

        return new Item(name,category,price,dateOfPurchase,buyer);
    }
}
