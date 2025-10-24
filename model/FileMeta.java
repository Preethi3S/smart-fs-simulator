package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FileMeta implements Serializable{
    private int size;
    private LocalDateTime createdAt;

    public FileMeta(int size) {
        this.size = size;
        this.createdAt = LocalDateTime.now();
    }

    public int getSize() { return size; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setSize(int size) { this.size = size; } 
}