package com.hawkins.simpletimeclock.domain;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public class WorkShift implements Serializable
{
	private final LocalDateTime startTime;
	private LocalDateTime endTime;
	
	// Required for Jackson
	public WorkShift()
	{
		startTime = null;
	}
	
	public WorkShift(@NonNull LocalDateTime startTime)
	{
		this.startTime = startTime;
	}
	
	public LocalDateTime getStartTime()
	{
		return startTime;
	}
	
	public LocalDateTime getEndTime()
	{
		return endTime;
	}
	
	public void setEndTime(LocalDateTime endTime)
	{
		this.endTime = endTime;
	}
}