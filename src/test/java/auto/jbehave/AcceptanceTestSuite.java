package auto.jbehave;


import auto.utils.OsCheck;
import auto.utils.ProjectLogger;
import ch.lambdaj.Lambda;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.jbehave.ThucydidesJUnitStories;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.RegexStoryParser;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AcceptanceTestSuite extends ThucydidesJUnitStories {
    private static final Logger LOG = ProjectLogger.getLogger(AcceptanceTestSuite.class.getSimpleName());
    private static final String X64_ARCH = "amd64";
    private static EnvironmentVariables environmentVariables = Injectors.getInjector()
            .getProvider(EnvironmentVariables.class).get();

    public AcceptanceTestSuite() {
        try {
            Class.forName("auto.utils.PropertiesCollector");
        } catch (ClassNotFoundException e) {
            LOG.error("error instantiating properties", e);
        }
        setDriverAccordingToOS();
        selectStoryFilesForRunningSuite();
    }

    private void selectStoryFilesForRunningSuite() {
        String storiesPattern = environmentVariables.getProperty("auto.stories");
        if (storiesPattern != null) {
            customRunningStories(storiesPattern);
        } else {
            parallelAcceptanceTestSuite(storyPaths());
        }
    }

    private void setDriverAccordingToOS() {
        OsCheck.OSType ostype = OsCheck.getOperatingSystemType();
        switch (ostype) {
            case Windows:
                setChromeDriverWindows();
                setPhantomJSDriverWindows();
                if (X64_ARCH.equals(System.getProperty("os.arch"))) {
                    setIeDriverWindows64();
                } else {
                    setIeDriverWindows32();
                }
                break;
            case MacOS:
                setChromeDriverOsx();
                setPhantomJSDriverOsx();
                break;
            case Linux:
                if (X64_ARCH.equals(System.getProperty("os.arch"))) {
                    setChromeDriverLinux64();
                    setPhantomJSDriverLinux64();
                } else {
                    setChromeDriverLinux32();
                    setPhantomJSDriverLinux32();
                }
                break;
            case Other:
                LOG.error("Can't define OS");
                break;
        }
    }

    public void customRunningStories(String runningSuite) {
        List<String> storyNames = new ArrayList<>();
        switch (runningSuite) {
            case "smoke":
                storyNames.add("auto/stories/consult_dictionary/WorkWithIframes.story");
                break;
            default:
                findStoriesCalled(runningSuite);
                return;
        }
        if (storyNames != null) {
            parallelAcceptanceTestSuite(storyNames);
        }

    }

    public void setChromeDriverLinux32() {
        System.setProperty("webdriver.chrome.driver", "drivers/linux/32bit/chromedriver");
    }

    public void setChromeDriverLinux64() {
        System.setProperty("webdriver.chrome.driver", "drivers/linux/64bit/chromedriver");
    }

    public void setChromeDriverWindows() {
        System.setProperty("webdriver.chrome.driver", "drivers/windows/chromedriver.exe");
    }

    public void setChromeDriverOsx() {
        System.setProperty("webdriver.chrome.driver", "drivers/osx/chromedriver");
    }

    public void setPhantomJSDriverLinux32() {
        System.setProperty("phantomjs.binary.path", "drivers/linux/32bit/phantomjs");
    }

    public void setPhantomJSDriverLinux64() {
        System.setProperty("phantomjs.binary.path", "drivers/linux/64bit/phantomjs");
    }

    public void setPhantomJSDriverWindows() {
        System.setProperty("phantomjs.binary.path", "drivers/windows/phantomjs.exe");
    }

    public void setPhantomJSDriverOsx() {
        System.setProperty("webdriver.phantomjs.driver", "drivers/osx/phantomjs");
    }

    public void setIeDriverWindows32() {
        System.setProperty("webdriver.ie.driver", "drivers/windows/32bit/iedriver.exe");
    }

    public void setIeDriverWindows64() {
        System.setProperty("webdriver.ie.driver", "drivers/windows/64bit/iedriver.exe");
    }

    @Override
    public Configuration configuration() {
        new MostUsefulConfiguration().useStoryParser(new RegexStoryParser(new ExamplesTableFactory(
                new LoadFromClasspath(this.getClass()))));
        return super.configuration();
    }

    public void parallelAcceptanceTestSuite(List<String> storyNames) {

        Integer agentNumber = environmentVariables.getPropertyAsInteger("parallel.agent.number", 1);
        Integer agentTotal = environmentVariables.getPropertyAsInteger("parallel.agent.total", 1);
        List storyPaths = storyNames;
        failIfAgentIsNotConfiguredCorrectly(agentNumber, agentTotal);
        failIfThereAreMoreAgentsThanStories(agentTotal, storyPaths.size());

        int reminder = storyPaths.size() % agentTotal; // The reminder should work out to be either be zero or one.
        int storiesPerAgent = storyPaths.size() / agentTotal;

        int startPos = storiesPerAgent * (agentNumber - 1);
        int endPos = startPos + storiesPerAgent;
        if (agentNumber == agentTotal) {
            // In the case of an uneven number the last agent picks up the extra story file.
            endPos += reminder;
        }
        List stories = storyPaths.subList(startPos, endPos);

        outputWhichStoriesAreBeingRun(stories);
        findStoriesCalled(Lambda.join(stories, ";"));

    }

    private void failIfAgentIsNotConfiguredCorrectly(Integer agentPosition, Integer agentCount) {
        if (agentPosition == null) {
            throw new RuntimeException("The agent number needs to be specified");
        } else if (agentCount == null) {
            throw new RuntimeException("The agent total needs to be specified");
        } else if (agentPosition < 1) {
            throw new RuntimeException("The agent number is invalid");
        } else if (agentCount < 1) {
            throw new RuntimeException("The agent total is invalid");
        } else if (agentPosition > agentCount) {
            throw new RuntimeException(
                    String.format(
                            "There were %d agents in total specified and this agent is outside that range (it is specified as %d)",
                            agentPosition, agentCount));
        }
    }

    private void failIfThereAreMoreAgentsThanStories(Integer agentCount, int storyCount) {
        if (storyCount < agentCount) {
            throw new RuntimeException("There are more agents then there are stories, this agent isn't necessary");
        }
    }

    private void outputWhichStoriesAreBeingRun(List<String> stories) {
        LOG.info("Running stories: ");
        for (String story : stories) {
            LOG.info(" - " + story);
        }
    }
}
