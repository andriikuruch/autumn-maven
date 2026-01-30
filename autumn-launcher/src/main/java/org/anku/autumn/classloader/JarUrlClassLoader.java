package org.anku.autumn.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

public class JarUrlClassLoader extends URLClassLoader {

    private final Map<URL, JarFile> jarFiles = new ConcurrentHashMap<>();


    static {
        ClassLoader.registerAsParallelCapable();
    }

    private JarUrlClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

//    @Override
//    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        return null;
//    }
//
//    @Override
//    public URL getResource(String name) {
//        return null;
//    }
}
