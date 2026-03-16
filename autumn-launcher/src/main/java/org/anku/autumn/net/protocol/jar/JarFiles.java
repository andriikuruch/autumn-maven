package org.anku.autumn.net.protocol.jar;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

public final class JarFiles {
    public static final JarFiles instance = new JarFiles();
    private final JarFileFactory factory;
    private final Cache cache = new Cache();

    public JarFiles() {
        this(new JarFileFactory());
    }

    public JarFiles(JarFileFactory factory) {
        this.factory = factory;
    }

    public JarFile getFromCache(URL url) {
        return cache.get(url);
    }

    public JarFile getOrCreate(boolean useCaches, URL url) throws IOException {
        if (useCaches) {
            JarFile cached = getFromCache(url);
            if (cached != null) {
                return cached;
            }
        }
        return factory.createJarFile(url, this::onClose);
    }

    public boolean cacheIfAbsent(boolean useCaches, URL jarFileUrl, JarFile jarFile) {
        if (!useCaches) {
            return false;
        }
        return this.cache.putIfAbsent(jarFileUrl, jarFile);
    }

    public void closeIfNotCached(URL jarFileURL, JarFile jarFile) throws IOException {
        JarFile cachedJar = getFromCache(jarFileURL);
        if (cachedJar != jarFile) {
            jarFile.close();
        }
    }

    public URLConnection reconnect(JarFile jarFile, URLConnection existingConnection) throws IOException {
        Boolean useCaches = existingConnection != null ? existingConnection.getUseCaches() : null;
        URLConnection connection = openConnection(jarFile);
        if (connection != null && useCaches != null) {
            connection.setUseCaches(useCaches);
        }
        return connection;
    }

    private URLConnection openConnection(JarFile jarFile) throws IOException {
        URL url = cache.get(jarFile);
        return url != null ? url.openConnection() : null;
    }

    private void onClose(JarFile jarFile) {
        cache.remove(jarFile);
    }

    private static final class Cache {
        private final Map<URL, JarFile> urlToJarFileMap;
        private final Map<JarFile, URL> jarFileToUrlMap;

        private Cache() {
            urlToJarFileMap = new HashMap<>();
            jarFileToUrlMap = new HashMap<>();
        }

        JarFile get(URL url) {
            synchronized (this) {
                return urlToJarFileMap.get(url);
            }
        }

        URL get(JarFile jarFile) {
            synchronized (this) {
                return jarFileToUrlMap.get(jarFile);
            }
        }

        boolean putIfAbsent(URL url, JarFile jarFile) {
            synchronized (this) {
                JarFile cached = urlToJarFileMap.get(url);
                if (cached == null) {
                    urlToJarFileMap.put(url, jarFile);
                    jarFileToUrlMap.put(jarFile, url);
                    return true;
                }
                return false;
            }
        }

        void remove(JarFile jarFile) {
            synchronized (this) {
                URL removedUrl = jarFileToUrlMap.remove(jarFile);
                if (removedUrl != null) {
                    urlToJarFileMap.remove(removedUrl);
                }
            }
        }

        void clear() {
            synchronized (this) {
                urlToJarFileMap.clear();
                jarFileToUrlMap.clear();
            }
        }
    }
}
