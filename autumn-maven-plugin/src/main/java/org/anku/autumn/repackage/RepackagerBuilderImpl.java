package org.anku.autumn.repackage;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public class RepackagerBuilderImpl implements Repackager.RepackagerBuilder {
    private File projectJar;
    private Set<Path> dependencies2puck;
    private Set<Path> pluginDependencies;
    private File outputDirectory;
    private String version;
    private String artifactId;
    private String groupId;
    private File basedir;
    private String mainClass;
    private Log log;

    RepackagerBuilderImpl() {  }

    @Override
    public Repackager.RepackagerBuilder addGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    @Override
    public Repackager.RepackagerBuilder addArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    @Override
    public Repackager.RepackagerBuilder addVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public Repackager.RepackagerBuilder addOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    @Override
    public Repackager.RepackagerBuilder addProjectJar(File projectJar) {
        this.projectJar = projectJar;
        return this;
    }

    @Override
    public Repackager.RepackagerBuilder addDependenciesToPuck(Set<Path> dependencies2puck) {
        this.dependencies2puck = dependencies2puck;
        return this;
    }

    @Override
    public Repackager.RepackagerBuilder addPluginDependencies(Set<Path> pluginDependencies) {
        this.pluginDependencies = pluginDependencies;
        return this;
    }

    @Override
    public Repackager.RepackagerBuilder addBasedir(File basedir) {
        this.basedir = basedir;
        return this;
    }

    @Override
    public Repackager.RepackagerBuilder addMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    @Override
    public Repackager.RepackagerBuilder addLogger(Log log) {
        this.log = log;
        return this;
    }

    @Override
    public Repackager build() {
        return new RepackagerImpl(
                projectJar,
                dependencies2puck,
                pluginDependencies,
                outputDirectory,
                version,
                artifactId,
                groupId,
                basedir,
                mainClass,
                log
        );
    }
}
