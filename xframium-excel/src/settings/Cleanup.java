package settings;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.xframium.artifact.ArtifactType;
import org.xframium.debugger.DebugManager;
import org.xframium.device.cloud.CloudRegistry;
import org.xframium.device.data.DataManager;
import org.xframium.device.ng.AbstractSeleniumTest;
import org.xframium.spi.RunDetails;
import utility.*;

public class Cleanup  extends AbstractSeleniumTest{
	
	
	public Cleanup() throws IOException{
 		
	/*	 try
        {
           
            if( DataManager.instance().isArtifactEnabled( ArtifactType.EXECUTION_RECORD_HTML ) )
            {
            	RunDetails.instance().writeHTMLIndex( DataManager.instance().getReportFolder(), true );
            	
                File htmlFile = RunDetails.instance().getIndex( DataManager.instance().getReportFolder() );
                try
                {
                    Desktop.getDesktop().browse( htmlFile.toURI() );
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
            }
            
            if( DataManager.instance().isArtifactEnabled( ArtifactType.DEBUGGER ) )
                DebugManager.instance().shutDown();

        }
        catch( Exception e )
        {
            log.fatal( "Error executing Tests", e );
        }
        finally
        {
            CloudRegistry.instance().shutdown();
        }
        */
		 String filePath =  Report.date_time;
		 File file = new File(System.getProperty("user.dir") + "//ExtReports//TestReport_" + filePath + ".html");
	        
	        //first check if Desktop is supported by Platform or not
	        if(!Desktop.isDesktopSupported()){
	            System.out.println("Desktop is not supported");
	            return;
	        }
	        
	        Desktop desktop = Desktop.getDesktop();
	        if(file.exists()) desktop.open(file);
	        
//	        //let's try to open PDF file
//	        file = new File(System.getProperty("user.dir") + "//ExtReports//TestReport_" + filePath + ".html");
//	        if(file.exists()) desktop.open(file);
	}

}
