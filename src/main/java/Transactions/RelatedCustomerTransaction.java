package Transactions;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

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

    // SUPER LONG TRANSACTION. Since it doesnt affect the state of the database, we are going to not run it.
    public void execute() {
        System.out.println("Start Related Customers...");
        try(Connection connection = ds.getConnection()) {

            // Get all current customers' orders
            String currentCus = "SELECT O_ID from Order_New WHERE O_W_ID = ? AND O_D_ID = ? AND O_C_ID = ?";
            PreparedStatement q0 = connection.prepareStatement(currentCus);
            q0.setInt(1, warehouseID);
            q0.setInt(2, districtID);
            q0.setInt(3, customerID);
            q0.execute();
            ResultSet r0 = q0.getResultSet();
            ArrayList<String> ordersOfCurrent = new FormResults().formResults(r0);

            // Get ItemsIDs for all Current customer's orders and add to hashset
            String currentCusItems = "SELECT OL_I_ID FROM OrderLine WHERE OL_W_ID = ? AND OL_D_ID = ? AND OL_O_ID = ?";
            PreparedStatement q1 = connection.prepareStatement(currentCusItems);
            HashSet<Integer> currCusItemIDs = new HashSet<>();
            for (String ord: ordersOfCurrent){
                q1.setInt(1, warehouseID);
                q1.setInt(2, districtID);
                q1.setInt(3, Integer.parseInt(ord));

                q1.execute();
                ResultSet r1 = q1.getResultSet();
                ArrayList<String> itemsOfCurr = new FormResults().formResults(r1);
                for (String item: itemsOfCurr){
                    currCusItemIDs.add(Integer.parseInt(item));
                }
            }

            // Get all related customers' orders together with their identifiers
            String relatedCus = "SELECT O_W_ID, O_D_ID, O_C_ID, O_ID from Order_New WHERE O_W_ID <> ? AND O_C_ID <> ?";
            PreparedStatement q2 = connection.prepareStatement(relatedCus);
            q2.setInt(1, warehouseID);
            q2.setInt(2, customerID);
            q2.execute();
            ResultSet r2 = q2.getResultSet();
            ArrayList<String> relatedCustomers = new FormResults().formResults(r2);

            // Get ItemsIDs for all related customer's orders and add to hashmap
            // Key: Relatedcus ID, Values: RelatedCus items
            String relatedCusOrders = "SELECT OL_I_ID FROM OrderLine WHERE OL_W_ID = ? AND OL_D_ID = ? AND OL_O_ID = ?";
            PreparedStatement q3 = connection.prepareStatement(relatedCusOrders);
            HashMap<String, ArrayList<Integer>> relatedCusAndItems = new HashMap<>();

            for (String cus: relatedCustomers){
                String [] customer = cus.split(",");
                q3.setInt(1, Integer.parseInt(customer[0]));
                q3.setInt(2, Integer.parseInt(customer[1]));
                q3.setInt(3, Integer.parseInt(customer[3]));
                q3.execute();


                String identifier = customer[0] + ", "+customer[1] + ", "+ customer[2];
                ResultSet r3 = q3.getResultSet();
                ArrayList<String> orderItems = new FormResults().formResults(r3);
                for (String item: orderItems){
                    ArrayList<Integer> itemsAlrdyAdded = relatedCusAndItems.getOrDefault(relatedCusAndItems.get(identifier), new ArrayList<Integer>());
                    itemsAlrdyAdded.add(Integer.parseInt(item));
                    relatedCusAndItems.put(identifier, itemsAlrdyAdded);
                }
            }

            System.out.println("Related customers are: ");

            // Iterate through relatedcus and find match
            for (Map.Entry<String, ArrayList<Integer>> eachCustomer: relatedCusAndItems.entrySet()){
                String cusIdentifier = eachCustomer.getKey();
                int count = 0;
                // Iterate through all items of related customers
                for(Integer item: eachCustomer.getValue()){
                    // and compare with hash set of current customers' items
                    if(currCusItemIDs.contains(item)){
                        count ++;
                    }
                    if(count == 2){
                        System.out.println(cusIdentifier);
                        break;
                    }
                }
            }

            System.out.println("Finish Related Customers...");

        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
