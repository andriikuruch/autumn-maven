package org.anku.autumn.net.protocol.jar;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class JarUrlConnection extends JarURLConnection {

    private static final UrlJarFiles jarFiles = new UrlJarFiles();

    private URLConnection jarFileConnection;
    private JarFile jarFile;
    private JarEntry jarEntry;
    private String entryName;
    private boolean connected;
    private String contentType;

    JarUrlConnection(URL url) throws IOException {
        super(url);
        entryName = getEntryName();
        jarFileConnection = getJarFileURL().openConnection();
        jarFileConnection.setUseCaches(this.getUseCaches());
    }

    public static URLConnection open(URL u) throws IOException {
        return new JarUrlConnection(u);
    }

    @Override
    public void connect() throws IOException {
        if (connected) {
            return;
        }
        jarFile = jarFiles.getOrCreate(getUseCaches(), getJarFileURL());
        jarEntry = getJarEntry(getJarFileURL());
        boolean addedToCache = jarFiles.cacheIfAbsent(getUseCaches(), getJarFileURL(), jarFile);
        if (addedToCache) {
            jarFileConnection = jarFiles.reconnect(jarFile, jarFileConnection);
        }
        connected = true;
    }

    private JarEntry getJarEntry(URL jarFileURL) throws IOException {
        if (entryName == null) {
            return null;
        }
        JarEntry jarEntry = jarFile.getJarEntry(entryName);
        if (jarEntry == null) {
            jarFiles.closeIfNotCached(jarFileURL, jarFile);
            throw new FileNotFoundException("Jar entry %s% not found in %s%".formatted(entryName, jarFile.getName()));
        }
        return jarEntry;
    }

    @Override
    public JarFile getJarFile() throws IOException {
        connect();
        return jarFile;
    }

    @Override
    public JarEntry getJarEntry() throws IOException {
        connect();
        return jarEntry;
    }

    @Override
    public Permission getPermission() throws IOException {
        return jarFileConnection != null
                ? jarFileConnection.getPermission()
                : null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        if (entryName == null && !UrlJarFileFactory.isNestedUrl(getJarFileURL())) {
            throw new IOException("no entry name specified");
        }
        if (jarEntry == null) {
            if (jarFile instanceof NestedJarFile nestedJarFile) {
                return nestedJarFile.getRawZipDataInputStream();
            }
            throw new FileNotFoundException("JAR entry " + entryName +
                    " not found in " +
                    jarFile.getName());
        }
        return new JarURLInputStream(jarFile.getInputStream(jarEntry));
    }

    @Override
    public int getContentLength() {
        long length = getContentLengthLong();
        if (length > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) length;
    }

    @Override
    public long getContentLengthLong() {
        try {
            connect();
            return jarEntry != null
                    ? jarEntry.getSize()
                    : jarFileConnection != null ? jarFileConnection.getContentLengthLong() : -1;
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public Object getContent() throws IOException {
        connect();
        return entryName != null
                ? super.getContent()
                : jarFile;
    }

    @Override
    public String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        if (entryName == null) {
            contentType = "x-java/jar";
            return contentType;
        }
        try {
            connect();
            try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                contentType = guessContentTypeFromStream(new BufferedInputStream(inputStream));
                if (contentType != null) {
                    return contentType;
                }
            }
        } catch (Exception e) {
            return null;
        }
        contentType = guessContentTypeFromName(entryName);
        return contentType != null
                ? contentType
                : "content/unknown";
    }

    @Override
    public String getHeaderField(String name) {
        return jarFileConnection != null
                ? jarFileConnection.getHeaderField(name)
                : null;
    }

    @Override
    public void setRequestProperty(String key, String value) {
        if (jarFileConnection != null) {
            jarFileConnection.setRequestProperty(key, value);
        }
    }

    @Override
    public String getRequestProperty(String key) {
        return jarFileConnection != null
                ? jarFileConnection.getRequestProperty(key)
                : null;
    }

    @Override
    public void addRequestProperty(String key, String value) {
        if (jarFileConnection != null) {
            jarFileConnection.addRequestProperty(key, value);
        }
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return jarFileConnection != null
                ? jarFileConnection.getRequestProperties()
                : Collections.emptyMap();
    }

    @Override
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        if (jarFileConnection != null) {
            jarFileConnection.setAllowUserInteraction(allowUserInteraction);
        }
    }

    @Override
    public boolean getAllowUserInteraction() {
        return jarFileConnection != null && jarFileConnection.getAllowUserInteraction();
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        if (jarFileConnection != null) {
            jarFileConnection.setUseCaches(useCaches);
        }
    }

    @Override
    public boolean getUseCaches() {
        return jarFileConnection != null && jarFileConnection.getUseCaches();
    }

    @Override
    public long getLastModified() {
        return jarFileConnection != null
                ? jarFileConnection.getLastModified()
                : super.getLastModified();
    }

    @Override
    public void setIfModifiedSince(long ifmodifiedsince) {
        if (jarFileConnection != null) {
            jarFileConnection.setIfModifiedSince(ifModifiedSince);
        }
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        if (jarFileConnection != null) {
            jarFileConnection.setDefaultUseCaches(defaultusecaches);
        }
    }

    @Override
    public boolean getDefaultUseCaches() {
        return jarFileConnection != null && jarFileConnection.getUseCaches();
    }

    private class JarURLInputStream extends InputStream {
        private volatile InputStream inputStream;

        public JarURLInputStream(InputStream inputStream) {
            super();
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return inputStream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return inputStream.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return inputStream.skip(n);
        }

        @Override
        public int available() throws IOException {
            return inputStream.available();
        }

        @Override
        public boolean markSupported() {
            return inputStream.markSupported();
        }

        @Override
        public synchronized void mark(int readLimit) {
            inputStream.mark(readLimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            inputStream.reset();
        }

        @Override
        public void close() throws IOException {
            try {
                inputStream.close();
            } finally {
                if (!getUseCaches()) {
                    JarUrlConnection.this.jarFile.close();
                }
            }
        }
    }
}
