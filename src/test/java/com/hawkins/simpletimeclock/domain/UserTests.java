package com.hawkins.simpletimeclock.domain;

import com.hawkins.simpletimeclock.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTests
{
	private static final String USER_ID = "987654321";
	private static final LocalDateTime START_TIME = LocalDateTime.of(2022, 12, 31, 12, 30);
	
	private User user;
	
	@BeforeEach
	public void setUp()
	{
		user = new User(USER_ID);
	}
	
	@Test
	public void startShift_When_NoCurrentWorkShiftExists_Then_DoesNotThrowException()
	{
		assertDoesNotThrow(() -> user.startShift(START_TIME));
	}
	
	@Test
	public void startShift_WhenShiftHasAlreadyStarted_Then_ThrowsShiftAlreadyStartedException()
	{
		user.startShift(START_TIME);
		assertThrows(UserAlreadyExistsException.class, () -> user.startShift(START_TIME));
	}
}