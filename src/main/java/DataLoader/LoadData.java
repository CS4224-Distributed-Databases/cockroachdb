package DataLoader;

import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Note that even though we define to load into node 2, it might also load into other nodes as well
public class LoadData {

    private static PGSimpleDataSource ds;

    public LoadData(PGSimpleDataSource datasource) {
        ds = datasource;
    }

    // Do not change the ordering
    public void loadAllData() throws Exception {
        loadWarehouse();
        System.out.println("Warehouse load data");
        loadDistrict();
        System.out.println("District load data");
        loadCustomer();
        System.out.println("Customer load data");
        loadOrder();
        System.out.println("Order_New load data");
        loadItem();
        System.out.println("Item load data");
        loadStock();
        System.out.println("Stock load data");
        loadOrderLine();
        System.out.println("OrderLine load data");
    }

    public void loadWarehouse() throws IOException {
        runSQL("IMPORT INTO Warehouse (W_ID, W_NAME, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP, W_TAX, W_YTD) " +
                "CSV DATA (\"nodelocal://2/data-files/warehouse.csv\");");
    }

    public void loadDistrict() throws IOException {
        runSQL("IMPORT INTO District (D_W_ID, D_ID, D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP, " +
                "D_TAX, D_YTD, D_NEXT_O_ID) " +
                "CSV DATA (\"nodelocal://2/data-files/district.csv\");");
    }

    public void loadCustomer() throws IOException {
        runSQL("IMPORT INTO Customer (C_W_ID, C_D_ID, C_ID, C_FIRST, C_MIDDLE, C_LAST, C_STREET_1, C_STREET_2, " +
                "C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT, C_CREDIT_LIM, C_DISCOUNT, C_BALANCE, " +
                "C_YTD_PAYMENT, C_PAYMENT_CNT, C_DELIVERY_CNT, C_DATA) " +
                "CSV DATA (\"nodelocal://2/data-files/customer.csv\");");
    }

    public void loadOrder() throws IOException {
        runSQL("IMPORT INTO Order_New (O_W_ID, O_D_ID, O_ID, O_C_ID, O_CARRIER_ID, O_OL_CNT, O_ALL_LOCAL, O_ENTRY_D) " +
                "CSV DATA (\"nodelocal://2/data-files/order.csv\") WITH nullif = 'null';");
    }

    public void loadItem() throws IOException {
        runSQL("IMPORT INTO Item (I_ID, I_NAME, I_PRICE, I_IM_ID, I_DATA) " +
                "CSV DATA (\"nodelocal://2/data-files/item.csv\")");
    }

    public void loadStock() throws IOException {
        runSQL("IMPORT INTO Stock (S_W_ID, S_I_ID, S_QUANTITY, S_YTD, S_ORDER_CNT, S_REMOTE_CNT, " +
                "S_DIST_01, S_DIST_02, S_DIST_03, S_DIST_04, S_DIST_05, S_DIST_06, S_DIST_07, S_DIST_08, S_DIST_09, " +
                "S_DIST_10, S_DATA) " +
                "CSV DATA (\"nodelocal://2/data-files/stock.csv\");");
    }

    public void loadOrderLine() throws IOException {
        runSQL("IMPORT INTO OrderLine (OL_W_ID, OL_D_ID, OL_O_ID, OL_NUMBER, OL_I_ID, OL_DELIVERY_D, " +
                "OL_AMOUNT, OL_SUPPLY_W_ID, OL_QUANTITY, OL_DIST_INFO) " +
                "CSV DATA (\"nodelocal://2/data-files/order-line.csv\") WITH nullif = 'null';");
    }

    public static void runSQL(String sqlCode) {
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