package org.anku.autumn.launch;

public class JarLauncher extends Launcher {

    JarLauncher() throws Exception {
        super();
    }

    public static void main(String[] args) throws Exception {
        new JarLauncher().launch(args);
//        URI uri = new URI("jar:nested:C:/Users/andre/IdeaProjects/embedded_tomcat/target/embedded_tomcat-1.0-SNAPSHOT.jar/!BOOT-INF/libs/tomcat-embed-core-11.0.10.jar!/org/apache/catalina/Service.class");
//        System.out.println(uri.getPath());
    }
}
