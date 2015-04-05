package auto.steps;

import auto.pages.IframePage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

public class IframeSteps extends ScenarioSteps {

    IframePage iframePage;

    @Step
    public void open_iframe_page() {
        iframePage.open();
    }

    @Step
    public void switch_to_iframe() {
        iframePage.switchToIframe();
    }

    @Step
    public void try_yourself_button_is_not_reachable() {
        iframePage.tryYourselfButtonIsNotReachable();
    }

    @Step
    public void try_yourself_button_is_reachable() {
        iframePage.tryYourselfButtonIsReachable();
    }
}