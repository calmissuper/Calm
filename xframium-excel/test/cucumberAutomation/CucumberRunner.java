package cucumberAutomation;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;


@RunWith(Cucumber.class)
@CucumberOptions(
		features = {"/Users/manjunathda/git/Calm/xframium-excel/src/features"},
		glue = {"/Users/manjunathda/git/Calm/xframium-excel/src/stepDefinations"}
		)

public class CucumberRunner 
{
	
}
