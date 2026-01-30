package org.anku.autumn.net.protocol.jar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
    private static final String PROTOCOL = "jar";
    private static final String SEPARATOR = "!/";
    static final Handler INSTANCE = new Handler();

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return JarUrlConnection.open(u);
    }

    @Override
    protected void parseURL(URL u, String spec, int start, int limit) {
        int index = spec.indexOf(SEPARATOR);
        if (index < 0 || index + 2 > limit) {
            throw new IllegalStateException("'!/' is required for url");
        }
        String innerUrl = spec.substring(start, index);
        org.anku.autumn.net.protocol.nested.Handler.assertUrlIsNotMalformed(innerUrl);
        String path = spec.substring(start, limit);
        setURL(u, PROTOCOL, "", -1, null, null, path, null, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected int hashCode(URL u) {
        String protocol = u.getProtocol();
        int hash = protocol != null ? protocol.hashCode() : 0;
        String file = u.getFile();
        int indexOfSeparator = file.indexOf(SEPARATOR);
        if (indexOfSeparator == -1 ) {
            return hash + file.hashCode();
        }
        String fileWithoutEntry = file.substring(0, indexOfSeparator);
        try {
            hash += new URL(fileWithoutEntry).hashCode();
        } catch (MalformedURLException e) {
            hash += fileWithoutEntry.hashCode();
        }
        String entry = file.substring(indexOfSeparator + 2);
        return hash + entry.hashCode();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean sameFile(URL u1, URL u2) {
        if (!PROTOCOL.equals(u1.getProtocol()) || !PROTOCOL.equals(u2.getProtocol())) {
            return false;
        }
        String file1 = u1.getFile();
        String file2 = u2.getFile();
        int indexOfSeparator1 = file1.indexOf(SEPARATOR);
        int indexOfSeparator2 = file2.indexOf(SEPARATOR);
        if (indexOfSeparator1 == -1 || indexOfSeparator2 == -1) {
            return super.sameFile(u1, u2);
        }
        String entry1 = file1.substring(indexOfSeparator1 + 2);
        String entry2 = file2.substring(indexOfSeparator2 + 2);
        if (!entry1.equals(entry2)) {
            return false;
        }
        try {
            URL innerUrl1 = new URL(file1.substring(0, indexOfSeparator1));
            URL innerUrl2 = new URL(file2.substring(0, indexOfSeparator2));
            if (!super.sameFile(innerUrl1, innerUrl2)) {
                return false;
            }
        } catch (MalformedURLException e) {
            return super.sameFile(u1, u2);
        }
        return true;
    }
}
