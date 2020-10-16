package DataLoader;

import org.postgresql.ds.PGSimpleDataSource;

import java.sql.*;

// TODO: Remove unwanted columns

public class CreateTables {

    private static PGSimpleDataSource ds;

    public CreateTables(PGSimpleDataSource datasource){
        ds = datasource;
        // DROP
        dropTables();
        // CREATE
        createWarehouse();
        createDistrict();
        createCustomer();
        createOrder();
        createItem();
        createOrderLine();
        createStock();
    }

    // Need to drop tables with FK dependencies first
    public void dropTables(){
        runSQL("DROP TABLE IF EXISTS Customer");
        runSQL("DROP TABLE IF EXISTS Order_New");
        runSQL("DROP TABLE IF EXISTS OrderLine");
        runSQL("DROP TABLE IF EXISTS District");
        runSQL("DROP TABLE IF EXISTS Stock");
        runSQL("DROP TABLE IF EXISTS Warehouse");
        runSQL("DROP TABLE IF EXISTS Item");

    }

    public void createWarehouse(){
        runSQL("CREATE TABLE IF NOT EXISTS Warehouse (W_ID INT PRIMARY KEY, W_NAME STRING," +
                "W_STREET_1 STRING, W_STREET_2 STRING, W_CITY STRING, W_STATE STRING, W_ZIP STRING, W_TAX DECIMAL," +
                "W_YTD DECIMAL)");
    }

    public void createDistrict() {
        runSQL("CREATE TABLE IF NOT EXISTS District (D_ID INT PRIMARY KEY, D_W_ID INT NOT NULL REFERENCES Warehouse (W_ID), " +
                "D_NAME STRING, D_STREET_1 STRING, D_STREET_2 STRING, D_CITY STRING, D_STATE STRING, D_ZIP STRING," +
                "D_TAX DECIMAL, D_YTD DECIMAL, D_NEXT_O_ID INT)");
    };

    public void createCustomer() {
        runSQL("CREATE TABLE IF NOT EXISTS Customer (C_ID INT PRIMARY KEY, C_W_ID INT NOT NULL REFERENCES Warehouse (W_ID), " +
                "C_D_ID INT NOT NULL REFERENCES District (D_ID), C_FIRST STRING, C_MIDDLE STRING, C_LAST STRING, C_STREET_1 STRING, " +
                "C_STREET_2 STRING, C_CITY STRING, C_STATE STRING, C_ZIP STRING, C_PHONE STRING, C_SINCE TIMESTAMP, C_CREDIT STRING, " +
                "C_CREDIT_LIM DECIMAL, C_DISCOUNT DECIMAL, C_BALANCE DECIMAL, C_YTD_PAYMENT FLOAT, C_PAYMENT_CNT INT, " +
                "C_DELIVERY_CNT INT, C_DATA STRING)");
    };

    public void createOrder(){
        runSQL("CREATE TABLE IF NOT EXISTS Order_New (C_ID INT PRIMARY KEY, C_W_ID INT NOT NULL REFERENCES Warehouse (W_ID), " +
                "C_D_ID INT NOT NULL REFERENCES District (D_ID), C_FIRST STRING, C_MIDDLE STRING, C_LAST STRING, C_STREET_1 STRING, " +
                "C_STREET_2 STRING, C_CITY STRING, C_STATE STRING, C_ZIP STRING, C_PHONE STRING, C_SINCE TIMESTAMP, C_CREDIT STRING, " +
                "C_CREDIT_LIM DECIMAL, C_DISCOUNT DECIMAL, C_BALANCE DECIMAL, C_YTD_PAYMENT FLOAT, C_PAYMENT_CNT INT, " +
                "C_DELIVERY_CNT INT, C_DATA STRING)");
    }

    public void createItem(){
        runSQL("CREATE TABLE IF NOT EXISTS Item (I_ID INT PRIMARY KEY, I_NAME STRING, I_PRICE DECIMAL, " +
                "I_IM_ID INT, I_DATA STRING)");
    }

    public void createOrderLine(){
        runSQL("CREATE TABLE IF NOT EXISTS OrderLine (OL_NUMBER INT PRIMARY KEY, OL_W_ID INT, OL_D_ID INT, " +
                "OL_O_ID INT, OL_I_ID INT, OL_DELIVERY_D TIMESTAMP, OL_AMOUNT DECIMAL, OL_SUPPLY_W_ID INT, OL_QUANTITY DECIMAL, " +
                "OL_DIST_INFO STRING)");
    }

    public void createStock(){
        runSQL("CREATE TABLE IF NOT EXISTS Stock (S_W_ID INT NOT NULL REFERENCES Warehouse (W_ID), S_I_ID INT, S_QUANTITY DECIMAL, " +
                "S_YTD DECIMAL, S_ORDER INTEGER, S_REMOTE INT, S_DIST_01 STRING, S_DIST_02 STRING, S_DIST_03 STRING, S_DIST_04 STRING, " +
                "S_DIST_05 STRING, S_DIST_06 STRING, S_DIST_07 STRING, S_DIST_08 STRING, S_DIST_09 STRING, " +
                "S_DIST_10 STRING, S_DATA STRING)");
    }

    public static void runSQL(String sqlCode){
        System.out.println(sqlCode);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(sqlCode);
            pstmt.execute();
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}