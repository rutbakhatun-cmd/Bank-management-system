import java.io.*;
import java.util.*;

public class Main {
    private static BankService bank = new BankService("bank_accounts.txt");

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int loaded = bank.load();

        System.out.println("=== Bank Management System ===");
        System.out.println(loaded + " account(s) loaded from bank_accounts.txt");

        while (true) {
            System.out.println("\n1. Create account   2. Search account   3. Deposit   4. Withdraw");
            System.out.println("5. Check balance    6. Recent transactions   7. Undo last transaction");
            System.out.println("8. Add service request   9. Process next request   0. Save & exit");
            System.out.print("Choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Name: ");
                    String name = sc.nextLine();
                    double openBal = readAmount(sc, "Opening balance: ");
                    if (!Double.isNaN(openBal)) {
                        System.out.println(bank.createAccount(name, openBal));
                    }
                    break;

                case "2":
                    System.out.println(bank.search(readId(sc)));
                    break;

                case "3":
                    String depId = readId(sc);
                    double depAmt = readAmount(sc, "Amount: ");
                    if (!Double.isNaN(depAmt)) {
                        System.out.println(bank.deposit(depId, depAmt));
                    }
                    break;

                case "4":
                    String witId = readId(sc);
                    double witAmt = readAmount(sc, "Amount: ");
                    if (!Double.isNaN(witAmt)) {
                        System.out.println(bank.withdraw(witId, witAmt));
                    }
                    break;

                case "5":
                    System.out.println(bank.balance(readId(sc)));
                    break;

                case "6":
                    System.out.println(bank.history(readId(sc)));
                    break;

                case "7":
                    System.out.println(bank.undo(readId(sc)));
                    break;

                case "8":
                    String reqId = readId(sc);
                    System.out.print("Request type (DEPOSIT / WITHDRAW / BALANCE): ");
                    String reqType = sc.nextLine().trim().toUpperCase();
                    double reqAmt = 0;
                    if (!reqType.equals("BALANCE")) {
                        reqAmt = readAmount(sc, "Amount: ");
                        if (Double.isNaN(reqAmt)) break;
                    }
                    System.out.println(bank.addRequest(reqId, reqType, reqAmt));
                    break;

                case "9":
                    System.out.println(bank.processNextRequest());
                    break;

                case "0":
                    bank.save();
                    System.out.println("Accounts saved to bank_accounts.txt. Goodbye!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please enter 0-9.");
            }
        }
    }

    private static String readId(Scanner sc) {
        System.out.print("Account ID: ");
        return sc.nextLine().trim();
    }

    private static double readAmount(Scanner sc, String msg) {
        System.out.print(msg);
        String input = sc.nextLine().trim();
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Error: invalid amount.");
            return Double.NaN;
        }
    }
}

class Transaction {
    private String type;
    private double amount;

    public Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        String formattedAmt = (amount == (long) amount) ? String.valueOf((long) amount) : String.valueOf(amount);
        return type + " " + formattedAmt;
    }
}

class ServiceRequest {
    private String accountId;
    private String type;
    private double amount;

    public ServiceRequest(String accountId, String type, double amount) {
        this.accountId = accountId;
        this.type = type.toUpperCase();
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }
}

class Account {
    private String id;
    private String name;
    private double balance;
    private Stack<Transaction> history;

    public Account(String id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.history = new Stack<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public Stack<Transaction> getHistory() {
        return history;
    }

    public void deposit(double amount) {
        balance += amount;
        history.push(new Transaction("Deposit", amount));
    }

    public void withdraw(double amount) {
        balance -= amount;
        history.push(new Transaction("Withdraw", amount));
    }

    public Transaction undoLast() {
        if (history.isEmpty()) {
            return null;
        }
        Transaction last = history.pop();
        if (last.getType().equals("Deposit")) {
            balance -= last.getAmount();
        } else if (last.getType().equals("Withdraw")) {
            balance += last.getAmount();
        }
        return last;
    }
}

class BankService {
    private LinkedList<Account> accounts;
    private HashMap<String, Account> index;
    private Queue<ServiceRequest> pending;
    private String dataFile;
    private int nextIdNumber;

    public BankService(String dataFile) {
        this.accounts = new LinkedList<>();
        this.index = new HashMap<>();
        this.pending = new LinkedList<>();
        this.dataFile = dataFile;
        this.nextIdNumber = 101;
    }

    private String formatAmount(double amt) {
        return (amt == (long) amt) ? String.valueOf((long) amt) : String.valueOf(amt);
    }

    public String createAccount(String name, double bal) {
        if (name == null || name.trim().isEmpty()) {
            return "Error: name cannot be empty.";
        }
        if (bal < 0) {
            return "Error: balance cannot be negative.";
        }

        String id = "ACC" + nextIdNumber++;
        while (index.containsKey(id)) {
            id = "ACC" + nextIdNumber++;
        }

        Account acc = new Account(id, name.trim(), bal);
        accounts.add(acc);
        index.put(id, acc);

        return "Account created: " + id + " | " + acc.getName() + " | Balance " + formatAmount(bal);
    }

    public String search(String id) {
        Account acc = index.get(id.toUpperCase());
        if (acc == null) {
            return "Account " + id + " not found.";
        }
        return "Account found: " + acc.getId() + " | " + acc.getName() + " | Balance " + formatAmount(acc.getBalance());
    }

    public String deposit(String id, double amt) {
        Account acc = index.get(id.toUpperCase());
        if (acc == null) {
            return "Account " + id + " not found.";
        }
        if (amt < 0) {
            return "Error: amount cannot be negative.";
        }

        acc.deposit(amt);
        return "Deposited " + formatAmount(amt) + ". Balance: " + formatAmount(acc.getBalance());
    }

    public String withdraw(String id, double amt) {
        Account acc = index.get(id.toUpperCase());
        if (acc == null) {
            return "Account " + id + " not found.";
        }
        if (amt < 0) {
            return "Error: amount cannot be negative.";
        }
        if (amt > acc.getBalance()) {
            return "Error: insufficient balance.";
        }

        acc.withdraw(amt);
        return "Withdrawn " + formatAmount(amt) + ". Balance: " + formatAmount(acc.getBalance());
    }

    public String balance(String id) {
        Account acc = index.get(id.toUpperCase());
        if (acc == null) {
            return "Account " + id + " not found.";
        }
        return "Balance: " + formatAmount(acc.getBalance());
    }

    public String history(String id) {
        Account acc = index.get(id.toUpperCase());
        if (acc == null) {
            return "Account " + id + " not found.";
        }

        Stack<Transaction> stack = acc.getHistory();
        if (stack.isEmpty()) {
            return "No recent transactions for " + acc.getId() + ".";
        }

        StringBuilder sb = new StringBuilder("Recent transactions: ");
        for (int i = stack.size() - 1; i >= 0; i--) {
            sb.append(stack.get(i).toString());
            if (i > 0) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String undo(String id) {
        Account acc = index.get(id.toUpperCase());
        if (acc == null) {
            return "Account " + id + " not found.";
        }

        Stack<Transaction> stack = acc.getHistory();
        if (stack.isEmpty()) {
            return "Error: no transaction to undo.";
        }

        Transaction top = stack.peek();
        if (top.getType().equals("Deposit") && acc.getBalance() < top.getAmount()) {
            return "Error: cannot undo deposit due to insufficient balance.";
        }

        Transaction undone = acc.undoLast();

        if (undone == null) {
            return "Error: no transaction to undo.";
        }
        return "Undone: " + undone + ". Balance: " + formatAmount(acc.getBalance());
    }

    public String addRequest(String id, String type, double amt) {
        Account acc = index.get(id.toUpperCase());
        if (acc == null) {
            return "Account " + id + " not found.";
        }
        type = type.toUpperCase();
        if (!type.equals("DEPOSIT") && !type.equals("WITHDRAW") && !type.equals("BALANCE")) {
            return "Error: request type must be DEPOSIT, WITHDRAW or BALANCE.";
        }
        if (!type.equals("BALANCE") && amt < 0) {
            return "Error: amount cannot be negative.";
        }

        pending.offer(new ServiceRequest(acc.getId(), type, amt));   // enqueue: O(1)
        return "Request added to queue. Pending requests: " + pending.size();
    }

    public String processNextRequest() {
        ServiceRequest req = pending.poll();                         // dequeue: O(1)
        if (req == null) {
            return "No pending requests.";
        }

        String result;
        if (req.getType().equals("DEPOSIT")) {
            result = deposit(req.getAccountId(), req.getAmount());
        } else if (req.getType().equals("WITHDRAW")) {
            result = withdraw(req.getAccountId(), req.getAmount());
        } else {
            result = balance(req.getAccountId());
        }
        return "Processed " + req.getType() + " for " + req.getAccountId() + " -> " + result;
    }

    // Reads lines of the form id,name,balance. Bad, duplicate or negative lines are skipped.
    public int load() {
        File file = new File(dataFile);
        if (!file.exists()) {
            return 0;
        }

        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue;

                String id = parts[0].trim().toUpperCase();
                String name = parts[1].trim();
                if (!id.matches("ACC\\d+") || name.isEmpty() || index.containsKey(id)) continue;

                double bal;
                try {
                    bal = Double.parseDouble(parts[2].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (Double.isNaN(bal) || Double.isInfinite(bal) || bal < 0) continue;

                Account acc = new Account(id, name, bal);
                accounts.add(acc);
                index.put(id, acc);
                nextIdNumber = Math.max(nextIdNumber, Integer.parseInt(id.substring(3)) + 1);
                count++;
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading " + dataFile + ": " + e.getMessage());
        }
        return count;
    }

    public void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(dataFile))) {
            for (Account acc : accounts) {
                pw.println(acc.getId() + "," + acc.getName().replace(",", " ") + "," + formatAmount(acc.getBalance()));
            }
        } catch (IOException e) {
            System.out.println("Error saving " + dataFile + ": " + e.getMessage());
        }
    }
}
