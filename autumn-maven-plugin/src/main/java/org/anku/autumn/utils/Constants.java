package org.anku.autumn.utils;

public final class Constants {

    private Constants() {
        // util class
    }

    public static final String REPACKAGER = "Repackager";
    public static final String MAVEN = "maven";
    public static final String LAUNCHER = "autumn-launcher";
    public static final String APACHE_COMPRESS = "commons-compress";
    public static final String LIBS = "libs";
    public static final String BOOT_INF = "BOOT-INF";
    public static final String META_INF = "META-INF";
    public static final String CLASSES = "classes";
    public static final String JAR_PROTOCOL = "jar:";
    public static final String JAR_EXTENSION = ".jar";
    public static final String ORIGINAL_EXTENSION = ".original";
    public static final String ROOT = "/";
    public static final String POM_XML = "pom.xml";
    public static final String POM_PROPERTIES = "pom.properties";

    public static final class ManifestFile {

        private ManifestFile() {
            // util class
        }

        public static final String MANIFEST_MF = "MANIFEST.MF";
        public static final String MANIFEST_VERSION = "1.0";
        public static final String MAVEN_ARCHIVER = "maven-archiver";

        public static final class Attributes {

            private Attributes() {
                // util class
            }

            public static final String MANIFEST_VERSION = "Manifest-Version";
            public static final String BUILD_JDK_SPEC = "Build-Jdk-Spec";
            public static final String CREATED_BY = "Created-By";
            public static final String IMPLEMENTATION_TITLE = "Implementation-Title";
            public static final String IMPLEMENTATION_VERSION = "Implementation-Version";
            public static final String IMPLEMENTATION_VERSION_ID = "Implementation-Vendor-Id";
            public static final String MAIN_CLASS = "Main-Class";
            public static final String START_CLASS = "Start-Class";
        }
    }

    public static final class Properties {

        private Properties() {
            // util class
        }

        public static final String JAVA_SPEC_VERSION = "java.specification.version";
    }
}
