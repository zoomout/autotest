package auto.panels;


import auto.pages.AbstractPage;
import auto.utils.WebDriverAdaptor;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.annotations.locators.SmartElementLocatorFactory;
import net.thucydides.core.annotations.locators.SmartFieldDecorator;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import net.thucydides.core.util.EnvironmentVariables;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.concurrent.TimeUnit;


public abstract class AbstractPanel {

    private static final int WAIT_FOR_ELEMENT_POLLING_INTERVAL = 50;
    private static final int MINIMUM_IMPLICIT_WAIT = 20;

    private final EnvironmentVariables environmentVariables;
    private AbstractPage driverDelegate;
    private WebDriverAdaptor panelToWebDriver;
    private long timeoutInMilliseconds;

    //TODO: review and find better way to locate base element.
    //(saving panelBaseLocation from constructor seems to be not working)
    @FindBy(xpath = "./../*")
    private WebElementFacade panelBase;

    public AbstractPanel(final WebElementFacade panelBaseLocation, final AbstractPage driverDelegate) {
        environmentVariables = Injectors.getInjector().getProvider(EnvironmentVariables.class).get();
        timeoutInMilliseconds = Long.valueOf(environmentVariables.getPropertyAsInteger(
                "webdriver.timeouts.implicitlywait", 10000));
        initPanel(panelBaseLocation, driverDelegate);
    }

    private void initPanel(final WebElementFacade panelBaseLocation, final AbstractPage driverDelegate) {
        this.driverDelegate = driverDelegate;
        this.timeoutInMilliseconds = driverDelegate.waitForTimeoutInMilliseconds();
        this.panelToWebDriver = new WebDriverAdaptor(panelBaseLocation, getDriver());
        ElementLocatorFactory finder = new SmartElementLocatorFactory(panelToWebDriver, (int) waitForTimeoutInSeconds());
        FieldDecorator decorator = new SmartFieldDecorator(finder, getDriver(), driverDelegate);
        PageFactory.initElements(decorator, this);
    }

    public boolean isPanelVisible() {
        return panelBase.isCurrentlyVisible();
    }

    public void waitForAppearance() {
        panelBase.waitUntilVisible();
    }


    public WebDriver getDriver() {
        return driverDelegate.getDriver();
    }

    public AbstractPage getDriverDelegate() {
        return driverDelegate;
    }

    public long waitForTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    private long waitForTimeoutInSeconds() {
        return (timeoutInMilliseconds < 1000) ? 1 : (timeoutInMilliseconds / 1000);
    }

    //method made private since there is no a precedent of its usage outside teh class + there is no public setter
    //to change timeout to another value
    private void restoreDefaultDriverTimeout() {
        long defaultTimeToWait = Long.valueOf(environmentVariables.getPropertyAsInteger(
                "webdriver.timeouts.implicitlywait", 10000));
        driverDelegate.getDriver().manage().timeouts().implicitlyWait(defaultTimeToWait, TimeUnit.MILLISECONDS);
    }

    public PageObject waitForAllTextToAppear(final String... expectedTexts) {
        return driverDelegate.waitForAllTextToAppear(expectedTexts);
    }

    public PageObject waitForAnyTextToAppear(final String... expectedText) {
        return driverDelegate.waitForAnyTextToAppear(expectedText);
    }

    public PageObject waitForAnyTextToAppear(final WebElement element, final String... expectedText) {
        return driverDelegate.waitForAnyTextToAppear(element, expectedText);
    }

    public PageObject waitForTextToDisappear(final WebElement element, final String expectedText) {
        return driverDelegate.waitForTextToDisappear(element, expectedText);
    }

    public PageObject waitForTextToDisappear(final String expectedText) {
        return driverDelegate.waitForTextToDisappear(expectedText);
    }

    public PageObject waitForTextToDisappear(final String expectedText, final long timeout) {
        return driverDelegate.waitForTextToDisappear(expectedText, timeout);
    }

    public AbstractPanel waitFor(final ExpectedCondition<?> expectedCondition, final long timeoutMillis) {
        long implicitWait = WAIT_FOR_ELEMENT_POLLING_INTERVAL > MINIMUM_IMPLICIT_WAIT ?
                WAIT_FOR_ELEMENT_POLLING_INTERVAL - MINIMUM_IMPLICIT_WAIT : MINIMUM_IMPLICIT_WAIT;
        getDriver().manage().timeouts().implicitlyWait(implicitWait, TimeUnit.MILLISECONDS);
        try {
            doWait(timeoutMillis).until(expectedCondition);
        } finally {
            restoreDefaultDriverTimeout();
        }
        return this;
    }

    public AbstractPanel waitFor(final ExpectedCondition<?> expectedCondition) {
        return waitFor(expectedCondition, timeoutInMilliseconds);
    }

    private FluentWait<WebDriver> doWait(final long timeoutInMilliseconds) {
        return new FluentWait<WebDriver>(panelToWebDriver).withTimeout(timeoutInMilliseconds, TimeUnit.MILLISECONDS)
                .pollingEvery(WAIT_FOR_ELEMENT_POLLING_INTERVAL, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class, NoSuchFrameException.class);
    }
}

