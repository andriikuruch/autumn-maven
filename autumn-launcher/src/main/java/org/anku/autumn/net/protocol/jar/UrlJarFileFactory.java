package org.anku.autumn.net.protocol.jar;

import org.anku.autumn.net.protocol.nested.NestedLocation;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.jar.JarFile;

class UrlJarFileFactory {

    public static boolean isNestedUrl(URL jarFileURL) {
        return false;
    }

    JarFile createJarFile(URL jarFileUrl, Consumer<JarFile> onClose)
            throws IOException {
        if ("nested".equalsIgnoreCase(jarFileUrl.getProtocol())) {
            return createNestedJar(jarFileUrl, onClose);
        }
        return null;
    }

    private JarFile createNestedJar(URL url, Consumer<JarFile> onClose)
            throws IOException {
        NestedLocation location = NestedLocation.fromUrl(url);
        return new UrlNestedJarFile(
                location.getPath().toFile(),
                location.getNestedEntryName(),
                onClose
        );
    }
}
