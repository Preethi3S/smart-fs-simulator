package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import core.FileNode;

public class Inode implements Serializable {
    private static int idCounter = 1;
    private int id;
    private FileType type;
    private String permissions = "rw";
    private String content = "";
    private Map<String, FileNode> children = new HashMap<>();
    private int refCount = 0;
    private LocalDateTime created;
    private LocalDateTime modified;
    private int size;
    private String targetPath; // For symlinks

    public Inode(FileType type) {
        this.id = idCounter++;
        this.type = type;
        this.created = LocalDateTime.now();
        this.modified = LocalDateTime.now();
        this.refCount = 1; // Starts with 1 reference
    }

    public int getId() { return id; }
    public FileType getType() { return type; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content; 
        this.size = content.length();
        this.modified = LocalDateTime.now();
    }
    
    public String getTargetPath() { return targetPath; }
    public void setTargetPath(String targetPath) { this.targetPath = targetPath; }

    public Map<String, FileNode> getChildren() { return children; }
    
    public int getRefCount() { return refCount; }
    public void incrementRefCount() { this.refCount++; }
    public void decrementRefCount() { this.refCount--; }

    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getModified() { return modified; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public FileNode deepClone(FileNode newParent) {
        // For deep clone (cp), we create a NEW Inode with same content
        Inode newInode = new Inode(this.type);
        newInode.setPermissions(this.permissions);
        newInode.setContent(this.content);
        newInode.setSize(this.size);
        
        // Children are handled by the caller or recursively here?
        // Since FileNode structure is recursive, the FileNode.deepClone handles the recursion.
        // But FileNode needs to create new Inodes for children.
        return null; // Helper for logic separation
    }
}
