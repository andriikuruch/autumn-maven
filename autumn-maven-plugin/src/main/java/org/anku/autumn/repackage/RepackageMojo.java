package org.anku.autumn.repackage;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.sisu.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.anku.autumn.utils.Constants.ORIGINAL_EXTENSION;

@Mojo(name = "repackage", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RepackageMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project.groupId}", required = true)
    private String groupId;

    @Parameter(defaultValue = "${project.artifactId}", required = true)
    private String artifactId;

    @Parameter(defaultValue = "${project.version}", required = true)
    private String version;

    @Parameter
    private @Nullable String mainClass;

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor pluginDescriptor;

    @Override
    public void execute() throws MojoExecutionException {
        File projectJar = project.getArtifact().getFile();
        File basedir = project.getBasedir();

        File originalJar = new File(projectJar.getAbsolutePath() + ORIGINAL_EXTENSION);
        try {
            Path path = Files.copy(projectJar.toPath(), originalJar.toPath());
            getLog().debug("Repackage: Copy original jar to " + path);
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }

        Set<Path> dependencies2puck = new HashSet<>(project.getArtifacts().size());
        for (Object dependency : project.getArtifacts()) {
            Path dependencyPath = ((Artifact) dependency).getFile().toPath();
            getLog().debug("Dependency: " + dependencyPath);
            dependencies2puck.add(dependencyPath);
        }

        Set<Path> pluginDependencies = pluginDescriptor.getArtifacts()
                .stream()
                .map(artifact -> artifact.getFile().toPath())
                .collect(Collectors.toSet());

        Repackager repackager = Repackager.RepackagerBuilder.builder()
                .addArtifactId(artifactId)
                .addGroupId(groupId)
                .addVersion(version)
                .addProjectJar(projectJar)
                .addDependenciesToPuck(dependencies2puck)
                .addPluginDependencies(pluginDependencies)
                .addOutputDirectory(outputDirectory)
                .addBasedir(basedir)
                .addMainClass(mainClass)
                .addLogger(getLog())
                .build();


        try {
            repackager.repackage();
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }
}
