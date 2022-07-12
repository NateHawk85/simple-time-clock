package com.hawkins.simpletimeclock.domain;

import com.hawkins.simpletimeclock.enums.BreakType;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

public class Break
{
	private final LocalDateTime startTime;
	private final BreakType breakType;
	private LocalDateTime endTime;
	
	public Break(BreakType breakType, @NonNull LocalDateTime startTime)
	{
		this.startTime = startTime;
		this.breakType = breakType;
	}
	
	public LocalDateTime getStartTime()
	{
		return startTime;
	}
	
	public BreakType getBreakType()
	{
		return breakType;
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