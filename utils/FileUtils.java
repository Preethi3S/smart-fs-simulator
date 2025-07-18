package utils;

import model.FileMeta;
import java.time.format.DateTimeFormatter;

public class FileUtils {
    public static String formatFileMeta(FileMeta meta) {
        if (meta == null) return "(No metadata)";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Size: " + meta.getSize() + "KB | Created: " + meta.getCreatedAt().format(formatter);
    }
}