import Transactions.FormResults;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EndStateRunner {

    private static PGSimpleDataSource ds;
    public static String directoryName;

    public static void main(String[] args) {

        // Configure the database connection.
        ds = new PGSimpleDataSource();
        //ds.setServerName("localhost"); //originally localhost
        //ds.setPortNumber(26257);
        ds.setUrl("jdbc:postgresql://192.168.48.169:50000/?sslmode=disable");

        System.out.println(ds.getDescription());
        ds.setDatabaseName("cs4224"); // Impt
        ds.setUser("root"); // Note that we created an insecure password that does not require password
        ds.setReWriteBatchedInserts(true); // add `rewriteBatchedInserts=true` to pg connection string
        ds.setApplicationName("CS4224");


        // Output database state
        try(Connection connection = ds.getConnection()) {

            PreparedStatement q1 = connection.prepareStatement("select sum(w_ytd) from warehouse");
            q1.execute();
            ResultSet r1 = q1.getResultSet();
            BigDecimal sum_wtd = new BigDecimal(new FormResults().formResults(r1).get(0));

            PreparedStatement q2 = connection.prepareStatement("select sum(d_ytd), sum(d_next_o_id) from district");
            q2.execute();
            ResultSet r2 = q2.getResultSet();
            String [] district = new FormResults().formResults(r2).get(0).split(",");
            BigDecimal sum_d_ytd = new BigDecimal(district[0]);
            BigDecimal sum_next_o_id = new BigDecimal(district[1]);


            PreparedStatement q3 = connection.prepareStatement("select sum(C_BALANCE), sum(C_YTD_PAYMENT), sum(C_PAYMENT_CNT), sum(C_DELIVERY_CNT) from customer");
            q3.execute();
            ResultSet r3 = q3.getResultSet();
            String [] customer = new FormResults().formResults(r3).get(0).split(",");
            BigDecimal sum_c_balance = new BigDecimal(customer[0]);
            Float sum_c_ytd_payment = new Float(customer[1]);
            Integer sum_c_payment_cnt = new Integer(customer[2]);
            Integer sum_c_delivery_cnt = new Integer(customer[3]);

            PreparedStatement q4 = connection.prepareStatement("select max(o_id), sum(o_ol_cnt) from order_new");
            q4.execute();
            ResultSet r4 = q4.getResultSet();
            String [] order = new FormResults().formResults(r4).get(0).split(",");
            Integer sum_o_id = new Integer(order[0]);
            BigDecimal sum_o_ol_cnt = new BigDecimal(order[1]);

            PreparedStatement q5 = connection.prepareStatement("select sum(ol_amount), sum(ol_quantity) from orderline");
            q5.execute();
            ResultSet r5 = q5.getResultSet();
            String [] orderline = new FormResults().formResults(r5).get(0).split(",");
            BigDecimal sum_ol_amount = new BigDecimal(orderline[0]);
            BigDecimal sum_ol_quantity = new BigDecimal(orderline[1]);

            PreparedStatement q6 = connection.prepareStatement("select sum(S_QUANTITY), sum(S_YTD), sum(S_ORDER_CNT), sum(S_REMOTE_CNT) from stock");
            q6.execute();
            ResultSet r6 = q6.getResultSet();
            String [] stock = new FormResults().formResults(r6).get(0).split(",");
            BigDecimal sum_s_quantity = new BigDecimal(stock[0]);
            BigDecimal sum_s_ytd = new BigDecimal(stock[1]);
            Integer sum_s_order_cnt = new Integer(stock[2]);
            Integer sum_s_remote_cnt = new Integer(stock[3]);

            directoryName = args[0];
            System.out.println("directory is " + directoryName);

            try (PrintWriter writer = new PrintWriter(new File(directoryName + "end_state.csv"))) {
                StringBuilder sb = new StringBuilder();
                // Key in the experiment number manually in a separate csv
                // 1-4 for Cassandra, 5-8 for Cockroach
                //          sb.append("experiment_number");
                //          sb.append(',');

                sb.append("sum_wtd");
                sb.append(',');
                sb.append("sum_d_ytd");
                sb.append(',');
                sb.append("sum_next_o_id");
                sb.append(',');
                sb.append("sum_c_balance");
                sb.append(',');
                sb.append("sum_c_ytd_payment");
                sb.append(',');
                sb.append("sum_c_payment_cnt");
                sb.append(',');
                sb.append("sum_c_delivery_cnt");
                sb.append(',');
                sb.append("sum_o_id");
                sb.append(',');
                sb.append("sum_o_ol_cnt");
                sb.append(',');
                sb.append("sum_ol_amount");
                sb.append(',');
                sb.append("sum_ol_quantity");
                sb.append(',');
                sb.append("sum_s_quantity");
                sb.append(',');
                sb.append("sum_s_ytd");
                sb.append(',');
                sb.append("sum_s_order_cnt");
                sb.append(',');
                sb.append("sum_s_remote_cnt");
                sb.append('\n');

                sb.append(sum_wtd);
                sb.append(',');
                sb.append(sum_d_ytd);
                sb.append(',');
                sb.append(sum_next_o_id);
                sb.append(',');
                sb.append(sum_c_balance);
                sb.append(',');
                sb.append(sum_c_ytd_payment);
                sb.append(',');
                sb.append(sum_c_payment_cnt);
                sb.append(',');
                sb.append(sum_c_delivery_cnt);
                sb.append(',');
                sb.append(sum_o_id);
                sb.append(',');
                sb.append(sum_o_ol_cnt);
                sb.append(',');
                sb.append(sum_ol_amount);
                sb.append(',');
                sb.append(sum_ol_quantity);
                sb.append(',');
                sb.append(sum_s_quantity);
                sb.append(',');
                sb.append(sum_s_ytd);
                sb.append(',');
                sb.append(sum_s_order_cnt);
                sb.append(',');
                sb.append(sum_s_remote_cnt);
                sb.append('\n');

                writer.write(sb.toString());

                System.out.println("done writing to end_state.csv");
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }

        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                e.getSQLState(), e.getCause(), e.getMessage());
        }


        close();
    }

    public static void runSQL(String sqlCode){
        System.out.println(sqlCode);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(sqlCode);
            pstmt.execute();
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }

    public static void close(){
        // close and exit
        System.exit(0);
    }

}
