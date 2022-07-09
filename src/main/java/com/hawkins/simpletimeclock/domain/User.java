package com.hawkins.simpletimeclock.domain;

public class User
{
	private final String userId;
	private String name;
	
	public User(String userId)
	{
		this.userId = userId;
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