package net.potatocloud.node.console;

import lombok.RequiredArgsConstructor;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.*;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

@RequiredArgsConstructor
public class ConsoleCompleter implements Completer {

    private final CommandManager commandManager;

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        final ScreenManager screenManager = Node.getInstance().getScreenManager();
        final Screen currentScreen = screenManager.getCurrentScreen();

        // Add leave and exit options for all screens except node and setup screens
        if (currentScreen != null && !currentScreen.name().equals(Screen.NODE_SCREEN) && !currentScreen.name().startsWith("setup_")) {
            candidates.add(new Candidate("leave"));
            candidates.add(new Candidate("exit"));
            return;
        }

        // Show setup options when user is inside a setup
        final Setup currentSetup = Node.getInstance().getSetupManager().getCurrentSetup();
        if (currentSetup != null) {
            if (currentSetup.isInSummary()) {
                // Options for summary page
                candidates.add(new Candidate("back"));
                candidates.add(new Candidate("confirm"));
                candidates.add(new Candidate("cancel"));
            } else {
                // Options while in a questions
                candidates.add(new Candidate("back"));
                candidates.add(new Candidate("cancel"));

                final List<String> possibleChoices = currentSetup.getQuestions().get(currentSetup.getCurrentIndex()).getSuggestions();
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
            return;
        }

        final Command command = commandManager.getCommand(words.getFirst());
        if (command == null) {
            return;
        }

        SubCommand currentSubCommand = null;
        List<SubCommand> currentLevel = command.getSubCommands();
        int argumentIndex = 1;

        while (argumentIndex < words.size() - 1) {
            final String token = words.get(argumentIndex);

            final SubCommand subCommand = currentLevel.stream()
                    .filter(sub -> sub.getName().equalsIgnoreCase(token))
                    .findFirst()
                    .orElse(null);

            if (subCommand == null) {
                break;
            }

            currentSubCommand = subCommand;
            currentLevel = subCommand.getSubCommands();
            argumentIndex++;
        }

        if (currentSubCommand == null) {
            for (SubCommand subCommand : currentLevel) {
                if (subCommand.getName().startsWith(currentWord)) {
                    candidates.add(new Candidate(subCommand.getName()));
                }
            }
            return;
        }

        final String[] argsToParse = words.subList(0, line.wordIndex()).toArray(new String[0]);
        final CommandContext.ParseResult parseResult =
                currentSubCommand.buildContext(argsToParse, argumentIndex);

        final CommandContext context = parseResult.getContext();

        int expectedArgs = currentSubCommand.getArguments().size();
        int parsedArgs = parseResult.getParsedArguments();

        if (parsedArgs >= expectedArgs && currentSubCommand.getSubCommands().isEmpty()) {
            return;
        }

        final int argsLength = parseResult.getParsedArguments();
        final List<String> customSuggestions = currentSubCommand.suggest(context, currentWord, argsLength);
        if (!customSuggestions.isEmpty()) {
            for (String suggestion : customSuggestions) {
                candidates.add(new Candidate(suggestion));
            }
            return;
        }

        if (parsedArgs < expectedArgs) {
            final ArgumentType<?> argumentType = currentSubCommand.getArguments().get(parsedArgs);
            final List<String> argumentSuggestions = argumentType.suggest(currentWord);
            if (!argumentSuggestions.isEmpty()) {
                for (String suggestion : argumentSuggestions) {
                    candidates.add(new Candidate(suggestion));
                }
            }
        }

        if (!currentSubCommand.getSubCommands().isEmpty()
                && currentSubCommand.getArguments().isEmpty()) {

            for (SubCommand sub : currentSubCommand.getSubCommands()) {
                if (sub.getName().startsWith(currentWord)) {
                    candidates.add(new Candidate(sub.getName()));
                }
            }
        }
    }
}
