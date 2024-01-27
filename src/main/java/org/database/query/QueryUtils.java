package org.database.query;

import org.database.Utils.FileTypes;
import org.database.Utils.Utils;

import java.io.*;
import java.util.*;

public class QueryUtils {

    public String primaryKeyField;
    public int primaryKeyFieldIdx;

    private boolean isTransactionBegin;
    private boolean isTransactionClose;
    private List<String> transactionQueryList = new ArrayList<>();
    public QueryUtils() {}

    public boolean isTransactionBegin() {
        return isTransactionBegin;
    }

    public void setTransactionBegin(boolean transactionBegin) {
        isTransactionBegin = transactionBegin;
    }

    public boolean isTransactionClose() {
        return isTransactionClose;
    }

    public void setTransactionClose(boolean transactionClose) {
        isTransactionClose = transactionClose;
    }

    public List<String> getTransactionQueryList() {
        return transactionQueryList;
    }

    public void setTransactionQueryList(List<String> transactionQueryList) {
        this.transactionQueryList = transactionQueryList;
    }

    /**
     Retrieves metadata about the specified table.
     @param tableName the name of the table to retrieve metadata for
     @return a map representing the metadata, with column names as keys and data types as values
     @throws IOException if an I/O error occurs during the file operations
     */
    public Map<String, String> getMetaData(String tableName) throws IOException {
        File tableMetadata = new File(Utils.getFileName(tableName, FileTypes.META_TABLE));
        Map<String, String> tableFieldMap = new LinkedHashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(tableMetadata));

        String value;
        int i = 0;
        while ((value = br.readLine()) != null) {
            List<String> field = List.of(value.split("\\|"));
            tableFieldMap.put(field.get(0), field.get(1));

            if (field.size() > 2 && field.get(2).equals("primarykey")) {
                primaryKeyField = field.get(0);
                primaryKeyFieldIdx = i;
            }
            i++;
        }
        return tableFieldMap;
    }

    public String getPrimaryKeyField() {
        return primaryKeyField;
    }
    public int getPrimaryKeyFieldIdx() {
        return primaryKeyFieldIdx;
    }

    /**
     Validates whether the given value is compatible with the specified column type.
     @param columnType the data type of the column
     @param value the value to be validated
     @return true if the value is compatible with the column type, false otherwise
     */
    private boolean tableColumnTypeValidator(String columnType, String value) {
        switch (columnType) {
            case "int" -> {
                if (intValueParser(value) != null) return true;
                return false;
            }
            case "varchar" -> {
                return true;
            }
            case "double" -> {
                if (doubleValueParser(value) != null) return true;
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     Parses the provided string and returns the corresponding integer value.
     @param str the string to be parsed as an integer
     @return the integer value parsed from the string, or null if parsing fails
     */
    public static Integer intValueParser(String str) {
        Integer retVal;
        try {
            retVal = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            retVal = null;
        }
        return retVal;
    }

    /**
     Parses the provided string and returns the corresponding double value.
     @param str the string to be parsed as a double
     @return the double value parsed from the string, or null if parsing fails
     */
    public static Double doubleValueParser(String str) {
        Double retVal;
        try {
            retVal = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            retVal = null;
        }
        return retVal;
    }

    /**
     Validates each value in the valuesList against its corresponding column data type specified in the fieldEntry map.
     @param fieldEntry the map representing the field names and their corresponding data types
     @param valuesList the list of values to be validated
     @return true if all values are compatible with their respective column data types, false otherwise
     */
    public boolean validateValueToType(Map<String, String> fieldEntry, List<String> valuesList) {
        List<Map.Entry<String, String>> entriesList = new ArrayList<>(fieldEntry.entrySet());

        for(int i = 0; i < entriesList.size(); i++) {
            String type = entriesList.get(i).getValue();
            if(!tableColumnTypeValidator(type, valuesList.get(i))) return false;
        }
        return true;
    }

    /**
     Validates whether the provided value is unique for the specified column in the given table.
     @param columnName the name of the column to validate as the primary key
     @param tableName the name of the table to validate the primary key against
     @param value the value to be validated as the primary key
     @return true if the value is unique for the specified column, false otherwise
     @throws IOException if an I/O error occurs during the file operations
     */
    public boolean validateValueForPrimaryKey(String columnName, String tableName, String value) throws IOException {
        File table = new File(Utils.getFileName(tableName, FileTypes.TABLE));

        if (!table.exists()) {
            System.out.println("File for the table " + tableName + " does not exist");
            return false;
        }
        Map<String, String> fieldEntry = getMetaData(tableName);
        try (BufferedReader br = new BufferedReader(new FileReader(table))) {
            String st;
            while ((st = br.readLine()) != null) {
                List<String> rowValues = List.of(st.split("\\|"));

                Map<String, String> row = new LinkedHashMap<>();

                for (Map.Entry<String, String> entry : fieldEntry.entrySet()) {
                    row.put(entry.getKey(), rowValues.get(row.size()));
                }

                if (row.get(columnName).equals(value)) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     Validates whether the specified fields are valid for the given column values.
     @param fields the fields to be validated in the SELECT query
     @param columnValues the map representing the column names and their corresponding values
     @return true if all fields are valid and present in the column values, false otherwise
     */
    public boolean validateSelectQueryFields(String fields, Map<String, String> columnValues) {
        if(fields.equals("*")) return true;

        List<String> selectFields = List.of(fields.split(","));
        for(String field: selectFields){
            if(!columnValues.containsKey(field.trim())){
                System.out.println("SELECT operation failed!\n" + field + " not exist!");
                return false;
            }
        }
        return true;
    }

    /**
     Evaluates the AND/OR conditions for the given row based on the provided conditions.
     @param row the map representing the row with column names as keys and row values as values
     @param conditions the string representing the AND/OR conditions to be evaluated
     @return true if the row satisfies the conditions, false otherwise
     */
    public boolean handleAndOrConditions(Map<String, String> row, String conditions) {
        if (conditions == null) {
            return true;
        }
        conditions = conditions.trim();

        if (conditions.contains("and")) {
            return handleAndConditions(row, conditions);
        } else if (conditions.contains("or")) {
            return handleOrConditions(row, conditions);
        } else {
            return handleCondition(row, conditions);
        }
    }

    private boolean handleAndConditions(Map<String, String> row, String conditions) {
        String[] conditionsArr = conditions.split("and", 2);
        String condition1 = conditionsArr[0].trim();
        String condition2 = conditionsArr[1].trim();

        return handleCondition(row, condition1) && handleCondition(row, condition2);
    }

    private boolean handleOrConditions(Map<String, String> row, String conditions) {
        String[] conditionsArr = conditions.split("or", 2);
        String condition1 = conditionsArr[0].trim();
        String condition2 = conditionsArr[1].trim();

        return handleCondition(row, condition1) || handleCondition(row, condition2);
    }

    private boolean handleCondition(Map<String, String> row, String condition) {
        String[] parts = condition.split("=", 2);
        String column = parts[0].trim();
        String value = parts[1].trim();

        return row.containsKey(column) && row.get(column).equalsIgnoreCase(value);
    }

    /**
     Copies the contents of the specified table file to another file.
     @param tableName the name of the table whose file contents are to be copied
     @throws IOException if an I/O error occurs during the file operations
     */
    public void copyAnotherFile(String tableName) throws IOException {
        FileWriter fileWriter = new FileWriter(Utils.getFileName(tableName, FileTypes.TABLE));
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        PrintWriter printWriter = new PrintWriter(bufferedWriter);

        File tempFile = new File(Utils.getFileName(tableName + "-temp", FileTypes.TABLE));
        BufferedReader br = new BufferedReader(new FileReader(tempFile));

        String string;
        while ((string = br.readLine()) != null) {
            printWriter.println(string);
        }
        printWriter.close();
        tempFile.delete();
    }

    /**

     Creates a map representing the column-value pairs for delete or update operations based on the provided row data.
     @param rowData the string representing the row data in the format of column values separated by a delimiter
     @param fieldEntry the map representing the field names and their corresponding entry values
     @return a map representing the column-value pairs for delete or update operations
     */
    public Map<String, String> creatDeleteUpdateMap(String rowData, Map<String, String> fieldEntry) {
        List<String> rowValues = List.of(rowData.split("\\|"));
        List<Map.Entry<String, String>> indexFieldMapping = new ArrayList<>(fieldEntry.entrySet());
        Map<String, String> row = new LinkedHashMap<>();

        for (int i = 0; i < rowValues.size(); i++) {
            row.put(indexFieldMapping.get(i).getKey(), rowValues.get(i));
        }
        return row;
    }
    public Map<String, String> parseColumnValues(List<String> columnValues) {
        Map<String, String> columnValueMap = new HashMap<>();

        for (String columnValue : columnValues) {
            String[] keyValue = columnValue.split("=");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            columnValueMap.put(key, value);
        }

        return columnValueMap;
    }

    public void updateRowWithColumnValues(Map<String, String> row, Map<String, String> columnValueMap) {
        for (Map.Entry<String, String> entry : columnValueMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            row.put(key, value);
        }
    }
}
