package org.database.auth;

import org.database.Utils.Utils;
import org.database.user.User;
import org.database.user.UserManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 The Auth class implements the IAuth interface, providing authentication functionality.
 This class can be used as a base class for implementing authentication mechanisms in an application.
 It should override the methods defined in the IAuth interface to provide the necessary implementation.
 */
public class Auth implements IAuth {
    public Auth() {
    }

    /**
     Private method used for authentication, which verifies the credentials of a user.
     @param user the User object containing the user's credentials
     @return true if the authentication is successful, false otherwise
     */
    private boolean authenticate(User user) {
        if(user == null) return false;
        if(user.getUsername() == null || user.getPassword() == null) return false;
        if(user.getUsername().isEmpty() || user.getPassword().isEmpty()) return false;
        UserManager userManager = new UserManager();
        User existingUser = userManager.getUserByUsername(user.getUsername());
        if(existingUser == null) return false;
        try {
            String hashValue = Utils.encrypt(user.getPassword().trim());
            if(!hashValue.equals(existingUser.getPassword()))
                return false;

            Scanner scanner = new Scanner(System.in);
            System.out.println("Please answer of this question: " + existingUser.getQuestion());
            String answer = scanner.nextLine();
            String hashAnswerValue = Utils.encrypt(answer);
            if(!hashAnswerValue.equals(existingUser.getAnswer())){
                System.out.println("Invalid security answer!");
                return false;
            }
        }
        catch (Exception e) {
            System.out.println("Something went wrong!");
            return false;
        }
        return true;
    }

    /**
     Private method used for user registration, which registers a new user.
     @param user the User object containing the user's information
     @return an integer value representing the registration status or result
     */
    private int register(User user) {
        UserManager userManager = new UserManager();
        PrintWriter usersFile = null;
        try {
            User existingUser = userManager.getUserByUsername(user.getUsername());
            if (existingUser != null) return 0;

            File file = new File("database/users.txt");
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            usersFile = new PrintWriter(br);
            String userString = user.getUsername() + "," + Utils.encrypt(user.getPassword()) + "," + user.getQuestion() + "," + Utils.encrypt(user.getAnswer());
            usersFile.println(userString);
        } catch (Exception e) {
            System.out.println(e);
            return 2;
        } finally {
            if(usersFile != null) usersFile.close();
        }
        return 1;
    }

    /**
     Public method used for user login, which prompts the user for login credentials and performs the login process.
     @param loginInput the Scanner object used for reading user input during the login process
     @return true if the login is successful, false otherwise
     */
    @Override
    public boolean loginUser(Scanner loginInput) {
        String username;
        String password;
        System.out.print("Enter your username: ");
        username = loginInput.nextLine();
        System.out.print("Enter your password: ");
        password = loginInput.nextLine();
        boolean status = authenticate(new User(username, password));
        if(status == true){
            System.out.println("User logged in successfully");
        } else {
            System.out.println("Invalid credentials");
        }
        return status;
    }

    /**
     Public method used for user registration, which prompts the user for registration information and performs the registration process.
     @param registerInput the Scanner object used for reading user input during the registration process
     @return true if the registration is successful, false otherwise
     */
    @Override
    public boolean registerUser(Scanner registerInput) {
        String username;
        String password;
        String question;
        String answer;
        System.out.print("Enter your username: ");
        username = registerInput.nextLine();
        System.out.print("Enter your password: ");
        password = registerInput.nextLine();
        System.out.print("Enter your security question: ");
        question = registerInput.nextLine();
        System.out.print("Enter your security answer: ");
        answer = registerInput.nextLine();

        User user = new User(username, password, question, answer);
        int isRegistered = register(user);
        if (isRegistered == 1) {
            System.out.println("User with username " + username + " successfully registered!");
        }
        else if (isRegistered == 0) {
            System.out.println("Opps! user already exists.");
        } else {
            System.out.println("Registration failed. Something went wrong");
        }
        return isRegistered == 1;
    }
}
