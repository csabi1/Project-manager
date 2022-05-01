package hu.econsult.exceptions;

public class UserRequestNotAllowedException extends RuntimeException {

	private static final long serialVersionUID = 4651919707886047105L;
	
	public UserRequestNotAllowedException() {
		super("Nincs jogosultsága a művelethez!");
	}

}
