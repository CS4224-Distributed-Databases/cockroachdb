package Transactions;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

// TODO: To check if the create view statements are valid
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

    @Override
    public void execute() {
        System.out.println("Start Related Customers...");
        try(Connection connection = ds.getConnection()) {

            // (1) Obtain D_NEXT_O_ID
            // https://stackoverflow.com/questions/30918633/sql-cte-vs-view
            // Views can be indexed but CTE can't => Hence we choose to use View here

            String orderItemsView = "CREATE VIEW CS4225.OrderItemsView(OI_C_ID, OL_I_ID) " +
                    "AS SELECT CS4225.Order_New.OI_C_ID, CS4225.OrderLine.OL_I_ID " +
                    "FROM CS4225.Order_New JOIN CS4225.OrderLine ON CS4225.Order_New.O_ID = CS4225.OrderLine.OL_O_ID";

            String cusOrderItemsView = "CREATE VIEW CS4225.CustomerOrderItemsView(COI_C_ID, COI_W_ID, COI_D_ID, COI_I_ID) " +
                    "AS SELECT CS4225.Customer.C_ID, CS4225.Customer.C_W_ID, CS4225.Customer.C_D_ID, CS4225.OrderItemsView.OL_I_ID " +
                    "FROM CS4225.Customer JOIN CS4225.Order_New ON CS4225.Customer.C_ID = CS4225.Order_New.O_C_ID " +
                    "JOIN CS4225.OrderItemsView ON CS4225.Customer.C_ID = CS4225.OrderItemsView.OI_C_ID";

            String cusPairView = "CREATE VIEW CS4225.CustomerOrderItemsPairView(C_ID_One, W_ID_One, D_ID_One, C_ID_Two, W_ID_Two, D_ID_Two) " +
                    "AS SELECT first.COI_C_ID, first.COI_W_ID, first.COI_D_ID, first.COI_I_ID, second.COI_C_ID, second.COI_W_ID, second.COI_D_ID, second.COI_I_ID " +
                    "FROM CS4225.CustomerOrderItemsView AS first JOIN CS4225.CustomerOrderItemsView AS second " +
                    "ON first.COI_C_ID <> second.COI_C_ID";

            String diffItemsQuery = "CREATE VIEW CS4225.CustomerOrderItemsFilteredView(C_ID_One, W_ID_One, D_ID_One, C_ID_Two, W_ID_Two, D_ID_Two) " +
                    "AS SELECT * " +
                    "FROM CS4225.CustomerOrderItemsPairView where D_ID_ONE = D_ID_Two";

            String twoItemsDiffQuery = "CREATE VIEW CS4225.RelatedCus (C_ID_One, W_ID_One, D_ID_One, C_ID_Two, W_ID_Two, D_ID_Two) " +
                    "AS SELECT * " +
                    "FROM CS4225.CustomerOrderItemsFilteredView " +
                    "GROUP BY C_ID_One, W_ID_One, D_ID_One, C_ID_Two, W_ID_Two, D_ID_Two " +
                    "HAVING COUNT(*) >= 2";

            String relatedCus = "SELECT C_ID_Two, W_ID_Two, D_ID_Two " +
                    "FROM CS4225.RelatedCus " +
                    "WHERE CS4225.RelatedCus.C_ID_One = ? AND W_ID_One = ? AND D_ID_One = ?";

            PreparedStatement q1 = connection.prepareStatement(orderItemsView);
            PreparedStatement q2 = connection.prepareStatement(cusOrderItemsView);
            PreparedStatement q3 = connection.prepareStatement(cusPairView);
            PreparedStatement q4 = connection.prepareStatement(diffItemsQuery);
            PreparedStatement q5 = connection.prepareStatement(twoItemsDiffQuery);
            PreparedStatement q6 = connection.prepareStatement(relatedCus);

            q1.execute();
            q2.execute();
            q3.execute();
            q4.execute();
            q5.execute();
            q6.execute();

            ResultSet r6 = q6.getResultSet();
            ArrayList<String> relatedCustomers = new FormResults().formResults(r6);

            System.out.println("Related customers are: ");
            for (String cus: relatedCustomers){
                System.out.println(cus);
            }

            System.out.println("Finish Related Customers...");

            // Delete View

            PreparedStatement q7 = connection.prepareStatement("DROP VIEW IF EXISTS CS4225.OrderItemsView");
            PreparedStatement q8 = connection.prepareStatement("DROP VIEW IF EXISTS CS4225.CustomerOrderItemsView");
            PreparedStatement q9 = connection.prepareStatement("DROP VIEW IF EXISTS CS4225.CustomerOrderItemsPairView");
            PreparedStatement q10 = connection.prepareStatement("DROP VIEW IF EXISTS CustomerOrderItemsFilteredView");
            PreparedStatement q11 = connection.prepareStatement("DROP VIEW IF EXISTS CS4225.RelatedCus");

            q7.execute();
            q8.execute();
            q9.execute();
            q10.execute();
            q11.execute();

        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
