package net.potatocloud.node.setup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.potatocloud.node.Node;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.answer.AnswerResult;
import net.potatocloud.node.setup.question.Question;
import net.potatocloud.node.setup.question.QuestionBuilder;
import net.potatocloud.node.setup.question.QuestionType;

import java.util.*;

@Getter
@RequiredArgsConstructor
public abstract class Setup {

    private final Console console;
    private final ScreenManager screenManager;

    private final List<Question> questions = new ArrayList<>();
    protected final Map<String, String> answers = new HashMap<>();

    private int currentIndex = 0;
    private boolean inSummary = false;
    private String lastErrorMessage;
    private Screen questionScreen;
    private Screen summaryScreen;

    public abstract String getName();

    public abstract void initQuestions();

    protected abstract void finish(Map<String, String> answers);

    protected QuestionBuilder text(String name, String prompt) {
        return new QuestionBuilder(questions, name, prompt, QuestionType.TEXT);
    }

    protected QuestionBuilder bool(String name, String prompt) {
        return new QuestionBuilder(questions, name, prompt, QuestionType.BOOLEAN);
    }

    protected QuestionBuilder number(String name, String prompt) {
        return new QuestionBuilder(questions, name, prompt, QuestionType.NUMBER);
    }

    public void start() {
        initQuestions();
        if (questions.isEmpty()) {
            finish(answers);
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
            cleanup();
            setupManager.endSetup();
            logger.info("Setup &a" + getName() + " &7was cancelled");
            return;
        }

        if (inSummary) {
            if (input.equalsIgnoreCase("back")) {
                inSummary = false;
                currentIndex = getPreviousIndex(questions.size());
                showQuestion();
                return;
            }

            if (input.equalsIgnoreCase("confirm")) {
                finish(Collections.unmodifiableMap(answers));
                cleanup();
                setupManager.endSetup();
                logger.info("Setup &a" + getName() + " &7was completed successfully");
                return;
            }

            showSummary();
            return;
        }

        if (input.equalsIgnoreCase("back")) {
            if (currentIndex == 0) {
                lastErrorMessage = "You are already at the first question";
            } else {
                currentIndex = getPreviousIndex(currentIndex);
            }
            showQuestion();
            return;
        }

        if (currentIndex >= questions.size()) {
            return;
        }

        final Question question = questions.get(currentIndex);
        String answer = input;

        if (question.getType() == QuestionType.BOOLEAN) {
            if (answer.equalsIgnoreCase("yes")) {
                answer = "true";
            }
            if (answer.equalsIgnoreCase("no")) {
                answer = "false";
            }
        }

        if (input.isEmpty()) {
            final String def = question.getDefaultAnswer();
            if (def != null && !def.isBlank()) {
                answer = def;
            } else {
                lastErrorMessage = "The input cannot be blank";
                showQuestion();
                return;
            }
        }

        final AnswerResult result = question.validate(answer);
        if (!result.isSuccess()) {
            lastErrorMessage = result.getErrorMessage();
            showQuestion();
            return;
        }

        answers.put(question.getName(), answer);
        lastErrorMessage = null;

        if (question.getAnswerAction() != null) {
            question.getAnswerAction().execute(answers, answer);
        }

        currentIndex++;

        while (currentIndex < questions.size() && questions.get(currentIndex).shouldSkip(answers)) {
            currentIndex++;
        }

        if (currentIndex >= questions.size()) {
            showSummary();
        } else {
            showQuestion();
        }
    }

    private void showQuestion() {
        final Question question = questions.get(currentIndex);
        final String screenName = "setup_" + getName().toLowerCase();

        questionScreen = new Screen(screenName);
        screenManager.register(questionScreen);
        screenManager.switchTo(screenName, false);

        console.setPrompt("> ");
        console.println("&7Setup: &a" + getName() + " &8(&7Question &a" + (currentIndex + 1) + "&8/&a" + questions.size() + "&8)");
        console.println(" ");
        console.println("&8» &7" + question.getPrompt());

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
        if (question.getSuggestions() != null && !question.getSuggestions().isEmpty()) {
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
        screenManager.register(summaryScreen);
        screenManager.switchTo(screenName, false);

        console.setPrompt("> ");
        console.println("&7Setup: &a" + getName() + " &8(&7Summary&8)");
        console.println("");

        for (Question question : questions) {
            if (question.shouldSkip(answers)) {
                continue;
            }

            final String answer = answers.getOrDefault(question.getName(), "<no answer>");
            console.println("&8» &7" + question.getPrompt());
            console.println("  &8• &7Answer: &a" + answer);
            console.println(" ");
        }

        console.println("");
        console.println("Type &aconfirm&7 to complete the setup, &aback&7 to return to the questions, or &ccancel&7 to exit the setup");
    }

    private int getPreviousIndex(int fromIndex) {
        for (int i = fromIndex - 1; i >= 0; i--) {
            if (!questions.get(i).shouldSkip(answers)) {
                return i;
            }
        }
        return 0;
    }

    private void cleanup() {
        screenManager.switchTo(Screen.NODE_SCREEN);
        screenManager.unregister(questionScreen.name());
        screenManager.unregister(summaryScreen.name());
    }
}