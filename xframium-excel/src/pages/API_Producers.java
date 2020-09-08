package pages;

import org.testng.asserts.SoftAssert;
import org.xframium.page.Page;
import org.xframium.page.Page.ScreenShot;
import org.xframium.page.Page.TimeMethod;

public interface API_Producers extends Page{
	@TimeMethod
	@ScreenShot
	public void validateProducerCode(String tcID, SoftAssert softAs , String strAuthCode) throws Exception;
	public void validateAllProducers(String tcID, SoftAssert softAs, String strAuthCode) throws Exception;
	public void validateAllActiveProducers(String tcID, SoftAssert softAs,String strAuthCode) throws Exception;
	public void validateCompanyDetails(String tcID, SoftAssert softAs) throws Exception;
	public void validateBusinessAddress(String tcID, SoftAssert softAs) throws Exception;
	public void validateMailingAddress(String tcID, SoftAssert softAs) throws Exception;
	public void validateDistributionChannel(String tcID, SoftAssert softAs) throws Exception;
	public void validateLastModifiedDate(String tcID, SoftAssert softAs) throws Exception;
	public void validateAllAddressChannelModifiedDate(String tcID, SoftAssert softAs) throws Exception;
	public void MRDH_validateDistributionChannel(String tcID, SoftAssert softAs) throws Exception;
	public void MRDH_ValidateBusinessSegment(String tcID, SoftAssert softAs) throws Exception;
	public void MRDH_ValidateCompany(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_OPCompany_Companies(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_AllOperatingCompanies(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_OPCompany_BusinessAddress(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_OPCompany_DistributionChannel(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_Company_BusinessSegment(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_OPCompany_Producers(String tcID, SoftAssert softAs) throws Exception;
	//EPRO-33290
	public void Validate_Producer_code(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_OPCompany_BusinessSegment(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_ProducerAgents(String tcID, SoftAssert softAs) throws Exception;
	
	public void Validate_PatchProducers(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_ProducersBeginEndDate(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_OPCompany_AsOfDate(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_ProducerMergerId(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_ProducerCodeMergerId(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_CompanyMergerId(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_OperatingCompanyMergerId(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_OperatingCompanyFilterMergerId(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_ProducerCodeFilterMergerId(String tcID, SoftAssert softAs) throws Exception;
	
	public void Validate_CompanyFilterMergerId(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_NationalProducerID(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_NationalProducerIDLicences(String tcID, SoftAssert softAs) throws Exception;
	public void Validate_FilterNationalProducerIDLicences(String tcID, SoftAssert softAs) throws Exception;
	public String get_ProducerCount(String tcID, SoftAssert softAs,String strAuthCode) throws Exception;
	public String get_ProducerOfficeCount(String tcID, SoftAssert softAs,String strAuthCode) throws Exception;
	public String get_CompanyCount(String tcID, SoftAssert softAs,String strAuthCode) throws Exception;
	public String get_DistributionChannel(String tcID, SoftAssert softAs,String strAuthCode) throws Exception;
	public String get_ProducerCode(String tcID, SoftAssert softAs,String strAuthCode) throws Exception;
	
	
	
	
}
