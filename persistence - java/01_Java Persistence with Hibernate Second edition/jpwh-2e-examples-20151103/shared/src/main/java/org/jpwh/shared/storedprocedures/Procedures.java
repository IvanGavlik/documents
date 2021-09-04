package org.jpwh.shared.storedprocedures;

import org.h2.tools.SimpleResultSet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author Christian Bauer
 */
public class Procedures {

    public static ResultSet simple() throws SQLException {
        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("ID", Types.INTEGER, 10, 0);
        rs.addColumn("NAME", Types.VARCHAR, 255, 0);
        rs.addRow(0, "Hello");
        rs.addRow(1, "World");
        return rs;
    }

    public static ResultSet loadItems(Connection connection) throws SQLException {
        return connection.createStatement().executeQuery(
            "select * from ITEM"
        );
    }

    public static ResultSet loadItem(Connection connection, Long itemId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
            "select * from ITEM where ID = ?"
        );
        statement.setLong(1, itemId);
        return statement.executeQuery();
    }

    public static int updateItem(Connection connection, Long itemId, String name) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
            "update ITEM set NAME = ? where ID = ?"
        );
        statement.setString(1, name);
        statement.setLong(2, itemId);
        return statement.executeUpdate();
    }

    /* Derby */
    public static void loadItems(ResultSet[] resultSets) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:default:connection");
        resultSets[0] = connection.createStatement().executeQuery(
            "select * from ITEM"
        );
    }

    public static void loadItem(Integer itemId, ResultSet[] resultSets) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:default:connection");
        PreparedStatement statement =
            connection.prepareStatement(
                "select * from ITEM where ID = ?"
            );
        statement.setInt(1, itemId);
        resultSets[0] = statement.executeQuery();
    }
}
