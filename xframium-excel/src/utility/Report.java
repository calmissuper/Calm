package utility;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

public class Report 
{
private static Report singleton=new Report();
	
	public static Report instance()
	{
		return singleton;
	}
	
	private Report(){}
	
	
	public static ExtentHtmlReporter htmlReporter;
	public static ExtentReports report;
	public static ExtentTest logger;
	public static File htmlfile;
	private static String reqReport = "";
	static LocalDateTime nowdate = LocalDateTime.now();
	static DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("ddMMYYYY_HHmmss");
	public static String date_time = dateformat.format(nowdate);

	public void log(String testname, String strOperation, String message) {
		try {
			switch (strOperation.toUpperCase()) {
			case "CREATE":
				
				
				htmlfile = new File(System.getProperty("user.dir") + "//ExtReports//TestReport_" + date_time + ".html");
				htmlfile.createNewFile();
				htmlReporter = new ExtentHtmlReporter(htmlfile.getAbsolutePath());
				report = new ExtentReports();
				report.attachReporter(htmlReporter);
				System.out.println("Test Report HTML File Created");
				break;
			case "START":
				// htmlReporter.setAppendExisting(true);
				logger = report.createTest(testname);    
				logger.assignAuthor("Manju");
				reqReport = "Report Initiated";

				break;
			case "PASS":
				if (reqReport.equalsIgnoreCase("Report Initiated")) {
					logger.log(Status.PASS, message);
					System.out.println("Test Report writing successfull - PASS");
				}
				break;
			case "FAIL":
				if (reqReport.equalsIgnoreCase("Report Initiated")) 
				{	
					logger.log(Status.FAIL,message);

					// logger.fail(message).addScreenCaptureFromPath("D:\\WS\\Check\\Sample.PNG",
					// "Google");
					// logger.log(Status.FAIL,
					// message+logger.addScreenCaptureFromPath("D:\\WS\\Check\\Sample.PNG",
					// "Google"));
				//	String screenshot_name = System.currentTimeMillis() + ".png";
				//	BufferedImage img = getRootTestObject().getScreenSnapshot();
				//	File file = new File(FailedScreenshot_Path + screenshot_name);
				//	ImageIO.write(img, "png", file);
				//MediaEntityModelProvider mediaModel = MediaEntityBuilder
					//			.createScreenCaptureFromPath(file.getAbsolutePath()).build();
				//	logger.fail(message, mediaModel);
					// file.delete();
					System.out.println("Test Report writing successfull - FAIL");
				}
				break;
			case "INFO":
				if (reqReport.equalsIgnoreCase("Report Initiated")) {
					logger.log(Status.INFO, message);
					System.out.println("Test Report writing successfull - INFO");
				}
				break;
			case "SCREENPRINTS":
				if (reqReport.equalsIgnoreCase("Report Initiated")) 
				{
			//		logger.info("<a href='"+screenShotWordDocPath+"'>Click here to view Screen Prints</a>");
					System.out.println("Test Report writing successfull - External Link to Screen Prints");				
				}
				break;
			case "ERROR":
				if (reqReport.equalsIgnoreCase("Report Initiated")) {
					logger.log(Status.ERROR, message);
					System.out.println("Test Report writing successfull - ERROR");
				}
				break;
			case "FINISH":
				if (reqReport.equalsIgnoreCase("Report Initiated")) {
					report.flush();
					System.out.println("Test Report saved successfully");
				}
				break;

			default:
				System.out.println("Please pass the correct operation string");
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
