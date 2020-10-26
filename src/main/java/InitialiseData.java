import java.sql.*;

import DataLoader.CreateTables;
import DataLoader.LoadData;
import org.postgresql.ds.PGSimpleDataSource;

public class InitialiseData {

    private static String DIRECTORY = "src/main/java/";
    private static PGSimpleDataSource ds;

    public static void InitialiseData(String[] args)  throws Exception {

        // Configure the database connection.
        ds = new PGSimpleDataSource();
        ds.setServerName("localhost");
        ds.setPortNumber(26257);

        ds.setDatabaseName("cs4224"); // Impt
        ds.setUser("root"); // Note that we created an insecure password that does not require password
        ds.setReWriteBatchedInserts(true); // add `rewriteBatchedInserts=true` to pg connection string
        ds.setApplicationName("CS4224");

        // Set up the 'CS4224' Database
        runSQL("CREATE DATABASE IF NOT EXISTS cs4224");

        // Create Tables
        CreateTables c = new CreateTables(ds);

        // Load Data
        LoadData l = new LoadData(ds);
        l.loadAllData();

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

    public static void close() {
        // close and exit
        System.exit(0);
    }

}