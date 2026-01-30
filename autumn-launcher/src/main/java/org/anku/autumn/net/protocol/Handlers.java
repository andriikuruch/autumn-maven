package org.anku.autumn.net.protocol;

public class Handlers {
    private static final String PROTOCOL_PKGS_PROPERTY = "java.protocol.handler.pkgs";
    public static final String PACKAGES = Handlers.class.getPackageName();

    private Handlers() {
        // util class
    }

    public static void register() {
        String packages = System.getProperty(PROTOCOL_PKGS_PROPERTY);
        if (packages == null || packages.isEmpty()) {
            System.setProperty(PROTOCOL_PKGS_PROPERTY, PACKAGES);
        } else {
            System.setProperty(PROTOCOL_PKGS_PROPERTY, packages + "|" + PACKAGES);
        }
    }
}
