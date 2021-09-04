package org.jpwh.shared.trigger;

import org.h2.tools.TriggerAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Runs on UPDATE and sets the current timestamp on a LASTMODIFIED column.
 */
public class UpdateLastModifiedTrigger extends TriggerAdapter {

    // Some assumptions about column names
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_LASTMODIFIED = "LASTMODIFIED";

    protected String tableName;

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {
        super.init(conn, schemaName, triggerName, tableName, before, type);
        this.tableName = tableName;
    }

    @Override
    public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
        if (newRow == null ) return; // This is a DELETE

        if (oldRow != null) {
            // This is an UPDATE
            // Yes, our UPDATE statement below will fire this trigger again, filter that!
            // TODO: Maybe on day H2 will have a better trigger API, http://groups.google.com/group/h2-database/browse_thread/thread/f8cdaa1ae0da7448
            Timestamp oldTimestamp = oldRow.getTimestamp(COLUMN_LASTMODIFIED);
            Timestamp newTimestamp = newRow.getTimestamp(COLUMN_LASTMODIFIED);
            if (oldTimestamp == null)
                return;
            if (oldTimestamp.getTime() != newTimestamp.getTime())
                return;
        }

        PreparedStatement statement = conn.prepareStatement(
                "update " + tableName +
                        " set " + COLUMN_LASTMODIFIED + " = current_timestamp()" +
                        " where " + COLUMN_ID + " = ?"
        );
        Long id = newRow.getLong(COLUMN_ID);
        statement.setLong(1, id);
        statement.execute();
    }
}
