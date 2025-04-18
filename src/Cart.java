
import java.util.HashMap;
import java.util.Map;
import java.util.Collections; // For unmodifiable map
import java.io.*;

/**
 * Represents a shopping cart associated with a user.
 * Stores products and their quantities.
 */
public class Cart {

    // Inner class to represent an item within the cart
    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            if (product == null) {
                throw new IllegalArgumentException("Product cannot be null.");
            }
            if (quantity <= 0) {
                 throw new IllegalArgumentException("Quantity must be positive.");
            }
            this.product = product;
            this.quantity = quantity;
        }

        // Getters
        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        // Setter for quantity (useful for updates)
        public void setQuantity(int quantity) {
             if (quantity <= 0) {
                 throw new IllegalArgumentException("Quantity must be positive.");
            }
            this.quantity = quantity;
        }

        public double getTotalPrice() {
            return product.getPrice() * quantity;
        }

        @Override
        public String toString() {
            return "CartItem{" +
                   "product=" + product.getProductName() + // Or product.toString()
                   ", quantity=" + quantity +
                   '}';
        }
    }

    // Map to store cart items: Product ID -> CartItem
    private final Map<String, CartItem> items;
    // Optional: Store the username this cart belongs to
    // private final String username;

    /**
     * Constructs an empty shopping cart.
     */
    public Cart(/* Optional: String username */) {
        this.items = new HashMap<>();
        // this.username = username;
    }

    /**
     * Adds a specified quantity of a product to the cart.
     * If the product is already in the cart, updates its quantity.
     *
     * git product The product to add.
     * @param quantity The quantity to add (must be positive).
     * @throws IllegalArgumentException if product is null or quantity is not positive.
     */
    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        // Check available stock before adding
        if (product.getStock() < quantity) {
             throw new IllegalArgumentException("Not enough stock available for " + product.getProductName() + ". Available: " + product.getStock());
        }


        String productId = product.getProductId();
        if (items.containsKey(productId)) {
            // Product exists, update quantity
            CartItem existingItem = items.get(productId);
            int newQuantity = existingItem.getQuantity() + quantity;
             // Re-check stock for the *total* quantity
            if (product.getStock() < newQuantity) {
                 throw new IllegalArgumentException("Not enough stock available to add more " + product.getProductName() + ". Total requested: " + newQuantity + ", Available: " + product.getStock());
            }
            existingItem.setQuantity(newQuantity);
        } else {
            // Product is new, add it
            items.put(productId, new CartItem(product, quantity));
        }
    }

    /**
     * Updates the quantity of a specific product in the cart.
     * If the new quantity is zero or less, the item is removed.
     *
     * @param productId The ID of the product to update.
     * @param newQuantity The new quantity for the product.
     * @throws IllegalArgumentException if product is not found or stock is insufficient.
     */
    public void updateQuantity(String productId, int newQuantity) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new IllegalArgumentException("Product with ID " + productId + " not found in cart.");
        }

        if (newQuantity <= 0) {
            // Remove item if quantity is zero or negative
            items.remove(productId);
        } else {
             // Check stock before updating
             if (item.getProduct().getStock() < newQuantity) {
                 throw new IllegalArgumentException("Not enough stock available for " + item.getProduct().getProductName() + ". Requested: " + newQuantity + ", Available: " + item.getProduct().getStock());
             }
            // Update quantity
            item.setQuantity(newQuantity);
        }
    }

    /**
     * Removes a product entirely from the cart.
     *
     * @param productId The ID of the product to remove.
     * @return The CartItem that was removed, or null if the product was not found.
     */
    public CartItem removeItem(String productId) {
        return items.remove(productId);
    }

    /**
     * Gets an unmodifiable view of the items in the cart.
     * Prevents external modification of the cart's internal map.
     *
     * @return An unmodifiable Map of Product ID to CartItem.
     */
    public Map<String, CartItem> getItems() {
        return Collections.unmodifiableMap(items);
    }

    /**
     * Calculates the total price of all items currently in the cart.
     *
     * @return The total price as a double.
     */
    public double getTotalPrice() {
        double totalPrice = 0.0;
        for (CartItem item : items.values()) {
            totalPrice += item.getTotalPrice();
        }
        return totalPrice;
    }

    /**
     * Checks if the cart is empty.
     *
     * @return true if the cart contains no items, false otherwise.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Removes all items from the cart.
     */
    public void clearCart() {
        items.clear();
    }

    /**
     * Gets the number of unique product types in the cart.
     *
     * @return The number of unique items.
     */
    public int getItemCount() {
        return items.size();
    }

     /**
     * Gets the total number of individual items (sum of quantities) in the cart.
     *
     * @return The total quantity of all items.
     */
    public int getTotalQuantity() {
        int totalQuantity = 0;
        for (CartItem item : items.values()) {
            totalQuantity += item.getQuantity();
        }
        return totalQuantity;
    }

    // Optional: Override toString for debugging
    @Override
    public String toString() {
        return "Cart{" +
               "items=" + items +
               '}';
    }

    public void saveCartToFile(String username) {
        String filename = "cart_" + username + ".txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (CartItem item : items.values()) {
                // Save productId and quantity (you can add more fields if needed)
                bw.write(item.getProduct().getProductId() + "," + item.getQuantity());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save cart: " + e.getMessage());
        }
    }

    public void loadCartFromFile(String username, Map<String, Product> productMap) {
        String filename = "cart_" + username + ".txt";
        items.clear();
        File file = new File(filename);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String productId = parts[0];
                    int quantity = Integer.parseInt(parts[1]);
                    Product product = productMap.get(productId);
                    if (product != null) {
                        items.put(productId, new CartItem(product, quantity));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load cart: " + e.getMessage());
        }
    }
}