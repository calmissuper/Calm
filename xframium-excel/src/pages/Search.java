package pages;

import org.testng.asserts.SoftAssert;
import org.xframium.page.Page;

public interface Search extends Page 
{
		
		@ElementDefinition
		public String txt_searchBox="txt_searchBox" ;
		@ElementDefinition
		public String btn_searchBox="btn_searchBox" ;
		@ElementDefinition
		public String label_sugesstions="label_sugesstions" ;
		
		
		
		@TimeMethod
		@ScreenShot
		public void searchProduct(String tcID, SoftAssert softAssert, String DeviceName, String productName) throws Exception;

}
