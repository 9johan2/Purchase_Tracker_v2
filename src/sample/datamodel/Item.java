package sample.datamodel;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class Item {

    private int id;
    private String name;
    private String category;
    private double price;
    private LocalDate dateOfPurchase;
    private String buyer;


    public Item() {

    }

    public Item(String name, String category, double price, LocalDate dateOfPurchase, String buyer) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.dateOfPurchase = dateOfPurchase;
        this.buyer = buyer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getDateOfPurchase() {
        return dateOfPurchase;
    }

    public void setDateOfPurchase(LocalDate dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    @Override
    public String toString() {
        return this.category + "\t" + this.name + "\n" + this.price + "\t" + this.dateOfPurchase.toString();
    }

    public static List<String> getCategories() {
        Category[] categories = Category.values();
        String[] strings = new String[categories.length];

        for (int i = 0; i<categories.length; i++) {
            strings[i] = categories[i].description;
        }

        return Arrays.asList(strings);
    }

    public static List<String> getBuyers() {
        Buyer[] buyers = Buyer.values();
        String[] strings = new String[buyers.length];

        for (int i = 0; i<buyers.length; i++) {
            strings[i] = buyers[i].name;
        }

        return Arrays.asList(strings);
    }


    // Provides constants for purchase categories
    public enum Category {
        ACTIVITIES ("Activities"),
        FOOD ("Food"),
        FURNISHING ("Furnishing"),
        PHONE ("Phone"),
        RENT ("Rent"),
        TRAVEL ("Travel"),
        OTHER ("Other");

        private final String description;

        Category(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

    }

        // Provides constants for the names of who performed the purchase
    public enum Buyer {
        LEFT("Johan"),
        RIGHT("Jana");

        private final String name;

        Buyer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }
}


