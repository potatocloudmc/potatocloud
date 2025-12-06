package net.potatocloud.core.networking;

public class PacketIds {

    public static final int SERVICE_ADD = 0;
    public static final int SERVICE_REMOVE = 1;
    public static final int SERVICE_UPDATE = 2;
    public static final int SERVICE_STARTED = 3;
    public static final int REQUEST_SERVICES = 4;
    public static final int START_SERVICE = 5;
    public static final int STOP_SERVICE = 6;
    public static final int SERVICE_EXECUTE_COMMAND = 7;
    public static final int SERVICE_COPY = 8;
    public static final int SERVICE_MEMORY_UPDATE = 9;

    public static final int REQUEST_GROUPS = 100;
    public static final int GROUP_ADD = 101;
    public static final int GROUP_UPDATE = 102;
    public static final int GROUP_DELETE = 104;

    public static final int PLAYER_ADD = 200;
    public static final int PLAYER_REMOVE = 201;
    public static final int PLAYER_UPDATE = 202;
    public static final int PLAYER_CONNECT = 203;
    public static final int REQUEST_PLAYERS = 204;

    public static final int EVENT = 300;

    public static final int PLATFORM_ADD = 400;
    public static final int PLATFORM_REMOVE = 401;
    public static final int REQUEST_PLATFORMS = 402;
    public static final int PLATFORM_UPDATE = 403;

    public static final int REQUEST_PROPERTIES = 500;
    public static final int PROPERTY_UPDATE = 501;
    public static final int PROPERTY_REMOVE = 502;
    public static final int PROPERTY_ADD = 503;

}
