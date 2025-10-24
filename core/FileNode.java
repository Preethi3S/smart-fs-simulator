package core;

import model.*;

import java.io.Serializable;
import java.util.*;

public class FileNode implements Serializable{
    private String name;
    private FileType type;
    private FileMeta meta;
    private FileNode parent;
    private Map<String, FileNode> children = new HashMap<>();
    private String content; 

    public FileNode(String name, FileType type, FileNode parent) {
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.content = ""; 
    }

    public String getName() { return name; }
    public FileType getType() { return type; }
    public FileNode getParent() { return parent; }
    public Map<String, FileNode> getChildren() { return children; }
    public FileMeta getMeta() { return meta; }
    public void setMeta(FileMeta meta) { this.meta = meta; }
    public void setParent(FileNode newParent) { this.parent = newParent; }
    
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content; 
        
        if (this.meta != null) {
            this.meta.setSize(this.content.length() + this.name.length() * 10); 
        }
    }
    
    public String getFullPath() {
        if (parent == null || name.equals("root")) {
            return name.equals("root") ? "/" : name; 
        }
        
        String parentPath = parent.getFullPath();
        if (parentPath.equals("/")) {
            return parentPath + name;
        }
        return parentPath + "/" + name;
    }

    public FileNode deepClone(FileNode newParent) {
        FileNode copy = new FileNode(this.name, this.type, newParent);
        
        if (this.meta != null) {
            FileMeta metaCopy = new FileMeta(this.meta.getSize()); 
            copy.setMeta(metaCopy); 
        }
        
        if (this.type == FileType.FILE && this.content != null) {
            copy.setContent(this.content); 
        }
        
        if (this.type == FileType.FOLDER) {
            for (FileNode child : this.children.values()) {
                FileNode childCopy = child.deepClone(copy); 
                copy.getChildren().put(childCopy.getName(), childCopy);
            }
        }
        
        return copy;
    }
}