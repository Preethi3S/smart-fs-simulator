package core;

import model.*;
import java.util.*;

public class FileNode {
    private String name;
    private FileType type;
    private FileMeta meta;
    private FileNode parent;
    private Map<String, FileNode> children = new HashMap<>();

    public FileNode(String name, FileType type, FileNode parent) {
        this.name = name;
        this.type = type;
        this.parent = parent;
    }

    public String getName() { return name; }
    public FileType getType() { return type; }
    public FileNode getParent() { return parent; }
    public Map<String, FileNode> getChildren() { return children; }
    public FileMeta getMeta() { return meta; }
    public void setMeta(FileMeta meta) { this.meta = meta; }
}