package com.pizzamania.exception;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.exacttarget.fuelsdk.ETSdkException;
import com.pizzamania.utility.Message;
import com.pizzamania.utility.MessageConfiguration;
import com.pizzamania.utility.Resource;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

	private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@Autowired
	MessageConfiguration messageConfig;

	/*
	 * To handle errors related to invalid use of null objects
	 */
	@ExceptionHandler(value = NullPointerException.class)
	public Resource<String> nullPointerException(NullPointerException e) {
		log.info("NullPointerException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("nullPointerException"),
				HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle errors related to CRUD operation
	 */
	@ExceptionHandler(value = CRUDFailureException.class)
	public Resource<String> crudFailureException(CRUDFailureException e) {
		log.info("CrudFailureException", e.getMessage(), e.getErrorType());
		return new Resource<String>(null, null, messageConfig.getMessage(e.getErrorType()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/*
	 * To handle errors related to argument type validation failure
	 */
	@ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
	public Resource<String> methodTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.info("MethodArgumentTypeMismatchException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("methodArgumentTypeMismatchException"),
				HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle errors related to invalid HTTP REST method
	 */
	@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
	public Resource<String> methodNotSupported(HttpRequestMethodNotSupportedException e) {
		log.info("HttpRequestMethodNotSupportedException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("httpRequestMethodNotSupportedException"),
				HttpStatus.METHOD_NOT_ALLOWED);
	}

	/*
	 * To handle errors related to request body missing for post requests
	 */
	@ExceptionHandler(value = HttpMessageNotReadableException.class)
	public Resource<String> htttpMessageNotReadable(HttpMessageNotReadableException e) {
		log.info("HttpMessageNotReadableException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("httpMessageNotReadableException"),
				HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle errors related to request body validation for post requests
	 */
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public Resource<String> methodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.info("HttpMessageNotReadableException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("httpMessageNotReadableException"),
				HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle errors related to request body validation for post requests
	 */
	@ExceptionHandler(value = ConstraintViolationException.class)
	public Resource<String> constraintViolationException(ConstraintViolationException e) {
		log.info("ConstraintViolationException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("dataIntegrityViolationException"),
				HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle unique key constraint violation
	 */
	@ExceptionHandler(value = DuplicateKeyException.class)
	public Resource<String> duplicateKeysException(DuplicateKeyException e) {
		log.info("DuplicateKeyException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("duplicateKeyException"),
				HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle errors related to SQL syntax
	 */
	@ExceptionHandler(value = BadSqlGrammarException.class)
	public Resource<String> sqlSyntaxException(BadSqlGrammarException e) {
		log.info("BadSqlGrammarException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("badSqlGrammarException"),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/*
	 * To handle errors related to database related failure
	 */
	@ExceptionHandler(value = SQLException.class)
	public Resource<String> sqlException(SQLException e) {
		log.info("SqlException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("SqlException"),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/*
	 * To handle errors related to database related failure
	 */
	@ExceptionHandler(value = DataIntegrityViolationException.class)
	public Resource<String> dataIntegrityViolationException(DataIntegrityViolationException e) {
		log.info("DataIntegrityViolationException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("dataIntegrityViolationException"),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/*
	 * To handle errors when you try to invoke salesforce api to send email
	 */
	@ExceptionHandler(value = ETSdkException.class)
	public Resource<String> handleETSdkException(ETSdkException e) {
		log.info("ETSdkException", e.getMessage());
		return new Resource<String>(null, null, messageConfig.getMessage("sdkException"),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/*
	 * To handle errors related to authentication/authorization failure
	 */
	@ExceptionHandler(value = BadCredentialsException.class)
	public Resource<String> badCredentialsException(BadCredentialsException e) {
		log.info("BadCredentialsException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("badCredentialsException"),
				HttpStatus.FORBIDDEN);
	}

	/*
	 * To handle errors related to not being allowed access to a specific endpoint
	 */
	@ExceptionHandler(value = AccessDeniedException.class)
	public Resource<String> accessDeniedException(AccessDeniedException e) {
		log.info("AccessDeniedException", e.getMessage());
		return new Resource<String>(null, null, messageConfig.getMessage("accessDeniedException"),
				HttpStatus.FORBIDDEN);
	}

	/*
	 * To handle errors when you try to create new records instead of updating them
	 */
	@ExceptionHandler(value = ResourceAlreadyExistsException.class)
	public Resource<String> activeRecordExistsException(ResourceAlreadyExistsException e) {
		log.info("ResourceAlreadyExistsException", e.getMessage());
		Message message = messageConfig.getMessage("activeRecordExistsException");
		if (null != e.getMessage()) {
			message.setDetail(e.getMessage());
		}
		return new Resource<String>(null, null, message, HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle errors when you try to update records but no record found for
	 * update
	 */
	@ExceptionHandler(value = ResourceNotFoundException.class)
	public Resource<String> noRecordExistException(ResourceNotFoundException e) {
		log.info("ResourceNotFoundException", e.getMessage());
		Message message = messageConfig.getMessage("noRecordExistsException");
		if (null != e.getMessage()) {
			message.setDetail(e.getMessage());
		}
		return new Resource<String>(null, null, message, HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle errors when you try to post request with missing required inputs
	 * for create and update
	 */
	@ExceptionHandler(value = EntityMissingException.class)
	public Resource<String> requiredFieldMissing(EntityMissingException e) {
		log.info("EntityMissingException", e.getMessage());
		Message message = messageConfig.getMessage("requiredInfoMissing");
		if (null != e.getMessage()) {
			message.setDetail(e.getMessage());
		}
		return new Resource<String>(null, null, message, HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle any unhandled exceptions
	 */
	@ExceptionHandler(value = IllegalArgumentException.class)
	public Resource<String> illegalArgumentException(IllegalArgumentException e) {
		log.info("IllegalArgumentException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("badRequest"), HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle any unhandled exceptions
	 */
	@ExceptionHandler(value = RuntimeException.class)
	public Resource<String> runtimeException(RuntimeException e) {
		log.info("RuntimeException", e.getMessage());
		return new Resource<String>(null, null, messageConfig.getMessage("badRequest"), HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle any unhandled exceptions
	 */
	@ExceptionHandler(value = ParseException.class)
	public Resource<String> parseException(ParseException e) {
		log.info("ParseException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("notParseable"), HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle any unhandled exceptions
	 */
	@ExceptionHandler(value = IOException.class)
	public Resource<String> iOException(IOException e) {
		log.info("IOException", e);
		return new Resource<String>(null, null, messageConfig.getMessage("badRequest"), HttpStatus.BAD_REQUEST);
	}

	/*
	 * To handle any unhandled exceptions
	 */
	@ExceptionHandler(value = Exception.class)
	public Resource<String> exception(Exception e) {
		log.info("Exception", e);
		return new Resource<String>(null, null, messageConfig.getMessage("exception"),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
