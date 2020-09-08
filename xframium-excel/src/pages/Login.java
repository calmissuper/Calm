package pages;

import org.testng.asserts.SoftAssert;
import org.xframium.page.Page;

public interface Login extends Page 
{
	
	@ElementDefinition
	public String btn_Login="btn_Login" ;
	@ElementDefinition
	public String btn_gmail="btn_gmail" ;
	@ElementDefinition
	public String btn_Next="btn_Next" ;
	@ElementDefinition
	public String txt_username="txt_username" ;
	@ElementDefinition
	public String txt_password="txt_password" ;
	@ElementDefinition
	public String Label_Logout="Label_Logout" ;
	@ElementDefinition
	public String btn_Logout="btn_Logout" ;
	@ElementDefinition
	public String label_User="label_User" ;
	
	
	

	
	
	@TimeMethod
	@ScreenShot
	public void Login(String tcID, SoftAssert softAssert, String DeviceName, String user) throws Exception;
	public void startPrintScreenShots(String tcID, SoftAssert softAssert, String DeviceName) throws Exception ;

	public void LogOut(String tcID, SoftAssert softAssert, String DeviceName) throws Exception ;

}
