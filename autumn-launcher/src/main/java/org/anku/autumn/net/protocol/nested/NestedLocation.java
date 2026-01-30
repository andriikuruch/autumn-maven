package org.anku.autumn.net.protocol.nested;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NestedLocation {
    private static final Map<String, NestedLocation> locationCache = new ConcurrentHashMap<>();
    private static final Map<String, Path> pathCache = new ConcurrentHashMap<>();

    private final Path path;
    private final String nestedEntryName;

    public NestedLocation(Path path, String nestedEntryName) {
        this.path = path;
        this.nestedEntryName = nestedEntryName;
    }

    public static NestedLocation fromUrl(URL url) {
        if (url == null || !"nested".equalsIgnoreCase(url.getProtocol())) {
            throw new IllegalStateException("url must not be null and must use nested protocol");
        }
        return parse(url.toString().substring(7));
    }

    public static NestedLocation parse(String location) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("location must not be null");
        }


        int index = location.indexOf("/!");
        String path = index != -1 ? location.substring(0, index) : location;
        String nestedEntryName = index != -1 ? location.substring(index + 2) : null;
        Path locationPath = pathCache.computeIfAbsent(path, key -> Path.of(fixPath(path)));

        return locationCache.computeIfAbsent(location, key -> new NestedLocation(locationPath, nestedEntryName));
    }

    private static String fixPath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    public Path getPath() {
        return path;
    }

    public String getNestedEntryName() {
        return nestedEntryName;
    }

    @Override
    public String toString() {
        return "NestedLocation{" +
                "path=" + path +
                ", nestedEntryName='" + nestedEntryName + '\'' +
                '}';
    }
}
