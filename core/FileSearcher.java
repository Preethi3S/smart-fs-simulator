package core;

import model.FileType;
import model.SearchResult;
import java.util.*;

public class FileSearcher {
    public static List<String> searchByType(core.FileNode current, FileType type) {
        List<String> results = new ArrayList<>();
        dfs(current, type, results);
        return results;
    }

    private static void dfs(core.FileNode node, FileType type, List<String> res) {
        if (node.getType() == type) res.add(node.getName());
        for (core.FileNode child : node.getChildren().values()) {
            dfs(child, type, res);
        }
    }
    
    public static List<SearchResult> searchByName(FileNode root, String name) {
        List<SearchResult> results = new ArrayList<>();
        findRecursive(root, name.toLowerCase(), results);
        return results;
    }

    private static void findRecursive(FileNode node, String nameToFind, List<SearchResult> results) {
        if (node.getName().toLowerCase().contains(nameToFind) && !node.getName().equals("root")) {
            results.add(new SearchResult(node.getName(), node.getFullPath()));
        }

        for (FileNode child : node.getChildren().values()) {
            findRecursive(child, nameToFind, results);
        }
    }
}