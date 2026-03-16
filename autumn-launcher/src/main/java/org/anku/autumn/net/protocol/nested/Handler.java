package org.anku.autumn.net.protocol.nested;

import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
    private static final String PREFIX = "nested:";

    @Override
    protected InetAddress getHostAddress(URL u) {
        return null;
    }

    @Override
    protected URLConnection openConnection(URL u) {
        return new NestedUrlConnection(u);
    }

    public static void assertUrlIsNotMalformed(String url) {
        if (url == null || !url.startsWith(PREFIX)) {
            throw new IllegalArgumentException("url must not be null or must use nested protocol");
        }
        NestedLocation.parse(url.substring(PREFIX.length()));
    }
}
