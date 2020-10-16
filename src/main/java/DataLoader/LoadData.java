package DataLoader;

import org.postgresql.ds.PGSimpleDataSource;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

// TODO: Remove unwanted columns

public class LoadData {

    private static PGSimpleDataSource ds;
    private static final String DIRECTORY = "src/main/java/DataSource/data-files/";
    private static int i = 0;
    private static final int limit = -1;

    public LoadData(PGSimpleDataSource datasource){
        ds = datasource;
    }

    // Do not change the ordering
    public void loadAllData() throws Exception{
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
        File file = new File(DIRECTORY + "warehouse.csv");
        BufferedReader br = new BufferedReader(new FileReader(file));

        // create parameterized INSERT statement
        try (Connection connection = ds.getConnection()){
            PreparedStatement insert = connection.prepareStatement("INSERT INTO Warehouse(W_ID, W_NAME, " +
                    "W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP," +
                    " W_TAX, W_YTD) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

            String line;
            while ((line = br.readLine()) != null) {
                i++;
                if (i == limit) {
                    i = 0;
                    break;
                }

                String[] row = line.split(",");

                insert.setInt(1, Integer.parseInt(row[0]));
                insert.setString(2, row[1]);
                insert.setString(3, row[2]);
                insert.setString(4, row[3]);
                insert.setString(5, row[4]);
                insert.setString(6, row[5]);
                insert.setString(7, row[6]);
                insert.setBigDecimal(8, DatatypeConverter.parseDecimal(row[7]));
                insert.setBigDecimal(9, DatatypeConverter.parseDecimal(row[8]));

                insert.execute();
            }
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }

    public void loadDistrict() throws IOException {
        File file = new File(DIRECTORY + "district.csv");
        BufferedReader br = new BufferedReader(new FileReader(file));

        // create parameterized INSERT statement
        try (Connection connection = ds.getConnection()){
            PreparedStatement insert = connection.prepareStatement("INSERT INTO District(D_ID, D_W_ID, " +
                    "D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE," +
                    " D_ZIP, D_TAX, D_YTD, D_NEXT_O_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            String line;
            while ((line = br.readLine()) != null) {
                i++;
                if (i == limit) {
                    i = 0;
                    break;
                }

                String[] row = line.split(",");

                insert.setInt(1, Integer.parseInt(row[0]));
                insert.setInt(2, Integer.parseInt(row[1]));
                insert.setString(3, row[2]);
                insert.setString(4, row[3]);
                insert.setString(5, row[4]);
                insert.setString(6, row[5]);
                insert.setString(7, row[6]);
                insert.setString(8, row[7]);
                insert.setBigDecimal(9, DatatypeConverter.parseDecimal(row[8]));
                insert.setBigDecimal(10, DatatypeConverter.parseDecimal(row[9]));
                insert.setInt(11, Integer.parseInt(row[10]));

                insert.execute();
            }
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }

    public void loadCustomer() throws IOException {
        File file = new File(DIRECTORY + "customer.csv");
        BufferedReader br = new BufferedReader(new FileReader(file));

        // create parameterized INSERT statement
        try (Connection connection = ds.getConnection()){
            PreparedStatement insert = connection.prepareStatement("INSERT INTO Customer (C_ID, C_W_ID, " +
                    "C_D_ID, C_FIRST, C_MIDDLE, C_LAST, C_STREET_1, " +
                    "C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT, C_CREDIT_LIM, C_DISCOUNT, C_BALANCE, C_YTD_PAYMENT, C_PAYMENT_CNT, " +
                    "C_DELIVERY_CNT, C_DATA) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            String line;
            while ((line = br.readLine()) != null) {
                i++;
                if (i == limit) {
                    i = 0;
                    break;
                }
                String[] row = line.split(",");

                insert.setInt(1, Integer.parseInt(row[0]));
                insert.setInt(2, Integer.parseInt(row[1]));
                insert.setInt(3, Integer.parseInt(row[2]));
                insert.setString(4, row[3]);
                insert.setString(5, row[4]);
                insert.setString(6, row[5]);
                insert.setString(7, row[6]);
                insert.setString(8, row[7]);
                insert.setString(9, row[8]);
                insert.setString(10, row[9]);
                insert.setString(11, row[10]);
                insert.setString(12, row[11]);
                insert.setTimestamp(13, Timestamp.valueOf(row[12]));
                insert.setString(14, row[13]);
                insert.setBigDecimal(15, DatatypeConverter.parseDecimal(row[14]));
                insert.setBigDecimal(16, DatatypeConverter.parseDecimal(row[15]));
                insert.setBigDecimal(17, DatatypeConverter.parseDecimal(row[16]));
                insert.setFloat(18, Float.parseFloat(row[17]));
                insert.setInt(19, Integer.parseInt(row[18]));
                insert.setInt(20, Integer.parseInt(row[19]));
                insert.setString(21, row[20]);

                insert.execute();
            }
        } catch (SQLException e) {



            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }

    public void loadOrder() throws IOException {
        File file = new File(DIRECTORY + "order.csv");
        BufferedReader br = new BufferedReader(new FileReader(file));

        // create parameterized INSERT statement
        try (Connection connection = ds.getConnection()){

            PreparedStatement insert = connection.prepareStatement("INSERT INTO Order_New (O_ID, O_W_ID, " +
                    "O_D_ID, O_C_ID, O_CARRIER_ID, O_OL_CNT, O_ALL_LOCAL, O_ENTRY_D)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            String line;
            while ((line = br.readLine()) != null) {
                i++;
                if (i == limit) {
                    i = 0;
                    break;
                }
                String[] row = line.split(",");

                insert.setInt(1, Integer.parseInt(row[0]));
                insert.setInt(2, Integer.parseInt(row[1]));
                insert.setInt(3, Integer.parseInt(row[2]));
                insert.setInt(4, Integer.parseInt(row[3]));
                insert.setInt(5, Integer.parseInt(row[4]));
                insert.setBigDecimal(6, DatatypeConverter.parseDecimal(row[5]));
                insert.setBigDecimal(7, DatatypeConverter.parseDecimal(row[6]));
                insert.setTimestamp(8, Timestamp.valueOf(row[7]));

                insert.execute();
            }
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }

    public void loadItem() throws IOException {
        File file = new File(DIRECTORY + "item.csv");
        BufferedReader br = new BufferedReader(new FileReader(file));

        // create parameterized INSERT statement
        try (Connection connection = ds.getConnection()){

            PreparedStatement insert = connection.prepareStatement("INSERT INTO Item(I_ID, I_NAME, " +
                    "I_PRICE, I_IM_ID, I_DATA) VALUES (?, ?, ?, ?, ?)");

            String line;
            while ((line = br.readLine()) != null) {
                i++;
                if (i == limit) {
                    i = 0;
                    break;
                }

                String[] row = line.split(",");

                insert.setInt(1, Integer.parseInt(row[0]));
                insert.setString(2, row[1]);
                insert.setBigDecimal(3, DatatypeConverter.parseDecimal(row[2]));
                insert.setInt(4, Integer.parseInt(row[3]));
                insert.setString(5, row[4]);

                insert.execute();
            }
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }

    public void loadStock() throws IOException {
        File file = new File(DIRECTORY + "stock.csv");
        BufferedReader br = new BufferedReader(new FileReader(file));

        // create parameterized INSERT statement
        try (Connection connection = ds.getConnection()){

            PreparedStatement insert = connection.prepareStatement("INSERT INTO Stock (S_W_ID, S_I_ID, " +
                    "S_QUANTITY, S_YTD, S_ORDER, S_REMOTE, S_DIST_01," +
                    " S_DIST_02, S_DIST_03, S_DIST_04, S_DIST_05, S_DIST_06, S_DIST_07, S_DIST_08, S_DIST_09, S_DIST_10, S_DATA) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            String line;
            while ((line = br.readLine()) != null) {
                i++;
                if (i == limit) {
                    i = 0;
                    break;
                }
                String[] row = line.split(",");

                insert.setInt(1, Integer.parseInt(row[0]));
                insert.setInt(2, Integer.parseInt(row[1]));
                insert.setBigDecimal(3, DatatypeConverter.parseDecimal(row[2]));
                insert.setBigDecimal(4, DatatypeConverter.parseDecimal(row[3]));
                insert.setInt(5, Integer.parseInt(row[4]));
                insert.setInt(6, Integer.parseInt(row[5]));
                insert.setString(7, row[6]);
                insert.setString(8, row[7]);
                insert.setString(9, row[8]);
                insert.setString(10, row[9]);
                insert.setString(11, row[10]);
                insert.setString(12, row[11]);
                insert.setString(13, row[12]);
                insert.setString(14, row[13]);
                insert.setString(15, row[14]);
                insert.setString(16, row[15]);
                insert.setString(17, row[16]);

                insert.execute();
            }
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }

    public void loadOrderLine() throws IOException {
        File file = new File(DIRECTORY + "order-line.csv");
        BufferedReader br = new BufferedReader(new FileReader(file));

        // create parameterized INSERT statement
        try (Connection connection = ds.getConnection()){

            PreparedStatement insert = connection.prepareStatement("INSERT INTO OrderLine (OL_NUMBER, OL_W_ID, " +
                    "OL_D_ID, OL_O_ID, OL_I_ID, OL_DELIVERY_D, OL_AMOUNT," +
                    " OL_SUPPLY_W_ID, OL_QUANTITY, OL_DIST_INFO) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            String line;
            while ((line = br.readLine()) != null) {
                i++;
                if (i == limit) {
                    i = 0;
                    break;
                }
                String[] row = line.split(",");

                insert.setInt(1, Integer.parseInt(row[0]));
                insert.setInt(2, Integer.parseInt(row[1]));
                insert.setInt(3, Integer.parseInt(row[2]));
                insert.setInt(4, Integer.parseInt(row[3]));
                insert.setInt(5, Integer.parseInt(row[4]));
                insert.setTimestamp(6, Timestamp.valueOf(row[5]));
                insert.setBigDecimal(7, DatatypeConverter.parseDecimal(row[6]));
                insert.setInt(8, Integer.parseInt(row[7]));
                insert.setBigDecimal(9, DatatypeConverter.parseDecimal(row[8]));
                insert.setString(10, row[9]);

                insert.execute();
            }
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}