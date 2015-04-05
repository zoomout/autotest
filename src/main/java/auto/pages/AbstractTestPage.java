package auto.pages;


import auto.engine.session.Key;
import auto.engine.session.Session;
import auto.panels.HeaderPanel;
import auto.utils.ProjectLogger;
import net.thucydides.core.annotations.WhenPageOpens;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import java.net.URI;
import java.util.Date;
import java.util.Set;

// TODO: add reference to footer panels
public abstract class AbstractTestPage extends AbstractPage {

    private static final Logger LOG = ProjectLogger.getLogger(AbstractTestPage.class.getSimpleName());

    @FindBy(xpath = "//div[@class='header']")
    private WebElementFacade pageHeaderPanelBase;

    private String intialPageHandle;

    public AbstractTestPage(WebDriver driver) {
        super(driver);
    }

    @WhenPageOpens
    public void eventOpened() {
        if (Session.get(Key.COOKIES_SET) == null) {
            if (setCookies()) {
                Session.put(Key.COOKIES_SET, 1);
            }
        }
        super.eventOpened();
    }

    private boolean setCookies() {
        final WebDriver driver = this.getDriver();
        final String currentFullUrl = driver.getCurrentUrl();
        if (!modifyCookie(driver)) {
            LOG.info(" cookies were not set...");
            return false;
        }
        driver.get(currentFullUrl);
        return true;
    }

    /**
     * This method modifies "cookie.name" cookie<br>
     * <br/>
     * It adds to cookie's Json body value: i = -1
     */
    private boolean modifyCookie(final WebDriver driver) {
        final String cookieName = "cookie.name";
        Cookie cookie = driver.manage().getCookieNamed(cookieName);
        if (cookie == null) {
            LOG.warn("will wait appropriate page to update cookie");
            return false;
        }
        String value = cookie.getValue();
        String valueModified = value.substring(0, 20) + "i%22%3A-1%2C%22" + value.substring(20);

        Cookie cookieModified = new Cookie(cookie.getName(), valueModified,
                cookie.getDomain(), cookie.getPath(), cookie.getExpiry(),
                cookie.isSecure(), cookie.isHttpOnly());

        driver.manage().deleteCookieNamed(cookieName);
        driver.manage().addCookie(cookieModified);

        LOG.info("driver has set special cookie: {}",
                driver.manage().getCookieNamed(cookieName).toString());
        return true;
    }

    /**
     * will switch to last opened window or stay on current window if no windows were opened
     */
    public void switchToNewWindow() {
        Set<String> set = this.getDriver().getWindowHandles();
        intialPageHandle = set.toArray()[0].toString();
        getDriver().switchTo().window(set.toArray()[set.size() - 1].toString());
    }

    /**
     * will switch on first opened window or stay on current if switchToNewWindow() was not used before doesn't close
     * windows
     */
    public void switchToParentWindow() {
        if (intialPageHandle != null) {
            getDriver().switchTo().window(intialPageHandle);
        }
    }

    public boolean isCurrentPageEnglish() {
        String currentUrl = getDriver().getCurrentUrl();
        return currentUrl.matches("^.+/en/.+\\.html$") || currentUrl.matches("^.+/en\\.html$");
    }

    public HeaderPanel getHeaderPanel() {
        pageHeaderPanelBase.waitUntilVisible();
        return new HeaderPanel(pageHeaderPanelBase, this);
    }

    public void switchToMainFrame() {
        LOG.info("switchToMainFrame");
        getDriver().switchTo().defaultContent();
    }
}
