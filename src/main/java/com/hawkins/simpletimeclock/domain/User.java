package com.hawkins.simpletimeclock.domain;

import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

public class User
{
	private final String userId;
	private String name;
	private WorkShift currentWorkShift;
	private List<WorkShift> priorWorkShifts = new ArrayList<>();
	
	public User(@NonNull String userId)
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
	
	public WorkShift getCurrentWorkShift()
	{
		return currentWorkShift;
	}
	
	public void setCurrentWorkShift(WorkShift currentWorkShift)
	{
		this.currentWorkShift = currentWorkShift;
	}
	
	public List<WorkShift> getPriorWorkShifts()
	{
		return priorWorkShifts;
	}
}