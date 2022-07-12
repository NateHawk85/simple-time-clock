package com.hawkins.simpletimeclock.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BreakInProgressExceptionTests
{
	@Test
	public void class_HasCorrectAnnotations()
	{
		ResponseStatus annotation = BreakInProgressException.class.getAnnotation(ResponseStatus.class);
		
		assertNotNull(annotation);
		assertEquals(HttpStatus.CONFLICT, annotation.code());
		assertEquals("Break is in progress", annotation.reason());
	}
}