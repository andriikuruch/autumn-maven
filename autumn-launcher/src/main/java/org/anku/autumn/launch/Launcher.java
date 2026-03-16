package org.anku.autumn.launch;

import org.anku.autumn.archive.Archive;
import org.anku.autumn.net.protocol.Handlers;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Manifest;

public abstract class Launcher {
    private static final String START_CLASS = "Start-Class";
    private final Archive archive;

    Launcher() throws Exception {
        archive = Archive.create(Launcher.class);
    }

    public void launch(String[] args) throws Exception {
        Handlers.register();
        String startClass = getStartClass();
        Set<URL> urls = archive.getClassPathUrls();
        ClassLoader autumnClassLoader = createClassLoader(urls);
        Thread.currentThread().setContextClassLoader(autumnClassLoader);
        Class<?> mainClass = Class.forName(startClass, true, autumnClassLoader);
        Method main = getMainMethod(mainClass);
        main.setAccessible(true);
        if (main.getParameterCount() == 0) {
            main.invoke(null);
        } else {
            main.invoke(null, new Object[]{args});
        }
    }

    private ClassLoader createClassLoader(Collection<URL> urls) {
        ClassLoader parentClassloader = getClass().getClassLoader();
        return new URLClassLoader(urls.toArray(new URL[0]), parentClassloader);
    }

    private static Method getMainMethod(Class<?> mainClass) throws Exception {
        try {
            return mainClass.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            return mainClass.getDeclaredMethod("main");
        }
    }

    private String getStartClass() throws Exception {
        return Optional.ofNullable(archive.getManifest())
                .map(Manifest::getMainAttributes)
                .map(mainAttrs -> mainAttrs.getValue(START_CLASS))
                .orElseThrow(() -> new IllegalStateException("Start-Class not defined"));
    }
}
