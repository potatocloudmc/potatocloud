package net.potatocloud.node.command.commands;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.api.utils.TimeFormatter;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.command.ArgumentType;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.ServiceManagerImpl;

import java.util.ArrayList;
import java.util.List;


@CommandInfo(name = "service", description = "Manage services", aliases = {"ser", "serv", "s"})
public class ServiceCommand extends Command {

    public ServiceCommand(Logger logger, ServiceManagerImpl serviceManager, ScreenManager screenManager, ClusterManagerImpl clusterManager) {
        defaultExecutor(_ -> sendHelp());

        sub("copy", "Copy files from a service to a template")
                .argument(ArgumentType.Service("service"))
                .argument(ArgumentType.String("template"))
                .optionalArgument(ArgumentType.String("filter"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("service") || argsLength != 1) {
                        return List.of();
                    }

                    final Service service = ctx.get("service");
                    if (service == null) {
                        return List.of();
                    }

                    return service.group()
                            .templates()
                            .stream()
                            .filter(name -> name.startsWith(input))
                            .toList();

                })
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    final String template = ctx.get("template");
                    final String filter = ctx.has("filter") ? ctx.get("filter") : "";

                    if (filter.isEmpty()) {
                        serviceManager.copyTo(service, template);
                    } else {
                        serviceManager.copyTo(service, template, filter);
                    }

                    logger.info("Copied &a" + (filter.isEmpty() ? "all service files" : filter) + " &7to template: &a" + template);
                });

        sub("edit", "Edit a service")
                .argument(ArgumentType.Service("service"))
                .argument(ArgumentType.String("key"))
                .argument(ArgumentType.String("value"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("service") || argsLength != 1) {
                        return List.of();
                    }

                    final List<String> suggestions = List.of(
                            "maxPlayers"
                    );

                    return suggestions.stream()
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    String key = ctx.get("key");
                    final String value = ctx.get("value");

                    key = key.toLowerCase();

                    try {
                        if (key.equals("maxplayers")) {
                            service.maxPlayers(Integer.parseInt(value));
                        } else {
                            sendHelp();
                        }
                    } catch (Exception e) {
                        logger.info("&cInvalid value &a" + value + " &cfor key &a" + key);
                        return;
                    }

                    serviceManager.update(service);
                    logger.info("Updated &a" + key + " &7for service &a" + service.name() + "&7 to &a" + value);
                });

        sub("execute", "Execute a command on a sevice")
                .argument(ArgumentType.Service("service"))
                .argument(ArgumentType.MultiString("command"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    final String command = ctx.get("command");

                    if (!service.running()) {
                        logger.info("Service &a" + service.name() + " &7is &coffline");
                        return;
                    }

                    serviceManager.execute(service, command);
                    logger.info("Executed command &a" + command + " &7on service &a" + service.name());
                });

        sub("info", "Show details of a service")
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");

                    logger.info("&7Info for service &a" + service.name() + "&8:");
                    logger.info("&8» &7Group: &a" + service.group().name());
                    logger.info("&8» &7Port: &a" + service.port());
                    logger.info("&8» &7State: &a" + service.state());
                    logger.info("&8» &7Online Players: &a" + service.playerCount());
                    logger.info("&8» &7Max Players: &a" + service.maxPlayers());
                    logger.info("&8» &7Memory usage: &a" + service.usedMemory() + "MB");
                    logger.info("&8» &7Online Time: &a" + TimeFormatter.formatAsDuration(service.uptime().toMillis()));
                    logger.info("&8» &7Started At: &a" + TimeFormatter.formatAsDateAndTime(service.startedAt().toEpochMilli()));
                });

        sub("list", "List all services")
                .executes(_ -> {
                    final List<Service> services = serviceManager.services();

                    if (services.isEmpty()) {
                        logger.info("There are &cno &7services");
                        return;
                    }

                    logger.info("All services&8:");
                    for (Service service : services) {
                        logger.info("&8» &a" + service.name() + " &7- Group: &a" + service.group().name() + " &7- State: &a" + service.state());
                    }
                });

        final SubCommand propertySub = sub("property", "Manage properties of a service");

        propertySub.executes(_ -> propertySub.sendHelp());

        propertySub.sub("set")
                .argument(ArgumentType.Service("service"))
                .argument(ArgumentType.String("key"))
                .argument(ArgumentType.String("value"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("service") || argsLength != 1) {
                        return List.of();
                    }

                    final List<String> suggestions = new ArrayList<>();

                    for (Property<?> property : DefaultProperties.asSet()) {
                        suggestions.add(property.name());
                    }

                    suggestions.add("<custom>");

                    return suggestions.stream()
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    final String key = ctx.get("key");
                    final String value = ctx.get("value");

                    try {
                        final Property<?> property = PropertyUtil.stringToProperty(key, value);

                        service.set(property);
                        serviceManager.update(service);
                        logger.info("Property &a" + key + " &7was set to &a" + value + " &7in service &a" + service.name());
                    } catch (Exception e) {
                        propertySub.sendHelp();
                    }
                });

        propertySub.sub("remove")
                .argument(ArgumentType.Service("service"))
                .argument(ArgumentType.String("key"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("service") || argsLength != 1) {
                        return List.of();
                    }

                    final Service service = ctx.get("service");

                    return service.properties().stream()
                            .map(Property::name)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    final String key = ctx.get("key");

                    final Property<?> property = service.property(key);
                    if (property == null) {
                        logger.info("Property &a" + key + "&7 was &cnot found &7in service &a" + service.name());
                        return;
                    }

                    service.propertyMap().remove(property.name());
                    serviceManager.update(service);
                    logger.info("Property &a" + key + " &7was removed in service &a" + service.name());
                });

        propertySub.sub("list")
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    final List<Property<?>> properties = service.properties();

                    if (properties.isEmpty()) {
                        logger.info("No properties found for service &a" + service.name());
                        return;
                    }

                    logger.info("Properties of service &a" + service.name() + "&8:");
                    for (Property<?> property : properties) {
                        logger.info("&8» &a" + property.name() + " &7- " + property.value());
                    }
                });

        sub("screen", "Switch to a service screen")
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");

                    screenManager.open(service.name());
                });

        sub("start", "Start new services")
                .argument(ArgumentType.Group("group"))
                .optionalArgument(ArgumentType.Integer("amount"))
                .executes(ctx -> {
                    final Group group = ctx.get("group");
                    final int amount = ctx.has("amount") ? ctx.get("amount") : 1;

                    int started = 0;
                    for (int i = 0; i < amount; i++) {
                        if (!serviceManager.hasEnoughMemory(group)) {
                            serviceManager.logMemoryWarning(group);
                            break;
                        }
                        serviceManager.start(group);
                        started++;
                    }

                    if (started > 0) {
                        logger.info("Starting " + started + " service" + (started == 1 ? "" : "s") + " in group &a" + group.name());
                    }
                });

        sub("stop", "Stop a service")
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");

                    final ServiceState state = service.state();
                    if (state == ServiceState.STOPPED || state == ServiceState.STOPPING) {
                        logger.info("Service &a" + service.name() + " &7is already &c" + state.name().toLowerCase());
                        return;
                    }

                    serviceManager.stop(service);
                });
    }
}
