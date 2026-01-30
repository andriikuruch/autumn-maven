package org.anku.autumn.utils;

import org.anku.autumn.launch.JarLauncher;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static org.anku.autumn.utils.Constants.ManifestFile.Attributes.*;

public final class ZipUtils {

    private ZipUtils() {
        // util class
    }

    public static String normalizeDir(String path) {
        return path.replace("\\", Constants.ROOT);
    }

    public static String renameWithSuffix(String path, Predicate<String> isFree) {
        int slash = path.lastIndexOf("/");
        String dir = slash >= 0 ? path.substring(0, slash + 1) : "";
        String file = slash >= 0 ? path.substring(slash + 1) : path;

        int dot = file.lastIndexOf(".");
        String fileName = dot >= 0 ? file.substring(0, dot) : file;
        String extension = dot >= 0 ? file.substring(dot) : "";

        String candidate;
        int i = 1;
        do {
            candidate = dir + fileName + "(" + i + ")" + extension;
            i++;
        } while (!isFree.test(candidate));
        return candidate;
    }

    public static byte[] buildManifestBytes(
            String version,
            String artifactId,
            String groupId,
            Path classes
    ) throws MojoExecutionException, IOException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.putValue(MANIFEST_VERSION, Constants.ManifestFile.MANIFEST_VERSION);
        attributes.putValue(BUILD_JDK_SPEC, BUILD_JDK_SPEC);
        attributes.putValue(CREATED_BY, Constants.REPACKAGER);
        attributes.putValue(IMPLEMENTATION_TITLE, artifactId);
        attributes.putValue(IMPLEMENTATION_VERSION, version);
        attributes.putValue(IMPLEMENTATION_VERSION_ID, groupId);
        attributes.putValue(MAIN_CLASS, JarLauncher.class.getName());
        attributes.putValue(START_CLASS, StartClassProvider.provide(classes));
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            manifest.write(baos);
            return baos.toByteArray();
        }
    }
}
