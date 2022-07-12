package com.hawkins.simpletimeclock.repository;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserAlreadyExistsException;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Repository;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTests
{
	private static final String USER_ID = "987654321";
	
	private UserRepository userRepository;
	
	@BeforeEach
	public void setUp()
	{
		userRepository = new UserRepository();
	}
	
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(UserRepository.class.getAnnotation(Repository.class));
	}
	
	//region create
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void create_When_UserDoesNotExistWithId_Then_ReturnsUser(String userId) throws UserAlreadyExistsException
	{
		User user = new User(userId);
		
		User actual = userRepository.create(user);
		
		assertEquals(user, actual);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void create_When_MultipleUsersExistButNoneWithId_Then_ReturnsUser(String userId) throws UserAlreadyExistsException
	{
		User user = new User(userId);
		
		User actual = userRepository.create(user);
		
		assertEquals(user, actual);
	}
	
	@Test
	public void create_When_UserExistsWithId_Then_ThrowsUserAlreadyExistsException() throws UserAlreadyExistsException
	{
		User user = new User(USER_ID);
		userRepository.create(user);
		
		assertThrows(UserAlreadyExistsException.class, () -> userRepository.create(new User(USER_ID)));
	}
	
	@Test
	public void create_When_MultipleUsersExistIncludingOneWithId_Then_ThrowsUserAlreadyExistsException() throws UserAlreadyExistsException
	{
		userRepository = new UserRepository();
		User user = new User(USER_ID);
		userRepository.create(user);
		createMultipleTestUsers();
		
		assertThrows(UserAlreadyExistsException.class, () -> userRepository.create(new User(USER_ID)));
	}
	
	//endregion
	
	//region find
	
	@Test
	public void find_When_UserDoesNotExistWithId_Then_ThrowsUserNotFoundException()
	{
		assertThrows(UserNotFoundException.class, () -> userRepository.find(USER_ID));
	}
	
	@Test
	public void find_When_MultipleUsersExistButNoneWithId_Then_ThrowsUserNotFoundException() throws UserAlreadyExistsException
	{
		createMultipleTestUsers();
		
		assertThrows(UserNotFoundException.class, () -> userRepository.find(USER_ID));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void find_When_UserExistsWithId_Then_ReturnsUser(String userId) throws UserAlreadyExistsException, UserNotFoundException
	{
		User user = new User(userId);
		userRepository.create(user);
		
		User actual = userRepository.find(userId);
		
		assertEquals(user, actual);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void find_When_MultipleUsersExistIncludingOneWithId_Then_ReturnsUser(String userId) throws UserAlreadyExistsException, UserNotFoundException
	{
		userRepository = new UserRepository();
		User user = new User(userId);
		userRepository.create(user);
		createMultipleTestUsers();
		
		User actual = userRepository.find(userId);
		
		assertEquals(user, actual);
	}
	
	//endregion
	
	//region update
	
	@Test
	public void update_When_UserDoesNotExistWithId_Then_ThrowsUserNotFoundException()
	{
		assertThrows(UserNotFoundException.class, () -> userRepository.update(new User(USER_ID)));
	}
	
	@Test
	public void update_When_MultipleUsersExistButNoneWithId_Then_ThrowsUserNotFoundException() throws UserAlreadyExistsException
	{
		createMultipleTestUsers();
		
		assertThrows(UserNotFoundException.class, () -> userRepository.update(new User(USER_ID)));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void update_When_UserExistsWithId_Then_ReturnsUpdatedUser(String userId) throws UserAlreadyExistsException, UserNotFoundException
	{
		User user = new User(userId);
		userRepository.create(user);
		
		User updatedUser = new User(userId);
		User actual = userRepository.update(updatedUser);
		
		assertEquals(updatedUser, actual);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void update_When_MultipleUsersExistIncludingOneWithId_Then_ReturnsUser(String userId) throws UserAlreadyExistsException, UserNotFoundException
	{
		userRepository = new UserRepository();
		User user = new User(userId);
		userRepository.create(user);
		createMultipleTestUsers();
		
		User updatedUser = new User(userId);
		User actual = userRepository.update(updatedUser);
		
		assertEquals(updatedUser, actual);
	}
	
	//endregion
	
	private void createMultipleTestUsers() throws UserAlreadyExistsException
	{
		userRepository.create(new User("123456789"));
		userRepository.create(new User("111111111"));
		userRepository.create(new User("222222222"));
		userRepository.create(new User("333333333"));
	}
}