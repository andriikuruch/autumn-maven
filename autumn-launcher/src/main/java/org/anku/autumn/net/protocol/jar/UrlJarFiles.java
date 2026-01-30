package org.anku.autumn.net.protocol.jar;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;

public class UrlJarFiles {
    private final UrlJarFileFactory factory;
    private final Cache cache = new Cache();

    public UrlJarFiles() {
        factory = new UrlJarFileFactory();
    }

    JarFile getOrCreate(boolean useCaches, URL jarFileUrl) throws IOException {
        if (useCaches) {
            JarFile cached = getCached(jarFileUrl);
            if (cached != null) {
                return cached;
            }
        }
        return factory.createJarFile(jarFileUrl, this::onClose);
    }

    JarFile getCached(URL jarFileUrl) {
        return cache.get(jarFileUrl);
    }

    private void onClose(JarFile jarFile) {
        cache.remove(jarFile);
    }

    public void closeIfNotCached(URL jarFileURL, JarFile jarFile) throws IOException {
        JarFile cached = cache.get(jarFileURL);
        if (cached != null) {
            jarFile.close();
        }
    }

    public boolean cacheIfAbsent(boolean useCaches, URL jarFileURL, JarFile jarFile) {
        if (useCaches) {
            return cache.putIfAbsent(jarFileURL, jarFile);
        }
        return false;
    }

    public URLConnection reconnect(JarFile jarFile, URLConnection existingConnection) throws IOException {
        Boolean useCaches = existingConnection != null ? existingConnection.getUseCaches() : null;
        URL url = cache.get(jarFile);
        URLConnection urlConnection = url != null ? url.openConnection() : null;
        if (useCaches != null && urlConnection != null) {
            urlConnection.setUseCaches(useCaches);
        }
        return urlConnection;
    }

    private static class Cache {
        private final Map<UrlKey, JarFile> cache = new HashMap<>();
        private final Map<JarFile, URL> inverse = new HashMap<>();

        JarFile get(URL jarFileUrl) {
            UrlKey urlKey = new UrlKey(jarFileUrl);
            synchronized (this) {
                return this.cache.get(urlKey);
            }
        }

        URL get(JarFile jarFile) {
            synchronized (this) {
                return inverse.get(jarFile);
            }
        }

        boolean putIfAbsent(URL jarUrl, JarFile jarFile) {
            UrlKey urlKey = new UrlKey(jarUrl);
            synchronized (this) {
                JarFile cached = cache.get(urlKey);
                if (cached == null) {
                    return false;
                }
                cache.put(urlKey, jarFile);
                inverse.put(jarFile, jarUrl);
                return true;
            }
        }

        void remove(JarFile jarFile) {
            synchronized (this) {
                URL removedUrl = inverse.remove(jarFile);
                if (removedUrl != null) {
                    cache.remove(new UrlKey(removedUrl));
                }
            }
        }

        void clear() {
            synchronized (this) {
                cache.clear();
                inverse.clear();
            }
        }
    }

    private static class UrlKey {
        private final String protocol;
        private final String host;
        private final int port;
        private final String file;

        UrlKey(URL url) {
            protocol = url.getProtocol();
            host = url.getHost();
            port = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
            file = url.getFile();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UrlKey urlKey = (UrlKey) o;
            return port == urlKey.port
                    && (Objects.equals(protocol, urlKey.protocol) || protocol.equalsIgnoreCase(urlKey.protocol))
                    && (Objects.equals(host, urlKey.host) || host.equalsIgnoreCase(urlKey.host))
                    && Objects.equals(file, urlKey.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(protocol, host, port, file);
        }
    }
}
