package pages;

import org.testng.asserts.SoftAssert;
import org.xframium.page.Page;

public interface Login extends Page 
{
	
	@ElementDefinition
	public String btn_Login="btn_Login" ;
	@ElementDefinition
	public String txt_email="txt_email" ;
	@ElementDefinition
	public String btn_Continue="btn_Continue" ;
	@ElementDefinition
	public String txt_username="txt_username" ;
	@ElementDefinition
	public String txt_password="txt_password" ;
	@ElementDefinition
	public String btn_LoginA="btn_LoginA" ;
	@ElementDefinition
	public String label_Logout="label_Logout" ;
	@ElementDefinition
	public String label_User="label_User" ;
	@ElementDefinition
	public String btn_Logout="btn_Logout" ;
	
	
	

	
	
	@TimeMethod
	@ScreenShot
	public void Login(String tcID, SoftAssert softAssert, String DeviceName) throws Exception;
	public void startPrintScreenShots(String tcID, SoftAssert softAssert, String DeviceName) throws Exception ;

	public void LogOut(String tcID, SoftAssert softAssert, String DeviceName) throws Exception ;

}
