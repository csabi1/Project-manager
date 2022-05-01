package hu.econsult.exceptions;

import org.springframework.http.HttpStatus;

public class CustomMessageException extends RuntimeException {

	private static final long serialVersionUID = -2118182162202906410L;

	private HttpStatus httpStatus;
	private String message;

	public CustomMessageException(String message, HttpStatus httpStatus) {
		super();
		this.httpStatus = httpStatus;
		this.message = message;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
