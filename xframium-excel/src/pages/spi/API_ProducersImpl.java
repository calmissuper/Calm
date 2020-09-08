package pages.spi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.testng.asserts.SoftAssert;
import org.xframium.page.StepStatus;
import org.xframium.page.data.PageData;
import org.xframium.page.data.PageDataManager;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import functions.GenericFunctions;
import pages.API_Producers;
import utility.CustomAbstractPage;
import utility.CustomReporting;

public class API_ProducersImpl extends CustomAbstractPage implements API_Producers {
	PageData data;
	//API particular producers
	
	@SuppressWarnings("unused")
	@Override
	
	
	public void validateProducerCode(String tcID, SoftAssert softAs, String strAuthCode) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers_QA", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");
			System.out.println(url);
			String cType = data.getData("ContentType");
			String value = data.getData("queryValue");
			String authKey = "Bearer "+strAuthCode;
			System.out.println(url + qParameter + cType + value);
			// getting the list values from DataSheet and iterating
			List<String> result = Arrays.asList(value.split(","));
			Object[] objArray = result.toArray();

			for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray[index]);
				String qValue = (String) objArray[index];
				Response response = cFunc.webServiceCall("", authKey, cType, url, qParameter, qValue, "get");
				

				// to find the response
				if (response != null) {
					String resbody = response.asString();
					System.out.println(resbody);
					int code = response.statusCode();
					

					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport(
								"", "", "Retrieve Service returned response successfully with code: " + code
										+ " and message : " + statusLine,
								StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting.logReport("", "",
								"Unable to Retrieve Account Response.Response received is: " + resbody + code
										+ response.statusLine(),
								"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}
					JSONArray producerArr = new JSONArray(resbody);
					// Query to be sent to DB with Qvalue from DataSheet
					String query = "select  distinct c.EPICKey firm_id , a.FirmName name, a.ShortName , a.AbbreviatedName, a.FirmTaxId , a.FirmEffectiveDate Firm_begin_date , \r\n"
							+ "  	 a.IsActive 	 \r\n" + "	from ProducerMDMDB.dbo.PRDFirm a\r\n"
							+ "	left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
							+ "	left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
							+ "	left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n"
							+ "	left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
							+ "	left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
							+ "	where ph.HierarchyLevelDesc = 'Producer Location'and c.EPICKey=" + qValue;

					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB(query, 7);
 
					String strFirmId = "";
					String strWinsAgent = "";
					// System.out.println("\n" +"producerArr"+producerArr.length());

					// Iteration is done for the Response
					for (int i = 0; i < producerArr.length(); i++) {
						String prod = producerArr.get(i).toString();
						System.out.println("\n" + "prod" + prod);
						JsonPath path = new JsonPath(prod);
						String FirmId = path.get("firm_id").toString();
						System.out.println("FirmId" + FirmId);
						// String prodCodeArr = path.get("producer_codes").toString();
						int counter = 0;

						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strFirmId = (queryData.get(x))[0];
							//strWinsAgent = (queryData.get(x))[7];
							System.out.println(strFirmId + strWinsAgent);

							// To check for the firm_id
							int id = 0;
							if (path.get("firm_id").equals(strFirmId)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									// To check for the producer_codes
								/*	if (producerArr.getJSONObject(i).has("producer_codes")) {

										JSONArray producerCodeArr = producerArr.getJSONObject(i)
												.getJSONArray("producer_codes");
										for (int j = 0; j < producerCodeArr.length(); j++) {
											String prodCode = producerCodeArr.get(j).toString();
											JsonPath prodCodePath = new JsonPath(prodCode);
											int count = 0;
											if (prodCodePath.get("code").equals(strWinsAgent)) {
												count++;
												if (count != 0) {
													list.add(true);
													CustomReporting.logReport("", "",
															"code value from Response is equal to Database"
																	+ prodCodePath.get("code"),
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"code value from Response is not equal to Database"
																	+ prodCodePath.get("code"),
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}
											}

										}
									} else {
										if (strWinsAgent != "null") {
											CustomReporting.logReport("", "",
													"producer code is not available in response and in Database it is "
															+ strWinsAgent,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(true);
											CustomReporting.logReport("", "",
													"producer code is not available in response and in Database it is "
															+ strWinsAgent,
													StepStatus.SUCCESS, new String[] {}, startTime);
										}

									}*/

								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								} else {
									continue;
								}
							}

						}
						// To give the
						boolean stepResult = cFunc.allStepsResult(list);

						if (stepResult) {
							CustomReporting.logReport("", "",
									"Successfully Verified  Fields Values In Response With DB Columns ",
									StepStatus.SUCCESS, new String[] {}, startTime);
						} else {
							CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE,
									new String[] {}, startTime, null);
							throw new RuntimeException();
						}
					}
				}

				else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

			}
		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API all producers
	public void validateAllProducers(String tcID, SoftAssert softAs, String strAuthCode) throws Exception {

		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			// Get the data from data sheet depends on the Environment --> QA or DEV
			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers_QA", tcID);
				}
			}
			
			String authKey = "Bearer "+strAuthCode;
			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String value = data.getData("queryValue");
			// getting the list values from DataSheet and iterating
			List<String> result = Arrays.asList(value.split(","));
			Object[] objArray = result.toArray();

			for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray[index]);
				String qValue = (String) objArray[index];
				Response response = cFunc.webServiceCall("", authKey, cType, url, qParameter, qValue, "get");
				// String resbody = response.asString();
				// System.out.println(resbody);

				// to find the response
				if (response != null) {
					
				

			/*// Get the URL and content_Type from the data_sheet
			String url = data.getData("EndPointUrl");
			String cType = data.getData("ContentType");
			
			// Getting the Response form the API by web_service_call
			Response response = cFunc.webServiceCall("", "", cType, url, "", "", "get");
			Thread.sleep(30000);
			//System.out.println(response);
			// Checking the response whether its is null or not
*/			if (response != null) {
				String resbody = response.asString();
				//System.out.println(resbody);
				int code = response.statusCode();
				// converting the string to JSON array
				JSONArray producerArr = new JSONArray(resbody);
				//System.out.println(producerArr);
				String statusLine = response.statusLine();
				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				CustomReporting.logReport("====Get Producers Service====" + url);

				// comparing the status code
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be send to Data_Base
				String query = " select distinct c.EPICKey firm_id , a.FirmName name, a.ShortName , a.AbbreviatedName, a.FirmTaxId , a.FirmEffectiveDate Firm_begin_date ,\r\n"
						+ "	 	 a.IsActive , b.WinsAgent,a.FirmTerminationDate EndDate	  \r\n"
						+ "			from ProducerMDMDB.dbo.PRDFirm a \r\n"
						+ "			left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey \r\n"
						+ "			left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
						+ "			left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey \r\n"
						+ "			left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey  \r\n"
						+ "			left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey  \r\n"
						+ "			where ph.HierarchyLevelDesc = 'Producer Location' and c.EPICKey=" + qValue;
				// and c.EPICKey=" + qValue

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// Getting the data from the DB using queryData hash_mapping
				queryData = cFunc.getDataFromDB(query, 9);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strName = "";
				String strShortName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[0];
						strName = (queryData.get(x))[1];
						strShortName = (queryData.get(x))[2];
						strTaxId = (queryData.get(x))[4];
						strBeginDate = (queryData.get(x))[5];
						strActive = (queryData.get(x))[6];
						strWinsAgent = (queryData.get(x))[7];
						strEndDate = (queryData.get(x))[8];

						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								// comparing the name from response with DB
								if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (producerArr.getJSONObject(i).has("short_name")) {
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}}else {
									list.add(true);
									CustomReporting.logReport("", "",
											"No Short Name value from Response and NULL in db " ,
											StepStatus.SUCCESS, new String[] {}, startTime);
								}

								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}
								// comparing the producer_codes from response with DB
								if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									for (int j = 0; j < producerCodeArr.length(); j++) {
										String prodCode = producerCodeArr.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"code value from Response is equal to Database"
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"code value from Response is not equal to Database"
																+ prodCodePath.get("code"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										}

									}
								} else {
									if (strWinsAgent != "null") {
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}

							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

} }else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		}} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API all active producers
	public void validateAllActiveProducers(String tcID, SoftAssert softAs , String strAuthCode) throws Exception {

		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers_QA", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);
			String authKey = "Bearer "+strAuthCode;
			String url = data.getData("EndPointUrl");
			System.out.println(url);	
			String cType = data.getData("ContentType");

			Response response = cFunc.webServiceCall("", authKey, cType, url, "", "", "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();

				JSONArray producerArr = new JSONArray(resbody);

				String statusLine = response.statusLine();
				System.out.println("API Count"+response.getHeader("X-Total-Count"));
				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				// to check with Tashique
				/*
				 * String query =
				 * "select FirmLinkKey, FirmKey, LastModifiedTimestamp from dbo.PRDFirm__Link where EPICKey=10453"
				 * ; ResultSet res = cFunc.verifyInDatabase(query);
				 * //System.out.println(res.getFetchSize()); while(res.next()) { int firmKey =
				 * res.getInt(1); System.out.println(firmKey); }
				 */
				String query = "  select distinct c.EPICKey firm_id , a.FirmName name, a.ShortName , a.AbbreviatedName, a.FirmTaxId , a.FirmEffectiveDate Firm_begin_date ,\r\n"
						+ "	 	 a.IsActive , b.WinsAgent,a.FirmTerminationDate EndDate	  \r\n"
						+ "			from ProducerMDMDB.dbo.PRDFirm a \r\n"
						+ "			left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey \r\n"
						+ "			left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
						+ "			left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey \r\n"
						+ "			left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey  \r\n"
						+ "			left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey  \r\n"
						+ "			where ph.HierarchyLevelDesc = 'Producer Location' and a.IsActive=1 ";
				// and c.EPICKey=" + qValue and a.IsActive=1
				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				queryData = cFunc.getDataFromDB(query, 9);

				String strFirmId = "";
				String strName = "";
				String strShortName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				int ResponseActive;
				int DataBaseActive;
				int counter = 0;
				// System.out.println("\n" +"producerArr"+producerArr.length());
				boolean flag;
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					// System.out.println("\n" + "prod" + prod);
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();
					// System.out.println("FirmId" + FirmId);
					// String prodCodeArr = path.get("producer_codes").toString();
					// String end_date = path.get("end_date").toString();

					// System.out.println("end_date"+end_date);

					ResponseActive = path.getBoolean("active") ? 1 : 0;
					flag = false;
					if (ResponseActive != 0) {
						flag = true;
						list.add(true);
						CustomReporting.logReport("", "",
								"Active status in Response is " + ResponseActive + " for respective FirmId " + FirmId,
								StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting.logReport("", "", "Active status in Response is " + ResponseActive + " for respective FirmId " + FirmId+ ResponseActive
								, "",
								StepStatus.FAILURE, new String[] {}, startTime, null);
						continue;
					}

					if (flag != false) {

						for (int x = 0; x < queryData.size(); x++) {
							// System.out.println(x);
							strFirmId = (queryData.get(x))[0];
							strName = (queryData.get(x))[1];
							strShortName = (queryData.get(x))[2];
							strTaxId = (queryData.get(x))[4];
							strBeginDate = (queryData.get(x))[5];
							strActive = (queryData.get(x))[6];
							strWinsAgent = (queryData.get(x))[7];
							strEndDate = (queryData.get(x))[8];
							// System.out.println(strFirmId + strWinsAgent);
							
							// System.out.println(queryData.size());
							int id = 0;

							if (path.get("firm_id").equals(strFirmId)) {
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									if (path.get("name").equals(strName)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Name value from Response " + path.get("name")
														+ " is equal to data from db " + strName,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Name value from Response " + path.get("name")
														+ " is  not equal to data from db " + strName,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (path.get("short_name").equals(strShortName)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Short Name value from Response " + path.get("short_name")
														+ " is equal to data from db " + strShortName,
												StepStatus.SUCCESS, new String[] {}, startTime);

									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Short Name value from Response " + path.get("short_name")
														+ " is  not equal to data from db " + strShortName,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

									if (path.get("tax_id").equals(strTaxId)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"TaxId value from Response " + path.get("tax_id")
														+ " is equal to data from db " + strTaxId,
												StepStatus.SUCCESS, new String[] {}, startTime);

									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"TaxId value from Response " + path.get("tax_id")
														+ " is  not equal to data from db " + strTaxId,
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									// String BooleanActive = String.valueOf(path.getBoolean("active"));
									DataBaseActive = Integer.parseInt(strActive);
									if (ResponseActive == (DataBaseActive)) {

										list.add(true);
										CustomReporting.logReport("", "",
												"Active value from Response " + path.getBoolean("active")
														+ " is  equal to data from db ",
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Active value from Response " + path.getBoolean("active")
														+ " is  equal to data from db ",
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
									if (splitResponseBeginDate.equals(strBeginDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"BeginDate value from Response " + path.get("begin_date")
														+ " is  equal to data from db " + strBeginDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"BeginDate value from Response " + path.get("begin_date")
														+ " is  not equal to data from db " + strBeginDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

									if (producerArr.getJSONObject(i).has("end_date")) {
										splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
										if (splitResponseEndDate.equals(strEndDate)) {
											System.out.println(FirmId + "end date" + strEndDate);
											list.add(true);
											CustomReporting.logReport("", "",
													"End_date value from Response " + path.get("end_date")
															+ " is equal to data from db " + strEndDate,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"End_date value from Response" + " is  not equal to data from db "
															+ strEndDate,
													StepStatus.FAILURE, new String[] {}, startTime);

										}

									} else {
										if (strEndDate == null) {
											System.out.println(FirmId + " NO end_date");
											list.add(true);
											CustomReporting.logReport("", "",
													"End_date value is not available in the Response and in DB value it is "
															+ strEndDate,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"End_date value from Response" + " is  not equal to the database value  "
															+ strEndDate,
													StepStatus.FAILURE, new String[] {}, startTime);

										}

									}
									if (producerArr.getJSONObject(i).has("producer_codes")) {

										JSONArray producerCodeArr = producerArr.getJSONObject(i)
												.getJSONArray("producer_codes");
										for (int j = 0; j < producerCodeArr.length(); j++) {
											String prodCode = producerCodeArr.get(j).toString();
											JsonPath prodCodePath = new JsonPath(prodCode);
											int count = 0;
											if (prodCodePath.get("code").equals(strWinsAgent)) {
												count++;
												if (count != 0) {
													list.add(true);
													CustomReporting.logReport("", "",
															"producer code value from Response is equal to Database "
																	+ prodCodePath.get("code"),
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"producer code value from Response is not equal to Database "
																	+ prodCodePath.get("code"),
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}
											}

										}
									} else {
										if (strWinsAgent != "null") {
											CustomReporting.logReport("", "",
													"producer code is not available in response and in Database it is "
															+ strWinsAgent,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(true);
											CustomReporting.logReport("", "",
													"producer code is not available in response and in Database it is "
															+ strWinsAgent,
													StepStatus.SUCCESS, new String[] {}, startTime);
										}

									}

								} else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id   " + path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							}
							

						}
					}
				}
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();
				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API company Details
	public void validateCompanyDetails(String tcID, SoftAssert softAs) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String qValue = data.getData("queryValue");
			
			Response response = cFunc.webServiceCall("", "", cType, url, qParameter, qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			// to find the response
			if (response != null) {
				String resbody = response.asString();
				//System.out.println(resbody);
				int code = response.statusCode();
				

				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				JSONArray producerArr = new JSONArray(resbody);
				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select  c.EPICKey firm_id,comp.CompanyCd  ,comp.CompanyName, pc.ValidFromdt company_begin_date , pc.ValidTodt company_end_date, a.IsActive\r\n" + 
						"from ProducerMDMDB.dbo.PRDFirm a\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__Company pc on pc.FirmKey = a.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDCompany comp on pc.CompanyKey=comp.CompanyKey\r\n" + 
						"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
						"where ph.HierarchyLevelDesc = 'Producer Location'\r\n" + 
						"group by c.EPICKey ,  comp.CompanyCd  ,comp.CompanyName, pc.ValidFromdt  , pc.ValidTodt , a.IsActive";

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 6);

				String strFirmId = "";
				String strCompanyID = "";
				String strName = "";
				String strBeginDate = "";
				String strActive = "";
				String strEndDate = "";
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// Iteration is done for the Response
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					System.out.println("\n" + "prod" + prod);
					JsonPath path = new JsonPath(prod);
					String companies = path.get("companies").toString();
					// String description=path.get("description").toString();
					System.out.println("companies" + companies);
					// String prodCodeArr = path.get("producer_codes").toString();
					int counter = 0;

					// Iteration is done for queryData
					for (int x = 0; x < queryData.size(); x++) {
						System.out.println(x);
						strFirmId = (queryData.get(x))[0];
						strCompanyID = (queryData.get(x))[1];
						strName = (queryData.get(x))[2];
						strActive = (queryData.get(x))[5];
						strBeginDate = (queryData.get(x))[3];
						strEndDate = (queryData.get(x))[4];
						System.out.println(strFirmId + strCompanyID);

						// To check for the firm_id
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								int ResponseActive = path.getBoolean("active") ? 1 : 0;
								int DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is not  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// To check for the producer_codes
								if (producerArr.getJSONObject(i).has("companies")) {

									JSONArray producerCodeArr = producerArr.getJSONObject(i).getJSONArray("companies");
									for (int j = 0; j < producerCodeArr.length(); j++) {
										String prodCode = producerCodeArr.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strCompanyID)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"company code value from Response is equal the database value " + strCompanyID,
														StepStatus.SUCCESS, new String[] {}, startTime);

												if (prodCodePath.get("description").equals(strName)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"Name value from Response" + prodCodePath.get("description")
																	+ " is equal to the database value " + strName,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Name value from Response" + prodCodePath.get("description")
																	+ " is  not equal to the database value " + strName,
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}

												String splitResponseBeginDate = ((String) prodCodePath
														.get("begin_date")).split("T")[0];
												String splitstrBeginDate = ((String)(strBeginDate))
														.split(" ")[0];
											
												if (splitResponseBeginDate.equals(splitstrBeginDate)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"BeginDate value from Response"
																	+ prodCodePath.get("begin_date")
																	+ " is  equal to the database value " + splitstrBeginDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "", "BeginDate value from Response"
															+ prodCodePath.get("begin_date")
															+ " is  not equal to the database value " + splitstrBeginDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												} 
												String splitstrEndDate = ((String)(strEndDate))
														.split(" ")[0];
													String splitResponseEndDate = ((String) prodCodePath.get("end_date")).split("T")[0];
													if (splitResponseEndDate.equals(splitstrEndDate)) {
														
														list.add(true);
														CustomReporting.logReport("", "",
																"End_date value from Response" + prodCodePath.get("end_date")
																		+ " is equal to the database value " + strEndDate,
																StepStatus.SUCCESS, new String[] {}, startTime);
													} else {
														list.add(false);
														CustomReporting.logReport("", "",
																"End_date value from Response" + " is  not equal to the database value "
																		+ strEndDate,
																StepStatus.FAILURE, new String[] {}, startTime);

													}
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"company code value from Response is not equal to Database value "
																+ prodCodePath.get("companies"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										}

									}
								} else {
									if (strCompanyID != "null") {
										CustomReporting.logReport("", "",
												"company code is not available in response and in Database it is "
														+ strCompanyID,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"company code is not available in response and in Database it is "
														+ strCompanyID,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}

							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to  Firm Id in Database " + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Firm Id in Database " + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							} else {
								continue;
							}
						}
					
					}
				}
					// To give the
					boolean stepResult = cFunc.allStepsResult(list);

					if (stepResult) {
						CustomReporting.logReport("", "",
								"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
								new String[] {}, startTime);
					} else {
						CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE,
								new String[] {}, startTime, null);
						throw new RuntimeException();
					}
					
			}

			else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}
		
		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API all BusinessAddress
	public void validateBusinessAddress(String tcID, SoftAssert softAs) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String qValue = data.getData("queryValue");

			Response response = cFunc.webServiceCall("", "", cType, url, qParameter, qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			// to find the response
			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);

				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = " select  distinct c.EPICKey firm_id,adt.AddressTypeDesc , ad.AddressLine1 , ad.AddressLine2 , ad.AddressLine3 , \r\n"
						+ " null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\"\r\n"
						+ "from ProducerMDMDB.dbo.PRDFirm a\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
						+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
						+ "	where ph.HierarchyLevelDesc = 'Producer Location' and adt.AddressTypeDesc = 'Business Location'";

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 10);

				String strFirmId = "";
				String strAddressType = "";
				String strAddress_1 = "";
				String strAddress_2 = "";
				String strAddress_3 = "";
				String strAddress_4 = "";
				String strLocality = "";
				String strPostalCode = "";
				String strRegion = "";
				String strCountry = "";
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// Iteration is done for the Response
				for (int i = 0; i < producerArr.length(); i++) {

					String prod = producerArr.get(i).toString();
					System.out.println("\n" + "prod" + prod);
					JsonPath path = new JsonPath(prod);

					int counter = 0;

					// Iteration is done for queryData
					for (int x = 0; x < queryData.size(); x++) {
						System.out.println(x);
						strFirmId = (queryData.get(x))[0];
						strAddressType = (queryData.get(x))[1];
						strAddress_1 = (queryData.get(x))[2];
						strAddress_2 = (queryData.get(x))[3];
						strAddress_3 = (queryData.get(x))[4];
						strAddress_4 = (queryData.get(x))[5];
						strLocality = (queryData.get(x))[6];
						strPostalCode = (queryData.get(x))[7];
						strRegion = (queryData.get(x))[8];
						strCountry = (queryData.get(x))[9];

						// System.out.println(strFirmId + strCompanyID);

						// To check for the firm_id
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								// To check for the business_address
								if (producerArr.getJSONObject(i).has("business_address")) {

									String country = path.get("business_address.country");

									if (path.get("business_address.address_line_1").equals(strAddress_1)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_1 from Response"
														+ path.get("business_address.address_line_1")
														+ " is equal to the database value " + strAddress_1,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_1 from Response"
														+ path.get("business_address.address_line_1")
														+ " is  not equal to the database value " + strAddress_1,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}

									if (path.get("business_address.address_line_2").equals(strAddress_2)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_2 from Response"
														+ path.get("business_address.address_line_2")
														+ " is equal to the database value " + strAddress_2,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_2 from Response"
														+ path.get("business_address.address_line_2")
														+ " is  not equal to the database value " + strAddress_2,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (path.get("business_address.address_line_3").equals(strAddress_3)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_3 from Response"
														+ path.get("business_address.address_line_3")
														+ " is equal to the database value " + strAddress_3,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_3 from Response"
														+ path.get("business_address.address_line_3")
														+ " is  not equal to the database value " + strAddress_3,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (!producerArr.getJSONObject(i).has("business_address.address_line_4")) {
										if (strAddress_4 == null) {
											list.add(true);
											CustomReporting.logReport("", "",
													"address_line_4 from Response is null and is equal to the database value ",
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_3 from Response is  not null and is not  equal to the database value ",
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_3 from Response is  not null and is not  equal to the database value ",
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (path.get("business_address.locality").equals(strLocality)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("business_address.locality")
														+ " is equal to the database value " + strLocality,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("business_address.locality")
														+ " is  not equal to the database value " + strLocality,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									if (path.get("business_address.region").equals(strRegion)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"region from Response" + path.get("business_address.region")
														+ " is equal to the database value " + strRegion,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"region from Response" + path.get("business_address.region")
														+ " is  not equal to the database value " + strRegion,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									if (path.get("business_address.postal_code").equals(strPostalCode)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("business_address.postal_code")
														+ " is equal to the database value " + strPostalCode,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("business_address.postal_code")
														+ " is  not equal to the database value " + strPostalCode,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									if (path.get("business_address.country").equals(strCountry)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"country from Response" + path.get("business_address.country")
														+ " is equal to the database value " + strCountry,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"country from Response" + path.get("business_address.country")
														+ " is  not equal to the database value " + strCountry,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}

								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to  Firm Id in Database "
													+ path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Firm Id in Database "
													+ path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								} else {
									continue;
								}
							}

						}
					}
				}
				// To give the
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();
				}

			}

			else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API all MailingAddress
	public void validateMailingAddress(String tcID, SoftAssert softAs) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) { 
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String qValue = data.getData("queryValue");

			Response response = cFunc.webServiceCall("", "", cType, url, qParameter, qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			// to find the response
			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);

				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = " select  distinct c.EPICKey firm_id,adt.AddressTypeDesc , ad.AddressLine1 , ad.AddressLine2 , ad.AddressLine3 , \r\n"
						+ " null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\"\r\n"
						+ "from ProducerMDMDB.dbo.PRDFirm a\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
						+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
						+ "	where ph.HierarchyLevelDesc = 'Producer Location' and adt.AddressTypeDesc = 'Mailing'";

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 10);

				String strFirmId = "";
				String strAddressType = "";
				String strAddress_1 = "";
				String strAddress_2 = "";
				String strAddress_3 = "";
				String strAddress_4 = "";
				String strLocality = "";
				String strPostalCode = "";
				String strRegion = "";
				String strCountry = "";
				// System.out.println("\n" +"producerArr"+producerArr.length());
				
				// Iteration is done for the Response
				for (int i = 0; i < producerArr.length(); i++) {
				
					String prod = producerArr.get(i).toString();
					System.out.println("\n" + "prod" + prod);
					JsonPath path = new JsonPath(prod);
					
					
					
				
					int counter = 0;

					// Iteration is done for queryData
					for (int x = 0; x < queryData.size(); x++) {
						System.out.println(x);
						strFirmId = (queryData.get(x))[0];
						strAddressType = (queryData.get(x))[1];
						strAddress_1 = (queryData.get(x))[2];
						strAddress_2 = (queryData.get(x))[3];
						strAddress_3 = (queryData.get(x))[4];
						strAddress_4 = (queryData.get(x))[5];
						strLocality = (queryData.get(x))[6];
						strPostalCode = (queryData.get(x))[7];
						strRegion = (queryData.get(x))[8];
						strCountry = (queryData.get(x))[9];

						// System.out.println(strFirmId + strCompanyID);

						// To check for the firm_id
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								// To check for the producer_codes
								if (producerArr.getJSONObject(i).has("mailing_address")) {

									String country = path.get("mailing_address.country");
									
									if (path.get("mailing_address.address_line_1").equals(strAddress_1)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_1 from Response" + path.get("mailing_address.address_line_1")
														+ " is equal to the database value " + strAddress_1,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_1 from Response" + path.get("mailing_address.address_line_1")
														+ " is  not equal to the database value " + strAddress_1,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}

									if (path.get("mailing_address.address_line_2").equals(strAddress_2)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_2 from Response" + path.get("mailing_address.address_line_2")
														+ " is equal to the database value " + strAddress_2,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_2 from Response" + path.get("mailing_address.address_line_2")
														+ " is  not equal to the database value " + strAddress_2,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (path.get("mailing_address.address_line_3").equals(strAddress_3)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_3 from Response" + path.get("mailing_address.address_line_3")
														+ " is equal to the database value " + strAddress_3,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_3 from Response" + path.get("mailing_address.address_line_3")
														+ " is  not equal to the database value " + strAddress_3,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (!producerArr.getJSONObject(i).has("mailing_address.address_line_4")) {
										if(strAddress_4==null)
										{
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_4 from Response is null and is equal to the database value " ,
												StepStatus.SUCCESS, new String[] {}, startTime);}
										else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_3 from Response is  not null and is not  equal to the database value " ,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_3 from Response is  not null and is not  equal to the database value " ,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (path.get("mailing_address.locality").equals(strLocality)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("mailing_address.locality")
														+ " is equal to the database value " + strLocality,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("mailing_address.locality")
														+ " is  not equal to the database value " + strLocality,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									

								}
									if (path.get("mailing_address.region").equals(strRegion)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"region from Response" + path.get("mailing_address.region")
														+ " is equal to the database value " + strRegion,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"region from Response" + path.get("mailing_address.region")
														+ " is  not equal to the database value " + strRegion,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									

								}
									if (path.get("mailing_address.postal_code").equals(strPostalCode)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("mailing_address.postal_code")
														+ " is equal to the database value " + strPostalCode,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("mailing_address.postal_code")
														+ " is  not equal to the database value " + strPostalCode,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									

								}
									if (path.get("mailing_address.country").equals(strCountry)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"country from Response" + path.get("mailing_address.country")
														+ " is equal to the database value " + strCountry,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"country from Response" + path.get("mailing_address.country")
														+ " is  not equal to the database value " + strCountry,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									

								}

							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to  Firm Id in Database "
												+ path.get("firm_id"),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Firm Id in Database "
												+ path.get("firm_id"),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
							} else {
								continue;
							}
						}

					}
				}}
				// To give the
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();
				}

				}

			else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API all DistributionChannel
	public void validateDistributionChannel(String tcID, SoftAssert softAs) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) { 
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String qValue = data.getData("queryValue");

			Response response = cFunc.webServiceCall("", "", cType, url, qParameter, qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			// to find the response
			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);

				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select distinct c.EPICKey firm_id, pd.DistributionChannelCD , pd.DistributionChannelDESC , a.ValidFromdt DistributionChannel_begin_date  , a.ValidTodt\r\n" + 
						"from   ProducerMDMDB.dbo.PRDFirm a\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n" + 
						"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n" + 
						"where ph.HierarchyLevelDesc = 'Producer Location'";

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 5);

				String strFirmId = "";
				String strCode = "";
				String strDescription = "";
				String strEndDate = "";
				String strBeginDate = "";
				
				// System.out.println("\n" +"producerArr"+producerArr.length());
				
				// Iteration is done for the Response
				for (int i = 0; i < producerArr.length(); i++) {
				
					String prod = producerArr.get(i).toString();
					System.out.println("\n" + "prod" + prod);
					JsonPath path = new JsonPath(prod);
					
					
					
				
					int counter = 0;

					// Iteration is done for queryData
					for (int x = 0; x < queryData.size(); x++) {
						System.out.println(x);
						strFirmId = (queryData.get(x))[0];
						strCode = (queryData.get(x))[1];
						strDescription = (queryData.get(x))[2];
						strBeginDate = (queryData.get(x))[3];
						strEndDate = (queryData.get(x))[4];
						

						// System.out.println(strFirmId + strCompanyID);

						// To check for the firm_id
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								// To check for the producer_codes
								if (producerArr.getJSONObject(i).has("distribution_channel")) {

									//String country = path.get("mailing_address.country");
									if (path.get("distribution_channel.code").equals(strCode)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"code value from Response"
														+ path.get("distribution_channel.code")
														+ " is  equal to the database value " + strCode,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "", "code value from Response"
												+ path.get("distribution_channel.code")
												+ " is  not equal to the database value " + strCode,
												StepStatus.FAILURE, new String[] {}, startTime);
									}
								
									if (path.get("distribution_channel.description").equals(strDescription)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"description value from Response"
														+ path.get("distribution_channel.description")
														+ " is  equal to the database value " + strDescription,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "", "description value from Response"
												+ path.get("distribution_channel.description")
												+ " is  not equal to the database value " + strDescription,
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									String splitResponseBeginDate = ((String) path
											.get("distribution_channel.begin_date")).split("T")[0];
									String splitstrBeginDate = ((String)(strBeginDate))
											.split(" ")[0];
								
									if (splitResponseBeginDate.equals(splitstrBeginDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"BeginDate value from Response"
														+ path.get("distribution_channel.begin_date")
														+ " is  equal to the database value " + strBeginDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "", "BeginDate value from Response"
												+ path.get("distribution_channel.begin_date")
												+ " is  not equal to the database value " + strBeginDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									} 
									String splitstrEndDate = ((String)(strEndDate))
											.split(" ")[0];
										String splitResponseEndDate = ((String) path.get("distribution_channel.end_date")).split("T")[0];
										if (splitResponseEndDate.equals(splitstrEndDate)) {
											
											list.add(true);
											CustomReporting.logReport("", "",
													"End_date value from Response" + path.get("distribution_channel.end_date")
															+ " is equal to the database value " + strEndDate,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"End_date value from Response" + path.get("distribution_channel.end_date")
													+ " is  not equal to the database value "
															+ strEndDate,
													StepStatus.FAILURE, new String[] {}, startTime);

										}
									
								}
								else {
									if(!producerArr.getJSONObject(i).has("distribution_channel.code") || !producerArr.getJSONObject(i).has("distribution_channel.description") )
									{if(strCode==null || strDescription==null) {
										list.add(true);
										CustomReporting.logReport("", "",
												"code and description value from Response is  equal to the database value null",
												StepStatus.SUCCESS, new String[] {}, startTime);
										continue;
									}
									}else {
										list.add(false);
									CustomReporting.logReport("", "",
											"Destribution_channel from Response is not found",
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
									continue;
								}
								}
									

							
								
							}
							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to  Firm Id in Database "
												+ path.get("firm_id"),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Firm Id in Database "
												+ path.get("firm_id"),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
							} else {
								continue;
							}
						}

					
				}}
				// To give the
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();
				}

				}

			else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}
	}
 	//API LastModified Date
	public void validateLastModifiedDate(String tcID, SoftAssert softAs) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String value = data.getData("queryValue");
			// getting the list values from DataSheet and iterating
		
			  List<String> result = Arrays.asList(value.split(",")); 
			  Object[] objArray = result.toArray();
			  
			  for (int index = 0; index < objArray.length; index++) {
			  System.out.println(objArray[index]);
			  String qValue = (String)objArray[index];
			 

			Response response = cFunc.webServiceCall("", "", cType, url, qParameter, qValue, "get");

			// String resbody = response.asString();
			// System.out.println(resbody);

			// to find the response
			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println("\n" + "producerArr" + producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select distinct c.EPICKey firm_id , a.FirmName name, a.ShortName , a.AbbreviatedName,\r\n" + 
						" a.FirmTaxId , a.FirmEffectiveDate Firm_begin_date ,  a.IsActive ,    b.WinsAgent ,a.FirmTerminationDate EndDate	,\r\n" + 
						" b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" , b.IsActive producer_code_IsActive , \r\n" + 
						" d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" ,\r\n" + 
						" b.ValidTodt \"businesssegment_endtdate\", a.LastModifiedTimestamp\r\n" + 
						"  from ProducerMDMDB.dbo.PRDFirm a\r\n" + 
						" left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey \r\n" + 
						"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey  \r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey \r\n" + 
						"where ph.HierarchyLevelDesc = 'Producer Location' and a.LastModifiedTimestamp>="+qValue+" order by c.EPICKey  ";

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 17);
				String BusinessSegmentCode = "";
				String BusinessSegmentDesc = "";
				String businessSegment_startdate = "";
				String businesssegment_endtdate = "";
				String strFirmId = "";
				String strName = "";
				String strShortName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				String producer_code_startdate = "";
				String producer_code_endtdate = "";
				String producer_code_IsActive = "";
				int ResponseActive;
				int DataBaseActive;
				int counter = 0;
				int prod_ResponseActive = 0;
				int prod_DataBaseActive = 0;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					 //System.out.println("\n" + "prod" + prod);
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();
					System.out.println("FirmId" + FirmId);
					// String prodCodeArr = path.get("producer_codes").toString();
					// String end_date = path.get("end_date").toString();

					// System.out.println("end_date"+end_date);

					for (int x = 0; x < queryData.size(); x++) {
						// System.out.println(x);
						strFirmId = (queryData.get(x))[0];
						strName = (queryData.get(x))[1];
						strShortName = (queryData.get(x))[2];
						strTaxId = (queryData.get(x))[4];
						strBeginDate = (queryData.get(x))[5];
						strActive = (queryData.get(x))[6];
						strWinsAgent = (queryData.get(x))[7];
						strEndDate = (queryData.get(x))[8];
						producer_code_startdate = (queryData.get(x))[9];
						producer_code_endtdate = (queryData.get(x))[10];
						producer_code_IsActive = (queryData.get(x))[11];
						BusinessSegmentCode = (queryData.get(x))[12];
						BusinessSegmentDesc = (queryData.get(x))[13];
						businessSegment_startdate = (queryData.get(x))[14];
						businesssegment_endtdate = (queryData.get(x))[15];
						// System.out.println(strFirmId + strWinsAgent);
						System.out.println(strActive);
						// System.out.println(queryData.size());
						int id = 0;

						if (path.get("firm_id").equals(strFirmId)) {
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}

								if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									/*
									 * JSONObject obj = new JSONObject(clientstring); JSONArray params =
									 * obj.getJsonArray("params"); JSONObject param1 = params.getJsonObject(0);
									 */
									for (int j = 0; j < producerCodeArr.length(); j++) {
										System.out.println("length" + producerCodeArr.length());
										String prodCode = producerCodeArr.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										System.out.println("prodCodePath" + prodCodePath.get("code"));
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {

												list.add(true);
												CustomReporting.logReport("", "",
														"producer code value from Response is equal to Database "
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);

												prod_ResponseActive = prodCodePath.getBoolean("active") ? 1 : 0;
												prod_DataBaseActive = Integer.parseInt(producer_code_IsActive);
												if (prod_ResponseActive == (prod_DataBaseActive)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"Active value from Response of producer_code"
																	+ prodCodePath.getBoolean("active")
																	+ " is  equal to data from db ",
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Active value from Response of  producer_code"
																	+ prodCodePath.getBoolean("active")
																	+ " is  equal to data from db ",
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												String splitProd_ResponseBeginDate = ((String) prodCodePath
														.get("begin_date")).split("T")[0];
												String compare_Prod_ResponseBeginDate = ((String) producer_code_startdate)
														.split(" ")[0];
												if (splitProd_ResponseBeginDate
														.equals(compare_Prod_ResponseBeginDate)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"BeginDate value from Response of  producer_code"
																	+ prodCodePath.get("begin_date")
																	+ " is  equal to data from db "
																	+ producer_code_startdate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"BeginDate value from Response of  producer_code"
																	+ prodCodePath.get("begin_date")
																	+ " is  not equal to data from db "
																	+ producer_code_startdate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

												String splitProd_ResponseEndDate = ((String) prodCodePath
														.get("end_date")).split("T")[0];
												String compare_Prod_ResponseEndDate = ((String) producer_code_endtdate)
														.split(" ")[0];
												if (splitProd_ResponseEndDate.equals(compare_Prod_ResponseEndDate)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"EndDate value from Response of  producer_code"
																	+ prodCodePath.get("end_date")
																	+ " is  equal to data from db "
																	+ producer_code_endtdate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"EndDate value from Response of  producer_code"
																	+ prodCodePath.get("end_date")
																	+ " is  not equal to data from db "
																	+ producer_code_endtdate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

												if (producerCodeArr.getJSONObject(j)
														.has("business_segment_restrictions")) {
													JSONArray businessCodeArr = producerCodeArr.getJSONObject(j)
															.getJSONArray("business_segment_restrictions");
													for (int k = 0; k < businessCodeArr.length(); k++) {
														System.out.println("length" + businessCodeArr.length());

														String BusinessCode = businessCodeArr.get(k).toString();

														JsonPath BusinessCodePath = new JsonPath(BusinessCode);
														System.out.println("code" + BusinessCodePath.get("code"));
														int count1 = 0;
														if (BusinessCodePath.get("code").equals(BusinessSegmentCode)) {
															count1++;
															if (count1 != 0) {

																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segment code value from Response is equal to Database "
																				+ BusinessCodePath.get("code"),
																		StepStatus.SUCCESS, new String[] {}, startTime);

																String splitBusiness_ResponseBeginDate = ((String) BusinessCodePath
																		.get("begin_date")).split("T")[0];
																String compare_Business_ResponseBeginDate = ((String) businessSegment_startdate)
																		.split(" ")[0];
																if (splitBusiness_ResponseBeginDate
																		.equals(compare_Business_ResponseBeginDate)) {
																	list.add(true);
																	CustomReporting.logReport("", "",
																			"BeginDate value from Response of  Business_segment"
																					+ BusinessCodePath.get("begin_date")
																					+ " is  equal to data from db "
																					+ businessSegment_startdate,
																			StepStatus.SUCCESS, new String[] {},
																			startTime);
																} else {
																	list.add(false);
																	CustomReporting.logReport("", "",
																			"BeginDate value from Response of  Business_segment"
																					+ BusinessCodePath.get("begin_date")
																					+ " is  not equal to data from db "
																					+ businessSegment_startdate,
																			StepStatus.FAILURE, new String[] {},
																			startTime);
																}

																String splitBusiness_ResponseEndDate = ((String) BusinessCodePath
																		.get("end_date")).split("T")[0];
																String compare_Business_ResponseEndDate = ((String) businesssegment_endtdate)
																		.split(" ")[0];
																if (splitBusiness_ResponseEndDate
																		.equals(compare_Business_ResponseEndDate)) {
																	list.add(true);
																	CustomReporting.logReport("", "",
																			"EndDate value from Response of  producer_code"
																					+ BusinessCodePath.get("end_date")
																					+ " is  equal to data from db "
																					+ businesssegment_endtdate,
																			StepStatus.SUCCESS, new String[] {},
																			startTime);
																} else {
																	list.add(false);
																	CustomReporting.logReport("", "",
																			"EndDate value from Response of  producer_code"
																					+ BusinessCodePath.get("end_date")
																					+ " is  not equal to data from db "
																					+ businesssegment_endtdate,
																			StepStatus.FAILURE, new String[] {},
																			startTime);
																}

																if (BusinessSegmentDesc
																		.equals(BusinessCodePath.get("description"))) {
																	list.add(true);
																	CustomReporting.logReport("", "",
																			"Description value from Response of  Business_segment"
																					+ BusinessCodePath
																							.get("description")
																					+ " is  equal to data from db "
																					+ BusinessSegmentDesc,
																			StepStatus.SUCCESS, new String[] {},
																			startTime);
																} else {
																	list.add(false);
																	CustomReporting.logReport("", "",
																			"Description value from Response of  Business_segment"
																					+ BusinessCodePath
																							.get("description")
																					+ " is  not equal to data from db "
																					+ BusinessSegmentDesc,
																			StepStatus.FAILURE, new String[] {},
																			startTime);
																}

															}
														}

													}

												} else {

												}

											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"producer code value from Response is not equal to Database "
																+ prodCodePath.get("code"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										} /*
											 * else { list.add(false); CustomReporting.logReport("", "",
											 * "producer code value from Response is not equal to Database " +
											 * prodCodePath.get("code"), "", StepStatus.FAILURE, new String[] {},
											 * startTime, null); continue }
											 */

									}
								} else {
									System.out.println("no producer code");
								}

							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						/*} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("fir  m_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						*/}
					}

				}
				// To give the
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();
				}

			}

			else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}
		}
		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API all BusinessAddress
	public void validateAllAddressChannelModifiedDate(String tcID, SoftAssert softAs) throws Exception {
			long startTime = System.currentTimeMillis();
			try {
				PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

				String env = System.getenv("PARAMETER_ENV");
				if (env != null) {
					if (env.equalsIgnoreCase("DEV")) {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					} else {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					}
				} else {
					if (EnvironmentData.getData("Environment").equals("DEV")) {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					} else {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					}
				}
				// data = PageDataManager.instance().getPageData("GetProducers", tcID);

				String url = data.getData("EndPointUrl");
				String qParameter = data.getData("queryParameter");
				String qParameter1 = data.getData("queryParameter1");
				String cType = data.getData("ContentType");

				String qValue = data.getData("queryValue");
				String qValue1 = data.getData("queryValue1");

				Response response = cFunc.webServiceCall_1("", "", cType, url, qParameter,qParameter1, qValue,qValue1, "get");
				// String resbody = response.asString();
				// System.out.println(resbody);
				
				if (response != null) {
					String resbody = response.asString();
					System.out.println(resbody);
					int code = response.statusCode();
					JSONArray producerArr = new JSONArray(resbody);
System.out.println(producerArr);
					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
								+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting
								.logReport("", "",
										"Unable to Retrieve Account Response.Response received is: " + resbody + code
												+ response.statusLine(),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}

					// Query to be sent to DB with Qvalue from DataSheet
					String query = " select  distinct c.EPICKey firm_id,adt.AddressTypeDesc , ad.AddressLine1 , ad.AddressLine2 , ad.AddressLine3 , \r\n"
							+ " null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\"\r\n"
							+ "from ProducerMDMDB.dbo.PRDFirm a\r\n"
							+ "left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
							+ "left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
							+ "left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n"
							+ "left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n"
							+ "left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n"
							+ "left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n"
							+ "left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n"
							+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
							+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
							+ "	where ph.HierarchyLevelDesc = 'Producer Location' and adt.AddressTypeDesc = 'Business Location'";

					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB(query, 10);

					// System.out.println("\n" +"producerArr"+producerArr.length());

					// Iteration is done for the Response
					for (int i = 0; i < producerArr.length(); i++) {

						String prod = producerArr.get(i).toString();
						System.out.println("\n" + "prod" + prod);
						JsonPath path = new JsonPath(prod);

						int counter = 0;
						
						String strFirmId = "";
						String strAddressType = "";
						String strAddress_1 = "";
						String strAddress_2 = "";
						String strAddress_3 = "";
						String strAddress_4 = "";
						String strLocality = "";
						String strPostalCode = "";
						String strRegion = "";
						String strCountry = "";
						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strFirmId = (queryData.get(x))[0];
							strAddressType = (queryData.get(x))[1];
							strAddress_1 = (queryData.get(x))[2];
							strAddress_2 = (queryData.get(x))[3];
							strAddress_3 = (queryData.get(x))[4];
							strAddress_4 = (queryData.get(x))[5];
							strLocality = (queryData.get(x))[6];
							strPostalCode = (queryData.get(x))[7];
							strRegion = (queryData.get(x))[8];
							strCountry = (queryData.get(x))[9];

							// System.out.println(strFirmId + strCompanyID);

							// To check for the firm_id
							int id = 0;
							if (path.get("firm_id").equals(strFirmId)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									// To check for the producer_codes
									if (producerArr.getJSONObject(i).has("business_address")) {

										String country = path.get("business_address.country");

										if (path.get("business_address.address_line_1").equals(strAddress_1)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"address_line_1 from Response"
															+ path.get("business_address.address_line_1")
															+ " is equal to the database value " + strAddress_1,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_1 from Response"
															+ path.get("business_address.address_line_1")
															+ " is  not equal to the database value " + strAddress_1,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}

										if (path.get("business_address.address_line_2").equals(strAddress_2)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"address_line_2 from Response"
															+ path.get("business_address.address_line_2")
															+ " is equal to the database value " + strAddress_2,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_2 from Response"
															+ path.get("business_address.address_line_2")
															+ " is  not equal to the database value " + strAddress_2,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
										if (path.get("business_address.address_line_3").equals(strAddress_3)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"address_line_3 from Response"
															+ path.get("business_address.address_line_3")
															+ " is equal to the database value " + strAddress_3,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_3 from Response"
															+ path.get("business_address.address_line_3")
															+ " is  not equal to the database value " + strAddress_3,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
										if (!producerArr.getJSONObject(i).has("business_address.address_line_4")) {
											if (strAddress_4 == null) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_4 from Response is null and is equal to the database value ",
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"address_line_3 from Response is  not null and is not  equal to the database value ",
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_3 from Response is  not null and is not  equal to the database value ",
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
										if (path.get("business_address.locality").equals(strLocality)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"locality from Response" + path.get("business_address.locality")
															+ " is equal to the database value " + strLocality,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"locality from Response" + path.get("business_address.locality")
															+ " is  not equal to the database value " + strLocality,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);

										}
										if (path.get("business_address.region").equals(strRegion)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"region from Response" + path.get("business_address.region")
															+ " is equal to the database value " + strRegion,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"region from Response" + path.get("business_address.region")
															+ " is  not equal to the database value " + strRegion,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);

										}
										if (path.get("business_address.postal_code").equals(strPostalCode)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"postal_code from Response" + path.get("business_address.postal_code")
															+ " is equal to the database value " + strPostalCode,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"postal_code from Response" + path.get("business_address.postal_code")
															+ " is  not equal to the database value " + strPostalCode,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);

										}
										if (path.get("business_address.country").equals(strCountry)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"country from Response" + path.get("business_address.country")
															+ " is equal to the database value " + strCountry,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"country from Response" + path.get("business_address.country")
															+ " is  not equal to the database value " + strCountry,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);

										}

									}

									else {

										list.add(false);
										CustomReporting.logReport("", "",
												"firm_id from Response is not equal to  Firm Id in Database "
														+ path.get("firm_id"),
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
								} else {
									counter++;
									if (counter >= queryData.size()) {
										list.add(false);
										CustomReporting.logReport("", "",
												"firm_id from Response is not equal to Firm Id in Database "
														+ path.get("firm_id"),
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									} else {
										continue;
									}
								}

							}
						}}
					
					// To give the
					
				

				/*else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}*/
				/*if (response != null) {
					String resbody = response.asString();
					System.out.println(resbody);
					int code = response.statusCode();
					JSONArray producerArr = new JSONArray(resbody);
					System.out.println("\n" + "producerArr" + producerArr);
					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
								+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting
								.logReport("", "",
										"Unable to Retrieve Account Response.Response received is: " + resbody + code
												+ response.statusLine(),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}
*/
					String query2 = " select  distinct c.EPICKey firm_id,adt.AddressTypeDesc , ad.AddressLine1 , ad.AddressLine2 , ad.AddressLine3 , \r\n"
							+ " null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\"\r\n"
							+ "from ProducerMDMDB.dbo.PRDFirm a\r\n"
							+ "left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
							+ "left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
							+ "left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n"
							+ "left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n"
							+ "left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n"
							+ "left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n"
							+ "left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n"
							+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
							+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
							+ "	where ph.HierarchyLevelDesc = 'Producer Location' and adt.AddressTypeDesc = 'Mailing'";

					LinkedHashMap<Integer, String[]> queryData2 = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData2 = cFunc.getDataFromDB(query2, 10);

					
					// System.out.println("\n" +"producerArr"+producerArr.length());
					
					// Iteration is done for the Response
					for (int i = 0; i < producerArr.length(); i++) {
					
						String prod = producerArr.get(i).toString();
						System.out.println("\n" + "prod" + prod);
						JsonPath path = new JsonPath(prod);
						
						String strFirmId = "";
						String strAddressType = "";
						String strAddress_1 = "";
						String strAddress_2 = "";
						String strAddress_3 = "";
						String strAddress_4 = "";
						String strLocality = "";
						String strPostalCode = "";
						String strRegion = "";
						String strCountry = "";
						
						
					
						int counter = 0;

						// Iteration is done for queryData
						for (int x = 0; x < queryData2.size(); x++) {
							System.out.println(x);
							strFirmId = (queryData2.get(x))[0];
							strAddressType = (queryData2.get(x))[1];
							strAddress_1 = (queryData2.get(x))[2];
							strAddress_2 = (queryData2.get(x))[3];
							strAddress_3 = (queryData2.get(x))[4];
							strAddress_4 = (queryData2.get(x))[5];
							strLocality = (queryData2.get(x))[6];
							strPostalCode = (queryData2.get(x))[7];
							strRegion = (queryData2.get(x))[8];
							strCountry = (queryData2.get(x))[9];

							// System.out.println(strFirmId + strCompanyID);

							// To check for the firm_id
							int id = 0;
							if (path.get("firm_id").equals(strFirmId)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									// To check for the producer_codes
									if (producerArr.getJSONObject(i).has("mailing_address")) {

										String country = path.get("mailing_address.country");
										
										if (path.get("mailing_address.address_line_1").equals(strAddress_1)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"address_line_1 from Response" + path.get("mailing_address.address_line_1")
															+ " is equal to the database value " + strAddress_1,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_1 from Response" + path.get("mailing_address.address_line_1")
															+ " is  not equal to the database value " + strAddress_1,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}

										if (path.get("mailing_address.address_line_2").equals(strAddress_2)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"address_line_2 from Response" + path.get("mailing_address.address_line_2")
															+ " is equal to the database value " + strAddress_2,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_2 from Response" + path.get("mailing_address.address_line_2")
															+ " is  not equal to the database value " + strAddress_2,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
										if (path.get("mailing_address.address_line_3").equals(strAddress_3)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"address_line_3 from Response" + path.get("mailing_address.address_line_3")
															+ " is equal to the database value " + strAddress_3,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_3 from Response" + path.get("mailing_address.address_line_3")
															+ " is  not equal to the database value " + strAddress_3,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
										if (!producerArr.getJSONObject(i).has("mailing_address.address_line_4")) {
											if(strAddress_4==null)
											{
											list.add(true);
											CustomReporting.logReport("", "",
													"address_line_4 from Response is null and is equal to the database value " ,
													StepStatus.SUCCESS, new String[] {}, startTime);}
											else {
												list.add(false);
												CustomReporting.logReport("", "",
														"address_line_3 from Response is  not null and is not  equal to the database value " ,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_3 from Response is  not null and is not  equal to the database value " ,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
										if (path.get("mailing_address.locality").equals(strLocality)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"locality from Response" + path.get("mailing_address.locality")
															+ " is equal to the database value " + strLocality,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"locality from Response" + path.get("mailing_address.locality")
															+ " is  not equal to the database value " + strLocality,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										

									}
										if (path.get("mailing_address.region").equals(strRegion)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"region from Response" + path.get("mailing_address.region")
															+ " is equal to the database value " + strRegion,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"region from Response" + path.get("mailing_address.region")
															+ " is  not equal to the database value " + strRegion,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										

									}
										if (path.get("mailing_address.postal_code").equals(strPostalCode)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"postal_code from Response" + path.get("mailing_address.postal_code")
															+ " is equal to the database value " + strPostalCode,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"postal_code from Response" + path.get("mailing_address.postal_code")
															+ " is  not equal to the database value " + strPostalCode,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										

									}
										if (path.get("mailing_address.country").equals(strCountry)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"country from Response" + path.get("mailing_address.country")
															+ " is equal to the database value " + strCountry,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"country from Response" + path.get("mailing_address.country")
															+ " is  not equal to the database value " + strCountry,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										

									}

								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to  Firm Id in Database "
													+ path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Firm Id in Database "
													+ path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								} else {
									continue;
								}
							}

						}
					}}
					
				
					
					// Query to be sent to DB with Qvalue from DataSheet
						
					String query1 = "select distinct c.EPICKey firm_id , a.FirmName name, a.ShortName , a.AbbreviatedName,\r\n" + 
							" a.FirmTaxId , a.FirmEffectiveDate Firm_begin_date ,  a.IsActive ,    b.WinsAgent ,a.FirmTerminationDate EndDate	,\r\n" + 
							" b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" , b.IsActive producer_code_IsActive , \r\n" + 
							" d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" ,\r\n" + 
							" b.ValidTodt \"businesssegment_endtdate\", a.LastModifiedTimestamp\r\n" + 
							"  from ProducerMDMDB.dbo.PRDFirm a\r\n" + 
							" left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey \r\n" + 
							"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey  \r\n" + 
							"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey \r\n" + 
							"where ph.HierarchyLevelDesc = 'Producer Location' and a.LastModifiedTimestamp>="+qValue1+" order by c.EPICKey  ";

					LinkedHashMap<Integer, String[]> queryData1 = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData1 = cFunc.getDataFromDB(query1, 17);
					System.out.println("queryData1.size("+queryData1.size());
					// System.out.println("\n" +"producerArr"+producerArr.length());
					for (int i = 0; i < producerArr.length(); i++) {

						String prod = producerArr.get(i).toString();
						System.out.println("\n" + "prod" + prod);
						JsonPath path = new JsonPath(prod);
				
						
						for (int x1 = 0; x1 < queryData1.size(); x1++) {
							// System.out.println(x);

							String BusinessSegmentCode = "";
							String BusinessSegmentDesc = "";
							String businessSegment_startdate = "";
							String businesssegment_endtdate = "";
							String strFirmId = "";
							String strName = "";
							String strShortName = "";
							String strActive = "";
							String strBeginDate = "";
							String strTaxId = "";
							String strWinsAgent = "";
							String strEndDate = "";
							String splitResponseEndDate = "";
							String producer_code_startdate = "";
							String producer_code_endtdate = "";
							String producer_code_IsActive = "";
							int ResponseActive;
							int DataBaseActive;
							//int counter = 0;
							int prod_ResponseActive = 0;
							int prod_DataBaseActive = 0;
							strFirmId = (queryData1.get(x1))[0];
							strName = (queryData1.get(x1))[1];
							strShortName = (queryData1.get(x1))[2];
							strTaxId = (queryData1.get(x1))[4];
							strBeginDate = (queryData1.get(x1))[5];
							strActive = (queryData1.get(x1))[6];
							strWinsAgent = (queryData1.get(x1))[7];
							strEndDate = (queryData1.get(x1))[8];
							producer_code_startdate = (queryData1.get(x1))[9];
							producer_code_endtdate = (queryData1.get(x1))[10];
							producer_code_IsActive = (queryData1.get(x1))[11];
							BusinessSegmentCode = (queryData1.get(x1))[12];
							BusinessSegmentDesc = (queryData1.get(x1))[13];
							businessSegment_startdate = (queryData1.get(x1))[14];
							businesssegment_endtdate = (queryData1.get(x1))[15];
							// System.out.println(strFirmId + strWinsAgent);
							System.out.println(strActive);
							// System.out.println(queryData.size());
							int id = 0;

							if (path.get("firm_id").equals(strFirmId)) {
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									if (path.get("name").equals(strName)) {
										list.add(true);
										CustomReporting.logReport(
												"", "", "Name value from Response" + path.get("name")
														+ " is equal to data from db " + strName,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Name value from Response" + path.get("name")
														+ " is  not equal to data from db " + strName,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									// comparing the short_name from response with DB
									if (path.get("short_name").equals(strShortName)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Short Name value from Response" + path.get("short_name")
														+ " is equal to data from db " + strShortName,
												StepStatus.SUCCESS, new String[] {}, startTime);

									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Short Name value from Response" + path.get("short_name")
														+ " is  not equal to data from db " + strShortName,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

									// comparing the tax_id from response with DB
									if (path.get("tax_id").equals(strTaxId)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"TaxId value from Response" + path.get("tax_id")
														+ " is equal to data from db " + strTaxId,
												StepStatus.SUCCESS, new String[] {}, startTime);

									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"TaxId value from Response" + path.get("tax_id")
														+ " is  not equal to data from db " + strTaxId,
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									// String BooleanActive = String.valueOf(path.getBoolean("active"));

									// comparing the Active status from response with DB
									ResponseActive = path.getBoolean("active") ? 1 : 0;
									DataBaseActive = Integer.parseInt(strActive);
									if (ResponseActive == (DataBaseActive)) {

										list.add(true);
										CustomReporting.logReport("", "",
												"Active value from Response" + path.getBoolean("active")
														+ " is  equal to data from db ",
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Active value from Response" + path.getBoolean("active")
														+ " is  equal to data from db ",
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									// comparing the begin_date from response with DB
									String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
									if (splitResponseBeginDate.equals(strBeginDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"BeginDate value from Response" + path.get("begin_date")
														+ " is  equal to data from db " + strBeginDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"BeginDate value from Response" + path.get("begin_date")
														+ " is  not equal to data from db " + strBeginDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									// comparing the end_date from response with DB
									if (!producerArr.getJSONObject(i).has("end_date")) {
										if (strEndDate == null) {
											System.out.println(path.get("firm_id") + " NO end_date");
											list.add(true);
											CustomReporting.logReport("", "",
													"End_date value is not available in the Response and in DB value is "
															+ strEndDate,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport(
													"", "", "End_date value from Response"
															+ " is  not equal to data from db " + strEndDate,
													StepStatus.FAILURE, new String[] {}, startTime);

										}
									} else {
										splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
										if (splitResponseEndDate.equals(strEndDate)) {
											System.out.println(path.get("firm_id") + "end date" + strEndDate);
											list.add(true);
											CustomReporting.logReport("", "",
													"End_date value from Response" + path.get("end_date")
															+ " is equal to data from db " + strEndDate,
													StepStatus.SUCCESS, new String[] {}, startTime);
										}
									}

									if (producerArr.getJSONObject(i).has("producer_codes")) {

										JSONArray producerCodeArr = producerArr.getJSONObject(i)
												.getJSONArray("producer_codes");
										/*
										 * JSONObject obj = new JSONObject(clientstring); JSONArray params =
										 * obj.getJsonArray("params"); JSONObject param1 = params.getJsonObject(0);
										 */
										for (int j = 0; j < producerCodeArr.length(); j++) {
											System.out.println("length" + producerCodeArr.length());
											String prodCode = producerCodeArr.get(j).toString();
											JsonPath prodCodePath = new JsonPath(prodCode);
											System.out.println("prodCodePath" + prodCodePath.get("code"));
											int count = 0;
											if (prodCodePath.get("code").equals(strWinsAgent)) {
												count++;
												if (count != 0) {

													list.add(true);
													CustomReporting.logReport("", "",
															"producer code value from Response is equal to Database "
																	+ prodCodePath.get("code"),
															StepStatus.SUCCESS, new String[] {}, startTime);

													prod_ResponseActive = prodCodePath.getBoolean("active") ? 1 : 0;
													prod_DataBaseActive = Integer.parseInt(producer_code_IsActive);
													if (prod_ResponseActive == (prod_DataBaseActive)) {

														list.add(true);
														CustomReporting.logReport("", "",
																"Active value from Response of producer_code"
																		+ prodCodePath.getBoolean("active")
																		+ " is  equal to data from db ",
																StepStatus.SUCCESS, new String[] {}, startTime);
													} else {
														list.add(false);
														CustomReporting.logReport("", "",
																"Active value from Response of  producer_code"
																		+ prodCodePath.getBoolean("active")
																		+ " is  equal to data from db ",
																StepStatus.FAILURE, new String[] {}, startTime);
													}
													String splitProd_ResponseBeginDate = ((String) prodCodePath
															.get("begin_date")).split("T")[0];
													String compare_Prod_ResponseBeginDate = ((String) producer_code_startdate)
															.split(" ")[0];
													if (splitProd_ResponseBeginDate
															.equals(compare_Prod_ResponseBeginDate)) {
														list.add(true);
														CustomReporting.logReport("", "",
																"BeginDate value from Response of  producer_code"
																		+ prodCodePath.get("begin_date")
																		+ " is  equal to data from db "
																		+ producer_code_startdate,
																StepStatus.SUCCESS, new String[] {}, startTime);
													} else {
														list.add(false);
														CustomReporting.logReport("", "",
																"BeginDate value from Response of  producer_code"
																		+ prodCodePath.get("begin_date")
																		+ " is  not equal to data from db "
																		+ producer_code_startdate,
																StepStatus.FAILURE, new String[] {}, startTime);
													}

													String splitProd_ResponseEndDate = ((String) prodCodePath
															.get("end_date")).split("T")[0];
													String compare_Prod_ResponseEndDate = ((String) producer_code_endtdate)
															.split(" ")[0];
													if (splitProd_ResponseEndDate.equals(compare_Prod_ResponseEndDate)) {
														list.add(true);
														CustomReporting.logReport("", "",
																"EndDate value from Response of  producer_code"
																		+ prodCodePath.get("end_date")
																		+ " is  equal to data from db "
																		+ producer_code_endtdate,
																StepStatus.SUCCESS, new String[] {}, startTime);
													} else {
														list.add(false);
														CustomReporting.logReport("", "",
																"EndDate value from Response of  producer_code"
																		+ prodCodePath.get("end_date")
																		+ " is  not equal to data from db "
																		+ producer_code_endtdate,
																StepStatus.FAILURE, new String[] {}, startTime);
													}

													if (producerCodeArr.getJSONObject(j)
															.has("business_segment_restrictions")) {
														JSONArray businessCodeArr = producerCodeArr.getJSONObject(j)
																.getJSONArray("business_segment_restrictions");
														for (int k = 0; k < businessCodeArr.length(); k++) {
															System.out.println("length" + businessCodeArr.length());

															String BusinessCode = businessCodeArr.get(k).toString();

															JsonPath BusinessCodePath = new JsonPath(BusinessCode);
															System.out.println("code" + BusinessCodePath.get("code"));
															int count1 = 0;
															if (BusinessCodePath.get("code").equals(BusinessSegmentCode)) {
																count1++;
																if (count1 != 0) {

																	list.add(true);
																	CustomReporting.logReport("", "",
																			"Business_segment code value from Response is equal to Database "
																					+ BusinessCodePath.get("code"),
																			StepStatus.SUCCESS, new String[] {}, startTime);

																	String splitBusiness_ResponseBeginDate = ((String) BusinessCodePath
																			.get("begin_date")).split("T")[0];
																	String compare_Business_ResponseBeginDate = ((String) businessSegment_startdate)
																			.split(" ")[0];
																	if (splitBusiness_ResponseBeginDate
																			.equals(compare_Business_ResponseBeginDate)) {
																		list.add(true);
																		CustomReporting.logReport("", "",
																				"BeginDate value from Response of  Business_segment"
																						+ BusinessCodePath.get("begin_date")
																						+ " is  equal to data from db "
																						+ businessSegment_startdate,
																				StepStatus.SUCCESS, new String[] {},
																				startTime);
																	} else {
																		list.add(false);
																		CustomReporting.logReport("", "",
																				"BeginDate value from Response of  Business_segment"
																						+ BusinessCodePath.get("begin_date")
																						+ " is  not equal to data from db "
																						+ businessSegment_startdate,
																				StepStatus.FAILURE, new String[] {},
																				startTime);
																	}

																	String splitBusiness_ResponseEndDate = ((String) BusinessCodePath
																			.get("end_date")).split("T")[0];
																	String compare_Business_ResponseEndDate = ((String) businesssegment_endtdate)
																			.split(" ")[0];
																	if (splitBusiness_ResponseEndDate
																			.equals(compare_Business_ResponseEndDate)) {
																		list.add(true);
																		CustomReporting.logReport("", "",
																				"EndDate value from Response of  producer_code"
																						+ BusinessCodePath.get("end_date")
																						+ " is  equal to data from db "
																						+ businesssegment_endtdate,
																				StepStatus.SUCCESS, new String[] {},
																				startTime);
																	} else {
																		list.add(false);
																		CustomReporting.logReport("", "",
																				"EndDate value from Response of  producer_code"
																						+ BusinessCodePath.get("end_date")
																						+ " is  not equal to data from db "
																						+ businesssegment_endtdate,
																				StepStatus.FAILURE, new String[] {},
																				startTime);
																	}

																	if (BusinessSegmentDesc
																			.equals(BusinessCodePath.get("description"))) {
																		list.add(true);
																		CustomReporting.logReport("", "",
																				"Description value from Response of  Business_segment"
																						+ BusinessCodePath
																								.get("description")
																						+ " is  equal to data from db "
																						+ BusinessSegmentDesc,
																				StepStatus.SUCCESS, new String[] {},
																				startTime);
																	} else {
																		list.add(false);
																		CustomReporting.logReport("", "",
																				"Description value from Response of  Business_segment"
																						+ BusinessCodePath
																								.get("description")
																						+ " is  not equal to data from db "
																						+ BusinessSegmentDesc,
																				StepStatus.FAILURE, new String[] {},
																				startTime);
																	}

																}
															}

														}

													} else {

													}

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"producer code value from Response is not equal to Database "
																	+ prodCodePath.get("code"),
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}
											} /*
												 * else { list.add(false); CustomReporting.logReport("", "",
												 * "producer code value from Response is not equal to Database " +
												 * prodCodePath.get("code"), "", StepStatus.FAILURE, new String[] {},
												 * startTime, null); continue }
												 */

										}
									} else {
										System.out.println("no producer code");
									}

								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
											StepStatus.FAILURE, new String[] {}, startTime, null);
								}

								// break;
							/*} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("fir  m_id"), "",
											StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							*/}
						}

					}
					
					// Query to be sent to DB with Qvalue from DataSheet
					String query3 = "select distinct c.EPICKey firm_id, pd.DistributionChannelCD , pd.DistributionChannelDESC , a.ValidFromdt DistributionChannel_begin_date  , a.ValidTodt\r\n" + 
							"from   ProducerMDMDB.dbo.PRDFirm a\r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
							"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n" + 
							"where ph.HierarchyLevelDesc = 'Producer Location'\r\n" + 
							"group by  c.EPICKey , pd.DistributionChannelCD , pd.DistributionChannelDESC , a.ValidFromdt   , a.ValidTodt";

					LinkedHashMap<Integer, String[]> queryData3 = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData3 = cFunc.getDataFromDB(query3, 5);

				
					
					// System.out.println("\n" +"producerArr"+producerArr.length());
					
					// Iteration is done for the Response
					for (int i = 0; i < producerArr.length(); i++) {
					
						String prod = producerArr.get(i).toString();
						System.out.println("\n" + "prod" + prod);
						JsonPath path = new JsonPath(prod);
						String strFirmId = "";
						String strCode = "";
						String strDescription = "";
						String strEndDate = "";
						String strBeginDate = "";
						
						System.out.println("queryData.size()"+queryData.size());
					
						int counter = 0;

						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strFirmId = (queryData3.get(x))[0];
							strCode = (queryData3.get(x))[1];
							strDescription = (queryData3.get(x))[2];
							strBeginDate = (queryData3.get(x))[3];
							strEndDate = (queryData3.get(x))[4];
							

							// System.out.println(strFirmId + strCompanyID);

							// To check for the firm_id
							int id = 0;
							if (path.get("firm_id").equals(strFirmId)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to  Firm Id in Database " + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									// To check for the producer_codes
									if (producerArr.getJSONObject(i).has("distribution_channel")) {

										//String country = path.get("mailing_address.country");
										if (path.get("distribution_channel.code").equals(strCode)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"code value from Response"
															+ path.get("distribution_channel.code")
															+ " is  equal to the database value " + strCode,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "", "code value from Response"
													+ path.get("distribution_channel.code")
													+ " is  not equal to the database value " + strCode,
													StepStatus.FAILURE, new String[] {}, startTime);
										}
									
										if (path.get("distribution_channel.description").equals(strDescription)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"description value from Response"
															+ path.get("distribution_channel.description")
															+ " is  equal to the database value " + strDescription,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "", "description value from Response"
													+ path.get("distribution_channel.description")
													+ " is  not equal to the database value " + strDescription,
													StepStatus.FAILURE, new String[] {}, startTime);
										}
										
									}
									else {
										if(!producerArr.getJSONObject(i).has("distribution_channel.code") || !producerArr.getJSONObject(i).has("distribution_channel.description") )
										{if(strCode==null || strDescription==null) {
											list.add(true);
											CustomReporting.logReport("", "",
													"code and description value from Response is  equal to the database value null",
													StepStatus.SUCCESS, new String[] {}, startTime);
											continue;
										}
										}else {
											list.add(false);
										CustomReporting.logReport("", "",
												"Destribution_channel from Response is not found",
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
										continue;
									}
									}
										String splitResponseBeginDate = ((String) path
												.get("distribution_channel.begin_date")).split("T")[0];
										String splitstrBeginDate = ((String)(strBeginDate))
												.split(" ")[0];
									
										if (splitResponseBeginDate.equals(splitstrBeginDate)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"BeginDate value from Response"
															+ path.get("distribution_channel.begin_date")
															+ " is  equal to the database value " + strBeginDate,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "", "BeginDate value from Response"
													+ path.get("distribution_channel.begin_date")
													+ " is  not equal to the database value " + strBeginDate,
													StepStatus.FAILURE, new String[] {}, startTime);
										} 
										String splitstrEndDate = ((String)(strEndDate))
												.split(" ")[0];
											String splitResponseEndDate = ((String) path.get("distribution_channel.end_date")).split("T")[0];
											if (splitResponseEndDate.equals(splitstrEndDate)) {
												
												list.add(true);
												CustomReporting.logReport("", "",
														"End_date value from Response" + path.get("distribution_channel.end_date")
																+ " is equal to the database value " + strEndDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"End_date value from Response" + path.get("distribution_channel.end_date")
														+ " is  not equal to the database value "
																+ strEndDate,
														StepStatus.FAILURE, new String[] {}, startTime);

											}

								
									
								}
								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to  Firm Id in Database "
													+ path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Firm Id in Database "
													+ path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								} else {
									continue;
								}
							}

						
					}}
					// To give the
					boolean stepResult = cFunc.allStepsResult(list);

					if (stepResult) {
						CustomReporting.logReport("", "",
								"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
								new String[] {}, startTime);
					} else {
						CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
								startTime, null);
						throw new RuntimeException();
					}

				}

				else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				
			} catch (RuntimeException ex) {
				throw ex;
			}
		}
	//API all MRDH_validateDistributionChannel
	public void MRDH_validateDistributionChannel(String tcID, SoftAssert softAs) throws Exception  {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String value = data.getData("queryValue");
			// getting the list values from DataSheet and iterating
			List<String> result = Arrays.asList(value.split(","));
			Object[] objArray = result.toArray();

			for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray[index]);
				String qValue = (String) objArray[index];
				Response response = cFunc.webServiceCall("", "", cType, url, qValue, "get");
				// String resbody = response.asString();
				// System.out.println(resbody);

				// to find the response
				if (response != null) {
					String resbody = response.asString();
					JsonPath prodCodePath = new JsonPath(resbody);
					System.out.println(resbody.length());
					int code = response.statusCode();
					//JSONArray producerArr = new JSONArray(resbody);

					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport(
								"", "", "Retrieve Service returned response successfully with code: " + code
										+ " and message : " + statusLine,
								StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting.logReport("", "",
								"Unable to Retrieve Account Response.Response received is: " + resbody + code
										+ response.statusLine(),
								"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}

					// Query to be sent to DB with Qvalue from DataSheet
					String query = "select distinct Distribution_Channel_Cd,Distribution_Channel_Desc,Valid_From,Valid_To from MasterReferenceDataHub.[dbo].[Distribution_Channel] \r\n" + 
							"where Distribution_Channel_Cd=" +"'"+ qValue+"'";

					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB1(query, 4);

					String strCode = "";
					String strdescription = "";
					String strBeginDate = "";
					String strEndDate = "";
					// System.out.println("\n" +"producerArr"+producerArr.length());

					// Iteration is done for the Response
					
						// String prodCodeArr = path.get("producer_codes").toString();
						int counter = 0;

						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strCode = (queryData.get(x))[0];
							strdescription = (queryData.get(x))[1];
							strBeginDate = (queryData.get(x))[2];
							strEndDate = (queryData.get(x))[3];
							
							
						
							// To check for the firm_id
							int id = 0;
							if (prodCodePath.get("Distribution_Channel_Cd").equals(strCode)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"code value from Response is equal to Database"
													+ prodCodePath.get("Distribution_Channel_Cd"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									if (prodCodePath.get("Distribution_Channel_Desc").equals(strdescription)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Description from Response" + prodCodePath.get("Distribution_Channel_Desc")
														+ " is equal to the database value " + strdescription,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Description from Response" + prodCodePath.get("Distribution_Channel_Desc")
														+ " is  not equal to the database value " + strdescription,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									String splitProd_ResponseBeginDate = ((String) prodCodePath.get("Valid_From"))
											.split("T")[0];
									String compare_Prod_ResponseBeginDate = ((String) strBeginDate).split(" ")[0];
									if (splitProd_ResponseBeginDate.equals(compare_Prod_ResponseBeginDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Valid_From value from Response of  producer_code"
														+ prodCodePath.get("Valid_From") + " is  equal to data from db "
														+ strBeginDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Valid_From value from Response of  producer_code"
														+ prodCodePath.get("Valid_From") + " is  not equal to data from db "
														+ strBeginDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

									String splitProd_ResponseEndDate = ((String) prodCodePath.get("Valid_To")).split("T")[0];
									String compare_Prod_ResponseEndDate = ((String) strEndDate).split(" ")[0];
									if (splitProd_ResponseEndDate.equals(compare_Prod_ResponseEndDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Valid_To value from Response of  producer_code" + prodCodePath.get("Valid_To")
														+ " is  equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Valid_To value from Response of  producer_code" + prodCodePath.get("Valid_To")
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"code value from Response is not equal to Database"
													+ prodCodePath.get("Distribution_Channel_Cd"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"code value from Response is not equal to Database"
													+ prodCodePath.get("Distribution_Channel_Cd"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								} else {
									continue;
								}
							}
						}
						
						// To give the
						boolean stepResult = cFunc.allStepsResult(list);

						if (stepResult) {
							CustomReporting.logReport("", "",
									"Successfully Verified  Fields Values In Response With DB Columns ",
									StepStatus.SUCCESS, new String[] {}, startTime);
						} else {
							CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE,
									new String[] {}, startTime, null);
							throw new RuntimeException();
						}
					
				}

				else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

			}
		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API all MRDH_ValidateBusinessSegment
	public void MRDH_ValidateBusinessSegment(String tcID, SoftAssert softAs) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String value = data.getData("queryValue");
			// getting the list values from DataSheet and iterating
			List<String> result = Arrays.asList(value.split(","));
			Object[] objArray = result.toArray();

			for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray.length);
				System.out.println(objArray[index]);
				String qValue = (String) objArray[index];
				Response response = cFunc.webServiceCall("", "", cType, url, qValue, "get");
				// String resbody = response.asString();
				// System.out.println(resbody);

				// to find the response
				
				if (response != null) {
					String resbody = response.asString();
					//JsonPath prodCodePath = new JsonPath(resbody);
					System.out.println(resbody.length());
					//String abc=prodCodePath.get("Business_Segment_Cd");
					
					int code = response.statusCode();
					
					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport(
								"", "", "Retrieve Service returned response successfully with code: " + code
										+ " and message : " + statusLine,
								StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting.logReport("", "",
								"Unable to Retrieve Account Response.Response received is: " + resbody + code
										+ response.statusLine(),
								"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}

					// Query to be sent to DB with Qvalue from DataSheet
					String query = "select distinct Business_Segment_Cd,Business_Segment_Desc,Valid_From,Valid_To from MasterReferenceDataHub.[dbo].[Business_Segment] \r\n" + 
							"where Business_Segment_Cd=" +"'"+ qValue+"'";

					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB1(query,4 );

					String strCode = "";
					String strdescription = "";
					String strBeginDate = "";
					String strEndDate = "";
					// System.out.println("\n" +"producerArr"+producerArr.length());

					// Iteration is done for the Response
					
					
						int counter = 0;

						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strCode = (queryData.get(x))[0];
							strdescription = (queryData.get(x))[1];
							strBeginDate = (queryData.get(x))[2];
							strEndDate = (queryData.get(x))[3];
							JsonPath prodCodePath = new JsonPath(resbody);
						
							// To check for the firm_id
							int id = 0;
							if (prodCodePath.get("Business_Segment_Cd").equals(strCode)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"code value from Response is equal to Database"
													+ prodCodePath.get("Business_Segment_Cd"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									if (prodCodePath.get("Business_Segment_Desc").equals(strdescription)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Description from Response" + prodCodePath.get("Business_Segment_Desc")
														+ " is equal to the database value " + strdescription,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Description from Response" + prodCodePath.get("Business_Segment_Desc")
														+ " is  not equal to the database value " + strdescription,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									String splitProd_ResponseBeginDate = ((String) prodCodePath.get("Valid_From"))
											.split("T")[0];
									String compare_Prod_ResponseBeginDate = ((String) strBeginDate).split(" ")[0];
									if (splitProd_ResponseBeginDate.equals(compare_Prod_ResponseBeginDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Valid_From value from Response of  producer_code"
														+ prodCodePath.get("Valid_From") + " is  equal to data from db "
														+ strBeginDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Valid_From value from Response of  producer_code"
														+ prodCodePath.get("Valid_From") + " is  not equal to data from db "
														+ strBeginDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

									String splitProd_ResponseEndDate = ((String) prodCodePath.get("Valid_To")).split("T")[0];
									String compare_Prod_ResponseEndDate = ((String) strEndDate).split(" ")[0];
									if (splitProd_ResponseEndDate.equals(compare_Prod_ResponseEndDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Valid_To value from Response of  producer_code" + prodCodePath.get("Valid_To")
														+ " is  equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Valid_To value from Response of  producer_code" + prodCodePath.get("Valid_To")
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"code value from Response is not equal to Database"
													+ prodCodePath.get("Business_Segment_Cd"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"code value from Response is not equal to Database"
													+ prodCodePath.get("Business_Segment_Cd"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								} else {
									continue;
								}
							}
						
						}
						// To give the
						boolean stepResult = cFunc.allStepsResult(list);

						if (stepResult) {
							CustomReporting.logReport("", "",
									"Successfully Verified  Fields Values In Response With DB Columns ",
									StepStatus.SUCCESS, new String[] {}, startTime);
						} else {
							CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE,
									new String[] {}, startTime, null);
							throw new RuntimeException();
						}
					
						}

				else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
	
		}
		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API all MRDH_ValidateCompany
	public void MRDH_ValidateCompany(String tcID, SoftAssert softAs) throws Exception{
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String value = data.getData("queryValue");
			// getting the list values from DataSheet and iterating
			List<String> result = Arrays.asList(value.split(","));
			Object[] objArray = result.toArray();

			for (int index = 0; index < objArray.length; index++) {
				//System.out.println(objArray.length);
				//System.out.println(objArray[index]);
				String qValue = (String) objArray[index];
				Response response = cFunc.webServiceCall("", "", cType, url, qValue, "get");
				// String resbody = response.asString();
				 System.out.println(qValue);

				// to find the response
				if (response != null) {
					String resbody = response.asString();
					JsonPath prodCodePath = new JsonPath(resbody);
					//System.out.println(resbody.length());
					int code = response.statusCode();
					/*JSONArray producerArr = new JSONArray(resbody);*/

					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport(
								"", "", "Retrieve Service returned response successfully with code: " + code
										+ " and message : " + statusLine,
								StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting.logReport("", "",
								"Unable to Retrieve Account Response.Response received is: " + resbody + code
										+ response.statusLine(),
								"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}

					// Query to be sent to DB with Qvalue from DataSheet
					String query = "select  Company_Cd,Company_Name,Valid_From,Valid_To from MasterReferenceDataHub.[dbo].Company where Company_Cd="+"'"+qValue+"'" ;
					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB1(query, 4);
					System.out.println(queryData.size());
					String strCode = "";
					String strname = "";
					String strBeginDate = "";
					String strEndDate = "";
					// System.out.println("\n" +"producerArr"+producerArr.length());

					// Iteration is done for the Response
					//System.out.println(prodCodePath.get("Company_Cd"));
						// String prodCodeArr = path.get("producer_codes").toString();
						int counter = 0;
						
						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strCode = (queryData.get(x))[0];
							strname = (queryData.get(x))[1];
							strBeginDate = (queryData.get(x))[2];
							strEndDate = (queryData.get(x))[3];
						// To check for the firm_id
							int id = 0;
							if (prodCodePath.get("Company_Cd").equals(strCode)) {
								counter = 0;
								
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"code value from Response is equal to Database"
													+ prodCodePath.get("Company_Cd"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									if (prodCodePath.get("Company_Name").equals(strname)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Company_Name from Response" + prodCodePath.get("Company_Name")
														+ " is equal to the database value " + strname,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Company_Name from Response" + prodCodePath.get("Company_Name")
														+ " is  not equal to the database value " + strname,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									String splitProd_ResponseBeginDate = ((String) prodCodePath.get("Valid_From"))
											.split("T")[0];
									String compare_Prod_ResponseBeginDate = ((String) strBeginDate).split(" ")[0];
									if (splitProd_ResponseBeginDate.equals(compare_Prod_ResponseBeginDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Valid_From value from Response of  producer_code"
														+ prodCodePath.get("Valid_From") + " is  equal to data from db "
														+ strBeginDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Valid_From value from Response of  producer_code"
														+ prodCodePath.get("Valid_From") + " is  not equal to data from db "
														+ strBeginDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

									String splitProd_ResponseEndDate = ((String) prodCodePath.get("Valid_To")).split("T")[0];
									String compare_Prod_ResponseEndDate = ((String) strEndDate).split(" ")[0];
									if (splitProd_ResponseEndDate.equals(compare_Prod_ResponseEndDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"Valid_To value from Response of  producer_code" + prodCodePath.get("Valid_To")
														+ " is  equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Valid_To value from Response of  producer_code" + prodCodePath.get("Valid_To")
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"code value from Response is not equal to Database"
													+ prodCodePath.get("Company_Cd"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"code value from Response is not equal to Database"
													+ prodCodePath.get("Company_Cd"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								} else {
									continue;
								}
							}}
						
				
						// To give the
						boolean stepResult = cFunc.allStepsResult(list);

						if (stepResult) {
							CustomReporting.logReport("", "",
									"Successfully Verified  Fields Values In Response With DB Columns ",
									StepStatus.SUCCESS, new String[] {}, startTime);
						} else {
							CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE,
									new String[] {}, startTime, null);
							throw new RuntimeException();
						}
					
			}
		
				else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

			
			} 
			}
		catch (RuntimeException ex) {
			throw ex;
		}
	}
	//API all Validate_OPCompany_Companies EPRO-32723
	public void Validate_OPCompany_Companies(String tcID, SoftAssert softAs) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String queryValue = data.getData("queryValue");
			String queryValue1 = data.getData("queryValue1");
			String queryParameter = data.getData("queryParameter");
			String cType = data.getData("ContentType");

			String endpoint = url + "/" + queryValue1 + "/" + "producers?include=" + queryValue;
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
			// Response response = cFunc.webServiceCall_1("", "", cType, url,
			// qParameter1,qParameter, qValue1,qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select distinct opc.HierarchyLevelDesc ,\r\n"
						+ " prdl.firm_id,prdl.name,PRDL.ShortName,prdl.IsActive,prdl.FirmTaxId, prdl.Firm_begin_date,prdl.Firm_End_date,\r\n"
						+ " Producer_code ,PRDL.producer_code_IsActive, \r\n"
						+ "prdl.CompanyCd, prdl.CompanyName,prdl.company_begin_date , prdl.company_end_date\r\n"
						+ "from \r\n" + "\r\n"
						+ "(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n"
						+ "Case when a.isactive=0 \r\n"
						+ "     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n"
						+ "else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n"
						+ "from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
						+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
						+ "where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC ----- Operating company fields\r\n"
						+ "\r\n" + "left join \r\n" + "\r\n"
						+ "(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  a.FirmName name ,a.AbbreviatedName, a.ShortName,\r\n"
						+ "a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , \r\n" + "Case when a.isactive=0 \r\n"
						+ "     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n"
						+ "else  FirmTerminationDate  end Firm_End_date  , a.IsActive , b.WinsAgent \"Producer_code\", b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" , b.IsActive producer_code_IsActive,\r\n"
						+ "b.BusinessSegmentKey,d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" , b.ValidTodt \"businesssegment_endtdate\" ,\r\n"
						+ "  pa.AddressKey ,pa.AddressTypeKey, ad.AddressLine1 ,adt.AddressTypeDesc , ad.AddressLine2 , ad.AddressLine3 , null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\",\r\n"
						+ "  comp.CompanyCd  ,comp.CompanyName, pc.ValidFromdt company_begin_date , pc.ValidTodt company_end_date, pc.IsActive Company_is_active,\r\n"
						+ "  a.DistributionChannelKey, pd.DistributionChannelCD , pd.DistributionChannelDESC , a.ValidFromdt DistributionChannel_begin_date  , a.ValidTodt\r\n"
						+ "from   ProducerMDMDB.dbo.PRDFirm a\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
						+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__Company pc on pc.FirmKey = a.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDCompany comp on pc.CompanyKey=comp.CompanyKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n"
						+ ") as PRDL  ---- Producer Location fields\r\n" + "\r\n"
						+ "on PRDL.ParentFirmKey = opc.firmkey \r\n" + "where opc.firm_id= " + queryValue1;

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 14);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strName = "";
				String strShortName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				String strCompanyID = "";
				String strDescription = "";
				String strCompanyEndDate = "";
				String strCompanyBeginDate = "";
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[1];
						strName = (queryData.get(x))[2];
						strShortName = (queryData.get(x))[3];
						strActive = (queryData.get(x))[4];
						strTaxId = (queryData.get(x))[5];
						strBeginDate = (queryData.get(x))[6];
						strEndDate = (queryData.get(x))[7];
						strWinsAgent = (queryData.get(x))[8];

						strCompanyID = (queryData.get(x))[10];
						strDescription = (queryData.get(x))[11];
						strCompanyEndDate = (queryData.get(x))[13];
						strCompanyBeginDate = (queryData.get(x))[12];
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								// comparing the name from response with DB
								if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}
								// comparing the producer_codes from response with DB
								if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr1 = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									for (int j = 0; j < producerCodeArr1.length(); j++) {
										String prodCode = producerCodeArr1.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is equal to Database"
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is not equal to Database"
																+ prodCodePath.get("code"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										}

									}
								} else {
									if (strWinsAgent != "null") {
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}
								// companies
								if (producerArr.getJSONObject(i).has("companies")) {

									JSONArray producerCodeArr = producerArr.getJSONObject(i).getJSONArray("companies");
									for (int j = 0; j < producerCodeArr.length(); j++) {
										String prodCode = producerCodeArr.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strCompanyID)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"company code value from Response is equal the database value "
																+ strCompanyID,
														StepStatus.SUCCESS, new String[] {}, startTime);

												if (prodCodePath.get("description").equals(strDescription)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"Name value from Response" + prodCodePath.get("description")
																	+ " is equal to the database value "
																	+ strDescription,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Name value from Response" + prodCodePath.get("description")
																	+ " is  not equal to the database value "
																	+ strDescription,
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}

												String splitResponseBeginDate1 = ((String) prodCodePath
														.get("begin_date")).split("T")[0];
												String splitstrBeginDate = ((String) (strCompanyBeginDate))
														.split(" ")[0];

												if (splitResponseBeginDate1.equals(splitstrBeginDate)) {
													list.add(true);
													CustomReporting.logReport("", "", "BeginDate value from Response"
															+ prodCodePath.get("begin_date")
															+ " is  equal to the database value " + splitstrBeginDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"BeginDate value from Response"
																	+ prodCodePath.get("begin_date")
																	+ " is  not equal to the database value "
																	+ splitstrBeginDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												String splitstrEndDate = ((String) (strCompanyEndDate)).split(" ")[0];
												String splitResponseEndDate1 = ((String) prodCodePath.get("end_date"))
														.split("T")[0];
												if (splitResponseEndDate1.equals(splitstrEndDate)) {

													list.add(true);
													CustomReporting.logReport("", "", "End_date value from Response"
															+ prodCodePath.get("end_date")
															+ " is equal to the database value " + splitstrEndDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "", "End_date value from Response"
															+ " is  not equal to the database value " + strEndDate,
															StepStatus.FAILURE, new String[] {}, startTime);

												}
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"company code value from Response is not equal to Database value "
																+ prodCodePath.get("companies"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										}

									}
								} else {
									if (strCompanyID != "null") {
										CustomReporting.logReport("", "",
												"company code is not available in response and in Database it is "
														+ strCompanyID,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"company code is not available in response and in Database it is "
														+ strCompanyID,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}

							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}

	}
	//API all Validate_AllOperatingCompanies   EPRO-32809
	public void Validate_AllOperatingCompanies(String tcID, SoftAssert softAs) throws Exception {
			long startTime = System.currentTimeMillis();
			try {
				PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

				String env = System.getenv("PARAMETER_ENV");
				if (env != null) {
					if (env.equalsIgnoreCase("DEV")) {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					} else {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					}
				} else {
					if (EnvironmentData.getData("Environment").equals("DEV")) {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					} else {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					}
				}
				// data = PageDataManager.instance().getPageData("GetProducers", tcID);

				String url = data.getData("EndPointUrl");
				
				String cType = data.getData("ContentType");

				
				Response response = cFunc.webServiceCall("", "", cType, url, "get");
				// Response response = cFunc.webServiceCall_1("", "", cType, url,
				// qParameter1,qParameter, qValue1,qValue, "get");
				// String resbody = response.asString();
				// System.out.println(resbody);

				if (response != null) {
					String resbody = response.asString();
					System.out.println(resbody);
					int code = response.statusCode();
					JSONArray producerArr = new JSONArray(resbody);
					System.out.println(producerArr);
					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
								+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting
								.logReport("", "",
										"Unable to Retrieve Account Response.Response received is: " + resbody + code
												+ response.statusLine(),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}

					// Query to be sent to DB with Qvalue from DataSheet
					String query = "select distinct c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
							"Case when a.isactive=0 \r\n" + 
							"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
							"else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n" + 
							"from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
							"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
							"where ph.HierarchyLevelDesc = 'Operating Company'";

					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB(query, 5);

					// Initializing the variables for JSON response
					String strFirmId = "";
					String strName = "";
					String strBeginDate = "";
					String strEndDate = "";
					String strActive = "";
					
					int ResponseActive;
					int DataBaseActive;
					// System.out.println("\n" +"producerArr"+producerArr.length());

					// comparing the JSON response with DB by iteration
					for (int i = 0; i < producerArr.length(); i++) {
						String prod = producerArr.get(i).toString();
						JsonPath path = new JsonPath(prod);
						String FirmId = path.get("firm_id").toString();
						System.out.println(FirmId);
						


						// Getting single JSON response and iterating with DB data
						int counter = 0;
						for (int x = 0; x < queryData.size(); x++) {
							// Initializing the variables for DB queryData
							strFirmId = (queryData.get(x))[0];
							strName = (queryData.get(x))[1];
							strActive = (queryData.get(x))[4]; 
							strBeginDate = (queryData.get(x))[2];
							strEndDate = (queryData.get(x))[3];
							
							// comparing the Firm Id from response with DB
							int id = 0;
							if (path.get("firm_id").equals(strFirmId)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									// comparing the name from response with DB
									if (path.get("name").equals(strName)) {
										list.add(true);
										CustomReporting.logReport(
												"", "", "Name value from Response" + path.get("name")
														+ " is equal to data from db " + strName,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Name value from Response" + path.get("name")
														+ " is  not equal to data from db " + strName,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									

									
									// String BooleanActive = String.valueOf(path.getBoolean("active"));

									// comparing the Active status from response with DB
									ResponseActive = path.getBoolean("active") ? 1 : 0;
									DataBaseActive = Integer.parseInt(strActive);
									if (ResponseActive == (DataBaseActive)) {

										list.add(true);
										CustomReporting.logReport("", "",
												"Active value from Response" + path.getBoolean("active")
														+ " is  equal to data from db ",
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Active value from Response" + path.getBoolean("active")
														+ " is  equal to data from db ",
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									// comparing the begin_date from response with DB
									String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
									if (splitResponseBeginDate.equals(strBeginDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"BeginDate value from Response" + path.get("begin_date")
														+ " is  equal to data from db " + strBeginDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"BeginDate value from Response" + path.get("begin_date")
														+ " is  not equal to data from db " + strBeginDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									// comparing the end_date from response with DB
									if (!producerArr.getJSONObject(i).has("end_date")) {
										if (strEndDate == null) {
											System.out.println(FirmId + " NO end_date");
											list.add(true);
											CustomReporting.logReport("", "",
													"End_date value is not available in the Response and in DB value is "
															+ strEndDate,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport(
													"", "", "End_date value from Response"
															+ " is  not equal to data from db " + strEndDate,
													StepStatus.FAILURE, new String[] {}, startTime);

										}
									} else {
										String splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
										if (splitResponseEndDate.equals(strEndDate)) {
											System.out.println(FirmId + "end date" + strEndDate);
											list.add(true);
											CustomReporting.logReport("", "",
													"End_date value from Response" + path.get("end_date")
															+ " is equal to data from db " + strEndDate,
													StepStatus.SUCCESS, new String[] {}, startTime);
										}
									}
								
									
								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
											StepStatus.FAILURE, new String[] {}, startTime, null);
								}

								// break;
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "", path.get("firm_id")+ 
											"firm_id from Response is not equal to  Firm Id present in DB" , "",
											StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							}
						}
					}
				
					boolean stepResult = cFunc.allStepsResult(list);

					if (stepResult) {
						CustomReporting.logReport("", "",
								"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
								new String[] {}, startTime);
					} else {
						CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
								startTime, null);
						throw new RuntimeException();

					}

				} else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

			} catch (RuntimeException ex) {
				throw ex;
			}

		}
	//API all Validate_OPCompany_BusinessAddress EPRO-32720
	public void Validate_OPCompany_BusinessAddress(String tcID, SoftAssert softAs) throws Exception {
				long startTime = System.currentTimeMillis();
				try {
					PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

					String env = System.getenv("PARAMETER_ENV");
					if (env != null) {
						if (env.equalsIgnoreCase("DEV")) {
							data = PageDataManager.instance().getPageData("GetProducers", tcID);
						} else {
							data = PageDataManager.instance().getPageData("GetProducers", tcID);
						}
					} else {
						if (EnvironmentData.getData("Environment").equals("DEV")) {
							data = PageDataManager.instance().getPageData("GetProducers", tcID);
						} else {
							data = PageDataManager.instance().getPageData("GetProducers", tcID);
						}
					}
					// data = PageDataManager.instance().getPageData("GetProducers", tcID);

					String url = data.getData("EndPointUrl");
					String queryValue = data.getData("queryValue");
					String queryValue1 = data.getData("queryValue1");
					
					String cType = data.getData("ContentType");

					String endpoint = url + "/" + queryValue1 + "/" + "producers?include=" + queryValue;
					System.out.println(endpoint);
					Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
					// Response response = cFunc.webServiceCall_1("", "", cType, url,
					// qParameter1,qParameter, qValue1,qValue, "get");
					// String resbody = response.asString();
					// System.out.println(resbody);

					if (response != null) {
						String resbody = response.asString();
						System.out.println(resbody);
						int code = response.statusCode();
						JSONArray producerArr = new JSONArray(resbody);
						System.out.println(producerArr);
						String statusLine = response.statusLine();

						List<Boolean> list = new ArrayList<Boolean>();
						Collections.fill(list, Boolean.TRUE);
						// To check the status code from response
						CustomReporting.logReport("====Get Producers Service====" + url);
						if (code == 200) {
							list.add(true);
							CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
									+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
						} else {
							list.add(false);
							CustomReporting
									.logReport("", "",
											"Unable to Retrieve Account Response.Response received is: " + resbody + code
													+ response.statusLine(),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
							throw new RuntimeException();
						}

						// Query to be sent to DB with Qvalue from DataSheet
						String query = "select distinct opc.HierarchyLevelDesc ,\r\n" + 
								" prdl.firm_id,prdl.name,PRDL.ShortName,prdl.IsActive,prdl.FirmTaxId, prdl.Firm_begin_date,prdl.Firm_End_date,\r\n" + 
								" Producer_code ,PRDL.producer_code_IsActive, \r\n" + 
								"prdl.AddressLine1, prdl.AddressLine2 , prdl.AddressLine3 ,prdl.AddressLine4, prdl.locality ,\r\n" + 
								" prdl.PostalCode ,prdl.region,prdl.country_code,prdl.Buis_ValidFromdt,prdl.Buis_ValidTodt\r\n" + 
								"\r\n" + 
								"\r\n" + 
								"from \r\n" + 
								"\r\n" + 
								"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
								"Case when a.isactive=0 \r\n" + 
								"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
								"else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n" + 
								"from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n" + 
								"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n" + 
								"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
								"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
								"where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC ----- Operating company fields\r\n" + 
								"\r\n" + 
								"left join \r\n" + 
								"\r\n" + 
								"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  a.FirmName name ,a.AbbreviatedName, a.ShortName,\r\n" + 
								"a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , \r\n" + 
								"Case when a.isactive=0 \r\n" + 
								"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
								"else  FirmTerminationDate  end Firm_End_date  , a.IsActive , b.WinsAgent \"Producer_code\", b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" , b.IsActive producer_code_IsActive,\r\n" + 
								"b.BusinessSegmentKey,d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" , b.ValidTodt \"businesssegment_endtdate\" ,\r\n" + 
								"  pa.AddressKey ,pa.AddressTypeKey, ad.AddressLine1 ,adt.AddressTypeDesc , ad.AddressLine2 , ad.AddressLine3 , null  \"AddressLine4\" ,\r\n" + 
								"  ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\",pa.ValidFromdt \"Buis_ValidFromdt\",pa.ValidTodt \"Buis_ValidTodt\",\r\n" + 
								"  comp.CompanyCd  ,comp.CompanyName, pc.ValidFromdt company_begin_date , pc.ValidTodt company_end_date, pc.IsActive Company_is_active,\r\n" + 
								"  a.DistributionChannelKey, pd.DistributionChannelCD , pd.DistributionChannelDESC , a.ValidFromdt DistributionChannel_begin_date  , a.ValidTodt\r\n" + 
								"from   ProducerMDMDB.dbo.PRDFirm a\r\n" + 
								"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
								"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
								"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n" + 
								"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
								"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
								"left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n" + 
								"left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n" + 
								"left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n" + 
								"left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n" + 
								"left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n" + 
								"left join ProducerMDMDB.dbo.PRDFirm__Company pc on pc.FirmKey = a.FirmKey\r\n" + 
								"left join ProducerMDMDB.dbo.PRDCompany comp on pc.CompanyKey=comp.CompanyKey\r\n" + 
								"left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n" + 
								") as PRDL  ---- Producer Location fields\r\n" + 
								"\r\n" + 
								"on PRDL.ParentFirmKey = opc.firmkey \r\n" + 
								"where opc.firm_id= " + queryValue1;

						LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
						// queryData from the Database
						queryData = cFunc.getDataFromDB(query, 20);

						// Initializing the variables for JSON response
						String strFirmId = "";
						String strName = "";
						String strShortName = "";
						String strActive = "";
						String strBeginDate = "";
						String strTaxId = "";
						String strWinsAgent = "";
						String strEndDate = "";
						String splitResponseEndDate = "";
					
						String strAddress_1 = "";
						String strAddress_2 = "";
						String strAddress_3 = "";
						String strAddress_4 = "";
						String strLocality = "";
						String strPostalCode = "";
						String strRegion = "";
						String strCountry = "";
						String strBusinessBeginDate="";
						String strBusinessEndDate="";
						int ResponseActive;
						int DataBaseActive;
						// System.out.println("\n" +"producerArr"+producerArr.length());

						// comparing the JSON response with DB by iteration
						for (int i = 0; i < producerArr.length(); i++) {
							String prod = producerArr.get(i).toString();
							JsonPath path = new JsonPath(prod);
							String FirmId = path.get("firm_id").toString();

							// Getting single JSON response and iterating with DB data
							int counter = 0;
							for (int x = 0; x < queryData.size(); x++) {
								// Initializing the variables for DB queryData
								strFirmId = (queryData.get(x))[1];
								strName = (queryData.get(x))[2];
								strShortName = (queryData.get(x))[3];
								strActive = (queryData.get(x))[4];
								strTaxId = (queryData.get(x))[5];
								strBeginDate = (queryData.get(x))[6];
								strEndDate = (queryData.get(x))[7];
								strWinsAgent = (queryData.get(x))[8];

								
								strAddress_1 = (queryData.get(x))[10];
								strAddress_2 = (queryData.get(x))[11];
								strAddress_3 = (queryData.get(x))[12];
								strAddress_4 = (queryData.get(x))[13];
								strLocality = (queryData.get(x))[14];
								strPostalCode = (queryData.get(x))[15];
								strRegion = (queryData.get(x))[16];
								strCountry = (queryData.get(x))[17];
								strBusinessBeginDate= (queryData.get(x))[18];
								strBusinessEndDate= (queryData.get(x))[19];
								// comparing the Firm Id from response with DB
								int id = 0;
								if (path.get("firm_id").equals(strFirmId)) {
									counter = 0;
									id++;
									if (id != 0) {
										list.add(true);
										CustomReporting.logReport("", "",
												"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
												StepStatus.SUCCESS, new String[] {}, startTime);

										// comparing the name from response with DB
										if (path.get("name").equals(strName)) {
											list.add(true);
											CustomReporting.logReport(
													"", "", "Name value from Response" + path.get("name")
															+ " is equal to data from db " + strName,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"Name value from Response" + path.get("name")
															+ " is  not equal to data from db " + strName,
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
										// comparing the short_name from response with DB
										if (producerArr.getJSONObject(i).has("short_name")) {
											if (path.get("short_name").equals(strShortName)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"Short Name value from Response" + path.get("short_name")
																+ " is equal to data from db " + strShortName,
														StepStatus.SUCCESS, new String[] {}, startTime);

											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"Short Name value from Response" + path.get("short_name")
																+ " is  not equal to data from db " + strShortName,
														StepStatus.FAILURE, new String[] {}, startTime);
											}}else {
												list.add(true);
												CustomReporting.logReport("", "",
														"No Short Name value from Response and NULL in db " ,
														StepStatus.SUCCESS, new String[] {}, startTime);
											}

										// comparing the tax_id from response with DB
										if (path.get("tax_id").equals(strTaxId)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"TaxId value from Response" + path.get("tax_id")
															+ " is equal to data from db " + strTaxId,
													StepStatus.SUCCESS, new String[] {}, startTime);

										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"TaxId value from Response" + path.get("tax_id")
															+ " is  not equal to data from db " + strTaxId,
													StepStatus.FAILURE, new String[] {}, startTime);
										}
										// String BooleanActive = String.valueOf(path.getBoolean("active"));

										// comparing the Active status from response with DB
										ResponseActive = path.getBoolean("active") ? 1 : 0;
										DataBaseActive = Integer.parseInt(strActive);
										if (ResponseActive == (DataBaseActive)) {

											list.add(true);
											CustomReporting.logReport("", "",
													"Active value from Response" + path.getBoolean("active")
															+ " is  equal to data from db ",
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"Active value from Response" + path.getBoolean("active")
															+ " is  equal to data from db ",
													StepStatus.FAILURE, new String[] {}, startTime);
										}
										// comparing the begin_date from response with DB
										String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
										if (splitResponseBeginDate.equals(strBeginDate)) {
											list.add(true);
											CustomReporting.logReport("", "",
													"BeginDate value from Response" + path.get("begin_date")
															+ " is  equal to data from db " + strBeginDate,
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"BeginDate value from Response" + path.get("begin_date")
															+ " is  not equal to data from db " + strBeginDate,
													StepStatus.FAILURE, new String[] {}, startTime);
										}
										// comparing the end_date from response with DB
										if (!producerArr.getJSONObject(i).has("end_date")) {
											if (strEndDate == null) {
												System.out.println(FirmId + " NO end_date");
												list.add(true);
												CustomReporting.logReport("", "",
														"End_date value is not available in the Response and in DB value is "
																+ strEndDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport(
														"", "", "End_date value from Response"
																+ " is  not equal to data from db " + strEndDate,
														StepStatus.FAILURE, new String[] {}, startTime);

											}
										} else {
											splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
											if (splitResponseEndDate.equals(strEndDate)) {
												System.out.println(FirmId + "end date" + strEndDate);
												list.add(true);
												CustomReporting.logReport("", "",
														"End_date value from Response" + path.get("end_date")
																+ " is equal to data from db " + strEndDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											}
										}
										// comparing the producer_codes from response with DB
										if (producerArr.getJSONObject(i).has("producer_codes")) {

											JSONArray producerCodeArr1 = producerArr.getJSONObject(i)
													.getJSONArray("producer_codes");
											for (int j = 0; j < producerCodeArr1.length(); j++) {
												String prodCode = producerCodeArr1.get(j).toString();
												JsonPath prodCodePath = new JsonPath(prodCode);
												int count = 0;
												if (prodCodePath.get("code").equals(strWinsAgent)) {
													count++;
													if (count != 0) {
														list.add(true);
														CustomReporting.logReport("", "",
																"producer_codes value from Response is equal to Database"
																		+ prodCodePath.get("code"),
																StepStatus.SUCCESS, new String[] {}, startTime);
													} else {
														list.add(false);
														CustomReporting.logReport("", "",
																"producer_codes value from Response is not equal to Database"
																		+ prodCodePath.get("code"),
																"", StepStatus.FAILURE, new String[] {}, startTime, null);
													}
												}

											}
										} else {
											if (strWinsAgent != "null") {
												CustomReporting.logReport("", "",
														"producer code is not available in response and in Database it is "
																+ strWinsAgent,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(true);
												CustomReporting.logReport("", "",
														"producer code is not available in response and in Database it is "
																+ strWinsAgent,
														StepStatus.SUCCESS, new String[] {}, startTime);
											}

										}
										// To check for the business_address
										if (producerArr.getJSONObject(i).has("business_address")) {

											String country = path.get("business_address.country");

											if (path.get("business_address.address_line_1").equals(strAddress_1)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_1 from Response"
																+ path.get("business_address.address_line_1")
																+ " is equal to the database value " + strAddress_1,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"address_line_1 from Response"
																+ path.get("business_address.address_line_1")
																+ " is  not equal to the database value " + strAddress_1,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}

											if (path.get("business_address.address_line_2").equals(strAddress_2)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_2 from Response"
																+ path.get("business_address.address_line_2")
																+ " is equal to the database value " + strAddress_2,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"address_line_2 from Response"
																+ path.get("business_address.address_line_2")
																+ " is  not equal to the database value " + strAddress_2,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											if (path.get("business_address.address_line_3").equals(strAddress_3)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_3 from Response"
																+ path.get("business_address.address_line_3")
																+ " is equal to the database value " + strAddress_3,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"address_line_3 from Response"
																+ path.get("business_address.address_line_3")
																+ " is  not equal to the database value " + strAddress_3,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											if (!producerArr.getJSONObject(i).has("business_address.address_line_4")) {
												if (strAddress_4 == null) {
													list.add(true);
													CustomReporting.logReport("", "",
															"address_line_4 from Response is null and is equal to the database value ",
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"address_line_3 from Response is  not null and is not  equal to the database value ",
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"address_line_3 from Response is  not null and is not  equal to the database value ",
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											if (path.get("business_address.locality").equals(strLocality)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"locality from Response" + path.get("business_address.locality")
																+ " is equal to the database value " + strLocality,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"locality from Response" + path.get("business_address.locality")
																+ " is  not equal to the database value " + strLocality,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											if (path.get("business_address.region").equals(strRegion)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"region from Response" + path.get("business_address.region")
																+ " is equal to the database value " + strRegion,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"region from Response" + path.get("business_address.region")
																+ " is  not equal to the database value " + strRegion,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											if (path.get("business_address.postal_code").equals(strPostalCode)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"postal_code from Response" + path.get("business_address.postal_code")
																+ " is equal to the database value " + strPostalCode,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"postal_code from Response" + path.get("business_address.postal_code")
																+ " is  not equal to the database value " + strPostalCode,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											if (path.get("business_address.country").equals(strCountry)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"country from Response" + path.get("business_address.country")
																+ " is equal to the database value " + strCountry,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"country from Response" + path.get("business_address.country")
																+ " is  not equal to the database value " + strCountry,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											// begin and end date
											String splitBusinessResponseBeginDate = ((String) path
													.get("business_address.begin_date")).split("T")[0];
											String splitstrBeginDate = ((String) strBusinessBeginDate).split(" ")[0];
											if (splitBusinessResponseBeginDate.equals(splitstrBeginDate)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"BeginDate value from Response"
																+ path.get("business_address.begin_date")
																+ " is  equal to data from db " + splitstrBeginDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"BeginDate value from Response"
																+ path.get("business_address.begin_date")
																+ " is  not equal to data from db " + splitstrBeginDate,
														StepStatus.FAILURE, new String[] {}, startTime);
											}
											
											// comparing the end_date from response with DB

											String splitBusinessResponseEndDate = ((String) path
													.get("business_address.end_date")).split("T")[0];
											String splitstrEndDate = ((String) strBusinessEndDate).split(" ")[0];
											if (splitBusinessResponseEndDate.equals(splitstrEndDate)) {
												// System.out.println("end date" + strEndDate);
												list.add(true);
												CustomReporting.logReport("", "",
														"End_date value from Response"
																+ path.get("business_address.end_date")
																+ " is equal to data from db " + strBusinessEndDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"End_date value from Response"
																+ path.get("business_address.end_date")
																+ " is  not equal to data from db " + strBusinessEndDate,
														StepStatus.FAILURE, new String[] {}, startTime);
											}

										}

										else {

											list.add(false);
											CustomReporting.logReport("", "",
													"firm_id from Response is not equal to  Firm Id in Database "
															+ path.get("firm_id"),
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
										
										

									}

									else {

										list.add(false);
										CustomReporting.logReport("", "",
												"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
												StepStatus.FAILURE, new String[] {}, startTime, null);
									}

									// break;
								} else {
									counter++;
									if (counter >= queryData.size()) {
										list.add(false);
										CustomReporting.logReport("", "",
												"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
												StepStatus.FAILURE, new String[] {}, startTime, null);
									}
								}
							}
						}

						// To give the
						boolean stepResult = cFunc.allStepsResult(list);

						if (stepResult) {
							CustomReporting.logReport("", "",
									"Successfully Verified  Fields Values In Response With DB Columns ",
									StepStatus.SUCCESS, new String[] {}, startTime);
						} else {
							CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE,
									new String[] {}, startTime, null);
							throw new RuntimeException();
						}
					
			}
		
				else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

			
			
			}
		catch (RuntimeException ex) {
			throw ex;
		}
	}
		//API all Validate_OPCompany_DistributionChannel EPRO-32717
	public void Validate_OPCompany_DistributionChannel(String tcID, SoftAssert softAs) throws Exception {
					long startTime = System.currentTimeMillis();
					try {
						PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

						String env = System.getenv("PARAMETER_ENV");
						if (env != null) {
							if (env.equalsIgnoreCase("DEV")) {
								data = PageDataManager.instance().getPageData("GetProducers", tcID);
							} else {
								data = PageDataManager.instance().getPageData("GetProducers", tcID);
							}
						} else {
							if (EnvironmentData.getData("Environment").equals("DEV")) {
								data = PageDataManager.instance().getPageData("GetProducers", tcID);
							} else {
								data = PageDataManager.instance().getPageData("GetProducers", tcID);
							}
						}
						// data = PageDataManager.instance().getPageData("GetProducers", tcID);

						String url = data.getData("EndPointUrl");
						String queryValue = data.getData("queryValue");
						String queryValue1 = data.getData("queryValue1");
						String queryParameter = data.getData("queryParameter");
						String cType = data.getData("ContentType");

						String endpoint = url + "/" + queryValue1 + "/" + "producers?include=" + queryValue;
						System.out.println(endpoint);
						Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
						// Response response = cFunc.webServiceCall_1("", "", cType, url,
						// qParameter1,qParameter, qValue1,qValue, "get");
						// String resbody = response.asString();
						// System.out.println(resbody);

						if (response != null) {
							String resbody = response.asString();
							System.out.println(resbody);
							int code = response.statusCode();
							JSONArray producerArr = new JSONArray(resbody);
							System.out.println(producerArr);
							String statusLine = response.statusLine();

							List<Boolean> list = new ArrayList<Boolean>();
							Collections.fill(list, Boolean.TRUE);
							// To check the status code from response
							CustomReporting.logReport("====Get Producers Service====" + url);
							if (code == 200) {
								list.add(true);
								CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
										+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
							} else {
								list.add(false);
								CustomReporting
										.logReport("", "",
												"Unable to Retrieve Account Response.Response received is: " + resbody + code
														+ response.statusLine(),
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
								throw new RuntimeException();
							}

							// Query to be sent to DB with Qvalue from DataSheet
							String query = "select distinct opc.HierarchyLevelDesc ,\r\n" + 
									" prdl.firm_id,prdl.name,PRDL.ShortName,prdl.IsActive,prdl.FirmTaxId, prdl.Firm_begin_date,prdl.Firm_End_date,\r\n" + 
									" prdl.Producer_code,\r\n" + 
									" prdl.DistributionChannelCD , prdl.DistributionChannelDESC ,prdl.DistributionChannel_begin_date  ,prdl.ValidTodt\r\n" + 
									"\r\n" + 
									"\r\n" + 
									"\r\n" + 
									"from \r\n" + 
									"\r\n" + 
									"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
									"Case when a.isactive=0 \r\n" + 
									"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
									"else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n" + 
									"from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n" + 
									"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n" + 
									"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
									"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
									"where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC ----- Operating company fields\r\n" + 
									"\r\n" + 
									"left join \r\n" + 
									"\r\n" + 
									"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  a.FirmName name ,a.AbbreviatedName, a.ShortName,\r\n" + 
									"a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , \r\n" + 
									"Case when a.isactive=0 \r\n" + 
									"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
									"else  FirmTerminationDate  end Firm_End_date  , a.IsActive , b.WinsAgent \"Producer_code\", b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" , b.IsActive producer_code_IsActive,\r\n" + 
									"b.BusinessSegmentKey,d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" , b.ValidTodt \"businesssegment_endtdate\" ,\r\n" + 
									"  pa.AddressKey ,pa.AddressTypeKey, ad.AddressLine1 ,adt.AddressTypeDesc , ad.AddressLine2 , ad.AddressLine3 , null  \"AddressLine4\" ,\r\n" + 
									"  ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\",\r\n" + 
									"  comp.CompanyCd  ,comp.CompanyName, pc.ValidFromdt company_begin_date , pc.ValidTodt company_end_date, pc.IsActive Company_is_active,\r\n" + 
									"  a.DistributionChannelKey, pd.DistributionChannelCD , pd.DistributionChannelDESC , a.ValidFromdt DistributionChannel_begin_date  , a.ValidTodt\r\n" + 
									"from   ProducerMDMDB.dbo.PRDFirm a\r\n" + 
									"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
									"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
									"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n" + 
									"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
									"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
									"left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n" + 
									"left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n" + 
									"left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n" + 
									"left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n" + 
									"left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n" + 
									"left join ProducerMDMDB.dbo.PRDFirm__Company pc on pc.FirmKey = a.FirmKey\r\n" + 
									"left join ProducerMDMDB.dbo.PRDCompany comp on pc.CompanyKey=comp.CompanyKey\r\n" + 
									"left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n" + 
									") as PRDL  ---- Producer Location fields\r\n" + 
									"\r\n" + 
									"on PRDL.ParentFirmKey = opc.firmkey \r\n" + 
									"where opc.firm_id=	" + queryValue1;

							LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
							// queryData from the Database
							queryData = cFunc.getDataFromDB(query, 13);

							// Initializing the variables for JSON response
							String strFirmId = "";
							String strName = "";
							String strShortName = "";
							String strActive = "";
							String strBeginDate = "";
							String strTaxId = "";
							String strWinsAgent = "";
							String strEndDate = "";
							String splitResponseEndDate = "";
							String strDC_Code = "";
							String strdescription = "";
							String strDCBeginDate = "";
							String strDCEndDate = "";
							int ResponseActive;
							int DataBaseActive;
							// System.out.println("\n" +"producerArr"+producerArr.length());

							// comparing the JSON response with DB by iteration
							for (int i = 0; i < producerArr.length(); i++) {
								String prod = producerArr.get(i).toString();
								JsonPath path = new JsonPath(prod);
								String FirmId = path.get("firm_id").toString();

								// Getting single JSON response and iterating with DB data
								int counter = 0;
								for (int x = 0; x < queryData.size(); x++) {
									// Initializing the variables for DB queryData
									strFirmId = (queryData.get(x))[1];
									strName = (queryData.get(x))[2];
									strShortName = (queryData.get(x))[3];
									strActive = (queryData.get(x))[4];
									strTaxId = (queryData.get(x))[5];
									strBeginDate = (queryData.get(x))[6];
									strEndDate = (queryData.get(x))[7];
									strWinsAgent = (queryData.get(x))[8];
									strDC_Code = (queryData.get(x))[9];
									strdescription = (queryData.get(x))[10];
									strDCBeginDate = (queryData.get(x))[11];
									strDCEndDate = (queryData.get(x))[12];

									
									// comparing the Firm Id from response with DB
									int id = 0;
									if (path.get("firm_id").equals(strFirmId)) {
										counter = 0;
										id++;
										if (id != 0) {
											list.add(true);
											CustomReporting.logReport("", "",
													"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
													StepStatus.SUCCESS, new String[] {}, startTime);

											// comparing the name from response with DB
											if (path.get("name").equals(strName)) {
												list.add(true);
												CustomReporting.logReport(
														"", "", "Name value from Response" + path.get("name")
																+ " is equal to data from db " + strName,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"Name value from Response" + path.get("name")
																+ " is  not equal to data from db " + strName,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											// comparing the short_name from response with DB
											if (producerArr.getJSONObject(i).has("short_name")) {
												if (path.get("short_name").equals(strShortName)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"Short Name value from Response" + path.get("short_name")
																	+ " is equal to data from db " + strShortName,
															StepStatus.SUCCESS, new String[] {}, startTime);

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Short Name value from Response" + path.get("short_name")
																	+ " is  not equal to data from db " + strShortName,
															StepStatus.FAILURE, new String[] {}, startTime);
												}}else {
													list.add(true);
													CustomReporting.logReport("", "",
															"No Short Name value from Response and NULL in db " ,
															StepStatus.SUCCESS, new String[] {}, startTime);
												}
											// comparing the tax_id from response with DB
											if (path.get("tax_id").equals(strTaxId)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"TaxId value from Response" + path.get("tax_id")
																+ " is equal to data from db " + strTaxId,
														StepStatus.SUCCESS, new String[] {}, startTime);

											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"TaxId value from Response" + path.get("tax_id")
																+ " is  not equal to data from db " + strTaxId,
														StepStatus.FAILURE, new String[] {}, startTime);
											}
											// String BooleanActive = String.valueOf(path.getBoolean("active"));

											// comparing the Active status from response with DB
											ResponseActive = path.getBoolean("active") ? 1 : 0;
											DataBaseActive = Integer.parseInt(strActive);
											if (ResponseActive == (DataBaseActive)) {

												list.add(true);
												CustomReporting.logReport("", "",
														"Active value from Response" + path.getBoolean("active")
																+ " is  equal to data from db ",
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"Active value from Response" + path.getBoolean("active")
																+ " is  equal to data from db ",
														StepStatus.FAILURE, new String[] {}, startTime);
											}
											// comparing the begin_date from response with DB
											String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
											if (splitResponseBeginDate.equals(strBeginDate)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"BeginDate value from Response" + path.get("begin_date")
																+ " is  equal to data from db " + strBeginDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"BeginDate value from Response" + path.get("begin_date")
																+ " is  not equal to data from db " + strBeginDate,
														StepStatus.FAILURE, new String[] {}, startTime);
											}
											// comparing the end_date from response with DB
											if (!producerArr.getJSONObject(i).has("end_date")) {
												if (strEndDate == null) {
													System.out.println(FirmId + " NO end_date");
													list.add(true);
													CustomReporting.logReport("", "",
															"End_date value is not available in the Response and in DB value is "
																	+ strEndDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport(
															"", "", "End_date value from Response"
																	+ " is  not equal to data from db " + strEndDate,
															StepStatus.FAILURE, new String[] {}, startTime);

												}
											} else {
												splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
												if (splitResponseEndDate.equals(strEndDate)) {
													System.out.println(FirmId + "end date" + strEndDate);
													list.add(true);
													CustomReporting.logReport("", "",
															"End_date value from Response" + path.get("end_date")
																	+ " is equal to data from db " + strEndDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												}
											}
											// comparing the producer_codes from response with DB
											if (producerArr.getJSONObject(i).has("producer_codes")) {

												JSONArray producerCodeArr1 = producerArr.getJSONObject(i)
														.getJSONArray("producer_codes");
												for (int j = 0; j < producerCodeArr1.length(); j++) {
													String prodCode = producerCodeArr1.get(j).toString();
													JsonPath prodCodePath = new JsonPath(prodCode);
													int count = 0;
													if (prodCodePath.get("code").equals(strWinsAgent)) {
														count++;
														if (count != 0) {
															list.add(true);
															CustomReporting.logReport("", "",
																	"producer_codes value from Response is equal to Database"
																			+ prodCodePath.get("code"),
																	StepStatus.SUCCESS, new String[] {}, startTime);
														} else {
															list.add(false);
															CustomReporting.logReport("", "",
																	"producer_codes value from Response is not equal to Database"
																			+ prodCodePath.get("code"),
																	"", StepStatus.FAILURE, new String[] {}, startTime, null);
														}
													}

												}
											} else {
												if (strWinsAgent != "null") {
													CustomReporting.logReport("", "",
															"producer code is not available in response and in Database it is "
																	+ strWinsAgent,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(true);
													CustomReporting.logReport("", "",
															"producer code is not available in response and in Database it is "
																	+ strWinsAgent,
															StepStatus.SUCCESS, new String[] {}, startTime);
												}

											}
											
											if (producerArr.getJSONObject(i).has("distribution_channel")) {
												if (path.get("distribution_channel.code").equals(strDC_Code)) 
												{
													list.add(true);
													CustomReporting.logReport("", "",
															"code value from Response is equal to Database"
																	+ path.get("distribution_channel.code"),
															StepStatus.SUCCESS, new String[] {}, startTime);
															}

												else {

													list.add(false);
													CustomReporting.logReport("", "",
															"code value from Response is not equal to Database"
																	+ path.get("distribution_channel.code"),
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}

													if (path.get("distribution_channel.description").equals(strdescription)) {
														list.add(true);
														CustomReporting.logReport("", "",
																"Description from Response" + path.get("distribution_channel.description")
																		+ " is equal to the database value " + strdescription,
																StepStatus.SUCCESS, new String[] {}, startTime);
													} else {
														list.add(false);
														CustomReporting.logReport("", "",
																"Description from Response" + path.get("distribution_channel.description")
																		+ " is  not equal to the database value " + strdescription,
																"", StepStatus.FAILURE, new String[] {}, startTime, null);

													}
													String splitProd_ResponseBeginDate = ((String) path.get("distribution_channel.begin_date"))
															.split("T")[0];
													String compare_Prod_ResponseBeginDate = ((String) strDCBeginDate).split(" ")[0];
													if (splitProd_ResponseBeginDate.equals(compare_Prod_ResponseBeginDate)) {
														list.add(true);
														CustomReporting.logReport("", "",
																"Valid_From value from Response of  producer_code"
																		+ path.get("distribution_channel.begin_date") + " is  equal to data from db "
																		+ strDCBeginDate,
																StepStatus.SUCCESS, new String[] {}, startTime);
													} else {
														list.add(false);
														CustomReporting.logReport("", "",
																"Valid_From value from Response of  producer_code"
																		+ path.get("distribution_channel.begin_date") + " is  not equal to data from db "
																		+ strDCBeginDate,
																StepStatus.FAILURE, new String[] {}, startTime);
													}

													String splitProd_ResponseEndDate = ((String) path.get("distribution_channel.end_date")).split("T")[0];
													String compare_Prod_ResponseEndDate = ((String) strDCEndDate).split(" ")[0];
													if (splitProd_ResponseEndDate.equals(compare_Prod_ResponseEndDate)) {
														list.add(true);
														CustomReporting.logReport("", "",
																"Valid_To value from Response of  producer_code" + path.get("distribution_channel.end_date")
																		+ " is  equal to data from db " + strDCEndDate,
																StepStatus.SUCCESS, new String[] {}, startTime);
													} else {
														list.add(false);
														CustomReporting.logReport("", "",
																"Valid_To value from Response of  producer_code" + path.get("distribution_channel.end_date")
																		+ " is  not equal to data from db " + strDCEndDate,
																StepStatus.FAILURE, new String[] {}, startTime);
													}
											}	else {
												if (strDC_Code != "null") {
													CustomReporting.logReport("", "",
															"distribution_channel  is not available in response and in Database it is "
																	+ strDC_Code,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(true);
													CustomReporting.logReport("", "",
															"distribution_channel  is not available in response and in Database it is "
																	+ strDC_Code,
															StepStatus.SUCCESS, new String[] {}, startTime);
												}

											}
											

										}

										else {

											list.add(false);
											CustomReporting.logReport("", "",
													"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
													StepStatus.FAILURE, new String[] {}, startTime, null);
										}

										// break;
									} else {
										counter++;
										if (counter >= queryData.size()) {
											list.add(false);
											CustomReporting.logReport("", "",
													"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
													StepStatus.FAILURE, new String[] {}, startTime, null);
										}
									}
								}
							}

							boolean stepResult = cFunc.allStepsResult(list);

							if (stepResult) {
								CustomReporting.logReport("", "",
										"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
										new String[] {}, startTime);
							} else {
								CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
										startTime, null);
								throw new RuntimeException();

							}

						} else {
							CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
									StepStatus.FAILURE, new String[] {}, startTime, null);
							throw new RuntimeException();
						}

					} catch (RuntimeException ex) {
						throw ex;
					}

				}
		//API all Validate_Company_BusinessSegment
	public void Validate_Company_BusinessSegment(String tcID, SoftAssert softAs) throws Exception {
		//EPRO-32684
			long startTime = System.currentTimeMillis();
			try {
				PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

				String env = System.getenv("PARAMETER_ENV");
				if (env != null) {
					if (env.equalsIgnoreCase("DEV")) {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					} else {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					}
				} else {
					if (EnvironmentData.getData("Environment").equals("DEV")) {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					} else {
						data = PageDataManager.instance().getPageData("GetProducers", tcID);
					}
				}
				// data = PageDataManager.instance().getPageData("GetProducers", tcID);
				String url = data.getData("EndPointUrl");
				String qParameter = data.getData("queryParameter");

				String cType = data.getData("ContentType");

				String value = data.getData("queryValue");
				// getting the list values from DataSheet and iterating
				List<String> result = Arrays.asList(value.split(","));
				Object[] objArray = result.toArray();

				for (int index = 0; index < objArray.length; index++) {
					System.out.println(objArray[index]);
					String qValue = (String) objArray[index];
					Response response = cFunc.webServiceCall("", "", cType, url, qParameter, qValue, "get");
				// qParameter1,qParameter, qValue1,qValue, "get");
				// String resbody = response.asString();
				// System.out.println(resbody);

				if (response != null) {
					String resbody = response.asString();
					System.out.println(resbody);
					int code = response.statusCode();
					
					//System.out.println(producerArr);
					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
								+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting
								.logReport("", "",
										"Unable to Retrieve Account Response.Response received is: " + resbody + code
												+ response.statusLine(),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}
					JSONArray producerArr = new JSONArray(resbody);
					// Query to be sent to DB with Qvalue from DataSheet
					String query = "select distinct opc.HierarchyLevelDesc , opc.firm_id , opc.name , opc.Firm_begin_date , opc.Firm_End_date, opc.IsActive, prdl.BusinessSegmentCd , prdl.BusinessSegmentDesc \r\n" + 
							" from \r\n" + 
							"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
							"Case when a.isactive=0 \r\n" + 
							"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
							"else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n" + 
							"from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
							"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
							"where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC ----- Operating company fields\r\n" + 
							"left join \r\n" + 
							"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  --a.FirmName name, a.AbbreviatedName, a.ShortName\r\n" + 
							"a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , \r\n" + 
							"Case when a.isactive=0 \r\n" + 
							"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
							"else  FirmTerminationDate  end Firm_End_date  , a.IsActive , b.WinsAgent \"Producer_code\", b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" ,b.IsActive producer_code_IsActive,\r\n" + 
							" d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" , b.ValidTodt \"businesssegment_endtdate\" ,\r\n" + 
							"  pa.AddressKey ,pa.AddressTypeKey, ad.AddressLine1 ,adt.AddressTypeDesc , ad.AddressLine2 , ad.AddressLine3 , null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\"\r\n" + 
							"from   ProducerMDMDB.dbo.PRDFirm a\r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
							"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n" + 
							"left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n" + 
							"left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey ) as PRDL  ---- Producer Location fields\r\n" + 
							"on PRDL.ParentFirmKey = opc.firmkey \r\n" + 
							"where BusinessSegmentCd="+"'"+ qValue +"'"+"order by  prdl.BusinessSegmentCd,opc.firm_id";

					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB(query, 8);
					// Initializing the variables for JSON response
					String strFirmId = "";
					String strName = "";
					String strBeginDate = "";
					String strActive = "";
					
					int ResponseActive;
					int DataBaseActive;
					// System.out.println("\n" +"producerArr"+producerArr.length());

					// comparing the JSON response with DB by iteration
					for (int i = 0; i < producerArr.length(); i++) {
						String prod = producerArr.get(i).toString();
						JsonPath path = new JsonPath(prod);
						String FirmId = path.get("firm_id").toString();
						System.out.println(FirmId);
					
						// Getting single JSON response and iterating with DB data

						int counter = 0;
						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strFirmId = (queryData.get(0))[1];
							strName = (queryData.get(0))[2];
							strActive = (queryData.get(0))[5];
							strBeginDate = (queryData.get(0))[3];
							System.out.println(strFirmId +strName+strActive+strBeginDate);
							// comparing the Firm Id from response with DB
							int id = 0;
							if (path.get("firm_id").equals(strFirmId)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									// comparing the name from response with DB
									if (path.get("name").equals(strName)) {
										list.add(true);
										CustomReporting.logReport(
												"", "", "Name value from Response" + path.get("name")
														+ " is equal to data from db " + strName,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Name value from Response" + path.get("name")
														+ " is  not equal to data from db " + strName,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									
									// comparing the begin_date from response with DB
									String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
									if (splitResponseBeginDate.equals(strBeginDate)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"BeginDate value from Response" + path.get("begin_date")
														+ " is  equal to data from db " + strBeginDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"BeginDate value from Response" + path.get("begin_date")
														+ " is  not equal to data from db " + strBeginDate,
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									
								
									
									// String BooleanActive = String.valueOf(path.getBoolean("active"));

									// comparing the Active status from response with DB
									ResponseActive = path.getBoolean("active") ? 1 : 0;
									DataBaseActive = Integer.parseInt(strActive);
									if (ResponseActive == (DataBaseActive)) {

										list.add(true);
										CustomReporting.logReport("", "",
												"Active value from Response" + path.getBoolean("active")
														+ " is  equal to data from db ",
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"Active value from Response" + path.getBoolean("active")
														+ " is  equal to data from db ",
												StepStatus.FAILURE, new String[] {}, startTime);
									}
									
									
								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
											StepStatus.FAILURE, new String[] {}, startTime, null);
								}

								// break;
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
											StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							}
						}
					}

					boolean stepResult = cFunc.allStepsResult(list);

					if (stepResult) {
						CustomReporting.logReport("", "",
								"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
								new String[] {}, startTime);
					} else {
						CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
								startTime, null);
						throw new RuntimeException();

					}

				} else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				}
			} catch (RuntimeException ex) {
				throw ex;
			}

		}
	//API all Validate_OPCompany_Producers 
	public void Validate_OPCompany_Producers(String tcID, SoftAssert softAs) throws Exception {
		//32810
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String queryValue = data.getData("queryValue");
			
			
			String cType = data.getData("ContentType");

			String endpoint = url + "/" + queryValue + "/" + "producers" ;
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
			// Response response = cFunc.webServiceCall_1("", "", cType, url,
			// qParameter1,qParameter, qValue1,qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select distinct opc.HierarchyLevelDesc ,prdl.firm_id,prdl.name,PRDL.ShortName,prdl.IsActive,prdl.FirmTaxId, prdl.Firm_begin_date,prdl.Firm_End_date,\r\n" + 
						"Producer_code , prdl.BusinessSegmentCd from \r\n" + 
						"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
						"Case when a.isactive=0 then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end)\r\n" + 
						"else  FirmTerminationDate  end Firm_End_date  , a.IsActive from  ProducerMDMDB.dbo.PRDFirm__Link c\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
						"where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC  left join\r\n" + 
						"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  a.FirmName name ,a.AbbreviatedName, a.ShortName,\r\n" + 
						" a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , Case when a.isactive=0 \r\n" + 
						"then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
						"else  FirmTerminationDate  end Firm_End_date  , a.IsActive , b.WinsAgent \"Producer_code\", b.BusinessSegmentKey,d.BusinessSegmentCd from   ProducerMDMDB.dbo.PRDFirm a\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n" + 
						"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
						"left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n" + 
						"left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n" + 
						"left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey  ) as PRDL\r\n" + 
						"on PRDL.ParentFirmKey = opc.firmkey where opc.firm_id= " + queryValue;

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 10);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strName = "";
				String strShortName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				String strBusinessSegmentCode="";
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[1];
						strName = (queryData.get(x))[2];
						strShortName = (queryData.get(x))[3];
						strActive = (queryData.get(x))[4];
						strTaxId = (queryData.get(x))[5];
						strBeginDate = (queryData.get(x))[6];
						strEndDate = (queryData.get(x))[7];
						strWinsAgent = (queryData.get(x))[8];
						strBusinessSegmentCode= (queryData.get(x))[9];
						
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								// comparing the name from response with DB
								if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}
								// comparing the producer_codes from response with DB
								if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr1 = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									for (int j = 0; j < producerCodeArr1.length(); j++) {
										String prodCode = producerCodeArr1.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is equal to Database"
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);
												
												
												
												if (producerCodeArr1.getJSONObject(j).has("business_segment_restrictions")) {

													JSONArray BusinessCodeArr1 = producerCodeArr1.getJSONObject(j)
															.getJSONArray("business_segment_restrictions");
													for (int k = 0; k < BusinessCodeArr1.length(); k++) {
														String BusinessCode = BusinessCodeArr1.get(k).toString();
														JsonPath BusinessCodePath = new JsonPath(BusinessCode);
														int count1 = 0;
														if (BusinessCodePath.get("code").equals(strBusinessSegmentCode)) {
															count1++;
															if (count1 != 0) {
															list.add(true);
															CustomReporting.logReport("", "",
																	"BusinessSegment_Code value from Response is equal to Database"
																			+ BusinessCodePath.get("code"),
																	StepStatus.SUCCESS, new String[] {}, startTime);
														}
														else {
															list.add(false);
															CustomReporting.logReport("", "",
																	"BusinessSegment_Code value from Response is not equal to Database"
																			+ BusinessCodePath.get("code"),
																	"", StepStatus.FAILURE, new String[] {}, startTime, null);
														}}
														
											}}else {
												if (strBusinessSegmentCode != "null") {
													CustomReporting.logReport("", "",
															"producer code is not available in response and in Database  "
																	,
															StepStatus.SUCCESS, new String[] {}, startTime);
												}else {
													list.add(false);
													CustomReporting.logReport("", "",
															"BusinessSegment_Code value from Response is not equal to Database"
																	+ strBusinessSegmentCode,
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												
												}

											} 
										}
											else {
											list.add(false);
											CustomReporting.logReport("", "",
													"producer_codes value from Response is not equal to Database"
															+ prodCodePath.get("code"),
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}

									}
								} }else {
									if (strWinsAgent != "null") {
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}
								
								
							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}

	}
	//EPRO-33290
	//API all Validate_Producer_code,Business_segment 
	public void Validate_Producer_code(String tcID, SoftAssert softAs) throws Exception{
		//33290
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);
			String url = data.getData("EndPointUrl");
			
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String value = data.getData("queryValue");
			// getting the list values from DataSheet and iterating
			List<String> result = Arrays.asList(value.split(","));
			Object[] objArray = result.toArray();

			for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray[index]);
				String qValue = (String) objArray[index];
			
				String endpoint = url + "/" + qValue + "/"  ;
				System.out.println(endpoint);
				Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
				// String resbody = response.asString();
				// System.out.println(resbody);
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String res = response.asString();
				String resbody = "[" + res + "]"  ;
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select distinct c.EPICKey firm_id , a.FirmName name, a.ShortName , a.AbbreviatedName, a.IsActive ,a.FirmTaxId , a.FirmEffectiveDate Firm_begin_date ,\r\n" + 
						"Case when a.isactive=0 then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
						"else  FirmTerminationDate  end Firm_End_date, \r\n" + 
						"b.WinsAgent \"Producer_code\", b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" , b.IsActive producer_code_IsActive,\r\n" + 
						"d.BusinessSegmentCd, b.ValidFromDt \"businessSegment_startdate\" , b.ValidTodt \"businesssegment_endtdate\" , d.BusinessSegmentDesc \r\n" + 
						"from ProducerMDMDB.dbo.PRDFirm a \r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey \r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey \r\n" + 
						"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey \r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey \r\n" + 
						"where ph.HierarchyLevelDesc = 'Producer Location' and c.EPICKey=" + qValue;

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 16);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strName = "";
				String strShortName = "";
				String strAbbreviatedName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				
				String strProdActive = "";
				String strProdBeginDate = "";
				String strProdEndDate = "";
				String strBusinessSegmentCode="";
				String strBSDescription = "";
				String strBSBeginDate = "";
				String strBSEndDate = "";
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[0];
						strName = (queryData.get(x))[1];
						strShortName = (queryData.get(x))[2];
						strAbbreviatedName= (queryData.get(x))[3];
						strActive = (queryData.get(x))[4];
						strTaxId = (queryData.get(x))[5];
						strBeginDate = (queryData.get(x))[6];
						strEndDate = (queryData.get(x))[7];
						
						
						strWinsAgent = (queryData.get(x))[8];
						strProdBeginDate = (queryData.get(x))[9];
						strProdEndDate = (queryData.get(x))[10];
						strProdActive=(queryData.get(x))[11];
						
						strBusinessSegmentCode= (queryData.get(x))[12];
						strBSBeginDate= (queryData.get(x))[13];
						strBSEndDate= (queryData.get(x))[14];
						strBSDescription= (queryData.get(x))[15];
						
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								// comparing the name from response with DB
								if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								
								if (path.get("abbreviated_name").equals(strAbbreviatedName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Abbreviated Name value from Response" + path.get("abbreviated_name")
													+ " is equal to data from db " + strAbbreviatedName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Abbreviated Name value from Response" + path.get("abbreviated_name")
													+ " is  not equal to data from db " + strAbbreviatedName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}
								// comparing the producer_codes from response with DB
								if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr1 = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									for (int j = 0; j < producerCodeArr1.length(); j++) {
										String prodCode = producerCodeArr1.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is equal to Database"
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);
												
												int ProdResponseActive = prodCodePath.getBoolean("active") ? 1 : 0;
												int ProdDataBaseActive = Integer.parseInt(strProdActive);
												if (ProdResponseActive == (ProdDataBaseActive)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"Producer Active value from Response" + prodCodePath.getBoolean("active")
																	+ " is  equal to data from db ",
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Producer Active value from Response" + prodCodePath.getBoolean("active")
																	+ " is  equal to data from db ",
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												
												String splitResponseProdBeginDate = ((String) prodCodePath.get("begin_date")).split("T")[0];
												if (splitResponseProdBeginDate.equals(strProdBeginDate.split(" ")[0])) {
													list.add(true);
													CustomReporting.logReport("", "",
															"Producer BeginDate value from Response" + prodCodePath.get("begin_date")
																	+ " is  equal to data from db " + strProdBeginDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Producer BeginDate value from Response" + prodCodePath.get("begin_date")
																	+ " is  not equal to data from db " + strProdBeginDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												
												
													String splitResponseProdEndDate = ((String) prodCodePath.get("end_date")).split("T")[0];
													if (splitResponseProdEndDate.equals(strProdEndDate.split(" ")[0])) {
														
														list.add(true);
														CustomReporting.logReport("", "",
																"Producer End_date value from Response" + prodCodePath.get("end_date")
																		+ " is equal to data from db " + strProdEndDate,
																StepStatus.SUCCESS, new String[] {}, startTime);
													}else {
														list.add(false);
														CustomReporting.logReport("", "",
																"Producer end_date value from Response" + prodCodePath.get("end_date")
																		+ " is  not equal to data from db " + strProdEndDate,
																StepStatus.FAILURE, new String[] {}, startTime);
													}
												
												if (producerCodeArr1.getJSONObject(j).has("business_segment_restrictions")) {

													JSONArray BusinessCodeArr1 = producerCodeArr1.getJSONObject(j)
															.getJSONArray("business_segment_restrictions");
													for (int k = 0; k < BusinessCodeArr1.length(); k++) {
														String BusinessCode = BusinessCodeArr1.get(k).toString();
														JsonPath BusinessCodePath = new JsonPath(BusinessCode);
														int count1 = 0;
														if (BusinessCodePath.get("code").equals(strBusinessSegmentCode)) {
															count1++;
															if (count1 != 0) {
															list.add(true);
															CustomReporting.logReport("", "",
																	"BusinessSegment_Code value from Response is equal to Database"
																			+ BusinessCodePath.get("code"),
																	StepStatus.SUCCESS, new String[] {}, startTime);
															
															String splitResponseBSBeginDate = ((String) BusinessCodePath.get("begin_date")).split("T")[0];
															if (splitResponseBSBeginDate.equals(strBSBeginDate.split(" ")[0])) {
																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segement BeginDate value from Response" + BusinessCodePath.get("begin_date")
																				+ " is  equal to data from db " + strBSBeginDate,
																		StepStatus.SUCCESS, new String[] {}, startTime);
															} else {
																list.add(false);
																CustomReporting.logReport("", "",
																		"Business_segement BeginDate value from Response" + BusinessCodePath.get("begin_date")
																				+ " is  not equal to data from db " + strBSBeginDate,
																		StepStatus.FAILURE, new String[] {}, startTime);
															}
															
															String splitResponseBSEndDate = ((String) BusinessCodePath.get("end_date")).split("T")[0];
															if (splitResponseBSEndDate.equals(strBSEndDate.split(" ")[0])) {
																
																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segement End_date value from Response" + BusinessCodePath.get("end_date")
																				+ " is equal to data from db " + strBSEndDate,
																		StepStatus.SUCCESS, new String[] {}, startTime);
															}else {
																list.add(false);
																CustomReporting.logReport("", "",
																		"Business_segement end_date value from Response" + BusinessCodePath.get("end_date")
																				+ " is  not equal to data from db " + strBSEndDate,
																		StepStatus.FAILURE, new String[] {}, startTime);
															}
															
															if (BusinessCodePath.get("description").equals(strBSDescription)) {
																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segement description value from Response" + BusinessCodePath.get("description")
																				+ " is equal to data from db " + strBSDescription,
																		StepStatus.SUCCESS, new String[] {}, startTime);

															} else {
																list.add(false);
																CustomReporting.logReport("", "",
																		"Business_segement description value from Response" + BusinessCodePath.get("description")
																				+ " is  not equal to data from db " + strBSDescription,
																		StepStatus.FAILURE, new String[] {}, startTime);
															}
															
														}
														else {
															list.add(false);
															CustomReporting.logReport("", "",
																	"BusinessSegment_Code value from Response is not equal to Database"
																			+ BusinessCodePath.get("code"),
																	"", StepStatus.FAILURE, new String[] {}, startTime, null);
														}}
														
											}}else {
												if (strBusinessSegmentCode != "null") {
													CustomReporting.logReport("", "",
															"producer code is not available in response and in Database  "
																	,
															StepStatus.SUCCESS, new String[] {}, startTime);
												}else {
													list.add(false);
													CustomReporting.logReport("", "",
															"BusinessSegment_Code value from Response is not equal to Database"
																	+ strBusinessSegmentCode,
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												
												}

											} 
										}
											else {
											list.add(false);
											CustomReporting.logReport("", "",
													"producer_codes value from Response is not equal to Database"
															+ prodCodePath.get("code"),
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}

									}
								} }else {
									if (strWinsAgent != "null") {
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}
								
								
							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}
			}
		} catch (RuntimeException ex) {
			throw ex;
		}

	}
	//EPRO-33518  
	//API all Validate_OPCompany_BusinessSegment 
	public void Validate_OPCompany_BusinessSegment(String tcID, SoftAssert softAs) throws Exception{ 
		//33518
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers_QA", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);
			String url = data.getData("EndPointUrl");
			String queryValue = data.getData("queryValue");
			String queryValue1 = data.getData("queryValue1");
			String qParameter = data.getData("queryParameter");
			String qParameter1 = data.getData("queryParameter1");
			String cType = data.getData("ContentType");
						
			String endpoint = url + "?" + qParameter + "="+queryValue+"&"+qParameter1+"="+queryValue1;
		//	String endpoint = url + "/" + queryValue + "/" + "producers"  + "/";
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall_1("", "", cType, endpoint,qParameter,qParameter1, queryValue, queryValue1,  "get");
			
			String splitValue = "";
			String qValue="";
			List<String> result = Arrays.asList(queryValue1.split(","));
			Object[] objArray = result.toArray();
			
			if(objArray.length==1) {splitValue="("+"'" + queryValue1 +"'"  +")";;
			
			}
			else {
		for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray[index]);
				 qValue = "'" +(String) objArray[index]+ "'" +","+qValue;
				 System.out.println(qValue);
		}
		String abc=qValue.substring(0, qValue.length() - 1);
		System.out.println(abc);
		splitValue="("+ abc  +")";
		System.out.println(splitValue);
			}	
				
			Boolean flag;
			if (!queryValue1.equals("")) {
				flag=true;
				
			}else {
				flag=false;
			}
				

			if (response != null) {
				String resbody = response.asString();
				
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();
				System.out.println(response.getHeader("X-Total-Count"));

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				String query = "";
				// Query to be sent to DB with Qvalue from DataSheet
				if(flag==true) {
					query= "select distinct   prdl.firm_id,  prdl.name , prdl.ShortName, prdl.AbbreviatedName,prdl.IsActive ,\r\n" + 
							"prdl.FirmTaxId ,prdl.Firm_begin_date , prdl.Firm_End_date  ,  prdl.Producer_code, prdl.producer_code_startdate , prdl.producer_code_endtdate ,prdl.producer_code_IsActive,\r\n" + 
							" prdl.BusinessSegmentCd,  prdl.businessSegment_startdate , prdl.businesssegment_endtdate ,prdl.BusinessSegmentDesc ,\r\n" + 
							"   prdl.AddressLine1 , prdl.AddressLine2 , prdl.AddressLine3 , prdl.AddressLine4 ,prdl.locality,  prdl.PostalCode, prdl.region , prdl.country_code\r\n" + 
							" from \r\n" + 
							"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
							"Case when a.isactive=0 \r\n" + 
							"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
							"else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n" + 
							"from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
							"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
							"where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC ----- Operating company fields\r\n" + 
							"left join \r\n" + 
							"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  a.FirmName name , a.AbbreviatedName, a.ShortName,\r\n" + 
							"a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , \r\n" + 
							"Case when a.isactive=0 \r\n" + 
							"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
							"else  FirmTerminationDate  end Firm_End_date  , a.IsActive , b.WinsAgent \"Producer_code\", b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" ,b.IsActive producer_code_IsActive,\r\n" + 
							" b.BusinessSegmentKey,d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" , b.ValidTodt \"businesssegment_endtdate\" ,\r\n" + 
							"  pa.AddressKey ,pa.AddressTypeKey,adt.AddressTypeDesc , ad.AddressLine1 , ad.AddressLine2 , ad.AddressLine3 , null  \"AddressLine4\" , ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\",\r\n" + 
							"  comp.CompanyCd  ,comp.CompanyName, pc.ValidFromdt company_begin_date , pc.ValidTodt company_end_date, pc.IsActive Company_is_active,\r\n" + 
							"  a.DistributionChannelKey, pd.DistributionChannelCD , pd.DistributionChannelDESC , a.ValidFromdt DistributionChannel_begin_date  , a.ValidTodt\r\n" + 
							"from   ProducerMDMDB.dbo.PRDFirm a\r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
							"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n" + 
							"left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n" + 
							"left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__Company pc on pc.FirmKey = a.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDCompany comp on pc.CompanyKey=comp.CompanyKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n" + 
							") as PRDL  ---- Producer Location fields\r\n" + 
							"on PRDL.ParentFirmKey = opc.firmkey "+
						"where  opc.firm_id="+queryValue +" and prdl.BusinessSegmentCd in"+  splitValue+"and prdl.AddressTypeDesc=+'Business Location' ";
				}
				else {
					query= "select distinct   prdl.firm_id,  prdl.name , prdl.ShortName, prdl.AbbreviatedName,prdl.IsActive ,\r\n" + 
							"prdl.FirmTaxId ,prdl.Firm_begin_date , prdl.Firm_End_date  ,  prdl.Producer_code, prdl.producer_code_startdate , prdl.producer_code_endtdate ,prdl.producer_code_IsActive,\r\n" + 
							" prdl.BusinessSegmentCd,  prdl.businessSegment_startdate , prdl.businesssegment_endtdate ,prdl.BusinessSegmentDesc from \r\n" + 
							"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
							"Case when a.isactive=0 \r\n" + 
							"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
							"else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n" + 
							"from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
							"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
							"where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC ----- Operating company fields\r\n" + 
							"left join \r\n" + 
							"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  a.FirmName name , a.AbbreviatedName, a.ShortName,\r\n" + 
							"a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , \r\n" + 
							"Case when a.isactive=0 \r\n" + 
							"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
							"else  FirmTerminationDate  end Firm_End_date  , a.IsActive , b.WinsAgent \"Producer_code\", b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" ,b.IsActive producer_code_IsActive,\r\n" + 
							" b.BusinessSegmentKey,d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" , b.ValidTodt \"businesssegment_endtdate\" \r\n" + 
							" \r\n" + 
							"from   ProducerMDMDB.dbo.PRDFirm a\r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
							"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n" + 
							"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
							"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
							"\r\n" + 
							"\r\n" + 
							"\r\n" + 
							"left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n" + 
							") as PRDL  ---- Producer Location fields\r\n" + 
							"on PRDL.ParentFirmKey = opc.firmkey \r\n" + 
							"where  opc.firm_id="+queryValue +" and prdl.BusinessSegmentCd in "+ splitValue;
				}
				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				//queryData = cFunc.getDataFromDB(query, 16);
				// queryData from the Database
			if(flag==true) {queryData = cFunc.getDataFromDB(query, 24);}
				else {queryData = cFunc.getDataFromDB(query, 16);}
				

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strName = "";
				String strShortName = "";
				String strAbbreviatedName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				
				String strProdActive = "";
				String strProdBeginDate = "";
				String strProdEndDate = "";
				String strBusinessSegmentCode="";
				String strBSDescription = "";
				String strBSBeginDate = "";
				String strBSEndDate = "";
				
				String strAddress_1 = "";
				String strAddress_2 = "";
				String strAddress_3 = "";
				String strAddress_4 = "";
				String strLocality = "";
				String strPostalCode = "";
				String strRegion = "";
				String strCountry = "";
				
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[0];
						strName = (queryData.get(x))[1];
						strShortName = (queryData.get(x))[2];
						strAbbreviatedName= (queryData.get(x))[3];
						strActive = (queryData.get(x))[4];
						strTaxId = (queryData.get(x))[5];
						strBeginDate = (queryData.get(x))[6];
						strEndDate = (queryData.get(x))[7];
						
						
						strWinsAgent = (queryData.get(x))[8];
						strProdBeginDate = (queryData.get(x))[9];
						strProdEndDate = (queryData.get(x))[10];
						strProdActive=(queryData.get(x))[11];
						
						strBusinessSegmentCode= (queryData.get(x))[12];
						strBSBeginDate= (queryData.get(x))[13];
						strBSEndDate= (queryData.get(x))[14];
						strBSDescription= (queryData.get(x))[15];
						
						if(flag==true) {
						strAddress_1 = (queryData.get(x))[16];
						strAddress_2 = (queryData.get(x))[17];
						strAddress_3 = (queryData.get(x))[18];
						strAddress_4 = (queryData.get(x))[19];
						strLocality = (queryData.get(x))[20];
						strPostalCode = (queryData.get(x))[21];
						strRegion = (queryData.get(x))[22];
						strCountry = (queryData.get(x))[23];
						}
						
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								// comparing the name from response with DB
								if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								
								if (path.get("abbreviated_name").equals(strAbbreviatedName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Abbreviated Name value from Response" + path.get("abbreviated_name")
													+ " is equal to data from db " + strAbbreviatedName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Abbreviated Name value from Response" + path.get("abbreviated_name")
													+ " is  not equal to data from db " + strAbbreviatedName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}
								// comparing the producer_codes from response with DB
								if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr1 = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									for (int j = 0; j < producerCodeArr1.length(); j++) {
										String prodCode = producerCodeArr1.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is equal to Database"
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);
												
												int ProdResponseActive = prodCodePath.getBoolean("active") ? 1 : 0;
												int ProdDataBaseActive = Integer.parseInt(strProdActive);
												if (ProdResponseActive == (ProdDataBaseActive)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"Producer Active value from Response" + prodCodePath.getBoolean("active")
																	+ " is  equal to data from db ",
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Producer Active value from Response" + prodCodePath.getBoolean("active")
																	+ " is  equal to data from db ",
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												
												String splitResponseProdBeginDate = ((String) prodCodePath.get("begin_date")).split("T")[0];
												if (splitResponseProdBeginDate.equals(strProdBeginDate.split(" ")[0])) {
													list.add(true);
													CustomReporting.logReport("", "",
															"Producer BeginDate value from Response" + prodCodePath.get("begin_date")
																	+ " is  equal to data from db " + strProdBeginDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Producer BeginDate value from Response" + prodCodePath.get("begin_date")
																	+ " is  not equal to data from db " + strProdBeginDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												
												
													String splitResponseProdEndDate = ((String) prodCodePath.get("end_date")).split("T")[0];
													if (splitResponseProdEndDate.equals(strProdEndDate.split(" ")[0])) {
														
														list.add(true);
														CustomReporting.logReport("", "",
																"Producer End_date value from Response" + prodCodePath.get("end_date")
																		+ " is equal to data from db " + strProdEndDate,
																StepStatus.SUCCESS, new String[] {}, startTime);
													}else {
														list.add(false);
														CustomReporting.logReport("", "",
																"Producer end_date value from Response" + prodCodePath.get("end_date")
																		+ " is  not equal to data from db " + strProdEndDate,
																StepStatus.FAILURE, new String[] {}, startTime);
													}
												
												if (producerCodeArr1.getJSONObject(j).has("business_segment_restrictions")) {

													JSONArray BusinessCodeArr1 = producerCodeArr1.getJSONObject(j)
															.getJSONArray("business_segment_restrictions");
													for (int k = 0; k < BusinessCodeArr1.length(); k++) {
														String BusinessCode = BusinessCodeArr1.get(k).toString();
														JsonPath BusinessCodePath = new JsonPath(BusinessCode);
														int count1 = 0;
														if (BusinessCodePath.get("code").equals(strBusinessSegmentCode)) {
															count1++;
															if (count1 != 0) {
															list.add(true);
															CustomReporting.logReport("", "",
																	"BusinessSegment_Code value from Response is equal to Database"
																			+ BusinessCodePath.get("code"),
																	StepStatus.SUCCESS, new String[] {}, startTime);
															
															String splitResponseBSBeginDate = ((String) BusinessCodePath.get("begin_date")).split("T")[0];
															if (splitResponseBSBeginDate.equals(strBSBeginDate.split(" ")[0])) {
																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segement BeginDate value from Response" + BusinessCodePath.get("begin_date")
																				+ " is  equal to data from db " + strBSBeginDate,
																		StepStatus.SUCCESS, new String[] {}, startTime);
															} else {
																list.add(false);
																CustomReporting.logReport("", "",
																		"Business_segement BeginDate value from Response" + BusinessCodePath.get("begin_date")
																				+ " is  not equal to data from db " + strBSBeginDate,
																		StepStatus.FAILURE, new String[] {}, startTime);
															}
															
															String splitResponseBSEndDate = ((String) BusinessCodePath.get("end_date")).split("T")[0];
															if (splitResponseBSEndDate.equals(strBSEndDate.split(" ")[0])) {
																
																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segement End_date value from Response" + BusinessCodePath.get("end_date")
																				+ " is equal to data from db " + strBSEndDate,
																		StepStatus.SUCCESS, new String[] {}, startTime);
															}else {
																list.add(false);
																CustomReporting.logReport("", "",
																		"Business_segement end_date value from Response" + BusinessCodePath.get("end_date")
																				+ " is  not equal to data from db " + strBSEndDate,
																		StepStatus.FAILURE, new String[] {}, startTime);
															}
															
															if (BusinessCodePath.get("description").equals(strBSDescription)) {
																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segement description value from Response" + BusinessCodePath.get("description")
																				+ " is equal to data from db " + strBSDescription,
																		StepStatus.SUCCESS, new String[] {}, startTime);

															} else {
																list.add(false);
																CustomReporting.logReport("", "",
																		"Business_segement description value from Response" + BusinessCodePath.get("description")
																				+ " is  not equal to data from db " + strBSDescription,
																		StepStatus.FAILURE, new String[] {}, startTime);
															}
															
														}
														else {
															list.add(false);
															CustomReporting.logReport("", "",
																	"BusinessSegment_Code value from Response is not equal to Database"
																			+ BusinessCodePath.get("code"),
																	"", StepStatus.FAILURE, new String[] {}, startTime, null);
														}}
														
											}}else {
												if (strBusinessSegmentCode != "null") {
													CustomReporting.logReport("", "",
															"producer code is not available in response and in Database  "
																	,
															StepStatus.SUCCESS, new String[] {}, startTime);
												}else {
													list.add(false);
													CustomReporting.logReport("", "",
															"BusinessSegment_Code value from Response is not equal to Database"
																	+ strBusinessSegmentCode,
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												
												}

											} 
										}
											else {
											list.add(false);
											CustomReporting.logReport("", "",
													"producer_codes value from Response is not equal to Database"
															+ prodCodePath.get("code"),
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}

									}
								} }else {
									if (strWinsAgent != "null") {
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}
								
								// To check for the business_address
								if(flag==true) {
								if (producerArr.getJSONObject(i).has("business_address")) {

									//String country = path.get("business_address.country");

									if (path.get("business_address.address_line_1").equals(strAddress_1)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_1 from Response"
														+ path.get("business_address.address_line_1")
														+ " is equal to the database value " + strAddress_1,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_1 from Response"
														+ path.get("business_address.address_line_1")
														+ " is  not equal to the database value " + strAddress_1,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}

									if (path.get("business_address.address_line_2").equals(strAddress_2)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_2 from Response"
														+ path.get("business_address.address_line_2")
														+ " is equal to the database value " + strAddress_2,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_2 from Response"
														+ path.get("business_address.address_line_2")
														+ " is  not equal to the database value " + strAddress_2,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (path.get("business_address.address_line_3").equals(strAddress_3)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_3 from Response"
														+ path.get("business_address.address_line_3")
														+ " is equal to the database value " + strAddress_3,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_3 from Response"
														+ path.get("business_address.address_line_3")
														+ " is  not equal to the database value " + strAddress_3,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (!producerArr.getJSONObject(i).has("business_address.address_line_4")) {
										if (strAddress_4 == null) {
											list.add(true);
											CustomReporting.logReport("", "",
													"address_line_4 from Response is null and is equal to the database value ",
													StepStatus.SUCCESS, new String[] {}, startTime);
										} else {
											list.add(false);
											CustomReporting.logReport("", "",
													"address_line_3 from Response is  not null and is not  equal to the database value ",
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_3 from Response is  not null and is not  equal to the database value ",
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (path.get("business_address.locality").equals(strLocality)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("business_address.locality")
														+ " is equal to the database value " + strLocality,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("business_address.locality")
														+ " is  not equal to the database value " + strLocality,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									if (path.get("business_address.region").equals(strRegion)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"region from Response" + path.get("business_address.region")
														+ " is equal to the database value " + strRegion,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"region from Response" + path.get("business_address.region")
														+ " is  not equal to the database value " + strRegion,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									if (path.get("business_address.postal_code").equals(strPostalCode)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("business_address.postal_code")
														+ " is equal to the database value " + strPostalCode,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("business_address.postal_code")
														+ " is  not equal to the database value " + strPostalCode,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									if (path.get("business_address.country").equals(strCountry)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"country from Response" + path.get("business_address.country")
														+ " is equal to the database value " + strCountry,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"country from Response" + path.get("business_address.country")
														+ " is  not equal to the database value " + strCountry,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									

								}}

								
								
							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}
			
		} catch (RuntimeException ex) {
			throw ex;
		}

	}
	 
	//API all Validate_ProducerAgents 
	public void Validate_ProducerAgents(String tcID, SoftAssert softAs) throws Exception {
		//33291
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String queryValue = data.getData("queryValue");

			String cType = data.getData("ContentType");

			String endpoint = url + "/" + queryValue + "/" + "agents";
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
			// Response response = cFunc.webServiceCall_1("", "", cType, url,
			// qParameter1,qParameter, qValue1,qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				System.out.println(code);
				
				//System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				JSONArray producerArr = new JSONArray(resbody);
				for (int i = 0; i < 2; i++) {
					System.out.println("i");
					LinkedHashMap<Integer, String[]> queryData = null;
					if (i == 0) {
						// Query to be sent to DB with Qvalue from DataSheet
						String query = "select distinct pfl.epickey, pp.PersonFirstName, pp.PersonMiddleName, pp.PersonLastName, pp.PersonNameSuffix, pp.NationalProducerId, pp.ValidFromdt, pp.ValidTodt, pp.EmailAddress,\r\n"
								+ " pa.AddressLine1, pa.AddressLine2, pa.AddressLine3, pa.CityName locality , s.State_Abbr_Cd Region, pa.ZipCd, c.Country_Cd\r\n"
								+ "from [ProducerMDMDB].[dbo].[PRDFirm] pf\r\n"
								+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Link] pfl on pfl.firmkey = pf.firmkey\r\n"
								+ "left join ProducerMDMDB.[dbo].[PRDFirm__Person] pfp on pfp.FirmKey = pfl.FirmKey\r\n"
								+ "left join ProducerMDMDB.[dbo].[PRDPerson] pp on pfp.PersonKey = pp.PersonKey\r\n"
								+ "left join ProducerMDMDB.dbo.PRDPerson__Address PPA on ppa.PersonKey = pp.PersonKey\r\n"
								+ "left join ProducerMDMDB.dbo.prdaddress pa on ppa.addresskey = pa.addresskey\r\n"
								+ "left join ProducerMDMDB.[dbo].[PRDAddressType] pat on ppa.AddressTypeKey = pat.AddressTypeKey\r\n"
								+ "left join ProducerMDMDB.[dbo].[DIMState] S on s.StateKey = pa.StateKey\r\n"
								+ "left join ProducerMDMDB.[dbo].[DimCountry] c on c.countrykey = pa.CountryKey\r\n"
								+ "left join ProducerMDMDB.dbo.[PRDPerson__License] L on l.PersonKey = pp.PersonKey\r\n"
								+ "where  pat.AddressTypeDesc='Business Location' and pfl.epickey = " + queryValue;

						queryData = new LinkedHashMap<Integer, String[]>();
						// queryData from the Database
						queryData = cFunc.getDataFromDB(query, 16);
					}
					if (i == 1) {
						// Query to be sent to DB with Qvalue from DataSheet
						String query = "select distinct l.State Region_Licence_state , l.LicenseTypeCode, l.LicenseType license_description, l.Number license_number, l.ValidFromdt effective_date, l.ValidTodt expiration_date\r\n"
								+ "from [ProducerMDMDB].[dbo].[PRDFirm] pf\r\n"
								+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Link] pfl on pfl.firmkey = pf.firmkey\r\n"
								+ "left join ProducerMDMDB.[dbo].[PRDFirm__Person] pfp on pfp.FirmKey = pfl.FirmKey\r\n"
								+ "left join ProducerMDMDB.[dbo].[PRDPerson] pp on pfp.PersonKey = pp.PersonKey\r\n"
								+ "left join ProducerMDMDB.dbo.PRDPerson__Address PPA on ppa.PersonKey = pp.PersonKey\r\n"
								+ "left join ProducerMDMDB.dbo.prdaddress pa on ppa.addresskey = pa.addresskey\r\n"
								+ "left join ProducerMDMDB.[dbo].[PRDAddressType] pat on ppa.AddressTypeKey = pat.AddressTypeKey\r\n"
								+ "left join ProducerMDMDB.[dbo].[DIMState] S on s.StateKey = pa.StateKey\r\n"
								+ "left join ProducerMDMDB.[dbo].[DimCountry] c on c.countrykey = pa.CountryKey\r\n"
								+ "left join ProducerMDMDB.dbo.[PRDPerson__License] L on l.PersonKey = pp.PersonKey\r\n"
								+ "where pfl.epickey =" + queryValue;

						queryData = new LinkedHashMap<Integer, String[]>();
						// queryData from the Database
						queryData = cFunc.getDataFromDB(query, 6);
					}

					// Initializing the variables for JSON response
					String strFirstName = "";
					String strMiddleName = "";
					String strLastName = "";
					String strsuffix = "";
					String strnational_producer_id = "";
					String strBeginDate = "";
					String strEndDate = "";
					String strEmail = "";

					String strAddress_1 = "";
					String strAddress_2 = "";
					String strAddress_3 = "";
					String strLocality = "";
					String strRegion = "";
					String strPostalCode = "";
					String strCountry = "";

					String strAgentRegion = "";
					String strLicense_type_code = "";
					String strLicense_description = "";
					String strLicense_number = "";
					String strEffective_date = "";
					String strExpiration_date = "";

					String splitResponseEndDate = "";

					// System.out.println("\n" +"producerArr"+producerArr.length());

					// comparing the JSON response with DB by iteration
					for (int j = 0; j < producerArr.length(); j++) {
						String prod = producerArr.get(j).toString();
						JsonPath path = new JsonPath(prod);

						// Getting single JSON response and iterating with DB data

						for (int x = 0; x < queryData.size(); x++) {

							// Initializing the variables for DB queryData
							if (i == 0) {
								strFirstName = (queryData.get(x))[1];
								strMiddleName = (queryData.get(x))[2];
								strLastName = (queryData.get(x))[3];
								strsuffix = (queryData.get(x))[4];
								strnational_producer_id = (queryData.get(x))[5];
								strBeginDate = (queryData.get(x))[6];
								strEndDate = (queryData.get(x))[7];
								strEmail = (queryData.get(x))[8];

								strAddress_1 = (queryData.get(x))[9];
								strAddress_2 = (queryData.get(x))[10];
								strAddress_3 = (queryData.get(x))[11];
								strLocality = (queryData.get(x))[12];
								strRegion = (queryData.get(x))[13];
								strPostalCode = (queryData.get(x))[14];
								strCountry = (queryData.get(x))[15];

								// comparing the name from response with DB

								if (path.get("first_name").equals(strFirstName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"first_name value from Response" + path.get("first_name")
													+ " is equal to data from db " + strFirstName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"first_name value from Response" + path.get("first_name")
													+ " is  not equal to data from db " + strFirstName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the middle_name from response with DB
								if (path.get("middle_name").equals(strMiddleName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"middle_name  value from Response" + path.get("middle_name")
													+ " is equal to data from db " + strMiddleName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"middle_name value from Response" + path.get("middle_name")
													+ " is  not equal to data from db " + strMiddleName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the Last_name from response with DB
								if (path.get("last_name").equals(strLastName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"last_name  value from Response" + path.get("last_name")
													+ " is equal to data from db " + strLastName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"last_name value from Response" + path.get("last_name")
													+ " is  not equal to data from db " + strLastName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the suffix from response with DB
								if (path.get("suffix").equals(strsuffix)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"suffix  value from Response" + path.get("suffix")
													+ " is equal to data from db " + strsuffix,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"suffix value from Response" + path.get("suffix")
													+ " is  not equal to data from db " + strsuffix,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the national_producer_id from response with DB
								if (path.get("national_producer_id").equals(strnational_producer_id)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"national_producer_id value from Response"
													+ path.get("national_producer_id") + " is equal to data from db "
													+ strnational_producer_id,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"national_producer_id value from Response"
													+ path.get("national_producer_id")
													+ " is  not equal to data from db " + strnational_producer_id,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								String splitstrBeginDate = ((String) strBeginDate).split(" ")[0];
								if (splitResponseBeginDate.equals(splitstrBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + splitstrBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + splitstrBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(j).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(" NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println("end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}

								if (path.get("email").equals(strEmail)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Email  value from Response" + path.get("email")
													+ " is equal to data from db " + strEmail,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Email value from Response" + path.get("email")
													+ " is  not equal to data from db " + strEmail,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// To check for the business_address
								if (producerArr.getJSONObject(i).has("business_address")) {

									if (path.get("business_address.address_line_1").equals(strAddress_1)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_1 from Response"
														+ path.get("business_address.address_line_1")
														+ " is equal to the database value " + strAddress_1,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_1 from Response"
														+ path.get("business_address.address_line_1")
														+ " is  not equal to the database value " + strAddress_1,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}

									if (path.get("business_address.address_line_2").equals(strAddress_2)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_2 from Response"
														+ path.get("business_address.address_line_2")
														+ " is equal to the database value " + strAddress_2,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_2 from Response"
														+ path.get("business_address.address_line_2")
														+ " is  not equal to the database value " + strAddress_2,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (path.get("business_address.address_line_3").equals(strAddress_3)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_3 from Response"
														+ path.get("business_address.address_line_3")
														+ " is equal to the database value " + strAddress_3,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_3 from Response"
														+ path.get("business_address.address_line_3")
														+ " is  not equal to the database value " + strAddress_3,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}

									if (path.get("business_address.locality").equals(strLocality)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("business_address.locality")
														+ " is equal to the database value " + strLocality,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("business_address.locality")
														+ " is  not equal to the database value " + strLocality,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									
									  if (path.get("business_address.region").equals(strRegion)) { list.add(true);
									  CustomReporting.logReport("", "", "region from Response" +
									  path.get("business_address.region") + " is equal to the database value " +
									  strRegion, StepStatus.SUCCESS, new String[] {}, startTime); } else {
									  list.add(false); CustomReporting.logReport("", "", "region from Response" +
									  path.get("business_address.region") + " is  not equal to the database value "
									  + strRegion, "", StepStatus.FAILURE, new String[] {}, startTime, null);
									  
									  }
									 
									if (path.get("business_address.postal_code").equals(strPostalCode)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("business_address.postal_code")
														+ " is equal to the database value " + strPostalCode,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("business_address.postal_code")
														+ " is  not equal to the database value " + strPostalCode,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									
									 if (path.get("business_address.country").equals(strCountry)) {
									 list.add(true); CustomReporting.logReport("", "", "country from Response" +
									 path.get("business_address.country") + " is equal to the database value " +
									 strCountry, StepStatus.SUCCESS, new String[] {}, startTime); } else {
									 list.add(false); CustomReporting.logReport("", "", "country from Response" +
									 path.get("business_address.country") +
									 " is  not equal to the database value " + strCountry, "", StepStatus.FAILURE,
									 new String[] {}, startTime, null);
									 
									 }
									

								}
							}
							if (i == 1) {

								strAgentRegion = (queryData.get(x))[0];
								strLicense_type_code = (queryData.get(x))[1];
								strLicense_description = (queryData.get(x))[2];
								strLicense_number = (queryData.get(x))[3];
								strEffective_date = (queryData.get(x))[4];
								strExpiration_date = (queryData.get(x))[5];
								if (producerArr.getJSONObject(j).has("licenses")) {

									JSONArray producerCodeArr = producerArr.getJSONObject(j).getJSONArray("licenses");

									for (int k = 0; k < producerCodeArr.length(); k++) {
										String prodCode = producerCodeArr.get(k).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										System.out.println();
										System.out.println();
										System.out.println();
										System.out.println();
										System.out.println();
										int count = 0;
										if ((prodCodePath.get("region").equals(strAgentRegion)) && (prodCodePath
												.get("license_type_code").equals(strLicense_type_code))) {

											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"Agents_Region  value from Response" + prodCodePath.get("region")
																+ " is equal to data from db " + strAgentRegion,
														StepStatus.SUCCESS, new String[] {}, startTime);
												
												list.add(true);
												CustomReporting.logReport("", "",
														"license_type_code  value from Response" + prodCodePath.get("license_type_code")
																+ " is equal to data from db " + strLicense_type_code,
														StepStatus.SUCCESS, new String[] {}, startTime);
												
												if (prodCodePath.get("license_description").equals(strLicense_description)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"License_description  value from Response" + prodCodePath.get("license_description")
																	+ " is equal to data from db " + strLicense_description,
															StepStatus.SUCCESS, new String[] {}, startTime);

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"License_description value from Response" + prodCodePath.get("license_description")
																	+ " is  not equal to data from db " + strLicense_description,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

												if (prodCodePath.get("license_number").equals(strLicense_number)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"license_number  value from Response" + prodCodePath.get("license_number")
																	+ " is equal to data from db " + strLicense_number,
															StepStatus.SUCCESS, new String[] {}, startTime);

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"license_number value from Response" + prodCodePath.get("license_number")
																	+ " is  not equal to data from db " + strLicense_number,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

											}

											// comparing the begin_date from response with DB
											
										}else {continue;}

									}
								}else {System.out.println("No licences");}
							}
						}

						// break;

					}
				}

				// To give the
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();
				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	
	//API all Validate_PatchProducers --not completed
	public void Validate_PatchProducers(String tcID, SoftAssert softAssert) throws Exception {
		 long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}

           
    			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

    		
    			
			String value = cFunc.getFromProperty("DS_CA_POS_01_queryValue");
			String path = data.getData("Payloadpath");
			String url = data.getData("EndPointUrl");
			
			String cType = data.getData("ContentType");
			String qParameter = data.getData("queryParameter");
			String FEIN = cFunc.getFromProperty("DS_CA_POS_01_FEIN");
			
			Response response = cFunc.webServiceCall(path, "", 
					cType, url, qParameter, value, Boolean.TRUE, "account", "primary_entity", "fein", FEIN );
			String resbody = response.asString();
			System.out.println(resbody);
			if (response != null) {
				int code = response.statusCode();
				String statusLine = response.statusLine();
				System.out.println(response.time());
				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// verify response code
				CustomReporting.logReport("====Update Account Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Update Account Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting.logReport("", "", "Unable to receive Update Account Response.Response received is : " + resbody+code+ response.statusLine(), "", StepStatus.FAILURE,
							new String[] {}, startTime, null);

				}
				// verify updated data to same account id
				String id = cFunc.getKeyValue(response, "account_id");
				if (value.equals(id)) {
					list.add(true);
					CustomReporting.logReport("", "", "Updated account Id is same as entered account id " + id,
							StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting.logReport("", "", "Updated account Id is not equal as entered account id " + id,
							StepStatus.FAILURE, new String[] {}, startTime);
				}

				// verify updated insured name
				Map<String, String> responseData = cFunc.getListOfKeyValue(resbody, "primary_entity");
				Map<String, String> payloadData = cFunc.getListOfKeyValueFromPayLoad(path, "account", "primary_entity");
				for (String key : payloadData.keySet()) {
					if (responseData.containsKey(key)) {
						if (key.equals("name")) {
							String reqInsuredName = payloadData.get(key);
							String resInsuredName = responseData.get(key);
							if (resInsuredName.equals(reqInsuredName)) {
								list.add(true);
								CustomReporting.logReport("", "",
										key.toUpperCase() + " value : " + reqInsuredName.toUpperCase()
												+ " from Request is Updated to " + key.toUpperCase()
												+ " value in Response : " + resInsuredName.toUpperCase(),
										StepStatus.SUCCESS, new String[] {}, startTime);
							} else {
								list.add(false);
								CustomReporting.logReport("", "",
										key.toUpperCase() + " value :" + reqInsuredName.toUpperCase()
												+ " from Request is not Updated to " + key.toUpperCase()
												+ " value in response " + resInsuredName.toUpperCase(),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
							}

						}
					}
				}
				// verify address details
				Map<String, String> addressData = cFunc.getListOfKeyValue(resbody, "account_address");
				Map<String, String> inputAddressData = cFunc.getListOfKeyValueFromPayLoad(path, "account",
						"account_address");
				String sheet = "UpdateAccount";
				for (String key : inputAddressData.keySet()) {
					if (addressData.containsKey(key)) {
						if (!(key.equals("address_line_1") || key.equals("address_line_2")
								|| key.equals("address_line_3") || key.equals("address_line_4"))) {
							String inputValue = inputAddressData.get(key);
							String responseValue = addressData.get(key);
							if (responseValue.equals(inputValue)) {
								list.add(true);
								CustomReporting.logReport("", "",
										key.toUpperCase() + " value : " + inputValue.toUpperCase()
												+ " from Request is Updated to " + key.toUpperCase()
												+ " value in Response : " + responseValue.toUpperCase(),
										StepStatus.SUCCESS, new String[] {}, startTime);
							} else {
								list.add(false);
								CustomReporting.logReport("", "",
										key.toUpperCase() + " value :" + inputValue.toUpperCase()
												+ " from Request is not Updated to " + key.toUpperCase()
												+ " value in Response: " + responseValue.toUpperCase(),
										"", StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						} else {
							if (key.equals("address_line_1")) {
								String inputValue = inputAddressData.get(key);
								String responseValue = addressData.get(key);
								if (responseValue.equals(inputValue)) {
									list.add(true);
									CustomReporting.logReport("", "",
											key.toUpperCase() + " value : " + inputValue.toUpperCase()
													+ " from Request is Updated to " + key.toUpperCase()
													+ " value in Response : " + responseValue.toUpperCase(),
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											key.toUpperCase() + " value :" + inputValue.toUpperCase()
													+ " from Request is not Updated to " + key.toUpperCase()
													+ " value in Response : " + responseValue.toUpperCase(),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							}
						}
					}
				}
				boolean stepResult = cFunc.allStepsResult(list);
				if (stepResult) {
					CustomReporting.logReport("", "", "Successfully Verified Account Details Updated ",
							StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification Unsuccessfull", "", StepStatus.FAILURE,
							new String[] {}, startTime, null);
					throw new RuntimeException();
				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Update account", "", StepStatus.FAILURE,
						new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}

	}
	
	public void Validate_ProducersBeginEndDate(String tcID, SoftAssert softAssert) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");

			String cType = data.getData("ContentType");

			String qValue = data.getData("queryValue");

			Response response = cFunc.webServiceCall("", "", cType, url, qParameter, qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			// to find the response
			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				String statusLine = response.statusLine();
				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				JSONArray producerArr = new JSONArray(resbody);
				for (int j = 0; j < 2; j++) {
					System.out.println("i");

					String query = "";
					if (j == 0) {

						// Query to be sent to DB with Qvalue from DataSheet
						query = " select  distinct c.EPICKey firm_id,adt.AddressTypeDesc , ad.AddressLine1 , ad.AddressLine2 , ad.AddressLine3 , \r\n"
								+ "null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\",pa.ValidFromdt,pa.ValidTodt\r\n"
								+ "from ProducerMDMDB.dbo.PRDFirm a\r\n"
								+ "left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
								+ "left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
								+ "left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n"
								+ "left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n"
								+ "left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n"
								+ "left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n"
								+ "left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n"
								+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
								+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
								+ "where ph.HierarchyLevelDesc = 'Producer Location' and adt.AddressTypeDesc = 'Business Location'";
					}
					if (j == 1) {
						query = " select  distinct c.EPICKey firm_id,adt.AddressTypeDesc , ad.AddressLine1 , ad.AddressLine2 , ad.AddressLine3 , \r\n"
								+ "null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\",pa.ValidFromdt,pa.ValidTodt\r\n"
								+ "from ProducerMDMDB.dbo.PRDFirm a\r\n"
								+ "left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
								+ "left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
								+ "left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n"
								+ "left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n"
								+ "left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n"
								+ "left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n"
								+ "left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n"
								+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
								+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
								+ "where ph.HierarchyLevelDesc = 'Producer Location' and adt.AddressTypeDesc = 'Mailing'";
					}
					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB(query, 12);

					String strFirmId = "";
					String strAddressType = "";
					String strAddress_1 = "";
					String strAddress_2 = "";
					String strAddress_3 = "";
					String strAddress_4 = "";
					String strLocality = "";
					String strPostalCode = "";
					String strRegion = "";
					String strCountry = "";
					String strBeginDate = "";
					String strEndDate = "";
					// System.out.println("\n" +"producerArr"+producerArr.length());

					// Iteration is done for the Response
					for (int i = 0; i < producerArr.length(); i++) {

						String prod = producerArr.get(i).toString();
						System.out.println("\n" + "prod" + prod);
						JsonPath path = new JsonPath(prod);

						int counter = 0;

						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strFirmId = (queryData.get(x))[0];
							strAddressType = (queryData.get(x))[1];
							strAddress_1 = (queryData.get(x))[2];
							strAddress_2 = (queryData.get(x))[3];
							strAddress_3 = (queryData.get(x))[4];
							strAddress_4 = (queryData.get(x))[5];
							strLocality = (queryData.get(x))[6];
							strPostalCode = (queryData.get(x))[7];
							strRegion = (queryData.get(x))[8];
							strCountry = (queryData.get(x))[9];
							strBeginDate = (queryData.get(x))[10];
							strEndDate = (queryData.get(x))[11];
							// System.out.println(strFirmId + strCompanyID);

							// To check for the firm_id
							int id = 0;
							if (path.get("firm_id").equals(strFirmId)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to  Firm Id in Database "
													+ path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);
									if (j == 0) {
										// To check for the business_address
										if (producerArr.getJSONObject(i).has("business_address")) {

											String country = path.get("business_address.country");

											if (path.get("business_address.address_line_1").equals(strAddress_1)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_1 from Response"
																+ path.get("business_address.address_line_1")
																+ " is equal to the database value " + strAddress_1,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "", "address_line_1 from Response"
														+ path.get("business_address.address_line_1")
														+ " is  not equal to the database value " + strAddress_1, "",
														StepStatus.FAILURE, new String[] {}, startTime, null);
											}

											if (path.get("business_address.address_line_2").equals(strAddress_2)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_2 from Response"
																+ path.get("business_address.address_line_2")
																+ " is equal to the database value " + strAddress_2,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "", "address_line_2 from Response"
														+ path.get("business_address.address_line_2")
														+ " is  not equal to the database value " + strAddress_2, "",
														StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											if (path.get("business_address.address_line_3").equals(strAddress_3)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_3 from Response"
																+ path.get("business_address.address_line_3")
																+ " is equal to the database value " + strAddress_3,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "", "address_line_3 from Response"
														+ path.get("business_address.address_line_3")
														+ " is  not equal to the database value " + strAddress_3, "",
														StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											if (!producerArr.getJSONObject(i).has("business_address.address_line_4")) {
												if (strAddress_4 == null) {
													list.add(true);
													CustomReporting.logReport("", "",
															"address_line_4 from Response is null and is equal to the database value ",
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"address_line_3 from Response is  not null and is not  equal to the database value ",
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"address_line_3 from Response is  not null and is not  equal to the database value ",
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											if (path.get("business_address.locality").equals(strLocality)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"locality from Response" + path.get("business_address.locality")
																+ " is equal to the database value " + strLocality,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"locality from Response" + path.get("business_address.locality")
																+ " is  not equal to the database value " + strLocality,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											if (path.get("business_address.region").equals(strRegion)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"region from Response" + path.get("business_address.region")
																+ " is equal to the database value " + strRegion,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"region from Response" + path.get("business_address.region")
																+ " is  not equal to the database value " + strRegion,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											if (path.get("business_address.postal_code").equals(strPostalCode)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"postal_code from Response"
																+ path.get("business_address.postal_code")
																+ " is equal to the database value " + strPostalCode,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "", "postal_code from Response"
														+ path.get("business_address.postal_code")
														+ " is  not equal to the database value " + strPostalCode, "",
														StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											if (path.get("business_address.country").equals(strCountry)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"country from Response" + path.get("business_address.country")
																+ " is equal to the database value " + strCountry,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"country from Response" + path.get("business_address.country")
																+ " is  not equal to the database value " + strCountry,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}

											// begin and end date
											String splitResponseBeginDate = ((String) path
													.get("business_address.begin_date")).split("T")[0];
											String splitstrBeginDate = ((String) strBeginDate).split(" ")[0];
											if (splitResponseBeginDate.equals(splitstrBeginDate)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"BeginDate value from Response"
																+ path.get("business_address.begin_date")
																+ " is  equal to data from db " + splitstrBeginDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"BeginDate value from Response"
																+ path.get("business_address.begin_date")
																+ " is  not equal to data from db " + splitstrBeginDate,
														StepStatus.FAILURE, new String[] {}, startTime);
											}

											// comparing the end_date from response with DB

											String splitResponseEndDate = ((String) path
													.get("business_address.end_date")).split("T")[0];
											String splitstrEndDate = ((String) strEndDate).split(" ")[0];
											if (splitResponseEndDate.equals(splitstrEndDate)) {
												// System.out.println("end date" + strEndDate);
												list.add(true);
												CustomReporting.logReport("", "",
														"End_date value from Response"
																+ path.get("business_address.end_date")
																+ " is equal to data from db " + strEndDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"End_date value from Response"
																+ path.get("business_address.end_date")
																+ " is  not equal to data from db " + strEndDate,
														StepStatus.FAILURE, new String[] {}, startTime);
											}

										}

									}
									if (j == 1) {

										if (producerArr.getJSONObject(i).has("mailing_address")) {
											String country = path.get("mailing_address.country");

											if (path.get("mailing_address.address_line_1").equals(strAddress_1)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_1 from Response"
																+ path.get("mailing_address.address_line_1")
																+ " is equal to the database value " + strAddress_1,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "", "address_line_1 from Response"
														+ path.get("mailing_address.address_line_1")
														+ " is  not equal to the database value " + strAddress_1, "",
														StepStatus.FAILURE, new String[] {}, startTime, null);
											}

											if (path.get("mailing_address.address_line_2").equals(strAddress_2)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_2 from Response"
																+ path.get("mailing_address.address_line_2")
																+ " is equal to the database value " + strAddress_2,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "", "address_line_2 from Response"
														+ path.get("mailing_address.address_line_2")
														+ " is  not equal to the database value " + strAddress_2, "",
														StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											if (path.get("mailing_address.address_line_3").equals(strAddress_3)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"address_line_3 from Response"
																+ path.get("mailing_address.address_line_3")
																+ " is equal to the database value " + strAddress_3,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "", "address_line_3 from Response"
														+ path.get("mailing_address.address_line_3")
														+ " is  not equal to the database value " + strAddress_3, "",
														StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											if (!producerArr.getJSONObject(i).has("mailing_address.address_line_4")) {
												if (strAddress_4 == null) {
													list.add(true);
													CustomReporting.logReport("", "",
															"address_line_4 from Response is null and is equal to the database value ",
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"address_line_3 from Response is  not null and is not  equal to the database value ",
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"address_line_3 from Response is  not null and is not  equal to the database value ",
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
											if (path.get("mailing_address.locality").equals(strLocality)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"locality from Response" + path.get("mailing_address.locality")
																+ " is equal to the database value " + strLocality,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"locality from Response" + path.get("mailing_address.locality")
																+ " is  not equal to the database value " + strLocality,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											if (path.get("mailing_address.region").equals(strRegion)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"region from Response" + path.get("mailing_address.region")
																+ " is equal to the database value " + strRegion,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"region from Response" + path.get("mailing_address.region")
																+ " is  not equal to the database value " + strRegion,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											if (path.get("mailing_address.postal_code").equals(strPostalCode)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"postal_code from Response"
																+ path.get("mailing_address.postal_code")
																+ " is equal to the database value " + strPostalCode,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "", "postal_code from Response"
														+ path.get("mailing_address.postal_code")
														+ " is  not equal to the database value " + strPostalCode, "",
														StepStatus.FAILURE, new String[] {}, startTime, null);

											}
											if (path.get("mailing_address.country").equals(strCountry)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"country from Response" + path.get("mailing_address.country")
																+ " is equal to the database value " + strCountry,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"country from Response" + path.get("mailing_address.country")
																+ " is  not equal to the database value " + strCountry,
														"", StepStatus.FAILURE, new String[] {}, startTime, null);

											}

											// begin and end date
											String splitResponseBeginDate = ((String) path
													.get("mailing_address.begin_date")).split("T")[0];
											String splitstrBeginDate = ((String) strBeginDate).split(" ")[0];
											if (splitResponseBeginDate.equals(splitstrBeginDate)) {
												list.add(true);
												CustomReporting.logReport("", "",
														"BeginDate value from Response"
																+ path.get("mailing_address.begin_date")
																+ " is  equal to data from db " + splitstrBeginDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"BeginDate value from Response"
																+ path.get("mailing_address.begin_date")
																+ " is  not equal to data from db " + splitstrBeginDate,
														StepStatus.FAILURE, new String[] {}, startTime);
											}

											// comparing the end_date from response with DB

											String splitResponseEndDate = ((String) path
													.get("mailing_address.end_date")).split("T")[0];
											String splitstrEndDate = ((String) strEndDate).split(" ")[0];
											if (splitResponseEndDate.equals(splitstrEndDate)) {
												// System.out.println("end date" + strEndDate);
												list.add(true);
												CustomReporting.logReport("", "",
														"End_date value from Response"
																+ path.get("mailing_address.end_date")
																+ " is equal to data from db " + strEndDate,
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"End_date value from Response"
																+ path.get("mailing_address.end_date")
																+ " is  not equal to data from db " + strEndDate,
														StepStatus.FAILURE, new String[] {}, startTime);
											}

										}
									}
								} else {
									counter++;
									if (counter >= queryData.size()) {
										list.add(false);
										CustomReporting.logReport("", "",
												"firm_id from Response is not equal to Firm Id in Database "
														+ path.get("firm_id"),
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									} else {
										continue;
									}
								}

							}
						}
					}
					// To give the
					boolean stepResult = cFunc.allStepsResult(list);

					if (stepResult) {
						CustomReporting.logReport("", "",
								"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
								new String[] {}, startTime);
					} else {
						CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE,
								new String[] {}, startTime, null);
						throw new RuntimeException();
					}

				}
			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	
	//API all Validate_OPCompany_AsOfDate EPRO-32713--not completed
	public void Validate_OPCompany_AsOfDate(String tcID, SoftAssert softAs) throws Exception { 
		//33518
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);
			String url = data.getData("EndPointUrl");
			String queryValue = data.getData("queryValue");
			String queryValue1 = data.getData("queryValue1");
			String queryParameter = data.getData("queryParameter");
			String cType = data.getData("ContentType");

			String endpoint = url + "/" + queryValue1 + "/" + "producers?as_of_date=" + queryValue;
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");

			if (response != null) {
				String  resbody= response.asString();
				//String resbody="["+abc+"]";
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				String query= "select distinct   prdl.firm_id,  prdl.name , prdl.ShortName, prdl.AbbreviatedName,prdl.IsActive ,prdl.FirmTaxId ,prdl.Firm_begin_date , prdl.Firm_End_date,prdl.firm_ValidFromdt,prdl.firm_ValidTodt,  \r\n" + 
						"prdl.Producer_code, prdl.producer_code_startdate , prdl.producer_code_enddate ,prdl.producer_code_IsActive,\r\n" + 
						"prdl.BusinessSegmentCd,  prdl.BusinessSegmentDesc , prdl.business_segment_validfromdt ,prdl.business_segment_ValidTodt from \r\n" + 
						"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
						"Case when a.isactive=0 \r\n" + 
						"then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
						"else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n" + 
						"from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n" + 
						"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
						"where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC ----- Operating company fields\\r\\n\" + \r\n" + 
						"left join \r\n" + 
						"(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  a.FirmName name , a.AbbreviatedName, a.ShortName,\r\n" + 
						"a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , \r\n" + 
						"Case when a.isactive=0\r\n" + 
						"then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
						"else  FirmTerminationDate  end Firm_End_date  , a.IsActive , a.ValidFromdt as firm_ValidFromdt,a.ValidTodt  firm_ValidTodt,\r\n" + 
						"b.Producer_code, b.producer_code_startdate , b.producer_code_enddate ,b.isActive producer_code_IsActive,\r\n" + 
						"b.BusinessSegmentKey,d.BusinessSegmentCd, d.BusinessSegmentDesc , b.business_segment_validfromdt  , b.business_segment_ValidTodt  \r\n" + 
						"\r\n" + 
						"from   ProducerMDMDB.dbo.PRDFirm a\r\n" + 
						"left join (select FirmKey,BusinessSegmentKey, convert(varchar(5),WinsAgent) Producer_code ,ValidFromdt business_segment_validfromdt,ValidTodt business_segment_ValidTodt,\r\n" + 
						"case when (sum(convert(int,isactive)) over ( partition by FirmKey,winsagent ) ) !=0 then 1 else 0 end as isActive,\r\n" + 
						"min (ValidFromdt) over ( partition by FirmKey,winsagent ) as producer_code_startdate,\r\n" + 
						"max (ValidTodt)  over ( partition by FirmKey,winsagent ) as producer_code_enddate \r\n" + 
						"from ProducerMDMDB.dbo.PRDFirm__BusinessSegment  ) b on a.FirmKey = b.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey \r\n" + 
						"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
						"left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n" + 
						") as PRDL  ---- Producer Location fields\r\n" + 
						"on PRDL.ParentFirmKey = opc.firmkey \r\n" + 
						"where   (prdl.firm_ValidFromdt <='2017-09-01 00:00:00.000' ) and \r\n" + 
						"(prdl.business_segment_validfromdt<='2017-09-01 00:00:00.000' and prdl.business_segment_ValidTodt>='2017-09-01 00:00:00.000' ) and \r\n" + 
						"opc.firm_id="+ queryValue1;
				
				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				//queryData = cFunc.getDataFromDB(query, 16);
				// queryData from the Database
			queryData = cFunc.getDataFromDB(query, 18);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strName = "";
				String strShortName = "";
				String strAbbreviatedName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				
				String strProdActive = "";
				String strProdBeginDate = "";
				String strProdEndDate = "";
				String strBusinessSegmentCode="";
				String strBSDescription = "";
				String strBSBeginDate = "";
				String strBSEndDate = "";
				
				
				int ResponseActive;
				int DataBaseActive;
				strFirmId = (queryData.get(0))[0];
				strName = (queryData.get(0))[1];
				strShortName = (queryData.get(0))[2];
				strAbbreviatedName= (queryData.get(0))[3];
				strActive = (queryData.get(0))[4];
				strTaxId = (queryData.get(0))[5];
				strBeginDate = (queryData.get(0))[6];
				strEndDate = (queryData.get(0))[7];
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[0];
						strName = (queryData.get(x))[1];
						strShortName = (queryData.get(x))[2];
						strAbbreviatedName= (queryData.get(x))[3];
						strActive = (queryData.get(x))[4];
						strTaxId = (queryData.get(x))[5];
						strBeginDate = (queryData.get(x))[6];
						strEndDate = (queryData.get(x))[7];
						
						
						strWinsAgent = (queryData.get(x))[8];
						strProdBeginDate = (queryData.get(x))[9];
						strProdEndDate = (queryData.get(x))[10];
						strProdActive=(queryData.get(x))[11];
						
						strBusinessSegmentCode= (queryData.get(x))[12];
						strBSBeginDate= (queryData.get(x))[13];
						strBSEndDate= (queryData.get(x))[14];
						strBSDescription= (queryData.get(x))[15];
						
						
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								// comparing the name from response with DB
								if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								
								if (path.get("abbreviated_name").equals(strAbbreviatedName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Abbreviated Name value from Response" + path.get("abbreviated_name")
													+ " is equal to data from db " + strAbbreviatedName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Abbreviated Name value from Response" + path.get("abbreviated_name")
													+ " is  not equal to data from db " + strAbbreviatedName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}
								// comparing the producer_codes from response with DB
								if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr1 = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									for (int j = 0; j < producerCodeArr1.length(); j++) {
										String prodCode = producerCodeArr1.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is equal to Database"
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);
												
												int ProdResponseActive = prodCodePath.getBoolean("active") ? 1 : 0;
												int ProdDataBaseActive = Integer.parseInt(strProdActive);
												if (ProdResponseActive == (ProdDataBaseActive)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"Producer Active value from Response" + prodCodePath.getBoolean("active")
																	+ " is  equal to data from db ",
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Producer Active value from Response" + prodCodePath.getBoolean("active")
																	+ " is  equal to data from db ",
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												
												String splitResponseProdBeginDate = ((String) prodCodePath.get("begin_date")).split("T")[0];
												if (splitResponseProdBeginDate.equals(strProdBeginDate.split(" ")[0])) {
													list.add(true);
													CustomReporting.logReport("", "",
															"Producer BeginDate value from Response" + prodCodePath.get("begin_date")
																	+ " is  equal to data from db " + strProdBeginDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Producer BeginDate value from Response" + prodCodePath.get("begin_date")
																	+ " is  not equal to data from db " + strProdBeginDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												
												
													String splitResponseProdEndDate = ((String) prodCodePath.get("end_date")).split("T")[0];
													if (splitResponseProdEndDate.equals(strProdEndDate.split(" ")[0])) {
														
														list.add(true);
														CustomReporting.logReport("", "",
																"Producer End_date value from Response" + prodCodePath.get("end_date")
																		+ " is equal to data from db " + strProdEndDate,
																StepStatus.SUCCESS, new String[] {}, startTime);
													}else {
														list.add(false);
														CustomReporting.logReport("", "",
																"Producer end_date value from Response" + prodCodePath.get("end_date")
																		+ " is  not equal to data from db " + strProdEndDate,
																StepStatus.FAILURE, new String[] {}, startTime);
													}
												
												if (producerCodeArr1.getJSONObject(j).has("business_segment_restrictions")) {

													JSONArray BusinessCodeArr1 = producerCodeArr1.getJSONObject(j)
															.getJSONArray("business_segment_restrictions");
													for (int k = 0; k < BusinessCodeArr1.length(); k++) {
														String BusinessCode = BusinessCodeArr1.get(k).toString();
														JsonPath BusinessCodePath = new JsonPath(BusinessCode);
														int count1 = 0;
														if (BusinessCodePath.get("code").equals(strBusinessSegmentCode)) {
															count1++;
															if (count1 != 0) {
															list.add(true);
															CustomReporting.logReport("", "",
																	"BusinessSegment_Code value from Response is equal to Database"
																			+ BusinessCodePath.get("code"),
																	StepStatus.SUCCESS, new String[] {}, startTime);
															
															String splitResponseBSBeginDate = ((String) BusinessCodePath.get("begin_date")).split("T")[0];
															if (splitResponseBSBeginDate.equals(strBSBeginDate.split(" ")[0])) {
																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segement BeginDate value from Response" + BusinessCodePath.get("begin_date")
																				+ " is  equal to data from db " + strBSBeginDate,
																		StepStatus.SUCCESS, new String[] {}, startTime);
															} else {
																list.add(false);
																CustomReporting.logReport("", "",
																		"Business_segement BeginDate value from Response" + BusinessCodePath.get("begin_date")
																				+ " is  not equal to data from db " + strBSBeginDate,
																		StepStatus.FAILURE, new String[] {}, startTime);
															}
															
															String splitResponseBSEndDate = ((String) BusinessCodePath.get("end_date")).split("T")[0];
															if (splitResponseBSEndDate.equals(strBSEndDate.split(" ")[0])) {
																
																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segement End_date value from Response" + BusinessCodePath.get("end_date")
																				+ " is equal to data from db " + strBSEndDate,
																		StepStatus.SUCCESS, new String[] {}, startTime);
															}else {
																list.add(false);
																CustomReporting.logReport("", "",
																		"Business_segement end_date value from Response" + BusinessCodePath.get("end_date")
																				+ " is  not equal to data from db " + strBSEndDate,
																		StepStatus.FAILURE, new String[] {}, startTime);
															}
															
															if (BusinessCodePath.get("description").equals(strBSDescription)) {
																list.add(true);
																CustomReporting.logReport("", "",
																		"Business_segement description value from Response" + BusinessCodePath.get("description")
																				+ " is equal to data from db " + strBSDescription,
																		StepStatus.SUCCESS, new String[] {}, startTime);

															} else {
																list.add(false);
																CustomReporting.logReport("", "",
																		"Business_segement description value from Response" + BusinessCodePath.get("description")
																				+ " is  not equal to data from db " + strBSDescription,
																		StepStatus.FAILURE, new String[] {}, startTime);
															}
															
														}
														else {
															list.add(false);
															CustomReporting.logReport("", "",
																	"BusinessSegment_Code value from Response is not equal to Database"
																			+ BusinessCodePath.get("code"),
																	"", StepStatus.FAILURE, new String[] {}, startTime, null);
														}}
														
											}}else {
												if (strBusinessSegmentCode != "null") {
													CustomReporting.logReport("", "",
															"producer code is not available in response and in Database  "
																	,
															StepStatus.SUCCESS, new String[] {}, startTime);
												}else {
													list.add(false);
													CustomReporting.logReport("", "",
															"BusinessSegment_Code value from Response is not equal to Database"
																	+ strBusinessSegmentCode,
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												
												}

											} 
										}
											else {
											list.add(false);
											CustomReporting.logReport("", "",
													"producer_codes value from Response is not equal to Database"
															+ prodCodePath.get("code"),
													"", StepStatus.FAILURE, new String[] {}, startTime, null);
										}

									}
								} }else {
									if (strWinsAgent != "null") {
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}
								
								// To check for the business_address
								

								
								
							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}
			
		} catch (RuntimeException ex) {
			throw ex;
		}

	}
	//API all Validate_ProducerMergerId EPRO-34998
	public void Validate_ProducerMergerId(String tcID, SoftAssert softAs) throws Exception{ 

		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			// Get the data from data sheet depends on the Environment --> QA or DEV
			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}

			// Get the URL and content_Type from the data_sheet
			String url = data.getData("EndPointUrl");
			String cType = data.getData("ContentType");
			String qParameter = data.getData("queryParameter");
			
			
			String qValue = data.getData("queryValue");
			// Getting the Response form the API by web_service_call
			
			Response response = cFunc.webServiceCall("", "", cType, url, qParameter, qValue, "get");
			System.out.println(response);
			// Checking the response whether its is null or not
			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				// converting the string to JSON array
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();
				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				CustomReporting.logReport("====Get Producers Service====" + url);

				// comparing the status code
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be send to Data_Base
				String query = " select distinct c.EPICKey firm_id , a.FirmName name, a.ShortName , a.AbbreviatedName, a.FirmTaxId , a.FirmEffectiveDate Firm_begin_date ,\r\n"
						+ "	 	 a.IsActive , b.WinsAgent,a.FirmTerminationDate EndDate	,c.MergeID   \r\n"
						+ "			from ProducerMDMDB.dbo.PRDFirm a \r\n"
						+ "			left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey \r\n"
						+ "			left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
						+ "			left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey \r\n"
						+ "			left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey  \r\n"
						+ "			left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey  \r\n"
						+ "			where ph.HierarchyLevelDesc = 'Producer Location'";
				// and c.EPICKey=" + qValue

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// Getting the data from the DB using queryData hash_mapping
				queryData = cFunc.getDataFromDB(query, 10);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strname = "";
				String strShortName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String strmergerid = "";
				String splitResponseEndDate = "";
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[0];
						strmergerid= (queryData.get(x))[9];
						/*strName = (queryData.get(x))[1];
						strShortName = (queryData.get(x))[2];
						strTaxId = (queryData.get(x))[4];
						strBeginDate = (queryData.get(x))[5];
						strActive = (queryData.get(x))[6];
						strWinsAgent = (queryData.get(x))[7];
						strEndDate = (queryData.get(x))[8];
						strmergerid= (queryData.get(x))[9];*/
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								if (( ((String) path.get("merger_id")).toUpperCase( )).equals(strmergerid)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"merger_id  value from Response" + path.get("merger_id")
													+ " is equal to data from db " + strmergerid,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"merger_id value from Response" + path.get("merger_id")
													+ " is  not equal to data from db " + strmergerid,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the name from response with DB
								/*if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}
								if (( ((String) path.get("merger_id")).toUpperCase( )).equals(strmergerid)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"merger_id  value from Response" + path.get("merger_id")
													+ " is equal to data from db " + strmergerid,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"merger_id value from Response" + path.get("merger_id")
													+ " is  not equal to data from db " + strmergerid,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the producer_codes from response with DB
								if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									for (int j = 0; j < producerCodeArr.length(); j++) {
										String prodCode = producerCodeArr.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"code value from Response is equal to Database"
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"code value from Response is not equal to Database"
																+ prodCodePath.get("code"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										}

									}
								} else {
									if (strWinsAgent != "null") {
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}*/

							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//
	public void Validate_ProducerCodeMergerId(String tcID, SoftAssert softAs) throws Exception{
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");
			String qParameter1 = data.getData("queryParameter1");
			String cType = data.getData("ContentType");
			String value1 = data.getData("queryValue1");
			String value = data.getData("queryValue");
			// getting the list values from DataSheet and iterating
			List<String> result = Arrays.asList(value.split(","));
			Object[] objArray = result.toArray();

			for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray[index]);
				String qValue = (String) objArray[index];
				String endpointurl=url+qValue;
				System.out.println(endpointurl);
				Response response = cFunc.webServiceCall("", "", cType, endpointurl, qParameter1, value1, "get");
				// String resbody = response.asString();
				// System.out.println(resbody);

				// to find the response
				if (response != null) {
					String abc = response.asString();
					String resbody="["+abc+"]";
					System.out.println(resbody);
					int code = response.statusCode();
					JSONArray producerArr = new JSONArray(resbody);

					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport(
								"", "", "Retrieve Service returned response successfully with code: " + code
										+ " and message : " + statusLine,
								StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting.logReport("", "",
								"Unable to Retrieve Account Response.Response received is: " + resbody + code
										+ response.statusLine(),
								"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}

					// Query to be sent to DB with Qvalue from DataSheet
					String query = "select  c.EPICKey firm_id ,c.MergeID 	 \r\n" 
					+ "	from ProducerMDMDB.dbo.PRDFirm a\r\n"
							+ "	left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
							+ "	left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
							+ "	left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n"
							+ "	left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
							+ "	left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
							+ "	where ph.HierarchyLevelDesc = 'Producer Location'and c.EPICKey=" + qValue;

					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB(query, 2);

					String strFirmId = "";
					String strmergerid = "";
					// System.out.println("\n" +"producerArr"+producerArr.length());

					// Iteration is done for the Response
					for (int i = 0; i < producerArr.length(); i++) {
						String prod = producerArr.get(i).toString();
						System.out.println("\n" + "prod" + prod);
						JsonPath path = new JsonPath(prod);
						String FirmId = path.get("firm_id").toString();
						System.out.println("FirmId" + FirmId);
						// String prodCodeArr = path.get("producer_codes").toString();
						int counter = 0;

						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strFirmId = (queryData.get(x))[0];
							strmergerid = (queryData.get(x))[1];
							//System.out.println(strFirmId + strWinsAgent);

							// To check for the firm_id
							int id = 0;
							if (path.get("firm_id").equals(strFirmId)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									// To check for the producer_codes
									if (( ((String) path.get("merger_id")).toUpperCase( )).equals(strmergerid)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"merger_id  value from Response" + path.get("merger_id")
														+ " is equal to data from db " + strmergerid,
												StepStatus.SUCCESS, new String[] {}, startTime);

									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"merger_id value from Response" + path.get("merger_id")
														+ " is  not equal to data from db " + strmergerid,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								} else {
									continue;
								}
							}

						}
						// To give the
						boolean stepResult = cFunc.allStepsResult(list);

						if (stepResult) {
							CustomReporting.logReport("", "",
									"Successfully Verified  Fields Values In Response With DB Columns ",
									StepStatus.SUCCESS, new String[] {}, startTime);
						} else {
							CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE,
									new String[] {}, startTime, null);
							throw new RuntimeException();
						}
					}
				}

				else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

			}
		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	
	public void Validate_CompanyMergerId(String tcID, SoftAssert softAs) throws Exception{
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);
			
			String qParameter = data.getData("queryParameter");
			
			String value = data.getData("queryValue");
			
			
			String url = data.getData("EndPointUrl");
			String cType = data.getData("ContentType");

			
			Response response = cFunc.webServiceCall("", "", cType, url, qParameter, value, "get");
			// Response response = cFunc.webServiceCall_1("", "", cType, url,
			// qParameter1,qParameter, qValue1,qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select distinct c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
						"Case when a.isactive=0 \r\n" + 
						"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
						"else  FirmTerminationDate  end Firm_End_date  , c.MergeID \r\n" + 
						"from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n" + 
						"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
						"where ph.HierarchyLevelDesc = 'Operating Company'";

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 5);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strName = "";
				String strBeginDate = "";
				String strEndDate = "";
				String strActive = "";
				String strmergerid="";
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();
					System.out.println(FirmId);

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[0];
						strName = (queryData.get(x))[1];
						
						strBeginDate = (queryData.get(x))[2];
						strEndDate = (queryData.get(x))[3];
						strmergerid= (queryData.get(x))[4];
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);
								
								if (( ((String) path.get("merger_id")).toUpperCase( )).equals(strmergerid)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"merger_id  value from Response" + path.get("merger_id")
													+ " is equal to data from db " + strmergerid,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"merger_id value from Response" + path.get("merger_id")
													+ " is  not equal to data from db " + strmergerid,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the name from response with DB
								if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								

								
								// String BooleanActive = String.valueOf(path.getBoolean("active"));
/*
								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}*/
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									String splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}
							
								
							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}

	}

	public void Validate_OperatingCompanyMergerId(String tcID, SoftAssert softAs) throws Exception{
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String queryValue = data.getData("queryValue");
			String queryValue1 = data.getData("queryValue1");
			String queryParameter = data.getData("queryParameter");
			String cType = data.getData("ContentType");
//http://enappwsd1:8001/api/v1/operating_companies/10437/producers?include=merger_id
			String endpoint = url + "/" + queryValue1 + "/" + "producers?include=" + queryValue;
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
			// Response response = cFunc.webServiceCall_1("", "", cType, url,
			// qParameter1,qParameter, qValue1,qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select distinct prdl.firm_id ,c.MergeID\r\n"
										
						+ "from \r\n" + "\r\n"
						+ "(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n"
						+ "Case when a.isactive=0 \r\n"
						+ "     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n"
						+ "else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n"
						+ "from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
						+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
						+ "where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC ----- Operating company fields\r\n"
						+ "\r\n" + "left join \r\n" + "\r\n"
						+ "(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  a.FirmName name ,a.AbbreviatedName, a.ShortName,\r\n"
						+ "a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , \r\n" + "Case when a.isactive=0 \r\n"
						+ "     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n"
						+ "else  FirmTerminationDate  end Firm_End_date  , a.IsActive , b.WinsAgent \"Producer_code\", b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" , b.IsActive producer_code_IsActive,\r\n"
						+ "b.BusinessSegmentKey,d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" , b.ValidTodt \"businesssegment_endtdate\" ,\r\n"
						+ "  pa.AddressKey ,pa.AddressTypeKey, ad.AddressLine1 ,adt.AddressTypeDesc , ad.AddressLine2 , ad.AddressLine3 , null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\",\r\n"
						+ "  comp.CompanyCd  ,comp.CompanyName, pc.ValidFromdt company_begin_date , pc.ValidTodt company_end_date, pc.IsActive Company_is_active,\r\n"
						+ "  a.DistributionChannelKey, pd.DistributionChannelCD,c.MergeID , pd.DistributionChannelDESC , a.ValidFromdt DistributionChannel_begin_date  , a.ValidTodt\r\n"
						+ "from   ProducerMDMDB.dbo.PRDFirm a\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
						+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__Company pc on pc.FirmKey = a.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDCompany comp on pc.CompanyKey=comp.CompanyKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n"
						+ ") as PRDL  ---- Producer Location fields\r\n" + "\r\n"
						+ "on PRDL.ParentFirmKey = opc.firmkey \r\n" + "where opc.firm_id= " + queryValue1;

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 2);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strmergerid = "";
				String strShortName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				String strCompanyID = "";
				String strDescription = "";
				String strCompanyEndDate = "";
				String strCompanyBeginDate = "";
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[0];
						strmergerid = (queryData.get(x))[1];
						/*strName = (queryData.get(x))[2];
						strShortName = (queryData.get(x))[3];
						strActive = (queryData.get(x))[4];
						strTaxId = (queryData.get(x))[5];
						strBeginDate = (queryData.get(x))[6];
						strEndDate = (queryData.get(x))[7];
						strWinsAgent = (queryData.get(x))[8];

						strCompanyID = (queryData.get(x))[10];
						strDescription = (queryData.get(x))[11];
						strCompanyEndDate = (queryData.get(x))[13];
						strCompanyBeginDate = (queryData.get(x))[12];*/
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								
								if (( ((String) path.get("merger_id")).toUpperCase( )).equals(strmergerid)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"merger_id  value from Response" + path.get("merger_id")
													+ " is equal to data from db " + strmergerid,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"merger_id value from Response" + path.get("merger_id")
													+ " is  not equal to data from db " + strmergerid,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the name from response with DB
								/*if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}*/
								// comparing the producer_codes from response with DB
								/*if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr1 = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									for (int j = 0; j < producerCodeArr1.length(); j++) {
										String prodCode = producerCodeArr1.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is equal to Database"
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is not equal to Database"
																+ prodCodePath.get("code"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										}

									}
								} else {
									if (strWinsAgent != "null") {
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}*/
								// companies
							/*	if (producerArr.getJSONObject(i).has("companies")) {

									JSONArray producerCodeArr = producerArr.getJSONObject(i).getJSONArray("companies");
									for (int j = 0; j < producerCodeArr.length(); j++) {
										String prodCode = producerCodeArr.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strCompanyID)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"company code value from Response is equal the database value "
																+ strCompanyID,
														StepStatus.SUCCESS, new String[] {}, startTime);

												if (prodCodePath.get("description").equals(strDescription)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"Name value from Response" + prodCodePath.get("description")
																	+ " is equal to the database value "
																	+ strDescription,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Name value from Response" + prodCodePath.get("description")
																	+ " is  not equal to the database value "
																	+ strDescription,
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}

												String splitResponseBeginDate1 = ((String) prodCodePath
														.get("begin_date")).split("T")[0];
												String splitstrBeginDate = ((String) (strCompanyBeginDate))
														.split(" ")[0];

												if (splitResponseBeginDate1.equals(splitstrBeginDate)) {
													list.add(true);
													CustomReporting.logReport("", "", "BeginDate value from Response"
															+ prodCodePath.get("begin_date")
															+ " is  equal to the database value " + splitstrBeginDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"BeginDate value from Response"
																	+ prodCodePath.get("begin_date")
																	+ " is  not equal to the database value "
																	+ splitstrBeginDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												String splitstrEndDate = ((String) (strCompanyEndDate)).split(" ")[0];
												String splitResponseEndDate1 = ((String) prodCodePath.get("end_date"))
														.split("T")[0];
												if (splitResponseEndDate1.equals(splitstrEndDate)) {

													list.add(true);
													CustomReporting.logReport("", "", "End_date value from Response"
															+ prodCodePath.get("end_date")
															+ " is equal to the database value " + splitstrEndDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "", "End_date value from Response"
															+ " is  not equal to the database value " + strEndDate,
															StepStatus.FAILURE, new String[] {}, startTime);

												}
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"company code value from Response is not equal to Database value "
																+ prodCodePath.get("companies"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										}

									}
								} else {
									if (strCompanyID != "null") {
										CustomReporting.logReport("", "",
												"company code is not available in response and in Database it is "
														+ strCompanyID,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"company code is not available in response and in Database it is "
														+ strCompanyID,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}
*/
							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}

	}
	
	public void Validate_OperatingCompanyFilterMergerId(String tcID, SoftAssert softAs) throws Exception{
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String Value = data.getData("queryValue");
			String queryValue1 = data.getData("queryValue1");
			
			String cType = data.getData("ContentType");
//http://enappwsd1:8001/api/v1/operating_companies/10437/producers?include=merger_id
			
			
			
			List<String> result = Arrays.asList(Value.split(","));
			Object[] objArray = result.toArray();
			
			
		for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray[index]);
				String queryValue = (String) objArray[index];
				String endpoint = url + "/" + queryValue1 + "/" + "producers?include=merger_id&merger_id=" + queryValue;
				System.out.println(endpoint);
				Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
				
		

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select distinct prdl.firm_id ,prdl.MergeID producerlocation_mergerid\r\n"
										
						+ "from \r\n" + "\r\n"
						+ "(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey, a.FirmKey,c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n"
						+ "Case when a.isactive=0 \r\n"
						+ "     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n"
						+ "else  FirmTerminationDate  end Firm_End_date  , a.IsActive \r\n"
						+ "from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
						+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
						+ "where ph.HierarchyLevelDesc = 'Operating Company' ) as OPC ----- Operating company fields\r\n"
						+ "\r\n" + "left join \r\n" + "\r\n"
						+ "(select ph.HierarchyLevelDesc ,ph.HierarchyLevelKey,pr.ParentFirmKey , a.FirmKey,c.EPICKey firm_id,  a.FirmName name ,a.AbbreviatedName, a.ShortName,\r\n"
						+ "a.FirmTaxId ,a.FirmEffectiveDate Firm_begin_date , \r\n" + "Case when a.isactive=0 \r\n"
						+ "     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n"
						+ "else  FirmTerminationDate  end Firm_End_date  , a.IsActive , b.WinsAgent \"Producer_code\", b.ValidFromDt \"producer_code_startdate\" , b.ValidTodt \"producer_code_endtdate\" , b.IsActive producer_code_IsActive,\r\n"
						+ "b.BusinessSegmentKey,d.BusinessSegmentCd, d.BusinessSegmentDesc , b.ValidFromDt \"businessSegment_startdate\" , b.ValidTodt \"businesssegment_endtdate\" ,\r\n"
						+ "  pa.AddressKey ,pa.AddressTypeKey, ad.AddressLine1 ,adt.AddressTypeDesc , ad.AddressLine2 , ad.AddressLine3 , null  \"AddressLine4\" ,ad.CityName  \"locality\",  ad.ZipCd \"PostalCode\", ds.State_Abbr_Cd \"region\" , dc.Country_Cd \"country_code\",\r\n"
						+ "  comp.CompanyCd  ,comp.CompanyName, pc.ValidFromdt company_begin_date , pc.ValidTodt company_end_date, pc.IsActive Company_is_active,\r\n"
						+ "  a.DistributionChannelKey, pd.DistributionChannelCD,c.MergeID , pd.DistributionChannelDESC , a.ValidFromdt DistributionChannel_begin_date  , a.ValidTodt\r\n"
						+ "from   ProducerMDMDB.dbo.PRDFirm a\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
						+ "left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDFirm__Address pa on pa.FirmKey = a.FirmKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddress  ad on ad.AddressKey = pa.AddressKey \r\n"
						+ "left join [ProducerMDMDB].[dbo].PRDAddressType adt on adt.AddressTypeKey = pa.AddressTypeKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DimCountry dc on dc.CountryKey = ad.CountryKey\r\n"
						+ "left join [ProducerMDMDB].[dbo].DIMState  ds on ds.StateKey = ad.StateKey \r\n"
						+ "left join ProducerMDMDB.dbo.PRDFirm__Company pc on pc.FirmKey = a.FirmKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDCompany comp on pc.CompanyKey=comp.CompanyKey\r\n"
						+ "left join ProducerMDMDB.dbo.PRDDistributionChannel pd on a.DistributionChannelKey =pd.DistributionChannelKey\r\n"
						+ ") as PRDL  ---- Producer Location fields\r\n" + "\r\n"
						+ "on PRDL.ParentFirmKey = opc.firmkey \r\n" + "where opc.firm_id= " + queryValue1 +"and prdl.MergeID=" + queryValue;

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 2);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strmergerid = "";
				String strShortName = "";
				String strActive = "";
				String strBeginDate = "";
				String strTaxId = "";
				String strWinsAgent = "";
				String strEndDate = "";
				String splitResponseEndDate = "";
				String strCompanyID = "";
				String strDescription = "";
				String strCompanyEndDate = "";
				String strCompanyBeginDate = "";
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[0];
						strmergerid = (queryData.get(x))[1];
						/*strName = (queryData.get(x))[2];
						strShortName = (queryData.get(x))[3];
						strActive = (queryData.get(x))[4];
						strTaxId = (queryData.get(x))[5];
						strBeginDate = (queryData.get(x))[6];
						strEndDate = (queryData.get(x))[7];
						strWinsAgent = (queryData.get(x))[8];

						strCompanyID = (queryData.get(x))[10];
						strDescription = (queryData.get(x))[11];
						strCompanyEndDate = (queryData.get(x))[13];
						strCompanyBeginDate = (queryData.get(x))[12];*/
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);

								
								if (( ((String) path.get("merger_id")).toUpperCase( )).equals(strmergerid)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"merger_id  value from Response" + path.get("merger_id")
													+ " is equal to data from db " + strmergerid,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"merger_id value from Response" + path.get("merger_id")
													+ " is  not equal to data from db " + strmergerid,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the name from response with DB
								/*if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the short_name from response with DB
								if (path.get("short_name").equals(strShortName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is equal to data from db " + strShortName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Short Name value from Response" + path.get("short_name")
													+ " is  not equal to data from db " + strShortName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the tax_id from response with DB
								if (path.get("tax_id").equals(strTaxId)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is equal to data from db " + strTaxId,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"TaxId value from Response" + path.get("tax_id")
													+ " is  not equal to data from db " + strTaxId,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// String BooleanActive = String.valueOf(path.getBoolean("active"));

								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}*/
								// comparing the producer_codes from response with DB
								/*if (producerArr.getJSONObject(i).has("producer_codes")) {

									JSONArray producerCodeArr1 = producerArr.getJSONObject(i)
											.getJSONArray("producer_codes");
									for (int j = 0; j < producerCodeArr1.length(); j++) {
										String prodCode = producerCodeArr1.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strWinsAgent)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is equal to Database"
																+ prodCodePath.get("code"),
														StepStatus.SUCCESS, new String[] {}, startTime);
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"producer_codes value from Response is not equal to Database"
																+ prodCodePath.get("code"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										}

									}
								} else {
									if (strWinsAgent != "null") {
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"producer code is not available in response and in Database it is "
														+ strWinsAgent,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}*/
								// companies
							/*	if (producerArr.getJSONObject(i).has("companies")) {

									JSONArray producerCodeArr = producerArr.getJSONObject(i).getJSONArray("companies");
									for (int j = 0; j < producerCodeArr.length(); j++) {
										String prodCode = producerCodeArr.get(j).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										int count = 0;
										if (prodCodePath.get("code").equals(strCompanyID)) {
											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"company code value from Response is equal the database value "
																+ strCompanyID,
														StepStatus.SUCCESS, new String[] {}, startTime);

												if (prodCodePath.get("description").equals(strDescription)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"Name value from Response" + prodCodePath.get("description")
																	+ " is equal to the database value "
																	+ strDescription,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"Name value from Response" + prodCodePath.get("description")
																	+ " is  not equal to the database value "
																	+ strDescription,
															"", StepStatus.FAILURE, new String[] {}, startTime, null);
												}

												String splitResponseBeginDate1 = ((String) prodCodePath
														.get("begin_date")).split("T")[0];
												String splitstrBeginDate = ((String) (strCompanyBeginDate))
														.split(" ")[0];

												if (splitResponseBeginDate1.equals(splitstrBeginDate)) {
													list.add(true);
													CustomReporting.logReport("", "", "BeginDate value from Response"
															+ prodCodePath.get("begin_date")
															+ " is  equal to the database value " + splitstrBeginDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"BeginDate value from Response"
																	+ prodCodePath.get("begin_date")
																	+ " is  not equal to the database value "
																	+ splitstrBeginDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												String splitstrEndDate = ((String) (strCompanyEndDate)).split(" ")[0];
												String splitResponseEndDate1 = ((String) prodCodePath.get("end_date"))
														.split("T")[0];
												if (splitResponseEndDate1.equals(splitstrEndDate)) {

													list.add(true);
													CustomReporting.logReport("", "", "End_date value from Response"
															+ prodCodePath.get("end_date")
															+ " is equal to the database value " + splitstrEndDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "", "End_date value from Response"
															+ " is  not equal to the database value " + strEndDate,
															StepStatus.FAILURE, new String[] {}, startTime);

												}
											} else {
												list.add(false);
												CustomReporting.logReport("", "",
														"company code value from Response is not equal to Database value "
																+ prodCodePath.get("companies"),
														"", StepStatus.FAILURE, new String[] {}, startTime, null);
											}
										}

									}
								} else {
									if (strCompanyID != "null") {
										CustomReporting.logReport("", "",
												"company code is not available in response and in Database it is "
														+ strCompanyID,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(true);
										CustomReporting.logReport("", "",
												"company code is not available in response and in Database it is "
														+ strCompanyID,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}

								}
*/
							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}
			
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}}

		} catch (RuntimeException ex) {
			throw ex;
		}

	}
	
	public void Validate_ProducerCodeFilterMergerId(String tcID, SoftAssert softAs) throws Exception{
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String qParameter = data.getData("queryParameter");
			
			String cType = data.getData("ContentType");
			
			String qvalue = data.getData("queryValue");
			// getting the list values from DataSheet and iterating
			
			List<String> result = Arrays.asList(qvalue.split(","));
			Object[] objArray = result.toArray();
			
			
		for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray[index]);
				String value = (String) objArray[index];
				
				Response response = cFunc.webServiceCall("", "", cType, url,qParameter, value, "get");
				// String resbody = response.asString();
				// System.out.println(resbody);

				// to find the response
				if (response != null) {
					String resbody = response.asString();
					
					System.out.println(resbody);
					int code = response.statusCode();
					JSONArray producerArr = new JSONArray(resbody);

					String statusLine = response.statusLine();

					List<Boolean> list = new ArrayList<Boolean>();
					Collections.fill(list, Boolean.TRUE);
					// To check the status code from response
					CustomReporting.logReport("====Get Producers Service====" + url);
					if (code == 200) {
						list.add(true);
						CustomReporting.logReport(
								"", "", "Retrieve Service returned response successfully with code: " + code
										+ " and message : " + statusLine,
								StepStatus.SUCCESS, new String[] {}, startTime);
					} else {
						list.add(false);
						CustomReporting.logReport("", "",
								"Unable to Retrieve Account Response.Response received is: " + resbody + code
										+ response.statusLine(),
								"", StepStatus.FAILURE, new String[] {}, startTime, null);
						throw new RuntimeException();
					}

					// Query to be sent to DB with Qvalue from DataSheet
					String query = "select  c.EPICKey firm_id ,c.MergeID 	 \r\n" 
					+ "	from ProducerMDMDB.dbo.PRDFirm a\r\n"
							+ "	left join ProducerMDMDB.dbo.PRDFirm__BusinessSegment b on a.FirmKey = b.FirmKey\r\n"
							+ "	left join ProducerMDMDB.dbo.PRDFirm__Link c on a.FirmKey = c.FirmKey\r\n"
							+ "	left join ProducerMDMDB.dbo.PRDBusinessSegment d on b.BusinessSegmentKey = d.BusinessSegmentKey\r\n"
							+ "	left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n"
							+ "	left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n"
							+ "	where ph.HierarchyLevelDesc = 'Producer Location'and c.MergeID ="+ value ;

					LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
					// queryData from the Database
					queryData = cFunc.getDataFromDB(query, 2);

					String strFirmId = "";
					String strmergerid = "";
					// System.out.println("\n" +"producerArr"+producerArr.length());

					// Iteration is done for the Response
					for (int i = 0; i < producerArr.length(); i++) {
						String prod = producerArr.get(i).toString();
						System.out.println("\n" + "prod" + prod);
						JsonPath path = new JsonPath(prod);
						String FirmId = path.get("firm_id").toString();
						System.out.println("FirmId" + FirmId);
						// String prodCodeArr = path.get("producer_codes").toString();
						int counter = 0;

						// Iteration is done for queryData
						for (int x = 0; x < queryData.size(); x++) {
							System.out.println(x);
							strFirmId = (queryData.get(x))[0];
							strmergerid = (queryData.get(x))[1];
							//System.out.println(strFirmId + strWinsAgent);

							// To check for the firm_id
							int id = 0;
							if (path.get("firm_id").equals(strFirmId)) {
								counter = 0;
								id++;
								if (id != 0) {
									list.add(true);
									CustomReporting.logReport("", "",
											"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
											StepStatus.SUCCESS, new String[] {}, startTime);

									// To check for the producer_codes
									if (( ((String) path.get("merger_id")).toUpperCase( )).equals(strmergerid)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"merger_id  value from Response" + path.get("merger_id")
														+ " is equal to data from db " + strmergerid,
												StepStatus.SUCCESS, new String[] {}, startTime);

									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"merger_id value from Response" + path.get("merger_id")
														+ " is  not equal to data from db " + strmergerid,
												StepStatus.FAILURE, new String[] {}, startTime);
									}

								}

								else {

									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
							} else {
								counter++;
								if (counter >= queryData.size()) {
									list.add(false);
									CustomReporting.logReport("", "",
											"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"),
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								} else {
									continue;
								}
							}

						}
						// To give the
						boolean stepResult = cFunc.allStepsResult(list);

						if (stepResult) {
							CustomReporting.logReport("", "",
									"Successfully Verified  Fields Values In Response With DB Columns ",
									StepStatus.SUCCESS, new String[] {}, startTime);
						} else {
							CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE,
									new String[] {}, startTime, null);
							throw new RuntimeException();
						}
					}
				}

				else {
					CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
							StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
		}
			
		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	
	public void Validate_CompanyFilterMergerId(String tcID, SoftAssert softAs) throws Exception{
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);
			
			String qParameter = data.getData("queryParameter");
						String qvalue = data.getData("queryValue");
String qParameter1 = data.getData("queryParameter1");
						String qvalue1 = data.getData("queryValue1");
			
			String url = data.getData("EndPointUrl");
			String cType = data.getData("ContentType");

			List<String> result = Arrays.asList(qvalue.split(","));
			Object[] objArray = result.toArray();
			
			
		for (int index = 0; index < objArray.length; index++) {
				System.out.println(objArray[index]);
				String value = (String) objArray[index];
			Response response = cFunc.webServiceCall("", "", cType, url, qParameter, value, "get");
			// Response response = cFunc.webServiceCall_1("", "", cType, url,
			// qParameter1,qParameter, qValue1,qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}

				// Query to be sent to DB with Qvalue from DataSheet
				String query = "select distinct c.EPICKey firm_id,  a.FirmName name, a.FirmEffectiveDate Firm_begin_date , \r\n" + 
						"Case when a.isactive=0 \r\n" + 
						"     then  COALESCE ( a.FirmTerminationDate , case when a.ValidTodt='9999-12-31 00:00:00.000' then a.LastModifiedTimestamp else a.ValidTodt end) \r\n" + 
						"else  FirmTerminationDate  end Firm_End_date  , c.MergeID \r\n" + 
						"from  ProducerMDMDB.dbo.PRDFirm__Link c \r\n" + 
						"left join ProducerMDMDB.dbo.PRDFirm a on a.FirmKey = c.FirmKey\r\n" + 
						"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on a.FirmKey = pr.FirmKey\r\n" + 
						"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
						"where ph.HierarchyLevelDesc = 'Operating Company' and c.MergeID=" + value;

				LinkedHashMap<Integer, String[]> queryData = new LinkedHashMap<Integer, String[]>();
				// queryData from the Database
				queryData = cFunc.getDataFromDB(query, 5);

				// Initializing the variables for JSON response
				String strFirmId = "";
				String strName = "";
				String strBeginDate = "";
				String strEndDate = "";
				String strActive = "";
				String strmergerid="";
				int ResponseActive;
				int DataBaseActive;
				// System.out.println("\n" +"producerArr"+producerArr.length());

				// comparing the JSON response with DB by iteration
				for (int i = 0; i < producerArr.length(); i++) {
					String prod = producerArr.get(i).toString();
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();
					System.out.println(FirmId);

					// Getting single JSON response and iterating with DB data
					int counter = 0;
					for (int x = 0; x < queryData.size(); x++) {
						// Initializing the variables for DB queryData
						strFirmId = (queryData.get(x))[0];
						strName = (queryData.get(x))[1];
						
						strBeginDate = (queryData.get(x))[2];
						strEndDate = (queryData.get(x))[3];
						strmergerid= (queryData.get(x))[4];
						// comparing the Firm Id from response with DB
						int id = 0;
						if (path.get("firm_id").equals(strFirmId)) {
							counter = 0;
							id++;
							if (id != 0) {
								list.add(true);
								CustomReporting.logReport("", "",
										"firm_id from Response is equal to Input Firm Id" + path.get("firm_id"),
										StepStatus.SUCCESS, new String[] {}, startTime);
								
								if (( ((String) path.get("merger_id")).toUpperCase( )).equals(strmergerid)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"merger_id  value from Response" + path.get("merger_id")
													+ " is equal to data from db " + strmergerid,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"merger_id value from Response" + path.get("merger_id")
													+ " is  not equal to data from db " + strmergerid,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the name from response with DB
								if (path.get("name").equals(strName)) {
									list.add(true);
									CustomReporting.logReport(
											"", "", "Name value from Response" + path.get("name")
													+ " is equal to data from db " + strName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Name value from Response" + path.get("name")
													+ " is  not equal to data from db " + strName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								

								
								// String BooleanActive = String.valueOf(path.getBoolean("active"));
/*
								// comparing the Active status from response with DB
								ResponseActive = path.getBoolean("active") ? 1 : 0;
								DataBaseActive = Integer.parseInt(strActive);
								if (ResponseActive == (DataBaseActive)) {

									list.add(true);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Active value from Response" + path.getBoolean("active")
													+ " is  equal to data from db ",
											StepStatus.FAILURE, new String[] {}, startTime);
								}*/
								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								if (splitResponseBeginDate.equals(strBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + strBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + strBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(i).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(FirmId + " NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									String splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println(FirmId + "end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}
							
								
							}

							else {

								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}

							// break;
						} else {
							counter++;
							if (counter >= queryData.size()) {
								list.add(false);
								CustomReporting.logReport("", "",
										"firm_id from Response is not equal to Input Firm Id" + path.get("firm_id"), "",
										StepStatus.FAILURE, new String[] {}, startTime, null);
							}
						}
					}
				}

				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();

				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}
		}
		} catch (RuntimeException ex) {
			throw ex;
		}

	}
//35001
	public void Validate_NationalProducerID(String tcID, SoftAssert softAs) throws Exception{
		//33291
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers_Stage", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

String url = data.getData("EndPointUrl");
			
			String qParameter = data.getData("queryParameter");
			String qvalue = data.getData("queryValue");
			String cType = data.getData("ContentType");
			String endpointurl=url+qvalue;

			Response response = cFunc.webServiceCall("", "", cType, endpointurl,"get");
			// Response response = cFunc.webServiceCall_1("", "", cType, url,
			if (response != null) {
				String abc = response.asString();
				String resbody="["+abc+"]";
				System.out.println(resbody);
				int code = response.statusCode();
				System.out.println(code);
				
				//System.out.println(producerArr);
				String statusLine = response.statusLine();


				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				JSONArray producerArr = new JSONArray(resbody);
				for (int i = 0; i < 2; i++) {
					System.out.println("i");
					LinkedHashMap<Integer, String[]> queryData = null;
					if (i == 0) {
						// Query to be sent to DB with Qvalue from DataSheet
						 String query = "select distinct  pf.FirmName ,pp.PersonFirstName, pp.PersonMiddleName, pp.PersonLastName, pp.PersonNameSuffix, \r\n" + 
									"pp.NationalProducerId, pp.ValidFromdt, pp.ValidTodt,\r\n" + 
									"pp.EmailAddress, pa.AddressLine1, pa.AddressLine2, pa.AddressLine3, pa.CityName locality , s.State_Abbr_Cd Region, pa.ZipCd, c.Country_Cd \r\n" + 
									"from [ProducerMDMDB].[dbo].[PRDFirm] pf\r\n" + 
									"left join [ProducerMDMDB].[dbo].[PRDFirm__Link] pfl on pfl.firmkey = pf.firmkey\r\n" + 
									"left join ProducerMDMDB.[dbo].[PRDFirm__Person] pfp on pfp.FirmKey = pfl.FirmKey\r\n" + 
									"left join ProducerMDMDB.[dbo].[PRDPerson] pp on pfp.PersonKey = pp.PersonKey\r\n" + 
									"left join ProducerMDMDB.dbo.PRDPerson__Address PPA on ppa.PersonKey = pp.PersonKey\r\n" + 
									"left join ProducerMDMDB.dbo.prdaddress pa on ppa.addresskey = pa.addresskey\r\n" + 
									"left join ProducerMDMDB.[dbo].[PRDAddressType] pat on ppa.AddressTypeKey = pat.AddressTypeKey\r\n" + 
									"left join ProducerMDMDB.[dbo].[DIMState] S on s.StateKey = pa.StateKey\r\n" + 
									"left join ProducerMDMDB.[dbo].[DimCountry] c on c.countrykey = pa.CountryKey\r\n" + 
									"left join ProducerMDMDB.dbo.[PRDPerson__License] L on l.PersonKey = pp.PersonKey\r\n" + 
									"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on pf.FirmKey = pr.FirmKey\r\n" + 
									"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
									"where ph.HierarchyLevelDesc = 'Producer Location' and pat.AddressTypeDesc ='Business Location'and pp.NationalProducerID = " + qvalue;
							 queryData = new LinkedHashMap<Integer, String[]>();
							// queryData from the Database
							queryData = cFunc.getDataFromDB(query, 16);
					}
					if (i == 1) {
						// Query to be sent to DB with Qvalue from DataSheet
						  String query = "select distinct  pf.FirmName ,pp.PersonFirstName, pp.PersonMiddleName, pp.PersonLastName, pp.PersonNameSuffix, \r\n" + 
									"pp.NationalProducerId, pp.ValidFromdt, pp.ValidTodt,\r\n" + 
									"pp.EmailAddress, pa.AddressLine1, pa.AddressLine2, pa.AddressLine3, pa.CityName locality , s.State_Abbr_Cd Region, pa.ZipCd, c.Country_Cd,\r\n" + 
									"l.PersonKey,l.State Region_Licence_state , l.LicenseTypeCode, l.LicenseType license_description, l.Number license_number, l.ValidFromdt effective_date, l.ValidTodt expiration_date\r\n" + 
									",pfl.epickey,ppa.addresskey,pat.AddressTypeDesc,l.PersonKey\r\n" + 
									"from [ProducerMDMDB].[dbo].[PRDFirm] pf\r\n" + 
									"left join [ProducerMDMDB].[dbo].[PRDFirm__Link] pfl on pfl.firmkey = pf.firmkey\r\n" + 
									"left join ProducerMDMDB.[dbo].[PRDFirm__Person] pfp on pfp.FirmKey = pfl.FirmKey\r\n" + 
									"left join ProducerMDMDB.[dbo].[PRDPerson] pp on pfp.PersonKey = pp.PersonKey\r\n" + 
									"left join ProducerMDMDB.dbo.PRDPerson__Address PPA on ppa.PersonKey = pp.PersonKey\r\n" + 
									"left join ProducerMDMDB.dbo.prdaddress pa on ppa.addresskey = pa.addresskey\r\n" + 
									"left join ProducerMDMDB.[dbo].[PRDAddressType] pat on ppa.AddressTypeKey = pat.AddressTypeKey\r\n" + 
									"left join ProducerMDMDB.[dbo].[DIMState] S on s.StateKey = pa.StateKey\r\n" + 
									"left join ProducerMDMDB.[dbo].[DimCountry] c on c.countrykey = pa.CountryKey\r\n" + 
									"left join ProducerMDMDB.dbo.[PRDPerson__License] L on l.PersonKey = pp.PersonKey\r\n" + 
									"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on pf.FirmKey = pr.FirmKey\r\n" + 
									"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
									"where ph.HierarchyLevelDesc = 'Producer Location' and pat.AddressTypeDesc ='Business Location'and pp.NationalProducerID = " + qvalue;
							 queryData = new LinkedHashMap<Integer, String[]>();
							// queryData from the Database
							queryData = cFunc.getDataFromDB(query, 27);
					}

					// Initializing the variables for JSON response
					String strFirstName = "";
					String strMiddleName = "";
					String strLastName = "";
					String strsuffix = "";
					String strnational_producer_id = "";
					String strBeginDate = "";
					String strEndDate = "";
					String strEmail = "";

					String strAddress_1 = "";
					String strAddress_2 = "";
					String strAddress_3 = "";
					String strLocality = "";
					String strRegion = "";
					String strPostalCode = "";
					String strCountry = "";

					String strAgentRegion = "";
					String strLicense_type_code = "";
					String strLicense_description = "";
					String strLicense_number = "";
					String strEffective_date = "";
					String strExpiration_date = "";

					String splitResponseEndDate = "";

					// System.out.println("\n" +"producerArr"+producerArr.length());

					// comparing the JSON response with DB by iteration
					for (int j = 0; j < producerArr.length(); j++) {
						String prod = producerArr.get(j).toString();
						JsonPath path = new JsonPath(prod);

						// Getting single JSON response and iterating with DB data

						for (int x = 0; x < queryData.size(); x++) {

							// Initializing the variables for DB queryData
							if (i == 0) {
								strFirstName = (queryData.get(x))[1];
								strMiddleName = (queryData.get(x))[2];
								strLastName = (queryData.get(x))[3];
								strsuffix = (queryData.get(x))[4];
								strnational_producer_id = (queryData.get(x))[5];
								strBeginDate = (queryData.get(x))[6];
								strEndDate = (queryData.get(x))[7];
								strEmail = (queryData.get(x))[8];

								strAddress_1 = (queryData.get(x))[9];
								strAddress_2 = (queryData.get(x))[10];
								strAddress_3 = (queryData.get(x))[11];
								strLocality = (queryData.get(x))[12];
								strRegion = (queryData.get(x))[13];
								strPostalCode = (queryData.get(x))[14];
								strCountry = (queryData.get(x))[15];

								// comparing the name from response with DB

								if (path.get("first_name").equals(strFirstName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"first_name value from Response" + path.get("first_name")
													+ " is equal to data from db " + strFirstName,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"first_name value from Response" + path.get("first_name")
													+ " is  not equal to data from db " + strFirstName,
											"", StepStatus.FAILURE, new String[] {}, startTime, null);
								}
								// comparing the middle_name from response with DB
								if (path.get("middle_name").equals(strMiddleName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"middle_name  value from Response" + path.get("middle_name")
													+ " is equal to data from db " + strMiddleName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"middle_name value from Response" + path.get("middle_name")
													+ " is  not equal to data from db " + strMiddleName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the Last_name from response with DB
								if (path.get("last_name").equals(strLastName)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"last_name  value from Response" + path.get("last_name")
													+ " is equal to data from db " + strLastName,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"last_name value from Response" + path.get("last_name")
													+ " is  not equal to data from db " + strLastName,
											StepStatus.FAILURE, new String[] {}, startTime);
								}
								// comparing the suffix from response with DB
								if (path.get("suffix").equals(strsuffix)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"suffix  value from Response" + path.get("suffix")
													+ " is equal to data from db " + strsuffix,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"suffix value from Response" + path.get("suffix")
													+ " is  not equal to data from db " + strsuffix,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the national_producer_id from response with DB
								if (path.get("national_producer_id").equals(strnational_producer_id)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"national_producer_id value from Response"
													+ path.get("national_producer_id") + " is equal to data from db "
													+ strnational_producer_id,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"national_producer_id value from Response"
													+ path.get("national_producer_id")
													+ " is  not equal to data from db " + strnational_producer_id,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the begin_date from response with DB
								String splitResponseBeginDate = ((String) path.get("begin_date")).split("T")[0];
								String splitstrBeginDate = ((String) strBeginDate).split(" ")[0];
								if (splitResponseBeginDate.equals(splitstrBeginDate)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  equal to data from db " + splitstrBeginDate,
											StepStatus.SUCCESS, new String[] {}, startTime);
								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"BeginDate value from Response" + path.get("begin_date")
													+ " is  not equal to data from db " + splitstrBeginDate,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// comparing the end_date from response with DB
								if (!producerArr.getJSONObject(j).has("end_date")) {
									if (strEndDate == null) {
										System.out.println(" NO end_date");
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value is not available in the Response and in DB value is "
														+ strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport(
												"", "", "End_date value from Response"
														+ " is  not equal to data from db " + strEndDate,
												StepStatus.FAILURE, new String[] {}, startTime);

									}
								} else {
									splitResponseEndDate = ((String) path.get("end_date")).split("T")[0];
									if (splitResponseEndDate.equals(strEndDate)) {
										System.out.println("end date" + strEndDate);
										list.add(true);
										CustomReporting.logReport("", "",
												"End_date value from Response" + path.get("end_date")
														+ " is equal to data from db " + strEndDate,
												StepStatus.SUCCESS, new String[] {}, startTime);
									}
								}

								if (path.get("email").equals(strEmail)) {
									list.add(true);
									CustomReporting.logReport("", "",
											"Email  value from Response" + path.get("email")
													+ " is equal to data from db " + strEmail,
											StepStatus.SUCCESS, new String[] {}, startTime);

								} else {
									list.add(false);
									CustomReporting.logReport("", "",
											"Email value from Response" + path.get("email")
													+ " is  not equal to data from db " + strEmail,
											StepStatus.FAILURE, new String[] {}, startTime);
								}

								// To check for the business_address
								if (producerArr.getJSONObject(i).has("business_address")) {

									if (path.get("business_address.address_line_1").equals(strAddress_1)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_1 from Response"
														+ path.get("business_address.address_line_1")
														+ " is equal to the database value " + strAddress_1,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_1 from Response"
														+ path.get("business_address.address_line_1")
														+ " is  not equal to the database value " + strAddress_1,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}

									if (path.get("business_address.address_line_2").equals(strAddress_2)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_2 from Response"
														+ path.get("business_address.address_line_2")
														+ " is equal to the database value " + strAddress_2,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_2 from Response"
														+ path.get("business_address.address_line_2")
														+ " is  not equal to the database value " + strAddress_2,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}
									if (path.get("business_address.address_line_3").equals(strAddress_3)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"address_line_3 from Response"
														+ path.get("business_address.address_line_3")
														+ " is equal to the database value " + strAddress_3,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"address_line_3 from Response"
														+ path.get("business_address.address_line_3")
														+ " is  not equal to the database value " + strAddress_3,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);
									}

									if (path.get("business_address.locality").equals(strLocality)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("business_address.locality")
														+ " is equal to the database value " + strLocality,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"locality from Response" + path.get("business_address.locality")
														+ " is  not equal to the database value " + strLocality,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									
									  if (path.get("business_address.region").equals(strRegion)) { list.add(true);
									  CustomReporting.logReport("", "", "region from Response" +
									  path.get("business_address.region") + " is equal to the database value " +
									  strRegion, StepStatus.SUCCESS, new String[] {}, startTime); } else {
									  list.add(false); CustomReporting.logReport("", "", "region from Response" +
									  path.get("business_address.region") + " is  not equal to the database value "
									  + strRegion, "", StepStatus.FAILURE, new String[] {}, startTime, null);
									  
									  }
									 
									if (path.get("business_address.postal_code").equals(strPostalCode)) {
										list.add(true);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("business_address.postal_code")
														+ " is equal to the database value " + strPostalCode,
												StepStatus.SUCCESS, new String[] {}, startTime);
									} else {
										list.add(false);
										CustomReporting.logReport("", "",
												"postal_code from Response" + path.get("business_address.postal_code")
														+ " is  not equal to the database value " + strPostalCode,
												"", StepStatus.FAILURE, new String[] {}, startTime, null);

									}
									
									 if (path.get("business_address.country").equals(strCountry)) {
									 list.add(true); CustomReporting.logReport("", "", "country from Response" +
									 path.get("business_address.country") + " is equal to the database value " +
									 strCountry, StepStatus.SUCCESS, new String[] {}, startTime); } else {
									 list.add(false); CustomReporting.logReport("", "", "country from Response" +
									 path.get("business_address.country") +
									 " is  not equal to the database value " + strCountry, "", StepStatus.FAILURE,
									 new String[] {}, startTime, null);
									 
									 }
									

								}
							}
							if (i == 1) {

								strAgentRegion = (queryData.get(x))[17];
								strLicense_type_code = (queryData.get(x))[18];
								strLicense_description = (queryData.get(x))[19];
								strLicense_number = (queryData.get(x))[20];
								strEffective_date = (queryData.get(x))[21];
								strExpiration_date = (queryData.get(x))[22];
								if (producerArr.getJSONObject(j).has("licenses")) {

									JSONArray producerCodeArr = producerArr.getJSONObject(j).getJSONArray("licenses");

									for (int k = 0; k < producerCodeArr.length(); k++) {
										String prodCode = producerCodeArr.get(k).toString();
										JsonPath prodCodePath = new JsonPath(prodCode);
										
										int count = 0;
										if ((prodCodePath.get("region").equals(strAgentRegion)) && (prodCodePath
												.get("license_type_code").equals(strLicense_type_code))) {

											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"Agents_Region  value from Response" + prodCodePath.get("region")
																+ " is equal to data from db " + strAgentRegion,
														StepStatus.SUCCESS, new String[] {}, startTime);
												
												list.add(true);
												CustomReporting.logReport("", "",
														"license_type_code  value from Response" + prodCodePath.get("license_type_code")
																+ " is equal to data from db " + strLicense_type_code,
														StepStatus.SUCCESS, new String[] {}, startTime);
												
												if (prodCodePath.get("license_description").equals(strLicense_description)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"License_description  value from Response" + prodCodePath.get("license_description")
																	+ " is equal to data from db " + strLicense_description,
															StepStatus.SUCCESS, new String[] {}, startTime);

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"License_description value from Response" + prodCodePath.get("license_description")
																	+ " is  not equal to data from db " + strLicense_description,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

												if (prodCodePath.get("license_number").equals(strLicense_number)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"license_number  value from Response" + prodCodePath.get("license_number")
																	+ " is equal to data from db " + strLicense_number,
															StepStatus.SUCCESS, new String[] {}, startTime);

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"license_number value from Response" + prodCodePath.get("license_number")
																	+ " is  not equal to data from db " + strLicense_number,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

											}

											// comparing the begin_date from response with DB
											
										}else {continue;}

									}
								}else {System.out.println("No licences");}
							}
						}

						// break;

					}
				}

				// To give the
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();
				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}
	}
//35002
	public void Validate_NationalProducerIDLicences(String tcID, SoftAssert softAs) throws Exception {
		// 33291
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers_Stage", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String queryValue = data.getData("queryValue");

			String cType = data.getData("ContentType");

			String endpoint = url + "/" + queryValue + "/" + "licenses";
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
			// Response response = cFunc.webServiceCall_1("", "", cType, url,
			// qParameter1,qParameter, qValue1,qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				System.out.println(code);

				// System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				JSONArray producerArr = new JSONArray(resbody);
				
								// Query to be sent to DB with Qvalue from DataSheet
						String query = "select distinct l.State Region_Licence_state , l.LicenseTypeCode, l.LicenseType license_description, l.Number license_number, l.ValidFromdt effective_date, l.ValidTodt expiration_date\r\n" + 
								"from [ProducerMDMDB].[dbo].[PRDFirm] pf\r\n" + 
								"left join [ProducerMDMDB].[dbo].[PRDFirm__Link] pfl on pfl.firmkey = pf.firmkey\r\n" + 
								"left join ProducerMDMDB.[dbo].[PRDFirm__Person] pfp on pfp.FirmKey = pfl.FirmKey\r\n" + 
								"left join ProducerMDMDB.[dbo].[PRDPerson] pp on pfp.PersonKey = pp.PersonKey\r\n" + 
								"left join ProducerMDMDB.dbo.PRDPerson__Address PPA on ppa.PersonKey = pp.PersonKey\r\n" + 
								"left join ProducerMDMDB.dbo.prdaddress pa on ppa.addresskey = pa.addresskey\r\n" + 
								"left join ProducerMDMDB.[dbo].[PRDAddressType] pat on ppa.AddressTypeKey = pat.AddressTypeKey\r\n" + 
								"left join ProducerMDMDB.[dbo].[DIMState] S on s.StateKey = pa.StateKey\r\n" + 
								"left join ProducerMDMDB.[dbo].[DimCountry] c on c.countrykey = pa.CountryKey\r\n" + 
								"left join ProducerMDMDB.dbo.[PRDPerson__License] L on l.PersonKey = pp.PersonKey\r\n" + 
								"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on pf.FirmKey = pr.FirmKey\r\n" + 
								"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
								"where ph.HierarchyLevelDesc = 'Producer Location' and pat.AddressTypeDesc ='Business Location'and pp.NationalProducerID = " + queryValue;

						LinkedHashMap<Integer, String[]> queryData= new LinkedHashMap<Integer, String[]>();
						// queryData from the Database
						queryData = cFunc.getDataFromDB(query, 6);
					

					// Initializing the variables for JSON response

					String strAgentRegion = "";
					String strLicense_type_code = "";
					String strLicense_description = "";
					String strLicense_number = "";
					String strEffective_date = "";
					String strExpiration_date = "";

					String splitResponseEndDate = "";

					// System.out.println("\n" +"producerArr"+producerArr.length());

					// comparing the JSON response with DB by iteration
					for (int j = 0; j < producerArr.length(); j++) {
						String prod = producerArr.get(j).toString();
						JsonPath prodCodePath = new JsonPath(prod);

						// Getting single JSON response and iterating with DB data

						for (int x = 0; x < queryData.size(); x++) {

							// Initializing the variables for DB queryData

							

								strAgentRegion = (queryData.get(x))[0];
								strLicense_type_code = (queryData.get(x))[1];
								strLicense_description = (queryData.get(x))[2];
								strLicense_number = (queryData.get(x))[3];
								strEffective_date = (queryData.get(x))[4];
								strExpiration_date = (queryData.get(x))[5];
								

									

										int count = 0;
										if ((prodCodePath.get("region").equals(strAgentRegion)) && (prodCodePath
												.get("license_type_code").equals(strLicense_type_code))  && (prodCodePath
														.get("license_number").equals(strLicense_number))) {

											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"Agents_Region  value from Response"
																+ prodCodePath.get("region")
																+ " is equal to data from db " + strAgentRegion,
														StepStatus.SUCCESS, new String[] {}, startTime);

												list.add(true);
												CustomReporting.logReport("", "",
														"license_type_code  value from Response"
																+ prodCodePath.get("license_type_code")
																+ " is equal to data from db " + strLicense_type_code,
														StepStatus.SUCCESS, new String[] {}, startTime);

												if (prodCodePath.get("license_description")
														.equals(strLicense_description)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"License_description  value from Response"
																	+ prodCodePath.get("license_description")
																	+ " is equal to data from db "
																	+ strLicense_description,
															StepStatus.SUCCESS, new String[] {}, startTime);

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"License_description value from Response"
																	+ prodCodePath.get("license_description")
																	+ " is  not equal to data from db "
																	+ strLicense_description,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

												if (prodCodePath.get("license_number").equals(strLicense_number)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"license_number  value from Response"
																	+ prodCodePath.get("license_number")
																	+ " is equal to data from db " + strLicense_number,
															StepStatus.SUCCESS, new String[] {}, startTime);

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"license_number value from Response"
																	+ prodCodePath.get("license_number")
																	+ " is  not equal to data from db "
																	+ strLicense_number,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												/*
												 * strEffective_date = (queryData.get(x))[4]; strExpiration_date =
												 * (queryData.get(x))[5];
												 */
												String splitResponseBeginDate = ((String) prodCodePath.get("effective_date"))
														.split("T")[0];
												String splitstrBeginDate = ((String) strEffective_date).split(" ")[0];
												if (splitResponseBeginDate.equals(splitstrBeginDate)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"effective_date value from Response"
																	+ prodCodePath.get("effective_date")
																	+ " is  equal to data from db " + splitstrBeginDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"effective_date value from Response"
																	+ prodCodePath.get("effective_date")
																	+ " is  not equal to data from db "
																	+ splitstrBeginDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												String splitResponseExpire = ((String) prodCodePath.get("expiration_date"))
														.split("T")[0];
												String splitstrEndDate = ((String) strExpiration_date).split(" ")[0];
												if (splitResponseExpire.equals(splitstrEndDate)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"expiration_date value from Response"
																	+ prodCodePath.get("expiration_date")
																	+ " is  equal to data from db " + splitstrEndDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"expiration_date value from Response"
																	+ prodCodePath.get("expiration_date")
																	+ " is  not equal to data from db "
																	+ splitstrEndDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

												// comparing the end_date from response with DB
												
											}

											// comparing the begin_date from response with DB

										} else {
											continue;
										}

									
							
							
						}

						// break;

					}
				

				// To give the
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();
				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) {
			throw ex;
		}
	}
	//35003
	public void Validate_FilterNationalProducerIDLicences(String tcID, SoftAssert softAs) throws Exception {
		// 33291
		long startTime = System.currentTimeMillis();
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null) {
				if (env.equalsIgnoreCase("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else {
				if (EnvironmentData.getData("Environment").equals("DEV")) {
					data = PageDataManager.instance().getPageData("GetProducers_Stage", tcID);
				} else {
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);

			String url = data.getData("EndPointUrl");
			String queryValue = data.getData("queryValue");

			String cType = data.getData("ContentType");
			String qParameter1 = data.getData("queryParameter1");
			String qvalue1 = data.getData("queryValue1");
			String endpoint = url + "/" + queryValue + "/" + "licenses?region=" + qvalue1;
			System.out.println(endpoint);
			
			Response response = cFunc.webServiceCall("", "", cType, endpoint, "get");
			// Response response = cFunc.webServiceCall_1("", "", cType, url,
			// qParameter1,qParameter, qValue1,qValue, "get");
			// String resbody = response.asString();
			// System.out.println(resbody);

			if (response != null) {
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				System.out.println(code);

				// System.out.println(producerArr);
				String statusLine = response.statusLine();

				List<Boolean> list = new ArrayList<Boolean>();
				Collections.fill(list, Boolean.TRUE);
				// To check the status code from response
				CustomReporting.logReport("====Get Producers Service====" + url);
				if (code == 200) {
					list.add(true);
					CustomReporting.logReport("", "", "Retrieve Service returned response successfully with code: "
							+ code + " and message : " + statusLine, StepStatus.SUCCESS, new String[] {}, startTime);
				} else {
					list.add(false);
					CustomReporting
							.logReport("", "",
									"Unable to Retrieve Account Response.Response received is: " + resbody + code
											+ response.statusLine(),
									"", StepStatus.FAILURE, new String[] {}, startTime, null);
					throw new RuntimeException();
				}
				JSONArray producerArr = new JSONArray(resbody);
				
								// Query to be sent to DB with Qvalue from DataSheet
						String query = "select distinct  l.State Region_Licence_state , l.LicenseTypeCode, l.LicenseType license_description, l.Number license_number, l.ValidFromdt effective_date, l.ValidTodt expiration_date\r\n" + 
								"from [ProducerMDMDB].[dbo].[PRDFirm] pf\r\n" + 
								"left join [ProducerMDMDB].[dbo].[PRDFirm__Link] pfl on pfl.firmkey = pf.firmkey\r\n" + 
								"left join ProducerMDMDB.[dbo].[PRDFirm__Person] pfp on pfp.FirmKey = pfl.FirmKey\r\n" + 
								"left join ProducerMDMDB.[dbo].[PRDPerson] pp on pfp.PersonKey = pp.PersonKey\r\n" + 
								"left join ProducerMDMDB.dbo.PRDPerson__Address PPA on ppa.PersonKey = pp.PersonKey\r\n" + 
								"left join ProducerMDMDB.dbo.prdaddress pa on ppa.addresskey = pa.addresskey\r\n" + 
								"left join ProducerMDMDB.[dbo].[PRDAddressType] pat on ppa.AddressTypeKey = pat.AddressTypeKey\r\n" + 
								"left join ProducerMDMDB.[dbo].[DIMState] S on s.StateKey = pa.StateKey\r\n" + 
								"left join ProducerMDMDB.[dbo].[DimCountry] c on c.countrykey = pa.CountryKey\r\n" + 
								"left join ProducerMDMDB.dbo.[PRDPerson__License] L on l.PersonKey = pp.PersonKey\r\n" + 
								"left join [ProducerMDMDB].[dbo].[PRDFirm__Relationships] PR on pf.FirmKey = pr.FirmKey\r\n" + 
								"left join  [ProducerMDMDB].[dbo].[PRDHierarchyLevel] ph on ph.HierarchyLevelKey = PR.HierarchyLevelKey\r\n" + 
								"where ph.HierarchyLevelDesc = 'Producer Location' and pat.AddressTypeDesc ='Business Location'and pp.NationalProducerID = "+queryValue +"  and l.State=" +"'" +qvalue1+"'";

						LinkedHashMap<Integer, String[]> queryData= new LinkedHashMap<Integer, String[]>();
						// queryData from the Database
						queryData = cFunc.getDataFromDB(query, 6);
					

					// Initializing the variables for JSON response

					String strAgentRegion = "";
					String strLicense_type_code = "";
					String strLicense_description = "";
					String strLicense_number = "";
					String strEffective_date = "";
					String strExpiration_date = "";

					String splitResponseEndDate = "";

					// System.out.println("\n" +"producerArr"+producerArr.length());

					// comparing the JSON response with DB by iteration
					for (int j = 0; j < producerArr.length(); j++) {
						String prod = producerArr.get(j).toString();
						JsonPath prodCodePath = new JsonPath(prod);

						// Getting single JSON response and iterating with DB data

						for (int x = 0; x < queryData.size(); x++) {

							// Initializing the variables for DB queryData

							

								strAgentRegion = (queryData.get(x))[0];
								strLicense_type_code = (queryData.get(x))[1];
								strLicense_description = (queryData.get(x))[2];
								strLicense_number = (queryData.get(x))[3];
								strEffective_date = (queryData.get(x))[4];
								strExpiration_date = (queryData.get(x))[5];
								

									

										int count = 0;
										if ((prodCodePath.get("region").equals(strAgentRegion)) && (prodCodePath
												.get("license_type_code").equals(strLicense_type_code))  && (prodCodePath
														.get("license_number").equals(strLicense_number))) {

											count++;
											if (count != 0) {
												list.add(true);
												CustomReporting.logReport("", "",
														"Agents_Region  value from Response"
																+ prodCodePath.get("region")
																+ " is equal to data from db " + strAgentRegion,
														StepStatus.SUCCESS, new String[] {}, startTime);

												list.add(true);
												CustomReporting.logReport("", "",
														"license_type_code  value from Response"
																+ prodCodePath.get("license_type_code")
																+ " is equal to data from db " + strLicense_type_code,
														StepStatus.SUCCESS, new String[] {}, startTime);

												if (prodCodePath.get("license_description")
														.equals(strLicense_description)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"License_description  value from Response"
																	+ prodCodePath.get("license_description")
																	+ " is equal to data from db "
																	+ strLicense_description,
															StepStatus.SUCCESS, new String[] {}, startTime);

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"License_description value from Response"
																	+ prodCodePath.get("license_description")
																	+ " is  not equal to data from db "
																	+ strLicense_description,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

												if (prodCodePath.get("license_number").equals(strLicense_number)) {

													list.add(true);
													CustomReporting.logReport("", "",
															"license_number  value from Response"
																	+ prodCodePath.get("license_number")
																	+ " is equal to data from db " + strLicense_number,
															StepStatus.SUCCESS, new String[] {}, startTime);

												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"license_number value from Response"
																	+ prodCodePath.get("license_number")
																	+ " is  not equal to data from db "
																	+ strLicense_number,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												/*
												 * strEffective_date = (queryData.get(x))[4]; strExpiration_date =
												 * (queryData.get(x))[5];
												 */
												String splitResponseBeginDate = ((String) prodCodePath.get("effective_date"))
														.split("T")[0];
												String splitstrBeginDate = ((String) strEffective_date).split(" ")[0];
												if (splitResponseBeginDate.equals(splitstrBeginDate)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"effective_date value from Response"
																	+ prodCodePath.get("effective_date")
																	+ " is  equal to data from db " + splitstrBeginDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"effective_date value from Response"
																	+ prodCodePath.get("effective_date")
																	+ " is  not equal to data from db "
																	+ splitstrBeginDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}
												String splitResponseExpire = ((String) prodCodePath.get("expiration_date"))
														.split("T")[0];
												String splitstrEndDate = ((String) strExpiration_date).split(" ")[0];
												if (splitResponseExpire.equals(splitstrEndDate)) {
													list.add(true);
													CustomReporting.logReport("", "",
															"expiration_date value from Response"
																	+ prodCodePath.get("expiration_date")
																	+ " is  equal to data from db " + splitstrEndDate,
															StepStatus.SUCCESS, new String[] {}, startTime);
												} else {
													list.add(false);
													CustomReporting.logReport("", "",
															"expiration_date value from Response"
																	+ prodCodePath.get("expiration_date")
																	+ " is  not equal to data from db "
																	+ splitstrEndDate,
															StepStatus.FAILURE, new String[] {}, startTime);
												}

												// comparing the end_date from response with DB
												
											}

											// comparing the begin_date from response with DB

										} else {
											continue;
										}

									
							
							
						}

						// break;

					}
				

				// To give the
				boolean stepResult = cFunc.allStepsResult(list);

				if (stepResult) {
					CustomReporting.logReport("", "",
							"Successfully Verified  Fields Values In Response With DB Columns ", StepStatus.SUCCESS,
							new String[] {}, startTime);
				} else {
					CustomReporting.logReport("", "", "Verification  failed", "", StepStatus.FAILURE, new String[] {},
							startTime, null);
					throw new RuntimeException();
				}

			} else {
				CustomReporting.logReport("", "", "Unable to recieve response for Get Producers", "",
						StepStatus.FAILURE, new String[] {}, startTime, null);
				throw new RuntimeException();
			}

		} catch (RuntimeException ex) 
		{
			
		}
	}
	@Override
	public String get_ProducerCount(String tcID, SoftAssert softAs,String strAuthCode) throws Exception 
	{
		CustomReporting.logReport("---------------API Validation-----------------");
		long startTime = System.currentTimeMillis();
		String apiPNCount = null;
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null)
			{
				if (env.equalsIgnoreCase("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
				else 
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else 
			{
				if (EnvironmentData.getData("Environment").equals("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} 
				else if(EnvironmentData.getData("Environment").equals("QA"))
				{
					data = PageDataManager.instance().getPageData("GetProducers_QA", tcID);
					
				}
				else
				{
					data = PageDataManager.instance().getPageData("GetProducers_Stage", tcID);
				}
				
			}
			// data = PageDataManager.instance().getPageData("GetProducers", tcID);
			String authCode ="";
			String url = data.getData("EndPointUrl");
			String cType = data.getData("ContentType");
			String qParameter = data.getData("queryParameter");
			String value = data.getData("queryValue");
			String qParameter1 = data.getData("queryParameter1");
			String value1 = data.getData("queryValue1");
			if (EnvironmentData.getData("Environment").equals("STAGE"))
			{
				authCode ="Bearer "+strAuthCode;
			} 
			else 
			{
				authCode = data.getData("AuthKey");
				 
			}
			String endpoint =url+"?"+qParameter+"="+value+"&"+qParameter1+"="+value1;
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", authCode, cType, endpoint, "Get");
			if (response != null)
			{
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				apiPNCount = response.getHeader("X-Total-Count")+"";
				GenericFunctions.instance()._addToGlobalVariableList("apiPNCount", apiPNCount);
				CustomReporting.logReport("Total No Of Records: "+apiPNCount);
			}
				
			}
			catch(Exception e)
			{
				throw new RuntimeException();
			}
		
		return apiPNCount;

}
	@Override
	public String get_ProducerOfficeCount(String tcID, SoftAssert softAs, String strAuthCode) throws Exception 
	{
		CustomReporting.logReport("---------------API Validation-----------------");
		long startTime = System.currentTimeMillis();
		String apiPOCount = null;
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null)
			{
				if (env.equalsIgnoreCase("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
				else 
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else 
			{
				if (EnvironmentData.getData("Environment").equals("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} 
				else if(EnvironmentData.getData("Environment").equals("QA"))
				{
					data = PageDataManager.instance().getPageData("GetProducers_QA", tcID);
					
				}
				else
				{
					data = PageDataManager.instance().getPageData("GetProducers_Stage", tcID);
				}
				
			}
			
			String authCode ="";
			String url = data.getData("EndPointUrl");
			String cType = data.getData("ContentType");
			String qParameter = data.getData("queryParameter");
			String value = data.getData("queryValue");
			String qParameter1 = data.getData("queryParameter1");
			String value1 = data.getData("queryValue1");
			String qParameter2 = data.getData("queryParameter2");
			String value2 = data.getData("queryValue2");
			String qParameter3 = data.getData("queryParameter3");
			String value3 = data.getData("queryValue3");	
			if (EnvironmentData.getData("Environment").equals("STAGE"))
			{
				authCode ="Bearer "+strAuthCode;
			} 
			else 
			{
				authCode = data.getData("AuthKey");
				 
			}
			String New_URL = url.replaceAll("operating_company_id", value);
			String endpoint =New_URL+"?"+qParameter1+"="+value1+"&"+qParameter2+"="+value2+"&"+qParameter3+"="+value3;
		//String endpoint =New_URL+"?"+P[1]+"="+v[1]+"&"+P[2]+"="+v[2]+"&"+P[3]+"="+v[3];
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", authCode, cType, endpoint, "Get");
			if (response != null)
			{
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				
				if(code!=404)
				{
					JSONArray producerArr = new JSONArray(resbody);
				//	Map<String, String> objectMap = JsonPath.read(json, "x[4].producer_codes[0].active");
					apiPOCount = producerArr.length()+"";
					GenericFunctions.instance()._addToGlobalVariableList("apiPOCount", apiPOCount);
					CustomReporting.logReport("Total No Of Records: "+apiPOCount);
				}
				else
				{
					apiPOCount="0";
				GenericFunctions.instance()._addToGlobalVariableList("apiPOCount", apiPOCount);
				CustomReporting.logReport("Total No Of Records: "+apiPOCount);
				}
			}
				
			}
			catch(Exception e)
			{
				throw new RuntimeException();
			}
		
		return apiPOCount;

		}
	@Override
	public String get_CompanyCount(String tcID, SoftAssert softAs, String strAuthCode) throws Exception
	{
		CustomReporting.logReport("---------------API Validation-----------------");
		long startTime = System.currentTimeMillis();
		String apiCCount = null;
		
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null)
			{
				if (env.equalsIgnoreCase("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
				else 
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else 
			{
				if (EnvironmentData.getData("Environment").equals("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} 
				else if(EnvironmentData.getData("Environment").equals("QA"))
				{
					data = PageDataManager.instance().getPageData("GetProducers_QA", tcID);
					
				}
				else
				{
					data = PageDataManager.instance().getPageData("GetProducers_Stage", tcID);
				}
				
			}
			
			String authCode ="";
			String url = data.getData("EndPointUrl");
			String cType = data.getData("ContentType");
			String qParameter = data.getData("queryParameter");
			String value = data.getData("queryValue");
			String qParameter1 = data.getData("queryParameter1");
			String value1 = data.getData("queryValue1");
			String qParameter2 = data.getData("queryParameter2");
			String value2 = data.getData("queryValue2");
			String qParameter3 = data.getData("queryParameter3");
			String value3 = data.getData("queryValue3");	
			String qParameter4= data.getData("queryParameter4");
			String value4 = data.getData("queryValue4");	
			if (EnvironmentData.getData("Environment").equals("STAGE"))
			{
				authCode ="Bearer "+strAuthCode;
			} 
			else 
			{
				authCode = data.getData("AuthKey");
				 
			}
			String New_URL = url.replaceAll("operating_company_id", value);
			String endpoint =New_URL+"?"+qParameter1+"="+value1+"&"+qParameter2+"="+value2+"&"+qParameter3+"="+value3;
		//String endpoint =New_URL+"?"+P[1]+"="+v[1]+"&"+P[2]+"="+v[2]+"&"+P[3]+"="+v[3];
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", authCode, cType, endpoint, "Get");
			if (response != null)
			{
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				
				for (int i = 0; i < producerArr.length(); i++)
				{
					String prod = producerArr.get(i).toString();
				//	System.out.println("\n" + "prod" + prod);
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();
					if(FirmId.equals(value4)) 
					{
						String companies = path.getString("companies.description").toString();
						String noofC [] = companies.split(",");
						apiCCount = noofC.length+"";
						for(int j =0;j<noofC.length;j++)
						{
						//noofC[j].replaceAll("[", "");
							CustomReporting.logReport("Name of companies : "+noofC[j]);
						}
						CustomReporting.logReport("Number of companies : "+apiCCount);
						
					}
					
				}
				
				
				
			
			}}
			catch(Exception e)
			{
				throw new RuntimeException();
			}
		
		return apiCCount;

	}
	@Override
	public String get_DistributionChannel(String tcID, SoftAssert softAs, String strAuthCode) throws Exception 
	{
		
		CustomReporting.logReport("---------------API Validation-----------------");
		long startTime = System.currentTimeMillis();
		String distribution = null;
		String l=null;
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null)
			{
				if (env.equalsIgnoreCase("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
				else 
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else 
			{
				if (EnvironmentData.getData("Environment").equals("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} 
				else if(EnvironmentData.getData("Environment").equals("QA"))
				{
					data = PageDataManager.instance().getPageData("GetProducers_QA", tcID);
					
				}
				else
				{
					data = PageDataManager.instance().getPageData("GetProducers_Stage", tcID);
				}
				
			}
			
			String authCode ="";
			String url = data.getData("EndPointUrl");
			String cType = data.getData("ContentType");
			String qParameter = data.getData("queryParameter");
			String value = data.getData("queryValue");
			String qParameter1 = data.getData("queryParameter1");
			String value1 = data.getData("queryValue1");
			String qParameter2 = data.getData("queryParameter2");
			String value2 = data.getData("queryValue2");
			String qParameter3 = data.getData("queryParameter3");
			String value3 = data.getData("queryValue3");	
			String qParameter4= data.getData("queryParameter4");
			String value4 = data.getData("queryValue4");	
			if (EnvironmentData.getData("Environment").equals("STAGE"))
			{
				authCode ="Bearer "+strAuthCode;
			} 
			else 
			{
				authCode = data.getData("AuthKey");
				 
			}
			String New_URL = url.replaceAll("operating_company_id", value);
			String endpoint =New_URL+"?"+qParameter1+"="+value1+"&"+qParameter2+"="+value2+"&"+qParameter3+"="+value3;
		//String endpoint =New_URL+"?"+P[1]+"="+v[1]+"&"+P[2]+"="+v[2]+"&"+P[3]+"="+v[3];
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", authCode, cType, endpoint, "Get");
			if (response != null)
			{
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				
				for (int i = 0; i < producerArr.length(); i++)
				{
					String prod = producerArr.get(i).toString();
				//	System.out.println("\n" + "prod" + prod);
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();
					if(FirmId.equals(value4)) 
					{
						 distribution = path.getString("distribution_channel.description").toString();
						String noofC [] = distribution.split(",");
						 l = noofC.length+"";
						CustomReporting.logReport("Number of distribution  : "+noofC.length);
						CustomReporting.logReport("Name of distribution channel : "+noofC[0]);
					}
					
				}
				
				
				
			
			}}
			catch(Exception e)
			{
				throw new RuntimeException();
			}
		
		return distribution;
	}
	@Override
	public String get_ProducerCode(String tcID, SoftAssert softAs, String strAuthCode) throws Exception 
	{
		CustomReporting.logReport("---------------API Validation-----------------");
		long startTime = System.currentTimeMillis();
		String apiCCount = null;
		String Desc=null;
		
		try {
			PageData EnvironmentData = PageDataManager.instance().getPageData("Config", tcID);

			String env = System.getenv("PARAMETER_ENV");
			if (env != null)
			{
				if (env.equalsIgnoreCase("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
				else 
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				}
			} else 
			{
				if (EnvironmentData.getData("Environment").equals("DEV"))
				{
					data = PageDataManager.instance().getPageData("GetProducers", tcID);
				} 
				else if(EnvironmentData.getData("Environment").equals("QA"))
				{
					data = PageDataManager.instance().getPageData("GetProducers_QA", tcID);
					
				}
				else
				{
					data = PageDataManager.instance().getPageData("GetProducers_Stage", tcID);
				}
				
			}
			
			String authCode ="";
			String url = data.getData("EndPointUrl");
			String cType = data.getData("ContentType");
			String qParameter = data.getData("queryParameter");
			String value = data.getData("queryValue");
			String qParameter1 = data.getData("queryParameter1");
			String value1 = data.getData("queryValue1");
			String qParameter2 = data.getData("queryParameter2");
			String value2 = data.getData("queryValue2");
			String qParameter3 = data.getData("queryParameter3");
			String value3 = data.getData("queryValue3");	
			String qParameter4= data.getData("queryParameter4");
			String value4 = data.getData("queryValue4");	
			if (EnvironmentData.getData("Environment").equals("STAGE"))
			{
				authCode ="Bearer "+strAuthCode;
			} 
			else 
			{
				authCode = data.getData("AuthKey");
				 
			}
			String New_URL = url.replaceAll("operating_company_id", value);
			String endpoint =New_URL+"?"+qParameter1+"="+value1+"&"+qParameter2+"="+value2+"&"+qParameter3+"="+value3;
		//String endpoint =New_URL+"?"+P[1]+"="+v[1]+"&"+P[2]+"="+v[2]+"&"+P[3]+"="+v[3];
			System.out.println(endpoint);
			Response response = cFunc.webServiceCall("", authCode, cType, endpoint, "Get");
			if (response != null)
			{
				String resbody = response.asString();
				System.out.println(resbody);
				int code = response.statusCode();
				JSONArray producerArr = new JSONArray(resbody);
				
				for (int i = 0; i < producerArr.length(); i++)
				{
					String prod = producerArr.get(i).toString();
				//	System.out.println("\n" + "prod" + prod);
					JsonPath path = new JsonPath(prod);
					String FirmId = path.get("firm_id").toString();
					if(FirmId.equals(value4)) 
					{
						String companies = path.getString("producer_codes.business_segment_restrictions.description").toString();
						String noofC [] = companies.split(",");
						Desc = noofC[0];
						Desc = Desc.substring(2, Desc.length()-1);
						System.out.println(Desc);
						Desc.replaceAll("]", "");
						CustomReporting.logReport("Name of Business Segment : "+Desc);
					}
					
				}
					}}
			catch(Exception e)
			{
				throw new RuntimeException();
			}
		
		return Desc;
	}
	}