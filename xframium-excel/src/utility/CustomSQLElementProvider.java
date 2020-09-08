/*******************************************************************
	*	Name              :	SQLElementProvider
	*	Description       : Overloaded class, to configure the Object Repository 	
 	*	Modification Log  :                                                     
	*	Date		Initials     	Description of Modifications 
	********************************************************************
	********************************************************************/
package utility;

import java.util.HashMap;
import java.util.Map;

import org.xframium.page.BY;
import org.xframium.page.ElementDescriptor;
import org.xframium.page.element.Element;
import org.xframium.page.element.ElementFactory;
import org.xframium.page.element.provider.AbstractElementProvider;
import org.xframium.utility.SQLUtil;

/**
 * The Class SQLElementProvider.
 */
public class CustomSQLElementProvider  extends AbstractElementProvider
{
    //
    // class data
    //

    private static final String DEF_QUERY =
        "SELECT PS.NAME, \n" +
        "       PP.NAME, \n" +
        "       PE.NAME, PE.DESCRIPTOR, PE.VALUE, PE.CONTEXT_NAME \n" +
        "FROM SITES PS \n" +
        "     INNER JOIN PAGES PP ON PP.SITE_ID = PS.ID \n" +
        "     INNER JOIN ELEMENTS PE ON PE.PAGE_ID = PP.ID \n" +
        "ORDER BY PS.NAME, PP.NAME";

    //
    // instance data
    //

    /** The element map. */
    private Map<String,Element> elementMap = new HashMap<String,Element>(20);

    /** The username. */
    private String username;
	
    /** The password. */
    private String password;
	
    /** The JDBC URL. */
    private String url;
	
    /** The driver class name. */
    private String driver;
	
    /** The query. */
    private String query;

	
    /**
     * Instantiates a new SQL element provider.
     *
     * @param username the user name
     * @param password the password
     * @param url the JDBC URL
     * @param driver the driver class name
     */
    public CustomSQLElementProvider( String username, String password, String url, String driver )
    {
        this.username = username;
        this.password = password;
        this.url = url;
        this.driver = driver;
        this.query = DEF_QUERY;
        
        readElements();
    }
	
    /**
     * Instantiates a new SQL element provider.
     *
     * @param username the user name
     * @param password the password
     * @param url the JDBC URL
     * @param driver the driver class name
     */
    public CustomSQLElementProvider( String username, String password, String url, String driver, String query )
    {
       // this( username, password, url, driver );
    	this.username = username;
        this.password = password;
        this.url = url;
        this.driver = driver;
        this.query = (( query != null ) ? query : DEF_QUERY);
                
        readElements();
    }
	
    private String parseString( String currentValue, String defaultValue, boolean emptyCheck )
    {
        if ( currentValue == null )
            return defaultValue;
        else
        {
            if ( emptyCheck && currentValue.trim().isEmpty() )
                return defaultValue;
            else
                return currentValue;
        }
    }
    
    /**
     * Read elements.
     */
    private void readElements()
    {
        try
        {
            Object[][] data = SQLUtil.getResults( username, password, url, driver, query, null );
            boolean elementsRead = true;
            for( int i = 0; i < data.length; ++i )
            {
                String siteName = parseString( (String) data[i][0], null, true );
                String pageName = parseString( (String) data[i][1], null, true );
                String eltName = parseString( (String) data[i][2], null, true );
                String eltDesc = parseString( (String) data[i][3], null, true );
                String eltVal = parseString( (String) data[i][4], null, true );
                String contextName = parseString( (String) data[i][5], null, true );
                

                ElementDescriptor elementDescriptor = new ElementDescriptor( siteName,
                                                                             pageName,
                                                                             eltName );
                
                Element currentElement = ElementFactory.instance().createElement( BY.valueOf( eltDesc ),
                                                                                  eltVal.replace( "$$", ","),
                                                                                  eltName,
                                                                                  pageName,
                                                                                  contextName );
            
                if ( log.isInfoEnabled() )
                    log.info( "Adding SQL Element using [" + elementDescriptor.toString() + "] as [" + currentElement );
                
                elementsRead = elementsRead & validateElement( elementDescriptor, currentElement );
                elementMap.put(elementDescriptor.toString(), currentElement );
            }
            
            setInitialized( elementsRead );
        }
        catch (Exception e)
        {
            log.fatal( "Error reading Excel Element File", e );
        }

    }
		
    @Override
    protected Element _getElement( ElementDescriptor elementDescriptor )
    {
        return elementMap.get(  elementDescriptor.toString() );
    }

	
}
