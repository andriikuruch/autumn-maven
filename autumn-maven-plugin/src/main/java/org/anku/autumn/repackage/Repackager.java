package org.anku.autumn.repackage;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface Repackager {

    interface RepackagerBuilder {

        static RepackagerBuilder builder() {
            return new RepackagerBuilderImpl();
        }

        RepackagerBuilder addGroupId(String groupId);

        RepackagerBuilder addArtifactId(String artifactId);

        RepackagerBuilder addVersion(String version);

        RepackagerBuilder addOutputDirectory(File outputDirectory);

        RepackagerBuilder addProjectJar(File projectJar);

        RepackagerBuilder addDependenciesToPuck(Set<Path> dependencies2puck);

        RepackagerBuilder addPluginDependencies(Set<Path> pluginDependencies);

        RepackagerBuilder addBasedir(File basedir);

        RepackagerBuilder addMainClass(String mainClass);

        RepackagerBuilder addLogger(Log log);

        Repackager build();
    }

    void repackage() throws MojoExecutionException, IOException;
}
