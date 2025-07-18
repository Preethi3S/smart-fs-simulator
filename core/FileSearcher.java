package core;

import model.FileType;
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
}