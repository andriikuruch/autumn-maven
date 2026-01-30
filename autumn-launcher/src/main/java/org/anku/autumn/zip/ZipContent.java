package org.anku.autumn.zip;

import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ZipContent implements Closeable {
    private static final Map<Source, ZipContent> cache = new ConcurrentHashMap<>();

    private Source source;

    @Override
    public void close() throws IOException {

    }

    private static final class Source {
        final Path path;
        final String entryName;

        private Source(Path path, String entryName) {
            this.path = path;
            this.entryName = entryName;
        }

        boolean isNested() {
            return entryName != null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Source source = (Source) o;
            return Objects.equals(path, source.path) && Objects.equals(entryName, source.entryName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, entryName);
        }

        @Override
        public String toString() {
            return "Source{" +
                    "path=" + path +
                    ", entryName='" + entryName + '\'' +
                    '}';
        }
    }

    public static ZipContent open(Path path) throws IOException {
        return open(path, null);
    }

    public static ZipContent open(Path path, String nestedEntryName) throws IOException {
        Source source = new Source(path.toAbsolutePath(), nestedEntryName);
        ZipContent zipContent = cache.get(source);
        if (zipContent != null) {
            return zipContent;
        }

        if (!source.isNested()) {
            return new ZipContent();
        }

        try (ZipContent zip = open(source.path)) {
            Entry entry = zip.getEntry(source.entryName);
            if (entry == null) {
                throw new IOException("Nested entry '%s' is not found in zip '%s'"
                        .formatted(source.entryName, source.path));
            }
            return new ZipContent();
        }
    }

    public Entry getEntry(String entryName) {
        return null;
    }

    public static class Entry {

    }


}
