package com.hawkins.simpletimeclock.service;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import com.hawkins.simpletimeclock.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests
{
	private static final String USER_ID = "987654321";
	
	@Mock
	private UserRepository userRepository;
	@InjectMocks
	private UserService userService;
	
	private User user;
	
	@BeforeEach
	public void setUp() throws UserNotFoundException
	{
		user = new User(USER_ID);
		lenient().when(userRepository.find(anyString())).thenReturn(user);
	}
	
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(UserService.class.getAnnotation(Service.class));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void findUser_CallsUserRepository(String userId) throws UserNotFoundException
	{
		userService.findUser(userId);
		
		verify(userRepository).find(userId);
	}
	
	@Test
	public void findUser_When_UserRepositoryReturnsUser_Then_ReturnsSameUser() throws UserNotFoundException
	{
		User actual = userService.findUser(USER_ID);
		
		assertEquals(user, actual);
	}
	
	@Test
	public void findUser_When_UserRepositoryThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.find(anyString())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.findUser(USER_ID));
	}
}