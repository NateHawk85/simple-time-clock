package com.hawkins.simpletimeclock.service;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContextURIServiceTests
{
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(ContextURIService.class.getAnnotation(Service.class));
	}
}