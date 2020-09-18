package pages.spi;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Set;

import org.openqa.selenium.By;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.testng.asserts.SoftAssert;
import org.xframium.page.StepStatus;
import org.xframium.page.data.PageData;
import org.xframium.page.data.PageDataManager;

import pages.Login;
import utility.CustomAbstractPage;
import utility.CustomReporting;
import utility.Report;

public class LoginImpl extends CustomAbstractPage implements Login 
{

	@Override
	public void Login (String tcID, SoftAssert softAssert,String DeviceName) throws Exception
	{
		try 
		{
		func._waitForPageToLoad(getWebDriver(), 200L);
		Thread.sleep(2000);
		PageData dataLogin = PageDataManager.instance().getPageData("Login",tcID);
		/*String strScreenShots=dataLogin.getData("PrintScreens");
        if(strScreenShots.equalsIgnoreCase("Yes"))
        {
          func._takeScreenShot(getWebDriver(), "START", "", "");
        }*/
        String strURL="";
        if(dataLogin.getData("Environment").toUpperCase().equals("QA"))
        {
        	strURL=dataLogin.getData("QAUrl");
        	
        }
        if(dataLogin.getData("Environment").toUpperCase().equals("STAGE"))
        {
        	strURL=dataLogin.getData("Stage_URL");
        	
        }
        if(dataLogin.getData("Environment").toUpperCase().equals("SIT"))
        {
        	strURL=dataLogin.getData("SIT_URL");
        	
        }
        
		getWebDriver().navigate().to(strURL);
	    getWebDriver().manage().window().maximize();
	    
	    Report.instance().log(tcID, "INFO", "Open Application URL : "+strURL);
	    //CustomReporting.logReport("Open Application URL : "+  strURL);
	   
	    Thread.sleep(2000);
	    if(dataLogin.getData("Environment").toUpperCase().equals("STAGE") || dataLogin.getData("Environment").toUpperCase().equals("SIT"))
        {
	    	func._click(getElement(btn_Login));
	    	/* _setValue(getElement(txt_username),dataLogin.getData("StageUsername"));
	    	Thread.sleep(1000);
	    	_setValue(getElement(txt_password),dataLogin.getData("StagePassword")); */
        }
	    if(dataLogin.getData("Environment").toUpperCase().equals("QA"))
        {
	    	func._waitForPageToLoad(getWebDriver(), 50L);	
	    	func._click(getElement(btn_Login));
	    	_setValue(getElement(txt_email), dataLogin.getData("QAUserName"));
	    	func._click(getElement(btn_Continue));
	      	_setValue(getElement(txt_password), dataLogin.getData("QAPassword"));
	      	func._click(getElement(btn_LoginA));
	      	
        }
	    
	    	func._takeBrowserScreenShot(getWebDriver(), "PRINT", getWebDriver().getCurrentUrl(), "");
	    	Thread.sleep(2000);
	   
	    
	    if(getElement(label_User).addToken("tkn_UserName", dataLogin.getData("My Account")).isVisible())
	    {	
	    	Report.instance().log(tcID, "PASS", "Logged into Application Sucessfully");
	    	
	    	//CustomReporting.logReport("Logged into Application Sucessfully as :"+ user, StepStatus.SUCCESS);
	    }
	    else
	    {	
	    	Report.instance().log(tcID, "FAIL", "Login Failed");
	    	//CustomReporting.logReport("Login Failed ", StepStatus.FAILURE);	
	    }
		}
		catch(Exception e)
		{
			Report.instance().log(tcID, "FINISH", "");
		}
	}
	@Override
	public void startPrintScreenShots(String tcID, SoftAssert softAssert, String DeviceName) throws Exception {
		PageData dataLogin = PageDataManager.instance().getPageData("Login",tcID);
		String strScreenShots=dataLogin.getData("PrintScreens");

		if (strScreenShots.equalsIgnoreCase("Yes")) {
			func._takeBrowserScreenShot(getWebDriver(), "START", "", "");
		}
		focusBrowser();
	}
	public void focusBrowser() throws Exception {

        try {

              // String
              // strDeviceName=GenericFunctions.instance()._getGlobalVariableValue("deviceName");

              File directory = new File("@/");
              System.out.println(directory.getAbsolutePath());
              String strDirectoryPath = directory.getAbsolutePath().split("\\@")[0];
              String strBundlePath = strDirectoryPath + "resources\\Images";
              ImagePath.setBundlePath(strBundlePath);
              Screen screen = new Screen();
              Pattern img_Desktop = new Pattern("Desktop.PNG");
              Robot robot = new Robot();

              try {
                    screen.find(img_Desktop);
                    robot.keyPress(KeyEvent.VK_ALT);
                    robot.keyPress(KeyEvent.VK_TAB);
                    robot.delay(100);
                    robot.keyPress(KeyEvent.VK_TAB);
                    robot.delay(100);
                    robot.keyPress(KeyEvent.VK_TAB);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_ALT);
                    robot.keyRelease(KeyEvent.VK_TAB);
                    robot.keyRelease(KeyEvent.VK_TAB);
                    robot.keyRelease(KeyEvent.VK_TAB);
                   /* robot.delay(100);
                   // Thread.sleep(1000);
                    robot.keyPress(KeyEvent.VK_ALT);
                    robot.keyPress(KeyEvent.VK_TAB);
                    robot.keyRelease(KeyEvent.VK_ALT);
                    robot.keyRelease(KeyEvent.VK_TAB);
                    robot.delay(100);
                   // Thread.sleep(1000);
                    robot.keyPress(KeyEvent.VK_ALT);
                    robot.keyPress(KeyEvent.VK_TAB);
                    robot.keyRelease(KeyEvent.VK_ALT);
                    robot.keyRelease(KeyEvent.VK_TAB);*/
                    CustomReporting.logReport("Browser brought to the foreground");
              } catch (Exception ex) {

              }

        }

        finally {

        }
  }
	@Override
	public void LogOut(String tcID, SoftAssert softAssert, String DeviceName) throws Exception 
	{
		
		func._waitForPageToLoad(getWebDriver(), 200L);
		//func._click(getElement(Label_Logout));
		func._actionClick(getElement(btn_Logout));
		func._waitForPageToLoad(getWebDriver(), 100L);
		 if(func._isVisible(getElement(btn_Login)))
		    {
			 	Report.instance().log(tcID, "PASS", "Logged out of Application Sucessfully");
		    	
		    	//CustomReporting.logReport("Logged Out of Application Sucessfully", StepStatus.SUCCESS);
		    }	    else
		    {
		    	Report.instance().log(tcID, "FAIL", "Logout Failed");
		    	
		    	//CustomReporting.logReport("LogOut Failed ", StepStatus.FAILURE);	
		    }
		
		
	}
	
}
