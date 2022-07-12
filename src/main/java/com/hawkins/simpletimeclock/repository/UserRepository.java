package com.hawkins.simpletimeclock.repository;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserAlreadyExistsException;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepository
{
	private final Map<String, User> users = new HashMap<>();
	
	public User create(@NonNull User user) throws UserAlreadyExistsException
	{
		if (users.get(user.getUserId()) != null)
		{
			throw new UserAlreadyExistsException();
		}
		
		users.put(user.getUserId(), user);
		
		return user;
	}
	
	public User find(String userId) throws UserNotFoundException
	{
		User user = users.get(userId);
		
		if (user == null)
		{
			throw new UserNotFoundException();
		}
		
		return user;
	}
	
	public User update(@NonNull User user) throws UserNotFoundException
	{
		if (users.get(user.getUserId()) == null)
		{
			throw new UserNotFoundException();
		}
		
		users.put(user.getUserId(), user);
		
		return user;
	}
}