package service;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import model.PolicyApprovalRequest;
import model.PolicyApprovalResponse;

@ExtendWith(MockitoExtension.class)
public class PolicyApprovalServiceTest {

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private PolicyApprovalService policyApprovalService;

	private static final String REQUEST_QUEUE = "requestQueue";
	private static final String RESPONSE_QUEUE = "responseQueue";

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		policyApprovalService = new PolicyApprovalService(rabbitTemplate);
		policyApprovalService.requestQueue = REQUEST_QUEUE;
		policyApprovalService.responseQueue = RESPONSE_QUEUE;
	}

	@Test
	public void testSendApprovalRequest_Success() {
		PolicyApprovalRequest request = new PolicyApprovalRequest();
		request.setPolicyId("policy123");
		request.setPolicyHolderName("John Doe");
		request.setCoverageAmount(100000.0);

		PolicyApprovalResponse expectedResponse = new PolicyApprovalResponse("policy123", true, "Message");

		when(rabbitTemplate.receiveAndConvert(RESPONSE_QUEUE, 5000)).thenReturn(expectedResponse);

		PolicyApprovalResponse actualResponse = policyApprovalService.sendApprovalRequest(request);

		assertNotNull(actualResponse);
		assertEquals(expectedResponse.getPolicyId(), actualResponse.getPolicyId());
		assertEquals(actualResponse.getApproved(), true);
		assertNotNull(actualResponse.getMessage());
		verify(rabbitTemplate).convertAndSend(REQUEST_QUEUE, request);
	}

	@Test
	public void testSendApprovalRequest_InvalidResponse() {
		PolicyApprovalRequest request = new PolicyApprovalRequest();
		request.setPolicyId("policy123");
		request.setPolicyHolderName("John Doe");
		request.setCoverageAmount(100000.0);

		PolicyApprovalResponse invalidResponse = new PolicyApprovalResponse("policy456", true, null);

		when(rabbitTemplate.receiveAndConvert(RESPONSE_QUEUE, 5000)).thenReturn(invalidResponse);

		PolicyApprovalResponse actualResponse = policyApprovalService.sendApprovalRequest(request);

		assertNotNull(actualResponse);
		assertFalse(actualResponse.getApproved());
		assertEquals("Invalid or incomplete approval data.", actualResponse.getMessage());
	}

	@Test
	public void testSendApprovalRequest_TimeoutResponse() {
		PolicyApprovalRequest request = new PolicyApprovalRequest();
		request.setPolicyId("policy123");
		request.setPolicyHolderName("John Doe");
		request.setCoverageAmount(100000.0);

		when(rabbitTemplate.receiveAndConvert(RESPONSE_QUEUE, 5000)).thenReturn(null);

		PolicyApprovalResponse actualResponse = policyApprovalService.sendApprovalRequest(request);

		assertNotNull(actualResponse);
		assertFalse(actualResponse.getApproved());
		assertEquals("Approval process timed out or failed.", actualResponse.getMessage());
	}
}