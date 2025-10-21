package net.potatocloud.node.setup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.potatocloud.node.Node;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.validator.BooleanValidator;

import java.util.*;

@Getter
@RequiredArgsConstructor
public abstract class Setup {

    private final Console console;
    private final ScreenManager screenManager;

    private final List<SetupQuestion> questions = new ArrayList<>();
    protected final Map<String, String> answers = new HashMap<>();
    private int currentIndex = 0;
    private boolean inSummary = false;
    private String lastErrorMessage = null;

    private Screen questionScreen;
    private Screen summaryScreen;

    public abstract String getName();

    public abstract void initQuestions();

    protected abstract void onFinish(Map<String, String> answers);


    public SetupQuestionBuilder question(String name) {
        return new SetupQuestionBuilder(name, this);
    }

    public void start() {
        initQuestions();
        if (questions.isEmpty()) {
            onFinish(answers);
            return;
        }
        showQuestion();
    }

    public void handleInput(String input) {
        input = input.strip();

        final Node node = Node.getInstance();
        final SetupManager setupManager = node.getSetupManager();
        final Logger logger = node.getLogger();

        if (input.equalsIgnoreCase("cancel")) {
            screenManager.switchScreen(Screen.NODE_SCREEN);
            screenManager.removeScreen(questionScreen);
            screenManager.removeScreen(summaryScreen);

            setupManager.endSetup();
            logger.info("Setup &a" + this.getName() + " &7was cancelled");
            return;
        }

        if (inSummary) {
            if (input.equalsIgnoreCase("back")) {
                inSummary = false;
                currentIndex = questions.size() - 1;
                showQuestion();
                return;
            }

            if (input.equalsIgnoreCase("confirm")) {
                // end setup
                screenManager.switchScreen(Screen.NODE_SCREEN);
                onFinish(Collections.unmodifiableMap(answers));

                screenManager.removeScreen(questionScreen);
                screenManager.removeScreen(summaryScreen);

                setupManager.endSetup();
                logger.info("Setup &a" + this.getName() + " &7was completed successfully");

                return;
            }

            showSummary();
            return;
        }

        // when in a question
        if (input.equalsIgnoreCase("back")) {
            if (currentIndex == 0) {
                lastErrorMessage = "You are already at the first question";
            } else {
                currentIndex--;
            }
            showQuestion();
            return;
        }

        if (currentIndex >= questions.size()) {
            return;
        }

        final SetupQuestion question = questions.get(currentIndex);
        String answer = input;

        //only replace yes and no to booleans in a question that expects a boolean
        if (question.getValidator() != null && question.getValidator() instanceof BooleanValidator) {
            if (answer.equalsIgnoreCase("yes")) {
                answer = "true";
            }
            if (answer.equalsIgnoreCase("no")) {
                answer = "false";
            }
        }

        final String defaultAnswer = question.getDefaultAnswer();
        if (input.isEmpty()) {
            if (defaultAnswer != null && !defaultAnswer.isBlank()) {
                // if the user just pressed enter use default answer if possible
                answer = defaultAnswer;
            } else {
                lastErrorMessage = "The input cannot be blank";
                showQuestion();
                return;
            }
        }

        if (question.getValidator() != null) {
            final SetupAnswerResult result = question.getValidator().validate(answer);
            if (!result.isSuccess()) {
                lastErrorMessage = result.getErrorMessage();
                showQuestion();
                return;
            }
        }

        answers.put(question.getName(), answer);
        lastErrorMessage = null;
        currentIndex++;

        // show the next question as long there are some left
        if (currentIndex < questions.size()) {
            showQuestion();
            return;
        }

        // if no more questions are left show summary page
        showSummary();
    }

    private void showQuestion() {
        // skip all questions that should be skipped
        while (currentIndex < questions.size() && questions.get(currentIndex).shouldSkip(answers)) {
            currentIndex++;
        }

        if (currentIndex >= questions.size()) {
            showSummary();
            return;
        }

        final SetupQuestion question = questions.get(currentIndex);
        final String screenName = "setup_" + getName().toLowerCase();
        questionScreen = new Screen(screenName);
        screenManager.addScreen(questionScreen);
        screenManager.switchScreen(screenName, false);

        console.setPrompt("> ");
        console.println("&7Setup: &a" + getName() + " &8(&7Question &a" + (currentIndex + 1) + "&8/&a" + questions.size() + "&8)");
        console.println(" ");
        console.println("&8» &7" + question.getQuestion());

        if (question.getDefaultAnswer() != null && !question.getDefaultAnswer().isBlank()) {
            console.println("  &8• &7Default&8: &a" + question.getDefaultAnswer());
        }

        final String previousAnswer = answers.get(question.getName());
        if (previousAnswer != null && !previousAnswer.isBlank()) {
            console.println("  &8• &7Previous&8: &a" + previousAnswer);
        }

        final List<String> commandsText = new ArrayList<>();
        if (question.getDefaultAnswer() != null && !question.getDefaultAnswer().isBlank()) {
            commandsText.add("&aEnter&7 = default");
        }

        if (question.getPossibleChoices(answers) != null && !question.getPossibleChoices(answers).isEmpty()) {
            commandsText.add("&aTab&7 = show options");
        }

        if (currentIndex != 0) {
            commandsText.add("&aBack&7 = previous question");
        }

        commandsText.add("&aCancel&7 = exit the setup");

        console.println(" ");
        console.println("&7Commands&8: &7" + String.join(" &8| &7", commandsText));

        console.println(" ");

        if (lastErrorMessage != null) {
            console.println("&c" + lastErrorMessage);
        }
    }

    private void showSummary() {
        inSummary = true;

        final String screenName = "setup_" + getName().toLowerCase() + "_summary";
        summaryScreen = new Screen(screenName);
        screenManager.addScreen(summaryScreen);

        screenManager.switchScreen(screenName, false);

        console.setPrompt("> ");
        console.println("&7Setup: &a" + getName() + " &8(&7Summary&8)");
        console.println("");

        for (final SetupQuestion question : questions) {
            final String answer = answers.getOrDefault(question.getName(), "<no answer>");

            console.println("&8» &7" + question.getQuestion());
            console.println("  &8• &7Answer: &a" + answer);
            console.println(" ");
        }

        console.println("");
        console.println("Type &aconfirm&7 to complete the setup, &aback&7 to return to the questions, or &ccancel&7 to exit the setup");
    }
}
