package org.anku.autumn.net.protocol.jar;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;

public class JarUrl {

    public static URL create(File file, String nested) {
        return create(file, nested, null);
    }

    public static URL create(File file, JarEntry nested) {
        return create(file, nested, null);
    }

    public static URL create(File file, JarEntry nested, String path) {
        return create(file, nested != null ? nested.getName() : null, path);
    }

    @SuppressWarnings("deprecation")
    public static URL create(File file, String nested, String path) {
        try {
            path = path != null ? path : "";
            String jarFilePath = file.toURI().getRawPath().replace("!", "%21");
            String jarReference = nested != null ? "nested:" + jarFilePath + "/!" + nested : "file:" + jarFilePath;
            return new URL(null, "jar:" + jarReference + "!/" + path, Handler.INSTANCE);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to create Jar url", e);
        }
    }
}
