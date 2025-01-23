package service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import model.PolicyApprovalRequest;
import model.PolicyApprovalResponse;

@Service
public class PolicyApprovalService {

	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.request.queue}")
	public String requestQueue;

	@Value("${rabbitmq.response.queue}")
	public String responseQueue;

	public PolicyApprovalService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public PolicyApprovalResponse sendApprovalRequest(PolicyApprovalRequest request) {
		rabbitTemplate.convertAndSend(requestQueue, request);

		Object response = rabbitTemplate.receiveAndConvert(responseQueue, 5000);

		return processApprovalResponse(response, request.getPolicyId());
	}

	private PolicyApprovalResponse processApprovalResponse(Object response, String policyId) {
		if (response instanceof PolicyApprovalResponse) {
			PolicyApprovalResponse approvalResponse = (PolicyApprovalResponse) response;

			// Valida os dados da resposta
			if (isValidResponse(approvalResponse, policyId)) {
				return approvalResponse;
			} else {
				return createErrorResponse(policyId, "Invalid or incomplete approval data.");
			}
		} else {
			return createErrorResponse(policyId, "Approval process timed out or failed.");
		}
	}

	private boolean isValidResponse(PolicyApprovalResponse response, String policyId) {
		return response.getPolicyId() != null && response.getPolicyId().equals(policyId)
				&& response.getApproved() != false;
	}

	private PolicyApprovalResponse createErrorResponse(String policyId, String errorMessage) {
		return new PolicyApprovalResponse(policyId, false, errorMessage);
	}
}
