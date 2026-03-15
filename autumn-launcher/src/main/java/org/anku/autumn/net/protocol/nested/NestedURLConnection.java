package org.anku.autumn.net.protocol.nested;

import org.anku.autumn.net.protocol.jar.JarFiles;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class NestedUrlConnection extends URLConnection {

    private static final DateTimeFormatter RFC_1123_DATE_TIME = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneId.of("GMT"));
    private static final String CONTENT_TYPE = "x-java/jar";

    private final NestedLocation location;
    private final JarFiles jarFiles;
    private InputStream inputStream;
    private long length = -1;
    private long lastModified = 0;
    private FilePermission permission;
    private Map<String, List<String>> headerFields;

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    protected NestedUrlConnection(URL url) {
        super(url); // url = nested:/path/myjar.jar/!BOOT-INF/lib/mylib.jar
        jarFiles = JarFiles.instance;
        location = NestedLocation.fromUrl(url); // location = {path=/path/myjar.jar, entry=BOOT-INF/lib/mylib.jar}
    }

    @Override
    public int getContentLength() {
        if (length <= Integer.MAX_VALUE) {
            return (int) length;
        }
        return -1;
    }

    @Override
    public String getHeaderField(int n) {
        Map.Entry<String, List<String>> entry = getHeaderFields()
                .entrySet()
                .stream()
                .skip(n)
                .findFirst()
                .orElse(null);
        return entry != null ? entry.getValue().getFirst() : null;
    }

    @Override
    public String getHeaderFieldKey(int n) {
        Map.Entry<String, List<String>> entry = getHeaderFields()
                .entrySet()
                .stream()
                .skip(n)
                .findFirst()
                .orElse(null);
        return entry != null ? entry.getKey() : null;
    }

    @Override
    public long getContentLengthLong() {
        return length;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public String getHeaderField(String name) {
        List<String> headerValues = getHeaderFields().get(name);
        return headerValues != null && !headerValues.isEmpty() ? headerValues.getFirst() : null;
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        try {
            connect();
        } catch (IOException e) {
            return Collections.emptyMap();
        }
        if (headerFields == null) {
            Map<String, List<String>> headerFields = new LinkedHashMap<>();
            long contentLength = getContentLengthLong();
            long modificationTime = getLastModified();
            if (contentLength > 0) {
                headerFields.put(
                        "content-length",
                        Collections.singletonList(Long.toString(contentLength))
                );
            }
            if (modificationTime > 0) {
                headerFields.put(
                        "last-modified",
                        List.of(RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(modificationTime)))
                );
            }
            this.headerFields = headerFields;
        }
        return headerFields;
    }

    @Override
    public Permission getPermission() throws IOException {
        if (permission == null) {
            File file = location.getPath().toFile();
            permission = new FilePermission(file.getCanonicalPath(), "read");
        }
        return permission;
    }

    @Override
    public void connect() throws IOException {
        if (connected) {
            return;
        }
        URL parentJarFileUrl = location.getPath().toUri().toURL();
        JarFile parentJarFile = jarFiles.getOrCreate(getUseCaches(), parentJarFileUrl);
        jarFiles.cacheIfAbsent(getUseCaches(), parentJarFileUrl, parentJarFile);
        JarEntry nestedJarEntry = parentJarFile
                .stream()
                .filter(entry -> entry.getName().endsWith(location.getNestedEntryName()))
                .findFirst()
                .orElseThrow();
        length = nestedJarEntry.getSize();
        lastModified = nestedJarEntry.getTime();
        inputStream = parentJarFile.getInputStream(nestedJarEntry);
        connected = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        if (inputStream == null) {
            throw new IOException("Jar file not found: " + getURL());
        }
        return inputStream;
    }
}
