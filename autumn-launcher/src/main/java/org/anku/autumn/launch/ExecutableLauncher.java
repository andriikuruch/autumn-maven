package org.anku.autumn.launch;

import org.anku.autumn.archive.Archive;
import org.anku.autumn.net.protocol.Handlers;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Manifest;

public abstract class ExecutableLauncher implements Launcher {
    private static final String START_CLASS = "Start-Class";
    private final Archive archive;
    private URLClassLoader classLoader;

    ExecutableLauncher() throws Exception {
        archive = Archive.create(Launcher.class);
    }

    @Override
    public void launch() throws Exception {
        Handlers.register();
        String startClass = getStartClass();
        Set<URL> urls = archive.getClassPathUrls();
        urls.forEach(System.out::println);
        URL url1 = new URL("jar:nested:C:/Users/andre/IdeaProjects/embedded_tomcat/target/embedded_tomcat-1.0-SNAPSHOT.jar/!BOOT-INF/libs/tomcat-embed-core-11.0.10.jar!/org/apache/catalina/Service.class");
        URL url2 = new URL("jar:nested:C:/Users/andre/IdeaProjects/embedded_tomcat/target/embedded_tomcat-1.0-SNAPSHOT.jar/!BOOT-INF/libs/tomcat-embed-core-11.0.10.jar!/org/apache/catalina/Service.class");
        System.out.println(url1);
        System.out.println(url2);
        System.out.println(url1.sameFile(url2));
        try {
            URL url3 = new URL("jar:C:/Users/andre/IdeaProjects/embedded_tomcat/target/embedded_tomcat-1.0-SNAPSHOT.jar/!BOOT-INF/libs/tomcat-embed-core-11.0.10.jar!/org/apache/catalina/Service.class");
        } catch (MalformedURLException e) {
            System.out.println(e);
        }
    }

    private String getStartClass() throws Exception {
        return Optional.ofNullable(archive.getManifest())
                .map(Manifest::getMainAttributes)
                .map(mainAttrs -> mainAttrs.getValue(START_CLASS))
                .orElseThrow(() -> new IllegalStateException("Start-Class not defined"));
    }
}
