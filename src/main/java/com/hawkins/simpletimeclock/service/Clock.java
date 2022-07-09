package com.hawkins.simpletimeclock.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class Clock
{
	// Wrapping static method for better testability
	public LocalDateTime now()
	{
		return LocalDateTime.now();
	}
}