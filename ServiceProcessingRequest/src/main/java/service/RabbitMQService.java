package service;

import model.PolicyApprovalRequest;
import model.PolicyApprovalResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class RabbitMQService {

	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.response.queue}")
	private String responseQueue;

	@Value("${rabbitmq.request.queue}")
	private String requestQueue;

	public RabbitMQService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public CompletableFuture<PolicyApprovalResponse> sendApprovalRequest(PolicyApprovalRequest request) {
		rabbitTemplate.convertAndSend(requestQueue, request);

		return CompletableFuture.supplyAsync(() -> {
			return (PolicyApprovalResponse) rabbitTemplate.receiveAndConvert(responseQueue, 5000);
		});
	}

	@RabbitListener(queues = "${rabbitmq.request.queue}")
	public void receiveMessage(PolicyApprovalRequest request) {
		PolicyApprovalResponse response = new PolicyApprovalResponse(request.getPolicyId(), true, "Approved");

		rabbitTemplate.convertAndSend(responseQueue, response);
	}
}
