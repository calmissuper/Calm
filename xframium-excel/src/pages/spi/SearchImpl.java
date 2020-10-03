package pages.spi;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.asserts.SoftAssert;
import pages.Search;
import utility.CustomAbstractPage;

public class SearchImpl extends CustomAbstractPage implements Search  
{

	@Override
	public void searchProduct(String tcID, SoftAssert softAssert, String DeviceName, String productName) throws Exception 
	{
		_setValue(getElement(txt_searchBox), productName);

		_click(getElement(btn_searchBox));
		System.out.println("Test");
	}


	

}
