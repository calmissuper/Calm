package uiAutomation;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.xframium.spi.Device;

import functions.GenericFunctions;
import pages.spi.PageImplInvoker;
import utility.CustomAbstractTest;
import utility.CustomReporting;
import utility.Report;


public class TestDriverClasses extends CustomAbstractTest
{
	

	@Test( dataProvider = "DeviceManager",enabled=true)
	public void FirstScript(TestName testName, Device device) throws Exception 
    {
		String tcID="";
		try
		{  
			tcID = getTestName(testName);
			Report.instance().log(tcID, "CREATE", "");
 			Report.instance().log(tcID, "START", "");
 			GenericFunctions.instance()._takeBrowserScreenShot(getWebDriver(), "START", "", tcID);
			SoftAssert softAssert= new SoftAssert();
			tcID = getTestName(testName);
			String gvTestKey=testName.toString().split(device.getBrowserName()+"_")[1].split("\\[")[0];
			GenericFunctions.instance()._addToGlobalVariableList(gvTestKey, testName.toString()); 
 			String DeviceName = getDeviceName(testName);
 			PageImplInvoker rentersClassInvoker = new PageImplInvoker();
 			rentersClassInvoker.Login(tcID,DeviceName);
 			/*rentersClassInvoker.navigateDeliveryAddress(tcID, DeviceName, "Abhishek");
 			rentersClassInvoker.addAddress(tcID, DeviceName, "Automation");
 			rentersClassInvoker.updateAddress(tcID+"_Edit", DeviceName, "Automation");
 			rentersClassInvoker.deleteAddress(tcID+"_Delete", DeviceName, "Automation");
 			rentersClassInvoker.LogOut(tcID, DeviceName);*/
 			//softAssert.assertAll();
 			Report.instance().log("FirstScript", "FINISH", "");
			
 			
		}
		catch(Exception ex)
		{
			
    	GenericFunctions.instance()._takeBrowserScreenShot(getWebDriver(), "FINISH", "", tcID);
    	Report.instance().log("FirstScript", "FINISH", "");
    	ex.printStackTrace();    
    	throw ex;
    	
		}	
    }
	
	@Test( dataProvider = "DeviceManager",enabled=true)
	public void SecondScript(TestName testName, Device device) throws Exception 
    {
		String tcID="";
		try
		{  
			tcID = getTestName(testName);
			Report.instance().log(tcID, "CREATE", "");
 			Report.instance().log(tcID, "START", "");
 			GenericFunctions.instance()._takeBrowserScreenShot(getWebDriver(), "START", "", tcID);
			SoftAssert softAssert= new SoftAssert();
			tcID = getTestName(testName);
			String gvTestKey=testName.toString().split(device.getBrowserName()+"_")[1].split("\\[")[0];
			GenericFunctions.instance()._addToGlobalVariableList(gvTestKey, testName.toString()); 
 			String DeviceName = getDeviceName(testName);
 			PageImplInvoker rentersClassInvoker = new PageImplInvoker();
 			rentersClassInvoker.Login(tcID,DeviceName);
 			/*rentersClassInvoker.navigateDeliveryAddress(tcID, DeviceName, "Abhishek");
 			rentersClassInvoker.addAddress(tcID, DeviceName, "Automation");
 			rentersClassInvoker.updateAddress(tcID+"_Edit", DeviceName, "Automation");
 			rentersClassInvoker.deleteAddress(tcID+"_Delete", DeviceName, "Automation");
 			rentersClassInvoker.LogOut(tcID, DeviceName);*/
 			softAssert.assertAll();
 			Report.instance().log("SecondScript", "FINISH", "");
			
 			
		}
		catch(Exception ex)
		{
			
    	GenericFunctions.instance()._takeBrowserScreenShot(getWebDriver(), "FINISH", "", tcID);
    	Report.instance().log("SecondScript", "FINISH", "");
    	ex.printStackTrace();    
    	throw ex;
    	
		}	
    }
}
