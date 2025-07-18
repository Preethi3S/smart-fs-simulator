package core;

import model.FileMeta;
import java.util.*;

public class Sorter {
    public static List<core.FileNode> sortByName(Collection<core.FileNode> files) {
        List<core.FileNode> list = new ArrayList<>(files);
        list.sort(Comparator.comparing(core.FileNode::getName));
        return list;
    }

    public static List<core.FileNode> sortBySize(Collection<core.FileNode> files) {
        List<core.FileNode> list = new ArrayList<>(files);
        list.sort(Comparator.comparingInt(a -> a.getMeta() != null ? a.getMeta().getSize() : 0));
        return list;
    }
}