package pages.spi;

import org.testng.asserts.SoftAssert;

import pages.Search;
import utility.CustomAbstractPage;

public class SearchImpl extends CustomAbstractPage implements Search  
{

	@Override
	public void searchProduct(String tcID, SoftAssert softAssert, String DeviceName, String productName) throws Exception 
	{
		_setValue(getElement(txt_searchBox), productName);
		
		func._click(getElement(btn_searchBox));
		
	}
	
	

}
