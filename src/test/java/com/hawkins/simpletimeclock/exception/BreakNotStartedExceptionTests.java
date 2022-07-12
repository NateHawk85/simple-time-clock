package com.hawkins.simpletimeclock.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BreakNotStartedExceptionTests
{
	@Test
	public void class_HasCorrectAnnotations()
	{
		ResponseStatus annotation = BreakNotStartedException.class.getAnnotation(ResponseStatus.class);
		
		assertNotNull(annotation);
		assertEquals(HttpStatus.CONFLICT, annotation.value());
		assertEquals("Break has not started", annotation.reason());
	}
}