package com.hawkins.simpletimeclock.service;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserServiceTests
{
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(UserService.class.getAnnotation(Service.class));
	}
}