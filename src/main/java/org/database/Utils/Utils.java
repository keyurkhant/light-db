package org.database.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


/**
 The Utils class provides a collection of utility methods and functions
 that can be used across different parts of an application.
 */
public class Utils {
    /**
     Reads lines from a file using a BufferedReader and returns them as a list of strings.
     @param bufferedReaderFile the BufferedReader object used to read from the file
     @return a list of strings representing the lines read from the file
     */
    public static List<String> readLinesFromFile(BufferedReader bufferedReaderFile) {
        List<String> fileContent = new ArrayList<>();
        if(bufferedReaderFile == null) return null;
        String line;
        while(true) {
            try {
                if((line = bufferedReaderFile.readLine()) == null) {
                    break;
                }
                fileContent.add(line);
            } catch (IOException e) {
                return null;
            }
        }
        return fileContent;
    }

    /**
     Encrypts a given value using the MD5 encryption algorithm.
     Reference: https://www.baeldung.com/java-md5
     @param value the value to be encrypted
     @return the encrypted string representation of the value
     */
    public static String encrypt(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytesOfMessage = value.getBytes(StandardCharsets.UTF_8);
            byte[] theMD5digest = md.digest(bytesOfMessage);
            BigInteger number = new BigInteger(1, theMD5digest);
            String hashValue = number.toString(16);
            while (hashValue.length() < 32) {
                hashValue = "0" + hashValue;
            }
            return hashValue;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     Checks if a given string is a valid string.
     @param value the string to be checked
     @return true if the string is valid, false otherwise
     */
    public static boolean isValidString(String value) {
        return value != null && !value.isEmpty() && !value.isBlank();
    }

    /**
     Generates a file name based on the given table name and file type.
     @param tableName the name of the table or data source
     @param type the file type enum representing the desired file extension
     @return a string representing the generated file name
     */
    public static String getFileName(String tableName, FileTypes type) {
        if(type.equals(FileTypes.TABLE)) {
            return "database/tables/" + tableName + ".txt";
        } else if(type.equals(FileTypes.META_TABLE)) {
            return "database/tables-metadata/" + tableName + "_metadata.txt";
        }
        return "";
    }

    /**
     Prints the specified fields and corresponding values from a row in a tabular format.
     @param fields the comma-separated string of field names to be printed
     @param row the map representing the row data, with field names as keys and values as values
     */
    public static void print(String fields, Map<String, String> row){
        if (fields.isEmpty()) {
            System.out.println("No data available!");
        }
        List<String> selectFields = List.of(fields.split(","));
        StringJoiner output = new StringJoiner(" | ");

        if (fields.equals("*")) {
            for (String value : row.values()) {
                output.add(value);
            }
        } else {
            for (String field : selectFields) {
                String rowValue = row.get(field.trim());
                output.add(rowValue);
            }
        }
        System.out.println(output);
    }
}
