package org.anku.autumn.net.protocol.nested;

import org.anku.autumn.zip.ZipContent;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.Permission;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NestedURLConnection extends URLConnection {

    private static final DateTimeFormatter RFC_1123_DATE_TIME = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneId.of("GMT"));

    private volatile long size = -1;
    private volatile InputStream inputStream;

    private long lastModified = -1;
    private FilePermission permission;
    private Map<String, List<String>> headerFields;
    private final NestedLocation location;

    protected NestedURLConnection(URL url) {
        super(url);
        this.location = NestedLocation.fromUrl(url);
    }

    @Override
    public String getHeaderField(String name) {
        List<String> values = getHeaderFields().get(name);
        return values != null && !values.isEmpty() ? values.getFirst() : null;
    }

    @Override
    public String getHeaderField(int n) {
        Map.Entry<String, List<String>> entry = getHeaderFieldEntry(n);
        List<String> values = entry != null ? entry.getValue() : null;
        return values != null && !values.isEmpty() ? values.getFirst() : null;
    }

    @Override
    public String getHeaderFieldKey(int n) {
        Map.Entry<String, List<String>> entry = getHeaderFieldEntry(n);
        return entry != null ? entry.getKey() : null;
    }

    private Map.Entry<String, List<String>> getHeaderFieldEntry(int n) {
        Iterator<Map.Entry<String, List<String>>> iterator = getHeaderFields().entrySet().iterator();
        Map.Entry<String, List<String>> entry = null;
        for (int i = 0; i < n; i++) {
            entry = iterator.hasNext() ? iterator.next() : null;
        }
        return entry;
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
            long lastModified = getLastModified();
            if (contentLength > 0) {
                headerFields.put("content-length", List.of(String.valueOf(contentLength)));
            }
            if (lastModified > 0) {
                headerFields.put("last-modified", List.of(RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(lastModified))));
            }
            this.headerFields = Collections.unmodifiableMap(headerFields);
        }
        return headerFields;
    }

    @Override
    public int getContentLength() {
        long contentLengthLong = getContentLengthLong();
        return contentLengthLong < Integer.MAX_VALUE ? (int) contentLengthLong : -1;
    }

    @Override
    public long getContentLengthLong() {
        try {
            connect();
            return size;
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public String getContentType() {
        return "x-java/jar";
    }

    @Override
    public long getLastModified() {
        if (lastModified == -1) {
            try {
                lastModified = Files.getLastModifiedTime(location.getPath()).toMillis();
            } catch (IOException e) {
                lastModified = 0;
            }
        }
        return lastModified;
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

        ZipContent zipContent = ZipContent.open(location.getPath(), location.getNestedEntryName());


        connected = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        return super.getInputStream();
    }
}
