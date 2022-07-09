package com.hawkins.simpletimeclock.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserNotFoundExceptionTests
{
	@Test
	public void class_HasCorrectAnnotations()
	{
		ResponseStatus annotation = UserNotFoundException.class.getAnnotation(ResponseStatus.class);
		
		assertNotNull(annotation);
		assertEquals(HttpStatus.NOT_FOUND, annotation.value());
		assertEquals("User Not Found", annotation.reason());
	}
}