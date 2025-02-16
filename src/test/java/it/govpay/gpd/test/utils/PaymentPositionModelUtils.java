package it.govpay.gpd.test.utils;

import java.time.OffsetDateTime;

import org.mockito.invocation.InvocationOnMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.govpay.gpd.client.beans.PaymentPositionModel;
import it.govpay.gpd.client.beans.PaymentPositionModelBaseResponse;
import it.govpay.gpd.client.beans.PaymentPositionModel.StatusEnum;

public class PaymentPositionModelUtils {
	
	private PaymentPositionModelUtils() {}
	
	public static ResponseEntity<PaymentPositionModelBaseResponse> creaResponseGetPositionOk(InvocationOnMock invocation, it.govpay.gpd.client.beans.PaymentPositionModelBaseResponse.StatusEnum statusEnum) {
		String codDominio = invocation.getArgument(0);
		String iupd = invocation.getArgument(1);
		String xRequestId = invocation.getArgument(2);
		
		PaymentPositionModelBaseResponse response = new PaymentPositionModelBaseResponse();
		response.setOrganizationFiscalCode(codDominio);
		response.setIupd(iupd);
		response.setStatus(statusEnum);
		
		return new ResponseEntity<>(response, GpdUtils.getHeadersCreatedOk(xRequestId), HttpStatus.OK);
	}
	
	public static ResponseEntity<PaymentPositionModel> creaResponsePublishPositionOk(InvocationOnMock invocation) {
		String codDominio = invocation.getArgument(0);
		String iupd = invocation.getArgument(1);
		String xRequestId = invocation.getArgument(2);
		
		PaymentPositionModel response = new PaymentPositionModel(null, StatusEnum.VALID);
		response.setFiscalCode(codDominio);
		response.setIupd(iupd);
		
		return new ResponseEntity<>(response, GpdUtils.getHeadersCreatedOk(xRequestId), HttpStatus.OK);
	}
	
	public static ResponseEntity<PaymentPositionModel> creaResponseInvalidatePositionOk(InvocationOnMock invocation) {
		String codDominio = invocation.getArgument(0);
		String iupd = invocation.getArgument(1);
		String xRequestId = invocation.getArgument(2);
		
		PaymentPositionModel response = new PaymentPositionModel(null, StatusEnum.INVALID);
		response.setFiscalCode(codDominio);
		response.setIupd(iupd);
		
		return new ResponseEntity<>(response, GpdUtils.getHeadersCreatedOk(xRequestId), HttpStatus.OK);
	}
	
	public static ResponseEntity<PaymentPositionModel> creaResponseCreatePaymentPositionModelOk(InvocationOnMock invocation) {
		PaymentPositionModel paymentPositionModel = invocation.getArgument(1);
		String xRequestId = invocation.getArgument(2);
		Boolean toPublish = invocation.getArgument(3);
		StatusEnum status = PaymentPositionModelUtils.getStatus(toPublish, paymentPositionModel.getValidityDate());
		
		PaymentPositionModel response = PaymentPositionModelUtils.createPaymentPositionModelResponse(paymentPositionModel, null, status);
		
		return new ResponseEntity<>(response, GpdUtils.getHeadersCreatedOk(xRequestId), HttpStatus.CREATED);
	}
	
	public static ResponseEntity<PaymentPositionModel> creaResponseUpdatePaymentPositionModelOk(InvocationOnMock invocation) {
		PaymentPositionModel paymentPositionModel = invocation.getArgument(2);
		String xRequestId = invocation.getArgument(3);
		Boolean toPublish = invocation.getArgument(4);
		StatusEnum status = PaymentPositionModelUtils.getStatus(toPublish, paymentPositionModel.getValidityDate());
		
		PaymentPositionModel response = PaymentPositionModelUtils.createPaymentPositionModelResponse(paymentPositionModel, null, status);
		
		return new ResponseEntity<>(response, GpdUtils.getHeadersCreatedOk(xRequestId), HttpStatus.OK);
	}
	
	public static StatusEnum getStatus(Boolean toPublish, OffsetDateTime validityDate) {
		if(toPublish != null && toPublish.booleanValue() && validityDate == null)
			return StatusEnum.VALID;
		
		return StatusEnum.DRAFT;
	}
	
	public static PaymentPositionModel createPaymentPositionModelResponse(PaymentPositionModel request, OffsetDateTime paymentDate, StatusEnum status) {
		PaymentPositionModel response = new PaymentPositionModel(paymentDate, status);

		response.setCity(request.getCity());
		response.setCivicNumber(request.getCivicNumber());
		response.setCompanyName(request.getCompanyName());
		response.setCountry(request.getCountry());
		response.setEmail(request.getEmail());
		response.setFiscalCode(request.getFiscalCode());
		response.setFullName(request.getFullName());
		response.setIupd(request.getIupd());
		response.setOfficeName(request.getOfficeName());
		response.setPaymentOption(request.getPaymentOption());
		response.setPayStandIn(request.isPayStandIn());
		response.setPhone(request.getPhone());
		response.setPostalCode(request.getPostalCode());
		response.setProvince(request.getProvince());
		response.setRegion(request.getRegion());
		response.setStreetName(request.getStreetName());
		response.setSwitchToExpired(request.isSwitchToExpired());
		response.setType(request.getType());
		response.setValidityDate(request.getValidityDate());
		
		return response;
	}
}
