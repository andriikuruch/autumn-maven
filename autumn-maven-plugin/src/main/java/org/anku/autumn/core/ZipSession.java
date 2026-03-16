package org.anku.autumn.core;

import org.anku.autumn.utils.Constants;
import org.anku.autumn.utils.ZipUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.function.Predicate;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipSession implements AutoCloseable {
    private final ZipOutputStream zos;
    private final Set<String> added;
    private final Set<String> classpathIdx;
    private final BuildOptions options;
    private final Log log;

    public ZipSession(Path outZip, BuildOptions options, Log log) throws IOException {
        this.zos = new ZipOutputStream(Files.newOutputStream(outZip));
        this.added = new HashSet<>();
        this.classpathIdx = new HashSet<>();
        this.options = options;
        this.log = log;
    }

    public void addFiles(Collection<Path> files, EntryNamer namer, Compression compression) throws IOException {
        for (Path path : files) {
            addPath(path, namer, compression);
        }
    }

    public void addJar(
            Path sourceJar,
            Predicate<String> filter,
            PassthroughNamer namer,
            Compression compression,
            boolean skipMetaInf
    ) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(sourceJar))) {
            ZipEntry in;
            while ((in = zis.getNextEntry()) != null) {
                String original = ZipUtils.normalizeDir(in.getName());
                if (
                        skipMetaInf && original.startsWith(Constants.META_INF + Constants.ROOT)
                                || filter != null && filter.test(original)
                ) {
                    zis.closeEntry();
                    continue;
                }
                if (in.isDirectory()) {
                    writeDirectoryEntry(namer.entryNameFroJar(original));
                    zis.closeEntry();
                    continue;
                }
                String targetName = namer.entryNameFroJar(original);
                String finalName = handleDuplication(ZipUtils.normalizeDir(targetName));
                if (finalName == null) {
                    zis.closeEntry();
                    continue;
                }
                addParentDirectories(finalName);

                ZipEntry out = new ZipEntry(finalName);
                out.setMethod(compression == Compression.STORED ? ZipEntry.STORED : ZipEntry.DEFLATED);

                if (compression == Compression.STORED) {
                    byte[] bytes = zis.readAllBytes();
                    CRC32 crc = new CRC32(); crc.update(bytes);
                    out.setSize(bytes.length);
                    out.setCompressedSize(bytes.length);
                    out.setCrc(crc.getValue());
                    zos.putNextEntry(out);
                    zos.write(bytes);
                } else if (Compression.DEFLATED == compression) {
                    out.setMethod(ZipEntry.DEFLATED);
                    zos.putNextEntry(out);
                    zis.transferTo(zos);
                }
                zos.closeEntry();
                zis.closeEntry();
            }
        }
    }

    public void addManifest(
            String version,
            String artifactId,
            String groupId,
            Path classes
    ) throws IOException, MojoExecutionException {
        String path = Constants.META_INF + Constants.ROOT + Constants.ManifestFile.MANIFEST_MF;
        if (isMarkedAdded(path)) {
            return;
        }
        byte[] bytes = ZipUtils.buildManifestBytes(version, artifactId, groupId, classes);
        ZipEntry ze = new ZipEntry(path);
        ze.setMethod(ZipEntry.DEFLATED);
        markAdded(path);
        zos.putNextEntry(ze);
        zos.write(bytes);
        zos.closeEntry();
    }

    private String handleDuplication(String name) {
        if (!isMarkedAdded(name)) {
            markAdded(name);
            return name;
        }

        return switch (options.duplicatePolicy()) {
            case SKIP -> null;
            case ERROR -> throw new IllegalArgumentException("Duplicate entry: " + name);
            case RENAME_WITH_SUFFIX -> {
                String renamed = ZipUtils.renameWithSuffix(name, path -> !isMarkedAdded(path));
                markAdded(renamed);
                yield renamed;
            }
        };
    }

    private void addPath(Path src, EntryNamer namer, Compression compression) throws IOException {
        if (Files.isDirectory(src)) {
            writeDirectoryEntry(namer.entryName(src));
            try (Stream<Path> children = Files.list(src)) {
                for (Path child : children.toList()) {
                    addPath(child, namer, compression);
                }
            }
            return;
        }

        String name = ZipUtils.normalizeDir(namer.entryName(src));
        String finalName = handleDuplication(name);
        if (finalName == null) {
            return;
        }
        addParentDirectories(finalName);
        writePath(src, finalName, compression);
    }

    private void writeDirectoryEntry(String name) throws IOException {
        String dirName = normalizeDirectoryEntryName(name);
        String finalName = handleDuplication(dirName);
        if (finalName == null) {
            return;
        }
        addParentDirectories(finalName);
        ZipEntry directory = new ZipEntry(finalName);
        directory.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(directory);
        zos.closeEntry();
    }

    private void addParentDirectories(String entryName) throws IOException {
        int slash = entryName.lastIndexOf(Constants.ROOT);
        if (slash <= 0) {
            return;
        }
        String[] parts = entryName.substring(0, slash).split(Constants.ROOT);
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            current.append(part).append(Constants.ROOT);
            String dir = current.toString();
            String finalName = handleDuplication(dir);
            if (finalName == null) {
                continue;
            }
            ZipEntry parent = new ZipEntry(finalName);
            parent.setMethod(ZipEntry.DEFLATED);
            zos.putNextEntry(parent);
            zos.closeEntry();
        }
    }

    private String normalizeDirectoryEntryName(String name) {
        String normalized = ZipUtils.normalizeDir(name);
        if (normalized.endsWith(Constants.ROOT)) {
            return normalized;
        }
        return normalized + Constants.ROOT;
    }

    private void writePath(Path src, String finalName, Compression compression) throws IOException {
        ZipEntry ze = new ZipEntry(finalName);
        if (Compression.STORED == compression) {
            long size = Files.size(src);
            CRC32 crc32 = new CRC32();
            try (InputStream inputStream = Files.newInputStream(src)) {
                byte[] bytes = inputStream.readAllBytes();
                crc32.update(bytes);
            }
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(size);
            ze.setCompressedSize(size);
            ze.setCrc(crc32.getValue());
            addToClasspath(finalName);
        } else if (Compression.DEFLATED == compression) {
            ze.setMethod(ZipEntry.DEFLATED);
        }
        zos.putNextEntry(ze);
        try (InputStream inputStream = Files.newInputStream(src)) {
            inputStream.transferTo(zos);
        }
        zos.closeEntry();
    }

    private boolean isMarkedAdded(String path) {
        return added.contains(path);
    }

    private void markAdded(String path) {
        added.add(path);
    }

    private void addToClasspath(String file) {
        classpathIdx.add(file);
    }

    private void addClasspathIndex() throws IOException {
        String classPathIx = Constants.BOOT_INF + Constants.ROOT + "classpath.idx";
//        String bootInf = Constants.BOOT_INF + Constants.ROOT;
//        String finalName = handleDuplication(bootInf + "classpath.idx");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DataOutputStream dos = new DataOutputStream(baos);
            for (String file : classpathIdx) {
                dos.writeBytes("- " + file + "\n");
                log.info(file);
            }
            dos.flush();
            ZipEntry ze = new ZipEntry(classPathIx);
            zos.putNextEntry(ze);
            zos.write(baos.toByteArray());
            zos.closeEntry();
        }
    }

    @Override
    public void close() throws IOException {
        addClasspathIndex();
        zos.close();
    }
}
