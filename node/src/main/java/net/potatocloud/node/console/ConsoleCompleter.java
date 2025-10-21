package net.potatocloud.node.console;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ConsoleCompleter implements Completer {

    private final CommandManager commandManager;

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        final ScreenManager screenManager = Node.getInstance().getScreenManager();
        final Screen currentScreen = screenManager.getCurrentScreen();

        // add leave and exit options for all screens except node and setup screens
        if (currentScreen != null && !currentScreen.getName().equals(Screen.NODE_SCREEN) && !currentScreen.getName().startsWith("setup_")) {
            candidates.add(new Candidate("leave"));
            candidates.add(new Candidate("exit"));
            return;
        }

        final Setup currentSetup = Node.getInstance().getSetupManager().getCurrentSetup();
        if (currentSetup != null) {
            if (currentSetup.isInSummary()) {
                candidates.add(new Candidate("back"));
                candidates.add(new Candidate("confirm"));
                candidates.add(new Candidate("cancel"));
            } else {
                candidates.add(new Candidate("back"));
                candidates.add(new Candidate("cancel"));

                final List<String> possibleChoices = currentSetup.getQuestions().get(currentSetup.getCurrentIndex()).getPossibleChoices(currentSetup.getAnswers());
                if (possibleChoices != null) {
                    for (String possibleChoice : possibleChoices) {
                        candidates.add(new Candidate(possibleChoice));
                    }
                }
            }
            return;
        }

        final List<String> words = line.words();
        final String currentWord = line.word();

        if (line.wordIndex() == 0) {
            for (String cmd : commandManager.getAllCommandNames()) {
                if (cmd.startsWith(currentWord)) {
                    candidates.add(new Candidate(cmd));
                }
            }
        } else {
            final String commandName = words.getFirst();
            final Command command = commandManager.getCommand(commandName);
            if (command == null) {
                return;
            }

            final String[] args = words.subList(1, words.size()).toArray(new String[0]);

            if (!command.getSubCommands().isEmpty() && args.length > 0) {
                final SubCommand subCommand = command.getSubCommand(args[0]);

                if (subCommand == null) {
                    for (SubCommand sub : command.getSubCommands()) {
                        if (sub.getName().startsWith(args[0])) {
                            candidates.add(new Candidate(sub.getName()));
                        }
                    }
                    return;
                }

                if (subCommand instanceof TabCompleter completer) {
                    for (String suggestion : completer.complete(Arrays.copyOfRange(args, 1, args.length))) {
                        if (suggestion.startsWith(currentWord)) {
                            candidates.add(new Candidate(suggestion));
                        }
                    }
                }
                return;
            }

            if (command instanceof TabCompleter completer) {
                for (String suggestion : completer.complete(args)) {
                    if (suggestion.startsWith(currentWord)) {
                        candidates.add(new Candidate(suggestion));
                    }
                }
            }

        }
    }
}
