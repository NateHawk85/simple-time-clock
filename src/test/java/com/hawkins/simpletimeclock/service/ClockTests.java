package com.hawkins.simpletimeclock.service;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClockTests
{
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(Clock.class.getAnnotation(Service.class));
	}
	
	@Test
	public void now_ReturnsStaticNow() throws InterruptedException
	{
		LocalDateTime before = LocalDateTime.now();
		Thread.sleep(5);
		LocalDateTime between = new Clock().now();
		Thread.sleep(5);
		LocalDateTime after = LocalDateTime.now();
		
		assertTrue(between.isAfter(before));
		assertTrue(between.isBefore(after));
	}
}