package pages.spi;

import org.testng.asserts.SoftAssert;
import org.xframium.device.ng.AbstractSeleniumTest;
import org.xframium.page.PageManager;

import pages.Address;
import pages.Login;
import pages.Search;

public class PageImplInvoker extends AbstractSeleniumTest{
	
/************************Alliance Flow Controller for Renter LOB**************************/
	SoftAssert softAssert= new SoftAssert();
	
	//Login Page
	
	public void Login(String tcID,String DeviceName) throws Exception
 	{
 		Login objLogin = (Login) PageManager.instance().createPage( Login.class, getWebDriver());
 		objLogin.Login(tcID, softAssert,DeviceName);
 	}
	
	public void LogOut(String tcID,String DeviceName) throws Exception
 	{
 		Login objLogin = (Login) PageManager.instance().createPage( Login.class, getWebDriver());
 		objLogin.LogOut(tcID, softAssert,DeviceName);
 	}
	
	//Search Page
	public void searchProduct(String tcID,String DeviceName, String productName) throws Exception
 	{
 		Search objSearch = (Search) PageManager.instance().createPage(Search.class, getWebDriver());
 		objSearch.searchProduct(tcID, softAssert, DeviceName, productName);
 	}
	
	//Address Page
	public void navigateDeliveryAddress(String tcID,String DeviceName, String productName) throws Exception
 	{
 		Address objSearch = (Address) PageManager.instance().createPage(Address.class, getWebDriver());
 		objSearch.navigateDeliveryAddress(tcID, softAssert, DeviceName, productName);
 	}
	public void addAddress(String tcID,String DeviceName, String productName) throws Exception
 	{
 		Address objSearch = (Address) PageManager.instance().createPage(Address.class, getWebDriver());
 		objSearch.addAddress(tcID, softAssert, DeviceName, productName);
 	}
	public void updateAddress(String tcID,String DeviceName, String productName) throws Exception
 	{
 		Address objSearch = (Address) PageManager.instance().createPage(Address.class, getWebDriver());
 		objSearch.updateAddress(tcID, softAssert, DeviceName, productName);
 	}
	public void deleteAddress(String tcID,String DeviceName, String productName) throws Exception
 	{
 		Address objSearch = (Address) PageManager.instance().createPage(Address.class, getWebDriver());
 		objSearch.deleteAddress(tcID, softAssert, DeviceName, productName);
 	}
	
	public void StartTakingScreenShots(String tcID,String DeviceName) throws Exception
 	{
		Login objLogin = (Login) PageManager.instance().createPage( Login.class, getWebDriver());
 		objLogin.startPrintScreenShots(tcID, softAssert,DeviceName);
		
 	}
	
}
