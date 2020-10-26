import org.postgresql.ds.PGSimpleDataSource;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TotalStatsRunner {

    private static PGSimpleDataSource ds;

    public static void main(String[] args) {

        // (1) Initialise Cluster
        // Configure the database connection.
        ds = new PGSimpleDataSource();
        ds.setServerName("localhost");
        ds.setPortNumber(26257);

        ds.setDatabaseName("cs4224"); // Impt
        ds.setUser("root"); // Note that we created an insecure password that does not require password
        ds.setReWriteBatchedInserts(true); // add `rewriteBatchedInserts=true` to pg connection string
        ds.setApplicationName("CS4224");

        // Iterate through the err log files and get the stats for max, min, avg throughputs among all clients
        // USER TO INPUT THE NUM OF CLIENTS THAT WAS PASSED IN TO TEST
        int numClients = Integer.parseInt(args[0]);
        double[] result = getThroughputsStatsFromLogFiles(numClients);
        writeTotalThroughputStatsToCsv(result[0], result[1], result[2], numClients);

        close();
    }

    public static double[] getThroughputsStatsFromLogFiles(int numClients) {
        double[] results = new double[3]; // first store min, second store max, third store total

        double minThroughputPercentage = Double.MAX_VALUE;
        double maxThroughputPercentage = 0;
        double totalThroughputPercentage = 0; // to calculate average later

        for (int i = 1; i <= numClients; i++) {
            String fileName = "log/" + i + ".err.log";
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                String line;
                while( (line = br.readLine() ) != null) {
                    if (line.startsWith("Transaction throughput: ")) {
                        Pattern p = Pattern.compile("(\\d+(?:\\.\\d+))");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            double throughput = Double.parseDouble(m.group(1));
                            minThroughputPercentage = Math.min(minThroughputPercentage, throughput);
                            maxThroughputPercentage = Math.max(maxThroughputPercentage, throughput);
                            totalThroughputPercentage += throughput;
                        }
                    }
                }
            } catch (IOException e) {
                e.getMessage();
            }
        }

        results[0] = minThroughputPercentage;
        results[1] = maxThroughputPercentage;
        results[2] = totalThroughputPercentage;

        return results;
    }

    public static void writeTotalThroughputStatsToCsv(double minThroughputPercentage,double maxThroughputPercentage,  double totalThroughputPercentage, int numClients) {
        /*
            THROUGHPUT STATS
         */
        try (PrintWriter writer = new PrintWriter(new File("src/output/throughput_stats.csv"))) {
            StringBuilder sb = new StringBuilder();
            // Key in the experiment number manually in a separate csv
            // 1-4 for Cassandra, 5-8 for Cockroach
            //          sb.append("experiment_number");
            //          sb.append(',');
            sb.append("min throughput");
            sb.append(',');
            sb.append("avg throughput");
            sb.append(',');
            sb.append("max throughput");
            sb.append('\n');

            sb.append(minThroughputPercentage);
            sb.append(',');
            sb.append(totalThroughputPercentage / numClients);
            sb.append(',');
            sb.append(maxThroughputPercentage);

            writer.write(sb.toString());

            System.out.println("done writing to output/throughput_stats.csv");
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void close(){
        // close and exit
        System.exit(0);
    }

}
