package Transactions;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

// Helper Class to read data from given ResultSet

public class FormResults {
    public ArrayList<String> formResults(ResultSet resultSet) throws SQLException {
        ArrayList<String> result = new ArrayList<>();
        ResultSetMetaData rsmd1 = resultSet.getMetaData();
        int NumOfCol = rsmd1.getColumnCount();
        while (resultSet.next())
        {
            String row = "";
            for(int i = 1; i <= NumOfCol; i++)
            {
                if(i==NumOfCol){
                    row += resultSet.getObject(i);
                } else {
                    row += resultSet.getObject(i) + ",";
                }
                System.out.print(resultSet.getObject(i));
            }
            result.add(row);
        }
        return result;
    }
}
