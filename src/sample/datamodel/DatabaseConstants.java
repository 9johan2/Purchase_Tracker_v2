package sample.datamodel;

public interface DatabaseConstants {

    /**
     * This interface acts as a placeholder for all
     * database constants used by Datasource.java.
      */

    String DB_NAME = "data.db";
    String CONNECTION_STRING = "jdbc:sqlite:C:\\Java Programs\\My Programs\\Economy_v2\\src\\sample\\datamodel\\" + DB_NAME;

        // Table and column names
    String TABLE = "items";
    String COLUMN_ID = "id";
    String COLUMN_NAME = "name";
    String COLUMN_CATEGORY = "category";
    String COLUMN_PRICE = "price";
    String COLUMN_DATE = "date";
    String COLUMN_BUYER = "buyer";

        // Prepared statements
    String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS \"" + TABLE + "\" (\"" + COLUMN_ID + "\" INTEGER, \"" + COLUMN_NAME + "\" TEXT, \"" +
            COLUMN_CATEGORY + "\" TEXT, \"" + COLUMN_PRICE + "\" TEXT, \"" + COLUMN_DATE + "\" INTEGER, \"" + COLUMN_BUYER + "\" TEXT, PRIMARY KEY(\"" +
                COLUMN_ID + "\") )";

    String ORDER_BY_DATE = " ORDER BY " + COLUMN_DATE;

    String QUERY_ALL = "SELECT * FROM " + TABLE + " WHERE " + COLUMN_BUYER + " = ?" + ORDER_BY_DATE;

    String QUERY_ALL_BY_DATE = "SELECT * FROM " + TABLE + " WHERE " + COLUMN_BUYER + " = ? AND " +
            COLUMN_DATE + " >= ? AND " + COLUMN_DATE + " <= ?" + ORDER_BY_DATE;

    String QUERY_CATEGORY = "SELECT * FROM " + TABLE + " WHERE " + COLUMN_BUYER + " = ? AND " + COLUMN_CATEGORY + " = ?" + ORDER_BY_DATE;

    String QUERY_CATEGORY_BY_DATE = "SELECT * FROM " + TABLE + " WHERE " + COLUMN_BUYER + " = ? AND " +
            COLUMN_CATEGORY + " = ? AND " + COLUMN_DATE + " >= ? AND " + COLUMN_DATE + " <= ?" + ORDER_BY_DATE;

        // INSERT INTO items (name, category, price, date, buyer) VALUES("Kalle", "Other", "22.65", "2021-07-25", "Johan")
    String INSERT_ITEM = "INSERT INTO " + TABLE + " (" + COLUMN_NAME + ", " + COLUMN_CATEGORY + ", " + COLUMN_PRICE + ", " +
                COLUMN_DATE + ", " + COLUMN_BUYER + ") VALUES(?, ? ,? ,? , ?)";

    String DELETE_ITEM = "DELETE FROM " + TABLE + " WHERE " + COLUMN_ID + " = ?";
}
