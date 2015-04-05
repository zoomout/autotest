package auto.pages;


import auto.engine.session.Key;
import auto.engine.session.Session;
import net.thucydides.core.annotations.WhenPageOpens;
import net.thucydides.core.annotations.findby.By;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import net.thucydides.core.util.EnvironmentVariables;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.fail;

public class AbstractPage extends PageObject {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPage.class);
    private static final String CHECK_AJAX_SCRIPT = "function checkAjax() { return jQuery.active == 1 ? true : false; }"
            + " return checkAjax();";

    protected final EnvironmentVariables environmentVariables;

    /**
     * Element in IE about security problems
     */
    @FindBy(partialLinkText = "Continue to this website")
    private WebElementFacade continueWorkLink;

    public AbstractPage(final WebDriver driver) {
        super(driver);
        environmentVariables = Injectors.getInjector().getProvider(EnvironmentVariables.class).get();
    }

    @WhenPageOpens
    public void eventOpened() {
        String browserProvidedType = environmentVariables.getProperty("webdriver.driver");
        if (Session.get(Key.BROWSER_FIRST_OPEN) == null) {
            if ("firefox".equalsIgnoreCase(browserProvidedType)) {
                maximizeBrowser();
            }
            if ("provided".equalsIgnoreCase(browserProvidedType)) {
                continueWorkLink.sendKeys(Keys.CONTROL);
                continueWorkLink.click();
            }
            Session.put(Key.BROWSER_FIRST_OPEN, 1);
        }
    }

    public void waitForAjaxToComplete(final Integer timeout) {
        final Date startTime = new Date();
        final FluentWait<WebDriver> wait = new FluentWait<WebDriver>(getDriver());
        wait.pollingEvery(500, TimeUnit.MILLISECONDS).withTimeout(timeout, TimeUnit.SECONDS);
        try {
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(final WebDriver driver) {
                    return !(Boolean) evaluateJavascript(CHECK_AJAX_SCRIPT);
                }
            });
            final Date endTime = new Date();
            LOG.info("ajax execution on page took about {} milliseconds", endTime.getTime() - startTime.getTime());
        } catch (org.openqa.selenium.TimeoutException te) {
            LOG.error("ajax wait exceeded timeout, proceeding with test anyway\nmessage: ", te);
        }
    }

    /**
     * created for default ajax waiting, makes 30 seconds wait polling every 500 ms
     */
    public void waitForAjaxToComplete() {
        this.waitForAjaxToComplete(20);
    }

    public void clickAndCloseOpenedTab(final WebElementFacade buttonToClick) {
        final long waitTimeout = Long.valueOf(environmentVariables.getPropertyAsInteger(
                "webdriver.timeouts.implicitlywait", 10000));
        final String mainWindow = getDriver().getWindowHandle();
        buttonToClick.click();
        this.waitForNumberOfWindowsToAppear(2, waitTimeout);
        String printWindow = "";
        for (String window : getDriver().getWindowHandles()) {
            if (!window.equals(mainWindow)) {
                printWindow = window;
            }
        }
        getDriver().switchTo().window(printWindow);
        getDriver().close();
        getDriver().switchTo().window(mainWindow);
    }

    private void waitForNumberOfWindowsToAppear(final int windowNumbers, final long millisec) {
        final WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, 1);
        wait.withTimeout(millisec, TimeUnit.MILLISECONDS).pollingEvery(250, TimeUnit.MILLISECONDS)
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(final WebDriver driver) {
                        return (driver.getWindowHandles().size() == windowNumbers);
                    }
                });
        LOG.info("actual number of windows now is {}", driver.getWindowHandles().toString());
    }

    protected void doubleClick(final String xpathLocator) {
        final Actions action = new Actions(getDriver());
        action.doubleClick(getDriver().findElement(By.xpath(xpathLocator)));
        action.perform();
    }

    public void confirmModalDialog() {
        WebDriver driver = getDriver();
        try {
            WebDriverWait wait = new WebDriverWait(driver, environmentVariables.getPropertyAsInteger(
                    "webdriver.timeouts.implicitlywait", 10000));
            wait.until(ExpectedConditions.alertIsPresent());
            getDriver().switchTo().alert().accept();
            getDriver().switchTo().defaultContent();
        } catch (NoAlertPresentException e) {
            LOG.error("Modal dialog did not appear", e);
            fail("Modal dialog is expected, but did not appear.");
        }
    }

    /**
     * this method is automatically called in openAt()
     */
    protected void maximizeBrowser() {
        final Window browserWindow = getDriver().manage().window();
        browserWindow.maximize();
        LOG.info("browser got maximized, checking dimensions...");
        Dimension actualDimension = browserWindow.getSize();
        final Integer actualHeight = actualDimension.getHeight();
        final Integer actualWidth = actualDimension.getWidth();
        LOG.info("width: {}, height: {}", actualWidth, actualHeight);
    }

}
