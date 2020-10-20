package Transactions;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class TopBalanceTransaction extends BaseTransaction{

    private DataSource ds;

    private int customerWarehouseId;
    private int customerDistrictId;
    private int customerId;
    private BigDecimal payment;

    public TopBalanceTransaction(PGSimpleDataSource datasource) {
        super(datasource);
        ds = datasource;
    }

    public void parseInput(Scanner sc, String inputLine) {
        assert(inputLine.equals("T"));
    }

    @Override
    public void execute() {

        System.out.println("Starting Execution of Top Balance Transaction...");
        try(Connection connection = ds.getConnection()) {

            // (1) Find top 10 customers with highest C_BALANCE in desc order
            // retrieve C_W_ID, C_D_ID, (C FIRST, C MIDDLE, C LAST)
            PreparedStatement q1 = connection.prepareStatement("SELECT C_W_ID, C_D_ID, C_FIRST, C_MIDDLE, C_LAST, C_BALANCE FROM Customer ORDER BY C_BALANCE DESC LIMIT 10");
            q1.execute();
            ResultSet r1 = q1.getResultSet();
            List<String> customerResultsList= new FormResults().formResults(r1);
            for(int i = 0; i < 10; i++) {
                String[] customerInfo = customerResultsList.get(i).split(",");
                System.out.println("Customer "+ i+1);
                System.out.println(String.format("1. Customer Name: %s, %s, %s", customerInfo[2], customerInfo[3], customerInfo[4]));
                System.out.println(String.format("2. Outstanding balance: %.2f", Double.parseDouble(customerInfo[5])));

                // (2) get W_NAME from Warehouse gievn C_W_ID
                PreparedStatement q2 = connection.prepareStatement("SELECT W_NAME FROM Warehouse WHERE W_ID = ?");
                q2.setInt(1, Integer.parseInt(customerInfo[0]));
                q2.execute();
                ResultSet r2 = q2.getResultSet();
                String warehouseName = new FormResults().formResults(r2).get(0);
                System.out.println(String.format("3. Warehouse Name: %s", warehouseName));

                // (3) get D_NAME from Warehouse gievn C_D_ID
                PreparedStatement q3 = connection.prepareStatement("SELECT D_NAME FROM District WHERE D_W_ID = ? AND D_ID = ?");
                q3.setInt(1, Integer.parseInt(customerInfo[0]));
                q3.setInt(2, Integer.parseInt(customerInfo[1]));
                q3.execute();
                ResultSet r3 = q3.getResultSet();
                String districtName = new FormResults().formResults(r3).get(0);
                System.out.println(String.format("4. District Name: %s", districtName));
            }

            System.out.println("Finish executing Top Balance Transaction...");

        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
