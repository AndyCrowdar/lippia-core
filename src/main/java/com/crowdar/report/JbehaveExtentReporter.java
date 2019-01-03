package com.crowdar.report;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.StoryReporter;
import org.openqa.selenium.WebDriver;

import com.crowdar.core.PropertyManager;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class JbehaveExtentReporter extends JbehaveReport {

    private static Method storyCancelled;
    private static Method storyNotAllowed;
    private static Method beforeStory;
    private static Method afterStory;
    private static Method narrative;
    private static Method lifecycle;
    private static Method scenarioNotAllowed;
    private static Method beforeScenario;
    private static Method scenarioMeta;
    private static Method afterScenario;
    private static Method givenStories;
    private static Method givenStoriesPaths;
    private static Method beforeExamples;
    private static Method example;
    private static Method afterExamples;
    private static Method beforeStep;
    private static Method successful;
    private static Method ignorable;
    private static Method pending;
    private static Method notPerformed;
    private static Method failed;
    private static Method failedOutcomes;
    private static Method dryRun;
    private static Method pendingMethods;
    private static Method restarted;
    private static Method restartedStory;

    // Extent Report variables
    private ExtentReports extent;
    private ExtentTest logger;
    private ExtentTest childLogger;

    // JBehave status handlers
    private Throwable failedReason;

    private static final String SKIP_EXCEPTION = "TBD";
    private static final String SKIP_TITLE = "<b>===== SKIPPED TEST =====</b></br>";
    private static final String SKIP_PREFIX = "<b>The test was skipped because of the following reason:</b></br>&nbsp;&nbsp;- ";
    private boolean scenarioSkipped = false;
    private String skippedReason;

    static {
        try {
            storyCancelled = StoryReporter.class.getMethod("storyCancelled", Story.class, StoryDuration.class);
            storyNotAllowed = StoryReporter.class.getMethod("storyNotAllowed", Story.class, String.class);
            beforeStory = StoryReporter.class.getMethod("beforeStory", Story.class, Boolean.TYPE);
            afterStory = StoryReporter.class.getMethod("afterStory", Boolean.TYPE);
            narrative = StoryReporter.class.getMethod("narrative", Narrative.class);
            lifecycle = StoryReporter.class.getMethod("lifecyle", Lifecycle.class);
            scenarioNotAllowed = StoryReporter.class.getMethod("scenarioNotAllowed", Scenario.class, String.class);
            beforeScenario = StoryReporter.class.getMethod("beforeScenario", String.class);
            scenarioMeta = StoryReporter.class.getMethod("scenarioMeta", Meta.class);
            afterScenario = StoryReporter.class.getMethod("afterScenario");
            givenStories = StoryReporter.class.getMethod("givenStories", GivenStories.class);
            givenStoriesPaths = StoryReporter.class.getMethod("givenStories", List.class);
            beforeExamples = StoryReporter.class.getMethod("beforeExamples", List.class, ExamplesTable.class);
            example = StoryReporter.class.getMethod("example", Map.class);
            afterExamples = StoryReporter.class.getMethod("afterExamples");
            beforeStep = StoryReporter.class.getMethod("beforeStep", String.class);
            successful = StoryReporter.class.getMethod("successful", String.class);
            ignorable = StoryReporter.class.getMethod("ignorable", String.class);
            pending = StoryReporter.class.getMethod("pending", String.class);
            notPerformed = StoryReporter.class.getMethod("notPerformed", String.class);
            failed = StoryReporter.class.getMethod("failed", String.class, Throwable.class);
            failedOutcomes = StoryReporter.class.getMethod("failedOutcomes", String.class, OutcomesTable.class);
            dryRun = StoryReporter.class.getMethod("dryRun");
            pendingMethods = StoryReporter.class.getMethod("pendingMethods", List.class);
            restarted = StoryReporter.class.getMethod("restarted", String.class, Throwable.class);
            restartedStory = StoryReporter.class.getMethod("restartedStory", Story.class, Throwable.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DelayedMethod> delayedMethods = new ArrayList<DelayedMethod>();
    private final StoryReporter crossReferencing;
    private final StoryReporter delegate;
    private final boolean multiThreading;
    private boolean invoked = false;
    private WebDriver driverInstance;

    public JbehaveExtentReporter(WebDriver driverInstance, StoryReporter crossReferencing, StoryReporter delegate,
                                 boolean multiThreading) {
        this.driverInstance = driverInstance;
        this.crossReferencing = crossReferencing;
        this.multiThreading = multiThreading;
        this.delegate = delegate;
        String report = "target/" + PropertyManager.getProperty("crowdar.extent.report.name") + ".html";
        extent = new ExtentReports(report, false);
        extent.addSystemInfo("User Name", System.getProperty("user.name"));
        extent.addSystemInfo("Environment", "(In progress)");

        extent.loadConfig(new File(System.getProperty("user.dir").concat(File.separator).concat("extent-config.xml")));
    }

    @Override
    public void storyNotAllowed(Story story, String filter) {
        crossReferencing.storyNotAllowed(story, filter);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(storyNotAllowed, story, filter));
        } else {
            delegate.storyNotAllowed(story, filter);
        }
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        if (story.getName().equalsIgnoreCase("BeforeStories") || story.getName().equalsIgnoreCase("AfterStories")) {
            // TODO: Do something with beforeStories and AfterStories in the
            // future
        } else {
            logger = extent.startTest(story.getName());
        }
        crossReferencing.beforeStory(story, givenStory);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(beforeStory, story, givenStory));
        } else {
            delegate.beforeStory(story, givenStory);
        }
    }

    @Override
    public void afterStory(boolean givenStory) {
        extent.endTest(logger);
        extent.flush();
        extent.close();
        crossReferencing.afterStory(givenStory);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(afterStory, givenStory));
        } else {
            delegate.afterStory(givenStory);
        }
    }

    @Override
    public void narrative(Narrative aNarrative) {
        crossReferencing.narrative(aNarrative);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(narrative, aNarrative));
        } else {
            delegate.narrative(aNarrative);
        }
    }

    @Override
    public void lifecyle(Lifecycle aLifecycle) {
        crossReferencing.lifecyle(aLifecycle);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(lifecycle, aLifecycle));
        } else {
            delegate.lifecyle(aLifecycle);
        }
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        crossReferencing.scenarioNotAllowed(scenario, filter);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(scenarioNotAllowed, scenario, filter));
        } else {
            delegate.scenarioNotAllowed(scenario, filter);
        }
    }

    @Override
    public void beforeScenario(String scenarioTitle) {
        String[] sameScenarios = scenarioTitle.split("\\|");
        StringBuilder scenarioLabel = new StringBuilder();

        if (sameScenarios.length > 1) {
            scenarioLabel.append("Scenarios:");
            for (int i = 0; i < sameScenarios.length; i++) {
                scenarioLabel.append("</br>").append("&nbsp;&nbsp;").append("- ").append(sameScenarios[i]);
            }
        } else {
            scenarioLabel.append("Scenario:").append("</br>").append("&nbsp;&nbsp;").append("- ").append(scenarioTitle);
        }

        childLogger = extent.startTest(scenarioLabel.toString());
        logger.appendChild(childLogger);
        crossReferencing.beforeScenario(scenarioTitle);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(beforeScenario, scenarioTitle));
        } else {
            delegate.beforeScenario(scenarioTitle);
        }
    }

    @Override
    public void scenarioMeta(Meta meta) {
        crossReferencing.scenarioMeta(meta);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(scenarioMeta, meta));
        } else {
            delegate.scenarioMeta(meta);
        }
    }

    @Override
    public void afterScenario() {

        if (scenarioSkipped) {
            this.childLogger.log(LogStatus.SKIP, SKIP_TITLE + SKIP_PREFIX + this.skippedReason);
        }

        crossReferencing.afterScenario();
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(afterScenario));
        } else {
            delegate.afterScenario();
        }
    }

    @Override
    public void givenStories(GivenStories stories) {
        crossReferencing.givenStories(stories);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(givenStories, stories));
        } else {
            delegate.givenStories(stories);
        }
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        crossReferencing.givenStories(storyPaths);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(givenStoriesPaths, storyPaths));
        } else {
            delegate.givenStories(storyPaths);
        }
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        crossReferencing.beforeExamples(steps, table);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(beforeExamples, steps, table));
        } else {
            delegate.beforeExamples(steps, table);
        }
    }

    @Override
    public void example(Map<String, String> tableRow) {
        crossReferencing.example(tableRow);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(example, tableRow));
        } else {
            delegate.example(tableRow);
        }
    }

    @Override
    public void afterExamples() {
        crossReferencing.afterExamples();
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(afterExamples));
        } else {
            delegate.afterExamples();
        }
    }

    @Override
    public void beforeStep(String step) {
        crossReferencing.beforeStep(step);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(beforeStep, step));
        } else {
            delegate.beforeStep(step);
        }
    }

    private String replaceStepSymbols(String step) {
        return step.replace("｟", "[").replace("｠", "]");
    }

    private void logStep(String step, LogStatus status) {
        StringBuilder information = new StringBuilder();
        information.append("<big>");
        information.append(replaceStepSymbols(step));
        information.append("</big>");


        if (status.equals(LogStatus.FAIL)) {
            appendScreenshot(information);
            information.append(logFailedReason());
        }

        this.childLogger.log(status, information.toString());
    }

    private void appendScreenshot(StringBuilder information) {
        File screenshot = new File(ScreenshotCapture.getScreenCaptureFileName());
        if (screenshot.exists()) {
            information.append(this.childLogger.addScreenCapture(screenshot.getPath()));
        }
    }

    private Object logFailedReason() {
        StringBuilder information = new StringBuilder();
        information.append("<br>");
        information.append("<pre>");
        information.append(this.failedReason.toString()).append("\n");
        StackTraceElement[] stackArray = this.failedReason.getStackTrace();

        for (int i = 0; i < stackArray.length; ++i) {
            StackTraceElement element = stackArray[i];
            information.append(element.toString() + "\n");
        }
        information.append("</pre>");
        return information.toString();
    }

    @Override
    public void successful(String step) {
        logStep(step, LogStatus.PASS);
        crossReferencing.successful(step);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(successful, step));
        } else {
            delegate.successful(step);
        }

    }

    @Override
    public void ignorable(String step) {
        logStep(step, LogStatus.SKIP);
        crossReferencing.ignorable(step);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(ignorable, step));
        } else {
            delegate.ignorable(step);
        }
    }

    @Override
    public void pending(String step) {
        logStep(step, LogStatus.SKIP);
        crossReferencing.pending(step);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(pending, step));
        } else {
            delegate.pending(step);
        }
    }

    @Override
    public void notPerformed(String step) {
        logStep(step, LogStatus.SKIP);
        crossReferencing.notPerformed(step);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(notPerformed, step));
        } else {
            delegate.notPerformed(step);
        }
    }

    @Override
    public void failed(String step, Throwable cause) {
        if (cause.getCause().getClass().toString().contains(SKIP_EXCEPTION)) {
            scenarioSkipped = true;
            skippedReason = cause.getCause().getMessage();
            logStep(step, LogStatus.SKIP);
        } else {
            if (driverInstance != null) {
                ScreenshotCapture.createScreenCapture(driverInstance);
            }
            failedReason = cause.getCause();
            logStep(step, LogStatus.FAIL);
        }

        crossReferencing.failed(step, cause);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(failed, step, cause));
        } else {
            delegate.failed(step, cause);
        }

    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        crossReferencing.failedOutcomes(step, table);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(failedOutcomes, step, table));
        } else {
            delegate.failedOutcomes(step, table);
        }
    }

    @Override
    public void dryRun() {
        crossReferencing.dryRun();
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(dryRun));
        } else {
            delegate.dryRun();
        }
    }

    @Override
    public void pendingMethods(List<String> methods) {
        crossReferencing.pendingMethods(methods);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(pendingMethods, methods));
        } else {
            delegate.pendingMethods(methods);
        }

    }

    @Override
    public void restarted(String step, Throwable cause) {
        crossReferencing.restarted(step, cause);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(restarted, step, cause));
        } else {
            delegate.restarted(step, cause);
        }
    }

    @Override
    public void restartedStory(Story story, Throwable cause) {
        crossReferencing.restartedStory(story, cause);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(restartedStory, story, cause));
        } else {
            delegate.restartedStory(story, cause);
        }
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        crossReferencing.storyCancelled(story, storyDuration);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(storyCancelled, story, storyDuration));
        } else {
            delegate.storyCancelled(story, storyDuration);
        }
    }

    public StoryReporter getDelegate() {
        return delegate;
    }

    public boolean invoked() {
        return invoked;
    }

    public void invokeDelayed() {
        if (!multiThreading) {
            return;
        }
        synchronized (delegate) {
            for (DelayedMethod delayed : Collections.unmodifiableList(delayedMethods)) {
                delayed.invoke(delegate);
            }
        }
        invoked = true;
    }

    public static class DelayedMethod {
        private Method method;
        private Object[] args;

        public DelayedMethod(Method method, Object... args) {
            this.method = method;
            this.args = args;
        }

        public void invoke(StoryReporter delegate) {
            try {
                method.invoke(delegate, args);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("" + method, e);
            }
        }
    }
}