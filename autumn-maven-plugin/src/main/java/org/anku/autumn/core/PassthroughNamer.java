package org.anku.autumn.core;

import org.anku.autumn.utils.ZipUtils;

public class PassthroughNamer {

    private final String path;

    public PassthroughNamer(String path) {
        this.path = ZipUtils.normalizeDir(path);
    }

    public String entryNameFroJar(String entryName) {
        return path + ZipUtils.normalizeDir(entryName);
    }
}
