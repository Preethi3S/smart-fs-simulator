// history/AccessHistory.java
package history;

import java.util.LinkedList;

public class AccessHistory {
    private LinkedList<String> accessList;

    public AccessHistory() {
        this.accessList = new LinkedList<>();
    }

    public void visit(String location) {
        accessList.addLast(location);
    }

    public void printAccessHistory() {
        if (accessList.isEmpty()) {
            System.out.println("📘 No access history.");
        } else {
            System.out.println("📘 Access History:");
            for (String entry : accessList) {
                System.out.println("- " + entry);
            }
        }
    }

    public void clearHistory() {
        accessList.clear();
    }

    public int size() {
        return accessList.size();
    }
}
