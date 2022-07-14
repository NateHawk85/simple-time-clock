package com.hawkins.simpletimeclock.repository;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserAlreadyExistsException;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTests
{
	private static final String USER_ID = "987654321";
	
	@Mock
	private SimpleDatabaseRepository simpleDatabaseRepository;
	@InjectMocks
	private UserRepository userRepository;
	
	private HashMap<String, User> users;
	
	@BeforeEach
	public void setUp()
	{
		users = new HashMap<>();
		lenient().when(simpleDatabaseRepository.read()).thenReturn(users);
	}
	
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(UserRepository.class.getAnnotation(Repository.class));
	}
	
	//region create
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void create_When_UserDoesNotExistWithId_Then_AddsUserToUsers(String userId) throws UserAlreadyExistsException
	{
		User user = new User(userId);
		
		userRepository.create(user);
		
		assertEquals(user, users.get(userId));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void create_When_UserDoesNotExistWithId_Then_WritesUsersToDatabase(String userId) throws UserAlreadyExistsException
	{
		User user = new User(userId);
		
		userRepository.create(user);
		
		verify(simpleDatabaseRepository).write(users);
	}
	
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
		createMultipleTestUsers();
		User user = new User(userId);
		
		User actual = userRepository.create(user);
		
		assertEquals(user, actual);
	}
	
	@Test
	public void create_When_UserExistsWithId_Then_ThrowsUserAlreadyExistsException()
	{
		users.put(USER_ID, new User("123"));
		
		assertThrows(UserAlreadyExistsException.class, () -> userRepository.create(new User(USER_ID)));
	}
	
	@Test
	public void create_When_MultipleUsersExistIncludingOneWithId_Then_ThrowsUserAlreadyExistsException()
	{
		users.put(USER_ID, new User("123"));
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
	public void find_When_MultipleUsersExistButNoneWithId_Then_ThrowsUserNotFoundException()
	{
		createMultipleTestUsers();
		
		assertThrows(UserNotFoundException.class, () -> userRepository.find(USER_ID));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void find_When_UserExistsWithId_Then_ReturnsUser(String userId) throws UserNotFoundException
	{
		User user = new User("123");
		users.put(userId, user);
		
		User actual = userRepository.find(userId);
		
		assertEquals(user, actual);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void find_When_MultipleUsersExistIncludingOneWithId_Then_ReturnsUser(String userId) throws UserNotFoundException
	{
		User user = new User("123");
		users.put(userId, user);
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
	public void update_When_MultipleUsersExistButNoneWithId_Then_ThrowsUserNotFoundException()
	{
		createMultipleTestUsers();
		
		assertThrows(UserNotFoundException.class, () -> userRepository.update(new User(USER_ID)));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void create_When_UserExistsWithId_Then_AddsUserToUsers(String userId) throws UserNotFoundException
	{
		User oldUser = new User("123");
		users.put(userId, oldUser);
		User newUser = new User(userId);
		
		userRepository.update(newUser);
		
		assertEquals(newUser, users.get(userId));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void create_When_UserExistsWithId_Then_WritesUsersToDatabase(String userId) throws UserNotFoundException
	{
		User oldUser = new User("123");
		users.put(userId, oldUser);
		User user = new User(userId);
		
		userRepository.update(user);
		
		verify(simpleDatabaseRepository).write(users);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void update_When_UserExistsWithId_Then_ReturnsUpdatedUser(String userId) throws UserNotFoundException
	{
		users.put(userId, new User("123"));
		
		User updatedUser = new User(userId);
		User actual = userRepository.update(updatedUser);
		
		assertEquals(updatedUser, actual);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void update_When_MultipleUsersExistIncludingOneWithId_Then_ReturnsUser(String userId) throws UserNotFoundException
	{
		users.put(USER_ID, new User("123"));
		createMultipleTestUsers();
		
		User updatedUser = new User(userId);
		User actual = userRepository.update(updatedUser);
		
		assertEquals(updatedUser, actual);
	}
	
	//endregion
	
	private void createMultipleTestUsers()
	{
		users.put("111111111", new User("111111111"));
		users.put("222222222", new User("222222222"));
		users.put("333333333", new User("333333333"));
		users.put("444444444", new User("444444444"));
	}
}