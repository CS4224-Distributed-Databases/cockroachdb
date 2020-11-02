package Transactions;

import org.postgresql.ds.PGSimpleDataSource;
import util.TimeHelper;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class OrderStatusTransaction extends BaseTransaction{
    private int customerWarehouseId;
    private int customerDistrictId;
    private int customerId;
    private int numOfItems;
    private DataSource ds;

    public OrderStatusTransaction(PGSimpleDataSource datasource) {
        super(datasource);
        ds = datasource;
    }

    public void parseInput(Scanner sc, String inputLine) {
        // Payment expects format of O,C W ID,C D ID,C ID.
        String[] input = inputLine.split(",");
        assert(input[0].equals("O"));
        customerWarehouseId = Integer.parseInt(input[1]);
        customerDistrictId = Integer.parseInt(input[2]);
        customerId = Integer.parseInt(input[3]);
    }

    public void execute(){

        System.out.println("Start Order Status...");
        try(Connection connection = ds.getConnection()){

            // Print out customer's details
            PreparedStatement q1 = connection.prepareStatement("SELECT C_FIRST, C_MIDDLE, C_LAST, C_BALANCE from Customer WHERE C_ID = ? AND C_W_ID = ? AND C_D_ID = ?");
            q1.setInt(1, customerId);
            q1.setInt(2, customerWarehouseId);
            q1.setInt(3, customerDistrictId);
            q1.execute();
            ResultSet r1 = q1.getResultSet();
            String [] customerInfo = new FormResults().formResults(r1).get(0).split(",");

            String customerFirstName = customerInfo[0];
            String customerMiddleName = customerInfo[1];
            String customerLastName = customerInfo[2];
            String customerBalance =  customerInfo[3];

            System.out.printf("Customer name: %s %s %s, balance: %f\n",
                    customerFirstName,
                    customerMiddleName,
                    customerLastName,
                    customerBalance);


            // Print out customer's last order X details
            PreparedStatement q2 = connection.prepareStatement("SELECT O_ID, O_ENTRY_D, O_CARRIER_ID from Order WHERE O_C_ID = ? ORDER BY O_ID DESC LIMIT 1");
            q2.setInt(1, customerId);
            q2.execute();
            ResultSet r2 = q2.getResultSet();
            String [] lastOrder = new FormResults().formResults(r2).get(0).split(",");
            String lastOrderID = lastOrder[0];
            String carrierID = lastOrder[1];
            String entryDate = lastOrder[2];
            System.out.printf("Customer's last order ID: %s, entry time: %s, carrier ID: %s\n",
                    lastOrderID,
                    entryDate,
                    carrierID);

            // Print out all the details of the items in the last order x
            PreparedStatement q3 = connection.prepareStatement("SELECT OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_DELIVERY_D from OrderLine WHERE OL_O_ID = ?");
            q3.setInt(1, Integer.parseInt(lastOrderID));
            q3.execute();
            ResultSet r3 = q3.getResultSet();
            ArrayList<String> lastOrderLine = new FormResults().formResults(r3);
            for (String orderLine: lastOrderLine) {
                String [] ol = orderLine.split(",");
                String itemID = ol[0];
                String supplyWarehouse = ol[1];
                String quantity = ol[2];
                String amount = ol[3];
                String deliveryDate = ol[4];
                System.out.printf("Order line in last order item ID: %s, supply warehouse ID: %s, "
                                + "quantity: %s, price: %s, delivery date: %s\n",
                        itemID,
                        supplyWarehouse,
                        quantity,
                        amount,
                        deliveryDate);
            }

            System.out.println("Finish Order Status...");

        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
