/*******************************************************************
	*	Name              :	AlliancePage
	*	Description       : New class which extends LocalAbstractPage, This class should be extended by all the local pages  	
 	*	Modification Log  :                                                     
	*	Date		Initials     	Description of Modifications 
	********************************************************************
	********************************************************************/

package utility;

import java.util.Arrays;

import org.openqa.selenium.WebDriver;
import org.xframium.page.ElementDescriptor;
import org.xframium.page.LocalAbstractPage;
import org.xframium.page.PageManager;
import org.xframium.page.data.PageData;
import org.xframium.page.data.PageDataManager;
import org.xframium.page.element.Element;

import functions.CustomFunctions;
import functions.GenericFunctions;

public abstract class CustomAbstractPage extends LocalAbstractPage{

	//Object Declaration
	//public  static String className;
	//public  static String functionName;	
	//public  static String testName;	
	public static PageDataManager dataManager = PageDataManager.instance();
	public GenericFunctions func = GenericFunctions.instance();
	public CustomFunctions cFunc=CustomFunctions.instance();
	protected PageData pageData [];
		
	public CustomAbstractPage(){
        
        String strpageName=this.getClass().getSimpleName().split("Impl")[0];
        func._setGlobalToken("glbPageName", strpageName);
  }
	
/***********************************************************************/
	@Override
	public void initializePage() {
		// TODO Auto-generated method stub
		dataManager = PageDataManager.instance();
		//className= this.getClass().getSimpleName();
	}

	public Element getClonedElement(String Name)
	{
		return super.getElement(Name).cloneElement();
	}
	
	
	public Element getClonedElement( String pageName, String elementName )
    {
    	return getElement( pageName, elementName );
    }
/***********************************************************************/
 
	protected PageData[] getRecords(String tableName)
	{
		//PageData[] pageData =dataManager.getRecords(testName+"-"+tableName);
		PageData[] pageData =dataManager.getRecords(tableName);
		//return pageData;
		return removeDuplicates(pageData);
	}
	
	
/***********************************************************************/
	
/***********************************************************************/
	 
	protected PageData[] removeDuplicates(PageData[] pageData)
	{
				 
		 int noElements = pageData.length;
		
			
		for (int i= 0; i<noElements;i++)
		{
			for(int j=i+1; j<noElements;j++){
				if(pageData[i].getName()==pageData[j].getName()){
					pageData[j]=pageData[noElements-1];
					noElements--;
					j--;
				}
				
			}
			
		}		
		PageData[] pageDatarLocal= Arrays.copyOf(pageData,noElements);
		
		return (PageData[]) pageDatarLocal;
	}
	
	
/***********************************************************************/
 
	protected void _setValue(Element element, String string)
	{
		func._setValue(element, string);
	}
	

	
	
	
/**
 * @throws SecurityException 
 * @throws NoSuchFieldException *********************************************************************/
	 
	protected void _click(Element element) 
	{
		func._click(element);
	}
	
/***********************************************************************/
	 
	protected void _setGlobalToken(String strTokenName, String strTokenValue)
	{
		func._setGlobalToken(strTokenName,strTokenValue);
	}
	
	
/***********************************************************************/
	
	public WebDriver getWebDriver()
	{
		return func.getCustumWebDriver();

	}
	

	/*************************************************************************/
	/*protected void _switchWindow(WebDriver driver, String strswatchOption,String switchExpValue)
	{
		func._swatchWindows(driver,strswatchOption,switchExpValue);
	}*/
	/*************************************************************************/
	public void _uploadFiles(String Filepath)
	{
		func._uploadFile(Filepath);
	}
	
	/*************************************************************************/
	/*protected void _switchWindow(WebDriver driver, String strswatchOption,String switchExpValue)
	{
		func._swatchWindows(driver,strswatchOption,switchExpValue);
	}*/
	/*************************************************************************/
	public void _checkDefaultValue(Element element, String strAttribute, String strExpValue)
	{
		func._checkDefaultValue(element,strAttribute,strExpValue);
	}
	
	public boolean _isVisible(Element element)
	{	
		return func._isVisible(element);
	}
	
	public String _getAttributeValue(Element element, String strAttribute)
	{
		return func._getAttributeValue(element,strAttribute);
	}

	




	
	
	

}
