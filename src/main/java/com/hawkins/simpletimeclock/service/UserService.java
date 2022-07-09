package com.hawkins.simpletimeclock.service;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import com.hawkins.simpletimeclock.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService
{
	private final UserRepository userRepository;
	
	public UserService(UserRepository userRepository)
	{
		this.userRepository = userRepository;
	}
	
	public User findUser(String userId) throws UserNotFoundException
	{
		return userRepository.find(userId);
	}
}