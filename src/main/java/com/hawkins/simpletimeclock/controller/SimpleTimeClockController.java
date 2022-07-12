package com.hawkins.simpletimeclock.controller;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.enums.BreakType;
import com.hawkins.simpletimeclock.exception.*;
import com.hawkins.simpletimeclock.service.ContextURIService;
import com.hawkins.simpletimeclock.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
	
	@PostMapping("/user/{userId}/startShift")
	public ResponseEntity<HttpStatus> startShift(@PathVariable String userId) throws UserNotFoundException, WorkShiftAlreadyStartedException
	{
		userService.startShift(userId);
		
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
	
	@PostMapping("/user/{userId}/endShift")
	public ResponseEntity<HttpStatus> endShift(@PathVariable String userId) throws UserNotFoundException, WorkShiftNotStartedException
	{
		userService.endShift(userId);
		
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
	
	@PostMapping("/user/{userId}/startBreak")
	public ResponseEntity<HttpStatus> startBreak(@PathVariable String userId, @RequestParam(required = false) BreakType breakType)
			throws UserNotFoundException, BreakAlreadyStartedException
	{
		breakType = breakType != null ? breakType : BreakType.Break;
		
		userService.startBreak(userId, breakType);
		
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
	
	@PostMapping("/user/{userId}/endBreak")
	public ResponseEntity<HttpStatus> endBreak(@PathVariable String userId) throws UserNotFoundException, BreakNotStartedException
	{
		userService.endBreak(userId);
		
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
}