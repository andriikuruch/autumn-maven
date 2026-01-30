package org.anku.autumn.net.protocol.jar;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public class UrlNestedJarFile extends NestedJarFile {
    private final Consumer<JarFile> closeAction;

    public UrlNestedJarFile(
            File file,
            String nestedEntryName,
            Consumer<JarFile> onClose
    ) throws IOException {
        super(file, nestedEntryName);
        this.closeAction = onClose;
    }
}
