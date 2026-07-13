package net.potatocloud.node.version;

public final class VersionUtil {

    private VersionUtil() {
    }

    public static int compare(String first, String second) {
        return versionKey(first).compareTo(versionKey(second));
    }

    private static String versionKey(String version) {
        final String[] split = version.split("[-+]")[0].split("\\.");

        return String.format(
                "%05d.%05d.%05d",
                split.length > 0 ? Integer.parseInt(split[0]) : 0,
                split.length > 1 ? Integer.parseInt(split[1]) : 0,
                split.length > 2 ? Integer.parseInt(split[2]) : 0
        );
    }
}
