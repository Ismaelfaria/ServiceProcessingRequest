package model;

public class PolicyApprovalResponse {

	private String policyId;
	private boolean approved;
	private String message;

	public PolicyApprovalResponse(String policyId, boolean approved, String message) {
		this.policyId = policyId;
		this.approved = approved;
		this.message = message;
	}

	public String getPolicyId() {
		return policyId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}

	public boolean getApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
