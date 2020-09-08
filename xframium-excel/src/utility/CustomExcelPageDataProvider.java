package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xframium.page.data.DefaultPageData;
import org.xframium.page.data.PageData;
import org.xframium.page.data.provider.AbstractPageDataProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class XMLPageDataProvider.
 */
public class CustomExcelPageDataProvider extends AbstractPageDataProvider
{
	
	/** The file name. */
	private File[] fileName;
	
	/** The resource name. */
	private String[] resourceName;
	
	/** The tab names. */
	private String tabNames;

	/**
	 * Instantiates a new XML page data provider.
	 *
	 * @param fileName the file name
	 * @param tabNames the tab names
	 */
	public CustomExcelPageDataProvider( File fileName, String tabNames )
	{
		this.tabNames = tabNames;
		this.fileName = new File[] { fileName };
	}
	
	public CustomExcelPageDataProvider( File[] fileName, String tabNames )
    {
        this.tabNames = tabNames;
        this.fileName = fileName;
    }

	/**
	 * Instantiates a new XML page data provider.
	 *
	 * @param resourceName the resource name
	 * @param tabNames the tab names
	 */
	public CustomExcelPageDataProvider( String resourceName, String tabNames )
	{
		this.tabNames = tabNames;
		this.resourceName = resourceName.split( "," );
	}

	/* (non-Javadoc)
	 * @see com.perfectoMobile.page.data.provider.AbstractPageDataProvider#readPageData()
	 */
	@Override
	public void readPageData()
	{
		if (fileName == null)
		{
			for ( String resource : resourceName )
			{
			    if (log.isInfoEnabled())
	                log.info( "Reading from CLASSPATH as " + resource );
			    readElements( getClass().getClassLoader().getResourceAsStream( resource ) );
			}
		}
		else
		{
			try
			{
				
				for ( File currentFile : fileName )
				{
				    if (log.isInfoEnabled())
	                    log.info( "Reading from FILE SYSTEM as [" + currentFile + "]" );
				    readElements( new FileInputStream( currentFile ) );
				}
			}
			catch (FileNotFoundException e)
			{
				log.fatal( "Could not read from " + fileName, e );
			}
		}
		
		populateTrees();
		
		

	}

	/**
	 * Gets the cell value.
	 *
	 * @param cell the cell
	 * @return the cell value
	 */
	private String getCellValue( XSSFCell cell )
	{
		if (cell != null)
		{
			switch (cell.getCellType())
			{
				case XSSFCell.CELL_TYPE_BLANK:
					return null;
				case XSSFCell.CELL_TYPE_BOOLEAN:
					return String.valueOf( cell.getBooleanCellValue() );
				case XSSFCell.CELL_TYPE_NUMERIC:
				{
				    String useValue = String.valueOf( cell.getNumericCellValue() );
				    if ( useValue.endsWith( ".0" ) )
				        return useValue.split( "\\." )[0];
				    else
				        return useValue;
				}
				case XSSFCell.CELL_TYPE_STRING:
					return cell.getRichStringCellValue().toString();
			}
		}
		return null;
	}
	
	
	/**
	 * Read elements.
	 *
	 * @param inputStream the input stream
	 */
	private void readElements( InputStream inputStream )
	{
		
		XSSFWorkbook workbook = null;

		try
		{
		    
		    
			workbook = new XSSFWorkbook( inputStream );
			String[] tabs= {""};
			
			if (tabNames.compareToIgnoreCase("all")==0) {
				int sheetCount=workbook.getNumberOfSheets();
		        String customTabNames="";
		        for(int i=0;i<sheetCount;i++) {
		  
		           XSSFSheet sheet=workbook.getSheetAt(i);
		           String sheetName=sheet.getSheetName().toString();
		           customTabNames=customTabNames+","+sheetName;
		        }
		        customTabNames=customTabNames.replaceFirst(",", "");
		        tabs=customTabNames.split(",");
			}
			else {
			 tabs = tabNames.split( "," );
			}
			
			for ( String tabName : tabs )
			{
			    if (log.isInfoEnabled())
                    log.info( "Reading Record Type [" + tabName + "]" );
			    
			    XSSFSheet sheet = workbook.getSheet( tabName );
			    
			    if ( sheet == null )
			        continue;
			    
				addRecordType( tabName, false );
				
				XSSFRow firstRow = sheet.getRow( 0 );
				
				for (int i = 1; i <= sheet.getLastRowNum(); i++)
				{
					XSSFRow currentRow = sheet.getRow( i );

					try
					{
    					DefaultPageData currentRecord = new DefaultPageData( tabName, tabName + "-" + i, true );
    					for ( int x=0; x<firstRow.getLastCellNum(); x++ )
    					{
    					    
    					    String currentName = getCellValue( firstRow.getCell( x ) );
    		                String currentValue = getCellValue( currentRow.getCell( x ) );
    		                
    		                if ( currentValue == null )
    		                    currentValue = "";
    		                
    		                if ( currentValue.startsWith( PageData.TREE_MARKER ) && currentValue.endsWith( PageData.TREE_MARKER ) )
    		                {
    		                    //
    		                    // This is a reference to another page data table
    		                    //
    		                    currentRecord.addPageData( currentName );
    		                    currentRecord.addValue( currentName + PageData.DEF, currentValue );
    		                    currentRecord.setContainsChildren( true );
    		                }
    		                else
    		                    currentRecord.addValue( currentName, currentValue );
    					}
    					
    					addRecord( currentRecord );
					}
					catch( Exception e )
					{
					    log.error( "Ignoring Row: " + e.getMessage() );
					}
				}
			}

			
		}
		catch (Exception e)
		{
			log.fatal( "Error reading Excel Element File", e );
		}
		finally
		{
			try
			{
				workbook.close();
			}
			catch (Exception e)
			{
			}
		}
	}
}

