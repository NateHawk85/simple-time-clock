package com.hawkins.simpletimeclock.service;

import com.hawkins.simpletimeclock.domain.Break;
import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.domain.WorkShift;
import com.hawkins.simpletimeclock.enums.BreakType;
import com.hawkins.simpletimeclock.exception.*;
import com.hawkins.simpletimeclock.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService
{
	private final UserRepository userRepository;
	private final Clock clock;
	
	public UserService(UserRepository userRepository, Clock clock)
	{
		this.userRepository = userRepository;
		this.clock = clock;
	}
	
	public User createUser(String userId) throws UserAlreadyExistsException
	{
		return userRepository.create(new User(userId));
	}
	
	public User findUser(String userId) throws UserNotFoundException
	{
		return userRepository.find(userId);
	}
	
	public void startShift(String userId) throws UserNotFoundException, WorkShiftInProgressException
	{
		User user = userRepository.find(userId);
		validateUserNotWorking(user);
		
		user.setCurrentWorkShift(new WorkShift(clock.now()));
		
		userRepository.update(user);
	}
	
	public void endShift(String userId) throws UserNotFoundException, WorkShiftNotStartedException, BreakInProgressException
	{
		User user = userRepository.find(userId);
		validateUserIsWorking(user);
		validateUserNotOnBreak(user);
		
		user.getCurrentWorkShift().setEndTime(clock.now());
		user.getPriorWorkShifts().add(user.getCurrentWorkShift());
		user.setCurrentWorkShift(null);
		
		userRepository.update(user);
	}
	
	public void startBreak(String userId, BreakType breakType) throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		User user = userRepository.find(userId);
		validateUserIsWorking(user);
		
		if (breakType == BreakType.Break)
		{
			if (user.getCurrentBreak() != null)
			{
				throw new BreakInProgressException();
			}
			
			user.setCurrentBreak(new Break(breakType, clock.now()));
		}
		if (breakType == BreakType.Lunch)
		{
			if (user.getCurrentLunchBreak() != null)
			{
				throw new BreakInProgressException();
			}
			
			user.setCurrentLunchBreak(new Break(breakType, clock.now()));
		}
		
		userRepository.update(user);
	}
	
	public void endBreak(String userId) throws UserNotFoundException, BreakNotStartedException
	{
		User user = userRepository.find(userId);
		
		if (user.getCurrentBreak() != null)
		{
			user.getCurrentBreak().setEndTime(clock.now());
			user.getPriorBreaks().add(user.getCurrentBreak());
			user.setCurrentBreak(null);
		} else if (user.getCurrentLunchBreak() != null)
		{
			user.getCurrentLunchBreak().setEndTime(clock.now());
			user.getPriorBreaks().add(user.getCurrentLunchBreak());
			user.setCurrentLunchBreak(null);
		} else
		{
			throw new BreakNotStartedException();
		}
		
		userRepository.update(user);
	}
	
	private void validateUserIsWorking(User user) throws WorkShiftNotStartedException
	{
		if (user.getCurrentWorkShift() == null)
		{
			throw new WorkShiftNotStartedException();
		}
	}
	
	private void validateUserNotWorking(User user) throws WorkShiftInProgressException
	{
		if (user.getCurrentWorkShift() != null)
		{
			throw new WorkShiftInProgressException();
		}
	}
	
	private void validateUserNotOnBreak(User user) throws BreakInProgressException
	{
		if (user.getCurrentBreak() != null || user.getCurrentLunchBreak() != null)
		{
			throw new BreakInProgressException();
		}
	}
}