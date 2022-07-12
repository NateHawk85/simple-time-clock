package com.hawkins.simpletimeclock.service;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.domain.WorkShift;
import com.hawkins.simpletimeclock.exception.UserAlreadyExistsException;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import com.hawkins.simpletimeclock.exception.WorkShiftAlreadyStartedException;
import com.hawkins.simpletimeclock.exception.WorkShiftNotStartedException;
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
	
	public void startShift(String userId) throws WorkShiftAlreadyStartedException, UserNotFoundException
	{
		User user = userRepository.find(userId);
		
		if (user.getCurrentWorkShift() != null)
		{
			throw new WorkShiftAlreadyStartedException();
		}
		
		user.setCurrentWorkShift(new WorkShift(clock.now()));
		
		userRepository.update(user);
	}
	
	public void endShift(String userId) throws WorkShiftNotStartedException, UserNotFoundException
	{
		User user = userRepository.find(userId);
		
		if (user.getCurrentWorkShift() == null)
		{
			throw new WorkShiftNotStartedException();
		}
		
		user.getCurrentWorkShift().setEndTime(clock.now());
		user.getPriorWorkShifts().add(user.getCurrentWorkShift());
		user.setCurrentWorkShift(null);
		
		userRepository.update(user);
	}
}