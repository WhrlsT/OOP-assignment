import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Category {
    private String categoryName;
    private String categoryId;
    private static final String FILE_PATH = "products.txt";
    
    public static Category motherboard = new Category("Motherboard", "001");
    public static Category graphicCard = new Category("Graphic Card", "002");
    public static Category cpu = new Category("CPU", "003");

    public Category() {
        categoryName = "";
        categoryId = "";
    }
    
    public Category(String categoryName, String categoryId) {
        this.categoryName = categoryName;
        this.categoryId = categoryId;
    }
    
    // Getters and Setters
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    /**
     * Gets the total number of products across all categories
     * @return Total number of products
     */
    public static int getTotal() {
        int count = 0;
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return 0;
            }
            
            BufferedReader br = new BufferedReader(new FileReader(file));
            while (br.readLine() != null) {
                count++;
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error reading product file: " + e.getMessage());
        }
        return count;
    }
    
    /**
     * Gets the total number of products in a specific category
     * @param categoryName The name of the category to count
     * @return Number of products in the specified category
     */
    public static int getTotalProducts(String categoryName) {
        int count = 0;
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return 0;
            }
            
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].equals(categoryName)) {
                    count++;
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error reading product file: " + e.getMessage());
        }
        return count;
    }
}