import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private String username;
    private String password;
    private static final String FILE_PATH = "accounts.txt";
    private Cart cart; // Add this line

    //Constructors
    public Account(){
        username = "";
        password = "";
    }
    
    public Account(String username, String password){
        this.username = username;
        this.password = password;
        this.cart = new Cart(); // Initialize the cart for the account
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    // Account Authentication with File
    public boolean authenticate(String username, String password) {
        List<Account> accounts = loadAccounts();
        for (Account account : accounts) {
            if (account.username.equals(username) && account.password.equals(password)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean usernameExists(String username) {
        List<Account> accounts = loadAccounts();
        for (Account account : accounts) {
            if (account.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean saveAccount() {
        try {
            FileWriter fw = new FileWriter(FILE_PATH, true); // true for append mode
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(username + "," + password);
            bw.newLine();
            bw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private static List<Account> loadAccounts() {
        List<Account> accounts = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                file.createNewFile();
                return accounts;
            }
            
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    accounts.add(new Account(parts[0], parts[1]));
                }
            }
            br.close();
        } catch (IOException e) {
            // Handle exception if needed
        }
        return accounts;
    }
    
    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

}