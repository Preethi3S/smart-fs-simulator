package core;

import model.FileType;
import java.util.*;

public class DuplicateChecker {
    public static Map<Integer, List<String>> findDuplicates(core.FileNode root) {
        Map<Integer, List<String>> map = new HashMap<>();
        dfs(root, map);
        return map;
    }

    private static void dfs(core.FileNode node, Map<Integer, List<String>> map) {
        if (node.getType() == FileType.FILE && node.getMeta() != null) {
            int hash = node.getMeta().getSize();
            map.putIfAbsent(hash, new ArrayList<>());
            map.get(hash).add(node.getName());
        }
        for (core.FileNode child : node.getChildren().values()) {
            dfs(child, map);
        }
    }
}
