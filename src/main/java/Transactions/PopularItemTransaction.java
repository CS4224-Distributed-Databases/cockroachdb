package Transactions;

import org.postgresql.ds.PGSimpleDataSource;
import util.ItemData;
import util.TimeHelper;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class PopularItemTransaction extends BaseTransaction{

    private DataSource ds;

    private int warehouseId;
    private int districtId;
    private int numOfLastOrders;

    public PopularItemTransaction(PGSimpleDataSource datasource) {
        super(datasource);
        ds = datasource;
    }

    public void parseInput(Scanner sc, String inputLine) {
        // PopularItem expects format of I,W_ID,D_ID,L.
        String[] input = inputLine.split(",");
        assert(input[0].equals("I"));
        this.warehouseId = Integer.parseInt(input[1]);
        this.districtId = Integer.parseInt(input[2]);
        this.numOfLastOrders = Integer.parseInt(input[3]);
    }

    @Override
    public void execute() {
        System.out.println("Start Popular Item...");
        try(Connection connection = ds.getConnection()) {
            // (1) Get N = next available order no (D_NEXT_O_ID)
            PreparedStatement q1 = connection.prepareStatement("SELECT D_NEXT_O_ID from District WHERE D_W_ID = ? AND D_ID = ?");
            q1.setInt(1, warehouseId);
            q1.setInt(2, districtId);
            q1.execute();
            ResultSet r1 = q1.getResultSet();
            String nextOrderNum = new FormResults().formResults(r1).get(0);
            int N = Integer.parseInt(nextOrderNum);

            System.out.println(String.format("1. District: (%d, %d)", warehouseId, districtId));
            System.out.println(String.format("2. Number of last orders: %d", numOfLastOrders));
            System.out.println("3. For each order");

            HashSet<ItemData> popularItems = new HashSet<>();

            // (2) Get the set of last orders for this district [N-L to N)
            // Using a range query on O_ID (an index) which is efficient for cockroach db unlike cassandra
            int minOrderNum = N - numOfLastOrders;
            int maxOrderNum = N - 1;

            String orderWithCustomerQuery = "SELECT CO_O_ID, CO_O_ENTRY_D, CO_C_FIRST, CO_C_MIDDLE, CO_C_LAST " +
                    "FROM CS4224.CustomerOrderView " +
                    "WHERE CO_W_ID = ? AND CO_D_ID = ? AND CO_O_ID BETWEEN ? AND ?";
            PreparedStatement q2 = connection.prepareStatement(orderWithCustomerQuery);
            q2.setInt(1, warehouseId);
            q2.setInt(2, districtId);
            q2.setInt(3, minOrderNum);
            q2.setInt(4, maxOrderNum);
            q2.execute();
            ResultSet r2 = q2.getResultSet();
            ArrayList<String> ordersList = new FormResults().formResults(r2);

            for (String orderRow : ordersList) {
                String [] orderInfo = orderRow.split(",");
                int orderNum = Integer.parseInt(orderInfo[0]);
                String entryDate = orderInfo[1] == null ? "null" : TimeHelper.formatDate(Timestamp.valueOf(orderInfo[1]));
                System.out.println(String.format("\t3.1 Order number: %d, Entry date: %s", orderNum, entryDate));
                System.out.println(String.format("\t3.2 Customer: (%s, %s, %s)", orderInfo[2], orderInfo[3], orderInfo[4]));
                System.out.println("\t3.3 Popular Items: ");

                // (3) For each order, get the items with highest OL_QUANTITY among all orderlines with this o_id
                // Join the orderline OL with item I on OL.OL_I_ID = I.I_ID (want to produce I_ID, I_NAME and OL_QUANTITY)

                String orderLineItemQuery = "SELECT I.I_ID, I.I_NAME, OL.OL_QUANTITY " +
                        "FROM CS4224.OrderLine AS OL JOIN CS4224.Item AS I ON OL.OL_I_ID = I.I_ID " +
                        "WHERE OL.OL_W_ID = ? AND OL.OL_D_ID = ? AND " +
                        "OL.OL_O_ID = ? AND OL.OL_QUANTITY = " +
                        "(SELECT MAX(OL_2.OL_QUANTITY) FROM CS4224.OrderLine AS OL_2 " +
                        "WHERE OL_2.OL_O_ID = ? AND OL_2.OL_W_ID = ? AND OL_2.OL_D_ID = ?)";
                PreparedStatement q3 = connection.prepareStatement(orderLineItemQuery);
                q3.setInt(1, warehouseId);
                q3.setInt(2, districtId);
                q3.setInt(3, orderNum);
                q3.setInt(4, orderNum);

                q3.setInt(5, warehouseId);
                q3.setInt(6, districtId);

                q3.execute();
                ResultSet r3 = q3.getResultSet();
                ArrayList<String> itemsList = new FormResults().formResults(r3);
                for (String itemRow : itemsList) {
                    String [] itemInfo = itemRow.split(",");
                    int itemId = Integer.parseInt(itemInfo[0]);
                    String itemName = itemInfo[1];
                    BigDecimal quantity = DatatypeConverter.parseDecimal(itemInfo[2]);
                    popularItems.add(new ItemData(itemId, itemName));
                    System.out.println(String.format("\t\t Item name: %s", itemName));
                    System.out.println(String.format("\t\t Quantity: %.0f", quantity));
                }
            }
            // Note that did not bother joining (2) and (3) into 1 query to minimise the intermediate table sizes
            // and no of rows returned as we would need to produce independent results for each order anyway

            // (4) Get the % of examined orders containing this popz item
            System.out.println("4. For each popular item");
            String ordersWithPopItemQuery = "SELECT COUNT(DISTINCT COI_O_ID)" +
                    "FROM CS4224.CustomerOrderItemsView " +
                    "WHERE COI_W_ID = ? AND COI_D_ID = ? AND COI_O_ID BETWEEN ? AND ? " +
                    "GROUP BY COI_W_ID, COI_D_ID, COI_O_ID " +
                    "HAVING COUNT(*) FILTER (WHERE COI_I_ID = ?) > 0 ";
            PreparedStatement q4 = connection.prepareStatement(ordersWithPopItemQuery);
            for (ItemData popularItemData : popularItems) {
                q4.setInt(1, warehouseId);
                q4.setInt(2, districtId);
                q4.setInt(3, minOrderNum);
                q4.setInt(4, maxOrderNum);
                q4.setInt(5, popularItemData.itemId);
                q4.execute();

                ResultSet r4 = q4.getResultSet();
                String numOfOrdersRes = new FormResults().formResults(r4).get(0);
                int numOfOrders = Integer.parseInt(numOfOrdersRes);
                double percentage =  ((double) numOfOrders / numOfLastOrders) * 100.0;
                System.out.println(String.format("\tItem name: %s", popularItemData.itemName));
                System.out.println(String.format("\tPercentage of last orders containing this: %.2f%%", percentage));
            }

            System.out.println("Finish Popular Item...");

        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
