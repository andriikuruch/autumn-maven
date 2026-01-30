package org.anku.autumn.core;

import org.anku.autumn.utils.ZipUtils;

import java.nio.file.Path;

public class HierarchyNamer implements EntryNamer {
    private final Path baseDir;
    private final String path;

    public HierarchyNamer(Path baseDir, String path) {
        this.baseDir = baseDir.toAbsolutePath().normalize();
        this.path = ZipUtils.normalizeDir(path);
        if (!baseDir.toFile().isDirectory()) {
            throw new IllegalArgumentException(baseDir + " is not directory.");
        }
    }

    @Override
    public String entryName(Path file) {
        Path f = file.toAbsolutePath().normalize();
        if (!f.startsWith(baseDir)) {
            throw new IllegalArgumentException(file + " is outside of " + baseDir);
        }
        String rel = ZipUtils.normalizeDir(baseDir.relativize(f).toString());
        return path + rel;
    }
}
