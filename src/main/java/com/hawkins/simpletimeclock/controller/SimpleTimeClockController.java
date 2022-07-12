package com.hawkins.simpletimeclock.controller;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserAlreadyExistsException;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import com.hawkins.simpletimeclock.service.ContextURIService;
import com.hawkins.simpletimeclock.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class SimpleTimeClockController
{
	private final UserService userService;
	private final ContextURIService contextURIService;
	
	public SimpleTimeClockController(UserService userService, ContextURIService contextURIService)
	{
		this.userService = userService;
		this.contextURIService = contextURIService;
	}
	
	@PostMapping("/user/{userId}")
	public ResponseEntity<User> createUser(@PathVariable String userId) throws UserAlreadyExistsException
	{
		User user = userService.createUser(userId);
		URI location = URI.create(contextURIService.fullContextPath() + "/user/" + userId);
		
		return ResponseEntity.created(location)
				.body(user);
	}
	
	@GetMapping("/user/{userId}")
	public ResponseEntity<User> findUser(@PathVariable String userId) throws UserNotFoundException
	{
		return ResponseEntity.ok(userService.findUser(userId));
	}
}