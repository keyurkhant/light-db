package org.database.auth;

import java.util.Scanner;

public interface IAuth {
    boolean loginUser(Scanner loginInput);

    boolean registerUser(Scanner registerInput);
}
