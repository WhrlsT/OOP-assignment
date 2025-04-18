import java.util.Scanner;
import java.util.List; 
import java.util.ArrayList; 
import java.util.Map; 
import java.util.HashMap;
import java.util.Comparator; 
import java.util.stream.Collectors;
import java.util.InputMismatchException; 


 
public class Main {
    
    private static final Scanner userInput = new Scanner(System.in);
    private static Account currentUser = null;
    private static String currentSortField = "id";
    private static boolean sortAscending = true;  
    private static String currentCategoryFilter = null;
    private static Map<String, Product> productMap = new HashMap<>();

    public static void main(String[] args) {
        // Load all products into the map at the start
        List<Product> products = Product.loadProducts();
        for (Product p : products) {
            productMap.put(p.getProductId(), p);
        }
        while (true) {
            //main menu
            switch(MainMenu()) {
                case 1 -> LoginMenu();
                case 2 -> SignUpMenu();
                case 3 -> { /* about us */ }
                case 4 -> System.exit(0);
            }
        }
    }    

    private static void Logo(){
        System.out.println("""
 ______ _           _             _   _ _              
|  ____| |         | |           | \\ | (_)             
| |__  | | ___  ___| |_ _ __ ___ |  \\| |_  __ _ ___   
|  __| | |/ _ \\/ __| __| '__/ _ \\| . ` | |/ _` / __|  
| |____| |  __/ (__| |_| | | (_) | |\\  | | (_| \\__ \\  
|______|_|\\___|\\___|\\__|_|  \\___/|_| \\_|_|\\__, |___/  
                                            __/ |       
                                            |___/        """);
    }

    private static void ClearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
        Logo();
    }

    private static void Delay() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private static int MainMenu() {
        while (true) {
            ClearScreen();
            System.out.println("""
------------------------------------------------------------
                        MAIN MENU
------------------------------------------------------------

                (1) Log In
                (2) Sign Up
                (3) About Us
                (4) Exit                                 \n""");
            
            System.out.print("Please select an option: ");
            try {
                int choice = userInput.nextInt();
                if (choice >= 1 && choice <= 4) {
                    return choice;
                } else {
                    System.out.println("Please enter a number between 1 and 4.");
                    Thread.sleep(1500); // Pause to show error message
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                userInput.nextLine(); // Clear the invalid input
                Delay();
            }
        }
    }

    private static void LoginMenu() {
        while (true) {
            ClearScreen();
            System.out.println("""
------------------------------------------------------------
                        LOG IN
------------------------------------------------------------
Please enter your account details (Enter 0 to return to main menu) """);

            System.out.print("Username: ");
            userInput.nextLine(); 
            String username = userInput.nextLine();
            if (username.equals("0")) {
                return; 
            }
            
            System.out.print("Password: ");
            String password = userInput.nextLine();
            if (password.equals("0")) {
                return; 
            }

            Account loginAccount = new Account(username, password);
            if (loginAccount.authenticate(username, password)) {
                System.out.println("Login successful!");
                Delay();
                currentUser = loginAccount;

                if (currentUser != null) {
                    if (currentUser.getUsername().equals("admin")) {
                        //admin menu
                        AdminMenu();
                    } else {
                        // --- Load the user's cart ---
                        currentUser.getCart().loadCartFromFile(currentUser.getUsername(), productMap);
                        Delay();
                        // --- End Load Cart ---

                        //user menu
                        UserMenu(); // Call UserMenu directly now
                    }
                }
                // No need for break here anymore as AdminMenu/UserMenu handle the loop until logout
            } else {
                System.out.println("Invalid username or password. Please try again.");
                Delay();
            }
            // ... rest of LoginMenu loop ...
            
        }
    }

    private static void SignUpMenu() {
        while (true) {
            ClearScreen();
            System.out.println("""
------------------------------------------------------------
                        SIGN UP
------------------------------------------------------------
Please enter your account details (Enter 0 to return to main menu) """);

            System.out.print("Username: ");
            userInput.nextLine();
            String username = userInput.nextLine();
            if (username.equals("0")) {
                return;
            }

            if (Account.usernameExists(username)) {
                System.out.println("Username already exists. Please choose a different username.");
                Delay();
                continue;
            }

            System.out.print("Password: ");
            String password = userInput.nextLine();
            if (password.equals("0")) {
                return;
            }

            System.out.print("Confirm Password: ");
            String confirmPassword = userInput.nextLine();
            if (confirmPassword.equals("0")) {
                return;
            } else if (!password.equals(confirmPassword)) {
                System.out.println("Passwords do not match. Please try again.");
                Delay();
            } else {
                Account newAccount = new Account(username, password);
                if (newAccount.saveAccount()) {
                    System.out.println("Account created successfully!");
                    Delay();
                    break;
                } else {
                    System.out.println("Failed to create account. Please try again.");
                    Delay();
                }
            }

        } 
    }

    private static void UserMenu() {
        while (true) {
            // Check if user logged out from a submenu (like CartMenu returning after checkout/logout)
            if (currentUser == null) {
                return; // Exit UserMenu if logged out
            }

            ClearScreen();
            System.out.println("Welcome, " + currentUser.getUsername() + "!"); // Personalized welcome
            System.out.println("""
------------------------------------------------------------
                        USER MENU
------------------------------------------------------------
                (1) Browse Products
                (2) View Cart
                (3) Order History
                (4) Log Out                                 """ );

                System.out.print("Please select an option: ");
                try {
                    int choice = userInput.nextInt();
                    userInput.nextLine(); // Consume newline
    
                    switch (choice) {
                        case 1 -> UserViewProducts();
                        case 2 -> CartMenu();
                        case 3 -> { /* order history */ }
                        case 4 -> {
                            // --- Save Cart ---
                            if (currentUser != null) {
                                 currentUser.getCart().saveCartToFile(currentUser.getUsername());
                                 Delay();
                            }
                            // --- End Save Cart ---
                            currentUser = null;
                            return; // Exit UserMenu and return to main loop
                        }
                        default -> {
                            System.out.println("Please enter a number between 1 and 4.");
                            Delay();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a number.");
                    userInput.nextLine(); // Clear the invalid input
                    Delay();
                }
            }
        }

    private static void UserViewProducts() {
        List<Product> products = Product.loadProducts();
        
        while (true) {
            ClearScreen();

            System.out.print("""
------------------------------------------------------------
                       BROWSE PRODUCTS
------------------------------------------------------------
            """);

            // --- Filtering (Same as ViewProduct) ---
            List<Product> filteredProducts;
            if (currentCategoryFilter != null) {
                filteredProducts = products.stream()
                    .filter(p -> p.getCategoryName().equalsIgnoreCase(currentCategoryFilter))
                    .collect(Collectors.toList());
                System.out.println("Filtering by Category: " + currentCategoryFilter);
            } else {
                filteredProducts = new ArrayList<>(products);
                System.out.println("Showing All Categories");
            }

            // --- Sorting (Same as ViewProduct) ---
            Comparator<Product> comparator = null;
            switch (currentSortField) {
                case "alpha":
                    comparator = Comparator.comparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER);
                    System.out.print("Sorting by Name ");
                    break;
                case "price":
                    comparator = Comparator.comparingDouble(Product::getPrice);
                    System.out.print("Sorting by Price ");
                    break;
                case "id":
                default:
                    comparator = Comparator.comparing(Product::getProductId);
                     System.out.print("Sorting by ID ");
                    break;
            }
            if (!sortAscending) {
                comparator = comparator.reversed();
                System.out.println("(Descending)");
            } else {
                 System.out.println("(Ascending)");
            }
            filteredProducts.sort(comparator);

            // --- Displaying (Same as ViewProduct) ---
            if (filteredProducts.isEmpty()) {
                System.out.println("\nNo products found matching the criteria.");
            } else {
                System.out.printf("%-8s %-15s %-30s %-10s %-6s%n", "ID", "Category", "Name", "Price", "Stock");
                System.out.println("-----------------------------------------------------------------------");
                for (Product p : filteredProducts) {
                    System.out.printf("%-8s %-15s %-30s RM%-9.2f %-6d%n",
                            p.getProductId(),
                            p.getCategoryName(),
                            p.getProductName(),
                            p.getPrice(),
                            p.getStock());
                }
                System.out.println("-----------------------------------------------------------------------");
            }

            // --- Input Handling for User ---
            System.out.println("\nCommands: [ID] add | sort alpha | sort price | sort id | [Category Name] | all | cart | 0 (Back)");
            System.out.print("Enter command: ");
            String command = userInput.nextLine().trim();

            if (command.equals("0")) {
                return; // Return to User Menu
            }

            String[] parts = command.split("\\s+");
            boolean commandProcessed = false;

            // 1. Check for Add to Cart command
            if (parts.length == 2 && parts[1].equalsIgnoreCase("add")) {
                String productId = parts[0].toUpperCase();
                Product product = Product.findProductById(productId);

                if (product == null) {
                    System.out.println("Product with ID '" + productId + "' not found.");
                } else {
                    // Call helper to handle adding (needs implementation)
                    AddItemToCart(product);

                    // --- Save Cart ---
                    if (currentUser != null) {
                        currentUser.getCart().saveCartToFile(currentUser.getUsername());
                        System.out.println("Cart saved."); // Optional feedback
                        Delay();
                    }
                    // --- End Save Cart ---

                }
                commandProcessed = true;
            }
            // 2. Check for Sort commands (Same as ViewProduct)
            else if (parts.length == 2 && parts[0].equalsIgnoreCase("sort")) {
                String sortType = parts[1].toLowerCase();
                if (sortType.equals("alpha") || sortType.equals("price") || sortType.equals("id")) {
                    if (currentSortField.equals(sortType)) {
                        sortAscending = !sortAscending;
                    } else {
                        currentSortField = sortType;
                        sortAscending = true;
                        if(sortType.equals("price")) sortAscending = false;
                    }
                } else {
                    System.out.println("Invalid sort type. Use 'alpha', 'price', or 'id'.");
                }
                commandProcessed = true;
            }
            // 3. Check for "all" command (clear filter - Same as ViewProduct)
            else if (command.equalsIgnoreCase("all")) {
                 currentCategoryFilter = null;
                 commandProcessed = true;
            }
            // 4. Check for "cart" command
            else if (command.equalsIgnoreCase("cart")) {
                 CartMenu(); // Navigate to the cart menu
                 commandProcessed = true;
            }
            // 5. Check for Category Filter command (Same as ViewProduct)
            else if (parts.length >= 1) {
                 String potentialCategory = command;
                 boolean isCategory = Category.graphicCard.getCategoryName().equalsIgnoreCase(potentialCategory) ||
                                      Category.motherboard.getCategoryName().equalsIgnoreCase(potentialCategory) ||
                                      Category.cpu.getCategoryName().equalsIgnoreCase(potentialCategory);
                 if (isCategory) {
                     currentCategoryFilter = potentialCategory;
                     commandProcessed = true;
                 }
            }

            // If command wasn't processed and wasn't empty, show error
            if (!commandProcessed && !command.isEmpty()) {
                System.out.println("Invalid command format or unknown category.");
                Delay();
            }
            // Loop continues
        }
    }
    
    private static void CartMenu() {
        // Ensure user is logged in
        if (currentUser == null) {
            System.out.println("Error: Not logged in.");
            Delay();
            return;
        }

        Cart userCart = currentUser.getCart();
        // Local state for cart sorting, independent of product browsing sort
        String cartSortField = "id"; // Default sort by ID
        boolean cartSortAscending = true; // Default ascending

        while (true) { // Loop to refresh display after actions
            ClearScreen();
            System.out.println("------------------------------------------------------------");
            System.out.println("                       YOUR CART");
            System.out.println("------------------------------------------------------------");

            if (userCart.isEmpty()) {
                System.out.println("\nYour cart is currently empty.");
            } else {
                // --- Get items and apply sorting ---
                List<Cart.CartItem> cartItems = new ArrayList<>(userCart.getItems().values());
                Comparator<Cart.CartItem> comparator = null;

                switch (cartSortField) {
                    case "alpha":
                        comparator = Comparator.comparing(item -> item.getProduct().getProductName(), String.CASE_INSENSITIVE_ORDER);
                        System.out.print("Sorting by Name ");
                        break;
                    case "price":
                        // Sort by individual product price, not subtotal
                        comparator = Comparator.comparingDouble(item -> item.getProduct().getPrice());
                        System.out.print("Sorting by Price ");
                        break;
                    case "id":
                    default:
                        comparator = Comparator.comparing(item -> item.getProduct().getProductId());
                        System.out.print("Sorting by ID ");
                        break;
                }

                if (!cartSortAscending) {
                    comparator = comparator.reversed();
                    System.out.println("(Descending)");
                } else {
                    System.out.println("(Ascending)");
                }
                cartItems.sort(comparator);

                // --- Display Cart Items in a Table ---
                System.out.printf("%-8s %-30s %-10s %-10s %-12s%n", "ID", "Name", "Price", "Quantity", "Subtotal");
                System.out.println("-----------------------------------------------------------------------");
                for (Cart.CartItem item : cartItems) {
                    Product p = item.getProduct();
                    System.out.printf("%-8s %-30s RM%-9.2f %-10d RM%-11.2f%n",
                            p.getProductId(),
                            p.getProductName(),
                            p.getPrice(),
                            item.getQuantity(),
                            item.getTotalPrice());
                }
                System.out.println("-----------------------------------------------------------------------");

                // --- Display Totals ---
                System.out.printf("Total Unique Items: %-5d Total Quantity: %-5d Total Price: RM%.2f%n",
                        userCart.getItemCount(), // Number of unique product types
                        userCart.getTotalQuantity(), // Sum of quantities of all items
                        userCart.getTotalPrice());
                System.out.println("-----------------------------------------------------------------------");
            }

            // --- Display Commands ---
            System.out.println("\nCommands: [ID] edit | [ID] del | sort alpha | sort price | sort id | checkout | 0 (Back)");
            System.out.print("Enter command: ");
            String command = userInput.nextLine().trim();

            if (command.equals("0")) {
                return; // Return to User Menu
            }

            String[] parts = command.split("\\s+");
            boolean commandProcessed = false;

            try {
                // 1. Edit Item Quantity
                if (parts.length == 2 && parts[1].equalsIgnoreCase("edit")) {
                    String productIdToEdit = parts[0].toUpperCase();
                    if (!userCart.getItems().containsKey(productIdToEdit)) {
                        System.out.println("Product ID '" + productIdToEdit + "' not found in cart.");
                    } else {
                        System.out.print("Enter new quantity for " + productIdToEdit + ": ");
                        try {
                            int newQuantity = userInput.nextInt();
                            userInput.nextLine(); // Consume newline
                            // updateQuantity handles stock check & removal if <= 0
                            userCart.updateQuantity(productIdToEdit, newQuantity);
                            if (newQuantity > 0) {
                                System.out.println("Updated quantity for " + productIdToEdit + " to " + newQuantity + ".");
                            } else {
                                System.out.println("Removed " + productIdToEdit + " from cart (quantity set to 0 or less).");
                            }
                        } catch (InputMismatchException e) {
                            System.out.println("Invalid quantity. Please enter a whole number.");
                            userInput.nextLine(); // Consume invalid input
                        } catch (IllegalArgumentException e) { // Catches errors from updateQuantity
                            System.out.println("Error updating quantity: " + e.getMessage());
                        }
                    }
                    commandProcessed = true;
                    Delay();
                }
                // 2. Delete/Remove Item
                else if (parts.length == 2 && (parts[1].equalsIgnoreCase("del") || parts[1].equalsIgnoreCase("remove"))) {
                    String productIdToRemove = parts[0].toUpperCase();
                    Cart.CartItem removedItem = userCart.removeItem(productIdToRemove);
                    if (removedItem != null) {
                        System.out.println("Removed " + removedItem.getProduct().getProductName() + " from cart.");
                    } else {
                        System.out.println("Product ID '" + productIdToRemove + "' not found in cart.");
                    }
                    commandProcessed = true;
                    Delay();
                }
                // 3. Sort commands
                else if (parts.length == 2 && parts[0].equalsIgnoreCase("sort")) {
                    String sortType = parts[1].toLowerCase();
                    if (sortType.equals("alpha") || sortType.equals("price") || sortType.equals("id")) {
                        if (cartSortField.equals(sortType)) {
                            cartSortAscending = !cartSortAscending; // Toggle direction
                        } else {
                            cartSortField = sortType; // Change field
                            cartSortAscending = true; // Default to ascending for new field
                            // Optional: Default price sort to high->low?
                            // if(sortType.equals("price")) cartSortAscending = false;
                        }
                    } else {
                        System.out.println("Invalid sort type. Use 'alpha', 'price', or 'id'.");
                        Delay();
                    }
                    commandProcessed = true;
                    // No Delay() needed here, loop will refresh display immediately
                }
                // 4. Checkout
                else if (command.equalsIgnoreCase("checkout")) {
                    if (userCart.isEmpty()) {
                        System.out.println("Your cart is empty. Cannot proceed to checkout.");
                    } else {
                        boolean checkoutSuccess = Checkout(); // Call the checkout method
                        if (checkoutSuccess) {
                            System.out.println("Checkout successful. Returning to User Menu.");
                            Delay();
                            UserMenu();
                            break; 
                        } else {
                            System.out.println("Checkout cancelled or failed. Returning to cart.");
                        }
                    }
                    commandProcessed = true;
                    Delay(); // Pause after checkout attempt message
                }

            } catch (Exception e) { // Catch unexpected errors
                 System.out.println("An error occurred processing the command: " + e.getMessage());
                 // e.printStackTrace(); // Uncomment for debugging
                 Delay();
            }

            // If command wasn't processed and wasn't empty/zero, show error
            if (!commandProcessed && !command.isEmpty() && !command.equals("0")) {
                System.out.println("Invalid command format.");
                Delay();
            }
            // Loop continues to show updated cart/sort or prompt again
        }
    }

    //TODO : Checkout Logics ( Payment )
    private static boolean Checkout(){
        return true;
    }
    // --- Updated Add Item to Cart Helper ---
    private static void AddItemToCart(Product product) {
        // Check if user is logged in
         if (currentUser == null) {
             System.out.println("\nError: No user logged in. Cannot add to cart.");
             Delay();
             return;
         }

         ClearScreen();
         System.out.println("------------------------------------------------------------");
         System.out.println("                     ADD TO CART");
         System.out.println("------------------------------------------------------------");
         System.out.println("Product: " + product.getProductName() + " (ID: " + product.getProductId() + ")");
         System.out.println("Price: RM" + String.format("%.2f", product.getPrice()));
         System.out.println("Stock Available: " + product.getStock());

         if (product.getStock() <= 0) {
             System.out.println("\nSorry, this product is out of stock.");
             Delay();
             return;
         }


         int quantity = 0;
         while (true) { // Loop until valid quantity is entered or user cancels
             System.out.print("Enter quantity to add (or 0 to cancel): ");
             try {
                 quantity = userInput.nextInt();
                 userInput.nextLine(); // Consume newline

                 if (quantity == 0) {
                     System.out.println("Add to cart cancelled.");
                     Delay();
                     return; // Exit the method
                 }

                 if (quantity < 0) {
                     System.out.println("Quantity must be positive.");
                     continue; // Ask again
                 }

                 // Check stock (considering items potentially already in cart)
                 Cart userCart = currentUser.getCart();
                 Cart.CartItem existingItem = userCart.getItems().get(product.getProductId());
                 int currentQuantityInCart = (existingItem != null) ? existingItem.getQuantity() : 0;
                 int totalQuantityNeeded = currentQuantityInCart + quantity;

                 if (totalQuantityNeeded > product.getStock()) {
                     System.out.println("Not enough stock available.");
                     System.out.println("You have " + currentQuantityInCart + " in cart. Available stock: " + product.getStock());
                     int canAdd = product.getStock() - currentQuantityInCart;
                     if (canAdd > 0) {
                        System.out.println("You can add at most " + canAdd + " more.");
                     } else {
                        System.out.println("You cannot add more of this item.");
                     }
                     // Let the user decide to enter a smaller amount or 0
                 } else {
                     // Valid quantity entered
                     break; // Exit the quantity input loop
                 }

             } catch (java.util.InputMismatchException e) {
                 System.out.println("Invalid input. Please enter a number.");
                 userInput.nextLine(); // Consume invalid input
             }
         }


         // --- Add to Cart using Cart object ---
         try {
             currentUser.getCart().addItem(product, quantity);
             System.out.println("\nSuccessfully added " + quantity + " of " + product.getProductName() + " to your cart.");
         } catch (IllegalArgumentException e) {
             // Catch errors from cart.addItem (e.g., re-checking stock just in case)
             System.out.println("\nError adding item: " + e.getMessage());
         }

         Delay();
    }

    private static void AdminMenu() {
        while (true){
            ClearScreen();
            System.out.println("""
------------------------------------------------------------
                     ADMIN MENU
------------------------------------------------------------
                (1) View Report
                (2) Manage Users
                (3) Manage Orders
                (4) Manage Products
                (5) Log Out                                 """);

            System.out.print("Please select an option: ");
            try {
                int choice = userInput.nextInt();
                if (choice >= 1 && choice <= 5) {
                    switch(choice){
                    case 1 -> { /* view report */ }
                    case 2 -> { /* manage users */ }
                    case 3 -> { /* manage orders */ }
                    case 4 -> {ManageProducts();}
                    case 5 -> {
                            currentUser = null; 
                            return;  // Return from the method instead of break
                        }
                    }
                }else {
                    System.out.println("Invalid input. Please enter a number.");
                    userInput.nextLine(); // Clear the invalid input
                    Delay();
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                userInput.nextLine(); // Clear the invalid input
                Delay(); 
            }
            

        }
    }

    private static void ManageProducts() {
        while (true) {
            ClearScreen();
            System.out.println("""
------------------------------------------------------------
                    PRODUCT MANAGEMENT
------------------------------------------------------------ """);

            System.out.println("Total Number of Products: " + Category.getTotal()); 
            System.out.println("Total Graphic Cards: " + Category.getTotalProducts("Graphic Card")); 
            System.out.println("Total Motherboards: " + Category.getTotalProducts("Motherboard"));
            System.out.println("Total Processors: " + Category.getTotalProducts("CPU"));

            System.out.println("""
                (1) Add Product
                (2) View Products
                (0) Return to Admin Menu""");
            
            System.out.print("Please select an option: ");
            try {
                int choice = userInput.nextInt();
                if (choice >= 0 && choice <= 2) {
                    switch (choice){ 
                    case 0 -> {return;}
                    case 1 -> {AddProduct(); }
                    case 2 -> {ViewProduct();}
                }
                } else {
                    System.out.println("Please enter a number between 0 and 2.");
                    Thread.sleep(1500); // Pause to show error message
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                userInput.nextLine(); // Clear the invalid input
                Delay();
            }
            
            

        }
    }

    private static void AddProduct() {
        ClearScreen();
        System.out.println("""
            --------------------------------------------
                    ADD PRODUCT ( Choose Category )
            --------------------------------------------

            Choose a category to add product into:
            (1) Graphic Card
            (2) Motherboard
            (3) CPU""");

        System.out.print("Please select an option: ");

        try {
            int choice = userInput.nextInt();
            if (choice >= 1 && choice <= 3) {
                Category toAddCategory = new Category();
                switch (choice) {
                    case 1 -> toAddCategory = Category.graphicCard;
                    case 2 -> toAddCategory = Category.motherboard;
                    case 3 -> toAddCategory = Category.cpu;
                }  
                
                System.out.println("Category : " + toAddCategory.getCategoryName());
                System.out.print("Product Name :");
                userInput.nextLine();
                //TODO : INPUT VALIDATIONS
                //SET MAX for STOCKS and PRICE value in PRODUCT class
                String productName = userInput.nextLine();
                System.out.print("Product Price : RM");
                double productPrice = userInput.nextDouble();
                System.out.print("Stocks : ");
                int productStocks = userInput.nextInt();

                Product toAddProduct = new Product(
                        toAddCategory.getCategoryName(), toAddCategory.getCategoryId(), productName, productPrice, productStocks
                    );

                toAddProduct.saveProduct();
                
            } 
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number.");
            userInput.nextLine(); // Clear the invalid input
            Delay();
        }

    }

    private static void ViewProduct(){
        // Load all products initially
        List<Product> allProducts = Product.loadProducts();

        while (true) {
            ClearScreen();

            System.out.print("""
------------------------------------------------------------
                        PRODUCTS
------------------------------------------------------------
            """);

            // --- Filtering ---
            List<Product> filteredProducts;
            if (currentCategoryFilter != null) {
                // Use stream API to filter by category name (case-insensitive)
                filteredProducts = allProducts.stream()
                    .filter(p -> p.getCategoryName().equalsIgnoreCase(currentCategoryFilter))
                    .collect(Collectors.toList());
                System.out.println("Filtering by Category: " + currentCategoryFilter);
            } else {
                filteredProducts = new ArrayList<>(allProducts); // Work with a copy if no filter
                System.out.println("Showing All Categories");
            }

            // --- Sorting ---
            Comparator<Product> comparator = null;
            switch (currentSortField) {
                case "alpha":
                    comparator = Comparator.comparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER);
                    System.out.print("Sorting by Name ");
                    break;
                case "price":
                    comparator = Comparator.comparingDouble(Product::getPrice);
                    System.out.print("Sorting by Price ");
                    break;
                case "id":
                default:
                    // Natural order sorting for IDs like P001, P002...
                    comparator = Comparator.comparing(Product::getProductId);
                     System.out.print("Sorting by ID ");
                    break;
            }

            if (!sortAscending) {
                comparator = comparator.reversed(); // Reverse the comparator if descending
                System.out.println("(Descending)");
            } else {
                 System.out.println("(Ascending)");
            }

            // Apply the sorting
            filteredProducts.sort(comparator);

            // --- Displaying ---
            if (filteredProducts.isEmpty()) {
                System.out.println("\nNo products found matching the criteria.");
            } else {
                System.out.printf("%-8s %-15s %-30s %-10s %-6s%n", "ID", "Category", "Name", "Price", "Stock");
                System.out.println("-----------------------------------------------------------------------");
                for (Product p : filteredProducts) {
                    System.out.printf("%-8s %-15s %-30s RM%-9.2f %-6d%n",
                            p.getProductId(),
                            p.getCategoryName(),
                            p.getProductName(),
                            p.getPrice(),
                            p.getStock());
                }
                System.out.println("-----------------------------------------------------------------------");
            }

            // --- Input Handling ---
            System.out.println("\nCommands: [ID] edit | [ID] del | sort alpha | sort price | sort id | [Category Name] | all | 0 (Return)");
            System.out.print("Enter command: ");
            String command = userInput.nextLine().trim();

            if (command.equals("0")) {
                // Optional: Reset sort/filter state when exiting?
                // currentSortField = "id";
                // sortAscending = true;
                // currentCategoryFilter = null;
                return; // Return to the ManageProducts menu
            }

            String[] parts = command.split("\\s+"); // Split command by whitespace

            // --- Command Processing ---
            boolean commandProcessed = false;

            // 1. Check for Edit/Delete commands
            if (parts.length == 2 && (parts[1].equalsIgnoreCase("edit") || parts[1].equalsIgnoreCase("del") || parts[1].equalsIgnoreCase("delete"))) {
                String productId = parts[0].toUpperCase();
                String action = parts[1].toLowerCase();
                Product product = Product.findProductById(productId); // Use existing find method

                if (product == null) {
                    System.out.println("Product with ID '" + productId + "' not found.");
                } else {
                    if (action.equals("edit")) {
                        EditProduct(product);
                    } else { // del or delete
                        DeleteProduct(product);
                    }
                    // Reload products after edit/delete as the file has changed
                    allProducts = Product.loadProducts();
                }
                commandProcessed = true;
            }
            // 2. Check for Sort commands
            else if (parts.length == 2 && parts[0].equalsIgnoreCase("sort")) {
                String sortType = parts[1].toLowerCase();
                if (sortType.equals("alpha") || sortType.equals("price") || sortType.equals("id")) {
                    if (currentSortField.equals(sortType)) {
                        sortAscending = !sortAscending; // Toggle direction if same field
                    } else {
                        currentSortField = sortType; // Change field
                        sortAscending = true;      // Reset to ascending for new field (or false for price high-low default)
                        if(sortType.equals("price")) sortAscending = false; // Default price sort to High->Low
                    }
                    // Reset category filter when sorting
                    // currentCategoryFilter = null; // Decide if sorting should reset filter
                } else {
                    System.out.println("Invalid sort type. Use 'alpha', 'price', or 'id'.");
                }
                commandProcessed = true;
            }
            // 3. Check for "all" command (clear filter)
            else if (command.equalsIgnoreCase("all")) {
                 currentCategoryFilter = null;
                 commandProcessed = true;
            }
            // 4. Check for Category Filter command (assuming category names don't contain spaces for simplicity here)
            //    If category names have spaces, need more robust parsing or specific command like "filter [Category Name]"
            else if (parts.length >= 1) {
                 // Check if the command matches a known category name (case-insensitive)
                 String potentialCategory = command; // Use the full command as potential category name
                 boolean isCategory = Category.graphicCard.getCategoryName().equalsIgnoreCase(potentialCategory) ||
                                      Category.motherboard.getCategoryName().equalsIgnoreCase(potentialCategory) ||
                                      Category.cpu.getCategoryName().equalsIgnoreCase(potentialCategory);

                 if (isCategory) {
                     currentCategoryFilter = potentialCategory; // Set the filter
                     commandProcessed = true;
                 }
            }

            // If command wasn't processed and wasn't empty, show error
            if (!commandProcessed && !command.isEmpty()) {
                System.out.println("Invalid command format or unknown category.");
                Delay();
            }
            // Loop continues to refresh the product list with new sort/filter/data
        }
    }

    // New method to handle editing a product
    private static void EditProduct(Product productToEdit) {
        ClearScreen();
        System.out.println("------------------------------------------------------------");
        System.out.println("                 EDIT PRODUCT (ID: " + productToEdit.getProductId() + ")");
        System.out.println("------------------------------------------------------------");
        System.out.println("Current Details:");
        System.out.println("  Name: " + productToEdit.getProductName());
        System.out.println("  Price: RM" + String.format("%.2f", productToEdit.getPrice()));
        System.out.println("  Stock: " + productToEdit.getStock());
        System.out.println("\nEnter new details (leave blank to keep current value):");

        System.out.print("New Product Name [" + productToEdit.getProductName() + "]: ");
        String newName = userInput.nextLine().trim();
        if (!newName.isEmpty()) {
            productToEdit.setProductName(newName); // Assumes setter exists
        }

        double newPrice = -1;
        while (newPrice < 0) {
             System.out.print("New Product Price [" + String.format("%.2f", productToEdit.getPrice()) + "]: RM");
             String priceInput = userInput.nextLine().trim();
             if (priceInput.isEmpty()) {
                 newPrice = productToEdit.getPrice(); // Keep old price
                 break;
             }
             try {
                 newPrice = Double.parseDouble(priceInput);
                 if (newPrice < 0) {
                     System.out.println("Price cannot be negative.");
                 } else {
                     productToEdit.setPrice(newPrice); // Assumes setter exists
                 }
             } catch (NumberFormatException e) {
                 System.out.println("Invalid price format. Please enter a number.");
                 newPrice = -1; // Reset to loop again
             }
        }


        int newStock = -1;
         while (newStock < 0) {
             System.out.print("New Stock [" + productToEdit.getStock() + "]: ");
             String stockInput = userInput.nextLine().trim();
             if (stockInput.isEmpty()) {
                 newStock = productToEdit.getStock(); // Keep old stock
                 break;
             }
             try {
                 newStock = Integer.parseInt(stockInput);
                 if (newStock < 0) {
                     System.out.println("Stock cannot be negative.");
                 } else {
                     productToEdit.setStock(newStock); // Assumes setter exists
                 }
             } catch (NumberFormatException e) {
                 System.out.println("Invalid stock format. Please enter a whole number.");
                 newStock = -1; // Reset to loop again
             }
         }


        System.out.print("\nSave changes? (y/n): ");
        String confirm = userInput.nextLine().trim().toLowerCase();

        if (confirm.equals("y")) {
            // Update the product in the file (Requires updateProduct in Product class)
            if (Product.updateProduct(productToEdit)) {
                System.out.println("Product updated successfully!");
            } else {
                System.out.println("Failed to update product.");
            }
        } else {
            System.out.println("Edit cancelled.");
        }
        Delay();
    }

    // New method to handle deleting a product
    private static void DeleteProduct(Product productToDelete) {
        ClearScreen();
        System.out.println("------------------------------------------------------------");
        System.out.println("                 DELETE PRODUCT");
        System.out.println("------------------------------------------------------------");
        System.out.println("You are about to delete the following product:");
        System.out.println("  ID: " + productToDelete.getProductId());
        System.out.println("  Name: " + productToDelete.getProductName());
        System.out.println("  Category: " + productToDelete.getCategoryName());
        System.out.println("  Price: RM" + String.format("%.2f", productToDelete.getPrice()));
        System.out.println("  Stock: " + productToDelete.getStock());

        System.out.print("\nAre you sure you want to delete this product? (y/n): ");
        String confirm = userInput.nextLine().trim().toLowerCase();

        if (confirm.equals("y")) {
            // Delete the product from the file (Requires deleteProduct in Product class)
            if (Product.deleteProduct(productToDelete.getProductId())) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println("Failed to delete product.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
        Delay();
    }

}
