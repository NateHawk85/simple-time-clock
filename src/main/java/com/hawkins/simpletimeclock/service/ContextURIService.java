package com.hawkins.simpletimeclock.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class ContextURIService
{
	// Wrapping static method for better testability
	public String fullContextPath()
	{
		return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
	}
}