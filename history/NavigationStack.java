package history;

import java.util.Stack;

public class NavigationStack {
    private Stack<String> stack = new Stack<>();

    public void push(String folder) {
        stack.push(folder);
    }

    public String pop() {
        return stack.isEmpty() ? null : stack.pop();
    }

    public void printHistory() {
        if (stack.isEmpty()) {
            System.out.println("📂 No navigation history.");
        } else {
            System.out.println("📂 Navigation Stack:");
            for (String s : stack) {
                System.out.println("- " + s);
            }
        }
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
