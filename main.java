import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

enum TableStatus {
    AVAILABLE,
    OCCUPIED,
    CLEANING
}

class RestaurantTable {
    private final int tableNumber;
    private int capacity;
    private TableStatus status;
    private int customerCount;

    public RestaurantTable(int tableNumber, int capacity) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = TableStatus.AVAILABLE;
        this.customerCount = 0;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public TableStatus getStatus() {
        return status;
    }

    public int getCustomerCount() {
        return customerCount;
    }

    public boolean assign(int customers) {
        if (status != TableStatus.AVAILABLE) {
            System.out.println("The table is not available.");
            return false;
        }

        if (customers > capacity) {
            System.out.println("Not enough seats in the table capacity.");
            return false;
        }

        customerCount = customers;
        status = TableStatus.OCCUPIED;
        System.out.println("Customers assigned successfully.");
        return true;
    }

    public void customersLeave() {
        if (status != TableStatus.OCCUPIED) {
            System.out.println("The table is not occupied.");
            return;
        }

        customerCount = 0;
        status = TableStatus.CLEANING;
        System.out.println("Customers left; the table needs cleaning.");
    }

    public void finishCleaning() {
        if (status != TableStatus.CLEANING) {
            System.out.println("The table is not being cleaned.");
            return;
        }

        status = TableStatus.AVAILABLE;
        System.out.println("Cleaning completed. The table is now available.");
    }

    public void updateCapacity(int newCapacity) {
        if (status != TableStatus.AVAILABLE) {
            System.out.println("Capacity can only be changed when the table is free.");
            return;
        }

        if (newCapacity <= 0) {
            System.out.println("Capacity must be greater than zero.");
            return;
        }

        capacity = newCapacity;
        System.out.println("Capacity updated successfully.");
    }

    public void display() {
        System.out.printf("%-12d %-10d %-12s %-10d%n",
                tableNumber, capacity, status, customerCount);
    }
}

public class main {
    private static final Map<Integer, RestaurantTable> tables = new HashMap<>();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        boolean run = true;

        while (run) {
            System.out.println("\nRestaurant Table Management");
            System.out.println("------------------------------------------------");
            System.out.println("1. Add table");
            System.out.println("2. View tables");
            System.out.println("3. Assign customers");
            System.out.println("4. Customers leave");
            System.out.println("5. Finish cleaning");
            System.out.println("6. Update seating capacity");
            System.out.println("7. Show statistics");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            int choice;
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
            } else {
                System.out.println("Invalid choice.");
                sc.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    addTable();
                    break;
                case 2:
                    viewTables();
                    break;
                case 3:
                    assignCustomers();
                    break;
                case 4:
                    customersLeave();
                    break;
                case 5:
                    finishCleaning();
                    break;
                case 6:
                    updateCapacity();
                    break;
                case 7:
                    showStatistics();
                    break;
                case 8:
                    run = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }

        sc.close();
    }

    private static void addTable() {
        System.out.print("Enter table number: ");
        int tableNumber = sc.nextInt();
        System.out.print("Enter table capacity: ");
        int capacity = sc.nextInt();

        if (tables.containsKey(tableNumber)) {
            System.out.println("A table with that number already exists.");
            return;
        }

        tables.put(tableNumber, new RestaurantTable(tableNumber, capacity));
        System.out.println("Table added successfully.");
    }

    private static void viewTables() {
        if (tables.isEmpty()) {
            System.out.println("No tables available.");
            return;
        }

        printHeader();
        for (RestaurantTable table : sortedTables()) {
            table.display();
        }
    }

    private static void assignCustomers() {
        System.out.print("Enter table number: ");
        int tableNumber = sc.nextInt();
        RestaurantTable table = tables.get(tableNumber);

        if (table == null) {
            System.out.println("Table not found.");
            return;
        }

        System.out.print("Enter number of customers: ");
        int customers = sc.nextInt();
        table.assign(customers);
    }

    private static void customersLeave() {
        System.out.print("Enter table number: ");
        int tableNumber = sc.nextInt();
        RestaurantTable table = tables.get(tableNumber);

        if (table == null) {
            System.out.println("Table not found.");
            return;
        }

        table.customersLeave();
    }

    private static void finishCleaning() {
        System.out.print("Enter table number: ");
        int tableNumber = sc.nextInt();
        RestaurantTable table = tables.get(tableNumber);

        if (table == null) {
            System.out.println("Table not found.");
            return;
        }

        table.finishCleaning();
    }

    private static void updateCapacity() {
        System.out.print("Enter table number: ");
        int tableNumber = sc.nextInt();
        RestaurantTable table = tables.get(tableNumber);

        if (table == null) {
            System.out.println("Table not found.");
            return;
        }

        System.out.print("Enter the new capacity: ");
        int newCapacity = sc.nextInt();
        table.updateCapacity(newCapacity);
    }

    private static void showStatistics() {
        int available = 0;
        int occupied = 0;
        int cleaning = 0;
        int totalCustomers = 0;

        for (RestaurantTable table : tables.values()) {
            switch (table.getStatus()) {
                case AVAILABLE:
                    available++;
                    break;
                case OCCUPIED:
                    occupied++;
                    break;
                case CLEANING:
                    cleaning++;
                    break;
                default:
                    break;
            }
            totalCustomers += table.getCustomerCount();
        }

        System.out.println("\n====== RESTAURANT STATISTICS ======");
        System.out.println("Total Tables: " + tables.size());
        System.out.println("Available Tables: " + available);
        System.out.println("Occupied Tables: " + occupied);
        System.out.println("Cleaning Tables: " + cleaning);
        System.out.println("Currently Seated Customers: " + totalCustomers);
    }

    private static List<RestaurantTable> sortedTables() {
        List<RestaurantTable> list = new ArrayList<>(tables.values());
        list.sort(Comparator.comparingInt(RestaurantTable::getTableNumber));
        return list;
    }

    private static void printHeader() {
        System.out.println("\n----------------------------------------------------");
        System.out.printf("%-12s %-10s %-12s %-10s%n", "Table No", "Capacity", "Status", "Customers");
        System.out.println("----------------------------------------------------");
    }
}
