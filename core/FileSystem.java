package core;

import model.*;
import history.NavigationStack;
import java.util.List;

public class FileSystem {
    private FileNode root;
    private FileNode current;
    private NavigationStack navStack;
    private FileIndexer indexer; // 1. Add FileIndexer

    public FileSystem() {
        root = new FileNode("root", FileType.FOLDER, null);
        current = root;
        navStack = new NavigationStack();
        indexer = new FileIndexer(); // Initialize the indexer
        buildIndex(); // Build the initial index
    }
    
    // --- New Indexing/Utility Methods ---
    
    /** Recursively inserts all file/folder names into the indexer. */
    private void buildIndex() {
        indexer.clear(); // Clear before rebuilding
        indexNode(root);
    }
    
    private void indexNode(FileNode node) {
        indexer.insert(node.getName());
        for (FileNode child : node.getChildren().values()) {
            indexNode(child);
        }
    }
    
    private void handleAutocomplete(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: ac <prefix>");
            return;
        }
        String prefix = parts[1];
        List<String> suggestions = indexer.search(prefix);
        
        System.out.println("💡 Suggestions for '" + prefix + "':");
        if (suggestions.isEmpty()) {
            System.out.println("  No matches found.");
        } else {
            for (String suggestion : suggestions) {
                System.out.println("- " + suggestion);
            }
        }
    }
    
    // --- Modified File Command Methods to Update Index ---

    private void makeDirectory(String[] parts) {
        if (parts.length < 2) return;
        String folder = parts[1];
        if (current.getChildren().containsKey(folder)) {
        System.out.println("❌ A folder with the name '" + folder + "' already exists.");
        return;
    }
        current.getChildren().put(folder, new FileNode(folder, FileType.FOLDER, current));
        indexer.insert(folder); // Index the new folder name
    }

    private void createFile(String[] parts) {
        if (parts.length < 2) return;
        String file = parts[1];
        if (current.getChildren().containsKey(file)) {
        System.out.println("❌ A file with the name '" + file + "' already exists.");
        return;
    }
        FileNode f = new FileNode(file, FileType.FILE, current);
        f.setMeta(new FileMeta(file.length() * 10));
        current.getChildren().put(file, f);
        indexer.insert(file); // Index the new file name
    }
    
    // --- Modified handleCommand ---

    public void handleCommand(String command) {
        String[] parts = command.trim().split("\\s+");
        String cmd = parts[0];

        switch (cmd) {
            case "pwd" -> System.out.println(current.getName());
            case "ls" -> list();
            case "cd" -> changeDirectory(parts);
            case "mkdir" -> makeDirectory(parts);
            case "touch" -> createFile(parts);
            case "open" -> openFile(parts);
            case "history" -> navStack.printHistory();
            case "back" -> back();
            case "stat" -> showStats(parts);
            case "ac" -> handleAutocomplete(parts); // Assuming previous step completed
            case "find" -> findFileOrFolder(parts); // New command
            default -> System.out.println("Invalid command");
        }
    }

    private void showStats(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: stat <file_or_folder>");
            return;
        }
        String name = parts[1];
        FileNode node = current.getChildren().get(name);

        if (node == null) {
            System.out.println("Item not found: " + name);
            return;
        }

        System.out.println("📊 Stats for: " + node.getName());
        System.out.println("- Type: " + node.getType());
        System.out.println("- Path: " + node.getFullPath()); // Use the new getFullPath()

        // Only show meta for files
        if (node.getType() == FileType.FILE && node.getMeta() != null) {
            System.out.println("- Size: " + node.getMeta().getSize() + " bytes");
            System.out.println("- Created At: " + node.getMeta().getCreatedAt());
        } else if (node.getType() == FileType.FOLDER) {
            System.out.println("- Contains: " + node.getChildren().size() + " items");
        }
    }

    // New method for handling the 'find' command
    private void findFileOrFolder(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: find <name>");
            return;
        }
        String name = parts[1];
        
        // Use the new search utility, starting from the global root
        List<SearchResult> results = FileSearcher.searchByName(root, name);

        if (results.isEmpty()) {
            System.out.println("No files or folders found matching '" + name + "'.");
        } else {
            System.out.println("🔎 Found " + results.size() + " matches:");
            results.forEach(System.out::println);
        }
    }
    private void list() {
        current.getChildren().forEach((name, node) ->
            System.out.println((node.getType() == FileType.FOLDER ? "[DIR] " : "[FILE] ") + name)
        );
    }

    private void changeDirectory(String[] parts) {
        if (parts.length < 2) return;
        String folder = parts[1];
        if (folder.equals("..")) {
            if (current.getParent() != null) current = current.getParent();
        } else if (current.getChildren().containsKey(folder)
                && current.getChildren().get(folder).getType() == FileType.FOLDER) {
            navStack.push(current.getName());
            current = current.getChildren().get(folder);
        } else {
            System.out.println("Folder not found");
        }
    }

    private void openFile(String[] parts) {
        if (parts.length < 2) return;
        String file = parts[1];
        if (current.getChildren().containsKey(file) &&
            current.getChildren().get(file).getType() == FileType.FILE) {
            System.out.println("📂 Opened " + file);
            navStack.push("Opened " + file);
            
        } else {
            System.out.println("File not found.");
        }
    }

    private void back() {
        String prev = navStack.pop();
        if (prev != null) {
            System.out.println("↩️  Back to: " + prev);
        }
    }
}