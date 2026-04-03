package net.potatocloud.node.command.commands;

import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.node.command.ArgumentType;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.ServiceImpl;

import java.util.ArrayList;
import java.util.List;


@CommandInfo(name = "service", description = "Manage services", aliases = {"ser", "serv", "s"})
public class ServiceCommand extends Command {

    public ServiceCommand(Logger logger, ServiceManager serviceManager, ScreenManager screenManager) {
        defaultExecutor(ctx -> sendHelp());

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

                    return service.getServiceGroup()
                            .getServiceTemplates()
                            .stream()
                            .filter(name -> name.startsWith(input))
                            .toList();

                })
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    final String template = ctx.get("template");
                    final String filter = ctx.has("filter") ? ctx.get("filter") : "";

                    if (filter.isEmpty()) {
                        service.copy(template);
                    } else {
                        service.copy(template, filter);
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
                            service.setMaxPlayers(Integer.parseInt(value));
                        } else {
                            sendHelp();
                        }
                    } catch (Exception e) {
                        logger.info("&cInvalid value &a" + value + " &cfor key &a" + key);
                        return;
                    }

                    service.update();
                    logger.info("Updated &a" + key + " &7for service &a" + service.getName() + "&7 to &a" + value);
                });

        sub("execute", "Execute a command on a sevice")
                .argument(ArgumentType.Service("service"))
                .argument(ArgumentType.MultiString("command"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    final String command = ctx.get("command");

                    if (!service.isOnline()) {
                        logger.info("Service &a" + service.getName() + " &7is &coffline");
                        return;
                    }

                    if (service.executeCommand(command)) {
                        logger.info("Executed command &a" + command + " &7on service &a" + service.getName());
                    } else {
                        logger.info("&cFailed to execute command &a" + command + " &con service &a" + service.getName());
                    }
                });
        sub("info", "Show details of a service")
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");

                    logger.info("&7Info for service &a" + service.getName() + "&8:");
                    logger.info("&8» &7Group: &a" + service.getServiceGroup().getName());
                    logger.info("&8» &7Port: &a" + service.getPort());
                    logger.info("&8» &7Status: &a" + service.getStatus());
                    logger.info("&8» &7Online Players: &a" + service.getOnlinePlayerCount());
                    logger.info("&8» &7Max Players: &a" + service.getMaxPlayers());
                    logger.info("&8» &7Memory usage: &a" + service.getUsedMemory() + "MB");
                    logger.info("&8» &7Online Time: &a" + service.getFormattedUptime());
                    logger.info("&8» &7Started At: &a" + service.getFormattedStartTimestamp());
                });

        sub("list", "List all services")
                .executes(ctx -> {
                    final List<Service> services = serviceManager.getAllServices();

                    if (services.isEmpty()) {
                        logger.info("There are &cno &7services");
                        return;
                    }

                    logger.info("All services&8:");
                    for (Service service : services) {
                        logger.info("&8» &a" + service.getName() + " &7- Group: &a" + service.getServiceGroup().getName() + " &7- Status: &a" + service.getStatus());
                    }
                });

        final SubCommand propertySub = sub("property", "Manage properties of a service");

        propertySub.executes(ctx -> propertySub.sendHelp());

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
                        suggestions.add(property.getName());
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

                        service.setProperty(property);
                        service.update();
                        logger.info("Property &a" + key + " &7was set to &a" + value + " &7in service &a" + service.getName());
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

                    return service.getProperties().stream()
                            .map(Property::getName)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    final String key = ctx.get("key");

                    final Property<?> property = service.getProperty(key);
                    if (property == null) {
                        logger.info("Property &a" + key + "&7 was &cnot found &7in service &a" + service.getName());
                        return;
                    }

                    service.getPropertyMap().remove(property.getName());
                    service.update();
                    logger.info("Property &a" + key + " &7was removed in service &a" + service.getName());
                });

        propertySub.sub("list")
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");
                    final List<Property<?>> properties = service.getProperties();

                    if (properties.isEmpty()) {
                        logger.info("No properties found for service &a" + service.getName());
                        return;
                    }

                    logger.info("Properties of service &a" + service.getName() + "&8:");
                    for (Property<?> property : properties) {
                        logger.info("&8» &a" + property.getName() + " &7- " + property.getValue());
                    }
                });

        sub("screen", "Switch to a service screen")
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");

                    if (service instanceof ServiceImpl serviceImpl) {
                        final Screen screen = serviceImpl.getScreen();
                        if (screen == null) {
                            logger.error("&cFailed to switch to screen of service " + service.getName());
                            return;
                        }

                        screenManager.switchTo(screen.name());
                    }
                });

        sub("start", "Start new services")
                .argument(ArgumentType.Group("group"))
                .optionalArgument(ArgumentType.Integer("amount"))
                .executes(ctx -> {
                    final ServiceGroup group = ctx.get("group");
                    final int amount = ctx.has("amount") ? ctx.get("amount") : 1;

                    serviceManager.startServices(group, amount);
                    logger.info("Starting " + amount + " service" + (amount == 1 ? "" : "s") + " in group &a" + group.getName());
                });

        sub("stop", "Stop a service")
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final Service service = ctx.get("service");

                    service.shutdown();
                });
    }
}
