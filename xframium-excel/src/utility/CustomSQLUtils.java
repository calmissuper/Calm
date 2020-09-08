package utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;




public class CustomSQLUtils {

//    private static final Map[] EMPTY_MAP_ARRAY = new Map[0];
    
    //
    // Implementation
    //
    /**
     * This method establishes the DB connection, executes the SQL statement and returns an array of Object
     * @param username
     * @param password
     * @param url
     * @param driver
     * @param query
     * @param params
     * @return object
     * @throws Exception
     */
	public static List<Object[][]> getResults(String username, String password, String url, String driver,String lob,String testCase) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String query = "Select * from tbl_TableInformation where BussinessArea='" +lob+"'";
		
		List<Object[][]> listOfLists = new ArrayList<Object[][]>();
		try {
			conn = getConnection(username, password, url, driver);
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			
					
			rs.next();

			do {
				//Prepare ArrayList of Test Case Namess of the table
				String tempQuery = "Select TestCase from " + rs.getString("TableName");
				PreparedStatement tempStmt = conn.prepareStatement(tempQuery);
							
				
				String tableName= rs.getString("TableName");
				String selectQuery = "Select * from " + tableName + " where TestCase='" + testCase +"'";
 						//+ "' order by asc";				
 				pstmt = conn.prepareStatement(selectQuery);
 				ResultSet rsInner = pstmt.executeQuery();
 				Object[][] dataSet = consume(rsInner,testCase,tableName);
 				listOfLists.add(dataSet);
 				
			
				
			} while (rs.next());

			return listOfLists;
		} finally {
			try {
				rs.close();
			} catch (Throwable e) {
			}

			try {
				pstmt.close();
			} catch (Throwable e) {
			}

			try {
				conn.close();
			} catch (Throwable e) {
			}
		}
	}

  

    /**
     * Gets the DB connection
     * @param username
     * @param password
     * @param url
     * @param driver
     * @return Connection
     * @throws Exception
     */
    private static Connection getConnection( String username,
                                             String password,
                                             String url,
                                             String driver )
        throws Exception
    {
        Connection conn = null;

        Class.forName( driver );

        conn = DriverManager.getConnection( url,
                                            username,
                                            password );
        
     //   TestMatrixGenarator.instance().genarateTestMatrixList(conn);

        return conn;
    }

    /**
     * This method converts the result set to a two dimensional array Object 
     * @param rs - ResultSet
     * @return Object[][]
     * @throws Exception
     */
    private static Object[][] consume( ResultSet rs,String testName, String tableName)
        throws Exception
    {
    	
        ResultSetMetaData rsmd = rs.getMetaData();
        LinkedList results = new LinkedList();
        int colCount = rsmd.getColumnCount();
        Object[] rowHeader = new Object[ colCount ];
        for( int iLoopCount = 1; iLoopCount <= colCount; ++iLoopCount )
        { 
            rowHeader[ iLoopCount - 1 ] = testName +"-" + tableName+"."+rsmd.getColumnName( iLoopCount );
        }
        results.add( rowHeader );
        while( rs.next() )
        {
            Object[] row = new Object[ colCount ];
       

            for( int iLoopCountInner = 1;iLoopCountInner <= colCount; ++iLoopCountInner )
            {
            	Object item= rs.getObject( iLoopCountInner );
            	String itemString;
            	if(item== null)
            			itemString="";
            		else
            			itemString= String.valueOf(item);
            	row[ iLoopCountInner - 1 ] = itemString;
                
            }
       
            results.add( row );
        }

        return toOutArray( results, colCount );
    }

   
    private static Object[][] toOutArray( LinkedList listOfArray, int colCount )
    {
        int length = listOfArray.size();
        Object[][] rtn = new Object[ length ][colCount ];

        for( int iLoopCount = 0; iLoopCount < rtn.length; ++iLoopCount )
        {
            rtn[iLoopCount] = (Object[]) listOfArray.get( iLoopCount );
        }

        return rtn;
    }
    
    
    private static ArrayList<String> getTestCaseList(ResultSet rempRS) throws Exception
    {
    	ArrayList<String> testCaseList = new ArrayList<>();
		while (rempRS.next())
		{
			Object item= rempRS.getObject( 1 );				
        	String testName;
        	if(item== null)
        			testName="";
        		else
        			testName= String.valueOf(item);
        	
        	 testCaseList.add(testName);
        	
		}
		
		return testCaseList;
    }

  
}