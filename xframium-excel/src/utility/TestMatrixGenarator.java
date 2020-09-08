package utility;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xframium.device.DeviceManager;

public class TestMatrixGenarator {
	
	public static HashMap<String, ArrayList<String>>  testMatrixList = new HashMap<>();
	private static TestMatrixGenarator singleton = new TestMatrixGenarator();
	
	public static TestMatrixGenarator instance()
	{
		return singleton;
	}
	
	private TestMatrixGenarator()
	{
		
	}
	
	public void genarateTestMatrixList() throws Exception
	{
		String url=DeviceManager.instance().getConfigurationProperties().getProperty("testset.path");
		String qTestCI=DeviceManager.instance().getConfigurationProperties().getProperty("qtest.ci");
		
		if (url.contains("accdb")){
		
		 PreparedStatement pstmt = null;
		 ResultSet rs = null;
		 String query = "Select Distinct(TestScriptName)from tbl_TestMappingMatrix order by TestScriptName";
				
		 String driver="net.ucanaccess.jdbc.UcanaccessDriver";
		 Connection conn=null;
		
		 conn=TestMatrixGenarator.getMSAccessConnection(url, driver);
		
		 pstmt = conn.prepareStatement(query);
		 rs = pstmt.executeQuery();
		//rs.next();
			
		 while(rs.next())
		 {
			String testScriptName =rs.getObject("TestScriptName").toString();
			ArrayList<String> dataSetNameList= new ArrayList<String>();
			dataSetNameList.clear();
			
			//Get List of Data Set for the Script
			//String query1 = "Select * from [tbl_TestMappingMatrix] where TestScriptName='" + testScriptName +"'";
			String query1 = "Select * from [tbl_TestMappingMatrix] where TestScriptName='" + testScriptName +"'" + " and Execute in ('Yes','yes')";
			PreparedStatement pstmt1 = conn.prepareStatement(query1);
			ResultSet rsDataSet = pstmt1.executeQuery();
			while(rsDataSet.next())
			{
				String datSetName =rsDataSet.getObject("DataSetName").toString();
				dataSetNameList.add(datSetName);
			}
			testMatrixList.put(testScriptName.toUpperCase(),dataSetNameList);
			
		 }
		 
		}
		else if (url.contains("xlsx")) {
		  if (qTestCI.toLowerCase().contains("no")){
			  
			HashMap<String, String> tempMap = new HashMap<>();
			
			File directory = new File("./");
			System.out.println(directory.getAbsolutePath());
			String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
			String strFilePath=url;
						
			if (url.contains("resources")) {
				strFilePath=strDirectoryPath+url;

				strFilePath= "/Users/manjunathda/.jenkins/workspace/BIGB_GIT/xframium-excel/resources/TestSet.xlsx";
			}
			
			FileInputStream file = new FileInputStream(new File(strFilePath));
		     
		    XSSFWorkbook  workbook = new XSSFWorkbook (file);
		    XSSFSheet sheet = workbook.getSheet("Sheet1");
		    int numRow=sheet.getLastRowNum();
		    
		    for(int i=1;i<=numRow;i++){

				//String strTesScriptName=sheet.getRow(i).getCell(0).getStringCellValue();
				String strTesScriptName=sheet.getRow(i).getCell(0,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
				
				if (!strTesScriptName.isEmpty() && !tempMap.containsKey(strTesScriptName)) {
				tempMap.put(strTesScriptName, "");
				ArrayList<String> dataSetNameList= new ArrayList<String>();
				dataSetNameList.clear();
				
				for(int j=1;j<=numRow;j++){

					String strTestName=sheet.getRow(j).getCell(0,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
					String strDataSetName=sheet.getRow(j).getCell(1,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
					String strExecuteFlag=sheet.getRow(j).getCell(2,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
					
					 if (!strTestName.isEmpty()) {
					  if (tempMap.containsKey(strTestName) && tempMap.get(strTestName).isEmpty()) 
			            {
						  if(strExecuteFlag.equalsIgnoreCase("yes")) {
						   dataSetNameList.add(strDataSetName);
						  }
			            }				
					 }				 
				  }
				testMatrixList.put(strTesScriptName.toUpperCase(),dataSetNameList);
				//tempMap.clear();	
				tempMap.put(strTesScriptName, "DataSet Added");
			  }
		    }
		    workbook.close();
		   }
		
		  else if (qTestCI.toLowerCase().contains("yes")) {
			ArrayList<String> TestCaseNameList=QTestIntegration.getTestRunNames();
			
			File directory = new File("./");
			System.out.println(directory.getAbsolutePath());
			String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
			
			String strFilePath=url;
			
			if (url.contains("resources")) {
				strFilePath=strDirectoryPath+url;
			}
			
			
			FileInputStream file = new FileInputStream(new File(strFilePath));
		     
		    XSSFWorkbook  workbook = new XSSFWorkbook (file);
		    XSSFSheet sheet = workbook.getSheet("Sheet1");
		    int numRow=sheet.getLastRowNum();
		    String TestCaseName="";

		    for(int i=0;i<TestCaseNameList.size();i++) {
		    	TestCaseName=TestCaseNameList.get(i).toString();
		    	ArrayList<String> dataSetNameList= new ArrayList<String>();
				dataSetNameList.clear();
		    	for(int j=1;j<=numRow;j++){
		    		String strTesScriptName=sheet.getRow(j).getCell(0,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
		    		String strDataSetName=sheet.getRow(j).getCell(1,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
		    		if (!strTesScriptName.isEmpty() && strTesScriptName.equals(TestCaseName) ){
		    			dataSetNameList.add(strDataSetName);
		    		}
		    	}
		    	testMatrixList.put(TestCaseName.toUpperCase(),dataSetNameList);
		    }
		    workbook.close();
		  }
		}	
	}
	
	/*public HashMap<String, ArrayList<String>> getTestMatrixList()
	{
		return testMatrixList;
		
	}*/
	
	
	public ArrayList<String> getTestList(String sMethodName)
	{
		ArrayList<String> returnList = new ArrayList<String>();
		String strKey = sMethodName.toUpperCase();
		if(testMatrixList.containsKey(strKey)) 
			returnList = testMatrixList.get(strKey);
		//else
		//	returnList.add(sMethodName);
			
		return returnList;
		
		
		
	}
	
	private static Connection getMSAccessConnection( String url,String driver) throws Exception
    {
        Connection conn1 = null;

        Class.forName( driver );

        conn1 = DriverManager.getConnection(url);

     //   TestMatrixGenarator.instance().genarateTestMatrixList(conn1);

      return conn1;
   }

}
