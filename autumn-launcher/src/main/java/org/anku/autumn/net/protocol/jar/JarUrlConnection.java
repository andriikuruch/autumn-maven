package org.anku.autumn.net.protocol.jar;


import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.security.Permission;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class JarUrlConnection extends JarURLConnection {
    private static final String CONTENT_TYPE = "x-java/jar";
    private static final String UNKNOWN_CONTENT = "content/unknown";
    private final JarFiles jarFiles = JarFiles.instance;
    private final String entryName;
    private String contentType;
    private JarFile jarFile;
    private JarEntry jarEntry;
    private URLConnection jarFileURLConnection;


    JarUrlConnection(URL url) throws IOException {
        super(url);
        entryName = getEntryName();
        jarFileURLConnection = getJarFileURL().openConnection();
        jarFileURLConnection.setUseCaches(useCaches);
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
    public void connect() throws IOException {
        if (connected) {
            return;
        }
        URL jarFileURL = getJarFileURL(); // nested:/path/myjar.jar/!BOOT-INF/lib/mylib.jar
        jarFile = jarFiles.getOrCreate(getUseCaches(), jarFileURL);
        jarEntry = getJarEntry(jarFileURL);
        boolean cached = jarFiles.cacheIfAbsent(getUseCaches(), jarFileURL, jarFile);
        if (cached) {
            boolean useCaches = jarFileURLConnection.getUseCaches();
            jarFileURLConnection = jarFiles.reconnect(jarFile, jarFileURLConnection);
            jarFileURLConnection.setUseCaches(useCaches);
        }
        connected = true;
    }

    private JarEntry getJarEntry(URL jarFileURL) throws IOException {
        if (entryName == null) {
            return null;
        }
        JarEntry entry = jarFile.getJarEntry(entryName);
        try {
            if (Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile().toString().equals(jarFile.getName())) {
                entry = jarFile.getJarEntry("BOOT-INF/classes/" + entryName);
            }
        } catch (URISyntaxException e) {
            // don't do anything
        }
        if (entry == null) {
            jarFiles.closeIfNotCached(jarFileURL, jarFile);
            throw new FileNotFoundException("Jar entry " + entryName + " not found in " + jarFile.getName());
        }
        return entry;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        String entryName = getEntryName();
        if (entryName == null) {
            throw new IOException("no entry name specified");
        }
        if (jarEntry == null) {
            throw new FileNotFoundException("Jar entry " + entryName + " not found in " + jarFile.getName());
        }

        return new ConnectionInputStream(jarFile.getInputStream(jarEntry));
    }

    class ConnectionInputStream extends FilterInputStream {

        protected ConnectionInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close () throws IOException {
            try {
                super.close();
            } finally {
                if (!getUseCaches()) {
                    jarFile.close();
                }
            }
        }
    }

    @Override
    public Permission getPermission() throws IOException {
        return jarFileURLConnection != null ? jarFileURLConnection.getPermission() : null;
    }

    @Override
    public int getContentLength() {
        long length = getContentLengthLong();
        return length <= Integer.MAX_VALUE ? (int) length : -1;
    }

    @Override
    public long getContentLengthLong() {
        try {
            connect();
            return jarEntry != null ? jarEntry.getSize() : jarFileURLConnection.getContentLengthLong();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public Object getContent() throws IOException {
        connect();
        return entryName != null ? super.getContent() : jarFile;
    }

    @Override
    public String getContentType() {
        if (contentType != null) {
            String entryName = getEntryName();
            if (entryName == null) {
                contentType = CONTENT_TYPE;
            } else {
                try {
                    connect();
                    InputStream inputStream = jarFile.getInputStream(jarEntry);
                    contentType = URLConnection.guessContentTypeFromStream(inputStream);
                    inputStream.close();
                } catch (IOException e) {
                    // don't do anything
                }
            }
            if (contentType == null) {
                contentType = URLConnection.guessContentTypeFromName(entryName);
            }
            if (contentType == null) {
                contentType =  UNKNOWN_CONTENT;
            }
        }
        return contentType;
    }

    @Override
    public String getHeaderField(String name) {
        return jarFileURLConnection != null ? jarFileURLConnection.getHeaderField(name) : null;
    }

    @Override
    public String getRequestProperty(String key) {
        return jarFileURLConnection != null ? jarFileURLConnection.getRequestProperty(key) : null;
    }

    @Override
    public void setRequestProperty(String key, String value) {
        if (jarFileURLConnection != null) {
            jarFileURLConnection.setRequestProperty(key, value);
        }
    }

    @Override
    public void addRequestProperty(String key, String value) {
        if (jarFileURLConnection != null) {
            jarFileURLConnection.addRequestProperty(key, value);
        }
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return jarFileURLConnection != null ? jarFileURLConnection.getRequestProperties() : Collections.emptyMap();
    }

    @Override
    public boolean getAllowUserInteraction() {
        return jarFileURLConnection != null && jarFileURLConnection.getAllowUserInteraction();
    }

    @Override
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        if (jarFileURLConnection != null) {
            jarFileURLConnection.setAllowUserInteraction(allowuserinteraction);
        }
    }

    @Override
    public boolean getUseCaches() {
        return jarFileURLConnection != null && jarFileURLConnection.getUseCaches();
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        if (jarFileURLConnection != null) {
            jarFileURLConnection.setUseCaches(usecaches);
        }
    }

    @Override
    public void setIfModifiedSince(long ifmodifiedsince) {
        if (jarFileURLConnection != null) {
            jarFileURLConnection.setIfModifiedSince(ifmodifiedsince);
        }
    }

    @Override
    public boolean getDefaultUseCaches() {
        return jarFileURLConnection != null && jarFileURLConnection.getDefaultUseCaches();
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        if (jarFileURLConnection != null) {
            jarFileURLConnection.setDefaultUseCaches(defaultusecaches);
        }
    }
}
