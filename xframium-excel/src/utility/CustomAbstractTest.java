/*******************************************************************
	*	Name              :	AbstractTest
	*	Description       : New class which extends AbstractSeleniumTest, This class should be extended by all the local Tests 	
 	*	Modification Log  :                                                     
	*	Date		Initials     	Description of Modifications 
	********************************************************************
	********************************************************************/
package utility;


import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.xframium.device.DeviceManager;
import org.xframium.device.ng.AbstractSeleniumTest;
import org.xframium.spi.Device;

import functions.GenericFunctions;
import settings.Cleanup;
import settings.Setup;


public class CustomAbstractTest  extends AbstractSeleniumTest {
	
	
	//protected  ImpersonatePage objLoginClassReference=null;
	
/************************ Object Declaration*******************************************/
	public void objectInitialization()
	{
		  //objLoginClassReference = (ImpersonatePage) PageManager.instance().createPage( ImpersonatePage.class, getWebDriver());
		  	  
	}
/************************ Object Declaration Ends**************************************/	
	
	
/************************ Test Case Name Reading and storing *******************************************/		
	protected void setTestName(TestName testName)
	{
		String test[]=parseTest(testName.getTestName());
		String testCaseName=test[1];
	
		
	}
	
	protected String getTestName(TestName testName)
	{
		String test[]=parseTest(testName.getTestName());
		String testCaseName=test[1];
		return(testCaseName);
		
	}
	
	protected String getDeviceName(TestName testName)
	{
		String test[]=parseTest(testName.getTestName());
		String Devicename=test[0];
		String Device[]=Devicename.split("_");
		return(Device[0]);
		
	}
	
	public String[] parseTest(String testi){
		
		
		String[] str_array = testi.split("\\[");
		String lobName= (str_array[0]);
		String testName1=str_array[1];
		String[] str_array1 = testName1.split("\\]");
		String testCaseName= (str_array1[0]);
		
		String test[]={lobName,testCaseName};		
		
		return test;
		
	}

/************************ End Test Case Name Reading and storing*******************************************/	
	
/************************ @DataProvider*******************************************/	
	/*******************************************************************
	*	Name              :	customDeviceManager
	*	Description       : Used run Test Case on multiple data Set
 	*	Modification Log  :                                                     
	*	Date		Initials     	Description of Modifications 
	********************************************************************/
	 @DataProvider ( name = "DeviceManager", parallel = true)
	    public Object[][] getCustomDeviceData(Method currentMethod)
	    {
			List<Device> deviceList = DeviceManager.instance().getDevices();
			Object[][] objDeviceList = getDeviceData( deviceList );
			
			//System.out.println(objDeviceList.length);
			
			ArrayList TestCaseList = new ArrayList<String>();
			String sMethodName=currentMethod.getName();
			TestCaseList=TestMatrixGenarator.instance().getTestList(sMethodName);
			int totCases = TestCaseList.size();
			int totDevice = deviceList.size();
			int totCount=totCases*totDevice;
			Object[][] obj = new Object[totCount][2];
			int iLoop =0;
			
				
					for(int intTCID=0;intTCID<totCases;intTCID++)
					{
						for(int iDevice=0;iDevice<totDevice;iDevice++)
						{
							String testName=TestCaseList.get(intTCID).toString();
							String browserName= deviceList.get(iDevice).getBrowserName();
		    				obj[iLoop][0]= new TestName(browserName + "_"+sMethodName + "[" + testName +"]");
							obj[iLoop][1]= deviceList.get(iDevice);
							
							iLoop++;
						}
											
					}
			 
			
		
				 	
	        return obj;
	    }
/************************End @DataProvider*******************************************/	
	
/************************BeforeSuite*******************************************/
	@BeforeSuite
    public void setupSuite()
    {
 		new Setup();  
   }
/*************************End BeforeSuite**************************************/	
	
/*************************AfterSuite
 * @throws IOException *******************************************/
 	@AfterSuite
 	public void cleanupSuite() throws IOException
 	{
 		String qTestUpdateTestSet = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.updatetestset");
		if (qTestUpdateTestSet.equalsIgnoreCase("yes")) {
		  QTestIntegration.instance().setUpTestSet("No");
		}
		
 		new Cleanup();  
 		
 	}
/*************************End AfterSuite***************************************/

 /*************************AfterSuite*******************************************/
 	/*@BeforeMethod
 	public void BeforeMethod(Method testCase)
 	{
 		String parameters[] =  testCase.getName().split("_");
 		Setup.LoadData(parameters[0], parameters[1]);
 	}*/
/*************************End AfterSuite***************************************/ 	
 	
 
	/*******************************************************************
	*	Name              :	ReportTestFailure
	*	Description       : Used to genarate the report
 	*	Modification Log  :                                                     
	*	Date		Initials     	Description of Modifications 
	********************************************************************/
 	protected void ReportTestFailure(Exception e) throws Exception
 	{
 		
 		long startTime = 000001;
 		
		String currentValue="currentValue";
		//CustomReporting.instance().logFail(e.toString());
		CustomReporting.logReport(e);
		
    	//CR.logFail(e.getCause().toString());
    	//CR.logFail(e.getCause().getMessage().toString());
    	e.printStackTrace();    
    	throw e; 
 	}
 /*************************End Report***************************************/
 	
 	@AfterMethod
 	public void AfterMethod(Method methodName,ITestResult testResult) throws IOException
 	{

 		//String strTestName=GenericFunctions.instance()._getGlobalVariableValue("gvTestName");
 		String strMethodName=methodName.getName();
 		//String strTestMethodName=methodName.toString().split("public void")[1].trim().split("\\(")[0];
 		String strTestMethodName=getQTestScriptName(methodName);
 		String strTestName=GenericFunctions.instance()._getGlobalVariableValue(strMethodName);
 		String strTesCase=strTestName.split("-->")[0];
 		String strDataID=strTesCase.split("\\[")[1].split("\\]")[0];
 		String strBrowser=strTesCase.split("_")[0];
 		String reportPDF = DeviceManager.instance().getConfigurationProperties().getProperty("report.pdf");
 		String qTestUpdateTestResults = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.updateresults");
 		try {
 			CustomPDFReport.instance().createReport(strTesCase, testResult);
 			/*if (reportPDF.equalsIgnoreCase("yes")) {
 			//CustomPDFReport.instance().createReport(strTesCase, testResult);
 			CustomPDFReport.instance().generatePDFReport(strTesCase,strBrowser,testResult);
 			}*/
 			CustomPDFReport.instance().updateTestStatus(strTesCase, strBrowser, testResult);
 			
 			if (qTestUpdateTestResults.equalsIgnoreCase("yes")) {
 				QTestIntegration.instance().updateExecutionLogs(strTestMethodName,strDataID, testResult);
 			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 	
 	public String getQTestScriptName(Method methodName) {
 		
 		String strTestMethodName="";
 		String strTestMethodNameTemp=methodName.toString().split("public void")[1].trim().split("\\(")[0];
 		String[] strTestMethodArr=strTestMethodNameTemp.split("\\.");
 		
 		int iLength=strTestMethodArr.length;
 		for(int i=0;i<strTestMethodArr.length-1;i++) {
 			strTestMethodName=strTestMethodName + "." + strTestMethodArr[i];
 		}
 		//strTestMethodName=strTestMethodName + "#" +strTestMethodArr[iLength-1];
 		strTestMethodName=strTestMethodArr[iLength-1];
 		//strTestMethodName=strTestMethodName.substring(1);
 		return strTestMethodName;
 	}

 	
}
