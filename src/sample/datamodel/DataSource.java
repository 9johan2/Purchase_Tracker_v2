package sample.datamodel;

import javafx.stage.FileChooser;
import sample.Main;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataSource implements DatabaseConstants{

    private final static DataSource instance = new DataSource();

    private final FileChooser fileChooser = new FileChooser();
    private File file = null;

    private Connection con;
    private PreparedStatement createTable;
    private PreparedStatement queryAll;
    private PreparedStatement queryAllDate;
    private PreparedStatement queryByCategory;
    private PreparedStatement queryByCategoryDate;
    private PreparedStatement insertItem;
    private PreparedStatement deleteItem;

    private DataSource() {
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text file","*.txt"));

    }
    public static DataSource getInstance() {
        return instance;
    }

    public void open() {
        try {
            con = DriverManager.getConnection(CONNECTION_STRING);

            createTable = con.prepareStatement(CREATE_TABLE);
            queryAll = con.prepareStatement(QUERY_ALL);
            queryAllDate = con.prepareStatement(QUERY_ALL_BY_DATE);
            queryByCategory = con.prepareStatement(QUERY_CATEGORY);
            queryByCategoryDate = con.prepareStatement(QUERY_CATEGORY_BY_DATE);
            insertItem = con.prepareStatement(INSERT_ITEM);
            deleteItem = con.prepareStatement(DELETE_ITEM);

        } catch (SQLException e) {
            System.out.println("Could not open database: " + e.getMessage());
        }
    }

    public void close() {
        try {
            closeStatement(createTable);
            closeStatement(queryAll);
            closeStatement(queryAllDate);
            closeStatement(queryByCategory);
            closeStatement(queryByCategoryDate);
            closeStatement(insertItem);
            closeStatement(deleteItem);

            if (con != null) {
                con.close();
            }

        }catch (SQLException e) {
            System.out.println("Could not close connection: " + e.getMessage());
        }

    }
    private void closeStatement(PreparedStatement statement) throws SQLException{
        if (statement != null) {
            statement.close();
        }
    }

    public List<Item> queryAll() {

        try {
            ResultSet resultSet = queryAll.executeQuery();
            return createListFromResult(resultSet);

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }

    }

            // Could optimize synchronization to improve performance.
    public synchronized List<Item> filteredQuery(String buyer, int from, int to, String category) {
        try {
            ResultSet resultSet;

            if (category.equals("No filter")) {
                queryAllDate.setString(1, buyer);
                queryAllDate.setString(2, Integer.toString(from));
                queryAllDate.setString(3, Integer.toString(to));

                resultSet = queryAllDate.executeQuery();

            } else {
                queryByCategoryDate.setString(1, buyer);
                queryByCategoryDate.setString(2, category);
                queryByCategoryDate.setString(3, Integer.toString(from));
                queryByCategoryDate.setString(4, Integer.toString(to));
                resultSet = queryByCategoryDate.executeQuery();
            }
            
            return createListFromResult(resultSet);

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    private List<Item> createListFromResult(ResultSet resultSet) throws SQLException{
        List<Item> items = new ArrayList<>();

        while(resultSet.next()) {
            Item item = new Item();
            item.setId(resultSet.getInt(1));
            item.setName(resultSet.getString(2));
            item.setCategory(resultSet.getString(3));
            item.setPrice(Double.parseDouble(resultSet.getString(4)));

            String tempDate = resultSet.getString(5);
            int year = Integer.parseInt(tempDate.substring(0,4));
            int month = Integer.parseInt(tempDate.substring(5,6));
            int day = Integer.parseInt(tempDate.substring(6,8));
            item.setDateOfPurchase(LocalDate.of(year,month,day));

            item.setBuyer(resultSet.getString(6));

            items.add(item);
        }

        return items;
    }

    public void insertItem(Item item) throws SQLException {

        insertItem.setString(1, item.getName());
        insertItem.setString(2, item.getCategory());
        insertItem.setString(3, Double.toString(item.getPrice()));
        insertItem.setString(4, item.getDateOfPurchase().toString().replaceAll("-",""));
        insertItem.setString(5, item.getBuyer());

        int affectedRows = insertItem.executeUpdate();

        if (affectedRows != 1) {
            throw new SQLException("Couldn't insert item");
        }
    }

    public boolean deleteItem(int id){
        try {
            con.setAutoCommit(false);
            deleteItem.setString(1, Integer.toString(id));

            int affectedRows = deleteItem.executeUpdate();
            if (affectedRows == 1) {
                con.commit();
                return true;
            } else {
                throw new SQLException();
            }

        } catch (Exception e) {
            try {
                con.rollback();
                return false;
            } catch (SQLException e1) {
                System.out.println("Couldn't revert changes: " + e1.getMessage());
                return false;
            }
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Couldn't reset auto commit: " + e.getMessage());
            }
        }
    }

    public void exportData() throws IOException {
        List<Item> list = queryAll();
        file = fileChooser.showSaveDialog(Main.getStage());
        if (file != null) {

            try(FileWriter fileWriter = new FileWriter(file)) {
                for (Item item: list) {
                    fileWriter.write( item.getId() + "," + item.getName() +","+item.getCategory() +"," +
                            item.getPrice() + "," + item.getDateOfPurchase() + "," + item.getBuyer() + "\n");
                }

            } catch (FileNotFoundException e) {
                throw new FileNotFoundException();
            } catch (IOException e) {
                throw new IOException();
            }
        }
    }

    public void importData() throws Exception{
        file = fileChooser.showOpenDialog(Main.getStage());
        if (file != null) {

            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (true) {
                    String data = br.readLine();
                    if (data == null) {
                        break;
                    }
                    String[] array = data.split(",");

                    Item item = new Item();
                    item.setId(Integer.parseInt(array[0]));
                    item.setName(array[1]);
                    item.setCategory(array[2]);
                    item.setPrice(Double.parseDouble(array[3]));

                    String tempDate = array[4];
                    int year = Integer.parseInt(array[4].substring(0,4));
                    int month = Integer.parseInt(array[4].substring(6,7));
                    int day = Integer.parseInt(array[4].substring(8,10));
                    item.setDateOfPurchase(LocalDate.of(year,month,day));
                    item.setBuyer(array[5]);

                    insertItem(item);
                }


            } catch (FileNotFoundException e) {
                throw new FileNotFoundException();
            } catch (IOException e) {
                throw new IOException();
            }
        }
    }
}
