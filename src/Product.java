import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Product extends Category {
    private static final String FILE_PATH = "products.txt";
    private static int nextProductId = 1; // Static counter for auto-increment
    private String productId;
    private String productName;
    private double price;
    private int stock;

    // Static block to initialize nextProductId from existing file
    static {
        initializeNextProductId();
    }

    public Product() {
        super();
        this.productId = generateProductId();
        this.productName = "";
        this.price = 0.0;
        this.stock = 0;
    }

    public Product(String categoryName, String categoryId, String productName, double price, int stock) {
        super(categoryName, categoryId);
        this.productId = generateProductId();
        this.productName = productName;
        this.price = price;
        this.stock = stock;
    }

    // Used when loading from file (with fixed productId)
    public Product(String categoryName, String categoryId, String productId, String productName, double price, int stock) {
        super(categoryName, categoryId);
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
        
        // Keep nextProductId updated
        try {
            int idNum = Integer.parseInt(productId.replaceAll("[^0-9]", ""));
            if (idNum >= nextProductId) {
                nextProductId = idNum + 1;
            }
        } catch (NumberFormatException e) {
            // Ignore invalid format
        }
    }

    // Generate new product ID like "P001", "P002", etc.
    private static String generateProductId() {
        return String.format("P%03d", nextProductId++);
    }

    private static void initializeNextProductId() {
        List<Product> products = loadProducts();
        for (Product p : products) {
            try {
                int idNum = Integer.parseInt(p.getProductId().replaceAll("[^0-9]", ""));
                if (idNum >= nextProductId) {
                    nextProductId = idNum + 1;
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isInStock() {
        return stock > 0;
    }

    @Override
    public String toString() {
        return String.format("%s - $%.2f (%s)",
                productName,
                price,
                isInStock() ? stock + " in stock" : "OUT OF STOCK"
        );
    }

    public boolean saveProduct() {
        try {
            FileWriter fw = new FileWriter(FILE_PATH, true); 
            BufferedWriter bw = new BufferedWriter(fw);
            // Format: categoryName,categoryId,productId,productName,price,stock
            bw.write(String.format("%s,%s,%s,%s,%.2f,%d",
                    getCategoryName(),
                    getCategoryId(),
                    productId,
                    productName,
                    price,
                    stock));
            bw.newLine();
            bw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                if (file.createNewFile()) {
                    nextProductId = 1; 
                }
                return products;
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int maxIdNum = 0; 
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String productId = parts[2];
                    products.add(new Product(
                            parts[0], // categoryName
                            parts[1], // categoryId
                            productId, // productId
                            parts[3], // productName
                            Double.parseDouble(parts[4]), // price
                            Integer.parseInt(parts[5])  // stock
                    ));
                    // Update maxIdNum based on loaded product ID
                    try {
                        int idNum = Integer.parseInt(productId.replaceAll("[^0-9]", ""));
                        if (idNum > maxIdNum) {
                            maxIdNum = idNum;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore products with non-standard IDs for max ID calculation
                    }
                }
            }
            br.close();
            // Ensure nextProductId is correctly set after loading all products
            nextProductId = maxIdNum + 1;
        } catch (IOException | NumberFormatException e) { // Catch potential exceptions
             System.err.println("Error loading products: " + e.getMessage());
             // Depending on requirements, you might want to return empty list or handle differently
        }
        return products;
    }

    private static boolean saveAllProducts(List<Product> products) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) { // Overwrite mode (false)
            for (Product p : products) {
                bw.write(String.format("%s,%s,%s,%s,%.2f,%d",
                        p.getCategoryName(),
                        p.getCategoryId(),
                        p.getProductId(),
                        p.getProductName(),
                        p.getPrice(),
                        p.getStock()));
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving products: " + e.getMessage());
            return false;
        }
    }

    // Find a product by its ID
    public static Product findProductById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return null;
        }
        List<Product> products = loadProducts(); // Load current products
        for (Product p : products) {
            if (p.getProductId().equalsIgnoreCase(productId.trim())) { // Case-insensitive comparison
                return p;
            }
        }
        return null; // Not found
    }

    // Update an existing product in the file
    public static boolean updateProduct(Product updatedProduct) {
        if (updatedProduct == null) return false;

        List<Product> products = loadProducts();
        boolean found = false;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductId().equals(updatedProduct.getProductId())) {
                products.set(i, updatedProduct); // Replace the old product with the updated one
                found = true;
                break;
            }
        }

        if (found) {
            return saveAllProducts(products); // Rewrite the file with the updated list
        } else {
            System.err.println("Update failed: Product with ID " + updatedProduct.getProductId() + " not found.");
            return false; // Product to update wasn't found
        }
    }

    // Delete a product from the file by its ID
    public static boolean deleteProduct(String productId) {
         if (productId == null || productId.trim().isEmpty()) {
            return false;
        }
        List<Product> products = loadProducts();
        List<Product> updatedList = new ArrayList<>();
        boolean found = false;
        for (Product p : products) {
            if (p.getProductId().equalsIgnoreCase(productId.trim())) {
                found = true; // Mark as found, but don't add to the new list
            } else {
                updatedList.add(p); // Add all other products to the new list
            }
        }

        if (found) {
            return saveAllProducts(updatedList); // Rewrite the file with the filtered list
        } else {
             System.err.println("Delete failed: Product with ID " + productId + " not found.");
            return false; // Product to delete wasn't found
        }
    }
}
