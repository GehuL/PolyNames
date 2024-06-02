package database;

import java.sql.SQLException;

public class PolyBayDatabase extends MySQLDatabase
{
    public PolyBayDatabase() throws SQLException
    {
        super("localhost", 3306, "poly_bay", "root", "");
    }
}