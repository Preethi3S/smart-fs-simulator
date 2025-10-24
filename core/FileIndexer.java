package core;

import java.util.*;

public class FileIndexer {
    private TrieNode root;

    public FileIndexer() { 
        this.root = new TrieNode();
    }
    
    public void clear() { 
        this.root = new TrieNode();
    }

    public void insert(String name) {
        TrieNode node = root;
        for (char ch : name.toCharArray()) {
            node.children.putIfAbsent(ch, new TrieNode());
            node = node.children.get(ch);
        }
        node.isEnd = true;
    }

    public List<String> getAllIndexedWords() {
        List<String> words = new ArrayList<>();
        collectWords("", root, words);
        return words;
    }

    private void collectWords(String prefix, TrieNode node, List<String> res) {
        if (node.isEnd) res.add(prefix);
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            collectWords(prefix + entry.getKey(), entry.getValue(), res);
        }
    }
    
    public void importWords(List<String> words) {
        clear();
        for (String word : words) {
            insert(word);
        }
    }

    public List<String> search(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode node = root;
        for (char ch : prefix.toCharArray()) {
            if (!node.children.containsKey(ch)) return results;
            node = node.children.get(ch);
        }
        dfs(prefix, node, results);
        return results;
    }

    private void dfs(String prefix, TrieNode node, List<String> res) {
        if (node.isEnd) res.add(prefix);
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            dfs(prefix + entry.getKey(), entry.getValue(), res);
        }
    }

    private static class TrieNode {
        boolean isEnd;
        Map<Character, TrieNode> children = new HashMap<>();
    }
}