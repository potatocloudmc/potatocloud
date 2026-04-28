package net.potatocloud.common;

import java.io.File;

public final class DockerFile {
    private static final boolean isInDocker = !(new File(".server").isDirectory());

    public static File fileFromServerDir(final String pathname) {
        return new File(isInDocker ? pathname : ".server/" + pathname);
    }

    public static String stringFromServerDir(final String pathname) {
        return isInDocker ? pathname : ".server/" + pathname;
    }

}