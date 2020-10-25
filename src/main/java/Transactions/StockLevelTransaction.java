package Transactions;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class StockLevelTransaction extends BaseTransaction {
    private int warehouseId;
    private int districtId;
    private BigDecimal stockThreshold;
    private int ordersToExamine;
    private DataSource ds;

    public StockLevelTransaction(PGSimpleDataSource datasource) {
        super(datasource);
        ds = datasource;
    }

    public void parseInput(Scanner sc, String inputLine) {
        // Input format W_ID, D_ID, T, L
        String[] input = inputLine.split(",");
        assert (input[0].equals("O"));
        warehouseId = Integer.parseInt(input[1]);
        districtId = Integer.parseInt(input[2]);
        stockThreshold = new BigDecimal((input[3]));
        ordersToExamine = Integer.parseInt(input[4]);
    }

    public void execute() {

        System.out.println("Starting Execution of Stock Level Transaction...");

        try (Connection connection = ds.getConnection()) {
            // Query 1: Get next available order number from specified district
            PreparedStatement q1 = connection.prepareStatement("SELECT d_next_o_id FROM district where d_w_id = ? AND d_id = ?");
            q1.setInt(1, warehouseId);
            q1.setInt(2, districtId);
            q1.execute();

            ResultSet r1 = q1.getResultSet();
            String districtInfo = new FormResults().formResults(r1).get(0);
            Integer nextOID = Integer.parseInt(districtInfo);
            Integer startingFromOrder = nextOID - ordersToExamine;

            // Query 2: Get the set of item from last L orders
            PreparedStatement q2 = connection.prepareStatement("SELECT ol_i_id FROM order_line WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id >= ? AND ol_o_id < ?");
            q2.setInt(1, warehouseId);
            q2.setInt(2, districtId);
            q2.setInt(3, startingFromOrder);
            q2.setInt(4, nextOID);
            q2.execute();

            ResultSet r2 = q2.getResultSet();
            List<String> lastLOrders = new FormResults().formResults(r2);

            Set<Integer> itemIDs = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                String[] lastKthOrder = lastLOrders.get(i).split(",");
                Integer itemId = Integer.parseInt(lastKthOrder[0]);
                itemIDs.add(itemId);
            }

            Integer itemsBelowThresholdCount = 0;
            for (Integer itemID : itemIDs) {
                // Query 3: Get stock items and check if their quantity is below threshold
                PreparedStatement q3 = connection.prepareStatement("SELECT s_quantity FROM stock WHERE s_w_id = ? and s_i_id = ?");
                q3.setInt(1, warehouseId);
                q3.setInt(2, itemID);
                q3.execute();
                ResultSet r3 = q3.getResultSet();
                String itemStock = new FormResults().formResults(r3).get(0);
                BigDecimal itemStockQuantity = BigDecimal.valueOf(Long.parseLong(itemStock));
                // Increase the count if stock quantity below threshold
                if (itemStockQuantity.compareTo(stockThreshold) < 0) {
                    itemsBelowThresholdCount++;
                }
            }

            System.out.printf("Num of items below threshold: %d\n", itemsBelowThresholdCount);
            System.out.println("Finish executing Stock Level Transaction...");

        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }
}
