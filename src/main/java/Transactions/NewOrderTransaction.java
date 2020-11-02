package Transactions;

import org.postgresql.ds.PGSimpleDataSource;
import util.TimeHelper;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class NewOrderTransaction extends BaseTransaction{
    private int customerWarehouseId;
    private int customerDistrictId;
    private int customerId;
    private int numOfItems;
    private DataSource ds;

    private List<Integer> itemNumbers;
    private List<Integer> supplierWarehouses;
    private List<BigDecimal> quantities;

    public NewOrderTransaction(PGSimpleDataSource datasource) {
        super(datasource);
        ds = datasource;
    }

    public void parseInput(Scanner sc, String inputLine) {
        // Payment expects format of N,C_ID,W_ID,D_ID,M and has more M lines
        String[] input = inputLine.split(",");
        assert(input[0].equals("N"));
        customerId = Integer.parseInt(input[1]);
        customerWarehouseId = Integer.parseInt(input[2]);
        customerDistrictId = Integer.parseInt(input[3]);
        numOfItems = Integer.parseInt(input[4]);

        itemNumbers = new ArrayList<Integer>();
        supplierWarehouses = new ArrayList<Integer>();
        quantities = new ArrayList<BigDecimal>();
        for(int i = 0; i < numOfItems; i++) {
            String nextLine = sc.nextLine();
            String[] itemInput =  nextLine.split(",");
            itemNumbers.add(Integer.parseInt(itemInput[0]));
            supplierWarehouses.add(Integer.parseInt(itemInput[1]));
            quantities.add(DatatypeConverter.parseDecimal(itemInput[2]));
        }
    }

    public void execute(){

        System.out.println("Start New Order...");
        try(Connection connection = ds.getConnection()){

            List<String> itemNames = new ArrayList<String>();
            List<BigDecimal> orderLineAmounts = new ArrayList<BigDecimal>();
            List<BigDecimal> adjustedQuantities = new ArrayList<BigDecimal>();

            // (1) Obtain D_NEXT_O_ID
            PreparedStatement q1 = connection.prepareStatement("SELECT * from District where D_ID = ? AND D_W_ID = ?");
            q1.setInt(1, customerDistrictId);
            q1.setInt(2, customerWarehouseId);
            q1.execute();
            ResultSet r1 = q1.getResultSet();
            String[] districtInfo = new FormResults().formResults(r1).get(0).split(",");
            int orderNumber = Integer.parseInt(districtInfo[10]);


            // (2) Update D_NEXT_O_ID
            PreparedStatement q2 = connection.prepareStatement("UPDATE District SET D_NEXT_O_ID = ? where D_ID = ? AND D_W_ID = ?");
            q2.setInt(1, orderNumber+ 1);
            q2.setInt(2, customerDistrictId);
            q2.setInt(3, customerWarehouseId);
            q2.execute();

            // (3) Iterate through all Items
            BigDecimal totalAmount = new BigDecimal(0);
            BigDecimal isAllLocal = BigDecimal.ONE;

            for(int i = 0; i < numOfItems; i++) {
                int itemNumber = itemNumbers.get(i);
                int supplierWarehouse = supplierWarehouses.get(i);
                BigDecimal quantity = quantities.get(i);

                // 2.1 get updated quantity and update stock
                int sDistrictNum = customerDistrictId;
                PreparedStatement q3 = connection.prepareStatement("SELECT * from Stock where S_I_ID = ? AND S_W_ID = ?");
                q3.setInt(1, itemNumber);
                q3.setInt(2, supplierWarehouse);
                q3.execute();
                ResultSet r3 = q3.getResultSet();
                String [] stockInfo = new FormResults().formResults(r3).get(0).split(",");

                BigDecimal stockQuantity = DatatypeConverter.parseDecimal(stockInfo[2]);
                BigDecimal adjustedQuantity = stockQuantity.subtract(quantity);

                if (adjustedQuantity.compareTo(new BigDecimal(10)) < 0) {
                    adjustedQuantity = adjustedQuantity.add(new BigDecimal(100));
                }
                adjustedQuantities.add(adjustedQuantity);

                BigDecimal stockYtd = DatatypeConverter.parseDecimal(stockInfo[3]);
                int orderCount = Integer.parseInt(stockInfo[4]);
                int remoteCount = Integer.parseInt(stockInfo[5]);

                int newRemoteCount = remoteCount;
                if (supplierWarehouse != customerWarehouseId) {
                    isAllLocal = BigDecimal.ZERO;
                    newRemoteCount += 1;
                }

                PreparedStatement q4 = connection.prepareStatement("UPDATE Stock SET S_QUANTITY = ?, S_YTD = ?, " +
                            "S_ORDER_CNT = ?, S_REMOTE_CNT = ? where S_W_ID = ? AND S_I_ID = ?");
                q4.setBigDecimal(1, adjustedQuantity);
                q4.setBigDecimal(2, stockYtd.add(BigDecimal.ONE));
                q4.setInt(3, orderCount + 1);
                q4.setInt(4, newRemoteCount);
                q4.setInt(5, supplierWarehouse);
                q4.setInt(6, itemNumber);
                q4.execute();

                PreparedStatement q5 = connection.prepareStatement("SELECT * from Item where I_ID = ?");
                q5.setInt(1, itemNumber);
                q5.execute();
                ResultSet r5 = q5.getResultSet();
                String[] itemInfo = new FormResults().formResults(r5).get(0).split(",");

                BigDecimal itemPrice = DatatypeConverter.parseDecimal(itemInfo[2]);
                BigDecimal itemAmount = itemPrice.multiply(quantity);
                totalAmount = totalAmount.add(itemAmount);
                orderLineAmounts.add(itemAmount);
                String itemName = itemInfo[1];
                itemNames.add(itemName);

                // 2.2 create new orderline
                // add I_NAME from customer too
                String sDistInfo = stockInfo[5+sDistrictNum];
                PreparedStatement q6 = connection.prepareStatement("INSERT INTO OrderLine (OL_NUMBER, OL_W_ID, " +
                        "OL_D_ID, OL_O_ID, OL_I_ID, OL_DELIVERY_D, OL_AMOUNT," +
                        " OL_SUPPLY_W_ID, OL_QUANTITY, OL_DIST_INFO) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                q6.setInt(1, i+1);
                q6.setInt(2, customerWarehouseId);
                q6.setInt(3, customerDistrictId);
                q6.setInt(4, orderNumber);
                q6.setInt(5, itemNumber);
                q6.setTimestamp(6, null);
                q6.setBigDecimal(7, itemAmount);
                q6.setInt(8, supplierWarehouse);
                q6.setBigDecimal(9, quantity);
                q6.setString(10, sDistInfo);
                q6.execute();
            }

            // Get Warehouse tax
            PreparedStatement q7 = connection.prepareStatement("SELECT W_TAX from Warehouse where W_ID = ?");
            q7.setInt(1, customerWarehouseId);
            q7.execute();
            ResultSet r7 = q7.getResultSet();
            BigDecimal warehouseTax = DatatypeConverter.parseDecimal(new FormResults().formResults(r7).get(0));

            //3. compute total amount
            BigDecimal districtTax = DatatypeConverter.parseDecimal(districtInfo[8]);
            PreparedStatement q8 = connection.prepareStatement("SELECT * from Customer where C_ID = ? AND C_W_ID = ? AND C_D_ID = ?");
            q8.setInt(1, customerId);
            q8.setInt(2, customerWarehouseId);
            q8.setInt(3, customerDistrictId);
            q8.execute();
            ResultSet r8 = q8.getResultSet();
            ArrayList<String> cus = new FormResults().formResults(r8);
            String [] customerInfo = cus.get(0).split(",");
            BigDecimal customerDiscount = DatatypeConverter.parseDecimal(customerInfo[15]);
            BigDecimal totalTaxes = BigDecimal.ONE.add(districtTax.add(warehouseTax));
            totalAmount = totalAmount.multiply(totalTaxes.multiply(BigDecimal.ONE.subtract(customerDiscount)));
            String customerLastName = customerInfo[5];

            //4. Create Order
            // brought to the end after creating all order lines to avoid an extra iteration for checking if warehouses are local
            Date entryDate = new Date();
            String entryDateStr = TimeHelper.formatDate(entryDate);
            PreparedStatement q9 = connection.prepareStatement("INSERT INTO Order_New (O_ID, O_W_ID, " +
                    "O_D_ID, O_C_ID, O_CARRIER_ID, O_OL_CNT, O_ALL_LOCAL, O_ENTRY_D)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            q9.setInt(1, orderNumber);
            q9.setInt(2, customerWarehouseId);
            q9.setInt(3, customerDistrictId);
            q9.setInt(4, customerId);
            q9.setObject(5, null);
            q9.setBigDecimal(6, new BigDecimal(numOfItems));
            q9.setBigDecimal(7, isAllLocal);
            q9.setTimestamp(8, Timestamp.valueOf(entryDateStr));
            q9.execute();

            //4. Print output
            System.out.println(String.format(
                    "1. Customer: (%d, %d, %d), C_LAST: %s, C_CREDIT: %s, C_DISCOUNT: %.4f",
                    customerWarehouseId, customerDistrictId, customerId,
                    customerLastName, customerInfo[13], customerDiscount));
            System.out.println(String.format("2. W_TAX: %.4f, D_TAX: %.4f", warehouseTax, districtTax));
            System.out.println(String.format("3. O_ID: %d, O_ENTRY_D: %s", orderNumber, entryDateStr));
            System.out.println(String.format("4. NUM_ITEMS: %d, TOTAL_AMOUNT: %.2f", numOfItems, totalAmount));
            System.out.println("5. Each item:");

            for(int i = 0; i < numOfItems; i++) {
                System.out.println(String.format(
                        "\t ITEM_NUMBER: %d, I_NAME: %s, SUPPLIER_WAREHOUSE: %d, QUANTITY: %.0f, OL_AMOUNT: %.2f, S_QUANTITY: %.0f",
                        itemNumbers.get(i), itemNames.get(i), supplierWarehouses.get(i), quantities.get(i), orderLineAmounts.get(i), adjustedQuantities.get(i)));
            }

            System.out.println("Finish New Order...");


        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
