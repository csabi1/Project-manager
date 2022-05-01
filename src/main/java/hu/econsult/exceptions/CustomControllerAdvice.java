package hu.econsult.exceptions;

import java.util.Date;
import java.util.NoSuchElementException;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import hu.econsult.exceptions.model.ExceptionResponse;

@RestController
@ControllerAdvice
public class CustomControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllException(Exception ex, WebRequest request) {
		ex.printStackTrace();
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(NoSuchElementException.class)
	public final ResponseEntity<Object> handleNoElementFound(Exception ex, WebRequest request) {
		ex.printStackTrace();
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(RuntimeException.class)
	public final ResponseEntity<Object> handleRuntimeException(Exception ex, WebRequest request) {
		ex.printStackTrace();
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(JAXBException.class)
	public final ResponseEntity<Object> handleParseException(Exception ex, WebRequest request) {
		ex.printStackTrace();
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Hiba történt az XML parseolása közben!", request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
		
	@ExceptionHandler(CustomMessageException.class)
	public final ResponseEntity<Object> handleCustomMessageException(CustomMessageException cme, WebRequest request) {
		cme.printStackTrace();
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), cme.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, cme.getHttpStatus());
	}
	
	@ExceptionHandler({ConstraintViolationException.class, DataIntegrityViolationException.class})
	public final ResponseEntity<Object> handleDBConstraintException(Exception ex, WebRequest request) {
		ex.printStackTrace();
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "A művelet nem hajtható végre, adatintegritási megszorítások sérülhetnek!!", request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid (
			MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		ex.printStackTrace();
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(), "VALIDATION FAILED!");
		return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

}

