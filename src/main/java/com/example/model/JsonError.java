package com.example.model;

public class JsonError {
	private int status;
	private String message;
	
	public JsonError(int status,String message){
		if(status<200 ||status>599){
			throw new IllegalArgumentException("tatus should be between 200 and 500");
		}
		this.status=status;
		this.message=message;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String toJson(){
		return "{\"status\":\""+this.status+"\", \"error\":\""+this.message+"\"}";
	}

}
