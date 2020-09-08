package utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.qas.qtest.api.auth.PropertiesQTestCredentials;
import org.qas.qtest.api.auth.QTestCredentials;
import org.qas.qtest.api.services.attachment.model.Attachment;
import org.qas.qtest.api.services.execution.TestExecutionService;
import org.qas.qtest.api.services.execution.TestExecutionServiceClient;
import org.qas.qtest.api.services.execution.model.AutomationTestLog;
import org.qas.qtest.api.services.execution.model.AutomationTestLogRequest;
import org.qas.qtest.api.services.execution.model.TestLog;
import org.testng.ITestResult;
import org.xframium.device.DeviceManager;
import org.xframium.page.StepStatus;

import com.jayway.restassured.response.Response;

import functions.CustomFunctions;
import functions.GenericFunctions;

public class QTestIntegration {
	
	private static QTestIntegration singleton = new QTestIntegration();

	private QTestIntegration() {}

    public static QTestIntegration instance() {
			return singleton;}
    
    public void updateExecutionLogs(String testName,String strDataID,ITestResult testResult) throws IOException {
     
     long startTime = System.currentTimeMillis();
     
     try {	
    	     	 
    	File directory = new File("./");
		String strQTestPropPath =directory.getAbsolutePath().split("\\.") [0] + "\\resources\\qTestCredentials.properties";
		InputStream input=new FileInputStream(strQTestPropPath);
		
		String strQTestEndPoint = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.endpoint");
		QTestCredentials credentials =new PropertiesQTestCredentials(input);
		TestExecutionService testExecutionService =new TestExecutionServiceClient(credentials);
		testExecutionService.setEndpoint(strQTestEndPoint);
		
		String strProjectID = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.projectid");
		long projectID=Long.parseLong(strProjectID);
		

		String qTestUpdateTestSet = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.updatetestset");
		String qTestCI = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.ci");

		String strTestRunId="";
		if (qTestCI.equalsIgnoreCase("yes")) {
			strTestRunId=getTestRunId(testName); 
		}
		else if(qTestUpdateTestSet.equalsIgnoreCase("no")) {
			strTestRunId=getTestRunId(testName); 
		}
		else {
			strTestRunId=getTestRunIdFromTRFile(testName);
		}
			
		
		//String strTestRunId=getTestRunId(testName);
		long testRunId=Long.parseLong(strTestRunId);
				
		String strTestStatus="SKIP";
		if (testResult.isSuccess()) {
			strTestStatus="PASSED";
		}
		else  {
			strTestStatus="FAILED";
		}

		String attachDocPath1=GenericFunctions.instance()._getGlobalVariableValue(strDataID+"_"+"Report");
		int arrLen=attachDocPath1.split("\\\\").length;
		String doc1Name=attachDocPath1.split("\\\\")[arrLen-1].split("\\.")[0];
	
		String attachDocPath2=DeviceManager.instance().getConfigurationProperties().getProperty("screenshot.path") 
				+ "\\" + GenericFunctions.instance()._getGlobalVariableValue(strDataID)+ ".docx";
		InputStream attachFile1=new FileInputStream(attachDocPath1);
		InputStream attachFile2=new FileInputStream(attachDocPath2);

        Attachment attach1=new Attachment();
        Attachment attach2=new Attachment();
        attach1.withData(attachFile1);
        attach1.withContentType("application/html");
        attach1.withName(doc1Name+".html");
        
        attach2.withData(attachFile2);
        attach2.withContentType("application/docx");
        attach2.withName(GenericFunctions.instance()._getGlobalVariableValue(strDataID)+".docx");
        
        List<Attachment> listAttachment = new ArrayList<Attachment>();
        listAttachment.add(attach1);
        listAttachment.add(attach2);
		
        String strExeTime=GenericFunctions.instance()._getGlobalVariableValue(strDataID+"_"+"ExecTime");
        Date exeStartDate=new Date();
        exeStartDate=new Date(exeStartDate.getTime() - Long.parseLong(strExeTime));
        
		AutomationTestLog automationTestLog =

				  new AutomationTestLog()
				    //.withExecutionStartDate(new Date())
				    .withExecutionStartDate(exeStartDate)
				    .withExecutionEndDate(new Date())
				    .withName(testName)
				    .withStatus(strTestStatus)
				    .withSystemName("TestNG")
				    .withAttachments(listAttachment);
		
		AutomationTestLogRequest automationTestLogRequest =

				  new AutomationTestLogRequest()
				    .withProjectId(projectID)
				    .withTestRunId(testRunId)
				    .withAutomationTestLog(automationTestLog);
		
		TestLog testLog =testExecutionService.submitAutomationTestLog(automationTestLogRequest);
		
		CustomReporting.logReport("","", "Test Logs updated successfully in qTest for the script: ["+testName+"]", "", StepStatus.SUCCESS,
				new String[] { }, startTime, null);
     }
     catch(Exception ex) {
    	 CustomReporting.logReport("","", "Failed to update Test Logs in qTest for the script: ["+testName+"]", "", StepStatus.FAILURE,
					new String[] { }, startTime, ex);
     }
    }
    
 //////////////////////////////////////////////////////////////////////////////////////////////////////////   
    public String getTestRunId(String testName) {
    	long startTime = System.currentTimeMillis();
    	String strTestRunId="";
    	try {
    		
    		Properties prop = new Properties();
			File directory = new File("./");
			String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
			InputStream input=new FileInputStream(strDirectoryPath+"\\resources\\qTestCredentials.properties");
			prop.load(input);
            String strAuthKey=prop.getProperty("token");
            
            String srrQTestServer = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.url").trim();
            String strProjectID = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.projectid").trim();
    		String strURI=srrQTestServer + "/api/v3/projects/"+ strProjectID + "/search/";
    		String strTestCycle = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testcycle").trim();
    		
    		
    		//String strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite").trim();
    		
    		String strTestSuite="";
    		String env=GenericFunctions.instance()._getGlobalVariableValue("PARAMETER_ENV");
    		
    		if (env.equalsIgnoreCase("DEV")) {
    			strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.dev").trim();
              } 
              else if(env.equalsIgnoreCase("STAGE")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.stage").trim(); 
              }
              else if(env.equalsIgnoreCase("ERACPAPT1")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.qa1").trim(); 
              }
              else if(env.equalsIgnoreCase("ERACPAPT2")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.qa2").trim();
              }
              else if(env.isEmpty()) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite").trim();
              }
              else if(env.equalsIgnoreCase("QA")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.qa").trim();
              }
    		
    		
    		String strContentType="application/json";
    		String strQuery="'Test Cycle' = '" + strTestCycle + "' and 'Test Suite' = '" + strTestSuite + "' and 'Name' = '"
    				+ testName +"'";
    		String strInputJson="{\r\n" + 
    				"  \"object_type\": \"test-runs\",\r\n" + 
    				"  \"fields\": [\r\n" + 
    				"    \"id\"\r\n" + 
    				"  ],\r\n" + 
    				"  \"query\": \"" + strQuery + "\"\r\n" + 
    				"}";
    		System.out.println(strInputJson);
    		Response response = CustomFunctions.instance().customWebServiceCall(strInputJson, strAuthKey, strContentType, strURI);
    		//Response response = CustomFunctions.instance().webServiceCall(strJsonName, strAuthKey, strContentType, strURI);
    		if (response != null ) {
    			int code = response.statusCode();
    			String statusLine = response.statusLine();
    			String resbody = response.asString();
    			System.out.println(resbody);
    			//strTestRunId = CustomFunctions.instance().getKeyValue(response, "items");
    			strTestRunId =resbody.split("\"id\":")[1].split("}")[0];
    			System.out.println(strTestRunId);
    			//CustomReporting.logReport("","", "TestRun ID retrieved successfully from qTest for the script: ["+testName+"]", "", StepStatus.SUCCESS,
    				//	new String[] { }, startTime, null);
    		}
    		else {
    			CustomReporting.logReport("","", "Failed to retrieve TestRun id from qTest for the script: ["+testName+"]", "", StepStatus.FAILURE,
    					new String[] { }, startTime, null);
    			System.out.println("Response retrieved is null");
    		}
    	}
    	catch(Exception ex) {
    		CustomReporting.logReport("","", "Failed to retrieve TestRun id from qTest for the script: ["+testName+"]", "", StepStatus.FAILURE,
					new String[] { }, startTime, ex);
    		ex.printStackTrace();
    	}
    	return strTestRunId;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public String getTestRunIdFromTRFile(String strTestCaseName) {
    	
    	long startTime = System.currentTimeMillis();
    	String strTestRunId="";
        try {
        	String[] arrTestCaseName=null;
    		File directory = new File("./");
    		String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
        	File file = new File(strDirectoryPath + "\\TR_Details.txt");
    		BufferedReader br = new BufferedReader(new FileReader(file));		 
    		String strLineValTemp;
    		String strLineVal = "";
    		 
    	    while ((strLineValTemp = br.readLine()) != null) {
    	    	strLineVal=strLineVal + strLineValTemp;
    		    System.out.println(strLineVal);
    		}
    	    br.close();
    	    arrTestCaseName=strLineVal.split("}");
    	    String strTestCaseNameTemp="";
    	    
    	    for(int i=0;i<arrTestCaseName.length-1;i++) {
    	    	
    	    	strTestCaseNameTemp=arrTestCaseName[i].split("\"Name\":\"")[1].split("\"")[0];
        	    
        	    if (strTestCaseNameTemp.equals(strTestCaseName)) {
        	    	strTestRunId=arrTestCaseName[i].split("\"Id\":\"")[1].split("\"")[0];
        	    	break;
        	    }
    	    	
    	    }
    	    
    		
    	}
    	catch(Exception ex) {
    		CustomReporting.logReport("","", "Error occurred while retrieving Test Run Id from TR_Details file for Test Case: " + strTestCaseName, "", StepStatus.FAILURE,
					new String[] { }, startTime, ex);
    	}

        if (strTestRunId.isEmpty()){
        	CustomReporting.logReport("","", "Test Run Id retrieved for Test Case: " + strTestCaseName + " is null. Please check the TR_Details file if the test case is available.", "", StepStatus.FAILURE,
					new String[] { }, startTime,new RuntimeException());
        }
    	return strTestRunId;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    public static ArrayList<String> getTestRunNames() {
    	long startTime = System.currentTimeMillis();
    	//String strTestRunId="";
    	ArrayList<String> TestCaseNameList= new ArrayList<String>();
    	try {
    		
    		Properties prop = new Properties();
			File directory = new File("./");
			String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
			InputStream input=new FileInputStream(strDirectoryPath+"\\resources\\qTestCredentials.properties");
			prop.load(input);
            String strAuthKey=prop.getProperty("token");
            
            String srrQTestServer = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.url").trim();
            String strProjectID = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.projectid").trim();
    		String strURI=srrQTestServer + "/api/v3/projects/"+ strProjectID + "/search/";
    		
    		String strTestCycle = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testcycle").trim();
    		String strTestSuite="";
    		String env=GenericFunctions.instance()._getGlobalVariableValue("PARAMETER_ENV");
    		
    		if (env.equalsIgnoreCase("DEV")) {
    			strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.dev").trim();
              } 
              else if(env.equalsIgnoreCase("STAGE")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.stage").trim(); 
              }
              else if(env.equalsIgnoreCase("ERACPAPT1")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.qa1").trim(); 
              }
              else if(env.equalsIgnoreCase("ERACPAPT2")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.qa2").trim();
              }
              else if(env.equalsIgnoreCase("QA")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.qa").trim();
              }
    		 		   		
    		String strContentType="application/json";
    		String strQuery="'Test Cycle' = '" + strTestCycle + "' and 'Test Suite' = '" + strTestSuite + "'";
    		String strInputJson="{\r\n" + 
    				"  \"object_type\": \"test-runs\",\r\n" + 
    				"  \"fields\": [\r\n" + 
    				"    \"name\"\r\n" + 
    				"  ],\r\n" + 
    				"  \"query\": \"" + strQuery + "\"\r\n" + 
    				"}";
    		System.out.println(strInputJson);
    		Response response = CustomFunctions.instance().customWebServiceCall(strInputJson, strAuthKey, strContentType, strURI);
    		//Response response = CustomFunctions.instance().webServiceCall(strJsonName, strAuthKey, strContentType, strURI);
    		if (response != null ) {
    			int code = response.statusCode();
    			String statusLine = response.statusLine();
    			String resbody = response.asString();
    			System.out.println(resbody);
    			
    			//ArrayList<String> TestCaseNameList= new ArrayList<String>();
    			String strTestTestCaseArr[] =resbody.split("\"name\":");
    			String TestCaseNameTemp="";
    			for(int i=1;i<strTestTestCaseArr.length;i++) {
    				TestCaseNameTemp=strTestTestCaseArr[i].split("}")[0].replace("\"", "");
    				TestCaseNameList.add(TestCaseNameTemp);
    			}
    			//strTestRunId =resbody.split("\"id\":")[1].split("}")[0];
    			//System.out.println(strTestRunId);
    			
    		}
    		else {
    			CustomReporting.logReport("","", "Failed to retrieve TestRun Names from qTest", "", StepStatus.FAILURE,
    					new String[] { }, startTime, null);
    			System.out.println("Response retrieved is null");
    		}
    	}
    	catch(Exception ex) {
    		CustomReporting.logReport("","", "Failed to retrieve Test Run Names from qTest", "", StepStatus.FAILURE,
					new String[] { }, startTime, ex);
    		ex.printStackTrace();
    	}
    	return TestCaseNameList;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static ArrayList<String> getTestRunNamesLoadBalance() {
    	long startTime = System.currentTimeMillis();
    	//String strTestRunId="";
    	ArrayList<String> TestCaseNameList= new ArrayList<String>();
    	try {
    		
    		Properties prop = new Properties();
			File directory = new File("./");
			String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
			InputStream input=new FileInputStream(strDirectoryPath+"\\resources\\qTestCredentials.properties");
			prop.load(input);
            String strAuthKey=prop.getProperty("token");
            
            String srrQTestServer = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.url").trim();
            String strProjectID = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.projectid").trim();
    		String strURI=srrQTestServer + "/api/v3/projects/"+ strProjectID + "/search/";
    		
    		String strTestCycle = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testcycle").trim();
    		String strTestSuite="";
    		String env=GenericFunctions.instance()._getGlobalVariableValue("PARAMETER_ENV");
    		int load=Integer.parseInt(GenericFunctions.instance()._getGlobalVariableValue("PARAMETER_LOAD"));
    		int noOfNodes=Integer.parseInt(DeviceManager.instance().getConfigurationProperties().getProperty("ci.noofnodes"));
    		
    		if (env.equalsIgnoreCase("DEV")) {
    			strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.dev").trim();
              } 
              else if(env.equalsIgnoreCase("STAGE")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.stage").trim(); 
              }
              else if(env.equalsIgnoreCase("ERACPAPT1")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.qa1").trim(); 
              }
              else if(env.equalsIgnoreCase("ERACPAPT2")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.qa2").trim();
              }
              else if(env.equalsIgnoreCase("QA")) {
            	  strTestSuite = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.testsuite.qa").trim();
              }
    		 		   		
    		String strContentType="application/json";
    		String strQuery="'Test Cycle' = '" + strTestCycle + "' and 'Test Suite' = '" + strTestSuite + "'";
    		String strInputJson="{\r\n" + 
    				"  \"object_type\": \"test-runs\",\r\n" + 
    				"  \"fields\": [\r\n" + 
    				"    \"name\"\r\n" + 
    				"  ],\r\n" + 
    				"  \"query\": \"" + strQuery + "\"\r\n" + 
    				"}";
    		System.out.println(strInputJson);
    		Response response = CustomFunctions.instance().customWebServiceCall(strInputJson, strAuthKey, strContentType, strURI);
    		//Response response = CustomFunctions.instance().webServiceCall(strJsonName, strAuthKey, strContentType, strURI);
    		if (response != null ) {
    			int code = response.statusCode();
    			String statusLine = response.statusLine();
    			String resbody = response.asString();
    			System.out.println(resbody);
    			
    			//ArrayList<String> TestCaseNameList= new ArrayList<String>();
    			String strTestTestCaseArr[] =resbody.split("\"name\":");
    			
    			int intArrLen=strTestTestCaseArr.length-1;
    			int x=0;
    			int y=0;
    			
    			if (load==1) {
    				x=1;
    			}
    			else {
    				x=(intArrLen/noOfNodes)*(load-1) +1;
    			}
    			
    			if (load!=noOfNodes) {
    				y=(intArrLen/noOfNodes)*load;
    			}
    			else {
    				y=intArrLen;
    			}
    			//load
    			//noOfNodes
    			
    			String TestCaseNameTemp="";
    			for(int i=x;i<=y;i++) {
    				TestCaseNameTemp=strTestTestCaseArr[i].split("}")[0].replace("\"", "");
    				TestCaseNameList.add(TestCaseNameTemp);
    			}
    			//strTestRunId =resbody.split("\"id\":")[1].split("}")[0];
    			//System.out.println(strTestRunId);
    			
    		}
    		else {
    			CustomReporting.logReport("","", "Failed to retrieve TestRun Names from qTest", "", StepStatus.FAILURE,
    					new String[] { }, startTime, null);
    			System.out.println("Response retrieved is null");
    		}
    	}
    	catch(Exception ex) {
    		CustomReporting.logReport("","", "Failed to retrieve Test Run Names from qTest", "", StepStatus.FAILURE,
					new String[] { }, startTime, ex);
    		ex.printStackTrace();
    	}
    	return TestCaseNameList;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void setUpTestSet(String strVal) throws IOException {
    		    
	    String[] arrTestCaseNames=getTestCaseNames();
	    _writeToExcel("Sheet1","Execute",strVal,arrTestCaseNames);
	    
    }
    
    public String[] getTestCaseNames() {
    	long startTime = System.currentTimeMillis();
    	String[] arrTestCaseName=null;
    	try {
    		
    		writeTestRunDetails();
    		
    		File directory = new File("./");
    		String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
        	File file = new File(strDirectoryPath + "\\TR_Details.txt");
    		BufferedReader br = new BufferedReader(new FileReader(file));		 
    		String strLineValTemp;
    		String strLineVal = "";
    		
    		//"Name":"com.everest.test.submission.TestDriver_Escape_Newsubmission#ESC_Renewal_NS" 
    	    while ((strLineValTemp = br.readLine()) != null) {
    	    	strLineVal=strLineVal + strLineValTemp;
    		    System.out.println(strLineVal);
    		}
    	    br.close();
    	    arrTestCaseName=strLineVal.split("}");
    	    
    	    for(int i=0;i<arrTestCaseName.length-1;i++) {
    	    	
        	    //arrTestCaseName[i]=arrTestCaseName[i].split("\"Name\":\"")[1].split("\"")[0].split("#")[1];
        	    arrTestCaseName[i]=arrTestCaseName[i].split("\"Name\":\"")[1].split("\"")[0];
    	    	
    	    }
    	    
    	   // String strTestCaseName=strLineVal.split("\"Name\":\"")[1].split("\"")[0].split("#")[1];
    		
    	}
    	catch(Exception ex) {
    		CustomReporting.logReport("","", "Error occurred while retrieving Test Names from qTest", "", StepStatus.FAILURE,
					new String[] { }, startTime, ex);
    	}
    	return arrTestCaseName;
    }
    
    
public static void writeTestRunDetails() throws IOException {
		
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			
			File directory = new File("./");
			String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
			String FILENAME = strDirectoryPath + "TR_Details.txt";
			System.setOut(new PrintStream(new FileOutputStream(strDirectoryPath+"Log.txt")));
			
			Properties prop = new Properties();
			InputStream input1=new FileInputStream(strDirectoryPath+"resources\\qTestCredentials.properties");
			prop.load(input1);
            String strAuthKey=prop.getProperty("token").trim();
			
			String strURI=System.getenv("QTE_SCHEDULED_TX_DATA");
			System.out.println("URI is: "+strURI+"\n");
			String strContentType="application/json";
			Response response =CustomFunctions.instance().customWebServiceCall(strAuthKey, strContentType, strURI);

	        String TR_Details="";
			
			if (response != null ) {
				int code = response.statusCode();
				String statusLine = response.statusLine();
				String resbody = response.asString();
				System.out.println("\n Response Body is: \n");
				System.out.println(resbody);
				
				TR_Details=resbody.split(",\"testSuite\"")[0].split("\"testRuns\":")[1];
				System.out.println("\n TR_Details is: \n");
				System.out.println(TR_Details);
				

				fw = new FileWriter(FILENAME);
				bw = new BufferedWriter(fw);
				bw.write(TR_Details);

				System.out.println("Done");	
				
			}
			else {   			
				System.out.println("Response retrieved is null");   			
			}
			
		}
		catch(Exception ex) {
			
		}
		finally {
			bw.close();
			fw.close();	
		}
		
	}

    public void _writeToExcel(String Sheetname,String strColName,String strVal,String[] strTCNames) throws IOException
    {
		long startTime = System.currentTimeMillis();
		FileInputStream file=null;
		XSSFWorkbook  workbook=null;
		FileOutputStream outFile=null;
    try {
      
      String writeExcelPath = DeviceManager.instance().getConfigurationProperties().getProperty("testset.path");
      file = new FileInputStream(new File(writeExcelPath));
     
      workbook = new XSSFWorkbook (file);
      XSSFSheet sheet = workbook.getSheet(Sheetname);
      
      int intCol=findExcelCol(sheet,strColName);
      
      if (strVal.equalsIgnoreCase("yes")){
    	  int numRow=sheet.getLastRowNum();					
		  for(int i=1;i<=numRow;i++){
			  sheet.getRow(i).createCell(intCol).setCellValue("No");
          }
      }
      
      for(int i=0;i<strTCNames.length-1;i++) {
    	  int intRow=findExcelRow(sheet,strTCNames[i]);   
    	  sheet.getRow(intRow).createCell(intCol).setCellValue(strVal);
      }
      outFile =new FileOutputStream(new File(writeExcelPath));
      workbook.write(outFile);
      //file.close();
      //outFile.close();
      //CustomReporting.logReport("","", strVal+" : written to Excel Successfully",StepStatus.SUCCESS,new String[] {},startTime);

  } catch (FileNotFoundException e) {
      e.printStackTrace();
      CustomReporting.logReport("","", strVal+" : writing to Excel failed","", StepStatus.FAILURE,
				new String[] {}, startTime, e);
  	  
  } catch (IOException e) {
      e.printStackTrace();
      CustomReporting.logReport("","", strVal+" : writing to Excel failed","", StepStatus.FAILURE,
				new String[] { }, startTime, e);
  
  }
    finally {
    	file.close();
        outFile.close();
    }

}
	
	public int findExcelCol(XSSFSheet sheet, String strColName)
	{
		int colNum=0;
		try {

			
			String colVal=sheet.getRow(0).getCell(colNum).getStringCellValue();
			
			while(!colVal.isEmpty()){
				if(colVal.equalsIgnoreCase(strColName)){
					break;
				}
				else
				{
				   colNum=colNum+1;
				   colVal=sheet.getRow(0).getCell(colNum).getStringCellValue();
				}

			}
						   
		    
		} catch (Exception e) {
			e.printStackTrace();
            
        }
		
		return colNum;
	}


	public int findExcelRow(XSSFSheet sheet, String strTCID)
	{
		int intRowNum=0;
		try {

			int numRow=sheet.getLastRowNum();		
			
			for(int i=0;i<=numRow;i++){
				 
				String cellVal=sheet.getRow(i).getCell(0).getStringCellValue();
				if (!cellVal.isEmpty() && cellVal.contains(strTCID)){
					break;
				}
				else
				{
					intRowNum=intRowNum+1;
				}
			 
			  }
		   
		    
		} catch (Exception e) {
			e.printStackTrace();
            
        }
		
		return intRowNum;
	}
	

}