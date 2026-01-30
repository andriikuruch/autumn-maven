package org.anku.autumn.net.protocol.nested;

import java.io.IOException;
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
//        return new NestedURLConnection(u);
        System.out.println(" URL: " + u);
        System.out.println("  Handler invoked!");
        System.out.println("  Protocol: " + u.getProtocol());
        System.out.println("  Path: " + u.getPath());
        System.out.println("  File: " + u.getFile());
        System.out.println("  Host: " + u.getHost());
        System.out.println("  Authority: " + u.getAuthority());
        System.out.println("  Query: " + u.getQuery());
        System.out.println("  Ref: " + u.getRef());

        return new URLConnection(u) {
            @Override
            public void connect() throws IOException {
                System.out.println("  connect() called");
            }
        };
    }

    public static void assertUrlIsNotMalformed(String url) {
        if (url == null || !url.startsWith(PREFIX)) {
            throw new IllegalArgumentException("url must not be null or must use nested protocol");
        }
        NestedLocation.parse(url.substring(PREFIX.length()));
    }
}
