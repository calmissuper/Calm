package functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xframium.device.DeviceManager;
import org.xframium.device.ng.AbstractSeleniumTest;
import org.xframium.page.StepStatus;
import org.xframium.page.element.Element;

import utility.CustomReporting;
import com.jayway.jsonpath.internal.JsonFormatter;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;


public class CustomFunctions extends AbstractSeleniumTest{
	
private static CustomFunctions singleton = new CustomFunctions();
public static GenericFunctions func = GenericFunctions.instance();

	private CustomFunctions() {

	}

	public static CustomFunctions instance() {
		return singleton;
	}
	
	
	@SuppressWarnings("finally")
	public String getCustomElementName(Element el, String strPage)  {

		//return el.toString();
		
        String strElementName=null;
         try {
        	 
        File directory = new File("./");
 		System.out.println(directory.getAbsolutePath());
 		String strDirectoryPath =directory.getAbsolutePath().split("//.")[0].replace(".", "");
 		String xmlPath=strDirectoryPath+"resources/pageElements.xml";
 		
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlPath);
        
        XPathFactory xPathfactory = XPathFactory.newInstance();
	  	XPath xpath = xPathfactory.newXPath();
	  	
	  	String strVal=el.toString().split("\\{")[1].split("\\}")[0].trim();
	  	String strPageName=strPage;
	  	String strXPath="//site/page[@name='" + strPageName + "']/element[";

	  	if (strVal.contains("'")){
	  		
	  		
	  		String[] arrList=strVal.split("'");
	  		System.out.println(arrList.length);
	  		for (int i=0;i<=arrList.length-1;i++) {
	  			strXPath=strXPath+"contains(@value,'" + arrList[i] +"') and ";
	  		}
	  		strXPath=strXPath.substring(0, strXPath.length()-5)+"]/@name";
	  		
	  	}
	  	else {
	  		strXPath = strXPath+"contains(@value,'" + strVal +"')" +"]/@name";
	  	}
	  	
	  	  System.out.println(strXPath);
	  	  XPathExpression expr = xpath.compile(strXPath);
	  	  
	  	  strElementName=(String) expr.evaluate(doc, XPathConstants.STRING);
	  	  
		  System.out.println(strElementName);
	  	
        return strElementName;
         }
          catch(Exception ex) {
          //throw ex; 
         }
         finally {
        	 return strElementName; 
         }
	}
	

public Response customWebServiceCall1(String inputStr, String authKey, String contentType, String endPointUrl) 
 			throws Exception{
 		Response response=null;
 		long startTime = System.currentTimeMillis();
 		
 		try {
 			RestAssured.useRelaxedHTTPSValidation();
 			RequestSpecification requestSpecification = RestAssured.given();
 			requestSpecification.header("Authorization", authKey);
 			requestSpecification.headers("content-type", contentType);
 			requestSpecification.body(inputStr);
 			response = requestSpecification.post(endPointUrl);
 			//CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
 		}catch(Exception ex) {
 			//ex.printStackTrace();
 			CustomReporting.logReport("","", "Error occurred while retrieving TestRun id from qTest", "", StepStatus.FAILURE,
 					new String[] { }, startTime, ex);
 		}
 		return response;

 	}


public static Response customWebServiceCall(String authKey, String contentType, String endPointUrl) 
                     throws Exception{
              Response response=null;
              long startTime = System.currentTimeMillis();
              
              try {
                     RestAssured.useRelaxedHTTPSValidation();
                     RequestSpecification requestSpecification = RestAssured.given();
                     requestSpecification.header("Authorization", authKey);
                     requestSpecification.headers("content-type", contentType);
                     response = requestSpecification.get(endPointUrl);
              }catch(Exception ex) {
                     
                     System.out.println("Response retrieved is null");
                     System.out.println("Error occurred while retrieving the response:  " +
                              "\n Error Description: " + ex.toString());
              }
              return response;

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
public String getKeyValue(Response res, String key) throws Exception {
	try {
		String value = null;
		if (!key.equalsIgnoreCase("null")) {
			value = res.path(key);
		}
		return value;
	} catch (Exception ex) {
		throw ex;
	}
}
/*==============POST WebService Call===================== */
public Response webServiceCall(String path, String authKey, String contentType, String endPointUrl) 
		throws Exception{
	Response response=null;
	String content;
	
	try {
		File directory = new File("@/");
		String strDirectoryPath =directory.getAbsolutePath().split("\\@") [0];
		String strDestinationPath=DeviceManager.instance().getConfigurationProperties().getProperty("payLoad.path"); 
		String payLoadPath = strDirectoryPath + strDestinationPath + path;
		content = new String(Files.readAllBytes(Paths.get(payLoadPath)));
		RequestSpecification requestSpecification = RestAssured.given();
		requestSpecification.header("Authorization", authKey);
		requestSpecification.headers("content-type", contentType);
		requestSpecification.body(content);
		response = requestSpecification.post(endPointUrl);
		CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
	}catch(Exception ex) {
		ex.printStackTrace();
	}
	return response;

}
public Response customWebServiceCall(String inputStr, String authKey, String contentType, String endPointUrl) 
		throws Exception{
	Response response=null;
	
	
	try {
		
		RequestSpecification requestSpecification = RestAssured.given();
		requestSpecification.header("Authorization", authKey);
		requestSpecification.headers("content-type", contentType);
		requestSpecification.body(inputStr);
		response = requestSpecification.post(endPointUrl);
		CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
	}catch(Exception ex) {
		ex.printStackTrace();
	}
	return response;

}
public Response customWebServiceCall(String inputStr, String authKey, String contentType, String endPointUrl, String queryValue) 
		throws Exception{
	Response response=null;
	
	
	try {
		
		RequestSpecification requestSpecification = RestAssured.given();
		requestSpecification.header("Authorization", authKey);
		requestSpecification.headers("content-type", contentType);
		requestSpecification.body(inputStr);
		response = requestSpecification.post(endPointUrl+queryValue);
		CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
	}catch(Exception ex) {
		ex.printStackTrace();
	}
	return response;

}

public Response customWebServiceCall(String inputStr, String authKey, String contentType, String endPointUrl,
		String queryParameter, String value) throws Exception {
	Response response = null;
	String content;

	try {

		RequestSpecification requestSpecification = RestAssured.given();
		requestSpecification.header("Authorization", authKey);
		requestSpecification.headers("content-type", contentType);
		requestSpecification.body(inputStr);
		response = requestSpecification.parameters(queryParameter, value).put(endPointUrl);
		CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	return response;

}

public Response webServiceCall(String path, String authKey, String contentType, String endPointUrl,boolean updateFlag,String pKey, String cKey, String toUpdateKey, String toUpdateValue) throws Exception{
	Response response=null;
	String content;
	
	try {
		File directory = new File("@/");
		String strDirectoryPath =directory.getAbsolutePath().split("\\@") [0];
		String strDestinationPath=DeviceManager.instance().getConfigurationProperties().getProperty("payLoad.path"); 
		String payLoadPath = strDirectoryPath + strDestinationPath + path;
		content = new String(Files.readAllBytes(Paths.get(payLoadPath)));
		if(updateFlag) {
			String updatedContent = updateInputRequest(content, pKey, cKey, toUpdateKey, toUpdateValue);
			RequestSpecification requestSpecification = RestAssured.given();
			requestSpecification.header("Authorization", authKey);
			requestSpecification.headers("content-type", contentType);
			requestSpecification.body(updatedContent);
			response = requestSpecification.post(endPointUrl);
			CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
		}else {
		RequestSpecification requestSpecification = RestAssured.given();
		requestSpecification.header("Authorization", authKey);
		requestSpecification.headers("content-type", contentType);
		requestSpecification.body(content);
		response = requestSpecification.post(endPointUrl);
		CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
		}
	}catch(Exception ex) {
		ex.printStackTrace();
	}
	return response;

}
/*==============GET/PUT WebService Call===================== */
//use this one
public Response webServiceCall(String inputStr, String authKey, String contentType, String endPointUrl,
		String queryParameter, String value, String action) {
	Response response=null;
	
	try {
		if (action.equalsIgnoreCase("put")) {
			/*File directory = new File("@/");
			String strDirectoryPath =directory.getAbsolutePath().split("\\@") [0];
			String strDestinationPath=DeviceManager.instance().getConfigurationProperties().getProperty("payLoad.path"); 
			String payLoadPath = strDirectoryPath + strDestinationPath + path;
			content = new String(Files.readAllBytes(Paths.get(payLoadPath)));*/
			RestAssured.useRelaxedHTTPSValidation(); 
			RequestSpecification requestSpecification = RestAssured.given();
			requestSpecification.header("Authorization", authKey);
			requestSpecification.headers("content-type", contentType);
			requestSpecification.body(inputStr);
			
		 response = requestSpecification.parameters(queryParameter, value).put(endPointUrl);
		 CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
		
		} else if (action.equalsIgnoreCase("get")) {
			RestAssured.useRelaxedHTTPSValidation(); 
			RequestSpecification requestSpecification = RestAssured.given();
			requestSpecification.header("Authorization", authKey);
			requestSpecification.headers("content-type", contentType);
			response = requestSpecification.parameters(queryParameter, value).get(endPointUrl);
			CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
			
		}

	} catch (Exception ex) {
		ex.printStackTrace();
	}
	return response;

}

//two values
public Response webServiceCall_1(String path, String authKey, String contentType, String endPointUrl,
		String queryParameter,String queryParameter1,String value, String value1, String action) {
	
	Response response=null;
	String content;
	try {
		if (action.equalsIgnoreCase("put")) {
			File directory = new File("@/");
			String strDirectoryPath =directory.getAbsolutePath().split("\\@") [0];
			String strDestinationPath=DeviceManager.instance().getConfigurationProperties().getProperty("payLoad.path"); 
			String payLoadPath = strDirectoryPath + strDestinationPath + path;
			content = new String(Files.readAllBytes(Paths.get(payLoadPath)));
			RequestSpecification requestSpecification = RestAssured.given();
			requestSpecification.header("Authorization", authKey);
			requestSpecification.headers("content-type", contentType);
			requestSpecification.body(content);
		 response = requestSpecification.parameters(queryParameter, value).parameters(queryParameter1, value1).put(endPointUrl);
		 CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
		
		} else if (action.equalsIgnoreCase("get")) {
			RequestSpecification requestSpecification = RestAssured.given();
			requestSpecification.header("Authorization", authKey);
			requestSpecification.headers("content-type", contentType);
			response = requestSpecification.parameters(queryParameter, value).parameters(queryParameter1, value1).get(endPointUrl);
		
			CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
			
		}

	} catch (Exception ex) {
		ex.printStackTrace();
	}
	return response;

}
//two values
	public Response webServiceCall(String path, String authKey, String contentType, String endPointUrl,
			 String action) {
		Response response=null;
		String content;
		try {
			if (action.equalsIgnoreCase("put")) {
				File directory = new File("@/");
				String strDirectoryPath =directory.getAbsolutePath().split("\\@") [0];
				String strDestinationPath=DeviceManager.instance().getConfigurationProperties().getProperty("payLoad.path"); 
				String payLoadPath = strDirectoryPath + strDestinationPath + path;
				content = new String(Files.readAllBytes(Paths.get(payLoadPath)));
				RestAssured.useRelaxedHTTPSValidation(); 
				RequestSpecification requestSpecification = RestAssured.given();
				requestSpecification.header("Authorization", authKey);
				requestSpecification.headers("content-type", contentType);
				requestSpecification.body(content);
			 response = requestSpecification.put(endPointUrl);
			 CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
			
			} else if (action.equalsIgnoreCase("get")) {
				RestAssured.useRelaxedHTTPSValidation(); 
				RequestSpecification requestSpecification = RestAssured.given();
				requestSpecification.header("Authorization", authKey);
				requestSpecification.headers("content-type", contentType);
				response = requestSpecification.get(endPointUrl);
				CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
				
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(response.asString());
		return response;

	}
//
public Response webServiceCall(String path, String authKey, String contentType, String endPointUrl,
		String queryParameter, String value, boolean updateFlag,String pKey, String cKey, String toUpdateKey, String toUpdateValue) {
	Response response=null;
	String content;
	String updatedContent="";
	try {
		
			File directory = new File("@/");
			String strDirectoryPath =directory.getAbsolutePath().split("\\@") [0];
			String strDestinationPath=DeviceManager.instance().getConfigurationProperties().getProperty("payLoad.path"); 
			String payLoadPath = strDirectoryPath + strDestinationPath + path;
			content = new String(Files.readAllBytes(Paths.get(payLoadPath)));
			if(updateFlag) {
				updatedContent = updateInputRequest(content, pKey, cKey, toUpdateKey, toUpdateValue);
				RequestSpecification requestSpecification = RestAssured.given();
				requestSpecification.header("Authorization", authKey);
				requestSpecification.headers("content-type", contentType);
				requestSpecification.body(updatedContent);
			 response = requestSpecification.parameters(queryParameter, value).put(endPointUrl);
			 CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
			}else {
			RequestSpecification requestSpecification = RestAssured.given();
			requestSpecification.header("Authorization", authKey);
			requestSpecification.headers("content-type", contentType);
			requestSpecification.body(content);
		 response = requestSpecification.parameters(queryParameter, value).put(endPointUrl);
		 CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
			}
	
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	return response;

}
public String getUpdateInputAsString(String path) {
	String inputStr="";
	try {
		File directory = new File("@/");
		String strDirectoryPath =directory.getAbsolutePath().split("\\@") [0];
		String strDestinationPath=DeviceManager.instance().getConfigurationProperties().getProperty("payLoad.path"); 
		String payLoadPath = strDirectoryPath + strDestinationPath + path;
		inputStr = new String(Files.readAllBytes(Paths.get(payLoadPath)));
	}catch (Exception ex) {
		ex.printStackTrace();
	}
	return inputStr;
}

public String updateInputRequest(String content, String parentKey, String childKey, String toUpdateKey,
		String toUpdateValue) {
	String formattedJson = "";
	try {
		JSONObject object = new JSONObject(content);
		if(!childKey.equals("")) {
		@SuppressWarnings("unused")
		JSONObject submissionObject = object.getJSONObject(parentKey).getJSONObject(childKey).put(toUpdateKey,
				toUpdateValue);
		formattedJson = JsonFormatter.prettyPrint(object.toString());
		}else {
			@SuppressWarnings("unused")
			JSONObject submissionObject = object.getJSONObject(parentKey).put(toUpdateKey,toUpdateValue);
			formattedJson = JsonFormatter.prettyPrint(object.toString());
		}

	} catch (Exception ex) {
		ex.printStackTrace();
	}
	return formattedJson;

}
public LinkedHashMap<Integer, String[]> getDataFromDB(String strQuery, int intColumnSize) throws ClassNotFoundException, SQLException {
	ResultSet res = null;
	Connection con = null;
	LinkedHashMap<Integer, String[]> queryData =new LinkedHashMap<Integer, String[]>();
	
	try {
		//String dburl = "jdbc:sqlserver://ENDWSQLD1\\PR;databaseName=ProducerMDMDB;integratedSecurity=true";
		//String dburl = "jdbc:sqlserver://ENDWSQLS;databaseName=ProducerMDMDB;integratedSecurity=true";
		String dburl = "jdbc:sqlserver://ENDWSQLS;databaseName=ProducerMDMDB;integratedSecurity=true";
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
		e.printStackTrace();
	} finally {

		if (con != null) {
			con.close();
		}
	}
	return queryData;
}
public LinkedHashMap<Integer, String[]> getDataFromDB1(String strQuery, int intColumnSize) throws ClassNotFoundException, SQLException {
	ResultSet res = null;
	Connection con = null;
	LinkedHashMap<Integer, String[]> queryData =new LinkedHashMap<Integer, String[]>();
	
	try {
		//String dburl = "jdbc:sqlserver://ENICDB-DEV\\ENIC_QA;databaseName=MasterReferenceDataHub;integratedSecurity=true";
		String dburl = "jdbc:sqlserver://enicdb-stage;databaseName=MasterReferenceDataHub;integratedSecurity=true";
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
            		//System.out.println(strFieldValues[i-1]);
            	}
            	queryData.put(j, strFieldValues);
            	j++;
                  
             }
            	            

	} catch (Exception e) {
		e.printStackTrace();
	} finally {

		if (con != null) {
			con.close();
		}
	}
	return queryData;
}

//check all steps verification
public boolean allStepsResult(List<Boolean> value) {
	for(boolean arr1:value) {
		if(!arr1) {
			return false;
		}
	}
	return true;
}
public Response webServiceCall(String inputStr, String authKey, String contentType, String endPointUrl, 
		String value, String action) {
	Response response=null;
	
	try {
		if (action.equalsIgnoreCase("put")) {
			
			RequestSpecification requestSpecification = RestAssured.given();
			requestSpecification.header("Authorization", authKey);
			requestSpecification.headers("content-type", contentType);
			requestSpecification.body(inputStr);
			response = requestSpecification.put(endPointUrl+value);
			CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
		
		} 
		else if (action.equalsIgnoreCase("get")) {
			RequestSpecification requestSpecification = RestAssured.given();
			requestSpecification.header("Authorization", authKey);
			requestSpecification.headers("content-type", contentType);
			response = requestSpecification.get(endPointUrl+value);
			CustomReporting.logReport( endPointUrl + " Response time " + response.getTime() + " ms");
			
		}

	} catch (Exception ex) {
		ex.printStackTrace();
	}
	return response;

	}
public String getFromProperty(String Key) throws IOException {
	long startTime = System.currentTimeMillis();
	Properties prop = new Properties();
	//File directory = new File("@/");
	//String strDirectoryPath =directory.getAbsolutePath().split("\\@") [0];
	String propPath = DeviceManager.instance().getConfigurationProperties().getProperty("property.path");
	//propPath = strDirectoryPath + propPath;
	
	InputStream input=new FileInputStream(propPath);
	prop.load(input);
	String strVal=prop.getProperty(Key);
	return strVal;
}
public Map<String, String> getListOfKeyValueFromPayLoad(String jsonFileName, String parentkey, String childkey)
		throws Exception {
	try {
		String value = null;
		File directory = new File("@/");
		String strDirectoryPath = directory.getAbsolutePath().split("\\@")[0];
		String strDestinationPath = DeviceManager.instance().getConfigurationProperties()
				.getProperty("payLoad.path");
		String payLoadPath = strDirectoryPath + strDestinationPath + jsonFileName;
		String content = new String(Files.readAllBytes(Paths.get(payLoadPath)));
		JSONObject responseObj = new JSONObject(content);
		JSONObject subObj = responseObj.getJSONObject(parentkey).getJSONObject(childkey);
		Map<String, String> mapList = new LinkedHashMap<String, String>();
		Iterator<?> keys = subObj.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			value = subObj.get(key).toString();
			mapList.put(key, value);
		}
		return mapList;
	} catch (Exception ex) {
		throw ex;
	}

}

public Map<String, String> getListOfKeyValueFromInputString(String inputStr, String parentkey, String childkey)
		throws Exception {
	try {
		String value = null;
		JSONObject responseObj = new JSONObject(inputStr);
		JSONObject subObj = responseObj.getJSONObject(parentkey).getJSONObject(childkey);
		Map<String, String> mapList = new LinkedHashMap<String, String>();
		Iterator<?> keys = subObj.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			value = subObj.get(key).toString();
			mapList.put(key, value);
		}
		return mapList;
	} catch (Exception ex) {
		throw ex;
	}

}
public Map<String, String> getListOfKeyValue(String res, String parentkey) throws Exception {
	try {
		String value = null;

		JSONObject responseObj = new JSONObject(res);
		JSONObject subObj = responseObj.getJSONObject(parentkey);
		Map<String, String> mapList = new LinkedHashMap<String, String>();
		Iterator<?> keys = subObj.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			value = subObj.get(key).toString();
			mapList.put(key, value);
		}

		return mapList;
	} catch (Exception ex) {
		throw ex;
	}
}
}
