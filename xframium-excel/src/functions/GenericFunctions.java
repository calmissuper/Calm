package functions;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.security.UserAndPassword;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.asserts.SoftAssert;
import org.xframium.device.DeviceManager;
import org.xframium.device.ng.AbstractSeleniumTest;
import org.xframium.page.ElementDescriptor;
import org.xframium.page.PageManager;
import org.xframium.page.StepStatus;
import org.xframium.page.data.PageData;
import org.xframium.page.data.PageDataManager;
import org.xframium.page.element.Element;
import org.xframium.page.element.Element.WAIT_FOR;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import utility.CustomAbstractPage;
import utility.CustomReporting;
import utility.GlobalVariableContainer;
import utility.Report;

public class GenericFunctions extends AbstractSeleniumTest{
	//KeyWordPageImpl
	
	private String strTokenName;
	private String strTokenValue;
	private FileInputStream pic = null;
	private String screenShotWordDocPath = "";
	private XWPFDocument doc = null;
	private XWPFParagraph p = null;
	private XWPFRun r = null;
	private String reqScreens = "";

	private static GenericFunctions singleton = new GenericFunctions();

	private GenericFunctions() {

	}

	public static GenericFunctions instance() {
		return singleton;
	}

	/*******************************************************************
	 * Name : getCustumWebDriver Description : Used to get the webDriver Object
	 * Modification Log : Date Initials Description of Modifications
	 ********************************************************************/
	public WebDriver getCustumWebDriver() {
		return getWebDriver();


		//return CustomFunctions.instance().getCustumWebDriver();

	}

	/*******************************************************************
	 * Name : getCustomPageName Description : Used to get the name of the page
	 * Modification Log : Date Initials Description of Modifications
	 ********************************************************************/
	public String getCustomPageName() {
		//return PageManager.instance().getPageCache().toString().split("Impl")[0].split("spi.")[1];
        return this.strTokenValue;
		//return cFun.getCustomPageName();
	}
	
	/*******************************************************************
	 * Name : getCustomElementName Description : Used to get the name of the element
	 * Modification Log : Date Initials Description of Modifications
	 ********************************************************************/
	
	public String getCustomElementName(Element el) {
		//String abc=PageManager.instance().
		//String abc=el.getKey();
		//return el.getKey();
		//return el.toString();
		return CustomFunctions.instance().getCustomElementName(el,this.strTokenValue);
		//return cFun.getCustomElementName();
	}

	/*******************************************************************
	 * Name : _setGlobalToken Description : Used to set the token value in
	 * global scope and _addToken function will called before every function
	 * Modification Log : Date Initials Description of Modifications
	 ********************************************************************/
	public void _setGlobalToken(String strTokenName, String strTokenValue) {
		this.strTokenName = strTokenName;
		this.strTokenValue = strTokenValue;
	}

	/*******************************************************************
	 * Name : _addToken Description : Used to to add the token value to an
	 * object in action Modification Log : Date Initials Description of
	 * Modifications
	 ********************************************************************/
	public void _addToken(Element element) {
		element.addToken(this.strTokenName, this.strTokenValue);
	}
	
	/*******************************************************************
	*	Name              :	_addToGlobalVariableList
	*	Description       : Used to add global variable to HashMap
	*   Parameters 		  : strKey - Key Value
	*   				  : strValue - Value
	*	Modification Log  :                                                     
	*	Date		Initials     	Description of Modifications 
	********************************************************************/
	public void _addToGlobalVariableList(String strKey,String strValue)
	{			
		GlobalVariableContainer.instance().addVariable(strKey, strValue);
	}
	
	/*******************************************************************
	*	Name              :	_addToGlobalVariableList
	*	Description       : Used to get global variable value from HashMap
	*   Parameters 		  : strKey - Key Value
	*	Modification Log  :                                                     
	*	Date		Initials     	Description of Modifications 
	********************************************************************/
	public String _getGlobalVariableValue(String strKey)
	{			
		return GlobalVariableContainer.instance().getVariable(strKey);
	}

	/*******************************************************************
	 * Name : _setValue Description : Used to set the value to any object, Only
	 * when there is data in DB Modification Log : Date Initials Description of
	 * Modifications
	 ********************************************************************/
	public void _setValue(Element element, String strValue) {

		long startTime = System.currentTimeMillis();
		WebElement elm =null;
		try 
		{
			if (!(strValue.isEmpty()))
			{
				element.setValue(strValue);
				Report.instance().log("", "INFO", "Entered the Value '"+strValue+"' in the element "+getCustomElementName(element));

				elm = (WebElement) element.getNative();
				//elm.sendKeys(Keys.TAB);
			}

		}
		
		//catch (StaleElementReferenceException e) 
		catch (Exception e) 
		{
			String[] strArr;
			//System.out.println(getCustomElementName(element));
			String locator=element.toString();
			//String locator=getCustomElementName(element);
			strArr=locator.split("\\{");
			String Xpath=strArr[1].replace("}", "");
			try 
			{
				WebElement ele=(getWebDriver().findElement(By.xpath(Xpath)));
				if ( ele.getTagName().equalsIgnoreCase( "select" ) )
				{
				//long startTime = System.currentTimeMillis();
			
					if (!(strValue.isEmpty()))
					{
						
				    	Select selValue=new Select(ele);
				    	selValue.selectByVisibleText(strValue);
				    	Report.instance().log("", "INFO", "Selected the Value from dropdown: "+strValue);

				    	ele.sendKeys(Keys.TAB);
					}
				
				
				}
				else
				{

					ele.clear();
					ele.sendKeys(strValue);
					Report.instance().log("", "INFO", "Entered the Value '"+strValue+"' in the element "+getCustomElementName(element));

				}
			}
			 catch (Exception ex) 
			 {
					// CustomReporting.logReport(keywordPage.getPageName(),element.getNative().toString(),"setValue","",StepStatus.FAILURE,new
					// String[] { strValue },startTime,ex);

					CustomReporting.logReport(getCustomPageName(),getCustomElementName(element), "setValue", "", StepStatus.FAILURE,
							new String[] { strValue }, startTime, ex);
					throw ex;
			 
			 }
		}
		
		
	}
	
	
	/*******************************************************************
	*	Name              :	_setValueByIndexInDropdown
	*	Description       : Used to select the dropdown values based on index
	*	
	********************************************************************/
	public void _setValueByIndexInDropdown(Element element, int index)
	{
		long startTime = System.currentTimeMillis();
        try
        {
		Select sel= new Select((WebElement) element.getNative());
    	sel.selectByIndex(index-1);
    	CustomReporting.logReport(getCustomPageName(),getCustomElementName(element), "setValue",StepStatus.SUCCESS,new String[] {index+"" },startTime);
        } catch(Exception ex)
        {
        	
        	CustomReporting.logReport(getCustomPageName(),getCustomElementName(element), "setValue", "", StepStatus.FAILURE,
					new String[] {  index+"" }, startTime, ex);
			throw ex;
        	
        }
	}

	/*******************************************************************
	 * Name : _checkDefaultValue Description : Used to compare the attribute
	 * value of an object Modification Log : Date Initials Description of
	 * Modifications
	 ********************************************************************/
	public void _checkDefaultValue( Element element, String strAttribute, String strExpValue) {
		String strActValue;
		long startTime = System.currentTimeMillis();

		// Get the Attribute value
		
		if(strAttribute.equalsIgnoreCase("TEXT")){
			WebElement ele = (WebElement) element.getNative();
			strActValue=ele.getText().trim();
		}else{
		strActValue = element.getAttribute(strAttribute);
		}

		// Compare the value
		if (!strActValue.equals(strExpValue)) {
			
			//CustomReporting.logReport(getCustomPageName(), element.toString(), "_label", "", StepStatus.FAILURE,
			//		new String[] {}, startTime, null);
			
			CustomReporting.logReport("","", "Expected value: "+strExpValue+" does not match with the actual value: "+strActValue, "", StepStatus.FAILURE,
					new String[] { }, startTime, null);
			//throw new RuntimeException();
		     }
			else
			{
				CustomReporting.logReport("","", "Expected value: "+strExpValue+" matches with the actual value: "+strActValue,StepStatus.SUCCESS,new String[] { },startTime);
			}
		

		//assertSoft.assertEquals(strExpValue, strActValue);
	}

	/*******************************************************************
	 * Name : _click Description : Used to click any type of object Modification
	 * Log : Date Initials Description of Modifications
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 ********************************************************************/
	public void _click(Element element) 
	{
		String strValue;
		
		long startTime = System.currentTimeMillis();
		try 
		{
			element.click();
	
			Report.instance().log("", "INFO", "Clicked on the Element : "+getCustomElementName(element));

		} catch (Exception ex) {

			CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "click", "", StepStatus.FAILURE,
					new String[] {}, startTime, ex);
			throw ex;

		}

	}
	
	/*******************************************************************
	 * Name : _clickXY 
	 * Description : Used to click any type of object based of its x,y coordinates
	 * Log : Date Initials Description of Modifications
	 * @throws Exception 
	 ********************************************************************/
	public void _clickXY(int x, int y) throws Exception
	{
		long startTime = System.currentTimeMillis();
        try
        {
        	Robot bot = new Robot();
		    bot.mouseMove(x,y);    
		    bot.mousePress(InputEvent.BUTTON1_MASK);
		    bot.mouseRelease(InputEvent.BUTTON1_MASK);
		    
    	CustomReporting.logReport(getCustomPageName(),"Coordinates("+x+","+y +")","click",StepStatus.SUCCESS,new String[] { },startTime);
        } catch(Exception ex)
        {
        	
        	CustomReporting.logReport(getCustomPageName(),"Coordinates("+x+","+y +")", "click", "", StepStatus.FAILURE,
					new String[] {   }, startTime, ex);
			throw ex;
        	
        }
	}

	/*******************************************************************
	 * Name : _getRelativeDate Description : Used to get the relative date wrt
	 * current date Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/
	public String _getRelativeDate(String strRelation, String intDays, String intMonths, String intYears) {
		DateFormat dateFormate = new SimpleDateFormat("MM/dd/yyyy");
		Date dateTodays = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateTodays);

		if (intDays.isEmpty())
			intDays = "0";

		if (intMonths.isEmpty())
			intMonths = "0";

		if (intYears.isEmpty())
			intYears = "0";

		switch (strRelation.toUpperCase()) {
		case "PAST":
			cal.add(Calendar.DATE, -Integer.parseInt(intDays));
			cal.add(Calendar.MONTH, -Integer.parseInt(intMonths));
			cal.add(Calendar.YEAR, -Integer.parseInt(intYears));
			break;
		case "FUTURE":
			cal.add(Calendar.DATE, Integer.parseInt(intDays));
			cal.add(Calendar.MONTH, Integer.parseInt(intMonths));
			cal.add(Calendar.YEAR, Integer.parseInt(intYears));
			break;

		default:
			break;

		}

		return dateFormate.format(cal.getTime());
	}

	/*******************************************************************
	 * Name : _getRandomString Description : Used to get the relative date wrt
	 * current date Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/
	public enum DATATYPE {
		number, varchar, character;
	}

	public String _getRandomString(DATATYPE type, int length) {
		String strType = type.toString();
		String defaultString = "";

		if (!strType.equalsIgnoreCase("number"))
			defaultString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

		if (!strType.equalsIgnoreCase("character"))
			defaultString = defaultString + "0123456789";

		StringBuilder strCh = new StringBuilder();
		Random rnd = new Random();

		for (int i = 0; i < length; i++)
			strCh.append(defaultString.charAt(rnd.nextInt(defaultString.length())));

		return strCh.toString();
	}

	/*******************************************************************
	 * Name : _alertHandler Description : Used to handle pop-up which comes up
	 * during execution Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/
	public void _alertHandler(WebDriver driver, String strOperation) {
		Alert alert=null;
		try {

			WebDriverWait wait = new WebDriverWait(driver, 60);
			alert = wait.until(ExpectedConditions.alertIsPresent());
			// Accept or dismiss the alert
			switch (strOperation.toUpperCase()) {
			case "OK":
				alert.accept();
				//driver.switchTo().defaultContent();
				log.info("Clicked on OK Button successfully");
				break;
			case "CANCEL":
				alert.dismiss();
				driver.switchTo().defaultContent();
				log.info("Clicked on CANCEL Button successfully");
				break;

			default:
				System.out.println("Pass the correct data either as 'OK' or 'CANCEL'");
				// Need to add custom report log and fail the test if needed
				System.exit(1);
				break;
			}

		} catch (Exception e) {
			try
			{
				alert.accept();
				//driver.switchTo().defaultContent();
				log.info("Clicked on OK Button successfully");
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/*******************************************************************
	 * Name : _closeWindow Description : Used to close popup windows which comes
	 * up during execution Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/
	public void _closeWindow(WebDriver driver) {
		String parentWindow = driver.getWindowHandle();

		for (String handle : driver.getWindowHandles()) {
			if (!handle.equals(parentWindow)) {
				driver.switchTo().window(handle);
				driver.close();
			}
		}
		driver.switchTo().window(parentWindow);
	}

	/*******************************************************************
	 * Name : _handleIMSecurityAlert Description : Used to handle windows
	 * authentication popup which comes up on clicking Alliance URL Modification
	 * Log : Date Initials Description of Modifications
	 * 
	 * @return
	 ********************************************************************/
	public void _handleIMSecurityAlert() throws InterruptedException {
		WebDriver driver = getCustumWebDriver();
		WebDriverWait wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.alertIsPresent());

		driver.switchTo().alert().authenticateUsing(new UserAndPassword("UserId", "Password"));
		driver.switchTo().defaultContent();
	}

	/*******************************************************************
	 * Name : _compareSubStringCaseSensitive Description : Used to compare sub
	 * string with case sensitive Modification Log : Date Initials Description
	 * of Modifications
	 * 
	 * @return
	 ********************************************************************/
	public boolean _compareSubStringCaseSensitive(String strChar, String strSearchChar) {
		Boolean blnCondition = false;

		if (StringUtils.contains(strChar, strSearchChar)) {
			blnCondition = true;
		}

		return blnCondition;
	}

	/*******************************************************************
	 * Name : _compareSubStringIgnorecase Description : Used to compare sub
	 * string with Ignore Case Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/
	public boolean _compareSubStringIgnorecase(String strChar, String strSearchChar) {
		boolean blnCondition = false;

		if (StringUtils.containsIgnoreCase(strChar, strSearchChar)) {
			blnCondition = true;
		}

		return blnCondition;
	}

	/*******************************************************************
	 * Name : _compareStringCaseSensitive Description : Used to compare strings
	 * with Case sensitive Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/
	public boolean _compareStringCaseSensitive(String strChar, String strSearchChar) {
		Boolean blnCondition = false;

		if (strChar.equals(strSearchChar)) {
			blnCondition = true;
		}
		return blnCondition;
	}

	/*******************************************************************
	 * Name : _compareStringIgnorecase Description : Used to compare strings
	 * with Ignore Case Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/
	public boolean _compareStringIgnorecase(String strChar, String strSearchChar) {
		boolean blnCondition = false;

		if (strChar.equalsIgnoreCase(strSearchChar)) {
			blnCondition = true;
		}

		return blnCondition;
	}

	/*******************************************************************
	 * Name : _splitDatabyPosition Description : Used to split and return the
	 * data by position Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/

	public String _splitDatabyPosition(String strChar, int intStartPosition, int intEndPosition) {
		String strData = null;
		strData = strChar.substring(intStartPosition, intEndPosition);
		return strData;

	}

	/*******************************************************************
	 * Name : _splitDatabyDelimiter Description : Used to split the data by
	 * delimiter and return the data as an array Modification Log : Date
	 * Initials Description of Modifications
	 * 
	 * @return
	 ********************************************************************/

	public String[] _splitDatabyDelimiter(String strChar, String strDelimiter) {
		String[] strData;
		strData = strChar.split(strDelimiter);
		return strData;

	}

	/*******************************************************************
	 * Name : _getAttributeValue Description : Used to retrieve the data from
	 * application either by innerHTML or Value Modification Log : Date Initials
	 * Description of Modifications
	 * 
	 * @return
	 ********************************************************************/

	public String _getAttributeValue(Element element, String strAttribute) {
		String strApplicationValue = null;
		// Get the Attribute value
		strApplicationValue = element.getAttribute(strAttribute);
		return strApplicationValue;

	}



/*******************************************************************
	 * Name : _takeBrowserScreenShot Description : Used to take screen shot of the WebDriver and save in
	 * word document Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/

	public void _takeBrowserScreenShot(WebDriver driver, String strOperation, String strTextEntry, String docName) {
		try {

			switch (strOperation.toUpperCase()) {
			case "START":
				doc = new XWPFDocument();
				p = doc.createParagraph();
				r = p.createRun();
				reqScreens = "Screenshots Initiated";
				log.info("Taking screenshot initiated");
				break;
			case "PRINT":
				if (reqScreens.equalsIgnoreCase("Screenshots Initiated")) {
					File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
					pic = new FileInputStream(scrFile);

					r.addBreak();
					r.setText(strTextEntry);
					r.addBreak();
					r.addPicture(pic, XWPFDocument.PICTURE_TYPE_PNG, "", Units.toEMU(400), Units.toEMU(300));
					r.addBreak();

					log.info("Screenshot taken successfully");
				}
				break;
			case "FINISH":
				if (reqScreens.equalsIgnoreCase("Screenshots Initiated")) {
					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
					String strTCID=docName; //added for qTest
			    	String docname = docName+"_"+timeStamp;
					screenShotWordDocPath = DeviceManager.instance().getConfigurationProperties()
							.getProperty("screenshot.path") + "\\" + docname + ".docx";
					FileOutputStream out = new FileOutputStream(screenShotWordDocPath);
					doc.write(out);
					out.close();
					pic.close();
					GenericFunctions.instance()._addToGlobalVariableList(strTCID, docname); //added for qTest
					log.info("Screenshot saved successfully");
					CustomReporting.instance().logReport("<a href=\"" + screenShotWordDocPath + "\"" + ">Click here to view screenprints</a>");
				}
				break;

			default:
				System.out.println("Please pass the correct operation string");
				// System.exit(1);
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/*******************************************************************
	 * Name : _takeScreenShot Description : Used to take screen shot of the Desktop and save in
	 * word document Modification Log : Date Initials Description of
	 * Modifications
	 * 
	 * @return
	 ********************************************************************/
	public void _takeScreenShot(WebDriver driver, String strOperation, String strTextEntry, String docName) {
		try {

			switch (strOperation.toUpperCase()) {
			case "START":
				doc = new XWPFDocument();
				p = doc.createParagraph();
				r = p.createRun();
				reqScreens = "Screenshots Initiated";
				log.info("Taking screenshot initiated");
				break;
			case "PRINT":
				if (reqScreens.equalsIgnoreCase("Screenshots Initiated")) {
					

					log.info("Screenshot taken successfully");
					
					Rectangle captureSize=new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
					Robot robot=new Robot();
					BufferedImage bufferedImage=robot.createScreenCapture(captureSize);
					//pic = new FileInputStream(bufferedImage);
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					
					ImageIO.write(bufferedImage, "PNG", os);
					InputStream is = new ByteArrayInputStream(os.toByteArray());
					
					r.addBreak();
					r.setText(strTextEntry);
					r.addBreak();
					r.addPicture(is, XWPFDocument.PICTURE_TYPE_PNG, "", Units.toEMU(400), Units.toEMU(300));
					
					r.addBreak();
				}
				break;
			case "FINISH":
				if (reqScreens.equalsIgnoreCase("Screenshots Initiated")) {
					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
					String strTCID=docName; //added for qTest
			    	String docname = docName+"_"+timeStamp;
					screenShotWordDocPath = DeviceManager.instance().getConfigurationProperties()
							.getProperty("screenshot.path") + "\\" + docname + ".docx";
					FileOutputStream out = new FileOutputStream(screenShotWordDocPath);
					doc.write(out);
					out.close();
					//pic.close();
					GenericFunctions.instance()._addToGlobalVariableList(strTCID, docname); //added for qTest
					log.info("Screenshot saved successfully");
					//CustomReporting.instance().logInfo("Screenshot: ", " ","", "<a href=\"" + screenShotWordDocPath + "\"" + ">Click here to view screenprints</a>");
					CustomReporting.instance().logReport("<a href=\"" + screenShotWordDocPath + "\"" + ">Click here to view screenprints</a>");
				}
				break;

			default:
				System.out.println("Please pass the correct operation string");
				// System.exit(1);
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/*******************************************************************
	 * Name : _uploadFile
	 * Description : Used to upload files
	 * 
	 ********************************************************************/
	public void _uploadFile(String FilePath )
    {
		  long startTime = System.currentTimeMillis();
          try
          {
        	         	  
          Toolkit toolkit=Toolkit.getDefaultToolkit();
          Clipboard clipboard=toolkit.getSystemClipboard();
          StringSelection ss=new StringSelection(FilePath);
          clipboard.setContents(ss, ss);
          String result=(String) clipboard.getData(DataFlavor.stringFlavor);
          System.out.println("string from the clipboard "+result);
          Thread.sleep(5000);
          
          Robot robot = new Robot();
          robot.keyPress(KeyEvent.VK_ALT);
          robot.keyPress(KeyEvent.VK_N);
          robot.keyRelease(KeyEvent.VK_ALT);
          robot.keyRelease(KeyEvent.VK_N);
          Thread.sleep(5000);
          robot.keyPress(KeyEvent.VK_CONTROL);
          robot.keyPress(KeyEvent.VK_V);
          robot.keyRelease(KeyEvent.VK_V);
          robot.keyRelease(KeyEvent.VK_CONTROL);
          Thread.sleep(5000);
          robot.keyPress(KeyEvent.VK_ENTER);
          robot.keyRelease(KeyEvent.VK_ENTER);
          
          Thread.sleep(8000);
          
          CustomReporting.logReport("","", "File uploaded successfully",StepStatus.SUCCESS,new String[] { },startTime);
          }
          catch(Exception ex)
          {
        	  CustomReporting.logReport("","", "File upload failed","", StepStatus.FAILURE,
  					new String[] {  }, startTime, ex);
        	  System.out.println(ex);
          }
    }
	
	/*******************************************************************
	 * Name : _launchOutlook 
	 * Description : Used to launch Outlook
	 * 
	 ********************************************************************/
	public void _launchOutlook( ) throws URISyntaxException
    {
		try {
		      //Desktop.getDesktop().mail( new URI( "mailto:javaexamplecenter@gmail.com?subject=Test%20message" ) );
		      //Desktop.getDesktop().mail( );
		      Runtime.getRuntime().exec("C:\\Program Files (x86)\\Microsoft Office\\Office14\\OUTLOOK.exe");
		     } 
		catch ( IOException ex )
		    {
		    } 
    }
	
	/*******************************************************************
	 * Name : _closeOutlook 
	 * Description : Used to close Outlook
	 * 
	 ********************************************************************/
	public void _closeOutlook( ) throws URISyntaxException
    {
		try {
		      //Desktop.getDesktop().mail( new URI( "mailto:javaexamplecenter@gmail.com?subject=Test%20message" ) );
		      //Desktop.getDesktop().mail( );
		      //Runtime.getRuntime().exec("C:\\Program Files (x86)\\Microsoft Office\\Office14\\OUTLOOK.exe");
		      Runtime.getRuntime().exec("taskkill /F /IM OUTLOOK.EXE");
		     } 
		catch ( IOException ex )
		    {
		    } 
    }
	/*******************************************************************
	 * Name : _writeToExcel 
	 * Description : Used for writing data to Excel
	 * 
	 ********************************************************************/
	public void _writeToExcel(String Sheetname,String strColName,String strVal,String strTCID)
    {
		long startTime = System.currentTimeMillis();
    try {
      
      String writeExcelPath = DeviceManager.instance().getConfigurationProperties().getProperty("data.path");
      FileInputStream file = new FileInputStream(new File(writeExcelPath));
     
      XSSFWorkbook  workbook = new XSSFWorkbook (file);
      XSSFSheet sheet = workbook.getSheet(Sheetname);
      int intRow=findExcelRow(sheet,strTCID);
      int intCol=findExcelCol(sheet,strColName);
      try
      {
    	  Cell cell=  sheet.getRow(intRow).createCell(intCol);
      	cell.setCellValue(strVal);
      }
      catch(Exception ex)
      {
    	  sheet.getRow(intRow).getCell(intCol).setCellValue(strVal);
      }
      FileOutputStream outFile =new FileOutputStream(new File(writeExcelPath));
      workbook.write(outFile);
      file.close();
      outFile.close();
      CustomReporting.logReport("","", strVal+" : written to Excel Successfully",StepStatus.SUCCESS,new String[] {},startTime);

  } catch (FileNotFoundException e) {
      e.printStackTrace();
      CustomReporting.logReport("","", strVal+" : writing to Excel failed","", StepStatus.FAILURE,
				new String[] {}, startTime, e);
  	  
  } catch (IOException e) {
      e.printStackTrace();
      CustomReporting.logReport("","", strVal+" : writing to Excel failed","", StepStatus.FAILURE,
				new String[] { }, startTime, e);
  
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
	
	/*******************************************************************
	 * Name : _isVisible 
	 * Description : Used to check if the object is displayed on the screen
	 * 
	 ********************************************************************/
	public boolean _isVisible(Element element)
	{
		
		boolean success = false;
		getCustumWebDriver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
				
		try
		{
			WebElement ele =(WebElement)element.getNative();
			success = ele.isDisplayed();
			Report.instance().log("", "INFO", "Element is visible : "+getCustomElementName(element));
			return success;
            
		}catch(Exception ex)
        {
		    return success;
        }finally{		
		getCustumWebDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }
	}
	
	/*******************************************************************
	 * Name : _switchWindows 
	 * Description : Used for switching to windows
	 * 
	 ********************************************************************/
	/*public boolean _switchWindows(WebDriver driver, String strswitchOption, String switchExpValue) 
    {
          boolean bolSwitchWindow=false;
          try {

                switch (strswitchOption.toUpperCase()) 
                {
                case "BY_WINTITLE":
                      bolSwitchWindow=verifySwitchWindow( driver, strswitchOption, switchExpValue );
                      break;
                case "BY_WINURL":
                      bolSwitchWindow=verifySwitchWindow( driver, strswitchOption, switchExpValue );
                      break;
                case "BY_WINID":
                    bolSwitchWindow=verifySwitchWindow( driver, strswitchOption, switchExpValue );
                    break;
                case "BY_FRAME":
                      driver.switchTo().frame( switchExpValue );
                default:
                      System.out.println("Please pass the correct operation string");
                      // System.exit(1);
                      
                }

          } catch (Exception e) {
                e.printStackTrace();
          }
          return bolSwitchWindow; 

    }*/
    private boolean verifySwitchWindow( WebDriver webDriver, String byTitleOrUrl, String winExpValue )
    {

          boolean bSwitchWindow = false;
          String winActValue = "";
          Set<String> availableWindows = webDriver.getWindowHandles();
          System.out.println("Total available windows: "+availableWindows.size());

          if ( !availableWindows.isEmpty() )
          {
                for ( String windowId : availableWindows )
                {
                      if ( byTitleOrUrl.equalsIgnoreCase( "BY_WINTITLE" ) )
                      {
                            winActValue = webDriver.switchTo().window( windowId ).getTitle().trim().toLowerCase();
                      }
                      else if ( byTitleOrUrl.equalsIgnoreCase( "BY_WINURL" ) )
                      {
                            winActValue = webDriver.switchTo().window( windowId ).getCurrentUrl().trim().toLowerCase();
                            
                      }
                      else
                      {
                          webDriver.switchTo().window( windowId );
                          winActValue=windowId;
                      }  

                      winExpValue = winExpValue.trim().toLowerCase();

                      if ( winActValue.contains( winExpValue ) )
                      {
                            bSwitchWindow = true;
                            System.out.println("Switched to window-" +winExpValue );
                            break;
                      }
                     /* else
                      {
                          bSwitchWindow = true;
                          System.out.println("Switched to window id - " +windowId );
                          
                      }*/
                }
          }

          return bSwitchWindow;
    }

    /*******************************************************************
	*	Name              :	_getValueDropDownBox
	*	Description       : Used to set the value to any object, Only when there is data in DB
	*	
	********************************************************************/
	public String[] _getValueDropDownBox(Element element)
	{
		
		WebElement elm = (WebElement) element.getNative();
		Select select=new Select (elm);
		
		List <WebElement> options=select.getOptions();
		String[] values=new String[options.size()];
		int i=0;
		for (WebElement option : options) 
		{
			values[i]=option.getText();
			i++;
		}
		return values;
	}
	
	/*******************************************************************
	*	Name              :	_waitFor
	*	Description       : Used to wait for an object, Default wait_for is VISIBLE
							User can pass this option explicitly also
	*	 
	********************************************************************/
	public void _waitFor(Element element, long timeOut, WAIT_FOR waitType, String value)
	{
		long startTime = System.currentTimeMillis();
               
        try
        {
        	element.waitFor(timeOut, TimeUnit.SECONDS, waitType, value);
        
        } catch(Exception ex)
        {
        	CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "present", "", StepStatus.FAILURE,
					new String[] { value }, startTime, ex);
			throw ex;
        }
       		
	}
	
	public void _waitFor(Element element, long timeOut, WAIT_FOR waitType)
	{
		_waitFor(element,timeOut, waitType, "");
	}
	
	public void _waitFor(Element element, long timeOut)
	{
		long startTime = System.currentTimeMillis();
     
		 try
	        {
			 element.waitForVisible(timeOut, TimeUnit.SECONDS);
	        } catch(Exception ex)
	        {
	        	CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "present", "", StepStatus.FAILURE,
						new String[] { "Visible" }, startTime, ex);
				throw ex;
	        }
		
		
	}
	
	public void _wait(long timeOut)  
	{
		try {
			Thread.sleep(timeOut);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*******************************************************************
	*	Name              :	_validateDropdownValues
	*	Description       : Used to validate values of a dropdown against given list of values
	*	 
	********************************************************************/
	public boolean _validateDropdownValues(Element dropDownElement,List<String> expOptions,boolean bOrder,boolean bIgnoreCase, SoftAssert assertSoft)
	{
		long startTime = System.currentTimeMillis();
		boolean success = false;
	    try
	    {
	    	Select sel= new Select((WebElement) dropDownElement.getNative());
	    	
	    	List<WebElement> options= sel.getOptions();
	    	
	    	List<String> expected= new ArrayList<String>();
	    	List<String> actual= new ArrayList<String>();
	    	//get actual values and convert to upper case if comparison is not case sensitive
		    for (WebElement option : options) 
		    {
		    	
		    	if(bIgnoreCase)
		    		actual.add(option.getText().toUpperCase().trim());
		    	else 
		    		actual.add(option.getText().trim());	    	

	    	}
		    //get expected values and convert to upper case if comparison is not case sensitive
		    for(String option: expOptions)
		    {
		    	if(bIgnoreCase)
		    		expected.add(option.toUpperCase().trim());
		    	else 
		    		expected.add(option.trim());
		    }
		    //sort the values if order is not matter in comparison
		    if(!bOrder)
		    {
		    	Collections.sort(actual);
		    	Collections .sort(expected);
		    }
		    //compare expected vs actual
		    	
		    if(actual.equals(expected))
	    	{
	    		success=true;
	    		CustomReporting.logReport(getCustomPageName(), getCustomElementName(dropDownElement),"Expected values :"+expected.toString(),StepStatus.SUCCESS,new String[] {expected.toString(),actual.toString()},startTime);
	    	}
	    	else
	    	{
	    		CustomReporting.logReport(getCustomPageName(), getCustomElementName(dropDownElement),"Expected values : "+expected.toString()+" Actual values: "+actual.toString(),StepStatus.FAILURE,new String[] {expected.toString(),actual.toString()},startTime);
	    	}
	    		
	    } catch(Exception ex)
	    {
	    	CustomReporting.logReport(getCustomPageName(), getCustomElementName(dropDownElement),"dropdown","",StepStatus.FAILURE,new String[] {},startTime,ex);
        	throw ex;
	    	
	    }  
	    
	    finally {
	        assertSoft.assertEquals(success, true);
		}
		return success;
	}
	
	/*******************************************************************
	*	Name              :	_isEnabled
	*	Description       : Used to check Object is enabled or disabled
	*	 
	********************************************************************/
	public boolean _isEnabled(Element element)
	{

		
		WebElement ele = (WebElement) element.getNative();
		
		String isDisabled = ele.getAttribute("disabled");
		if (isDisabled==null)
			return true;
		else
			return false;
			
			
	}
	
	/*******************************************************************
	*	Name              :	_checkAttributeValue
	*	Description       : Used  to compare the attribute value of an object
	*	 
	********************************************************************/
	public boolean _checkAttributeValue(Element element,  String strAttribute, String strExpValue,SoftAssert assertSoft)
	{
		String strActValue="";
		long startTime = System.currentTimeMillis();		
						
		//Compare the value
		try
		{
			if(_isVisible(element))
			{
				
				//Get the Attribute value
				if(strAttribute.equalsIgnoreCase("TEXT"))
				{
					
					WebElement ele = (WebElement) element.getNative();
					if(ele.getTagName().equalsIgnoreCase("select"))
					{
						Select sel= new Select(ele);	
						strActValue= sel.getFirstSelectedOption().getText().trim();
					}
					else
					{
						strActValue=ele.getText().trim();
					}
					//CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "attribute",StepStatus.SUCCESS,new String[] { strAttribute, strExpValue},startTime);	
				}
				else
				{
					strActValue = element.getAttribute(strAttribute).trim();
				}
				
				
				if ( !strActValue.equalsIgnoreCase( strExpValue.trim() ) )
				{
					CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "Expected value:'" +strExpValue+ "'does not matches with the actual value '"+strActValue+"'","",StepStatus.FAILURE,new String[] { strAttribute, strExpValue},startTime,null);
					
					//CustomReporting.logReport("", "", "Expected value:" +strExpValue+ "does not matches with the actual value: "+strActValue,"",StepStatus.FAILURE,new String[] { strAttribute, strExpValue},startTime,null);
					return false;
				}
				else
				{
					CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "Expected value '"+strExpValue+" matches with the actual value: "+strActValue+"'",StepStatus.SUCCESS,new String[] { strAttribute, strExpValue},startTime);	
					//CustomReporting.logReport("", "", "Expected value: "+strExpValue+" matches with the actual value: "+strActValue,StepStatus.SUCCESS,new String[] { strAttribute, strExpValue},startTime);
					
					return true;
					
				}
			}
			else
			{
				CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "_attrValue","",StepStatus.FAILURE,new String[] { strAttribute, strExpValue," Object not visible"},startTime,null);
				return false;
			}
			
		
		}
		catch(Exception ex)
		{
			CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "attrValue","",StepStatus.FAILURE,new String[] { strAttribute, strExpValue},startTime,ex);
        	//throw ex;
			return false;
		}
		finally
		{
			assertSoft.assertEquals(strExpValue.toUpperCase(), strActValue.toUpperCase());
		}
	}
	
	
	/*******************************************************************
	*	Name              :	_getAttributeValue
	*	Description       : Used  to retrive the attribute value of an object
	*	
	********************************************************************/
	@SuppressWarnings("finally")
    public String _getAttributeValue(Element element, String strAttribute, SoftAssert assertSoft)
    {
          String strActValue="";
          long startTime = System.currentTimeMillis();
          boolean blnStatus = false;
                
                
          //Compare the value
          try
          {
                //Get the Attribute value
                if(strAttribute.equalsIgnoreCase("TEXT"))
                {
                      WebElement ele = (WebElement) element.getNative();
                      if(ele.getTagName().equalsIgnoreCase("select"))
                      {
                            Select sel= new Select(ele);  
                            strActValue= sel.getFirstSelectedOption().getText().trim();
                      }
                      else
                      {
                            strActValue=ele.getText();
                      }
                      CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "attribute",StepStatus.SUCCESS,new String[] { strAttribute, strActValue},startTime);
                }
                else
                {
                      strActValue = element.getAttribute(strAttribute);
                }
                
                blnStatus = true;
                
          }catch(Exception ex)
          {
                CustomReporting.logReport(getCustomPageName(), getCustomElementName(element), "attribute","",StepStatus.FAILURE,new String[] { strAttribute, strActValue},startTime,ex);
                throw ex;
          }finally
          {
                assertSoft.assertEquals(true, blnStatus);
                return strActValue;
          }
    }

	
	public void DownloadFileIE( Element element)
	{
		
		 try {
	            Robot robot = new Robot();
	            //get the focus on the element..don't use click since it stalls the driver
	           WebElement elm = (WebElement) element.getNative();
	            elm.sendKeys("");
	            //simulate pressing enter            

	            robot.keyPress(KeyEvent.VK_ENTER);
	            robot.keyRelease(KeyEvent.VK_ENTER);
	            //wait for the modal dialog to open            
	            Thread.sleep(2000);
	            //press s key to save            
	            robot.keyPress(KeyEvent.VK_ALT);
	            robot.keyPress(KeyEvent.VK_S);
	            robot.keyRelease(KeyEvent.VK_ALT);
	            robot.keyRelease(KeyEvent.VK_S);
	            Thread.sleep(2000);
		 	}

	 	 	catch (Exception e)
		 	{

	            e.printStackTrace();
	           
	           
		
		 	}
	}
	
	public boolean CheckFileExist (String directory, String file) 
	{
	    File dir = new File(directory);
	    File[] dir_contents = dir.listFiles();
	   /* String temp = file + ".PDF";*/
	    boolean isFileExist=false;
	    for(int i = 0; i<dir_contents.length;i++) 
	    {
	        if(dir_contents[i].getName().contains(file))
	        {
	        	isFileExist=true;
	        	//dir_contents[i].delete();
	        	break;
	        }
	    }

	    return isFileExist;
	}
	
	
	public long DateFormatReturns (String strDate, String strSelectOption) throws Exception 
	{
		long returnValue = 0;
		try
		{
			
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");
			Date date = new Date();
			System.out.println(dateFormat.format(date));
			Date datenew=new SimpleDateFormat("MM/dd/yyyy").parse(strDate);
			System.out.println(dateFormat.format(datenew));
			Date datecurr = new Date(date.getYear(),date.getMonth(),date.getDay());
	        Date dateexp = new Date(datenew.getYear(),datenew.getMonth(),datenew.getDay());
	        Calendar calendar1 = Calendar.getInstance();
	        Calendar calendar2 = Calendar.getInstance();
	        calendar1.setTime(datecurr);
	        calendar2.setTime(dateexp);
	        long milliseconds1 = calendar1.getTimeInMillis();
	        long milliseconds2 = calendar2.getTimeInMillis();
	       
	        
	        switch (strSelectOption.toUpperCase())
	        {
				case "DAYS":
					 long diff = milliseconds2 - milliseconds1;
					 returnValue=diff / (24 * 60 * 60 * 1000);
					
	        }
		}
		catch(Exception ex)
		{
			
        	throw ex;
        	
			
		}
		return returnValue;	
			
	}
	
	public String Date_Manipulation (String strDate,String strSelectOption,int num) throws Exception 
	{
		String datevalue ="";
		
		try
		{
	
			DateFormat dateFormate = new SimpleDateFormat("MM/dd/yyyy");
			Date date = new Date();
			Calendar cal = Calendar.getInstance();
			if(!strDate.equals(""))
			{
				cal.setTime(dateFormate.parse(strDate));
			}
			else
			{
				cal.setTime(date);
			}
			
						
			switch (strSelectOption.toUpperCase())
	        {
				case "YEAR":
				case "YEARS":
			
					cal.add(Calendar.YEAR,num);
					datevalue =dateFormate.format(cal.getTime());
					break;
					
				case "DAYS":
				case "DAY":
					cal.add(Calendar.DAY_OF_MONTH,num);
					datevalue =dateFormate.format(cal.getTime());
					break;
					
				case "MONTHS":
				case "MONTH":
					cal.add(Calendar.MONTH,num);
					datevalue =dateFormate.format(cal.getTime());
					break;
					
				case "NOW":
					
					datevalue =dateFormate.format(date);
					break;

	        }
		}
		catch(Exception ex)
		{
			
        	throw ex;
        	
			
		}
		return datevalue;	
			
	}
	
	public boolean _isReadOnly(Element element)
	{

		
		WebElement ele = (WebElement) element.getNative();
		
		String isDisabled = ele.getAttribute("readonly");
		if (isDisabled==null)
			return false;
		else
			return true;
			
			
	}
	
	public boolean _isChecked(Element element)
	{

		
		WebElement ele = (WebElement) element.getNative();
		
		String isDisabled = ele.getAttribute("checked");
		if (isDisabled==null)
			return false;
		else
			return true;
			
			
	}
	
	public boolean _switchWindows(WebDriver driver, String strswatchOption, String switchExpValue) 
	{
		boolean bolSwitchWindow=false;
		try {

			switch (strswatchOption.toUpperCase()) 
			{
			case "BY_WINTITLE":
				bolSwitchWindow=verifySwitchWindow( driver, strswatchOption, switchExpValue );
				break;
			case "BY_WINURL":
				bolSwitchWindow=verifySwitchWindow( driver, strswatchOption, switchExpValue );
				break;
			case "BY_FRAME":
				driver.switchTo().frame( switchExpValue );
				break;
				
			case "BY_WINDOW":
				System.out.println("Window handle-"+driver.getWindowHandle());
				System.out.println("Window title-"+driver.switchTo().window(driver.getWindowHandle()).getTitle());
				break;
				
			case "BY_WINURL_CLOSE":
				CloseWindow( driver, strswatchOption, switchExpValue );
				
			case "GET_TITLE":
				String pageTitle = driver.getTitle();
				if ( !pageTitle.equals( pageTitle ) )
		        {
		            throw new ScriptException( "Expected Title of [" + switchExpValue + "] but received [" + pageTitle + "]" );
		        }
				break;
			/*default:
				System.out.println("Please pass the correct operation string");
				// System.exit(1);
*/				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bolSwitchWindow; 

	}
	private void CloseWindow( WebDriver webDriver, String byTitleOrUrl, String winExpValue )
	{
		String winActValue = "";
		Set<String> availableWindows = webDriver.getWindowHandles();
		System.out.println(availableWindows.size());

		if ( !availableWindows.isEmpty() )
		{
			for ( String windowId : availableWindows )
			{
				if ( byTitleOrUrl.equalsIgnoreCase( "BY_WINTITLE" ) )
				{
					winActValue = webDriver.switchTo().window( windowId ).getTitle().trim().toLowerCase();
				}
				else
				{
					winActValue = webDriver.switchTo().window( windowId ).getCurrentUrl().trim().toLowerCase();
					
				}

				winExpValue = winExpValue.trim().toLowerCase();
				System.out.println("Appp window title :"+winActValue);
				System.out.println("Expected window title :"+winExpValue);
				if ( winActValue.contains( winExpValue ) )
				{
					webDriver.switchTo().window( windowId ).close();
					System.out.println(winActValue+  " Window is closed" );
					break;
				}
			}
		}
	}
	
public boolean _verifyPDFDocument(String strFilepath,String reqTextInPDF) {
		
		long startTime = System.currentTimeMillis();
		boolean isTextFound=false;
		String parsedText="";
		try {

			PdfReader readertext = new PdfReader(strFilepath);
			PdfReaderContentParser parser = new PdfReaderContentParser(readertext);
			TextExtractionStrategy strategy = null;
			for(int i = 1; i <= readertext.getNumberOfPages(); i++) {
		    	strategy = parser.processContent(i,new SimpleTextExtractionStrategy());
		        parsedText = parsedText+strategy.getResultantText();
			}
			System.out.println(parsedText);
			     
		      if(parsedText.contains(reqTextInPDF)) {
		    	  isTextFound=true;
		    	  CustomReporting.logReport("","", "Given text: "+reqTextInPDF+" - is present in the the given PDF document",StepStatus.SUCCESS,new String[] { },startTime);
				}
		      else 
		      {
		    	  CustomReporting.logReport("","", "Given text: "+reqTextInPDF+" - is not present in the the given PDF document","", StepStatus.FAILURE,
		  					new String[] {  }, startTime, null);
		        	 
		      }
		      
		      //Closing the document
		      //document.close();
		      readertext.close();
			
		}
		catch(Exception ex) 
		{
			try
			{
				PdfReader readertext = new PdfReader(strFilepath);
				PdfReaderContentParser parser = new PdfReaderContentParser(readertext);
				TextExtractionStrategy strategy = null;
				for(int i = 1; i <= readertext.getNumberOfPages(); i++) {
			    	strategy = parser.processContent(i,new SimpleTextExtractionStrategy());
			        parsedText = parsedText+strategy.getResultantText();
				}
				System.out.println(parsedText);
				     
			      if(parsedText.contains(reqTextInPDF)) {
			    	  isTextFound=true;
			    	  CustomReporting.logReport("","", "Given text: "+reqTextInPDF+" - is present in the the given PDF document",StepStatus.SUCCESS,new String[] { },startTime);
					}
			      else 
			      {
			    	  CustomReporting.logReport("","", "Given text: "+reqTextInPDF+" - is not present in the the given PDF document","", StepStatus.FAILURE,
			  					new String[] {  }, startTime, null);
			        	 
			      }
			      
			      //Closing the document
			      //document.close();
			      readertext.close();
			}
			catch(Exception e) 
			{
				CustomReporting.logReport("","", "Error occurred while verifying PDF contents","", StepStatus.FAILURE,
	  					new String[] {  }, startTime, e);
	        	  System.out.println(e);
			}
			
		}
		
		
		
		return isTextFound;
	}

public void _waitForPageToLoad(WebDriver driver, Long timeOutInSeconds) {
	
	long startTime = System.currentTimeMillis();

	ExpectedCondition < Boolean > pageLoad = new
		    ExpectedCondition < Boolean > () {
		        public Boolean apply(WebDriver driver) {
		        	return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
	        	
		        }
		    };
		    
		    Wait < WebDriver > wait = new WebDriverWait(driver, timeOutInSeconds);
		    try {Thread.sleep(2000);
		        wait.until(pageLoad);

		    } catch (Exception ex) {
		    	
		        CustomReporting.logReport("","", "Timeout during page load","", StepStatus.FAILURE,
	  					new String[] {  }, startTime, ex);
		    }
	
}

public void _record_PageLoadTime(WebDriver driver, Long timeOutInSeconds) {
    
    long startTime = System.currentTimeMillis();
    long elapsedTime=0L;
    ExpectedCondition < Boolean > pageLoad = new
                        ExpectedCondition < Boolean > () {
                            public Boolean apply(WebDriver driver) {
                                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
                            }
                        };
                        
                        Wait < WebDriver > wait = new WebDriverWait(driver, timeOutInSeconds);
                        try {
                            wait.until(pageLoad);
                            elapsedTime = (new Date()).getTime() - startTime;
                            //CustomReporting.logReport("","", "Time taken to load the page: "+getCustomPageName()+" is "+elapsedTime +" ms",StepStatus.SUCCESS,new String[] { },startTime);
                            CustomReporting.logReport("Time taken to load the page: "+getCustomPageName()+" is "+elapsedTime +" ms");
                        } catch (Exception ex) {
                                    
                            //CustomReporting.logReport("","", "Timeout during page load","", StepStatus.FAILURE,
                                                                                    //new String[] {  }, startTime, ex);
                            System.out.println(ex);
                        }
    
}

/*******************************************************************
 * Name : _downloadFile
 * Description : Used to save document files at the specified location
 * 
 ********************************************************************/
public void _downloadFile(String FilePath,String fileName )
{
	  long startTime = System.currentTimeMillis();
      try
      {
    
      GenericFunctions.instance().CheckFileExist(FilePath, fileName);
      FilePath=FilePath+fileName;
      Toolkit toolkit=Toolkit.getDefaultToolkit();
      Clipboard clipboard=toolkit.getSystemClipboard();
      StringSelection ss=new StringSelection(FilePath);
      
      clipboard.setContents(ss, ss);
      String result=(String) clipboard.getData(DataFlavor.stringFlavor);
      System.out.println("string from the clipboard "+result);
      Thread.sleep(5000);
      
      Robot robot = new Robot();
      robot.keyPress(KeyEvent.VK_ALT);
      robot.keyPress(KeyEvent.VK_N);
      robot.keyRelease(KeyEvent.VK_ALT);
      robot.keyRelease(KeyEvent.VK_N);
      Thread.sleep(5000);
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_CONTROL);
      Thread.sleep(5000);
      robot.keyPress(KeyEvent.VK_ENTER);
      robot.keyRelease(KeyEvent.VK_ENTER);
      
      Thread.sleep(8000);
      
      CustomReporting.logReport("","", "File downloaded successfully",StepStatus.SUCCESS,new String[] { },startTime);
      }
      catch(Exception ex)
      {
    	  CustomReporting.logReport("","", "File download failed","", StepStatus.FAILURE,
					new String[] {  }, startTime, ex);
    	  System.out.println(ex);
      }
}

public void DownloadFileIELatest( Element element)
{
	
	 try {
           Robot robot = new Robot();
            //get the focus on the element..don't use click since it stalls the driver
           WebElement elm = (WebElement) element.getNative();
            elm.sendKeys("");
            //simulate pressing enter            

            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            //wait for the modal dialog to open            
            Thread.sleep(2000);
            //press s key to save            
            robot.keyPress(KeyEvent.VK_F6);
            robot.keyRelease(KeyEvent.VK_F6);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
            robot.keyPress(KeyEvent.VK_DOWN);
            robot.keyRelease(KeyEvent.VK_DOWN);
            robot.keyPress(KeyEvent.VK_DOWN);
            robot.keyRelease(KeyEvent.VK_DOWN);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            Thread.sleep(2000);
	 	}

 	 	catch (Exception e)
	 	{

            e.printStackTrace();
           
           
	
	 	}
}


public void waitForJSandJQueryToLoad(int defaultWAit) 
{
	
    WebDriverWait wait = new WebDriverWait(getWebDriver(), defaultWAit);
    // wait for jQuery to load
    ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
      @Override
      public Boolean apply(WebDriver driver) {
        try {
            Long r = (Long)((JavascriptExecutor)driver).executeScript("return $.active");
            return r == 0;
        } catch (Exception e) {
            log.info("no jquery present");
            return true;
        }
      }
    };

    // wait for Javascript to load
    ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
      @Override
      public Boolean apply(WebDriver driver) {
        return ((JavascriptExecutor)driver).executeScript("return document.readyState")
        .toString().equals("complete");
      }
    };
    while(!wait.until(jQueryLoad) && wait.until(jsLoad));
    {
    	
    }
  //return wait.until(jQueryLoad) && wait.until(jsLoad);
}


public String RenameFile (String directory, String fileName) 
{
	
	CustomReporting.logReport("","", directory,"", StepStatus.REPORT,
			new String[] {  }, System.currentTimeMillis(), null);	
	
	File dir = new File(directory);
    File[] files = dir.listFiles();
    if (files == null || files.length == 0) {
    	
    	CustomReporting.logReport("","", "File download failed","", StepStatus.FAILURE,
				new String[] {  }, System.currentTimeMillis(), null);
	 
       
    }

    File lastModifiedFile = files[0];
    for (int i = 1; i < files.length; i++) {
       if (lastModifiedFile.lastModified() < files[i].lastModified()) {
           lastModifiedFile = files[i];
       }
    }
   
	// File oldFile = new File(directory+fileName+".pdf");
	/* long startTime = System.currentTimeMillis();
     String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date()).replace(".", "_");
     String renameFile=fileName+"_"+timeStamp+".pdf";*/
     String RenameFile="";
     

     File newFile= new File(directory + "\\" + fileName);
    
     boolean success = lastModifiedFile.renameTo(newFile);

        if(success)
        {
        	RenameFile=fileName;
        	
        }
        return RenameFile;
  
}

	public String  generate_RandomNumber (int Digits) 
	{
		long timeStamp = System.nanoTime(); // to get the current date time value
		
		double randomNum= Math.random() * 1000; // random number generation
		
		long midSeed = (long) (timeStamp * randomNum);
		// mixing up the time and random number. variable timeStamp will be unique variable random will ensure no relation between the numbers
		String s = midSeed + "";
		String subStr = s.substring(0, Digits);
		
		//int finalSeed = Integer.parseInt(subStr);
		
		System.out.println(subStr);
		return subStr;
	}
	
	public String  generate_RandomString (int Charactors) 
	{
		
		Random ran = new Random();
		int top = Charactors;
		char data = ' ';
		String RandomString = "";

		for (int i=0; i<=top; i++)
		{
		  data = (char)(ran.nextInt(25)+97);
		  RandomString = data + RandomString;
		}

		return RandomString;

	}
	
	public int findExcelRow(XSSFSheet sheet, String strTCID,int colNum)
	{
		int intRowNum=0;
		try {

			int numRow=sheet.getLastRowNum();		
			
			for(int i=0;i<=numRow;i++){
				 
				String cellVal=sheet.getRow(i).getCell(colNum).getStringCellValue();
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

	/*******************************************************************
     *     Name              :     _getDataAsList
     *     Description       : Used  to get the data from a field converting them as list items delimited by ;
     *     
     ********************************************************************/
     public List<String> _getDataAsList(String tcID,String sheet,String field,SoftAssert assertSoft) {
		PageData dataSubmission = PageDataManager.instance().getPageData(sheet, tcID);
		String expectedValues = dataSubmission.getData(field);
		String[] arraystr;
		arraystr = expectedValues.split(";");
		List<String> expected = new ArrayList<String>();
		for (String opt : arraystr) {
			expected.add((String)opt.trim());
		}
		return expected;
	}
     
     public LinkedHashMap<Integer, String[]> getDataFromDB(String strQuery, int intColumnSize, String databaseName, String sqlServer) 
    			throws ClassNotFoundException, SQLException {
    		ResultSet res = null;
    		Connection con = null;
    		LinkedHashMap<Integer, String[]> queryData =new LinkedHashMap<Integer, String[]>();

    		try {
    			String dburl = "jdbc:sqlserver://"+sqlServer+";databaseName="+databaseName+";integratedSecurity=true";
    			/*String dbDetailsQADev = "enicdb-dev\\enic_qa;databaseName="+databaseName+";";
    			String dbDetailsStage = "enicdb-stage;databaseName="+databaseName+";";

    			if(environment.equalsIgnoreCase("STAGE")) {
    				dburl = "jdbc:sqlserver://"+dbDetailsStage+"integratedSecurity=true";
    			}
    			else {
    				dburl = "jdbc:sqlserver://"+dbDetailsQADev+"integratedSecurity=true";
    			}*/
    			
    			String dbUserName = "";
    			String dbPassword = "";
    			
    			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    			con =  DriverManager.getConnection(dburl, dbUserName, dbPassword);
    			
    			System.out.println(con + "connected..con");
    			Statement stmt =  con.createStatement();
    			res = stmt.executeQuery(strQuery);
    			
    			int j=0;
    			while (res.next()) 
    			{
    				String[] strFieldValues=new String[intColumnSize];
    					
    				System.out.println("\n Printing data for record: "+j);
    				for(int i=1;i<=intColumnSize;i++) {
    					strFieldValues[i-1]=res.getString(i);
    					System.out.println(strFieldValues[i-1]);
    					
    				}
    				queryData.put(j, strFieldValues);
    				j++;

    			}


    		} catch (Exception e) {
    			CustomReporting.logReport(e);
    			e.printStackTrace();
    		} finally {

    			if (con != null) {
    				con.close();
    			}
    		}
    		return queryData;
    	}
     public void setZoomLevel() throws Exception 
     {
    	 WebElement html = getCustumWebDriver().findElement(By.tagName("html"));
 		 html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
 		
 		 Robot robot = new Robot();
 		 WebDriver driver = getCustumWebDriver();
 		 robot.keyPress(KeyEvent.VK_WINDOWS);
 		 robot.keyPress(KeyEvent.VK_D);
 		 robot.keyRelease(KeyEvent.VK_WINDOWS);
 		 robot.keyRelease(KeyEvent.VK_D);
 		 robot.delay(500);
 		 try
 		 {
 			 driver.manage().window().maximize();
 		 }
 		 catch(Exception e) {}
 		 robot.delay(500);
 		 String currentWindowHandle = driver.getWindowHandle();
 		 ((JavascriptExecutor)driver).executeScript("alert('Test')");
 		 driver.switchTo().alert().accept();
 		 driver.switchTo().window(currentWindowHandle);
 		 robot.delay(500);
 		 for(int i=0;i<5;i++)
 		 {
 			 robot.keyPress(KeyEvent.VK_CONTROL);
 			 robot.keyPress(KeyEvent.VK_MINUS);
 			robot.keyRelease(KeyEvent.VK_CONTROL);
			 robot.keyRelease(KeyEvent.VK_MINUS);
			 
 			 
 		 }
 		 
 				 
 	 }	
     
     public void resetZoomLevel() throws Exception
     {

    	 WebElement html = getCustumWebDriver().findElement(By.tagName("html"));
 		 html.sendKeys(Keys.chord(Keys.CONTROL, "0"));
     }

     public void _actionClick(Element element) throws Exception
     {
    	 WebElement ele = (WebElement)element.getNative();
    	 Actions ac= new Actions(getWebDriver());
    	 ac.moveToElement(ele).click().build().perform();
    	 
     }
     public String elementName(Element element)
 		{
 		
 		return element.toString();
 		}
     public WebElement convertToWebElement(Element element)
     {
    
 		WebElement ele = (WebElement)element.getNative();
 		return ele;
     }
     
     
}
