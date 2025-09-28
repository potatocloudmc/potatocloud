package net.potatocloud.node.command.commands;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.utils.TimeFormatter;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.console.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

@RequiredArgsConstructor
@CommandInfo(name = "info", description = "Shows system and node info", aliases = {"me"})
public class InfoCommand extends Command {

    private final Logger logger;

    @Override
    public void execute(String[] args) {
        final SystemInfo info = new SystemInfo();
        final GlobalMemory memory = info.getHardware().getMemory();
        final CentralProcessor processor = info.getHardware().getProcessor();

        logger.info("OS&8: &a" + System.getProperty("os.name") + " &8(&a" + System.getProperty("os.version") + "&8, &a" + System.getProperty("os.arch") + "&8)");
        logger.info("User&8: &a" + System.getProperty("user.name"));
        logger.info("Java version&8: &a" + System.getProperty("java.version") + " &8(&a" + System.getProperty("java.vendor") + "&8)");
        logger.info("Uptime&8: &a" + TimeFormatter.formatAsDuration(Node.getInstance().getUptime()));
        logger.info("Started At&8: &a" + TimeFormatter.formatAsDateAndTime(Node.getInstance().getStartupTime()));

        final double totalMemory = memory.getTotal() / (1024.0 * 1024 * 1024);
        final double availableMemory = memory.getAvailable() / (1024.0 * 1024 * 1024);
        final double usedMemory = totalMemory - availableMemory;

        logger.info("System Memory&8: &a" + String.format("%.2f", usedMemory) +
                " GB &8/ &a" + String.format("%.2f", totalMemory) + " GB");

        final String cpuName = processor.getProcessorIdentifier().getName();
        final int cores = processor.getPhysicalProcessorCount();
        final int threads = processor.getLogicalProcessorCount();

        logger.info("CPU&8: &a" + cpuName +
                " &8(&a" + cores + " cores&8, &a" + threads + " threads&8)");
    }
}
