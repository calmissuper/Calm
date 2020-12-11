package pages.spi;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.asserts.SoftAssert;
import org.xframium.page.StepStatus;
import org.xframium.page.data.PageData;
import org.xframium.page.data.PageDataManager;

import utility.CustomAbstractPage;
import utility.CustomReporting;

public class DB extends CustomAbstractPage implements pages.DB
{
	
	
	@Override
	public void validateDataDBUI(String tcID, SoftAssert softAssert, String DeviceName) throws Exception 
	{
		
		PageData dataAccount = PageDataManager.instance().getPageData("NewBusiness",tcID);
		
        LinkedHashMap<Integer, String[]>AccountDHWDBvalues =null;
        //Creating Map for the UI Values an storing them
        Map<String, String> newBusinessUIDataMap = new LinkedHashMap<>();
        Map<String, String> newBusinessAccountDBDataMap = new LinkedHashMap<>();
         Boolean isMatched =true;
      
        /***New Business Data Validation***/
		//New Business UI Data 
		String  accountNumber = dataAccount.getData("AccountNumber").split("-")[0];
		String accountVersionNumber=dataAccount.getData("AccountNumber").split("-")[1];
		String stateName= dataAccount.getData("MailingState");
		String countryName=dataAccount.getData("Country");
		newBusinessUIDataMap.put("AccountNumber", accountNumber);
		newBusinessUIDataMap.put("AccountVersionNumber", accountVersionNumber);
		newBusinessUIDataMap.put("InsuredName", dataAccount.getData("InsuredName"));
		newBusinessUIDataMap.put("MailingStreetAddress", dataAccount.getData("MailingStreetAddress"));
		newBusinessUIDataMap.put("MailingCity", dataAccount.getData("MailingCity"));
		newBusinessUIDataMap.put("MailingState", dataAccount.getData("MailingState"));
		newBusinessUIDataMap.put("Country", dataAccount.getData("Country"));
		newBusinessUIDataMap.put("MailingZipCode", dataAccount.getData("MailingZipCode"));
		newBusinessUIDataMap.put("MailingZipAddon", dataAccount.getData("MailingZipAddon"));
		newBusinessUIDataMap.put("System_Generate_FEIN", dataAccount.getData("System_Generate_FEIN"));
		Thread.sleep(2000);
        
       try
        {
                   	AccountDHWDBvalues =func.getDataFromDB("SELECT * FROM [EscapeDB].[dbo].[SUB_Account]  WHERE Account_Nbr ='"+accountNumber+"'", 36, "EscapeDB","enicdb-dev\\enic_qa");	 
            
               
			String[] AccountStageDHWDBvalues1;
              		AccountStageDHWDBvalues1=AccountDHWDBvalues.get(0);
              		newBusinessAccountDBDataMap.put("AccountNumber", AccountStageDHWDBvalues1[1]);
               	newBusinessAccountDBDataMap.put("AccountVersionNumber", AccountStageDHWDBvalues1[2]);
       		newBusinessAccountDBDataMap.put("InsuredName", AccountStageDHWDBvalues1[3]);		
       		newBusinessAccountDBDataMap.put("MailingStreetAddress", AccountStageDHWDBvalues1[4]);
       		newBusinessAccountDBDataMap.put("MailingCity", AccountStageDHWDBvalues1[5]);
       		newBusinessAccountDBDataMap.put("MailingZipCode", AccountStageDHWDBvalues1[7]);
       		newBusinessAccountDBDataMap.put("MailingZipAddon", AccountStageDHWDBvalues1[8]);
       		newBusinessAccountDBDataMap.put("System_Generate_FEIN", AccountStageDHWDBvalues1[21]);
       		for(String key : newBusinessUIDataMap.keySet())
       		{
       			String valueUI = newBusinessUIDataMap.get(key);
       			String valueDB = newBusinessAccountDBDataMap.get(key);
       			
       			if(valueUI!=null && valueDB!=null)
       			{
       				if(!(valueUI.equalsIgnoreCase(valueDB)))
       				{

       					isMatched=false;
       					CustomReporting.logReport("","", "Created Account Data for  "+key+" the UI value "+valueUI+" and DB Value "+valueDB+" are NOT Equal","", StepStatus.FAILURE,new String[] {  }, System.currentTimeMillis(), null);	
       				}
       			}
       			else {
       				CustomReporting.logReport("Created Account Data for  "+key+" the UI value "+valueUI+" and DB Value "+valueDB);
       			}
       		}
       		if(isMatched)
       		{
					CustomReporting.logReport("","", "All Account Details Matched","", StepStatus.SUCCESS,new String[] {  }, System.currentTimeMillis(), null);
					
       		}
       	}
        catch(Exception e)
        {   
       	 CustomReporting.logReport("","", "Account created in stage env is not exist in the DWH DB","", StepStatus.FAILURE,new String[] { }, System.currentTimeMillis(), null);
        //throw new RuntimeException(); 
       	 
        }
    
		
		
	}

}
