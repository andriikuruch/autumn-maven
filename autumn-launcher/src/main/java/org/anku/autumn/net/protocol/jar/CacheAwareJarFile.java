package org.anku.autumn.net.protocol.jar;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public class CacheAwareJarFile extends JarFile {
    private final Consumer<JarFile> onCloseCallback;

    public CacheAwareJarFile(File file, Consumer<JarFile> onCloseCallback) throws IOException {
        super(file);
        this.onCloseCallback = onCloseCallback;
    }

    @Override
    public void close() throws IOException {
        if (onCloseCallback != null) {
            onCloseCallback.accept(this);
        }
        super.close();
    }
}
