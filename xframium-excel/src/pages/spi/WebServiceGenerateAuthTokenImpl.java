package pages.spi;
import org.testng.asserts.SoftAssert;
import org.xframium.page.data.PageData;
import org.xframium.page.data.PageDataManager;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import pages.WebServiceGenerateAuthToken;
import utility.CustomAbstractPage;
public class WebServiceGenerateAuthTokenImpl extends CustomAbstractPage implements WebServiceGenerateAuthToken {
	PageData data;
	
	@Override
	public String generateAuthTokenCode(String tcID, SoftAssert softAssert) throws Exception {
        long startTime = System.currentTimeMillis();
        String accessToken = "";
        try {

              
              PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);
//              
//              String env= GenericFunctions.instance()._getGlobalVariableValue("PARAMETER_ENV");                  
//        if (!env.isEmpty()) {
//              if (env.equalsIgnoreCase("DEV")) {
//                 data = PageDataManager.instance().getPageData("GenerateAuthKey_DEV", tcID);
//              } else {
//                if(env.equalsIgnoreCase("QA")) {
//                       data = PageDataManager.instance().getPageData("GenerateAuthKey_QA", tcID);
//                }else {
//                      data = PageDataManager.instance().getPageData("GenerateAuthKey_STAGE", tcID); 
//                }
//              }
//        }
//        else {
//              if (EnvironmentData.getData("Environment").equals("DEV")) {
//                data = PageDataManager.instance().getPageData("GenerateAuthKey_DEV", tcID);
//              } else {
//                if(EnvironmentData.getData("Environment").equals("QA")) {
//                       data = PageDataManager.instance().getPageData("GenerateAuthKey_QA", tcID);
//                      }else {
//                           data = PageDataManager.instance().getPageData("GenerateAuthKey_STAGE", tcID);  
//                      }
//              }
//        }
        data = PageDataManager.instance().getPageData("GenerateAuthToken", tcID);
              String client_id = data.getData("client_id");
              String client_secret = data.getData("client_secret");
              String grant_type = data.getData("grant_type");
              String url=data.getData("EndPointUrl");
             // String username = data.getData("Username");
             // String password = data.getData("password");
              String contentType = data.getData("contentType");
              RestAssured.useRelaxedHTTPSValidation();
              
              RequestSpecification requestSpecification = RestAssured.given()
                          // .auth().basic(username, password)
                          .header("Content-Type", contentType).formParam("client_id", client_id)
                          .formParam("client_secret", client_secret).formParam("grant_type", grant_type);
                          
              Response response = requestSpecification.post(url);
              
              if (response != null) {
                    int code = response.statusCode();
                    String statusLine = response.statusLine();
                    String resbody = response.asString();
                    System.out.println(resbody);
              }

              accessToken = cFunc.getKeyValue(response, "access_token");
              System.out.println(accessToken);
            		  
            		 // cFunc.getKeyValue(response, "access_token");

        } catch (RuntimeException ex) {
              throw ex;
        }
        return accessToken;

  }


}
