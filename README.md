# Smart File System Simulator

A robust, in-memory file system simulator written in Java. This project mimics the core functionality of a Unix-like file system, providing a rich command-line interface (CLI) for file management, navigation, and diagnostics.

It is designed to demonstrate advanced data structures and system design concepts, including **Inode-based architecture**, **Command Pattern for Undo/Redo**, and **Graph traversal algorithms**.

## Key Features

### Core File Operations
- **File & Directory Management**: `mkdir`, `touch`, `rm` (recursive), `mv`, `cp`.
- **Navigation**: `cd`, `pwd`, `ls` (with detailed view), `tree` (visual structure).
- **Content Manipulation**: `write`, `read`, `cat`, `grep` (text search).

### Advanced Linking
- **Hard Links**: Create multiple directory entries pointing to the same Inode (`ln`).
- **Symbolic Links**: Create soft links to other paths with cycle detection (`ln -s`).
- **Path Resolution**: Robust engine handling absolute/relative paths, `..`, `.`, and symlink traversal.

### System Integrity & Diagnostics
- **Inode Architecture**: Separation of file metadata (Inode) from directory entries (FileNode).
- **Consistency Checker (`fsck`)**: Detects orphaned nodes, broken symlinks, and reference count mismatches.
- **Disk Usage (`du`)**: Calculates directory sizes while handling cycles gracefully.
- **Permissions**: Basic `r` (read) and `w` (write) permission system (`chmod`).

### History & Persistence
- **Undo/Redo**: Full support for undoing and redoing file operations.
- **Snapshots**: Save and restore the entire file system state to disk (`snapshot`, `restore`).
- **Access History**: Tracks navigation history and recently accessed files (`history`, `mru`).

## Getting Started

### Prerequisites
- Java SE 22 or higher.

### Running the Simulator
1. **Compile the project**:
   ```bash
   javac Main.java core/*.java history/*.java model/*.java
   ```
2. **Run the application**:
   ```bash
   java Main
   ```

## Command Reference

| Command | Description |
|---------|-------------|
| `ls [-l]` | List files in current directory (use `-l` for details). |
| `cd <path>` | Change directory. Supports `..` and symlinks. |
| `mkdir <name>` | Create a new directory. |
| `touch <name>` | Create a new empty file. |
| `write <file> "<text>"` | Write text content to a file. |
| `cat <file>` | Display file content. |
| `ln [-s] <src> <dst>` | Create a hard link or symbolic link (`-s`). |
| `rm [-r] <name>` | Remove a file or directory (recursively with `-r`). |
| `cp <src> <dst>` | Copy a file or directory. |
| `mv <src> <dst>` | Move or rename a file or directory. |
| `find <name>` | Search for files or directories by name. |
| `grep <text> <file>` | Search for text within a file. |
| `chmod <perm> <file>` | Change permissions (e.g., `chmod rw file.txt`). |
| `fsck` | Run file system consistency check. |
| `undo` / `redo` | Undo or redo the last operation. |
| `tree` | Display the directory structure as a tree. |
| `stat <name>` | Show detailed inode statistics. |
| `snapshot` / `restore` | Save or load the file system state. |
| `exit` | Quit the simulator. |

## Architecture

- **`core/FileSystem.java`**: The main controller handling commands and logic.
- **`core/FileNode.java`**: Represents a directory entry (name + pointer to Inode).
- **`model/Inode.java`**: Stores actual file data, metadata, and reference counts.
- **`history/UndoManager.java`**: Implements the Command Pattern for operation history. 
