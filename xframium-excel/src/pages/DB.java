package pages;

import org.testng.asserts.SoftAssert;

public interface DB 
{
	public void validateDataDBUI(String tcID, SoftAssert softAssert, String DeviceName) throws Exception;

}
