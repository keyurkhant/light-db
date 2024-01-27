# Lightweight Database Management System using Java

The goal of this project is to create a simple database management system (DBMS) using Java.
The DBMS should be lightweight and able to handle different types of queries, like creating and
modifying the database structure (DDL) and manipulating data (DML). The data will be stored in
TXT files, and there will be a security feature for authentication. An interesting addition is the
transaction processing feature, which ensures that data changes are consistent, isolated, durable,
and atomic. This report will provide an overview of the implemented DBMS, highlighting its main
features, design choices, and functionality.

File structure:
```
.
└── LightDB/
    ├── database/
    │ ├── tables/
    │ │   └── employee.txt
    │ ├── tables-metadata/
    │ │   └── employee_metadata.txt
    │ └── users.txt
    └── src/
        └── main/
            └── java/
                └── database/
                    ├── auth/
                    │   ├── IAuth.java
                    │   └── Auth.java
                    ├── query/
                    │   ├── IQueryHandler.java
                    │   ├── IQueryManager.java
                    │   ├── QueryHandler.java
                    │   ├── QueryManager.java
                    │   └── QueryUtils.java
                    ├── user/
                    │   ├── User.java
                    │   └── UserManager.java
                    ├── utils/
                    │   └── Utils.java
                    └── Main.java
```


Here, I have followed the above structure in which code is segregated into various classes and all data
is stored in a database directory in LightDB.

1. The UserManager class is a part of the lightweight database management system (DBMS)
implementation in Java. It is responsible for managing user-related operations and authentication
within the DBMS.
2. The QueryManager class is a key component of the lightweight database management system
(DBMS) implementation in Java. It handles the execution and management of different types of
queries within the DBMS.
3. The QueryHandler class is a part of the lightweight database management system (DBMS)
implementation in Java. It is responsible for handling and executing individual queries received by the
system. 
4. The QueryHandler class interacts with the QueryManager and other relevant components to
process the query. It translates the query into appropriate database operations and ensures their
successful execution.
5. The Auth class interacts with the user management module, such as the UserManager, to validate
user credentials during the login process. It securely stores and compares user passwords to ensure
authentication accuracy.
6. In the database directory, users.txt contains all user credentials. Furthermore, tables contain all
database tables and table-metadata contains column fields of the specific tables.


Note: Throughout the application development, I have used Java W3 documentation pattern to add
class and method commenting.