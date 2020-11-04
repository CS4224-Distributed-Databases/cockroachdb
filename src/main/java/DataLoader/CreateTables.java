package DataLoader;

import org.postgresql.ds.PGSimpleDataSource;

import java.sql.*;

// TODO: Remove unused columns

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

        createViews();
    }

    // Need to drop tables with FK dependencies first
    public void dropTables(){
        runSQL("DROP VIEW IF EXISTS CustomerOrderItemsView");
        runSQL("DROP VIEW IF EXISTS CustomerOrderView");

        runSQL("DROP TABLE IF EXISTS OrderLine");
        runSQL("DROP TABLE IF EXISTS Stock");
        runSQL("DROP TABLE IF EXISTS Item");
        runSQL("DROP TABLE IF EXISTS Order_New");
        runSQL("DROP TABLE IF EXISTS Customer");
        runSQL("DROP TABLE IF EXISTS District");
        runSQL("DROP TABLE IF EXISTS Warehouse");

    }

    public void createWarehouse(){
        runSQL("CREATE TABLE IF NOT EXISTS Warehouse (W_ID INT PRIMARY KEY UNIQUE, W_NAME STRING," +
                "W_STREET_1 STRING, W_STREET_2 STRING, W_CITY STRING, W_STATE STRING, W_ZIP STRING, W_TAX DECIMAL," +
                "W_YTD DECIMAL)");
    }

    public void createDistrict() {
        runSQL("CREATE TABLE IF NOT EXISTS District (D_ID INT NOT NULL, D_W_ID INT NOT NULL REFERENCES Warehouse (W_ID), " +
                "D_NAME STRING, D_STREET_1 STRING, D_STREET_2 STRING, D_CITY STRING, D_STATE STRING, D_ZIP STRING," +
                "D_TAX DECIMAL, D_YTD DECIMAL, D_NEXT_O_ID INT, PRIMARY KEY (D_ID, D_W_ID))");
    };

    public void createCustomer() {
        runSQL("CREATE TABLE IF NOT EXISTS Customer (C_ID INT NOT NULL, C_W_ID INT NOT NULL , " +
                "C_D_ID INT NOT NULL, C_FIRST STRING, C_MIDDLE STRING, C_LAST STRING, C_STREET_1 STRING, " +
                "C_STREET_2 STRING, C_CITY STRING, C_STATE STRING, C_ZIP STRING, C_PHONE STRING, C_SINCE TIMESTAMP, C_CREDIT STRING, " +
                "C_CREDIT_LIM DECIMAL, C_DISCOUNT DECIMAL, C_BALANCE DECIMAL, C_YTD_PAYMENT FLOAT, C_PAYMENT_CNT INT, " +
                "C_DELIVERY_CNT INT, C_DATA STRING, PRIMARY KEY(C_ID, C_W_ID, C_D_ID), FOREIGN KEY(C_D_ID, C_W_ID) REFERENCES District (D_ID, D_W_ID))");
    };

    public void createOrder(){
        runSQL("CREATE TABLE IF NOT EXISTS Order_New (O_ID INT NOT NULL, O_W_ID INT NOT NULL, " +
                "O_D_ID INT NOT NULL, O_C_ID INT NOT NULL, O_CARRIER_ID INT, " +
                "O_OL_CNT DECIMAL, O_ALL_LOCAL DECIMAL, O_ENTRY_D TIMESTAMP, PRIMARY KEY(O_ID, O_W_ID, O_D_ID), " +
                "FOREIGN KEY(O_C_ID, O_W_ID, O_D_ID) REFERENCES Customer (C_ID, C_W_ID, C_D_ID))");
    }

    public void createItem(){
        runSQL("CREATE TABLE IF NOT EXISTS Item (I_ID INT PRIMARY KEY, I_NAME STRING, I_PRICE DECIMAL, " +
                "I_IM_ID INT, I_DATA STRING)");
    }

    public void createOrderLine(){
        runSQL("CREATE TABLE IF NOT EXISTS OrderLine (OL_NUMBER INT NOT NULL, OL_W_ID INT NOT NULL, OL_D_ID INT NOT NULL, " +
                "OL_O_ID INT NOT NULL, OL_I_ID INT, OL_DELIVERY_D TIMESTAMP, OL_AMOUNT DECIMAL, OL_SUPPLY_W_ID INT, OL_QUANTITY DECIMAL, " +
                "OL_DIST_INFO STRING, PRIMARY KEY(OL_NUMBER, OL_W_ID, OL_D_ID, OL_O_ID), FOREIGN KEY(OL_O_ID, OL_W_ID, OL_D_ID) REFERENCES Order_New(O_ID, O_W_ID, O_D_ID))");
    }

    public void createStock(){
        runSQL("CREATE TABLE IF NOT EXISTS Stock (S_W_ID INT NOT NULL REFERENCES Warehouse (W_ID), S_I_ID INT NOT NULL REFERENCES Item (I_ID) , S_QUANTITY DECIMAL, " +
                "S_YTD DECIMAL, S_ORDER_CNT INTEGER, S_REMOTE_CNT INT, S_DIST_01 STRING, S_DIST_02 STRING, S_DIST_03 STRING, S_DIST_04 STRING, " +
                "S_DIST_05 STRING, S_DIST_06 STRING, S_DIST_07 STRING, S_DIST_08 STRING, S_DIST_09 STRING, " +
                "S_DIST_10 STRING, S_DATA STRING, PRIMARY KEY(S_W_ID, S_I_ID))");
    }

    public void createViews() {

        //For Popular Item Transactions (first two)

        // https://stackoverflow.com/questions/30918633/sql-cte-vs-view
        // Views can be indexed but CTE can't => Hence we choose to use View here
        // Note that cannot use * which is not yet implemented in views

        //used by both
        runSQL("CREATE VIEW CustomerOrderView(CO_O_ID, CO_O_ENTRY_D, CO_C_ID, CO_W_ID, CO_D_ID, CO_C_FIRST, CO_C_MIDDLE, CO_C_LAST) " +
                "AS SELECT Order_New.O_ID, Order_New.O_ENTRY_D, Customer.C_ID, Customer.C_W_ID, Customer.C_D_ID, Customer.C_FIRST, Customer.C_MIDDLE, Customer.C_LAST FROM " +
                "Customer JOIN Order_New ON Customer.C_ID = Order_New.O_C_ID");

        //used by both
        runSQL("CREATE VIEW CustomerOrderItemsView(COI_C_ID, COI_W_ID, COI_D_ID, COI_I_ID, COI_O_ID) " +
                "AS SELECT CustomerOrderView.CO_C_ID, CustomerOrderView.CO_W_ID, CustomerOrderView.CO_D_ID, OrderLine.OL_I_ID, OrderLine.OL_O_ID " +
                "FROM CustomerOrderView JOIN OrderLine ON CustomerOrderView.CO_O_ID = OrderLine.OL_O_ID");

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