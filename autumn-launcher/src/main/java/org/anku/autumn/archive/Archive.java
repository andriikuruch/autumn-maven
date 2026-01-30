package org.anku.autumn.archive;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Set;
import java.util.jar.Manifest;

public interface Archive extends AutoCloseable {

    Manifest getManifest() throws IOException;

    Set<URL> getClassPathUrls();

    static Archive create(Class<?> target) throws URISyntaxException, IOException {
        CodeSource codeSource = target.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            throw new IllegalArgumentException("Can not determine source code");
        }
        URI location = codeSource.getLocation().toURI();
        File targetFile = Path.of(location).toFile();
        if (targetFile.isDirectory()) {
            throw new IllegalArgumentException("Exploded directory unsupported");
        }
        return new JarArchive(targetFile);
    }

    @Override
    default void close() throws Exception { }
}
