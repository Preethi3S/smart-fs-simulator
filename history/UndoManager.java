package history;

import java.util.Stack;

public class UndoManager {
    private Stack<Runnable> undoStack = new Stack<>();
    private Stack<Runnable> redoStack = new Stack<>();

    public void addOperation(Runnable undoOp) {
        undoStack.push(undoOp);
        redoStack.clear(); // New operation clears redo history
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("❌ Nothing to undo.");
            return;
        }
        Runnable op = undoStack.pop();
        op.run();
        // In a real command pattern, the undo op would push to redo stack.
        // But here we are just running the inverse. 
        // To support redo, the inverse operation itself needs to push to redo stack.
        // This simple Runnable approach is tricky for Redo.
        // Let's just support Undo for now as requested, or use a better pattern.
        System.out.println("✅ Undo successful.");
    }
    
    // For full Undo/Redo, we need Command objects that know how to do and undo.
    public interface Command {
        void execute();
        void undo();
    }
    
    private Stack<Command> commandStack = new Stack<>();
    private Stack<Command> redoCommandStack = new Stack<>();
    
    public void executeCommand(Command cmd) {
        cmd.execute();
        commandStack.push(cmd);
        redoCommandStack.clear();
    }
    
    public void undoCommand() {
        if (commandStack.isEmpty()) {
            System.out.println("❌ Nothing to undo.");
            return;
        }
        Command cmd = commandStack.pop();
        cmd.undo();
        redoCommandStack.push(cmd);
        System.out.println("✅ Undo successful.");
    }
    
    public void redoCommand() {
        if (redoCommandStack.isEmpty()) {
            System.out.println("❌ Nothing to redo.");
            return;
        }
        Command cmd = redoCommandStack.pop();
        cmd.execute();
        commandStack.push(cmd);
        System.out.println("✅ Redo successful.");
    }
}
