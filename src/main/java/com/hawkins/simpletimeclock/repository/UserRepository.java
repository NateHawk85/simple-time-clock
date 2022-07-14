package com.hawkins.simpletimeclock.repository;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserAlreadyExistsException;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class UserRepository
{
	private final SimpleDatabaseRepository simpleDatabaseRepository;
	
	public UserRepository(SimpleDatabaseRepository simpleDatabaseRepository)
	{
		this.simpleDatabaseRepository = simpleDatabaseRepository;
	}
	
	public User create(@NonNull User user) throws UserAlreadyExistsException
	{
		Map<String, User> users = simpleDatabaseRepository.read();
		
		if (users.get(user.getUserId()) != null)
		{
			throw new UserAlreadyExistsException();
		}
		
		users.put(user.getUserId(), user);
		simpleDatabaseRepository.write(users);
		
		return user;
	}
	
	public User find(String userId) throws UserNotFoundException
	{
		Map<String, User> users = simpleDatabaseRepository.read();
		User user = users.get(userId);
		
		if (user == null)
		{
			throw new UserNotFoundException();
		}
		
		return user;
	}
	
	public Map<String, User> findAllUsers()
	{
		return simpleDatabaseRepository.read();
	}
	
	public User update(@NonNull User user) throws UserNotFoundException
	{
		Map<String, User> users = simpleDatabaseRepository.read();
		
		if (users.get(user.getUserId()) == null)
		{
			throw new UserNotFoundException();
		}
		
		users.put(user.getUserId(), user);
		simpleDatabaseRepository.write(users);
		
		return user;
	}
}