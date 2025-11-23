package net.potatocloud.node.command;

import lombok.experimental.UtilityClass;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.Node;

import java.util.List;

@UtilityClass
public class TabCompleters {

    public List<String> group(String[] args, int startsWith) {
        return Node.getInstance().getServiceGroupManager().getAllServiceGroups().stream()
                .map(ServiceGroup::getName)
                .filter(name -> name.startsWith(args[startsWith]))
                .toList();
    }

    public List<String> group(String[] args) {
        return group(args, 0);
    }

    public List<String> service(String[] args, int startsWith) {
        return Node.getInstance().getServiceManager().getAllServices().stream()
                .map(Service::getName)
                .filter(name -> name.startsWith(args[startsWith]))
                .toList();
    }

    public List<String> service(String[] args) {
        return service(args, 0);
    }

    public List<String> platform(String[] args, int startsWith) {
        return Node.getInstance().getPlatformManager().getPlatforms().stream()
                .map(Platform::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
    }

    public List<String> platform(String[] args) {
        return platform(args, 0);
    }

    public List<String> platformVersion(Platform platform, String[] args, int startsWith) {
        if (platform == null) {
            return List.of();
        }
        return platform.getVersions().stream()
                .map(PlatformVersion::getName)
                .filter(ver -> ver.toLowerCase().startsWith(args[1].toLowerCase()))
                .toList();
    }

    public List<String> platformVersion(Platform platform, String[] args) {
        return platformVersion(platform, args, 0);
    }
}

