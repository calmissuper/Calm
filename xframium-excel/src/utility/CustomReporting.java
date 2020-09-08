package utility;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xframium.device.ng.AbstractSeleniumTest;
import org.xframium.page.PageManager;
import org.xframium.page.StepStatus;

import functions.GenericFunctions;

public class CustomReporting {

	private static Log log = LogFactory.getLog( AbstractSeleniumTest.class );
	private static CustomReporting singleton=new CustomReporting();
	
	public static CustomReporting instance()
	{
		return singleton;
	}
	
	private CustomReporting(){}
	
	private static long getCurrentTimeStamp()	{
		return System.currentTimeMillis();
	}
	
	private static long getExecutionTime(long executionStartTimeStamp)	{
		if (executionStartTimeStamp == 0)	{ return 0;}
		return getCurrentTimeStamp() - executionStartTimeStamp;
	}
	
	public static List<String> errorMessages = new ArrayList<String>();
	public static int getErrorMessageCount()	{
		return errorMessages.size();
	}
	
	
	public static void logReport(Exception exception)	{
		//if(ExceptionUtils.indexOfType(exception, IgnoreMLQException.class)==-1)
		//{
			logReport("","","errMessage","", StepStatus.FAILURE,new String[]{""},getCurrentTimeStamp(),exception);
		//}		
	}
	
	public static void logReport(String strLogMsg)	{
		logReport("","",strLogMsg,strLogMsg, StepStatus.REPORT,new String[]{},getCurrentTimeStamp(),null);
    }
	
	public static void logReport(String msgDetail,StepStatus blnStatus)	{
		logReport("","",msgDetail,msgDetail, blnStatus,new String[]{""},getCurrentTimeStamp(),null);
    }
	
	
	public static void logReport(String pageName,String objName,String type, StepStatus blnStatus)	{		
		logReport(pageName,objName,type,"", blnStatus,new String[]{""},getCurrentTimeStamp(),null);
	}
	
	public static void logReport(String pageName,String objName,String type, StepStatus blnStatus, long startTime)	{
		logReport(pageName,objName,type,"", blnStatus,new String[]{""},startTime,null);
		
	}
	
	public static void logReport(String pageName,String objName,String type, StepStatus blnStatus,String[] parameterArray)	{		
		logReport(pageName,objName,type,"", blnStatus,parameterArray,getCurrentTimeStamp(),null);
	}
	
	public static void logReport(String pageName,String objName,String type, StepStatus blnStatus,String[] parameterArray, long startTime)	{
		logReport(pageName,objName,type,"", blnStatus,parameterArray,startTime,null);		
	}

  /* public static void logReport(String pageName, String objName, String type, String msgDetail, StepStatus blnStatus,String[] parameterArray,long startTime){
		
		GenericFunctions func = GenericFunctions.instance();
		String executionId = PageManager.instance().getExecutionId(func.getCustumWebDriver());
		String deviceName = PageManager.instance().getDeviceName(func.getCustumWebDriver());
		PageManager.instance().addExecutionLog( executionId,deviceName, pageName, objName, type, System.currentTimeMillis(), System.currentTimeMillis() - startTime, blnStatus," ",null, 0, "View Error Detail", false, parameterArray);
		
		if (blnStatus.name().equals("FAILURE")) {	errorMessages.add(msgDetail);	} 
		logConsole(pageName, objName, type, msgDetail, startTime);
	}*/

	public static void logReport(String pageName, String objName, String type, String msgDetail, StepStatus blnStatus,String[] parameterArray,long startTime, Exception ex){
		
		GenericFunctions func = GenericFunctions.instance();
		String executionId = PageManager.instance().getExecutionId(func.getCustumWebDriver());
		String deviceName = PageManager.instance().getDeviceName(func.getCustumWebDriver());
		PageManager.instance().addExecutionLog( executionId,deviceName, pageName, objName, type, System.currentTimeMillis(), System.currentTimeMillis() - startTime, blnStatus," ", ex, 0, "View Error Detail", false, parameterArray);
		
		if (blnStatus.name().equals("FAILURE")) {	errorMessages.add(msgDetail);	} 
		logConsole(pageName, objName, type, msgDetail, startTime);
	}

	
	/**
	 * only writes to console, not to output file. use only for debugging purposes
	 * @param area
	 * @param objName
	 * @param strLogMsg
	 * @param msgDetail
	 * @param executionStartTimeStamp
	 */
	public static void logConsole(String area, String objName, String strLogMsg, String msgDetail, long executionStartTimeStamp)	{
		String logmsg =  ">>> "
				.concat(area).concat(" ")
				.concat(objName).concat(" ").concat(strLogMsg)
				.concat(" ").concat(msgDetail);
		if (executionStartTimeStamp != 0)	{	
			logmsg = logmsg.concat(" Execution time: ").concat(Long.toString(getExecutionTime(executionStartTimeStamp)));		}
		log.info(logmsg);
	}
	
	//new String[] { attributeName, returnValue }
}
