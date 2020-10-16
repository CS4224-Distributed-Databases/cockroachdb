// Keeping this file for future references
// Taken from their documentation for cockroachdb

import java.sql.*;

import DataLoader.CreateTables;
import org.postgresql.ds.PGSimpleDataSource;

public class Main {

    private static String DIRECTORY = "src/main/java/";
    private static PGSimpleDataSource ds;

    public static void main(String[] args) {

        // Configure the database connection.
        ds = new PGSimpleDataSource();
        ds.setServerName("localhost");
        ds.setPortNumber(26257);

        ds.setDatabaseName("cs4224"); // Impt
        
        ds.setReWriteBatchedInserts(true); // add `rewriteBatchedInserts=true` to pg connection string
        ds.setApplicationName("CS4224");

        // TODO: Explore how to set up more than 1 node using this -> i manually created the cluster.

        // Set up the 'CS4224' Database
        runSQL("CREATE DATABASE IF NOT EXISTS cs4224");

        // Create Tables
        CreateTables c = new CreateTables(ds);

        // Load Data

    }

    public static void runSQL(String sqlCode){
        try (Connection connection = ds.getConnection()){
            PreparedStatement pstmt = connection.prepareStatement(sqlCode);

            if (pstmt.execute()) {
                // We know that `pstmt.getResultSet()` will
                // not return `null` if `pstmt.execute()` was
                // true
                ResultSet rs = pstmt.getResultSet();
                ResultSetMetaData rsmeta = rs.getMetaData();
                int colCount = rsmeta.getColumnCount();

                while (rs.next()) {
                    for (int i=1; i <= colCount; i++) {
                        String name = rsmeta.getColumnName(i);
                        String type = rsmeta.getColumnTypeName(i);

                        // In this "bank account" example we know we are only handling
                        // integer values (technically 64-bit INT8s, the CockroachDB
                        // default).  This code could be made into a switch statement
                        // to handle the various SQL types needed by the application.
                        if (type == "int8") {
                            int val = rs.getInt(name);

                            // This printed output is for debugging and/or demonstration
                            // purposes only.  It would not be necessary in production code.
                            System.out.printf("    %-8s => %10s\n", name, val);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.out.printf("Execute.runSQL ERROR: { state => %s, cause => %s, message => %s }\n",
                    e.getSQLState(), e.getCause(), e.getMessage());
        }

    }

}
