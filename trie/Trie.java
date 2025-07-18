package trie;

import java.util.ArrayList;
import java.util.List;

public class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
        }
        current.isEndOfWord = true;
    }

    public boolean search(String word) {
        TrieNode node = getNode(word);
        return node != null && node.isEndOfWord;
    }

    public List<String> startsWith(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode node = getNode(prefix);
        if (node != null) dfs(prefix, node, results);
        return results;
    }

    private TrieNode getNode(String word) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            if (!current.children.containsKey(ch)) return null;
            current = current.children.get(ch);
        }
        return current;
    }

    private void dfs(String prefix, TrieNode node, List<String> results) {
        if (node.isEndOfWord) results.add(prefix);
        for (char ch : node.children.keySet()) {
            dfs(prefix + ch, node.children.get(ch), results);
        }
    }
}
