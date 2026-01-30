package org.anku.autumn.core;

import org.anku.autumn.utils.ZipUtils;

import java.nio.file.Path;

public class FlatNamer implements EntryNamer {

    private final String path;

    public FlatNamer(String path) {
        this.path = ZipUtils.normalizeDir(path);
    }


    @Override
    public String entryName(Path file) {
        return path + file.getFileName();
    }
}
