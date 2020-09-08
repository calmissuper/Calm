package pages;

import org.testng.asserts.SoftAssert;
import org.xframium.page.Page.ElementDefinition;
import org.xframium.page.Page.ScreenShot;
import org.xframium.page.Page.TimeMethod;

public interface Address 
{
	
	@ElementDefinition
	public String label_User="label_User" ;
	@ElementDefinition
	public String btn_MyAccount="btn_MyAccount" ;
	@ElementDefinition
	public String Navi_DeliveryAddress = "Navi_DeliveryAddress";
	@ElementDefinition
	public String Section_DeliveryAddress="Section_DeliveryAddress" ;
	@ElementDefinition
	public String btn_AddNewAddress ="btn_AddNewAddress";
	@ElementDefinition
	public String Section_AddAddress ="Section_AddAddress";
	@ElementDefinition
	public String txt_firstName ="txt_firstName";
	@ElementDefinition
	public String txt_lastName ="txt_lastName";
	@ElementDefinition
	public String txt_mobileNumber ="txt_mobileNumber";
	@ElementDefinition
	public String txt_City ="txt_City";
	@ElementDefinition
	public String txt_houseNo ="txt_houseNo";
	@ElementDefinition
	public String txt_area ="txt_area";
	@ElementDefinition
	public String txt_pin ="txt_pin";
	@ElementDefinition
	public String btn_confirmLocation = "btn_confirmLocation";
	@ElementDefinition
	public String btn_add ="btn_add";
	@ElementDefinition
	public String label_addedAddress ="label_addedAddress";
	@ElementDefinition
	public String btn_deleteAddress ="btn_deleteAddress";
	@ElementDefinition
	public String btn_editAddress ="btn_editAddress";
	@ElementDefinition
	public String Section_EditAddress ="Section_EditAddress";
	@ElementDefinition
	public String btn_edit ="btn_edit";
	@ElementDefinition
	public String label_editedAddress ="label_editedAddress";
	@ElementDefinition
	public String btn_deleteConfirm ="btn_deleteConfirm";
	@ElementDefinition
	public String btn_deleteCancel ="btn_deleteCancel";
	
	
	
	
	
	
	
	
	@TimeMethod
	@ScreenShot
	public void navigateDeliveryAddress(String tcID, SoftAssert softAssert, String DeviceName, String user) throws Exception;
	public void addAddress(String tcID, SoftAssert softAssert, String DeviceName, String user) throws Exception;
	public void updateAddress(String tcID, SoftAssert softAssert, String DeviceName, String user) throws Exception;
	public void deleteAddress(String tcID, SoftAssert softAssert, String DeviceName, String user) throws Exception;
	

}
