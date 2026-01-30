package org.anku.autumn.repackage;

import org.anku.autumn.core.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.anku.autumn.utils.Constants.*;
import static org.anku.autumn.utils.Constants.ManifestFile.MAVEN_ARCHIVER;

public class RepackagerImpl implements Repackager {

    private final File basedir;
    private final File projectJar;
    private final Set<Path> dependencies2puck;
    private final Set<Path> pluginDependencies;
    private final File outputDirectory;
    private final String version;
    private final String artifactId;
    private final String groupId;
    private final String startClass;
    private final Log log;

    private Path bootInfDir;
    private String classesDir;
    private String libsDir;
    private String artifactIdDir;
    private Path metaInfDir;
    private Path mavenDir;
    private Path groupIdDir;
    private Path sourceClasses;
    private Path launcherJar;
    private Path apacheCompress;
    private Set<Path> projectClasses;
    private Path sourcePom;
    private Path sourcePomProperties;

    public RepackagerImpl(
            File projectJar,
            Set<Path> dependencies2puck,
            Set<Path> pluginDependencies,
            File outputDirectory,
            String version,
            String artifactId,
            String groupId,
            File basedir,
            String startClass,
            Log log
    ) {
        this.projectJar = projectJar;
        this.dependencies2puck = dependencies2puck;
        this.pluginDependencies = pluginDependencies;
        this.outputDirectory = outputDirectory;
        this.version = version;
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.basedir = basedir;
        this.startClass = startClass;
        this.log = log;
    }

    @Override
    public void repackage() throws MojoExecutionException, IOException {
        definePaths();
        resolveLauncher();
        resolveProjectClasses();

        try (ZipSession jar = new ZipSession(projectJar.toPath(), new BuildOptions(DuplicatePolicy.SKIP), log)) {
            jar.addFiles(projectClasses, new HierarchyNamer(sourceClasses, classesDir), Compression.DEFLATED);
            jar.addFiles(dependencies2puck, new FlatNamer(libsDir), Compression.STORED);
            jar.addFiles(Set.of(sourcePom, sourcePomProperties), new FlatNamer(artifactIdDir), Compression.DEFLATED);
            jar.addJar(launcherJar, null, new PassthroughNamer(""), Compression.DEFLATED, true);
            jar.addJar(apacheCompress, null, new PassthroughNamer(""), Compression.DEFLATED, true);
            jar.addManifest(version, artifactId, groupId, sourceClasses);
        }
    }

    private void definePaths() {
        bootInfDir = Path.of(BOOT_INF); // /BOOT-INF
        classesDir = bootInfDir.resolve(CLASSES)+ ROOT; // /BOOT-INF/classes/
        libsDir = bootInfDir.resolve(LIBS) + ROOT; // /BOOT-INF/libs
        metaInfDir = Path.of(META_INF); // /META-INF
        mavenDir = metaInfDir.resolve(MAVEN); // /META-INF/maven
        groupIdDir = mavenDir.resolve(this.groupId); // /META-INF/maven/<groupId>
        artifactIdDir = groupIdDir.resolve(artifactId) + ROOT; // /META-INF/maven/<groupId>/<artifactId>

        sourceClasses = outputDirectory.toPath().resolve(CLASSES); // /<path-to-build-directory>/classes
                                                                   // example: /project/module/target/classes
        sourcePom = basedir.toPath().resolve(POM_XML); // /<path-to-project-directory>/pom.xml
                                                       // example: /project/module/pom.xml
        sourcePomProperties = outputDirectory.toPath()
                .resolve(MAVEN_ARCHIVER)
                .resolve(POM_PROPERTIES); // /<path-to-build-directory>/maven-archive/pom.properties
                                          // example: /project/module/target/maven-archive/pom.properties
    }

    private void resolveLauncher() {
        for (Path pluginDependency : pluginDependencies) {
            if (pluginDependency.getFileName().toString().startsWith(LAUNCHER)) {
                launcherJar = pluginDependency;
                continue;
            }
            if (pluginDependency.getFileName().toString().startsWith(APACHE_COMPRESS)) {
                apacheCompress = pluginDependency;
            }
        }
    }

    private void resolveProjectClasses() throws IOException {
        try (Stream<Path> walk = Files.walk(sourceClasses)) {
            projectClasses = walk.filter(Files::isRegularFile).collect(Collectors.toSet());
        }
    }
}
