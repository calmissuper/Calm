package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.xframium.artifact.ArtifactTime;
import org.xframium.artifact.ArtifactType;
import org.xframium.device.ConnectedDevice;
import org.xframium.device.DeviceManager;
import org.xframium.device.artifact.Artifact;
import org.xframium.device.artifact.ArtifactProducer;
import org.xframium.device.data.DataManager;
import org.xframium.device.ng.AbstractSeleniumTest;
import org.xframium.spi.RunDetails;

import com.itextpdf.text.Document;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import au.com.bytecode.opencsv.CSVReader;
import functions.GenericFunctions;

public class CustomPDFReport extends AbstractSeleniumTest{
	
	private static CustomPDFReport singleton = new CustomPDFReport();
	//ThreadLocal<HashMap<String, ConnectedDevice>> threadDevices = new ThreadLocal<HashMap<String, ConnectedDevice>>();
	// private static ThreadLocal<TestContext> threadContext = new ThreadLocal<TestContext>();
	private CustomPDFReport() {

		}

		public static CustomPDFReport instance() {
			return singleton;
		}
		
	
	    public void createReport(String testName,ITestResult testResult) {
	    	
	    	
	    	//HashMap<String, ConnectedDevice> map = getDevicesToCleanUp();
	        //threadContext.set( null );
	        //Iterator<String> keys = ((map != null) ? map.keySet().iterator() : null);
	        
		String DEFAULT = "default";
		WebDriver webDriver = getWebDriver();
        ConnectedDevice device=getConnectedDevice( DEFAULT );
        String runKey=testName;

        File rootFolder = new File( DataManager.instance().getReportFolder(), RunDetails.instance().getRootFolder() );
        rootFolder.mkdirs();
        
        File directory = new File("./");
		String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
        //String strSourcePath=strDirectoryPath+rootFolder.toString()+"\\"+testName;
		String strSourcePath=rootFolder.toString()+"/"+testName;
        String strDestinationPath=DeviceManager.instance().getConfigurationProperties().getProperty("htmlresult.path")+"\\"+testName;
        
        if ( DataManager.instance().getAutomaticDownloads() != null )
        {
            if ( webDriver instanceof ArtifactProducer )
            {
                if ( DataManager.instance().getReportFolder() == null )
                    DataManager.instance().setReportFolder( new File( "." ) );

                for ( ArtifactType aType : DataManager.instance().getAutomaticDownloads() )
                {
                    if ( aType.getTime() == ArtifactTime.AFTER_TEST )
                    {
                        try
                        {
                            Artifact currentArtifact = ((ArtifactProducer) webDriver).getArtifact( webDriver, aType,  device, runKey, testResult.getStatus() == ITestResult.SUCCESS );
                            if ( currentArtifact != null )
                                currentArtifact.writeToDisk( rootFolder );
                        }
                        catch ( Exception e )
                        {
                            log.error( "Error acquiring Artifacts - " + e );
                        }
                    }
                }
            }	
	     }
        
        //copy the result files to the provided path if test case has passed
        if (!strDestinationPath.contains("null")) {
         if (testResult.isSuccess()) {
        	copyResultFiles(strSourcePath,strDestinationPath);
		  }
        }
		
        //chnages for qTest Integration
        String strBrowser=testName.split("\\_")[0];
        String qTestHTMLReportPath=strSourcePath+"\\"+strBrowser+"\\"+testName+".html";
        String strTCID=testName.split("\\[")[1].split("\\]")[0];
        GenericFunctions.instance()._addToGlobalVariableList(strTCID+"_"+"Report", qTestHTMLReportPath);
	   }
	
	public void generatePDFReport(String strTesCase,String strBrowser,ITestResult testResult ) throws URISyntaxException
    {
		try {
		      
			File directory = new File("./");
			System.out.println(directory.getAbsolutePath());
			String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
			String strTestStatus="";

			if (testResult.isSuccess()) {
				strTestStatus="PASS";
			}
			else {
				strTestStatus="FAIL";
			}
			
			String strArtifactpath=DataManager.instance().getReportFolder().getPath()+"\\"+RunDetails.instance().getRootFolder().toString();
			String strCSVFilePath=strDirectoryPath+strArtifactpath+"\\"+strTesCase+"\\"+strBrowser+"\\"+strTesCase+".csv";
			String strPDFPath=strDirectoryPath+strArtifactpath+"\\"+strTesCase+"\\"+strBrowser+"\\"+strTesCase+".pdf";
			
	        CSVReader reader = new CSVReader(new FileReader(strCSVFilePath));
	        /* Variables to loop through the CSV File */
	        String [] nextLine; /* for every line in the file */            
	        int lnNum = 0; /* line number */
	        /* Step-2: Initialize PDF documents - logical objects */
	        Document my_pdf_data = new Document();
	        PdfWriter.getInstance(my_pdf_data, new FileOutputStream(strPDFPath));
	        my_pdf_data.open();  
	        
	        PdfPTable testCase_table=new PdfPTable(2);
	        PdfPCell tesCase_table_cell;
	        tesCase_table_cell=new PdfPCell(new Phrase("TestCase: "+strTesCase));
	        testCase_table.addCell(tesCase_table_cell);
	        
	        tesCase_table_cell=new PdfPCell(new Phrase("Status: "+strTestStatus));
	        testCase_table.addCell(tesCase_table_cell);
	        my_pdf_data.add(testCase_table); 
	        
	        PdfPTable my_first_table = new PdfPTable(6);
	        PdfPCell table_cell;
	        
	        table_cell=new PdfPCell(new Phrase("Page"));
	        my_first_table.addCell(table_cell);
	        table_cell=new PdfPCell(new Phrase("Element"));
	        my_first_table.addCell(table_cell); 
	        table_cell=new PdfPCell(new Phrase("Step"));
	        my_first_table.addCell(table_cell); 
	        table_cell=new PdfPCell(new Phrase("TimeStamp"));
	        my_first_table.addCell(table_cell); 
	        table_cell=new PdfPCell(new Phrase("Time in ms"));
	        my_first_table.addCell(table_cell); 
	        table_cell=new PdfPCell(new Phrase("Status"));
	        my_first_table.addCell(table_cell);
	        /* Step -3: Loop through CSV file and populate data to PDF table */
	        while ((nextLine = reader.readNext()) != null) {
	                lnNum++;        
	                table_cell=new PdfPCell(new Phrase(nextLine[4]));
	                my_first_table.addCell(table_cell);
	                table_cell=new PdfPCell(new Phrase(nextLine[5]));
	                my_first_table.addCell(table_cell); 
	                table_cell=new PdfPCell(new Phrase(nextLine[6]));
	                my_first_table.addCell(table_cell); 
	                table_cell=new PdfPCell(new Phrase(nextLine[7]));
	                my_first_table.addCell(table_cell); 
	                table_cell=new PdfPCell(new Phrase(nextLine[8]));
	                my_first_table.addCell(table_cell); 
	                table_cell=new PdfPCell(new Phrase(nextLine[9]));
	                my_first_table.addCell(table_cell); 
	        }
	        /* Step -4: Attach table to PDF and close the document */
	        my_pdf_data.add(my_first_table);                       
	        my_pdf_data.close(); 
	        reader.close();
	        System.out.println("PDF report generated successfully");
	        CustomReporting.instance().logReport("<a href=\"" + strPDFPath + "\"" + ">Click here to view the PDF report</a>");
		     } 
		catch (Exception ex )
		    {
			System.out.println(ex);
		    } 
       }
	
	public void updateTestStatus(String strTestCase,String strBrowser,ITestResult testResult ) throws URISyntaxException
    {
		try {
			
			File directory = new File("./");
			System.out.println(directory.getAbsolutePath());
			String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
			int intStepFailureCount=0;
			//String strBundlePath=strDirectoryPath+"resources\\Images";
			
			String resultFilePath = DeviceManager.instance().getConfigurationProperties().getProperty("result.xlsx");
			
			if (resultFilePath.contains("resources")) {
				resultFilePath=strDirectoryPath+resultFilePath;
			}
			

			String strTestStatus="";

			if (testResult.isSuccess()) {
				strTestStatus="PASS";
				intStepFailureCount=getTestStepStatus(strTestCase,strBrowser,testResult );
			}
			else {
				strTestStatus="FAIL";
			}
			
			Long strStartTimeinMilliSec=testResult.getStartMillis();
			Long strEndTimeinMilliSec=testResult.getEndMillis();
			Long strExecutionTime=strEndTimeinMilliSec-strStartTimeinMilliSec;
			
			 //chnages for qTest Integration
			String strTCID=strTestCase.split("\\[")[1].split("\\]")[0];
			GenericFunctions.instance()._addToGlobalVariableList(strTCID+"_"+"ExecTime", strExecutionTime.toString());
			
			Long seconds, minutes, hours;
			seconds = strExecutionTime / 1000;
			minutes = seconds / 60;
			seconds = seconds % 60;
			hours = minutes / 60;
			minutes = minutes % 60;
			
			String strExecTime=hours+"h"+" "+minutes+"m"+" "+seconds+"s";
			
			String strArtifactpath=DataManager.instance().getReportFolder().getPath()+"\\"+RunDetails.instance().getRootFolder().toString();
			String strCSVFilePath=strDirectoryPath+strArtifactpath+"\\"+strTestCase+"\\"+strBrowser+"\\"+strTestCase+".csv";
	        CSVReader reader = new CSVReader(new FileReader(strCSVFilePath));
	        String strTimeStamp = reader.readNext()[7];
	        String strHTMLResultPath=strArtifactpath+"\\"+strTestCase+"\\"+strBrowser+"\\"+strTestCase+".html";
	        
	        String[] strArrTimeStamp=strTimeStamp.split("_");
	        int year = Calendar.getInstance().get(Calendar.YEAR);
	        String sDate1=strArrTimeStamp[0].replace("-","/")+"/"+year+" "+strArrTimeStamp[1].replace("-", ":");
	        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	        Date ExcDate = dateFormat.parse(sDate1);
	        
	        FileInputStream file = new FileInputStream(new File(resultFilePath));
		     
		      XSSFWorkbook  workbook = new XSSFWorkbook (file);
		      XSSFSheet sheet = workbook.getSheet("Sheet1");
		      CellStyle cellStyle = workbook.createCellStyle();
		      CreationHelper createHelper = workbook.getCreationHelper();
		      cellStyle.setDataFormat(
		      createHelper.createDataFormat().getFormat("MM/dd/yyyy h:mm"));
		      
		      String strTestID=strTestCase.split("\\[")[1].split("\\]")[0];
		      int intRow=GenericFunctions.instance().findExcelRow(sheet, strTestID,1);
		      int intCol1=GenericFunctions.instance().findExcelCol(sheet,"Execution Status");
		      int intCol2=GenericFunctions.instance().findExcelCol(sheet,"Time Stamp");
		      int intCol3=GenericFunctions.instance().findExcelCol(sheet,"Execution Time");
		      int intCol4=GenericFunctions.instance().findExcelCol(sheet,"Execution Time in Seconds");
		      int intCol5=GenericFunctions.instance().findExcelCol(sheet,"Step Information");
		      int intCol6=GenericFunctions.instance().findExcelCol(sheet,"Link To HTML Report");
		      File fileHTML = new File(strHTMLResultPath);
		      System.out.println(fileHTML.toURI().toString());
		      XSSFCreationHelper helper= workbook.getCreationHelper();
		      XSSFHyperlink HTMLResult_link=(XSSFHyperlink)helper.createHyperlink(Hyperlink.LINK_FILE);
		      HTMLResult_link.setAddress(fileHTML.toURI().toString());
		      
		      sheet.getRow(intRow).createCell(intCol1).setCellValue(strTestStatus);
		      sheet.getRow(intRow).createCell(intCol2).setCellValue(strTimeStamp);
		      sheet.getRow(intRow).createCell(intCol3).setCellValue(strExecTime);
		      sheet.getRow(intRow).createCell(intCol4).setCellValue(strExecutionTime/1000);
		      
		      if (intStepFailureCount!=0){
		    	  sheet.getRow(intRow).createCell(intCol5).setCellValue(intStepFailureCount + " steps have failed. "+"Please check the HTML report for more details.");		    	  
		      }
		      
		      sheet.getRow(intRow).createCell(intCol6).setHyperlink(HTMLResult_link);
		      sheet.getRow(intRow).createCell(intCol6).setCellValue("Click here to open the HTML result");
		      
		      FileOutputStream outFile =new FileOutputStream(new File(resultFilePath));
		      workbook.write(outFile);
		      file.close();
		      outFile.close();
		      workbook.close();
		      reader.close();
	        System.out.println("Test status updated successfully");
	        
		     } 
		catch (Exception ex )
		    {
			System.out.println(ex);
		    } 
       }
	
	public void copyResultFiles(String strSourcePath, String strDestinationPath) {
		
		File srcFolder = new File(strSourcePath);
    	File destFolder = new File(strDestinationPath);

    	//make sure source exists
    	if(!srcFolder.exists()){

           System.out.println("Directory does not exist.");
           //just exit
           System.exit(0);

        }else{

           try{
        	copyFolder(srcFolder,destFolder);
           }catch(IOException e){
        	e.printStackTrace();
        	//error, just exit
             System.exit(0);
           }
        }

    	System.out.println("Done");
		
	   }
	
	public void copyFolder(File src, File dest)
	    	throws IOException{

	    	if(src.isDirectory()){

	    		//if directory not exists, create it
	    		if(!dest.exists()){
	    		   dest.mkdir();
	    		   System.out.println("Directory copied from "
	                              + src + "  to " + dest);
	    		}

	    		//list all the directory contents
	    		String files[] = src.list();

	    		for (String file : files) {
	    		   //construct the src and dest file structure
	    		   File srcFile = new File(src, file);
	    		   File destFile = new File(dest, file);
	    		   //recursive copy
	    		   copyFolder(srcFile,destFile);
	    		}

	    	}else{
	    		//if file, then copy it
	    		//Use bytes stream to support all file types
	    		    InputStream in = new FileInputStream(src);
	    		    
	    		    if (!src.getName().contains("index")) {
	    		    	
	    	          OutputStream out=new FileOutputStream(dest);

	    	          byte[] buffer = new byte[1024];

	    	          int length;
	    	          //copy the file content in bytes
	    	        
	    	          
	    	           while ((length = in.read(buffer)) > 0){
	    	    	    out.write(buffer, 0, length);
	    	            }
	    	          	    	           
		    	       out.close();
	    	        }
	    		    in.close(); 
	    	        
	    	        System.out.println("File copied from " + src + " to " + dest);
	    	}
	    }
	
	public int getTestStepStatus(String strTesCase,String strBrowser,ITestResult testResult ) throws URISyntaxException
    {
		int intNoOfStepFailure=0;
		try {
		      
			File directory = new File("./");
			System.out.println(directory.getAbsolutePath());
			String strDirectoryPath =directory.getAbsolutePath().split("\\.") [0];
			//String strTestStatus="";
			
			String strArtifactpath=DataManager.instance().getReportFolder().getPath()+"\\"+RunDetails.instance().getRootFolder().toString();
			String strCSVFilePath=strDirectoryPath+strArtifactpath+"\\"+strTesCase+"\\"+strBrowser+"\\"+strTesCase+".csv";
			//String strPDFPath=strDirectoryPath+strArtifactpath+"\\"+strTesCase+"\\"+strBrowser+"\\"+strTesCase+".pdf";
			
	        CSVReader reader = new CSVReader(new FileReader(strCSVFilePath));
	        String [] nextLine; /* for every line in the file */            
            
            String strStepStatus="";
            
	        /* Step -3: Loop through CSV file and populate data to PDF table */
	        while ((nextLine = reader.readNext()) != null) {
	               	                
	        	strStepStatus=nextLine[9];
	        	if (strStepStatus.equals("FAILURE")) {
	        		intNoOfStepFailure=intNoOfStepFailure+1;
	        	}
	            System.out.println(strStepStatus);
	        }
	        System.out.println("Total no of steps failed: " + intNoOfStepFailure);
	        reader.close();
	        
	        
		     } 
		catch (Exception ex )
		    {
			System.out.println(ex);
		    }
		return intNoOfStepFailure;
       }
	   
	
	}
