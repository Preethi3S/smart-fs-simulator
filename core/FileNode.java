package core;

import java.io.Serializable;
import java.util.Map;

import model.FileMeta;
import model.FileType;
import model.Inode;

public class FileNode implements Serializable {
    private String name;
    private FileNode parent;
    private Inode inode;

    public FileNode(String name, FileNode parent, Inode inode) {
        this.name = name;
        this.parent = parent;
        this.inode = inode;
    }

    public String getName() { return name; }
    public FileNode getParent() { return parent; }
    public void setParent(FileNode newParent) { this.parent = newParent; }
    
    public Inode getInode() { return inode; }

    // Delegated methods for convenience
    public FileType getType() { return inode.getType(); }
    public Map<String, FileNode> getChildren() { return inode.getChildren(); }
    public String getContent() { return inode.getContent(); }
    public void setContent(String content) { inode.setContent(content); }
    public String getPermissions() { return inode.getPermissions(); }
    public void setPermissions(String p) { inode.setPermissions(p); }
    public int getSize() { return inode.getSize(); }
    public String getTargetPath() { return inode.getTargetPath(); }
    public void setTargetPath(String path) { inode.setTargetPath(path); }
    public FileMeta getMeta() { return null; } // Deprecated, kept for compatibility if needed, but returning null
    
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
        // Create new Inode (copy)
        Inode newInode = new Inode(this.inode.getType());
        newInode.setPermissions(this.inode.getPermissions());
        newInode.setContent(this.inode.getContent());
        newInode.setSize(this.inode.getSize());
        
        FileNode copy = new FileNode(this.name, newParent, newInode);
        
        if (this.inode.getType() == FileType.FOLDER) {
            for (FileNode child : this.inode.getChildren().values()) {
                FileNode childCopy = child.deepClone(copy); 
                newInode.getChildren().put(childCopy.getName(), childCopy);
            }
        }
        
        return copy;
    }
}