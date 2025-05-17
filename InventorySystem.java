import java.sql.*;
import java.util.Scanner;

public class InventorySystem {
    static final String DB_URL = "jdbc:sqlite:inventory.db";
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTable(conn);

            while (true) {
                System.out.println("\n======= INVENTORY SYSTEM =======");
                System.out.println("1. Add Item");
                System.out.println("2. View All Items");
                System.out.println("3. Update Quantity");
                System.out.println("4. Remove Item");
                System.out.println("5. Search Item");
                System.out.println("6. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1 -> addItem(conn);
                    case 2 -> viewAll(conn);
                    case 3 -> updateQuantity(conn);
                    case 4 -> removeItem(conn);
                    case 5 -> searchItem(conn);
                    case 6 -> {
                        System.out.println("Exiting... Thank you!");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    static void createTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS items (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL
            );
            """;
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }

    static void addItem(Connection conn) throws SQLException {
        System.out.print("Enter Item ID: ");
        String id = scanner.nextLine();

        if (itemExists(conn, id)) {
            System.out.println("Item with this ID already exists.");
            return;
        }

        System.out.print("Enter Item Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Quantity: ");
        int qty = scanner.nextInt();

        System.out.print("Enter Price: Rs.");
        double price = scanner.nextDouble();
        scanner.nextLine();

        String sql = "INSERT INTO items (id, name, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setInt(3, qty);
            pstmt.setDouble(4, price);
            pstmt.executeUpdate();
            System.out.println("Item added successfully.");
        }
    }

    static void viewAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM items";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %s | Name: %s | Qty: %d | Price: Rs.%.2f\n",
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"));
            }
            if (!found) {
                System.out.println("Inventory is empty.");
            }
        }
    }

    static void updateQuantity(Connection conn) throws SQLException {
        System.out.print("Enter Item ID: ");
        String id = scanner.nextLine();

        if (!itemExists(conn, id)) {
            System.out.println("Item not found.");
            return;
        }

        System.out.print("Enter new quantity: ");
        int qty = scanner.nextInt();
        scanner.nextLine();

        String sql = "UPDATE items SET quantity = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, qty);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            System.out.println("Quantity updated.");
        }
    }

    static void removeItem(Connection conn) throws SQLException {
        System.out.print("Enter Item ID to remove: ");
        String id = scanner.nextLine();

        if (!itemExists(conn, id)) {
            System.out.println("Item not found.");
            return;
        }

        String sql = "DELETE FROM items WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            System.out.println("Item removed.");
        }
    }

    static void searchItem(Connection conn) throws SQLException {
        System.out.print("Enter Item ID to search: ");
        String id = scanner.nextLine();

        String sql = "SELECT * FROM items WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.printf("ID: %s | Name: %s | Qty: %d | Price: Rs.%.2f\n",
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"));
            } else {
                System.out.println("Item not found.");
            }
        }
    }

    static boolean itemExists(Connection conn, String id) throws SQLException {
        String sql = "SELECT id FROM items WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }
}
