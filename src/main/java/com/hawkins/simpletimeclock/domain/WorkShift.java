package com.hawkins.simpletimeclock.domain;

import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

public class WorkShift
{
	private final LocalDateTime startTime;
	private LocalDateTime endTime;
	
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