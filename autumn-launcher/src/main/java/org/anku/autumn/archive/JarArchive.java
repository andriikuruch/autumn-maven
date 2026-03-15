package org.anku.autumn.archive;

import org.anku.autumn.net.protocol.jar.JarUrl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class JarArchive implements Archive, AutoCloseable {

    private final File file;
    public final JarFile jarFile;

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
        Set<URL> urls = jarFile
                .stream()
                .filter(jarEntry -> jarEntry.getName().startsWith("BOOT-INF/")
                        && jarEntry.getName().endsWith(".jar"))
                .map(jarEntry -> JarUrl.create(file, jarEntry))
                .collect(Collectors.toSet());
        return urls;
    }

    @Override
    public void close() throws Exception {
        jarFile.close();
    }
}
