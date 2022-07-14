package com.hawkins.simpletimeclock.domain;

import com.hawkins.simpletimeclock.enums.Role;

import java.io.Serializable;

public class ReportDataFilters implements Serializable
{
	private String userIdToView;
	private int priorWorkShiftsThreshold;
	private int priorBreaksThreshold;
	private boolean isCurrentlyOnBreak;
	private boolean isCurrentlyOnLunch;
	private Role roleToView;
	
	// Required for Jackson
	public ReportDataFilters()
	{
	}
	
	public ReportDataFilters(String userIdToView, int priorWorkShiftsThreshold, int priorBreaksThreshold, boolean isCurrentlyOnBreak,
							 boolean isCurrentlyOnLunch, Role roleToView)
	{
		this.userIdToView = userIdToView;
		this.priorWorkShiftsThreshold = priorWorkShiftsThreshold;
		this.priorBreaksThreshold = priorBreaksThreshold;
		this.isCurrentlyOnBreak = isCurrentlyOnBreak;
		this.isCurrentlyOnLunch = isCurrentlyOnLunch;
		this.roleToView = roleToView;
	}
	
	public String getUserIdToView()
	{
		return userIdToView;
	}
	
	public void setUserIdToView(String userIdToView)
	{
		this.userIdToView = userIdToView;
	}
	
	public int getPriorWorkShiftsThreshold()
	{
		return priorWorkShiftsThreshold;
	}
	
	public void setPriorWorkShiftsThreshold(int priorWorkShiftsThreshold)
	{
		this.priorWorkShiftsThreshold = priorWorkShiftsThreshold;
	}
	
	public int getPriorBreaksThreshold()
	{
		return priorBreaksThreshold;
	}
	
	public void setPriorBreaksThreshold(int priorBreaksThreshold)
	{
		this.priorBreaksThreshold = priorBreaksThreshold;
	}
	
	public boolean isCurrentlyOnBreak()
	{
		return isCurrentlyOnBreak;
	}
	
	public void setCurrentlyOnBreak(boolean currentlyOnBreak)
	{
		isCurrentlyOnBreak = currentlyOnBreak;
	}
	
	public boolean isCurrentlyOnLunch()
	{
		return isCurrentlyOnLunch;
	}
	
	public void setCurrentlyOnLunch(boolean currentlyOnLunch)
	{
		isCurrentlyOnLunch = currentlyOnLunch;
	}
	
	public Role getRoleToView()
	{
		return roleToView;
	}
	
	public void setRoleToView(Role roleToView)
	{
		this.roleToView = roleToView;
	}
}