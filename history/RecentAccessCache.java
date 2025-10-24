
package history; 

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedList; 

public class RecentAccessCache {
    private static final int CACHE_SIZE = 5;

    private LinkedHashMap<String, String> cache;

    public RecentAccessCache() {
        this.cache = new LinkedHashMap<String, String>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > CACHE_SIZE;
            }
        };
    }

    public void recordAccess(String fullPath, String command) {
        cache.put(fullPath, command); 
    }

    public void printMRU() {
        if (cache.isEmpty()) {
            System.out.println("⭐ No recently accessed items.");
            return;
        }
        
        LinkedList<String> keys = new LinkedList<>(cache.keySet());
        
        System.out.println("⭐ Most Recently Used (Top " + CACHE_SIZE + "):");
        
        for (int i = keys.size() - 1; i >= 0; i--) {
            String path = keys.get(i);
            String command = cache.get(path);
            System.out.println("- [" + command.split("\\s+")[0].toUpperCase() + "] " + path);
        }
    }
    
    public void clear() {
        cache.clear();
    }
}