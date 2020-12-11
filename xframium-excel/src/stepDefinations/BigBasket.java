package stepDefinations;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import utility.CustomAbstractPage;

public class BigBasket extends CustomAbstractPage
{
	
	@Given("I have login button")
	public void i_have_login_button() 
	{	
		
		getWebDriver().get("https://wwww.google.com");
	   
	}

	@When("Clik on the login button")
	public void clik_on_the_login_button() {
	    System.out.println("When");
	    //throw new io.cucumber.java.PendingException();
	}

	@Then("Enter username and password and click on Signin button")
	public void enter_username_and_password_and_click_on_signin_button() {
	    System.out.println("Then");
	    //throw new io.cucumber.java.PendingException();
	}


}
