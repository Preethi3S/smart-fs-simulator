package model;

import java.time.LocalDateTime;

public class FileMeta {
    private int size;
    private LocalDateTime createdAt;

    public FileMeta(int size) {
        this.size = size;
        this.createdAt = LocalDateTime.now();
    }

    public int getSize() { return size; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}