package utility;

import java.util.HashMap;

public class GlobalVariableContainer {

public static GlobalVariableContainer singleton = new GlobalVariableContainer();
	
	
	//Declare Variable
	HashMap<String, String> GlobalVariableList = new HashMap<String, String>();
	
	//Single Ton Concept, Make constructor private
	private GlobalVariableContainer(){}
	
	//Implement Single ton 
	public static GlobalVariableContainer instance()
	{
		return singleton;
	}
	
	
	public void addVariable(String strKey, String strValue)
	{
		GlobalVariableList.put(strKey.toUpperCase(), strValue);
		
	}
	
	
	public String getVariable(String strKey)
	{
		if(GlobalVariableList.containsKey(strKey.toUpperCase()))
			return GlobalVariableList.get(strKey.toUpperCase());
		else
			return "";
	}
}
