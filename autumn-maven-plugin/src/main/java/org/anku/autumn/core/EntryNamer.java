package org.anku.autumn.core;

import java.nio.file.Path;

@FunctionalInterface
public interface EntryNamer {

    String entryName(Path file);
}
