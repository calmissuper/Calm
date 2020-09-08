package pages.spi;

import org.testng.asserts.SoftAssert;
import org.xframium.page.StepStatus;
import org.xframium.page.data.PageData;
import org.xframium.page.data.PageDataManager;

import pages.Address;
import utility.CustomAbstractPage;
import utility.CustomReporting;
import utility.Report;

public class AddressImpl extends CustomAbstractPage implements Address 
{

	@Override
	public void navigateDeliveryAddress(String tcID, SoftAssert softAssert, String DeviceName, String user) throws Exception 
	{
		func._waitForPageToLoad(getWebDriver(), 200L);
		func._click(getElement(label_User));
		func._actionClick(getElement(btn_MyAccount));
		func._waitForPageToLoad(getWebDriver(), 100L);
		func._click(getElement(Navi_DeliveryAddress));
		 if(func._isVisible(getElement(Section_DeliveryAddress)))
		    {
			 	Report.instance().log(tcID, "PASS", "Entered in to Delivery Address Section ");
			 	//CustomReporting.logReport("Entered in to Delivery Address Section ", StepStatus.SUCCESS);
		    }
		 else
		    {	Report.instance().log(tcID, "FAIL", "Not Entered in to Delivery Address Section ");
		    	//CustomReporting.logReport("Not Entered in to Delivery Address Section ", StepStatus.FAILURE);	
		    }
	}

	@Override
	public void addAddress(String tcID, SoftAssert softAssert, String DeviceName, String user) throws Exception 
	{	
		PageData dataAddress = PageDataManager.instance().getPageData("Address",tcID);
		func._click(getElement(btn_AddNewAddress));
		if(func._isVisible(getElement(Section_AddAddress)))
		{
			Report.instance().log(tcID, "PASS", "Entered in to Add Address Section");
		 	//CustomReporting.logReport("Entered in to Add Address Section ", StepStatus.SUCCESS);
	    }
		else
	    {
			Report.instance().log(tcID, "FAIL", "Not Entered in to Add Address Section");
	    	//CustomReporting.logReport("Not Entered in to Add Address Section ", StepStatus.FAILURE);	
	    }
		if(dataAddress.getData("Action").equalsIgnoreCase("Add"))
		{
		func._wait(2000);
		Thread.sleep(20000);
		func._waitForPageToLoad(getWebDriver(), 1000L);
		
		_setValue(getElement(txt_firstName), dataAddress.getData("FirstName"));
		_setValue(getElement(txt_lastName), dataAddress.getData("LastName"));
		_setValue(getElement(txt_mobileNumber), dataAddress.getData("MobileNumber"));
		_setValue(getElement(txt_houseNo), dataAddress.getData("HouseNo"));
		_setValue(getElement(txt_area), dataAddress.getData("Area"));
		//_setValue(getElement(txt_City), dataAddress.getData("City"));
		_setValue(getElement(txt_pin), dataAddress.getData("Pin"));
		_click(getElement(btn_add));
		func._waitForPageToLoad(getWebDriver(), 1000L);
		_click(getElement(btn_confirmLocation));
		if(_isVisible(getElement(btn_confirmLocation)))
		{
			//_click(getElement(btn_confirmLocation));
		}
		
		func._waitForPageToLoad(getWebDriver(), 50L);
		if(_isVisible(getElement(label_addedAddress).addToken("tkn_address",dataAddress.getData("FirstName"))))
		{
			Report.instance().log(tcID, "PASS", "Address added sucessfully ");
		 	//CustomReporting.logReport("Address added sucessfully ", StepStatus.SUCCESS);
	    }
		else
	    {
			Report.instance().log(tcID, "FAIL", "Address not added");
	    	//CustomReporting.logReport("Address not added ", StepStatus.FAILURE);	
	    }
		}
		
	}

	@Override
	public void updateAddress(String tcID, SoftAssert softAssert, String DeviceName, String user) throws Exception 
	{
		PageData dataAddress = PageDataManager.instance().getPageData("Address",tcID);
		if(_isVisible(getElement(btn_editAddress).addToken("tkn_address",dataAddress.getData("FirstName"))))
		{
			_click(getElement(btn_editAddress).addToken("tkn_address",dataAddress.getData("FirstName")));
		}
		if(func._isVisible(getElement(Section_EditAddress)))
		{
			Report.instance().log(tcID, "PASS", "Entered in to Edit Address Section");
		 	//CustomReporting.logReport("Entered in to Add Address Section ", StepStatus.SUCCESS);
	    }
		else
	    {
			Report.instance().log(tcID, "FAIL", "Not Entered in to Edit Address Section");
	    	//CustomReporting.logReport("Not Entered in to Add Address Section ", StepStatus.FAILURE);	
	    }
		if(dataAddress.getData("Action").equalsIgnoreCase("Update"))
		{
		func._wait(2000);
		Thread.sleep(20000);
		func._waitForPageToLoad(getWebDriver(), 1000L);
		_setValue(getElement(txt_houseNo), dataAddress.getData("HouseNo"));
		_click(getElement(btn_edit));
		func._waitForPageToLoad(getWebDriver(), 50L);
		}
		_click(getElement(btn_confirmLocation));
		if(_isVisible(getElement(btn_confirmLocation)))
		{
		//	_click(getElement(btn_confirmLocation));
		}
		if(_isVisible(getElement(label_editedAddress).addToken("tkn_address",dataAddress.getData("HouseNo"))))
		{
			Report.instance().log(tcID, "PASS", "Address Edited sucessfully ");
		 	//CustomReporting.logReport("Address added sucessfully ", StepStatus.SUCCESS);
	    }
		else
	    {
			Report.instance().log(tcID, "FAIL", "Address not Edited");
	    	//CustomReporting.logReport("Address not added ", StepStatus.FAILURE);	
	    }
	}
		
		
	


	@Override
	public void deleteAddress(String tcID, SoftAssert softAssert, String DeviceName, String user) throws Exception 
	{
		PageData dataAddress = PageDataManager.instance().getPageData("Address",tcID);
		
		if(_isVisible(getElement(btn_deleteAddress).addToken("tkn_address",dataAddress.getData("FirstName"))))
		{
			_click(getElement(btn_deleteAddress).addToken("tkn_address",dataAddress.getData("FirstName")));
		}
		_click(getElement(btn_deleteConfirm));
		
		if(_isVisible(getElement(label_addedAddress).addToken("tkn_address",dataAddress.getData("FirstName"))))
		{
			Report.instance().log(tcID, "FAIL", "Address not Deleted");
				//CustomReporting.logReport("Address added sucessfully ", StepStatus.SUCCESS);
	    }
		else
	    {
			Report.instance().log(tcID, "PASS", "Address Deleted sucessfully ");
			//CustomReporting.logReport("Address not added ", StepStatus.FAILURE);	
	    }
		
		
	}
	

}
