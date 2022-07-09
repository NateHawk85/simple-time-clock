package com.hawkins.simpletimeclock.domain;

import java.time.LocalDateTime;

public class User
{
	private final String userId;
	private String name;
	private WorkShift currentWorkShift;
	
	public User(String userId)
	{
		this.userId = userId;
	}
	
	// TODO - implement/test
	public void startShift(LocalDateTime startTime)
	{
	
	}
	
	public String getUserId()
	{
		return userId;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
}