
import core.FileSystem;
import history.AccessHistory;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FileSystem fileSystem = new FileSystem();
        AccessHistory accessHistory = new AccessHistory();
        Scanner scanner = new Scanner(System.in);

        System.out.println("🧠 Smart File Simulator with History Support");
        System.out.println("Type 'exit' to quit, 'access' to see visited items\n");

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) break;
            if (input.equalsIgnoreCase("access")) {
                accessHistory.printAccessHistory();
                continue;
            }
            
            if(input.equalsIgnoreCase("clear history") || input.equalsIgnoreCase("clear")) {
                accessHistory.clearHistory();
                fileSystem.clearCache(); 
                System.out.println("✅ Access history and MRU cache cleared.");
                continue;
            }
            
            if(input.equalsIgnoreCase("clear history") || input.equalsIgnoreCase("clear")) {
                accessHistory.clearHistory();
                System.out.println("✅ Access history cleared.");
                continue;
            }

            if (input.startsWith("cd") || input.startsWith("open") || input.startsWith("touch") || input.startsWith("mkdir")) {
                accessHistory.visit(input);
            }
            

             fileSystem.handleCommand(input);
        }

        System.out.println("👋 Simulator ended. Goodbye!");
        scanner.close();
    }
}
