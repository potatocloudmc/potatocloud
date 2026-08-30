package net.potatocloud.node.utils;

import oshi.ffm.SystemInfo;

public final class SystemUtils {

    public static final SystemInfo SYSTEM_INFO = new SystemInfo();

    private SystemUtils() {
    }

    public static int cpuCores() {
        return SYSTEM_INFO.getHardware().getProcessor().getPhysicalProcessorCount();
    }

    public static int ram() {
        return (int) (SYSTEM_INFO.getHardware().getMemory().getTotal() / (1024L * 1024 * 1024));
    }

    public static boolean lowHardware() {
        return cpuCores() < 4 || ram() < 4;
    }
}
