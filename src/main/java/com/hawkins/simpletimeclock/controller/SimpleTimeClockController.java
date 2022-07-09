package com.hawkins.simpletimeclock.controller;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import com.hawkins.simpletimeclock.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleTimeClockController
{
	private final UserService userService;
	
	public SimpleTimeClockController(UserService userService)
	{
		this.userService = userService;
	}
	
	@GetMapping("/user/{userId}")
	public User findUser(@PathVariable String userId) throws UserNotFoundException
	{
		return userService.findUser(userId);
	}
}