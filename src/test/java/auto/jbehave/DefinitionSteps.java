package auto.jbehave;

import auto.steps.EndUserSteps;
import auto.steps.IframeSteps;
import net.thucydides.core.annotations.Steps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jruby.RubyProcess;

public class DefinitionSteps {

    @Steps
    EndUserSteps endUser;

    @Steps
    IframeSteps iframeSteps;

    @Given("the user is on the Wikionary home page")
    public void givenTheUserIsOnTheWikionaryHomePage() {
        endUser.is_the_home_page();
    }

    @When("the user looks up the definition of the word '$word'")
    public void whenTheUserLooksUpTheDefinitionOf(String word) {
        endUser.looks_for(word);
    }

    @Then("they should see the definition '$definition'")
    public void thenTheyShouldSeeADefinitionContainingTheWords(String definition) {
        endUser.should_see_definition(definition);
    }

    @Given("the user is on the W3Schools HTML5 iframe page")
    public void givenTheUserIsOnW3SchoolsPage() {
        iframeSteps.open_iframe_page();
    }

    @When("the user switches to iframe")
    public void whenTheUserSwitchesToIframe() {
        iframeSteps.switch_to_iframe();
    }

    @Then("button Try Yourself is reachable")
    public void thenHeCanSeeText() {
        iframeSteps.try_yourself_button_is_reachable();
    }

    @Then("button Try Yourself is not reachable")
    public void thenHeCannotSeeText() {
        iframeSteps.try_yourself_button_is_not_reachable();
    }

}
