package Transactions;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import static util.TimeHelper.formatDate;

public class DeliveryTransaction extends BaseTransaction{

    private DataSource ds;

    private static final int NUM_DISTRICTS = 10;
    private int warehouseID;
    private int carrierID;

    public DeliveryTransaction(PGSimpleDataSource datasource) {
        super(datasource);
        ds = datasource;
    }

    public void parseInput(Scanner sc, String inputLine) {
        // Payment expects format of D,W_ID,CARRIER_ID
        String[] input = inputLine.split(",");
        assert(input[0].equals("D"));
        this.warehouseID = Integer.parseInt(input[1]);
        this.carrierID = Integer.parseInt(input[2]);
    }

    @Override
    public void execute() {
        System.out.println("Start Delivery...");
        try(Connection connection = ds.getConnection()) {

            PreparedStatement q1 = connection.prepareStatement("SELECT O_ID, O_C_ID from Order_New WHERE O_W_ID = ? AND O_D_ID = ? AND O_CARRIER_ID IS NULL ORDER BY O_ID ASC LIMIT 1");
            PreparedStatement q2 = connection.prepareStatement("UPDATE Order_New SET O_CARRIER_ID = ? WHERE O_ID = ?");
            PreparedStatement q3 = connection.prepareStatement("SELECT OL_NUMBER, OL_AMOUNT from OrderLine WHERE OL_O_ID = ?");
            PreparedStatement q4 = connection.prepareStatement("UPDATE OrderLine SET OL_DELIVERY_D = ? WHERE OL_NUMBER = ?");
            PreparedStatement q5 = connection.prepareStatement("UPDATE Customer SET C_BALANCE = ? AND C_DELIVERY_CNT = C_DELIVERY_CNT + 1 WHERE C_ID = ?");

            for (int i = 1; i <= NUM_DISTRICTS; i++) {
                // (1) Obtain D_NEXT_O_ID
                q1.setInt(1, warehouseID);
                q1.setInt(2, i);
                q1.execute();
                ResultSet r1 = q1.getResultSet();
                String [] info = new FormResults().formResults(r1).get(0).split(",");

                int orderNumber = Integer.parseInt(info[0]);
                int customerNumber = Integer.parseInt(info[1]);

                // (2) Update order X by setting O_CARRIER_ID to CARRIER_ID
                q2.setInt(1, carrierID);
                q2.setInt(2, orderNumber);
                q2.execute();

                // (3) Update all the order lines associated with order X by setting OL DELIVERY D to the current date and time
                q3.setInt(1, orderNumber);
                q3.execute();
                ResultSet r3 = q3.getResultSet();
                ArrayList<String> allOrderLines = new FormResults().formResults(r3);

                BigDecimal orderLineAmount = new BigDecimal(0);
                Date now = new Date();
                Timestamp time = Timestamp.valueOf(formatDate(now));

                for (String orderLine: allOrderLines){
                    // Update DELIVERY_OL_DELIVERY_D to current date and time
                    // OL_DELIVERY_D, OL_NUMBER, OL_W_ID, OL_D_ID, OL_O_ID
                    int orderLineNumber = Integer.parseInt(orderLine.split(",")[0]);
                    BigDecimal amount = DatatypeConverter.parseDecimal(orderLine.split(",")[1]);

                    q4.setTimestamp(1, time);
                    q4.setInt(2, orderLineNumber);
                    // Sum the amount from all orderLines
                    orderLineAmount = orderLineAmount.add(amount);
                }

                // 4: Update balance and delivery count for customer C
                // C_ID, C_W_ID, C_D_ID
                q5.setBigDecimal(1, orderLineAmount);
                q5.setInt(2, customerNumber);
                q5.execute();
            }


            System.out.println("Finish Delivery...");
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
