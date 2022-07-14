package com.hawkins.simpletimeclock.domain;

import com.hawkins.simpletimeclock.enums.Role;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

public class User
{
	private final String userId;
	private final List<WorkShift> priorWorkShifts = new ArrayList<>();
	private final List<Break> priorBreaks = new ArrayList<>();
	private String name;
	private WorkShift currentWorkShift;
	private Break currentBreak;
	private Break currentLunchBreak;
	private Role role;
	
	// Required for Jackson
	public User()
	{
		this.userId = null;
	}
	
	public User(@NonNull String userId)
	{
		this.userId = userId;
	}
	
	public String getUserId()
	{
		return userId;
	}
	
	public List<WorkShift> getPriorWorkShifts()
	{
		return priorWorkShifts;
	}
	
	public List<Break> getPriorBreaks()
	{
		return priorBreaks;
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
	
	public Break getCurrentBreak()
	{
		return currentBreak;
	}
	
	public void setCurrentBreak(Break currentBreak)
	{
		this.currentBreak = currentBreak;
	}
	
	public Break getCurrentLunchBreak()
	{
		return currentLunchBreak;
	}
	
	public void setCurrentLunchBreak(Break currentLunchBreak)
	{
		this.currentLunchBreak = currentLunchBreak;
	}
	
	public Role getRole()
	{
		return role;
	}
	
	public void setRole(Role role)
	{
		this.role = role;
	}
}