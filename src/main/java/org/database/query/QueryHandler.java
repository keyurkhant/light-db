package org.database.query;

import org.database.Utils.FileTypes;
import org.database.Utils.Utils;

import java.io.*;
import java.util.*;

public class QueryHandler implements IQueryHandler {

    QueryUtils queryUtils = new QueryUtils();

    /**
     Creates a database query for creating a table with the specified table name and column definitions.
     @param tableName the name of the table to be created
     @param typeColumns a string representing the column definitions with their data types and constraints
     @return true if the query is successfully created, false otherwise
     */
    @Override
    public boolean createQuery(String tableName, String typeColumns) {
        File tableFile = new File(Utils.getFileName(tableName, FileTypes.TABLE));
        if(tableFile.exists()) {
            System.out.println("Table not exists");
            return false;
        }
        try {
            tableFile.createNewFile();

            File tableMetaFile = new File(Utils.getFileName(tableName, FileTypes.META_TABLE));

            tableMetaFile.createNewFile();

            List<String> typeFields = List.of(typeColumns.split(","));
            FileWriter fileWriter = new FileWriter(tableMetaFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            for(String field: typeFields){
                field = field.trim();
                field = field.replace(' ', '|');
                printWriter.println(field);
            }
            printWriter.close();
            System.out.println("New table created: " + tableName);
            return true;
        } catch (Exception e) {
            System.out.println("Error while creating table, try again!");
            e.printStackTrace();
            return false;
        }
    }

    /**
     Creates a SELECT query for retrieving data from the specified table with the given fields and conditions.
     @param tableName the name of the table to select data from
     @param fields a string representing the fields to be included in the SELECT statement
     @param conditions a string representing the conditions to be applied in the WHERE clause
     @return true if the SELECT query is successfully created, false otherwise
     */
    @Override
    public boolean selectQuery(String tableName, String fields, String conditions) {
        File table = new File(Utils.getFileName(tableName, FileTypes.TABLE));
        if (!table.exists()) {
            System.out.println("Table " + tableName + " does not exist");
            return false;
        }
        try {
            Map<String, String> fieldEntry = queryUtils.getMetaData(tableName);
            if (!queryUtils.validateSelectQueryFields(fields, fieldEntry)) {
                return false;
            }
            BufferedReader br = new BufferedReader(new FileReader(table));
            String st;
            while ((st = br.readLine()) != null) {
                List<String> rowValues = List.of(st.split("\\|"));
                Map<String, String> row = createRowMap(fieldEntry, rowValues);
                boolean validCondition = queryUtils.handleAndOrConditions(row, conditions);

                if (validCondition) {
                    Utils.print(fields, row);
                }
            }
        } catch (Exception e) {
            System.out.println("Something went wrong!");
            return false;
        }
        return true;
    }

    /**
     Creates a row map by combining the fieldEntry map with the provided rowValues list.
     @param fieldEntry the map representing the field names and their corresponding entry values
     @param rowValues the list of values for the row in the same order as the field names
     @return a map representing the row with field names as keys and row values as values
     */
    private Map<String, String> createRowMap(Map<String, String> fieldEntry, List<String> rowValues) {
        Map<String, String> row = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : fieldEntry.entrySet()) {
            row.put(entry.getKey(), rowValues.get(row.size()));
        }
        return row;
    }

    /**
     Creates an INSERT query for inserting data into the specified table with the given values.
     @param tableName the name of the table to insert data into
     @param values a string representing the values to be inserted
     @return true if the INSERT query is successfully created, false otherwise
     */
    @Override
    public boolean insertQuery(String tableName, String values) {
        File tableFile = new File(Utils.getFileName(tableName, FileTypes.TABLE));
        if(!tableFile.exists()) {
            System.out.println("Table not exists");
            return false;
        }
        try {
            File tableMetadataFile = new File(Utils.getFileName(tableName, FileTypes.META_TABLE));
            if(!tableMetadataFile.exists()) {
                System.out.println("Table not exists");
                return false;
            }

            List<String> queryValuesList = List.of(values.split(","));
            Map<String, String> fieldEntry = queryUtils.getMetaData(tableName);

            if(fieldEntry.size() != queryValuesList.size()) {
                System.out.println("INSERT operation failed!.\nRequires " + fieldEntry.size() + " values instead of "
                        + queryValuesList.size());
                return false;
            }

            if(!queryUtils.validateValueToType(fieldEntry, queryValuesList)) {
                System.out.println("INSERT operation failed!.\nValues may contains invalid type.");
                return false;
            }

            FileWriter fileWriter = new FileWriter(Utils.getFileName(tableName, FileTypes.TABLE), true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter);

            if(queryUtils.getPrimaryKeyField() != null &&
                    !queryUtils.validateValueForPrimaryKey(queryUtils.getPrimaryKeyField(), tableName, queryValuesList.get(queryUtils.getPrimaryKeyFieldIdx()).trim())) {
                System.out.println("INSERT operation failed!\n Duplicate primary key values.");
                return false;
            }

            printWriter.println(values.replace(',', '|'));
            printWriter.close();
            return true;
        } catch (Exception e) {
            System.out.println("Something went wrong!");
            return false;
        }
    }

    /**
     Creates an UPDATE query for updating data in the specified table with the given values and conditions.
     @param tableName the name of the table to update data in
     @param values a string representing the column-value pairs to be updated
     @param conditions a string representing the conditions to be applied in the WHERE clause
     @return true if the UPDATE query is successfully created, false otherwise
     */
    @Override
    public boolean updateQuery(String tableName, String values, String conditions) {
        File tableFile = new File(Utils.getFileName(tableName, FileTypes.TABLE));
        if (!tableFile.exists()) {
            System.out.println("UPDATE operation failed!\nTable does not exist");
            return false;
        }

        File tableMetadataFile = new File(Utils.getFileName(tableName, FileTypes.META_TABLE));
        if (!tableMetadataFile.exists()) {
            System.out.println("UPDATE operation failed!\nTable metadata does not exist");
            return false;
        }

        try {
            List<String> valuesList = List.of(values.split(","));
            Map<String, String> fieldEntry = queryUtils.getMetaData(tableName);
            Map<String, String> columnValueMap = queryUtils.parseColumnValues(valuesList);

            String primaryKeyNewValue = columnValueMap.get(queryUtils.getPrimaryKeyField());
            if (primaryKeyNewValue != null) {
                if (!queryUtils.validateValueForPrimaryKey(queryUtils.getPrimaryKeyField(), tableName, primaryKeyNewValue)) {
                    System.out.println("UPDATE operation failed!\nDuplicate primary key values");
                    return false;
                }
            }

            File tempTableFile = new File(Utils.getFileName(tableName + "-temp", FileTypes.TABLE));
            try (BufferedReader br = new BufferedReader(new FileReader(tableFile));
                 PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(tempTableFile)))) {

                String st;
                while ((st = br.readLine()) != null) {
                    Map<String, String> row = queryUtils.creatDeleteUpdateMap(st, fieldEntry);
                    boolean conditionStatus = queryUtils.handleAndOrConditions(row, conditions);

                    StringBuilder output = new StringBuilder(st);
                    if (conditionStatus) {
                        queryUtils.updateRowWithColumnValues(row, columnValueMap);

                        StringBuilder temp = new StringBuilder();
                        for (Map.Entry<String, String> entry : row.entrySet()) {
                            String value = entry.getValue();
                            temp.append(value).append("|");
                        }
                        temp.deleteCharAt(temp.length() - 1);
                        output = temp;
                    }
                    printWriter.println(output);
                }
            }

            queryUtils.copyAnotherFile(tableName);
        } catch (Exception e) {
            System.out.println("Something went wrong!");
            return false;
        }

        return true;
    }

    /**
     Creates a DELETE query for deleting data from the specified table based on the given conditions.
     @param tableName the name of the table to delete data from
     @param conditions a string representing the conditions to be applied in the WHERE clause
     @return true if the DELETE query is successfully created, false otherwise
     */
    @Override
    public boolean deleteQuery(String tableName, String conditions) {
        File tableFile = new File(Utils.getFileName(tableName, FileTypes.TABLE));
        if (!tableFile.exists()) {
            System.out.println("DELETE operation failed!\nTable does not exist");
            return false;
        }

        File tableMetadataFile = new File(Utils.getFileName(tableName, FileTypes.META_TABLE));
        if (!tableMetadataFile.exists()) {
            System.out.println("DELETE operation failed!\nTable metadata does not exist");
            return false;
        }

        try {
            Map<String, String> fieldEntry = queryUtils.getMetaData(tableName);
            File tempTableFile = new File(Utils.getFileName(tableName + "-temp", FileTypes.TABLE));

            deleteRowsBasedOnConditions(tableFile, tempTableFile, fieldEntry, conditions);
            queryUtils.copyAnotherFile(tableName);
        } catch (Exception e) {
            System.out.println("Something went wrong!");
            return false;
        }

        return true;
    }

    /**
     Deletes rows from the table file based on the specified conditions.
     @param tableFile the file representing the table to delete rows from
     @param tempTableFile the temporary file used for writing the modified table data
     @param fieldEntry the map representing the field names and their corresponding entry values
     @param conditions the string representing the conditions to be applied in the WHERE clause
     @throws IOException if an I/O error occurs during the file operations
     */
    private void deleteRowsBasedOnConditions(File tableFile, File tempTableFile, Map<String, String> fieldEntry, String conditions) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(tableFile));
             PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(tempTableFile)))) {

            String string;
            while ((string = br.readLine()) != null) {
                Map<String, String> row = queryUtils.creatDeleteUpdateMap(string, fieldEntry);
                boolean conditionStatus = queryUtils.handleAndOrConditions(row, conditions);
                if (!conditionStatus) {
                    printWriter.println(string);
                }
            }
        }
    }
}
