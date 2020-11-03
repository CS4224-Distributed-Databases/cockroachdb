package Transactions;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class RelatedCustomerTransaction extends BaseTransaction{

    private DataSource ds;

    private static final int NUM_DISTRICTS = 10;
    private int warehouseID;
    private int districtID;
    private int customerID;

    public RelatedCustomerTransaction(PGSimpleDataSource datasource) {
        super(datasource);
        ds = datasource;
    }

    public void parseInput(Scanner sc, String inputLine) {
        // Related Customers expects format of R, W_ID, D_ID, C_ID
        String[] input = inputLine.split(",");
        assert(input[0].equals("R"));
        this.warehouseID = Integer.parseInt(input[1]);
        this.districtID = Integer.parseInt(input[2]);
        this.customerID = Integer.parseInt(input[3]);
    }

    public void execute() {
        System.out.println("Start Related Customers...");
        try(Connection connection = ds.getConnection()) {

            // remove relatedCus view and combine it with the query
            // because Groupby inside a view is expensive.
            String relatedCus = "SELECT C_ID_Two, W_ID_Two, D_ID_Two " +
                    "FROM CustomerOrderItemsPairView " +
                    "WHERE C_ID_One = ? AND W_ID_One = ? AND D_ID_One = ? " +
                    "GROUP BY C_ID_One, W_ID_One, D_ID_One, C_ID_Two, W_ID_Two, D_ID_Two " +
                    "HAVING COUNT(*) >= 2";

            PreparedStatement q1 = connection.prepareStatement(relatedCus);
            q1.setInt(1, customerID);
            q1.setInt(2, warehouseID);
            q1.setInt(3, districtID);

            q1.execute();

            ResultSet r1 = q1.getResultSet();
            ArrayList<String> relatedCustomers = new FormResults().formResults(r1);

            System.out.println("Related customers are: ");
            for (String cus: relatedCustomers){
                System.out.println(cus);
            }

            System.out.println("Finish Related Customers...");


        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
