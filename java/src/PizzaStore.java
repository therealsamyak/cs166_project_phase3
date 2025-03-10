/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
         new InputStreamReader(System.in));

   private String currentUserLogin = null;
   private String currentUserRole = null;

   public void setCurrentUser(String login, String role) {
      this.currentUserLogin = login;
      this.currentUserRole = role;
   }

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try {
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      } catch (Exception e) {
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      } // end catch
   }// end PizzaStore

   /**
    * Method to execute an update SQL statement. Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate(String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the update instruction
      stmt.executeUpdate(sql);

      // close the instruction
      stmt.close();
   }// end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()) {
         if (outputHeader) {
            for (int i = 1; i <= numCol; i++) {
               System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i = 1; i <= numCol; ++i)
            System.out.print(rs.getString(i) + "\t");
         System.out.println();
         ++rowCount;
      } // end while
      stmt.close();
      return rowCount;
   }// end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result = new ArrayList<List<String>>();
      while (rs.next()) {
         List<String> record = new ArrayList<String>();
         for (int i = 1; i <= numCol; ++i)
            record.add(rs.getString(i));
         result.add(record);
      } // end while
      stmt.close();
      return result;
   }// end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      int rowCount = 0;

      // iterates through the result set and count nuber of results.
      while (rs.next()) {
         rowCount++;
      } // end while
      stmt.close();
      return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement();

      ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup() {
      try {
         if (this._connection != null) {
            this._connection.close();
         } // end if
      } catch (SQLException e) {
         // ignored.
      } // end try
   }// end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login
    *             file>
    */
   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println(
               "Usage: " +
                     "java [-classpath <classpath>] " +
                     PizzaStore.class.getName() +
                     " <dbname> <port> <user>");
         return;
      } // end if

      Greeting();
      PizzaStore esql = null;
      try {
         // use postgres JDBC driver.
         Class.forName("org.postgresql.Driver").newInstance();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore(dbname, dbport, user, "");
         // nobody is logged in yet
         esql.setCurrentUser("", "");

         boolean keepon = true;
         while (keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");

            switch (readChoice()) {
               case 1:
                  CreateUser(esql);
                  break;
               case 2:
                  LogIn(esql);
                  break;
               case 9:
                  keepon = false;
                  break;
               default:
                  System.out.println("Unrecognized choice!");
                  break;
            }// end switch
            if (!esql.currentUserRole.isEmpty()) {
               boolean usermenu = true;
               while (usermenu) {
                  System.out.println("");
                  System.out.println("MAIN MENU");
                  System.out.println("---------");
                  System.out.println("1. View Profile");
                  System.out.println("2. Update Profile");
                  System.out.println("3. View Menu");
                  System.out.println("4. Place Order"); // make sure user specifies which store
                  System.out.println("5. View Full Order ID History");
                  System.out.println("6. View Past 5 Order IDs");
                  System.out.println("7. View Order Information"); // user should specify orderID and then be able to
                                                                   // see detailed information about the order
                  System.out.println("8. View Stores");

                  if (!esql.currentUserRole.equals("customer")) {
                     // **the following functionalities should only be able to be used by drivers &
                     // managers**
                     System.out.println("9. Update Order Status");
                  }

                  // **the following functionalities should ony be able to be used by managers**
                  if (esql.currentUserRole.equals("manager")) {
                     System.out.println("10. Update Menu");
                     System.out.println("11. Update User");
                  }

                  System.out.println(".........................");
                  System.out.println("20. Log out");
                  switch (readChoice()) {
                     case 1:
                        viewProfile(esql);
                        break;
                     case 2:
                        updateProfile(esql);
                        break;
                     case 3:
                        viewMenu(esql);
                        break;
                     case 4:
                        placeOrder(esql);
                        break;
                     case 5:
                        viewAllOrders(esql);
                        break;
                     case 6:
                        viewRecentOrders(esql);
                        break;
                     case 7:
                        viewOrderInfo(esql);
                        break;
                     case 8:
                        viewStores(esql);
                        break;
                     case 9:
                        if (!esql.currentUserRole.equals("customer")) {
                           updateOrderStatus(esql);
                        } else {
                           System.out
                                 .println("Unauthorized access! Only drivers and managers can update order status.");
                        }
                        break;
                     case 10:
                        if (esql.currentUserRole.equals("manager")) {
                           updateMenu(esql);
                        } else {
                           System.out.println("Unauthorized access! Only managers can update the menu.");
                        }
                        break;
                     case 11:
                        if (esql.currentUserRole.equals("manager")) {
                           updateUser(esql);
                        } else {
                           System.out.println("Unauthorized access! Only managers can update users.");
                        }
                        break;
                     case 20:
                        usermenu = false;
                        esql.setCurrentUser("", "");
                        break;
                     default:
                        System.out.println("Unrecognized choice!");
                        break;
                  }
               }
            }
         } // end while
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         // make sure to cleanup the created table and close the connection.
         try {
            if (esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup();
               System.out.println("Done\n\nBye !");
            } // end if
         } catch (Exception e) {
            // ignored.
         } // end try
      } // end try
   }// end main

   public static void Greeting() {
      System.out.println(
            "\n\n*******************************************************\n" +
                  "              User Interface      	               \n" +
                  "*******************************************************\n");
   }// end Greeting

   /*
    * Reads the users choice given from the keyboard
    * 
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         } catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         } // end try
      } while (true);
      return input;
   }// end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql) {
      try {
         System.out.println("");
         System.out.println("CREATE USER");
         System.out.println("-------------");
         System.out.print("Enter your login: ");
         String login = in.readLine().trim();

         System.out.print("Enter your password: ");
         String password = in.readLine().trim();

         System.out.print("Enter your phone number: ");
         String phoneNum = in.readLine().trim();

         String role = "customer";
         String favoriteItems = "NULL";

         try {
            String query = String.format(
                  "INSERT INTO Users (login, password, role, favoriteItems, phoneNum) " +
                        "VALUES ('%s', '%s', '%s', NULL, '%s');",
                  login, password, role, phoneNum);

            esql.executeUpdate(query);
            System.out.println("User created successfully in the database!");
            System.out.println("\n");
         } catch (SQLException e) {
            System.err.println("Error inserting user into the database: " + e.getMessage());
         }
      } catch (Exception e) {
         System.err.println("Error reading input: " + e.getMessage());
      }
   }// end CreateUser

   /*
    * Check log in credentials for an existing user
    * 
    * @return User login or null if the user does not exist or credentials are
    * invalid
    **/
   public static void LogIn(PizzaStore esql) {
      try {
         System.out.println("");
         System.out.println("LOGIN");
         System.out.println("-------------");
         System.out.print("Enter your login: ");
         String login = in.readLine().trim();

         System.out.print("Enter your password: ");
         String password = in.readLine().trim();

         String query = String.format(
               "SELECT login, role FROM Users WHERE TRIM(login) = TRIM('%s') AND TRIM(password) = TRIM('%s');",
               login, password);

         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.size() > 0) {
            String userLogin = result.get(0).get(0).trim();
            String role = result.get(0).get(1).trim();

            System.out.println("Login successful!");
            System.out.println("Welcome, " + userLogin + "! Your role is: " + role);
            System.out.println("");

            // Set the current user in the PizzaStore instance
            esql.setCurrentUser(userLogin, role);
            return;

         } else {
            System.out.println("Invalid login or password. Please try again.\n");
            return;
         }
      } catch (Exception e) {
         System.err.println("Error during login: " + e.getMessage());
         return;
      }
   }// end

   // Rest of the functions definition go in here

   // view profile
   public static void viewProfile(PizzaStore esql) {
      try {
         if (esql.currentUserLogin.isEmpty()) {
            System.out.println("No user is currently logged in.");
            return;
         }

         String query = String.format(
               "SELECT login, role, phoneNum, favoriteItems FROM Users WHERE login = '%s' AND role = '%s';",
               esql.currentUserLogin, esql.currentUserRole);

         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.isEmpty()) {
            System.out.println("No profile found for the user: " + esql.currentUserLogin);
         } else {
            // Display profile details
            List<String> profile = result.get(0);
            System.out.println("");
            System.out.println("USER PROFILE");
            System.out.println("-------------");
            System.out.println("Login: " + profile.get(0));
            System.out.println("Role: " + profile.get(1));
            System.out.println("Phone Number: " + profile.get(2));
            System.out.println("Favorite Items: " + (profile.get(3) != null ? profile.get(3) : "None"));
            System.out.println("-------------");
         }
      } catch (SQLException e) {
         System.err.println("Error retrieving user profile: " + e.getMessage());
      }
   }

   // update profile
   public static void updateProfile(PizzaStore esql) {
      try {
         if (esql.currentUserLogin.isEmpty()) {
            System.out.println("You must be logged in to update your profile.");
            return;
         }

         while (true) {
            System.out.println("");
            System.out.println("UPDATE PROFILE");
            System.out.println("-------------");
            System.out.println("0. View profile");
            System.out.println("1. Update password");
            System.out.println("2. Update phone number");
            System.out.println("3. Update favorite items");
            System.out.println(".........................");
            System.out.println("4. Go back");

            switch (readChoice()) {
               case 0:
                  viewProfile(esql);
                  break;
               case 1:
                  System.out.print("Enter new password: ");
                  String newPassword = in.readLine().trim();
                  String passwordQuery = String.format(
                        "UPDATE Users SET password = '%s' WHERE login = '%s'",
                        newPassword, esql.currentUserLogin);
                  esql.executeUpdate(passwordQuery);
                  System.out.println("Password updated successfully!");
                  break;
               case 2:
                  System.out.print("Enter new phone number: ");
                  String newPhone = in.readLine().trim();
                  String phoneQuery = String.format(
                        "UPDATE Users SET phoneNum = '%s' WHERE login = '%s'",
                        newPhone, esql.currentUserLogin);
                  esql.executeUpdate(phoneQuery);
                  System.out.println("Phone number updated successfully!");
                  break;
               case 3:
                  System.out.print("Enter your favorite items (comma-separated): ");
                  String favorites = in.readLine().trim();
                  String favoritesQuery = String.format(
                        "UPDATE Users SET favoriteItems = '%s' WHERE login = '%s'",
                        favorites, esql.currentUserLogin);
                  esql.executeUpdate(favoritesQuery);
                  System.out.println("Favorite items updated successfully!");
                  break;
               case 4:
                  return;
               default:
                  System.out.println("Unrecognized choice!");
            }
         }
      } catch (Exception e) {
         System.err.println("Error updating profile: " + e.getMessage());
      }
   }

   // view menu
   public static void viewMenu(PizzaStore esql) {
      try {
         // Variables for filters
         String currentTypeFilter = "";
         String currentPriceFilter = "";
         String currentSortOrder = ""; // Add a new filter for price sorting

         while (true) {
            System.out.println("");
            System.out.println("STORE MENU");
            System.out.println("-------------");

            System.out.println("Current Filters:");
            System.out.println("Type: " + (currentTypeFilter.isEmpty() ? "Any" : currentTypeFilter));
            System.out.println("Price: " + (currentPriceFilter.isEmpty() ? "Any" : currentPriceFilter));
            System.out.println("Sort: " + (currentSortOrder.isEmpty() ? "None" : currentSortOrder));

            System.out.println("");
            System.out.println("0. View items (w/ filters)");
            System.out.println("1. Filter by type");
            System.out.println("2. Filter by price (maximum)");
            System.out.println("3. Sort by price (Lowest to Highest)");
            System.out.println("4. Sort by price (Highest to Lowest)");
            System.out.println("5. Reset all filters");
            System.out.println(".........................");
            System.out.println("6. Go back");

            switch (readChoice()) {
               case 0:
                  // Build the query
                  String query = "SELECT itemName, ingredients, typeOfItem, price, description FROM Items";
                  boolean hasWhereClause = false;

                  if (!currentTypeFilter.isEmpty()) {
                     query += " WHERE TRIM(typeOfItem) = \'" + currentTypeFilter.trim() + "\'";
                     hasWhereClause = true;
                  }
                  if (!currentPriceFilter.isEmpty()) {
                     query += (hasWhereClause ? " AND" : " WHERE") + " price <= " + currentPriceFilter;
                     hasWhereClause = true;
                  }
                  if (!currentSortOrder.isEmpty()) {
                     query += " ORDER BY price " + currentSortOrder;
                  }

                  // Execute and display results
                  List<List<String>> result = esql.executeQueryAndReturnResult(query);
                  if (result.isEmpty()) {
                     System.out.println("\nNo items available in the menu. Please select a different filter.");
                  } else {

                     System.out.println("");
                     System.out.println("FILTERED MENU");
                     System.out.println("-------------");
                     for (List<String> item : result) {
                        String itemName = item.get(0);
                        String ingredients = item.get(1);
                        String typeOfItem = item.get(2);
                        String price = item.get(3);
                        String description = item.get(4);

                        System.out.println("Item: " + itemName);
                        System.out.println("Ingredients: " + ingredients);
                        System.out.println("Type: " + typeOfItem);
                        System.out.println("Price: $" + price);
                        System.out
                              .println("Description: " + (description != null && !description.isEmpty() ? description
                                    : "No description available."));
                        System.out.println("-----------");
                     }
                  }
                  break;
               case 1:
                  // Filter by type
                  System.out.print("Enter type to filter (e.g., 'drinks', 'sides'): ");
                  String type = in.readLine().trim();
                  currentTypeFilter = type;
                  System.out.println("Filter set to type: " + type);
                  break;
               case 2:
                  // Filter by price
                  System.out.print("Enter maximum price to filter (e.g., 10.00): ");
                  String price = in.readLine().trim();
                  currentPriceFilter = price;
                  System.out.println("Filter set to price: $" + price);
                  break;
               case 3:
                  // Sort by price (Lowest to Highest)
                  currentSortOrder = "ASC";
                  System.out.println("Sorting by price: Lowest to Highest");
                  break;
               case 4:
                  // Sort by price (Highest to Lowest)
                  currentSortOrder = "DESC";
                  System.out.println("Sorting by price: Highest to Lowest");
                  break;
               case 5:
                  // Reset all filters
                  currentPriceFilter = "";
                  currentTypeFilter = "";
                  currentSortOrder = "";
                  System.out.println("Filters reset.");
                  break;
               case 6:
                  return;
               default:
                  System.out.println("Unrecognized choice!");
            }
         }
      } catch (Exception e) {
         System.err.println("Error viewing menu: " + e.getMessage());
      }
   }

   // place order
   public static void placeOrder(PizzaStore esql) {
      try {
         if (esql.currentUserLogin.isEmpty()) {
            System.out.println("You must be logged in to place an order.");
            return;
         }

         System.out.println("\nAVAILABLE STORES");
         System.out.println("----------------");

         String storeQuery = "SELECT storeID, address, city, state, isOpen FROM Store ORDER BY isOpen DESC";
         List<List<String>> stores = esql.executeQueryAndReturnResult(storeQuery);

         if (stores.isEmpty()) {
            System.out.println("No stores available to order from.");
            return;
         }

         List<Integer> openStoreIDs = new ArrayList<>();
         for (List<String> store : stores) {
            int storeID = Integer.parseInt(store.get(0));
            String address = store.get(1);
            String city = store.get(2);
            String state = store.get(3);
            String isOpen = store.get(4);

            System.out.printf("Store ID: %d - %s, %s, %s - %s\n",
                  storeID, address, city, state,
                  (isOpen.equals("yes") ? "OPEN" : "CLOSED"));

            if (isOpen.equals("yes")) {
               openStoreIDs.add(storeID);
            }
         }

         if (openStoreIDs.isEmpty()) {
            System.out.println("Sorry, no stores are currently open for ordering.");
            return;
         }

         System.out.print("\nEnter the Store ID you want to order from (q to quit): ");
         String idChoice = in.readLine().trim();
         if (idChoice.equals("q")) {
            return;
         }
         int selectedStoreID = Integer.parseInt(idChoice);

         if (!openStoreIDs.contains(selectedStoreID)) {
            System.out.println("Invalid store selection or the store is closed.");
            return;
         }

         String maxOrderIDQuery = "SELECT MAX(orderID) FROM FoodOrder";
         int newOrderID = 10000;

         List<List<String>> maxIDResult = esql.executeQueryAndReturnResult(maxOrderIDQuery);
         if (!maxIDResult.isEmpty() && maxIDResult.get(0).get(0) != null) {
            newOrderID = Integer.parseInt(maxIDResult.get(0).get(0)) + 1;
         }

         String initOrderQuery = String.format(
               "INSERT INTO FoodOrder (orderID, login, storeID, orderTimestamp, totalPrice, orderStatus) " +
                     "VALUES (%d, '%s', %d, TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')::timestamp, 0.00, 'incomplete')",
               newOrderID, esql.currentUserLogin, selectedStoreID);

         esql.executeUpdate(initOrderQuery);
         System.out.println("\nStarted a new order with ID: " + newOrderID);

         double totalPrice = 0.0;
         boolean continueShopping = true;

         while (continueShopping) {
            System.out.println("\nMENU ITEMS");
            System.out.println("----------");

            String menuQuery = String.format(
                  "SELECT itemName, price, ingredients, typeOfItem FROM Items " +
                        "ORDER BY typeOfItem, itemName");

            List<List<String>> menuItems = esql.executeQueryAndReturnResult(menuQuery);

            if (menuItems.isEmpty()) {
               System.out.println("No items available on the menu.");
               String deleteOrderQuery = String.format("DELETE FROM FoodOrder WHERE orderID = %d", newOrderID);
               esql.executeUpdate(deleteOrderQuery);
               return;
            }

            String currentType = "";
            for (List<String> item : menuItems) {
               String itemName = item.get(0);
               double price = Double.parseDouble(item.get(1));
               String ingredients = item.get(2);
               String type = item.get(3);

               if (!type.equals(currentType)) {
                  System.out.println("\n" + type.toUpperCase() + ":");
                  currentType = type;
               }

               System.out.printf("- %s ($%.2f) - %s\n", itemName, price, ingredients);
            }

            System.out.print("\nEnter the item name (or type 'done' to finish ordering): ");
            String itemName = in.readLine().trim();

            if (itemName.equalsIgnoreCase("done")) {
               continueShopping = false;
               continue;
            }

            String itemCheckQuery = String.format(
                  "SELECT price FROM Items WHERE TRIM(itemName) = TRIM('%s')",
                  itemName);

            List<List<String>> itemCheck = esql.executeQueryAndReturnResult(itemCheckQuery);

            if (itemCheck.isEmpty()) {
               System.out.println("Item not found. Please try again.");
               continue;
            }

            double itemPrice = Double.parseDouble(itemCheck.get(0).get(0));

            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(in.readLine().trim());

            if (quantity <= 0) {
               System.out.println("Quantity must be greater than zero.");
               continue;
            }

            String addItemQuery = String.format(
                  "INSERT INTO ItemsInOrder (orderID, itemName, quantity) " +
                        "VALUES (%d, '%s', %d)",
                  newOrderID, itemName, quantity);

            esql.executeUpdate(addItemQuery);

            double subtotal = itemPrice * quantity;
            totalPrice += subtotal;

            System.out.printf("Added %d x %s (Subtotal: $%.2f)\n", quantity, itemName, subtotal);
         }

         if (totalPrice > 0) {
            String updatePriceQuery = String.format(
                  "UPDATE FoodOrder SET totalPrice = %.2f WHERE orderID = %d",
                  totalPrice, newOrderID);

            esql.executeUpdate(updatePriceQuery);

            System.out.println("\nORDER SUMMARY");
            System.out.println("-------------");
            System.out.println("Order ID: " + newOrderID);
            System.out.println("Store ID: " + selectedStoreID);
            System.out.printf("Total Price: $%.2f\n", totalPrice);
            System.out.println("Status: incomplete");
            System.out.println("\nThank you for your order!");
         } else {
            String deleteOrderQuery = String.format("DELETE FROM FoodOrder WHERE orderID = %d", newOrderID);
            esql.executeUpdate(deleteOrderQuery);
            System.out.println("Order cancelled - no items were added.");
         }

      } catch (Exception e) {
         System.err.println("Error placing order: " + e.getMessage());
      }
   }

   // view all order
   public static void viewAllOrders(PizzaStore esql) {
      try {
         if (esql.currentUserLogin.isEmpty()) {
            System.out.println("You must be logged in to view orders.");
            return;
         }

         String query;

         if (esql.currentUserRole.equals("customer")) {
            // customer
            query = String.format(
                  "SELECT orderID, storeID, orderTimestamp, totalPrice, orderStatus " +
                        "FROM FoodOrder " +
                        "WHERE login = '%s' " +
                        "ORDER BY orderTimestamp DESC;",
                  esql.currentUserLogin);
         } else {
            // manager + driver
            query = "SELECT orderID, login, storeID, orderTimestamp, totalPrice, orderStatus " +
                  "FROM FoodOrder " +
                  "ORDER BY orderTimestamp DESC;";
         }

         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.isEmpty()) {
            System.out.println("\nNo orders found.");
         } else {
            System.out.println("");
            System.out.println("ORDER HISTORY");
            System.out.println("-------------");

            // customer
            if (esql.currentUserRole.equals("customer")) {
               System.out.println("OrderID\tStoreID\tTimestamp\t\tTotal Price\tStatus");
               System.out.println("----------------------------------------------------------");
               for (List<String> order : result) {
                  System.out.printf("%-8s%-8s%-24s$%-14s%-20s\n",
                        order.get(0), // orderID
                        order.get(1), // storeID
                        order.get(2), // orderTimestamp
                        order.get(3), // totalPrice
                        order.get(4) // orderStatus
                  );
               }
            } else {
               // managers + drivers
               System.out.println("OrderID Customer\tStoreID\tTimestamp\t\tTotal Price\tStatus");
               System.out
                     .println("------------------------------------------------------------------------------------");
               for (List<String> order : result) {
                  System.out.printf("%-8s%-16s%-8s%-24s$%-14s%-20s\n",
                        order.get(0), // orderID
                        order.get(1), // login
                        order.get(2), // storeID
                        order.get(3), // orderTimestamp
                        order.get(4), // totalPrice
                        order.get(5) // orderStatus
                  );
               }
            }
            System.out.println("-------------");
         }
      } catch (Exception e) {
         System.err.println("Error viewing order history: " + e.getMessage());
      }
   }

   // view recent order
   public static void viewRecentOrders(PizzaStore esql) {
      try {
         if (esql.currentUserLogin.isEmpty()) {
            System.out.println("You must be logged in to view orders.");
            return;
         }

         String query;

         if (esql.currentUserRole.equals("customer")) {
            // customers
            query = String.format(
                  "SELECT orderID, storeID, orderTimestamp, totalPrice, orderStatus " +
                        "FROM FoodOrder " +
                        "WHERE login = '%s' " +
                        "ORDER BY orderTimestamp DESC " +
                        "LIMIT 5;",
                  esql.currentUserLogin);
         } else {
            // manager + driver
            query = "SELECT orderID, login, storeID, orderTimestamp, totalPrice, orderStatus " +
                  "FROM FoodOrder " +
                  "ORDER BY orderTimestamp DESC " +
                  "LIMIT 5;";
         }

         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.isEmpty()) {
            System.out.println("\nNo recent orders found.");
         } else {
            System.out.println("");
            System.out.println("RECENT ORDERS (Last 5)");
            System.out.println("---------------------");

            // customer
            if (esql.currentUserRole.equals("customer")) {
               System.out.println("OrderID\tStoreID\tTimestamp\t\tTotal Price\tStatus");
               System.out.println("----------------------------------------------------------");
               for (List<String> order : result) {
                  System.out.printf("%-8s%-8s%-24s$%-14s%-20s\n",
                        order.get(0), // orderID
                        order.get(1), // storeID
                        order.get(2), // orderTimestamp
                        order.get(3), // totalPrice
                        order.get(4) // orderStatus
                  );
               }
            } else {
               // managers + drivers
               System.out.println("OrderID Customer\tStoreID\tTimestamp\t\tTotal Price\tStatus");
               System.out
                     .println("------------------------------------------------------------------------------------");
               for (List<String> order : result) {
                  System.out.printf("%-8s%-16s%-8s%-24s$%-14s%-20s\n",
                        order.get(0), // orderID
                        order.get(1), // login
                        order.get(2), // storeID
                        order.get(3), // orderTimestamp
                        order.get(4), // totalPrice
                        order.get(5) // orderStatus
                  );
               }
            }
            System.out.println("---------------------");
         }
      } catch (Exception e) {
         System.err.println("Error viewing recent orders: " + e.getMessage());
      }
   }

   // view order info
   public static void viewOrderInfo(PizzaStore esql) {
      try {
         if (esql.currentUserLogin.isEmpty()) {
            System.out.println("You must be logged in to view order information.");
            return;
         }

         System.out.print("Enter the Order ID you want to view: ");
         String orderID = in.readLine().trim();

         // First check if the order exists and if the user has permission to view it
         String checkQuery;

         if (esql.currentUserRole.equals("customer")) {
            // Customers can only view their own orders
            checkQuery = String.format(
                  "SELECT COUNT(*) FROM FoodOrder WHERE orderID = %s AND login = '%s';",
                  orderID, esql.currentUserLogin);
         } else {
            // Managers and drivers can view all orders
            checkQuery = String.format(
                  "SELECT COUNT(*) FROM FoodOrder WHERE orderID = %s;",
                  orderID);
         }

         int count = Integer.parseInt(esql.executeQueryAndReturnResult(checkQuery).get(0).get(0));

         if (count == 0) {
            if (esql.currentUserRole.equals("customer")) {
               System.out.println("Order not found or you don't have permission to view this order.");
            } else {
               System.out.println("Order not found.");
            }
            return;
         }

         // Get the basic order information
         String orderQuery = String.format(
               "SELECT login, storeID, orderTimestamp, totalPrice, orderStatus " +
                     "FROM FoodOrder WHERE orderID = %s;",
               orderID);

         List<List<String>> orderResult = esql.executeQueryAndReturnResult(orderQuery);

         if (orderResult.isEmpty()) {
            System.out.println("Error retrieving order information.");
            return;
         }

         List<String> orderInfo = orderResult.get(0);
         String customerLogin = orderInfo.get(0);
         String storeID = orderInfo.get(1);
         String timestamp = orderInfo.get(2);
         String totalPrice = orderInfo.get(3);
         String status = orderInfo.get(4);

         // Get the ordered items with quantities
         String itemsQuery = String.format(
               "SELECT i.itemName, o.quantity, i.price " +
                     "FROM ItemsInOrder o JOIN Items i ON o.itemName = i.itemName " +
                     "WHERE o.orderID = %s;",
               orderID);

         List<List<String>> itemsResult = esql.executeQueryAndReturnResult(itemsQuery);

         // Display all the order information
         System.out.println("\nORDER DETAILS - Order #" + orderID);
         System.out.println("--------------------------------------------------------------------------");
         System.out.println("Customer: " + customerLogin);
         System.out.println("Store: " + storeID);
         System.out.println("Date/Time: " + timestamp);
         System.out.println("Status: " + status);
         System.out.println("Total Price: $" + totalPrice);
         System.out.println("\n");
         System.out.println("----------------------------------------------------------------");
         System.out.printf("%-30s %-10s %-10s %-10s\n", "Item", "Quantity", "Unit Price", "Subtotal");
         System.out.println("----------------------------------------------------------------");

         if (itemsResult.isEmpty()) {
            System.out.println("No items found for this order.");
         } else {
            double calculatedTotal = 0.0;
            for (List<String> item : itemsResult) {
               String itemName = item.get(0);
               int quantity = Integer.parseInt(item.get(1));
               double unitPrice = Double.parseDouble(item.get(2));
               double subtotal = quantity * unitPrice;
               calculatedTotal += subtotal;

               System.out.printf("%-30s %-10d $%-9.2f $%-9.2f\n",
                     itemName, quantity, unitPrice, subtotal);
            }
            System.out.println("----------------------------------------------------------------");
            System.out.printf("%-52s $%-9.2f\n", "Calculated Total:", calculatedTotal);
         }

      } catch (Exception e) {
         System.err.println("Error viewing order information: " + e.getMessage());
      }
   }

   // view store
   public static void viewStores(PizzaStore esql) {
      try {
         String query = "SELECT storeID, address, city, state, isOpen, reviewScore FROM Store";
         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.isEmpty()) {
            System.out.println("No stores available.");
         } else {
            System.out.println("");
            System.out.println("AVAILABLE STORES");
            System.out.println("----------------");

            for (List<String> store : result) {
               String storeID = store.get(0);
               String address = store.get(1);
               String city = store.get(2);
               String state = store.get(3);
               String isOpen = store.get(4);
               String reviewScore = store.get(5);

               System.out.println("Store ID: " + storeID);
               System.out.println("Location: " + address + ", " + city + ", " + state);
               System.out.println("Review Score: " + reviewScore);
               System.out.println("Status: " + (isOpen.equals("1") ? "OPEN" : "CLOSED"));
               System.out.println("----------------");
            }
         }
      } catch (Exception e) {
         System.err.println("Error viewing stores: " + e.getMessage());
      }
   }

   // update order status
   public static void updateOrderStatus(PizzaStore esql) {
      try {
         if (esql.currentUserLogin.isEmpty()) {
            System.out.println("You must be logged in to update an order status.");
            return;
         }

         while (true) {
            System.out.println("");
            System.out.println("UPDATE ORDER");
            System.out.println("-------------");
            System.out.println("0. View all orders");
            System.out.println("1. View recent orders");
            System.out.println("2. Quit");
            System.out.println("10000 (or higher). Update that Specific OrderID");

            int choice = readChoice();

            if (choice == 0) {
               viewAllOrders(esql);
            } else if (choice == 1) {
               viewRecentOrders(esql);
            } else if (choice == 2) {
               return;
            } else if (choice >= 10000) {
               String orderID = String.valueOf(choice);

               System.out.print("Enter the new status (complete/incomplete): ");
               String newStatus = in.readLine().trim().toLowerCase();

               if (!newStatus.equals("complete") && !newStatus.equals("incomplete")) {
                  System.out.println("Invalid status. Please enter 'complete' or 'incomplete'.");
                  continue;
               }

               String query = String.format(
                     "UPDATE FoodOrder SET orderStatus = '%s' WHERE orderID = '%s';",
                     newStatus, orderID);

               try {
                  esql.executeUpdate(query);
                  System.out.println("Order status updated successfully.");
               } catch (Exception e) {
                  System.out.println("Order ID not found or update failed.");
               }
               return;

            } else {
               System.out.println("Invalid input. Please enter 0, 1, 2, or an Order ID 10000 or higher.");
            }
         }
      } catch (Exception e) {
         System.err.println("Error updating order status: " + e.getMessage());
      }
   }

   public static void updateMenu(PizzaStore esql) {
      try {
         if (esql.currentUserLogin.isEmpty()) {
            System.out.println("You must be logged in to update the menu.");
            return;
         }

         while (true) {
            System.out.println("\nUPDATE MENU");
            System.out.println("-----------");
            System.out.println("0. View menu");
            System.out.println("1. Update an item");
            System.out.println("2. Add a new item");
            System.out.println("3. Quit");

            int choice = readChoice();
            switch (choice) {
               case 0:
                  viewMenu(esql);
                  break;

               case 1:
                  System.out.print("Enter the name of the item to update: ");
                  String itemName = in.readLine().trim();

                  String query1 = String.format("SELECT * FROM Items WHERE itemName = '%s';", itemName);
                  int rowCount = esql.executeQuery(query1);
                  if (rowCount == 0) {
                     System.out.println("Item not found.");
                     continue;
                  }

                  System.out.println("Select the attribute to update:");
                  System.out.println("1. Ingredients");
                  System.out.println("2. Type of Item");
                  System.out.println("3. Price");
                  System.out.println("4. Description");

                  int updateChoice = readChoice();
                  String attribute = "";
                  String newValue = "";

                  switch (updateChoice) {
                     case 1:
                        System.out.print("Enter new ingredients: ");
                        newValue = in.readLine().trim();
                        attribute = "ingredients";
                        break;
                     case 2:
                        System.out.print("Enter new type: ");
                        newValue = in.readLine().trim();
                        attribute = "typeOfItem";
                        break;
                     case 3:
                        System.out.print("Enter new price: ");
                        newValue = in.readLine().trim();
                        attribute = "price";
                        break;
                     case 4:
                        System.out.print("Enter new description: ");
                        newValue = in.readLine().trim();
                        attribute = "description";
                        break;
                     default:
                        System.out.println("Invalid choice.");
                        continue;
                  }

                  String updateQuery = String.format("UPDATE Items SET %s = '%s' WHERE itemName = '%s';", attribute,
                        newValue, itemName);
                  esql.executeUpdate(updateQuery);
                  System.out.println("Item updated successfully.");
                  break;

               case 2:
                  System.out.print("Enter new item name: ");
                  String newItemName = in.readLine().trim();

                  String checkQuery = String.format("SELECT * FROM Items WHERE itemName = '%s';", newItemName);
                  int rowCountCheck = esql.executeQuery(checkQuery);
                  if (rowCountCheck > 0) {
                     System.out.println("An item with this name already exists.");
                     continue;
                  }

                  System.out.print("Enter ingredients: ");
                  String ingredients = in.readLine().trim();
                  System.out.print("Enter type of item: ");
                  String typeOfItem = in.readLine().trim();
                  System.out.print("Enter price: ");
                  String price = in.readLine().trim();
                  System.out.print("Enter description: ");
                  String description = in.readLine().trim();

                  String insertQuery = String.format(
                        "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) VALUES ('%s', '%s', '%s', %s, '%s');",
                        newItemName, ingredients, typeOfItem, price, description);
                  esql.executeUpdate(insertQuery);
                  System.out.println("New item added successfully.");
                  break;

               case 3:
                  return;

               default:
                  System.out.println("Invalid input. Please enter a valid option.");
            }
         }

      } catch (Exception e) {
         System.err.println("Error updating menu: " + e.getMessage());
      }
   }

   public static void updateUser(PizzaStore esql) {
      try {
         String currentUserLogin = "";
         boolean validUser = false;

         while (!validUser) {
            System.out.print("Enter the login of the user to update: ");
            currentUserLogin = in.readLine().trim();

            String checkUserQuery = String.format("SELECT * FROM Users WHERE login = '%s';", currentUserLogin);
            int userExists = esql.executeQuery(checkUserQuery);

            if (userExists > 0) {
               validUser = true;
            } else {
               System.out.println("User not found. Please try again.");
            }
         }

         while (true) {
            System.out.println("\nUPDATE USER DETAILS");
            if (validUser) {
               System.out.println("Selected User: " + currentUserLogin);
            }
            System.out.println("--------------------");
            System.out.println("0. View user details");
            System.out.println("1. Update password");
            System.out.println("2. Update role");
            System.out.println("3. Update favorite items");
            System.out.println("4. Update phone number");
            System.out.println("5. Change user to update");
            System.out.println("6. Quit");

            int choice = readChoice();
            switch (choice) {
               case 0:
                  String viewQuery = String.format("SELECT * FROM Users WHERE login = '%s';", currentUserLogin);
                  List<List<String>> userDetails = esql.executeQueryAndReturnResult(viewQuery);

                  System.out.println("\nUser Details:");
                  for (List<String> record : userDetails) {
                     System.out.println("Login: " + record.get(0));
                     System.out.println("Password: " + record.get(1));
                     System.out.println("Role: " + record.get(2));
                     System.out.println("Favorite Items: " + record.get(3));
                     System.out.println("Phone Number: " + record.get(4));
                  }
                  break;

               case 1:
                  System.out.print("Enter new password: ");
                  String newPassword = in.readLine().trim();

                  String updatePasswordQuery = String.format("UPDATE Users SET password = '%s' WHERE login = '%s';",
                        newPassword, currentUserLogin);
                  esql.executeUpdate(updatePasswordQuery);
                  System.out.println("Password updated successfully.");
                  break;

               case 2:
                  System.out.print("Enter new role: ");
                  String newRole = in.readLine().trim();

                  String updateRoleQuery = String.format("UPDATE Users SET role = '%s' WHERE login = '%s';", newRole,
                        currentUserLogin);
                  esql.executeUpdate(updateRoleQuery);
                  System.out.println("Role updated successfully.");
                  break;

               case 3:
                  System.out.print("Enter new favorite items: ");
                  String newFavorites = in.readLine().trim();

                  String updateFavoritesQuery = String.format(
                        "UPDATE Users SET favoriteItems = '%s' WHERE login = '%s';", newFavorites, currentUserLogin);
                  esql.executeUpdate(updateFavoritesQuery);
                  System.out.println("Favorite items updated successfully.");
                  break;

               case 4:
                  System.out.print("Enter new phone number: ");
                  String newPhoneNum = in.readLine().trim();

                  String updatePhoneQuery = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s';",
                        newPhoneNum, currentUserLogin);
                  esql.executeUpdate(updatePhoneQuery);
                  System.out.println("Phone number updated successfully.");
                  break;

               case 5:
                  validUser = false;
                  while (!validUser) {
                     System.out.print("Enter the login of the new user to update: ");
                     currentUserLogin = in.readLine().trim();

                     String checkNewUserQuery = String.format("SELECT * FROM Users WHERE login = '%s';",
                           currentUserLogin);
                     int newUserExists = esql.executeQuery(checkNewUserQuery);

                     if (newUserExists > 0) {
                        validUser = true;
                     } else {
                        System.out.println("User not found. Please try again.");
                     }
                  }
                  break;

               case 6:
                  return;

               default:
                  System.out.println("Invalid input. Please enter a valid option.");
            }
         }

      } catch (Exception e) {
         System.err.println("Error updating user details: " + e.getMessage());
      }
   }

}// end PizzaStore
