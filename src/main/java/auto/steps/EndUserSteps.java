package auto.steps;

import auto.pages.DictionaryPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;


public class EndUserSteps extends ScenarioSteps {


    DictionaryPage dictionaryPage;

    @Step
    public void enters(String keyword) {
        dictionaryPage.enter_keywords(keyword);
    }

    @Step
    public void starts_search() {
        dictionaryPage.lookup_terms();
    }

    @Step
    public void should_see_definition(String expectedDefinition) {
        List<String> definitions = dictionaryPage.getDefinitions();
        boolean isExpectedDefinitionPresent = false;
        for (String actualDefinition : definitions) {
            if (actualDefinition.contains(expectedDefinition)) {
                isExpectedDefinitionPresent = true;
                break;
            }
        }
        assertThat(isExpectedDefinitionPresent).as("Expected definition is not present on the page").isTrue();
    }

    @Step
    public void is_the_home_page() {
        dictionaryPage.open(System.getProperty("host.name"));
    }

    @Step
    public void looks_for(String term) {
        enters(term);
        starts_search();
    }
}