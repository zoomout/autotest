package auto.pages;

import com.google.common.base.Predicate;
import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.WebElementFacade;
import org.fest.assertions.api.Assertions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

@DefaultUrl("http://www.w3schools.com/html/html_iframe.asp")
public class IframePage extends AbstractTestPage {

    @FindBy(xpath = "//iframe[@src='default.asp']")
    private WebElementFacade iframe;

    @FindBy(xpath = "//*[text()='Examples in Every Chapter']")
    private WebElementFacade tryYourselfButton;

    public IframePage(WebDriver driver) {
        super(driver);
    }

    public void switchToIframe() {
        switchToFrame();
    }

    public void tryYourselfButtonIsReachable() {
        Assertions.assertThat(tryYourselfButton.isVisible()).as("Element is not reachable.").isTrue();
    }

    public void tryYourselfButtonIsNotReachable() {
        Assertions.assertThat(tryYourselfButton.isVisible()).as("Element is reachable.").isFalse();
    }

    /**
     * method fluently waits until iFrame is present and fully loaded (corresponding url is valid)
     */
    private void switchToFrame() {
        final long waitTimeout = Long.valueOf(environmentVariables.getPropertyAsInteger(
                "webdriver.timeouts.implicitlywait", 10000)) / 1000;
        WebDriverWait wait = new WebDriverWait(getDriver(), waitTimeout);
        wait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(final WebDriver driver) {
                driver.switchTo().defaultContent();
                driver.switchTo().frame(iframe);
                String url = driver.getCurrentUrl();
                return !url.isEmpty() && !"about:blank".equals(url);
            }
        });
    }
}