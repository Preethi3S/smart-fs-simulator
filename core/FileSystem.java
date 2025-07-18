package core;

import model.*;
import utils.FileUtils;
import history.NavigationStack;
import java.util.*;

public class FileSystem {
    private FileNode root;
    private FileNode current;
    private NavigationStack navStack;

    public FileSystem() {
        root = new FileNode("root", FileType.FOLDER, null);
        current = root;
        navStack = new NavigationStack();
    }

    public void handleCommand(String command) {
        String[] parts = command.trim().split("\\s+");
        String cmd = parts[0];

        switch (cmd) {
            case "ls" -> list();
            case "cd" -> changeDirectory(parts);
            case "mkdir" -> makeDirectory(parts);
            case "touch" -> createFile(parts);
            case "open" -> openFile(parts);
            case "history" -> navStack.printHistory();
            case "back" -> back();
            default -> System.out.println("Invalid command");
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

    private void makeDirectory(String[] parts) {
        if (parts.length < 2) return;
        String folder = parts[1];
        current.getChildren().put(folder, new FileNode(folder, FileType.FOLDER, current));
    }

    private void createFile(String[] parts) {
        if (parts.length < 2) return;
        String file = parts[1];
        FileNode f = new FileNode(file, FileType.FILE, current);
        f.setMeta(new FileMeta(file.length() * 10));
        current.getChildren().put(file, f);
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