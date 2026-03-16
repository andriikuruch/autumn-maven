package org.anku.autumn.net.protocol.jar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileFactory {
    private final URL fatJarFileUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
    private JarFile parentJarFile;

    public JarFile createJarFile(URL url, Consumer<JarFile> onCloseCallback) throws IOException {
        if (url.getProtocol().equals("file")) {
            try {
                CacheAwareJarFile cacheAwareJarFile = new CacheAwareJarFile(Path.of(url.toURI()).toFile(), onCloseCallback);
                return cacheAwareJarFile;
            } catch (URISyntaxException e) {
                throw new IOException(e.getCause());
            }
        }
        if (parentJarFile == null) {
            try {
                parentJarFile = new CacheAwareJarFile(Path.of(fatJarFileUrl.toURI()).toFile(), onCloseCallback);
            } catch (URISyntaxException e) {
                throw new IOException(e.getCause());
            }
        }

        JarEntry nestedJarEntry = parentJarFile
                .stream()
                .filter(entry -> url.getPath().endsWith(entry.getName())) // url.getPath = /path/myjar.jar/!BOOT-INF/lib/mylib.jar
                .findFirst()
                .orElseThrow();
        if (nestedJarEntry.getName().equals("BOOT-INF/classes/")) {
            return parentJarFile;
        }
        try (InputStream inputStream = parentJarFile.getInputStream(nestedJarEntry)) {
            Path path = Files.createTempFile("jar-unpacked", null);
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            CacheAwareJarFile cacheAwareJarFile = new CacheAwareJarFile(path.toFile(), onCloseCallback);
            path.toFile().deleteOnExit();
            return cacheAwareJarFile;
        }
    }
}
