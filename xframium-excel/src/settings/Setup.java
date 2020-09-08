package settings;

import java.io.File;

import org.xframium.device.DeviceManager;
import org.xframium.device.ng.AbstractSeleniumTest;
import org.xframium.page.PageManager;
import org.xframium.page.data.PageDataManager;
import org.xframium.page.listener.LoggingExecutionListener;

import utility.CustomExcelPageDataProvider;
import utility.CustomSQLElementProvider;
import utility.CustomSQLPageDataProvider;
import utility.QTestIntegration;
import utility.TestMatrixGenarator;


public class Setup  extends AbstractSeleniumTest{
	
	
	public Setup()  {
		log.info("Setup started..");
		
		String directory = System.getProperty("user.dir");
		System.out.println(directory);
		File configFile = new File(directory+"/resources/driverConfig.txt");
		if (!configFile.exists()) {
			System.err.println("[" + configFile.getAbsolutePath() + "] could not be located");
			System.exit(-1);
		}

		try {
			
			new JavaTXTConfigurationReader().readConfiguration(configFile, false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//LoadRepository();
		LoadData();

		try {
			
			String qTestUpdateTestSet = DeviceManager.instance().getConfigurationProperties().getProperty("qtest.updatetestset");
			if (qTestUpdateTestSet.equalsIgnoreCase("yes")) {
			  QTestIntegration.instance().setUpTestSet("Yes");
			}
			TestMatrixGenarator.instance().genarateTestMatrixList();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("Setup done..");
	}
	
	
	public static void LoadRepository()
	{
		        		
		 String driverClassName="net.ucanaccess.jdbc.UcanaccessDriver";
		 String  url = "";
		 String  username="";
		 String  password="";
		 
		 
		 url=DeviceManager.instance().getConfigurationProperties().getProperty("pr.url");
        
		 String default_qyuery ="SELECT PS.NAME,PP.NAME,PE.NAME, PE.DESCRIPTOR, PE.VALUE, PE.CONTEXT_NAME "
        		+ "FROM SITES PS INNER JOIN PAGES PP ON PP.SITE_NAME = PS.NAME INNER JOIN ELEMENTS PE ON PE.PAGE_NAME = PP.NAME "
        		+ "ORDER BY PS.NAME, PP.NAME" ;
     	        		
         PageManager.instance().setSiteName( "Escape" );
	     PageManager.instance().registerExecutionListener( new LoggingExecutionListener() );
	     PageManager.instance().setElementProvider( new CustomSQLElementProvider( username,password,url,driverClassName,default_qyuery));
	    
	}	
	
	
	public static void LoadData(String lob,String strDB, String testName)
	{
		String url="";
		String username="";
        String driverClassName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
        if (strDB.equalsIgnoreCase("RTD")){
        	url= "jdbc:sqlserver://p4sst006:1433;databaseName=QCTest;integratedSecurity=false";
        	username="RTDDB";
		 }
		 else if (strDB.equalsIgnoreCase("RR")){

			 url="jdbc:sqlserver://p4sst006:1433;databaseName=QCTEST;integratedSecurity=false"; 
			 username="RRDB";
		 }
		 else if (strDB.equalsIgnoreCase("PT")){

			 url="jdbc:sqlserver://p4sst006:1433;databaseName=QCTest;integratedSecurity=false"; 
			 username="FTDB";
		 }

        //String username="";
        String password="Pa$$w0rd";
        		
        PageDataManager.instance().setPageDataProvider( new CustomSQLPageDataProvider(username, password, url, driverClassName,lob,testName));
 	}
	
	public static void LoadData()
	{
		
       //data provider for reading data from excel
       // PageDataManager.instance().setPageDataProvider( new ExcelPageDataProvider(new File("resources/TestData.xlsx"),"Login,Account,NewSubmission,AddPolicy,NewBusiness,Search"));
        PageDataManager.instance().setPageDataProvider( new CustomExcelPageDataProvider(new File("resources/TestData.xlsx"),"All"));
 	}
}
