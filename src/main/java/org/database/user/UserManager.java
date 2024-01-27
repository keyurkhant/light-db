package org.database.user;

import org.database.Utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private User user;
    public UserManager() {
    }

    /**
     Retrieves all users from the data source.
     @return a list of User objects representing all the users
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        try {
            File userFile = new File("database/users.txt");
            userFile.createNewFile();
            BufferedReader allUsersFile = new BufferedReader(new FileReader(userFile));
            List<String> allUsersList = Utils.readLinesFromFile(allUsersFile);
            for(String user: allUsersList) {
                List<String> userData = List.of(user.split(","));
                userList.add(new User(userData.get(0), userData.get(1), userData.get(2), userData.get(3)));
            }
        } catch (IOException e) {
            return null;
        }
        return userList;
    }

    /**
     Retrieves a user from the data source based on the provided username.
     @param username the username of the user to retrieve
     @return the User object corresponding to the given username, or null if not found
     */
    public User getUserByUsername(String username) {
        List<User> userList = getAllUsers();
        User resultedUser = null;
        for(User user: userList) {
            if(user.getUsername().equals(username)) {
                resultedUser = user;
                break;
            }
        }
        return resultedUser;
    }
}
