package model;

public class SearchResult {
    private String name;
    private String path;

    public SearchResult(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() { return name; }
    public String getPath() { return path; }

    @Override
    public String toString() {
        return name + " => " + path;
    }
}
