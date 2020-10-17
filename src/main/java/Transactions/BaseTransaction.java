package Transactions;

import org.postgresql.ds.PGSimpleDataSource;
import util.TimeHelper;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public abstract class BaseTransaction {
    private DataSource ds;

    public BaseTransaction(PGSimpleDataSource datasource) {
        ds = datasource;
    }

    public abstract void parseInput(Scanner sc, String inputLine);

    public abstract void execute();
}
