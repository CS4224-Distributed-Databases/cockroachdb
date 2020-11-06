import java.sql.*;
import java.util.*;

import Transactions.*;
import org.postgresql.ds.PGSimpleDataSource;

public class Main {

    private static PGSimpleDataSource ds;

    private static final double convertSecondsDenom = 1000000000.0;
    private static final double convertMilliSecondsDenom = 1000000.0;

    public static void main(String[] args) throws Exception {

        String serverIP = args[0];

        System.out.println("Running code on node " + serverIP);
        // Configure the database connection.
        ds = new PGSimpleDataSource();
        // ds.setServerName("localhost"); //originally localhost
        // ds.setPortNumber(26257);
        ds.setUrl("jdbc:postgresql://" + serverIP + "/?sslmode=disable");

        System.out.println(ds.getDescription());
        ds.setDatabaseName("cs4224"); // Impt
        ds.setUser("root"); // Note that we created an insecure password that does not require password
        ds.setReWriteBatchedInserts(true); // add `rewriteBatchedInserts=true` to pg connection string
        ds.setApplicationName("CS4224");

        Scanner sc = new Scanner(System.in);
        int numOfTransactions = 0;
        long startTime;
        long endTime;
        long transactionStart;
        long transactionEnd;
        List<Long> latencies = new ArrayList<>();

        System.out.println("Start executing transactions ..... ");

        startTime = System.nanoTime();
        while (sc.hasNext()) {
            String inputLine = sc.nextLine();

            BaseTransaction transaction = null;
            if (inputLine.startsWith("N")) {
                transaction = new NewOrderTransaction(ds);
            } else if (inputLine.startsWith("P")) {
                transaction = new PaymentTransaction(ds);
            } else if (inputLine.startsWith("D")) {
                transaction = new DeliveryTransaction(ds);
            } else if (inputLine.startsWith("O")) {
                transaction = new OrderStatusTransaction(ds);
            } else if (inputLine.startsWith("S")) { // checked
                transaction = new StockLevelTransaction(ds);
            } else if (inputLine.startsWith("I")) {
                transaction = new PopularItemTransaction(ds);
            } else if (inputLine.startsWith("T")) {
                transaction = new TopBalanceTransaction(ds);
            } else if (inputLine.startsWith("R")) {
                transaction = new RelatedCustomerTransaction(ds);
            }

            if (transaction != null) {
                numOfTransactions++;
                transaction.parseInput(sc, inputLine);
                transactionStart = System.nanoTime();
                transaction.execute();
                transactionEnd = System.nanoTime();
                latencies.add(transactionEnd - transactionStart);
            }
        }
        endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        double timeElapsedInSeconds = timeElapsed / convertSecondsDenom;
        Collections.sort(latencies);
        double averageLatencyInMs = getAverageLatency(latencies) / convertMilliSecondsDenom;
        double medianLatencyInMs = getMedianLatency(latencies) / convertMilliSecondsDenom;
        double percentileLatency95InMs = getPercentileLatency(latencies, 95) / convertMilliSecondsDenom;
        double percentileLatency99InMs = getPercentileLatency(latencies, 99) / convertMilliSecondsDenom;

        printPerformance(numOfTransactions, timeElapsedInSeconds, averageLatencyInMs, medianLatencyInMs, percentileLatency95InMs, percentileLatency99InMs);

        close();

    }

    public static void runSQL(String sqlCode) {
        System.out.println(sqlCode);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(sqlCode);
            pstmt.execute();
        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }
    }

    private static double getAverageLatency(List<Long> latencies) {
        double sum = 0.0;
        for (Long latency : latencies) {
            sum += latency;
        }
        return sum / latencies.size();
    }

    private static double getMedianLatency(List<Long> latencies) {
        int length = latencies.size();
        double medianValue;
        int index = length / 2;
        if (length % 2 == 0) {
            medianValue = latencies.get(index) + (latencies.get(index + 1) - latencies.get(index)) / 2.0; //avoid overflow
        } else {
            medianValue = latencies.get(index);
        }
        return medianValue;
    }

    private static long getPercentileLatency(List<Long> latencies, int percentile) {
        int length = latencies.size();
        int index = (int) (length * ((double) percentile / 100.0));
        return latencies.get(index);
    }

    private static void printPerformance(int numOfTransactions, double timeElapsedInSeconds, double averageLatencyInMs, double medianLatencyInMs, double percentileLatency95InMs, double percentileLatency99InMs) {
        System.err.println("---------------- Performance Output ----------------");
        System.err.println("Number of executed transactions: " + numOfTransactions);
        System.err.println(String.format("Total transaction execution time (sec): %.2f", timeElapsedInSeconds));
        System.err.println(String.format("Transaction throughput: %.2f", numOfTransactions / timeElapsedInSeconds));
        System.err.println(String.format("Average transaction latency (ms): %.2f", averageLatencyInMs));
        System.err.println(String.format("Median transaction latency (ms): %.2f", medianLatencyInMs));
        System.err.println(String.format("95th percentile transaction latency (ms): %.2f", percentileLatency95InMs));
        System.err.println(String.format("99th percentile transaction latency (ms): %.2f", percentileLatency99InMs));
        System.err.println("----------------------------------------------------");
    }

    public static void close() {
        // close and exit
        System.exit(0);
    }

}
