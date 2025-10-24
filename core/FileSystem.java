package core;
import java.io.*;

import model.*;
import history.NavigationStack;
import history.RecentAccessCache;

import java.util.ArrayList;
import java.util.List;

public class FileSystem {
    private static final String SNAPSHOT_FILE = "smartfs_snapshot.ser";
    private static List<String> INDEX_CACHE = new ArrayList<>();
    private FileNode root;
    private FileNode current;
    private NavigationStack navStack;
    private FileIndexer indexer; 
    private RecentAccessCache accessCache;

    public FileSystem() {
        root = new FileNode("root", FileType.FOLDER, null);
        current = root;
        navStack = new NavigationStack();
        indexer = new FileIndexer(); 
        buildIndex(); 
        accessCache = new RecentAccessCache();
    }
    
    private void buildIndex() {
        indexer.clear(); 
        indexNode(root);
    }
    
    private void indexNode(FileNode node) {
        indexer.insert(node.getName());
        for (FileNode child : node.getChildren().values()) {
            indexNode(child);
        }
    }

    public void clearCache() {
    if (accessCache != null) {
        accessCache.clear();
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

    private void makeDirectory(String[] parts) {
        if (parts.length < 2) return;
        String folder = parts[1];
        if (current.getChildren().containsKey(folder)) {
        System.out.println("❌ A folder with the name '" + folder + "' already exists.");
        return;
    }
        current.getChildren().put(folder, new FileNode(folder, FileType.FOLDER, current));
        indexer.insert(folder); 
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
        indexer.insert(file); 
    }

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
            case "rm" -> removeFileOrFolder(parts);
            case "stat" -> showStats(parts);
            case "ac" -> handleAutocomplete(parts); 
            case "find" -> findFileOrFolder(parts); 
            case "index-export" -> exportIndex();
            case "index-import" -> importIndex();
            case "mv" -> moveItem(parts);
            case "mru" -> accessCache.printMRU();
            case "write" -> writeToFile(command); 
            case "read" -> readFromFile(parts);
            case "cp" -> copyItem(parts);
            case "snapshot" -> saveSnapshot(); 
            case "restore" -> restoreSnapshot();
            default -> System.out.println("Invalid command");
        }
    }

    private void saveSnapshot() {
        try (FileOutputStream fos = new FileOutputStream(SNAPSHOT_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            
            oos.writeObject(root); 
            System.out.println("📸 Snapshot saved successfully to " + SNAPSHOT_FILE);
        } catch (IOException e) {
            System.out.println("❌ Error saving snapshot: " + e.getMessage());
        }
    }


    private void restoreSnapshot() {
        File snapshot = new File(SNAPSHOT_FILE);
        if (!snapshot.exists()) {
            System.out.println("❌ No snapshot file found to restore.");
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(SNAPSHOT_FILE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            
            root = (FileNode) ois.readObject(); 
            current = root; 
            buildIndex(); 
            System.out.println("💿 File system restored successfully from snapshot.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("❌ Error restoring snapshot: " + e.getMessage());
        }
    }

    private void copyItem(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: cp <source_item_name> <destination_folder_name>");
            return;
        }
        
        String sourceName = parts[1];
        String destName = parts[2];

        FileNode sourceNode = current.getChildren().get(sourceName);
        FileNode destNode = current.getChildren().get(destName);

        if (sourceNode == null) {
            System.out.println("❌ Source item not found: " + sourceName);
            return;
        }
        if (destNode == null || destNode.getType() != FileType.FOLDER) {
            System.out.println("❌ Destination must be an existing folder: " + destName);
            return;
        }
        
        if (destNode.getChildren().containsKey(sourceName)) {
            System.out.println("❌ Cannot copy. Item with name '" + sourceName + "' already exists in destination.");
            return;
        }
        
        FileNode copiedNode = sourceNode.deepClone(destNode);
        
        destNode.getChildren().put(copiedNode.getName(), copiedNode);
        buildIndex(); 
        
        System.out.println("✅ Copied '" + sourceName + "' to '" + destName + "'.");
    }

    private void writeToFile(String command) {
        String[] parts = command.trim().split("\\s+", 3);
        
        if (parts.length < 3) {
            System.out.println("Usage: write <filename> \"<content>\"");
            return;
        }

        String fileName = parts[1];
        String content = parts[2];

        if (content.startsWith("\"") && content.endsWith("\"")) {
            content = content.substring(1, content.length() - 1);
        } else {
             System.out.println("❌ Content must be enclosed in double quotes.");
             return;
        }

        FileNode fileNode = current.getChildren().get(fileName);

        if (fileNode == null || fileNode.getType() != FileType.FILE) {
            System.out.println("❌ File not found or is a folder: " + fileName);
            return;
        }

        fileNode.setContent(content);
   
        accessCache.recordAccess(fileNode.getFullPath(), "write " + fileName); 
        
        System.out.println("📝 Wrote " + content.length() + " characters to " + fileName);
    }


    private void readFromFile(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: read <filename>");
            return;
        }
        
        String fileName = parts[1];
        FileNode fileNode = current.getChildren().get(fileName);

        if (fileNode == null || fileNode.getType() != FileType.FILE) {
            System.out.println("❌ File not found or is a folder: " + fileName);
            return;
        }

        String content = fileNode.getContent();
        
        System.out.println("--- Content of " + fileName + " ---");
        System.out.println(content);
        System.out.println("--------------------------------");
        
        accessCache.recordAccess(fileNode.getFullPath(), "read " + fileName); 
    }

    
    
    private void moveItem(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: mv <source_item_name> <destination_folder_name>");
            return;
        }
        
        String sourceName = parts[1];
        String destName = parts[2];

        FileNode sourceNode = current.getChildren().get(sourceName);
        if (sourceNode == null) {
            System.out.println("❌ Source item not found: " + sourceName);
            return;
        }

     
        FileNode destNode = current.getChildren().get(destName);
        if (destNode == null) {
            System.out.println("❌ Destination folder not found: " + destName);
            return;
        }
        if (destNode.getType() != FileType.FOLDER) {
            System.out.println("❌ Destination must be a folder.");
            return;
        }
        
        if (destNode.getChildren().containsKey(sourceName)) {
            System.out.println("❌ Cannot move. Item with name '" + sourceName + "' already exists in destination.");
            return;
        }
        
   
        current.getChildren().remove(sourceName);
       
        sourceNode.setParent(destNode);
 
        destNode.getChildren().put(sourceName, sourceNode);
        
        System.out.println("✅ Moved '" + sourceName + "' to '" + destName + "'.");
    }
    
    private void exportIndex() {
        INDEX_CACHE = indexer.getAllIndexedWords();
        System.out.println("💾 File index exported successfully. Total words: " + INDEX_CACHE.size());
    }

    private void importIndex() {
        if (INDEX_CACHE.isEmpty()) {
            System.out.println("❌ Index cache is empty. Nothing to import.");
            return;
        }
        indexer.importWords(INDEX_CACHE);
        System.out.println("💿 File index imported successfully. Total words: " + INDEX_CACHE.size());
    }

    private void removeFileOrFolder(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: rm <file_or_folder>");
            return;
        }
        String name = parts[1];
        FileNode nodeToRemove = current.getChildren().get(name);

        if (nodeToRemove == null) {
            System.out.println("Item not found: " + name);
            return;
        }

        if (nodeToRemove.getType() == FileType.FOLDER) {
            if (!nodeToRemove.getChildren().isEmpty()) {
                System.out.println("❌ Folder is not empty. Cannot delete non-empty folders.");
                return;
            }
        }
        
        current.getChildren().remove(name);
        
        buildIndex(); 
        
        System.out.println("✅ Removed: " + name);
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
        System.out.println("- Path: " + node.getFullPath());
        if (node.getType() == FileType.FILE && node.getMeta() != null) {
            System.out.println("- Size: " + node.getMeta().getSize() + " bytes");
            System.out.println("- Created At: " + node.getMeta().getCreatedAt());
        } else if (node.getType() == FileType.FOLDER) {
            System.out.println("- Contains: " + node.getChildren().size() + " items");
        }
    }

    private void findFileOrFolder(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: find <name>");
            return;
        }
        String name = parts[1];
        
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