package org.database.query;

import java.io.IOException;

public interface IQueryHandler {
    public boolean createQuery(String tableName, String typeColumns);
    public boolean selectQuery(String tableName, String fields, String conditions) throws IOException;
    public boolean insertQuery(String tableName, String values);
    public boolean updateQuery(String tableName, String values, String conditions);
    public boolean deleteQuery(String tableName, String conditions);
}
