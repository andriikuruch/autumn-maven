package org.anku.autumn.archive;

import org.anku.autumn.net.protocol.jar.JarUrl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarArchive implements Archive {

    private final File file;
    private final JarFile jarFile;

    public JarArchive(File file) throws IOException {
        this.file = file;
        this.jarFile = new JarFile(file);
    }
    
    @Override
    public Manifest getManifest() throws IOException {
        return jarFile.getManifest();
    }

    @Override
    public Set<URL> getClassPathUrls() {
        Set<URL> urls = new LinkedHashSet<>();
        urls.add(JarUrl.create(file, "BOOT-INF/classes/"));
        jarFile
                .stream()
                .filter(this::isLibrary)
                .map(jarEntry -> JarUrl.create(file, jarEntry))
                .forEach(urls::add);
        return urls;
    }

    private boolean isLibrary(JarEntry jarEntry) {
        return jarEntry.getName().startsWith("BOOT-INF/libs");
    }

    @Override
    public void close() throws Exception {
        jarFile.close();
    }
}
