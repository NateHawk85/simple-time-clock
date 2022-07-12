package com.hawkins.simpletimeclock.service;

import com.hawkins.simpletimeclock.domain.Break;
import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.domain.WorkShift;
import com.hawkins.simpletimeclock.enums.BreakType;
import com.hawkins.simpletimeclock.exception.*;
import com.hawkins.simpletimeclock.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests
{
	private static final String USER_ID = "987654321";
	private static final LocalDateTime START_TIME = LocalDateTime.of(2022, 12, 31, 12, 30);
	private static final LocalDateTime END_TIME = LocalDateTime.of(2022, 12, 31, 20, 29);
	
	@Captor
	private ArgumentCaptor<User> userCaptor;
	
	@Mock
	private UserRepository userRepository;
	@Mock
	private Clock clock;
	@InjectMocks
	private UserService userService;
	
	private User user;
	
	@BeforeEach
	public void setUp() throws UserNotFoundException, UserAlreadyExistsException
	{
		user = new User(USER_ID);
		lenient().when(userRepository.create(any())).thenReturn(user);
		lenient().when(userRepository.find(anyString())).thenReturn(user);
		lenient().when(clock.now()).thenReturn(START_TIME);
	}
	
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(UserService.class.getAnnotation(Service.class));
	}
	
	//region createUser
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void createUser_CallsUserRepositoryWithUserWithId(String userId) throws UserAlreadyExistsException
	{
		userService.createUser(userId);
		
		verify(userRepository).create(userCaptor.capture());
		assertEquals(userId, userCaptor.getValue().getUserId());
	}
	
	@Test
	public void createUser_When_UserRepositoryThrowsUserAlreadyExistsException_Then_ThrowsSameException() throws UserAlreadyExistsException
	{
		when(userRepository.create(any())).thenThrow(new UserAlreadyExistsException());
		
		assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(USER_ID));
	}
	
	@Test
	public void createUser_ReturnsWhatUserRepositoryReturns() throws UserAlreadyExistsException
	{
		User actual = userService.createUser(USER_ID);
		
		assertEquals(user, actual);
	}
	
	//endregion
	
	//region findUser
	
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
	
	//endregion
	
	//region startShift
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void startShift_CallsUserRepositoryForUser(String userId) throws UserNotFoundException, WorkShiftAlreadyStartedException
	{
		userService.startShift(userId);
		
		verify(userRepository).find(userId);
	}
	
	@Test
	public void startShift_When_UserRepositoryFindThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.find(anyString())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.startShift(USER_ID));
	}
	
	@Test
	public void startShift_When_CurrentWorkShiftExists_Then_ThrowsShiftAlreadyStartedException()
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		assertThrows(WorkShiftAlreadyStartedException.class, () -> userService.startShift(USER_ID));
	}
	
	@Test
	public void startShift_When_NoCurrentWorkShiftExists_Then_CreatesNewWorkShiftWithCurrentTime() throws WorkShiftAlreadyStartedException,
																										  UserNotFoundException
	{
		userService.startShift(USER_ID);
		
		assertEquals(START_TIME, user.getCurrentWorkShift().getStartTime());
	}
	
	@Test
	public void startShift_When_NoCurrentWorkShiftExists_Then_CallsUserRepository() throws WorkShiftAlreadyStartedException, UserNotFoundException
	{
		userService.startShift(USER_ID);
		
		verify(userRepository).update(user);
	}
	
	@Test
	public void startShift_When_UserRepositorySaveThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.update(any())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.startShift(USER_ID));
	}
	
	//endregion
	
	//region endShift
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void endShift_CallsUserRepositoryForUser(String userId) throws UserNotFoundException, WorkShiftNotStartedException
	{
		WorkShift currentWorkShift = new WorkShift(START_TIME);
		user.setCurrentWorkShift(currentWorkShift);
		userService.endShift(userId);
		
		verify(userRepository).find(userId);
	}
	
	@Test
	public void endShift_When_UserRepositoryFindThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.find(anyString())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.endShift(USER_ID));
	}
	
	@Test
	public void endShift_When_NoCurrentWorkShiftExists_Then_ThrowsWorkShiftNotStartedException()
	{
		assertThrows(WorkShiftNotStartedException.class, () -> userService.endShift(USER_ID));
	}
	
	@Test
	public void endShift_When_CurrentWorkShiftExists_Then_AddsCurrentWorkShiftToPriorWorkShifts() throws UserNotFoundException, WorkShiftNotStartedException
	{
		WorkShift currentWorkShift = new WorkShift(START_TIME);
		user.setCurrentWorkShift(currentWorkShift);
		
		userService.endShift(USER_ID);
		
		assertEquals(singletonList(currentWorkShift), user.getPriorWorkShifts());
	}
	
	@Test
	public void endShift_When_CurrentWorkShiftExists_Then_SetsEndTimeOnCurrentWorkShift() throws UserNotFoundException, WorkShiftNotStartedException
	{
		when(clock.now()).thenReturn(END_TIME);
		WorkShift currentWorkShift = new WorkShift(START_TIME);
		user.setCurrentWorkShift(currentWorkShift);
		
		userService.endShift(USER_ID);
		
		assertEquals(END_TIME, currentWorkShift.getEndTime());
	}
	
	@Test
	public void endShift_When_CurrentWorkShiftExists_Then_SetsCurrentWorkShiftToNull() throws WorkShiftNotStartedException, UserNotFoundException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		userService.endShift(USER_ID);
		
		assertNull(user.getCurrentWorkShift());
	}
	
	@Test
	public void endShift_When_CurrentWorkShiftExists_Then_CallsUserRepository() throws WorkShiftNotStartedException, UserNotFoundException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		userService.endShift(USER_ID);
		
		verify(userRepository).update(user);
	}
	
	@Test
	public void endShift_When_UserRepositorySaveThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		when(userRepository.update(any())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.endShift(USER_ID));
	}
	
	//endregion
	
	//region startBreak
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void startBreak_CallsUserRepositoryForUser(String userId) throws UserNotFoundException, BreakAlreadyStartedException
	{
		userService.startBreak(userId, BreakType.Break);
		
		verify(userRepository).find(userId);
	}
	
	@Test
	public void startBreak_When_UserRepositoryFindThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.find(anyString())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.startBreak(USER_ID, BreakType.Break));
	}
	
	//region Break
	
	@Test
	public void startBreak_When_CurrentBreakExistsAndBreakTypeIsBreak_Then_ThrowsBreakAlreadyStartedException()
	{
		user.setCurrentBreak(new Break(BreakType.Break, START_TIME));
		
		assertThrows(BreakAlreadyStartedException.class, () -> userService.startBreak(USER_ID, BreakType.Break));
	}
	
	@Test
	public void startBreak_When_NoCurrentBreakExists_Then_CreatesNewBreakWithCurrentTimeAndBreakType() throws BreakAlreadyStartedException,
																											  UserNotFoundException
	{
		userService.startBreak(USER_ID, BreakType.Break);
		
		assertNull(user.getCurrentLunchBreak());
		assertEquals(START_TIME, user.getCurrentBreak().getStartTime());
		assertEquals(BreakType.Break, user.getCurrentBreak().getBreakType());
	}
	
	@Test
	public void startBreak_When_CurrentLunchBreakExistsAndBreakTypeIsBreak_Then_CreatesNewBreakWithCurrentTimeAndBreakType() throws UserNotFoundException,
																																	BreakAlreadyStartedException
	{
		user.setCurrentLunchBreak(new Break(BreakType.Lunch, START_TIME));
		
		userService.startBreak(USER_ID, BreakType.Break);
		
		assertEquals(START_TIME, user.getCurrentBreak().getStartTime());
		assertEquals(BreakType.Break, user.getCurrentBreak().getBreakType());
	}
	
	@Test
	public void startBreak_When_NoCurrentBreakExists_Then_CallsUserRepository() throws BreakAlreadyStartedException, UserNotFoundException
	{
		userService.startBreak(USER_ID, BreakType.Break);
		
		verify(userRepository).update(user);
	}
	
	//endregion
	
	//region Lunch
	
	@Test
	public void startBreak_When_CurrentLunchBreakExistsAndBreakTypeIsLunch_Then_ThrowsBreakAlreadyStartedException()
	{
		user.setCurrentLunchBreak(new Break(BreakType.Lunch, START_TIME));
		
		assertThrows(BreakAlreadyStartedException.class, () -> userService.startBreak(USER_ID, BreakType.Lunch));
	}
	
	@Test
	public void startBreak_When_NoCurrentLunchBreakExists_Then_CreatesNewLunchBreakWithCurrentTimeAndLunchType() throws BreakAlreadyStartedException,
																														UserNotFoundException
	{
		userService.startBreak(USER_ID, BreakType.Lunch);
		
		assertNull(user.getCurrentBreak());
		assertEquals(START_TIME, user.getCurrentLunchBreak().getStartTime());
		assertEquals(BreakType.Lunch, user.getCurrentLunchBreak().getBreakType());
	}
	
	@Test
	public void startBreak_When_CurrentBreakExistsAndBreakTypeIsLunch_Then_CreatesNewLunchBreakWithCurrentTimeAndLunchType()
			throws BreakAlreadyStartedException, UserNotFoundException
	{
		user.setCurrentBreak(new Break(BreakType.Break, START_TIME));
		
		userService.startBreak(USER_ID, BreakType.Lunch);
		
		assertEquals(START_TIME, user.getCurrentLunchBreak().getStartTime());
		assertEquals(BreakType.Lunch, user.getCurrentLunchBreak().getBreakType());
	}
	
	@Test
	public void startBreak_When_NoCurrentLunchBreakExists_Then_CallsUserRepository() throws BreakAlreadyStartedException, UserNotFoundException
	{
		userService.startBreak(USER_ID, BreakType.Lunch);
		
		verify(userRepository).update(user);
	}
	
	//endregion
	
	@Test
	public void startBreak_When_UserRepositorySaveThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.update(any())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.startBreak(USER_ID, BreakType.Break));
	}
	
	//endregion
	
	//region endBreak
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void endBreak_CallsUserRepositoryForUser(String userId) throws UserNotFoundException, BreakNotStartedException
	{
		Break currentBreak = new Break(BreakType.Break, START_TIME);
		user.setCurrentBreak(currentBreak);
		userService.endBreak(userId);

		verify(userRepository).find(userId);
	}

	@Test
	public void endBreak_When_UserRepositoryFindThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.find(anyString())).thenThrow(new UserNotFoundException());

		assertThrows(UserNotFoundException.class, () -> userService.endBreak(USER_ID));
	}

	@Test
	public void endBreak_When_NoCurrentBreakExists_Then_ThrowsBreakNotStartedException()
	{
		assertThrows(BreakNotStartedException.class, () -> userService.endBreak(USER_ID));
	}
	
	//region Break
	
	@Test
	public void endBreak_When_CurrentBreakExists_Then_AddsCurrentBreakToPriorBreaks() throws UserNotFoundException, BreakNotStartedException
	{
		Break currentBreak = new Break(BreakType.Break, START_TIME);
		user.setCurrentBreak(currentBreak);
		
		userService.endBreak(USER_ID);
		
		assertEquals(singletonList(currentBreak), user.getPriorBreaks());
	}
	
	@Test
	public void endBreak_When_CurrentBreakExists_Then_SetsEndTimeOnCurrentBreak() throws UserNotFoundException, BreakNotStartedException
	{
		when(clock.now()).thenReturn(END_TIME);
		Break currentBreak = new Break(BreakType.Break, START_TIME);
		user.setCurrentBreak(currentBreak);
		
		userService.endBreak(USER_ID);
		
		assertEquals(END_TIME, currentBreak.getEndTime());
	}
	
	@Test
	public void endBreak_When_CurrentBreakExists_Then_SetsCurrentBreakToNull() throws BreakNotStartedException, UserNotFoundException
	{
		user.setCurrentBreak(new Break(BreakType.Break, START_TIME));
		
		userService.endBreak(USER_ID);
		
		assertNull(user.getCurrentBreak());
	}
	
	@Test
	public void endBreak_When_CurrentBreakExists_Then_CallsUserRepository() throws BreakNotStartedException, UserNotFoundException
	{
		user.setCurrentBreak(new Break(BreakType.Break, START_TIME));
		
		userService.endBreak(USER_ID);
		
		verify(userRepository).update(user);
	}
	
	//endregion
	
	//region Lunch
	
	@Test
	public void endBreak_When_CurrentLunchBreakExists_Then_AddsCurrentLunchBreakToPriorBreaks() throws UserNotFoundException, BreakNotStartedException
	{
		Break currentBreak = new Break(BreakType.Lunch, START_TIME);
		user.setCurrentLunchBreak(currentBreak);
		
		userService.endBreak(USER_ID);
		
		assertEquals(singletonList(currentBreak), user.getPriorBreaks());
	}
	
	@Test
	public void endBreak_When_CurrentLunchBreakExists_Then_SetsEndTimeOnCurrentLunchBreak() throws UserNotFoundException, BreakNotStartedException
	{
		when(clock.now()).thenReturn(END_TIME);
		Break currentBreak = new Break(BreakType.Lunch, START_TIME);
		user.setCurrentLunchBreak(currentBreak);
		
		userService.endBreak(USER_ID);
		
		assertEquals(END_TIME, currentBreak.getEndTime());
	}
	
	@Test
	public void endBreak_When_CurrentLunchBreakExists_Then_SetsCurrentLunchBreakToNull() throws BreakNotStartedException, UserNotFoundException
	{
		user.setCurrentLunchBreak(new Break(BreakType.Lunch, START_TIME));
		
		userService.endBreak(USER_ID);
		
		assertNull(user.getCurrentBreak());
	}
	
	@Test
	public void endBreak_When_CurrentLunchBreakExists_Then_CallsUserRepository() throws BreakNotStartedException, UserNotFoundException
	{
		user.setCurrentLunchBreak(new Break(BreakType.Lunch, START_TIME));
		
		userService.endBreak(USER_ID);
		
		verify(userRepository).update(user);
	}
	
	//endregion
	
	@Test
	public void endBreak_When_BothBreaksExists_Then_AddsCurrentBreakToPriorBreaks() throws UserNotFoundException, BreakNotStartedException
	{
		Break currentBreak = new Break(BreakType.Break, START_TIME);
		user.setCurrentBreak(currentBreak);
		Break currentLunchBreak = new Break(BreakType.Break, START_TIME);
		user.setCurrentLunchBreak(currentLunchBreak);
		
		userService.endBreak(USER_ID);
		
		assertEquals(singletonList(currentBreak), user.getPriorBreaks());
		assertEquals(currentLunchBreak, user.getCurrentLunchBreak());
	}

	@Test
	public void endBreak_When_UserRepositorySaveThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		user.setCurrentBreak(new Break(BreakType.Break, START_TIME));

		when(userRepository.update(any())).thenThrow(new UserNotFoundException());

		assertThrows(UserNotFoundException.class, () -> userService.endBreak(USER_ID));
	}
	
	//endregion
}