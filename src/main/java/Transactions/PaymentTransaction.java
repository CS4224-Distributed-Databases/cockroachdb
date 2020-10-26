package Transactions;

import org.postgresql.ds.PGSimpleDataSource;
import util.TimeHelper;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class PaymentTransaction extends BaseTransaction{

    private DataSource ds;

    private int customerWarehouseId;
    private int customerDistrictId;
    private int customerId;
    private BigDecimal payment;

    public PaymentTransaction(PGSimpleDataSource datasource) {
        super(datasource);
        ds = datasource;
    }

    public void parseInput(Scanner sc, String inputLine) {
        // Payment expects format of P,C W ID,C D ID,C ID,PAYMENT.
        String[] input = inputLine.split(",");
        assert(input[0].equals("P"));
        customerWarehouseId = Integer.parseInt(input[1]);
        customerDistrictId = Integer.parseInt(input[2]);
        customerId = Integer.parseInt(input[3]);
        payment = new BigDecimal(input[4]);
    }

    @Override
    public void execute() {
        System.out.println("Start Payment...");
        try(Connection connection = ds.getConnection()) {

            // (1) Obtain D_NEXT_O_ID
            PreparedStatement q1 = connection.prepareStatement("UPDATE Warehouse SET W_YTD = W_YTD + ? WHERE W_ID = ?");
            q1.setBigDecimal(1, payment);
            q1.setInt(2, customerWarehouseId);
            q1.execute();

            // (2) Update the district (C W ID,C D ID) by incrementing D YTD by PAYMENT
            PreparedStatement q2 = connection.prepareStatement("UPDATE District SET D_YTD = D_YTD + ? WHERE D_ID = ? AND D_W_ID = ?");
            q2.setBigDecimal(1, payment);
            q2.setInt(2, customerDistrictId);
            q2.setInt(3, customerWarehouseId);
            q2.execute();

            // (3) Update the customer
            PreparedStatement q3 = connection.prepareStatement("UPDATE Customer SET C_BALANCE = C_BALANCE - ?,  C_YTD_PAYMENT = C_YTD_PAYMENT + ?, C_PAYMENT_CNT = C_PAYMENT_CNT + 1 WHERE C_W_ID = ? AND C_D_ID = ? AND C_ID = ?");
            q3.setBigDecimal(1, payment);
            q3.setFloat(2, payment.floatValue());
            q3.setInt(3, customerWarehouseId);
            q3.setInt(4, customerDistrictId);
            q3.setInt(5, customerId);
            q3.execute();

            //4. Print output
            PreparedStatement q4 = connection.prepareStatement("SELECT * FROM Customer WHERE C_W_ID = ? AND C_D_ID = ? AND C_ID = ?");
            q4.setInt(1, customerWarehouseId);
            q4.setInt(2, customerDistrictId);
            q4.setInt(3, customerId);
            q4.execute();
            ResultSet r4 = q4.getResultSet();
            String[] itemInfo = new FormResults().formResults(r4).get(0).split(",");

            System.out.println(String.format(
                "1. Customer: (%d, %d, %d), Name: (%s, %s, %s), Address: (%s, %s, %s, %s, %s), C_PHONE: %s, C_SINCE: %s, C_CREDIT: %s, C_CREDIT_LIM: %.2f, C_DISCOUNT: %.4f, C_BALANCE: %.2f",
                customerWarehouseId, customerDistrictId, customerId,
                    itemInfo[3], itemInfo[4], itemInfo[5], itemInfo[6], itemInfo[7], itemInfo[8], itemInfo[9], itemInfo[10], itemInfo[11], itemInfo[12],
                    itemInfo[13], itemInfo[14], itemInfo[15], itemInfo[16]
                    ));

            PreparedStatement q5 = connection.prepareStatement("SELECT * FROM Warehouse WHERE W_ID = ?");
            q5.setInt(1, customerWarehouseId);
            q5.execute();
            ResultSet r5 = q5.getResultSet();
            String[] warehouseInfo = new FormResults().formResults(r5).get(0).split(",");

            System.out.println(String.format(
                "2. Warehouse Address: (%s, %s, %s, %s, %s)",
               warehouseInfo[2], warehouseInfo[3], warehouseInfo[4], warehouseInfo[5], warehouseInfo[6]
            ));

            PreparedStatement q6 = connection.prepareStatement("SELECT * FROM District WHERE D_ID = ? AND D_W_ID = ?");
            q6.setInt(1, customerDistrictId);
            q6.setInt(2, customerWarehouseId);
            q6.execute();
            ResultSet r6 = q6.getResultSet();
            String[] districtInfo = new FormResults().formResults(r6).get(0).split(",");

            System.out.println(String.format(
                "3. District Address: (%s, %s, %s, %s, %s)",
                districtInfo[3], districtInfo[4], districtInfo[5], districtInfo[6], districtInfo[7]
            ));

            System.out.println(String.format("4. Payment: %.2f", payment));

            System.out.println("Finish Payment...");

        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
