import java.util.*;
import java.io.*;

class Account {
    String id, name;
    double balance;
    Stack<String> history = new Stack<>();

    Account(String id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }
}

public class Main {

    static LinkedList<Account> accounts = new LinkedList<>();
    static HashMap<String, Account> map = new HashMap<>();
    static Queue<String> requests = new LinkedList<>();
    static int count = 101;

    static Scanner sc = new Scanner(System.in);

    static Account find(String id) {
        return map.get(id);
    }

    static void createAccount() {
        System.out.print("Enter name: ");
        String name = sc.nextLine();

        System.out.print("Enter opening balance: ");
        double balance = sc.nextDouble();
        sc.nextLine();

        if (name.trim().isEmpty() || balance < 0) {
            System.out.println("Invalid details!");
            return;
        }

        String id = "ACC" + count++;

        Account a = new Account(id, name, balance);
        accounts.add(a);
        map.put(id, a);

        System.out.println("Account created: " + id);
    }

    static void deposit() {
        System.out.print("Enter account ID: ");
        String id = sc.nextLine();

        Account a = find(id);

        if (a == null) {
            System.out.println("Account not found!");
            return;
        }

        System.out.print("Enter amount: ");
        double amount = sc.nextDouble();
        sc.nextLine();

        if (amount <= 0) {
            System.out.println("Invalid amount!");
            return;
        }

        a.balance += amount;
        a.history.push("Deposit: " + amount);

        System.out.println("Deposit successful!");
    }

    static void withdraw() {
        System.out.print("Enter account ID: ");
        String id = sc.nextLine();

        Account a = find(id);

        if (a == null) {
            System.out.println("Account not found!");
            return;
        }

        System.out.print("Enter amount: ");
        double amount = sc.nextDouble();
        sc.nextLine();

        if (amount <= 0 || amount > a.balance) {
            System.out.println("Invalid amount or insufficient balance!");
            return;
        }

        a.balance -= amount;
        a.history.push("Withdraw: " + amount);

        System.out.println("Withdrawal successful!");
    }

    static void checkBalance() {
        System.out.print("Enter account ID: ");
        String id = sc.nextLine();

        Account a = find(id);

        if (a != null)
            System.out.println("Balance: " + a.balance);
        else
            System.out.println("Account not found!");
    }

    static void history() {
        System.out.print("Enter account ID: ");
        String id = sc.nextLine();

        Account a = find(id);

        if (a == null) {
            System.out.println("Account not found!");
            return;
        }

        if (a.history.isEmpty()) {
            System.out.println("No transactions!");
        } else {
            System.out.println(a.history);
        }
    }

    static void addRequest() {
        System.out.print("Enter service request: ");
        String request = sc.nextLine();

        requests.add(request);
        System.out.println("Request added!");
    }

    static void processRequest() {
        if (requests.isEmpty()) {
            System.out.println("No pending requests!");
        } else {
            System.out.println("Processed: " + requests.poll());
        }
    }

    static void save() {
        try {
            PrintWriter p = new PrintWriter("bank_accounts.txt");

            for (Account a : accounts) {
                p.println(a.id + "," + a.name + "," + a.balance);
            }

            p.close();
            System.out.println("Data saved successfully!");

        } catch (Exception e) {
            System.out.println("File error!");
        }
    }

    public static void main(String[] args) {

        int choice;

        do {
            System.out.println("\n===== BANK MANAGEMENT SYSTEM =====");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Check Balance");
            System.out.println("5. Transaction History");
            System.out.println("6. Add Service Request");
            System.out.println("7. Process Request");
            System.out.println("8. Save Data");
            System.out.println("0. Exit");

            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: createAccount(); break;
                case 2: deposit(); break;
                case 3: withdraw(); break;
                case 4: checkBalance(); break;
                case 5: history(); break;
                case 6: addRequest(); break;
                case 7: processRequest(); break;
                case 8: save(); break;
                case 0:
                    save();
                    System.out.println("Thank you!");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }

        } while (choice != 0);

        sc.close();
    }
}