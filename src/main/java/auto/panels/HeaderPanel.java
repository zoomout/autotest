package auto.panels;


import auto.pages.AbstractPage;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.WebElementFacade;


public class HeaderPanel extends AbstractPanel {

    @FindBy(xpath = ".//div[@class='logo']")
    private WebElementFacade headerLogo;
    @FindBy(id = "signIn")
    private WebElementFacade signIn;
    @FindBy(id = "signOut")
    private WebElementFacade singOut;

    public HeaderPanel(final WebElementFacade panelBaseLocation, final AbstractPage driverDelegate) {
        super(panelBaseLocation, driverDelegate);
    }

    public void clickHeaderLogo() {
        headerLogo.click();
    }

    public boolean isSignInPresent() {
        return signIn.isCurrentlyVisible();
    }

    public String getSignOutText() {
        return singOut.getText().trim();
    }
}

