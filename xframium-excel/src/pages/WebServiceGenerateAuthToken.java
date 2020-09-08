package pages;

import org.testng.asserts.SoftAssert;
import org.xframium.page.Page;
import org.xframium.page.Page.ScreenShot;
import org.xframium.page.Page.TimeMethod;

public interface WebServiceGenerateAuthToken  extends Page {
	
	@TimeMethod
	@ScreenShot
	public String generateAuthTokenCode(String tcID, SoftAssert softAssert) throws Exception;


}
