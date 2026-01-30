package org.anku.autumn.net.protocol.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

public class NestedJarFile extends JarFile {

    private final String nestedEntryName;

    public NestedJarFile(File file, String nestedEntryName) throws IOException {
        super(file);
        this.nestedEntryName = nestedEntryName;
    }

    public InputStream getRawZipDataInputStream() {
        return null;
    }
}
