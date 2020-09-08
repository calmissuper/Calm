package uiAutomation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.TestNG;

import functions.GenericFunctions;

public class RunTestNG {
                
                public static void main(String[] args) {
                	
                    System.out.println("DEBUG: arguments passed to the main - " + Arrays.toString(args));
                	
                	try{ 
                	 System.out.println("\n Argument value is: "+args[0]);                	
                	 GenericFunctions.instance()._addToGlobalVariableList("PARAMETER_ENV", args[0]);
                	 GenericFunctions.instance()._addToGlobalVariableList("PARAMETER_LOAD", args[1]);
                	 
                	 
                	}
                	catch(Exception e) {
                	 GenericFunctions.instance()._addToGlobalVariableList("PARAMETER_ENV", "");
                	 GenericFunctions.instance()._addToGlobalVariableList("PARAMETER_LOAD", "");
                	}
                             //   
                                // Create object of TestNG Class
                                TestNG runner=new TestNG();
                                
                                // Create a list of String 
                                List<String> suitefiles=new ArrayList<String>();
                                
                                File directory = new File("@/");
                                String strDirectoryPath =directory.getAbsolutePath().split("\\@") [0];
                                String[] arrProjectName= strDirectoryPath.split("\\\\");
                                String strProjectName=arrProjectName[arrProjectName.length-1];
                                String strSuitPath="..\\\\" + strProjectName + "\\\\Suite.xml";
                                // Add xml file which you have to execute
                                suitefiles.add(strSuitPath);
                                
                                // now set xml file for execution
                                runner.setTestSuites(suitefiles);
                                
                                // finally execute the runner using run method
                                runner.run();
                                }
                                

}
