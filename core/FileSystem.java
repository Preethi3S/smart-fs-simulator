package core;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import history.NavigationStack;
import history.RecentAccessCache;
import history.UndoManager;
import model.FileType;
import model.Inode;
import model.SearchResult;

public class FileSystem {
    private static final String SNAPSHOT_FILE = "smartfs_snapshot.ser";
    private static List<String> INDEX_CACHE = new ArrayList<>();
    private FileNode root;
    private FileNode current;
    private NavigationStack navStack;
    private FileIndexer indexer; 
    private RecentAccessCache accessCache;
    private UndoManager undoManager;

    public FileSystem() {
        Inode rootInode = new Inode(FileType.FOLDER);
        root = new FileNode("root", null, rootInode);
        current = root;
        navStack = new NavigationStack();
        indexer = new FileIndexer(); 
        buildIndex(); 
        accessCache = new RecentAccessCache();
        undoManager = new UndoManager();
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
        
        System.out.println("Suggestions for '" + prefix + "':");
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
            System.out.println("Error: A folder with the name '" + folder + "' already exists.");
            return;
        }
        
        UndoManager.Command cmd = new UndoManager.Command() {
            FileNode createdNode;
            @Override
            public void execute() {
                Inode inode = new Inode(FileType.FOLDER);
                createdNode = new FileNode(folder, current, inode);
                current.getChildren().put(folder, createdNode);
                indexer.insert(folder);
            }
            @Override
            public void undo() {
                current.getChildren().remove(folder);
                buildIndex(); // Rebuild index to remove
            }
        };
        undoManager.executeCommand(cmd);
    }

    private void createFile(String[] parts) {
        if (parts.length < 2) return;
        String file = parts[1];
        if (current.getChildren().containsKey(file)) {
            System.out.println("Error: A file with the name '" + file + "' already exists.");
            return;
        }
        
        UndoManager.Command cmd = new UndoManager.Command() {
            FileNode createdNode;
            @Override
            public void execute() {
                Inode inode = new Inode(FileType.FILE);
                inode.setSize(file.length() * 10); // Dummy size logic
                createdNode = new FileNode(file, current, inode);
                current.getChildren().put(file, createdNode);
                indexer.insert(file);
            }
            @Override
            public void undo() {
                current.getChildren().remove(file);
                buildIndex();
            }
        };
        undoManager.executeCommand(cmd);
    }

    public void handleCommand(String command) {
        String[] parts = command.trim().split("\\s+");
        String cmd = parts[0];

        switch (cmd) {
            case "pwd" -> System.out.println(current.getName());
            case "ls" -> list(parts);
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
            case "tree" -> showTree();
            case "du" -> showDiskUsage(parts);
            case "chmod" -> changePermissions(parts);
            case "grep" -> grepFile(parts);
            case "cat" -> catFile(parts);
            case "ln" -> createLink(parts);
            case "fsck" -> performFsck();
            case "undo" -> undoManager.undoCommand();
            case "redo" -> undoManager.redoCommand();
            case "help" -> showHelp();
            default -> System.out.println("Invalid command. Type 'help' for available commands.");
        }
    }

    private void showHelp() {
        System.out.println("Available Commands:");
        System.out.println("  ls [-l]             - List files (use -l for detailed view)");
        System.out.println("  pwd                 - Print working directory");
        System.out.println("  cd <dir>            - Change directory (.. for parent)");
        System.out.println("  mkdir <dir>         - Create a directory");
        System.out.println("  touch <file>        - Create a file");
        System.out.println("  rm [-r] <name>      - Remove a file or folder");
        System.out.println("  mv <src> <dest>     - Move a file or folder");
        System.out.println("  cp <src> <dest>     - Copy a file or folder");
        System.out.println("  ln [-s] <src> <dst> - Create a hard link or symlink (-s)");
        System.out.println("  write <file> \"txt\"  - Write content to a file");
        System.out.println("  read <file>         - Read content of a file");
        System.out.println("  cat <file>          - Display file content");
        System.out.println("  grep <txt> <file>   - Search text inside a file");
        System.out.println("  chmod <perm> <file> - Change permissions");
        System.out.println("  stat <name>         - Show file/folder statistics");
        System.out.println("  find <name>         - Search for files/folders");
        System.out.println("  tree                - Show directory structure tree");
        System.out.println("  du [name]           - Show disk usage");
        System.out.println("  fsck                - Check file system consistency");
        System.out.println("  undo                - Undo last operation");
        System.out.println("  redo                - Redo last operation");
        System.out.println("  history             - Show navigation history");
        System.out.println("  access              - Show access history");
        System.out.println("  mru                 - Show most recently used items");
        System.out.println("  snapshot            - Save file system state");
        System.out.println("  restore             - Restore file system state");
        System.out.println("  exit                - Exit the simulator");
    }

    private void performFsck() {
        System.out.println("Starting File System Consistency Check...");
        
        Map<Integer, Integer> observedRefCounts = new HashMap<>();
        Map<Integer, Inode> inodeMap = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        // Traverse the entire tree
        checkNodeConsistency(root, observedRefCounts, inodeMap, issues);
        
        // Verify Ref Counts
        for (Map.Entry<Integer, Inode> entry : inodeMap.entrySet()) {
            int inodeId = entry.getKey();
            Inode inode = entry.getValue();
            int expected = inode.getRefCount();
            int actual = observedRefCounts.getOrDefault(inodeId, 0);
            
            if (expected != actual) {
                issues.add("RefCount Mismatch for Inode " + inodeId + " (" + inode.getType() + "): Expected " + expected + ", Found " + actual);
            }
        }
        
        if (issues.isEmpty()) {
            System.out.println("File System is consistent. No errors found.");
        } else {
            System.out.println("Found " + issues.size() + " consistency issues:");
            for (String issue : issues) {
                System.out.println("  - " + issue);
            }
        }
    }

    private void checkNodeConsistency(FileNode node, Map<Integer, Integer> counts, Map<Integer, Inode> inodeMap, List<String> issues) {
        Inode inode = node.getInode();
        counts.merge(inode.getId(), 1, Integer::sum);
        inodeMap.putIfAbsent(inode.getId(), inode);
        
        // Check Parent Pointer Consistency
        if (node != root) {
            if (node.getParent() == null) {
                issues.add("Orphaned Node: " + node.getName() + " (Parent is null)");
            } else if (!node.getParent().getChildren().containsValue(node)) {
                issues.add("Parent-Child Link Broken: " + node.getName() + " points to parent " + node.getParent().getName() + ", but parent does not contain it.");
            }
        }
        
        // Check Symlinks
        if (node.getType() == FileType.SYMLINK) {
            String targetPath = node.getTargetPath();
            if (resolvePath(targetPath) == null) {
                issues.add("Broken Symbolic Link: " + node.getName() + " -> " + targetPath);
            }
        }
        
        // Recurse if folder
        if (node.getType() == FileType.FOLDER) {
            for (FileNode child : node.getChildren().values()) {
                checkNodeConsistency(child, counts, inodeMap, issues);
            }
        }
    }

    private void showTree() {
        System.out.println(".");
        printTree(current, "", true, new HashSet<>());
    }

    private void printTree(FileNode node, String prefix, boolean isTail, Set<Integer> visitedInodes) {
        if (visitedInodes.contains(node.getInode().getId())) {
            System.out.println(prefix + (isTail ? "└── " : "├── ") + node.getName() + " [CYCLE DETECTED]");
            return;
        }
        visitedInodes.add(node.getInode().getId());

        List<FileNode> children = new ArrayList<>(node.getChildren().values());
        for (int i = 0; i < children.size(); i++) {
            FileNode child = children.get(i);
            boolean isLast = (i == children.size() - 1);
            System.out.println(prefix + (isLast ? "└── " : "├── ") + child.getName() + 
                (child.getType() == FileType.SYMLINK ? " -> " + child.getTargetPath() : ""));
            
            if (child.getType() == FileType.FOLDER) {
                printTree(child, prefix + (isLast ? "    " : "│   "), false, new HashSet<>(visitedInodes));
            }
        }
    }

    private void showDiskUsage(String[] parts) {
        FileNode target = current;
        if (parts.length > 1) {
            String name = parts[1];
            target = resolvePath(name);
            if (target == null) {
                System.out.println("Item not found: " + name);
                return;
            }
        }
        int totalSize = calculateSize(target, new HashSet<>());
        System.out.println("Disk usage of " + target.getName() + ": " + totalSize + " bytes");
    }

    private int calculateSize(FileNode node, Set<Integer> visitedInodes) {
        if (visitedInodes.contains(node.getInode().getId())) return 0; // Avoid cycles
        visitedInodes.add(node.getInode().getId());

        int size = 0;
        if (node.getType() == FileType.FILE) {
            size += node.getSize();
        } else if (node.getType() == FileType.FOLDER) {
            for (FileNode child : node.getChildren().values()) {
                size += calculateSize(child, visitedInodes);
            }
        }
        return size;
    }

    private FileNode resolvePath(String path) {
        return resolvePath(path, 0);
    }

    private FileNode resolvePath(String path, int depth) {
        if (depth > 40) {
            System.out.println("Error: Too many levels of symbolic links.");
            return null;
        }
        
        if (path.equals("/")) return root;
        
        String[] parts = path.split("/");
        FileNode node = path.startsWith("/") ? root : current;
        
        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (node.getParent() != null) node = node.getParent();
                continue;
            }
            
            if (node.getType() != FileType.FOLDER) return null;
            
            FileNode next = node.getChildren().get(part);
            if (next == null) return null;
            
            // Follow symlinks
            if (next.getType() == FileType.SYMLINK) {
                String targetPath = next.getTargetPath();
                FileNode target = resolvePath(targetPath, depth + 1);
                if (target != null) node = target;
                else return null; // Broken link
            } else {
                node = next;
            }
        }
        return node;
    }

    private void saveSnapshot() {
        try (FileOutputStream fos = new FileOutputStream(SNAPSHOT_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            
            oos.writeObject(root); 
            System.out.println("Snapshot saved successfully to " + SNAPSHOT_FILE);
        } catch (IOException e) {
            System.out.println("Error saving snapshot: " + e.getMessage());
        }
    }


    private void restoreSnapshot() {
        File snapshot = new File(SNAPSHOT_FILE);
        if (!snapshot.exists()) {
            System.out.println("No snapshot file found to restore.");
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(SNAPSHOT_FILE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            
            root = (FileNode) ois.readObject(); 
            current = root; 
            buildIndex(); 
            System.out.println("File system restored successfully from snapshot.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error restoring snapshot: " + e.getMessage());
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
            System.out.println("Error: Source item not found: " + sourceName);
            return;
        }
        if (destNode == null || destNode.getType() != FileType.FOLDER) {
            System.out.println("Error: Destination must be an existing folder: " + destName);
            return;
        }
        
        if (destNode.getChildren().containsKey(sourceName)) {
            System.out.println("Error: Cannot copy. Item with name '" + sourceName + "' already exists in destination.");
            return;
        }
        
        UndoManager.Command cmd = new UndoManager.Command() {
            FileNode copiedNode;
            @Override
            public void execute() {
                copiedNode = sourceNode.deepClone(destNode);
                destNode.getChildren().put(copiedNode.getName(), copiedNode);
                buildIndex();
                System.out.println("Copied '" + sourceName + "' to '" + destName + "'.");
            }
            @Override
            public void undo() {
                destNode.getChildren().remove(sourceName);
                buildIndex();
            }
        };
        undoManager.executeCommand(cmd);
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
             System.out.println("Error: Content must be enclosed in double quotes.");
             return;
        }

        final String finalContent = content;

        FileNode fileNode = resolvePath(fileName);

        if (fileNode == null || fileNode.getType() != FileType.FILE) {
            System.out.println("Error: File not found or is a folder: " + fileName);
            return;
        }

        if (!fileNode.getPermissions().contains("w")) {
            System.out.println("Error: Permission denied: Write access required.");
            return;
        }

        UndoManager.Command cmd = new UndoManager.Command() {
            String oldContent;
            @Override
            public void execute() {
                oldContent = fileNode.getContent();
                fileNode.setContent(finalContent);
                accessCache.recordAccess(fileNode.getFullPath(), "write " + fileName);
                System.out.println("Wrote " + finalContent.length() + " characters to " + fileName);
            }
            @Override
            public void undo() {
                fileNode.setContent(oldContent);
            }
        };
        undoManager.executeCommand(cmd);
    }


    private void readFromFile(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: read <filename>");
            return;
        }
        
        String fileName = parts[1];
        FileNode fileNode = resolvePath(fileName);

        if (fileNode == null || fileNode.getType() != FileType.FILE) {
            System.out.println("Error: File not found or is a folder: " + fileName);
            return;
        }

        if (!fileNode.getPermissions().contains("r")) {
            System.out.println("Error: Permission denied: Read access required.");
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
            System.out.println("Error: Source item not found: " + sourceName);
            return;
        }

     
        FileNode destNode = current.getChildren().get(destName);
        if (destNode == null) {
            System.out.println("Error: Destination folder not found: " + destName);
            return;
        }
        if (destNode.getType() != FileType.FOLDER) {
            System.out.println("Error: Destination must be a folder.");
            return;
        }
        
        if (destNode.getChildren().containsKey(sourceName)) {
            System.out.println("Error: Cannot move. Item with name '" + sourceName + "' already exists in destination.");
            return;
        }
        
        UndoManager.Command cmd = new UndoManager.Command() {
            @Override
            public void execute() {
                current.getChildren().remove(sourceName);
                sourceNode.setParent(destNode);
                destNode.getChildren().put(sourceName, sourceNode);
                System.out.println("Moved '" + sourceName + "' to '" + destName + "'.");
            }
            @Override
            public void undo() {
                destNode.getChildren().remove(sourceName);
                sourceNode.setParent(current);
                current.getChildren().put(sourceName, sourceNode);
            }
        };
        undoManager.executeCommand(cmd);
    }
    
    private void exportIndex() {
        INDEX_CACHE = indexer.getAllIndexedWords();
        System.out.println("File index exported successfully. Total words: " + INDEX_CACHE.size());
    }

    private void importIndex() {
        if (INDEX_CACHE.isEmpty()) {
            System.out.println("Error: Index cache is empty. Nothing to import.");
            return;
        }
        indexer.importWords(INDEX_CACHE);
        System.out.println("File index imported successfully. Total words: " + INDEX_CACHE.size());
    }

    private void removeFileOrFolder(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: rm [-r] <file_or_folder>");
            return;
        }

        boolean recursive = false;
        String name;

        if (parts[1].equals("-r")) {
            if (parts.length < 3) {
                System.out.println("Usage: rm -r <file_or_folder>");
                return;
            }
            recursive = true;
            name = parts[2];
        } else {
            name = parts[1];
        }

        FileNode nodeToRemove = current.getChildren().get(name);

        if (nodeToRemove == null) {
            System.out.println("Item not found: " + name);
            return;
        }

        if (nodeToRemove.getType() == FileType.FOLDER && !recursive) {
            if (!nodeToRemove.getChildren().isEmpty()) {
                System.out.println("Error: Folder is not empty. Use 'rm -r " + name + "' to delete recursively.");
                return;
            }
        }
        
        UndoManager.Command cmd = new UndoManager.Command() {
            @Override
            public void execute() {
                current.getChildren().remove(name);
                nodeToRemove.getInode().decrementRefCount();
                buildIndex();
                System.out.println("Removed: " + name);
            }
            @Override
            public void undo() {
                current.getChildren().put(name, nodeToRemove);
                nodeToRemove.getInode().incrementRefCount();
                buildIndex();
            }
        };
        undoManager.executeCommand(cmd);
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

        System.out.println("Stats for: " + node.getName());
        System.out.println("- Inode ID: " + node.getInode().getId());
        System.out.println("- Type: " + node.getType());
        System.out.println("- Permissions: " + node.getPermissions());
        System.out.println("- Links: " + node.getInode().getRefCount());
        System.out.println("- Path: " + node.getFullPath());
        if (node.getType() == FileType.FILE) {
            System.out.println("- Size: " + node.getSize() + " bytes");
            System.out.println("- Created At: " + node.getInode().getCreated());
            System.out.println("- Modified At: " + node.getInode().getModified());
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
            System.out.println("Found " + results.size() + " matches:");
            results.forEach(System.out::println);
        }
    }
    
    private void list(String[] parts) {
        boolean detailed = parts.length > 1 && parts[1].equals("-l");
        
        if (detailed) {
            System.out.println("Type\tPerms\tLinks\tSize\tName");
            System.out.println("----\t-----\t-----\t----\t----");
        }
        
        current.getChildren().forEach((name, node) -> {
            if (detailed) {
                String type = node.getType() == FileType.FOLDER ? "d" : "-";
                System.out.printf("%s\t%s\t%d\t%d\t%s%n", 
                    type, 
                    node.getPermissions(), 
                    node.getInode().getRefCount(),
                    node.getSize(),
                    name
                );
            } else {
                System.out.println((node.getType() == FileType.FOLDER ? "[DIR] " : "[FILE] ") + 
                                   "[" + node.getPermissions() + "] " + name);
            }
        });
    }

    private void changeDirectory(String[] parts) {
        if (parts.length < 2) return;
        String path = parts[1];
        
        FileNode target = resolvePath(path);
        
        if (target == null) {
            System.out.println("Error: Directory not found: " + path);
            return;
        }
        
        if (target.getType() != FileType.FOLDER) {
            System.out.println("Error: Not a directory: " + path);
            return;
        }
        
        navStack.push(current.getName());
        current = target;
    }

    private void openFile(String[] parts) {
        if (parts.length < 2) return;
        String file = parts[1];
        FileNode target = resolvePath(file);
        
        if (target != null && target.getType() == FileType.FILE) {
            System.out.println("Opened " + file);
            navStack.push("Opened " + file);
        } else {
            System.out.println("File not found or not a file.");
        }
    }

    private void back() {
        String prev = navStack.pop();
        if (prev != null) {
            System.out.println("Back to: " + prev);
        }
    }

    private void changePermissions(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: chmod <permissions> <file_or_folder>");
            return;
        }
        String perms = parts[1];
        String name = parts[2];
        
        if (!perms.matches("[rw]+")) {
             System.out.println("Error: Invalid permissions. Use combinations of 'r' and 'w' (e.g., rw, r, w).");
             return;
        }

        FileNode node = resolvePath(name);
        if (node == null) {
            System.out.println("Item not found: " + name);
            return;
        }
        
        UndoManager.Command cmd = new UndoManager.Command() {
            String oldPerms;
            @Override
            public void execute() {
                oldPerms = node.getPermissions();
                node.setPermissions(perms);
                System.out.println("Permissions for '" + name + "' changed to: " + perms);
            }
            @Override
            public void undo() {
                node.setPermissions(oldPerms);
            }
        };
        undoManager.executeCommand(cmd);
    }

    private void grepFile(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: grep <text> <file>");
            return;
        }
        String text = parts[1];
        String fileName = parts[2];
        
        FileNode fileNode = resolvePath(fileName);
        if (fileNode == null || fileNode.getType() != FileType.FILE) {
            System.out.println("Error: File not found or is a folder: " + fileName);
            return;
        }
        
        if (!fileNode.getPermissions().contains("r")) {
            System.out.println("Error: Permission denied: Read access required.");
            return;
        }

        String content = fileNode.getContent();
        if (content.contains(text)) {
            System.out.println("Found match in " + fileName + ":");
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains(text)) {
                    System.out.println((i + 1) + ": " + lines[i]);
                }
            }
        } else {
            System.out.println("No matches found.");
        }
    }

    private void catFile(String[] parts) {
        readFromFile(parts);
    }
    
    private void createLink(String[] parts) {
        boolean isSymlink = false;
        int srcIndex = 1;
        int dstIndex = 2;

        if (parts.length > 1 && parts[1].equals("-s")) {
            isSymlink = true;
            srcIndex = 2;
            dstIndex = 3;
        }

        if (parts.length <= dstIndex) {
            System.out.println("Usage: ln [-s] <source> <link_name>");
            return;
        }

        String sourcePath = parts[srcIndex];
        String linkName = parts[dstIndex];

        if (current.getChildren().containsKey(linkName)) {
            System.out.println("Error: Destination already exists: " + linkName);
            return;
        }

        if (isSymlink) {
            UndoManager.Command cmd = new UndoManager.Command() {
                @Override
                public void execute() {
                    Inode inode = new Inode(FileType.SYMLINK);
                    inode.setTargetPath(sourcePath);
                    FileNode linkNode = new FileNode(linkName, current, inode);
                    current.getChildren().put(linkName, linkNode);
                    indexer.insert(linkName);
                    System.out.println("Symbolic link created: " + linkName + " -> " + sourcePath);
                }
                @Override
                public void undo() {
                    current.getChildren().remove(linkName);
                    buildIndex();
                }
            };
            undoManager.executeCommand(cmd);
        } else {
            FileNode sourceNode = resolvePath(sourcePath);
            if (sourceNode == null) {
                System.out.println("Error: Source file not found: " + sourcePath);
                return;
            }
            
            if (sourceNode.getType() == FileType.FOLDER) {
                System.out.println("Error: Hard links to directories are not allowed.");
                return;
            }
            
            UndoManager.Command cmd = new UndoManager.Command() {
                @Override
                public void execute() {
                    FileNode linkNode = new FileNode(linkName, current, sourceNode.getInode());
                    sourceNode.getInode().incrementRefCount();
                    current.getChildren().put(linkName, linkNode);
                    indexer.insert(linkName);
                    System.out.println("Hard link created: " + linkName + " -> Inode " + sourceNode.getInode().getId());
                }
                @Override
                public void undo() {
                    current.getChildren().remove(linkName);
                    sourceNode.getInode().decrementRefCount();
                    buildIndex();
                }
            };
            undoManager.executeCommand(cmd);
        }
    }
}