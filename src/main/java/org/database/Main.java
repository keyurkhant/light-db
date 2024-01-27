package org.database;

import org.database.auth.Auth;
import org.database.query.IQueryManager;
import org.database.query.QueryManager;
import org.database.query.QueryUtils;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        QueryUtils queryUtils = new QueryUtils();
        Scanner input = new Scanner(System.in);
        int choice;
        boolean isAuthenticated = false;
        boolean isRegistered = false;
        Auth authentication = new Auth();

        while (!isAuthenticated || !isRegistered) {
            System.out.println("Welcome to the LIGHT DB");
            System.out.println("1. Login");
            System.out.println("2. Registration");
            System.out.print("Enter your choice: ");
            choice = Integer.parseInt(input.nextLine());
            switch (choice) {
                case 1:
                    isAuthenticated = authentication.loginUser(input);
                    if(isAuthenticated) isRegistered = true;
                    break;
                case 2:
                    isRegistered = authentication.registerUser(input);
                    break;
                default:
                    break;
            }
        }

        if(isAuthenticated) {
            while(true) {
                System.out.print("QUERY> ");
                String query = input.nextLine();
                IQueryManager queryManager = new QueryManager();

                int result = queryManager.runner(query, queryUtils);
                if(result == 0) System.out.println("Invalid Query!\nPlease try again.");
            }
        }
    }
}