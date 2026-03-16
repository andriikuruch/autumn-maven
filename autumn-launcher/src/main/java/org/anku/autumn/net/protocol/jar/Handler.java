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
        return new JarUrlConnection(u);
    }

    @Override
    protected void parseURL(URL u, String spec, int start, int limit) {
        if (spec.length() >= 4 && spec.regionMatches(true, 0, "jar:", 0, 4)) {
            String path = parseAbsolutePath(spec, start, limit);
            setURL(u, PROTOCOL, "", -1, null, null, path, null, null);
            return;
        }
        String path = parseContextSpec(u, spec);
        int indexedOfBangSlash = indexOfBangSlash(path);
        path = canonicalizeString(path, indexedOfBangSlash);
        setURL(u, PROTOCOL, "", -1, null, null, path, null, null);
    }

    private static String parseAbsolutePath(String spec, int start, int limit) {
        int index = spec.indexOf(SEPARATOR);
        if (index < 0 || index + 2 > limit) {
            throw new IllegalStateException("'!/' is required for url");
        }
        String innerUrl = spec.substring(start, index);
        org.anku.autumn.net.protocol.nested.Handler.assertUrlIsNotMalformed(innerUrl);
        return spec.substring(start, limit);
    }

    private String parseContextSpec(URL url, String spec) {
        String ctxFile = url.getFile();
        // if the spec begins with /, chop up the jar back !/
        if (spec.startsWith("/")) {
            int bangSlash = indexOfBangSlash(ctxFile);
            if (bangSlash == -1) {
                throw new NullPointerException("malformed " +
                        "context url:" +
                        url +
                        ": no !/");
            }
            ctxFile = ctxFile.substring(0, bangSlash);
        } else {
            // chop up the last component
            int lastSlash = ctxFile.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new NullPointerException("malformed " +
                        "context url:" +
                        url);
            } else if (lastSlash < ctxFile.length() - 1) {
                ctxFile = ctxFile.substring(0, lastSlash + 1);
            }
        }
        return (ctxFile + spec);
    }

    private static int indexOfBangSlash(String spec) {
        int indexOfBang = spec.length();
        while((indexOfBang = spec.lastIndexOf('!', indexOfBang)) != -1) {
            if ((indexOfBang != (spec.length() - 1)) &&
                    (spec.charAt(indexOfBang + 1) == '/')) {
                return indexOfBang + 1;
            } else {
                indexOfBang--;
            }
        }
        return -1;
    }

    private static String canonicalizeString(String file, int off) {
        int len = file.length();
        if (off >= len || (file.indexOf("./", off) == -1 && file.charAt(len - 1) != '.')) {
            return file;
        } else {
            // Defer substring and concat until canonicalization is required
            String before = file.substring(0, off);
            String after = file.substring(off);
            return before + doCanonicalize(after);
        }
    }

    private static String doCanonicalize(String file) {
        int i, lim;

        // Remove embedded /../
        while ((i = file.indexOf("/../")) >= 0) {
            if ((lim = file.lastIndexOf('/', i - 1)) >= 0) {
                file = file.substring(0, lim) + file.substring(i + 3);
            } else {
                file = file.substring(i + 3);
            }
        }
        // Remove embedded /./
        while ((i = file.indexOf("/./")) >= 0) {
            file = file.substring(0, i) + file.substring(i + 2);
        }
        // Remove trailing ..
        while (file.endsWith("/..")) {
            i = file.indexOf("/..");
            if ((lim = file.lastIndexOf('/', i - 1)) >= 0) {
                file = file.substring(0, lim+1);
            } else {
                file = file.substring(0, i);
            }
        }
        // Remove trailing .
        if (file.endsWith("/."))
            file = file.substring(0, file.length() -1);

        return file;
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
