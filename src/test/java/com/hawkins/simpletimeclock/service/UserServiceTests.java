package com.hawkins.simpletimeclock.service;

import com.hawkins.simpletimeclock.domain.Break;
import com.hawkins.simpletimeclock.domain.ReportDataFilters;
import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.domain.WorkShift;
import com.hawkins.simpletimeclock.enums.BreakType;
import com.hawkins.simpletimeclock.enums.Role;
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
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests
{
	private static final String USER_ID = "987654321";
	private static final String NAME = "Anna";
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
	private ReportDataFilters filters;
	private Map<String, User> users;
	
	@BeforeEach
	public void setUp() throws UserNotFoundException, UserAlreadyExistsException
	{
		user = new User(USER_ID);
		user.setRole(Role.Administrator);
		users = new HashMap<>();
		users.put(USER_ID, user);
		filters = new ReportDataFilters();
		lenient().when(userRepository.create(any())).thenReturn(user);
		lenient().when(userRepository.find(anyString())).thenReturn(user);
		lenient().when(userRepository.findAllUsers()).thenReturn(users);
		lenient().when(userRepository.update(any())).thenReturn(user);
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
	public void createUser_When_UserExists_Then_SetsUserRoleToNonAdministrator() throws UserAlreadyExistsException
	{
		user.setRole(Role.Administrator);
		
		userService.createUser(USER_ID);
		
		verify(userRepository).create(userCaptor.capture());
		
		assertEquals(Role.NonAdministrator, userCaptor.getValue().getRole());
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
	
	//region findUserActivity
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void findUserActivity_CallsUserRepositoryFind(String userId) throws AccessDeniedException, UserNotFoundException
	{
		userService.findUserActivity(userId, filters);
		
		verify(userRepository).find(userId);
	}
	
	@Test
	public void findUserActivity_When_UserRepositoryFindThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.find(anyString())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.findUserActivity(USER_ID, filters));
	}
	
	@Test
	public void findUserActivity_When_UserIsNotAdministrator_Then_ThrowsAccessDeniedException()
	{
		user.setRole(null);
		
		assertThrows(AccessDeniedException.class, () -> userService.findUserActivity(USER_ID, filters));
		
		user.setRole(Role.NonAdministrator);
		
		assertThrows(AccessDeniedException.class, () -> userService.findUserActivity(USER_ID, filters));
	}
	
	@Test
	public void findUserActivity_When_UserIsAdministrator_Then_ReturnsWhatUserRepositoryReturns() throws AccessDeniedException, UserNotFoundException
	{
		Map<String, User> actual = userService.findUserActivity(USER_ID, filters);
		
		assertEquals(users, actual);
	}
	
	@Test
	public void findUserActivity_When_UserIsAdministratorAndUserIdFilterExists_Then_OnlyShowsUsersWithGivenUserId()
			throws UserNotFoundException, AccessDeniedException
	{
		filters.setUserIdToView(USER_ID);
		users.put("111", new User("111"));
		users.put("222", new User("222"));
		users.put("333", new User("333"));
		
		Map<String, User> actual = userService.findUserActivity(USER_ID, filters);
		
		assertEquals(singletonMap(USER_ID, user), actual);
	}
	
	@Test
	public void findUserActivity_When_UserIsAdministratorAndPriorWorkShiftsThresholdExists_Then_OnlyShowsUsersWithNumberOfThresholdsOrGreater()
			throws UserNotFoundException, AccessDeniedException
	{
		filters.setPriorWorkShiftsThreshold(1);
		users.put("111", new User("111"));
		users.put("222", new User("222"));
		users.put("333", new User("333"));
		User oneShift = new User("444");
		oneShift.getPriorWorkShifts().add(new WorkShift(LocalDateTime.now()));
		users.put("444", oneShift);
		User twoShifts = new User("555");
		twoShifts.getPriorWorkShifts().add(new WorkShift(LocalDateTime.now()));
		users.put("555", twoShifts);
		
		Map<String, User> actual = userService.findUserActivity(USER_ID, filters);
		
		Map<String, User> expected = new HashMap<>();
		expected.put("444", oneShift);
		expected.put("555", twoShifts);
		assertEquals(expected, actual);
	}
	
	@Test
	public void findUserActivity_When_UserIsAdministratorAndPriorBreaksThresholdExists_Then_OnlyShowsUsersWithNumberOfThresholdsOrGreater()
			throws UserNotFoundException, AccessDeniedException
	{
		filters.setPriorBreaksThreshold(1);
		users.put("111", new User("111"));
		users.put("222", new User("222"));
		users.put("333", new User("333"));
		User oneShift = new User("444");
		oneShift.getPriorBreaks().add(new Break(BreakType.Break, LocalDateTime.now()));
		users.put("444", oneShift);
		User twoShifts = new User("555");
		twoShifts.getPriorBreaks().add(new Break(BreakType.Break, LocalDateTime.now()));
		users.put("555", twoShifts);
		
		Map<String, User> actual = userService.findUserActivity(USER_ID, filters);
		
		Map<String, User> expected = new HashMap<>();
		expected.put("444", oneShift);
		expected.put("555", twoShifts);
		assertEquals(expected, actual);
	}
	
	@Test
	public void findUserActivity_When_UserIsAdministratorAndIsCurrentlyOnBreakFilterExists_Then_OnlyShowsUsersCurrentlyOnBreak()
			throws UserNotFoundException, AccessDeniedException
	{
		filters.setCurrentlyOnBreak(true);
		users.put("111", new User("111"));
		User onBreak1 = new User("222");
		onBreak1.setCurrentBreak(new Break(BreakType.Break, LocalDateTime.now()));
		users.put("222", onBreak1);
		users.put("333", new User("333"));
		User onBreak2 = new User("444");
		onBreak2.setCurrentBreak(new Break(BreakType.Break, LocalDateTime.now()));
		users.put("444", onBreak2);
		users.put("555", new User("555"));
		
		Map<String, User> actual = userService.findUserActivity(USER_ID, filters);
		
		Map<String, User> expected = new HashMap<>();
		expected.put("222", onBreak1);
		expected.put("444", onBreak2);
		assertEquals(expected, actual);
	}
	
	@Test
	public void findUserActivity_When_UserIsAdministratorAndIsCurrentlyOnLunchBreakFilterExists_Then_OnlyShowsUsersCurrentlyOnLunchBreak()
			throws UserNotFoundException, AccessDeniedException
	{
		filters.setCurrentlyOnLunch(true);
		users.put("111", new User("111"));
		User onBreak1 = new User("222");
		onBreak1.setCurrentLunchBreak(new Break(BreakType.Lunch, LocalDateTime.now()));
		users.put("222", onBreak1);
		users.put("333", new User("333"));
		User onBreak2 = new User("444");
		onBreak2.setCurrentLunchBreak(new Break(BreakType.Lunch, LocalDateTime.now()));
		users.put("444", onBreak2);
		users.put("555", new User("555"));
		
		Map<String, User> actual = userService.findUserActivity(USER_ID, filters);
		
		Map<String, User> expected = new HashMap<>();
		expected.put("222", onBreak1);
		expected.put("444", onBreak2);
		assertEquals(expected, actual);
	}
	
	@Test
	public void findUserActivity_When_UserIsAdministratorAndRoleFilterExists_Then_OnlyShowsUsersWithGivenRole()
			throws UserNotFoundException, AccessDeniedException
	{
		filters.setRoleToView(Role.NonAdministrator);
		users.put("111", new User("111"));
		User administrator = new User("222");
		administrator.setRole(Role.Administrator);
		users.put("222", administrator);
		users.put("333", new User("333"));
		User nonAdministrator1 = new User("444");
		nonAdministrator1.setRole(Role.NonAdministrator);
		users.put("444", nonAdministrator1);
		User nonAdministrator2 = new User("555");
		nonAdministrator2.setRole(Role.NonAdministrator);
		users.put("555", nonAdministrator2);
		
		Map<String, User> actual = userService.findUserActivity(USER_ID, filters);
		
		Map<String, User> expected = new HashMap<>();
		expected.put("444", nonAdministrator1);
		expected.put("555", nonAdministrator2);
		assertEquals(expected, actual);
	}
	
	//endregion
	
	//region updateUser
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "987654321"})
	public void updateUser_CallsUserRepository(String userId) throws UserNotFoundException
	{
		userService.updateUser(userId, NAME, Role.Administrator);
		
		verify(userRepository).find(userId);
	}
	
	@Test
	public void updateUser_When_UserRepositoryThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.find(anyString())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.updateUser(USER_ID, NAME, Role.Administrator));
	}
	
	@Test
	public void updateUser_When_UserExists_Then_UpdatesNonNullFieldsOnUser() throws UserNotFoundException
	{
		user.setName("Some name");
		user.setRole(Role.NonAdministrator);
		
		userService.updateUser(USER_ID, NAME, Role.Administrator);
		
		assertEquals(NAME, user.getName());
		assertEquals(Role.Administrator, user.getRole());
	}
	
	@Test
	public void updateUser_When_UserExistsAndNameIsNull_Then_DoesNotOverrideName() throws UserNotFoundException
	{
		user.setName(NAME);
		user.setRole(Role.NonAdministrator);
		
		userService.updateUser(USER_ID, null, Role.Administrator);
		
		assertEquals(NAME, user.getName());
		assertEquals(Role.Administrator, user.getRole());
	}
	
	@Test
	public void updateUser_When_UserExistsAndRoleIsNull_Then_DoesNotOverrideRole() throws UserNotFoundException
	{
		user.setName(NAME);
		user.setRole(Role.NonAdministrator);
		
		userService.updateUser(USER_ID, "Bob", null);
		
		assertEquals("Bob", user.getName());
		assertEquals(Role.NonAdministrator, user.getRole());
	}
	
	@Test
	public void updateUser_When_UserExistsAndFieldsAreNull_Then_DoesNotOverrideFields() throws UserNotFoundException
	{
		user.setName(NAME);
		user.setRole(Role.NonAdministrator);
		
		userService.updateUser(USER_ID, null, null);
		
		assertEquals(NAME, user.getName());
		assertEquals(Role.NonAdministrator, user.getRole());
	}
	
	@Test
	public void updateUser_When_UserExists_Then_UpdatesFieldsOnUser_AltParams() throws UserNotFoundException
	{
		user.setName("Some name");
		user.setRole(Role.NonAdministrator);
		
		userService.updateUser(USER_ID, "Bob", Role.NonAdministrator);
		
		assertEquals("Bob", user.getName());
		assertEquals(Role.NonAdministrator, user.getRole());
	}
	
	@Test
	public void updateUser_When_UserExists_Then_CallsUserRepositoryToUpdate() throws UserNotFoundException
	{
		userService.updateUser(USER_ID, NAME, Role.Administrator);
		
		verify(userRepository).update(user);
	}
	
	@Test
	public void updateUser_When_UserRepositoryUpdateThrowsUserNotFoundException_Then_ThrowsSameExcetpion() throws UserNotFoundException
	{
		when(userRepository.update(any())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.updateUser(USER_ID, NAME, Role.Administrator));
	}
	
	@Test
	public void updateUser_When_UserExists_Then_ReturnsWhatUserRepositoryReturns() throws UserNotFoundException
	{
		User actual = userService.updateUser(USER_ID, NAME, Role.Administrator);
		
		assertEquals(user, actual);
	}
	
	//endregion
	
	//region startShift
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void startShift_CallsUserRepositoryForUser(String userId) throws UserNotFoundException, WorkShiftInProgressException
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
		
		assertThrows(WorkShiftInProgressException.class, () -> userService.startShift(USER_ID));
	}
	
	@Test
	public void startShift_When_NoCurrentWorkShiftExists_Then_CreatesNewWorkShiftWithCurrentTime() throws WorkShiftInProgressException, UserNotFoundException
	{
		userService.startShift(USER_ID);
		
		assertEquals(START_TIME, user.getCurrentWorkShift().getStartTime());
	}
	
	@Test
	public void startShift_When_NoCurrentWorkShiftExists_Then_CallsUserRepository() throws WorkShiftInProgressException, UserNotFoundException
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
	public void endShift_CallsUserRepositoryForUser(String userId) throws UserNotFoundException, WorkShiftNotStartedException, BreakInProgressException
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
	public void endShift_When_CurrentBreakExists_Then_ThrowsBreakInProgressException()
	{
		user.setCurrentBreak(new Break(BreakType.Break, START_TIME));
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		assertThrows(BreakInProgressException.class, () -> userService.endShift(USER_ID));
	}
	
	@Test
	public void endShift_When_CurrentLunchBreakExists_Then_ThrowsBreakInProgressException()
	{
		user.setCurrentLunchBreak(new Break(BreakType.Lunch, START_TIME));
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		assertThrows(BreakInProgressException.class, () -> userService.endShift(USER_ID));
	}
	
	@Test
	public void endShift_When_NoCurrentWorkShiftExists_Then_ThrowsWorkShiftNotStartedException()
	{
		assertThrows(WorkShiftNotStartedException.class, () -> userService.endShift(USER_ID));
	}
	
	@Test
	public void endShift_When_CurrentWorkShiftExists_Then_AddsCurrentWorkShiftToPriorWorkShifts()
			throws UserNotFoundException, WorkShiftNotStartedException, BreakInProgressException
	{
		WorkShift currentWorkShift = new WorkShift(START_TIME);
		user.setCurrentWorkShift(currentWorkShift);
		
		userService.endShift(USER_ID);
		
		assertEquals(singletonList(currentWorkShift), user.getPriorWorkShifts());
	}
	
	@Test
	public void endShift_When_CurrentWorkShiftExists_Then_SetsEndTimeOnCurrentWorkShift()
			throws UserNotFoundException, WorkShiftNotStartedException, BreakInProgressException
	{
		when(clock.now()).thenReturn(END_TIME);
		WorkShift currentWorkShift = new WorkShift(START_TIME);
		user.setCurrentWorkShift(currentWorkShift);
		
		userService.endShift(USER_ID);
		
		assertEquals(END_TIME, currentWorkShift.getEndTime());
	}
	
	@Test
	public void endShift_When_CurrentWorkShiftExists_Then_SetsCurrentWorkShiftToNull()
			throws WorkShiftNotStartedException, UserNotFoundException, BreakInProgressException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		userService.endShift(USER_ID);
		
		assertNull(user.getCurrentWorkShift());
	}
	
	@Test
	public void endShift_When_CurrentWorkShiftExists_Then_CallsUserRepository()
			throws WorkShiftNotStartedException, UserNotFoundException, BreakInProgressException
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
	public void startBreak_CallsUserRepositoryForUser(String userId) throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		userService.startBreak(userId, BreakType.Break);
		
		verify(userRepository).find(userId);
	}
	
	@Test
	public void startBreak_When_UserRepositoryFindThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.find(anyString())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> userService.startBreak(USER_ID, BreakType.Break));
	}
	
	@Test
	public void startBreak_When_NoCurrentWorkShiftExists_Then_ThrowsWorkShiftNotStartedException()
	{
		assertThrows(WorkShiftNotStartedException.class, () -> userService.startBreak(USER_ID, BreakType.Break));
	}
	
	//region Break
	
	@Test
	public void startBreak_When_CurrentBreakExistsAndBreakTypeIsBreak_Then_ThrowsBreakInProgressException()
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		user.setCurrentBreak(new Break(BreakType.Break, START_TIME));
		
		assertThrows(BreakInProgressException.class, () -> userService.startBreak(USER_ID, BreakType.Break));
	}
	
	@Test
	public void startBreak_When_NoCurrentBreakExists_Then_CreatesNewBreakWithCurrentTimeAndBreakType()
			throws BreakInProgressException, UserNotFoundException, WorkShiftNotStartedException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		userService.startBreak(USER_ID, BreakType.Break);
		
		assertNull(user.getCurrentLunchBreak());
		assertEquals(START_TIME, user.getCurrentBreak().getStartTime());
		assertEquals(BreakType.Break, user.getCurrentBreak().getBreakType());
	}
	
	@Test
	public void startBreak_When_CurrentLunchBreakExistsAndBreakTypeIsBreak_Then_CreatesNewBreakWithCurrentTimeAndBreakType()
			throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		user.setCurrentLunchBreak(new Break(BreakType.Lunch, START_TIME));
		
		userService.startBreak(USER_ID, BreakType.Break);
		
		assertEquals(START_TIME, user.getCurrentBreak().getStartTime());
		assertEquals(BreakType.Break, user.getCurrentBreak().getBreakType());
	}
	
	//endregion
	
	//region Lunch
	
	@Test
	public void startBreak_When_CurrentLunchBreakExistsAndBreakTypeIsLunch_Then_ThrowsBreakInProgressException()
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		user.setCurrentLunchBreak(new Break(BreakType.Lunch, START_TIME));
		
		assertThrows(BreakInProgressException.class, () -> userService.startBreak(USER_ID, BreakType.Lunch));
	}
	
	@Test
	public void startBreak_When_NoCurrentLunchBreakExists_Then_CreatesNewLunchBreakWithCurrentTimeAndLunchType()
			throws BreakInProgressException, UserNotFoundException, WorkShiftNotStartedException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		userService.startBreak(USER_ID, BreakType.Lunch);
		
		assertNull(user.getCurrentBreak());
		assertEquals(START_TIME, user.getCurrentLunchBreak().getStartTime());
		assertEquals(BreakType.Lunch, user.getCurrentLunchBreak().getBreakType());
	}
	
	@Test
	public void startBreak_When_CurrentBreakExistsAndBreakTypeIsLunch_Then_CreatesNewLunchBreakWithCurrentTimeAndLunchType()
			throws BreakInProgressException, UserNotFoundException, WorkShiftNotStartedException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		user.setCurrentBreak(new Break(BreakType.Break, START_TIME));
		
		userService.startBreak(USER_ID, BreakType.Lunch);
		
		assertEquals(START_TIME, user.getCurrentLunchBreak().getStartTime());
		assertEquals(BreakType.Lunch, user.getCurrentLunchBreak().getBreakType());
	}
	
	//endregion
	
	@Test
	public void startBreak_When_NoCurrentBreakExists_Then_CallsUserRepository()
			throws BreakInProgressException, UserNotFoundException, WorkShiftNotStartedException
	{
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
		userService.startBreak(USER_ID, BreakType.Break);
		
		verify(userRepository).update(user);
	}
	
	@Test
	public void startBreak_When_UserRepositorySaveThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userRepository.update(any())).thenThrow(new UserNotFoundException());
		user.setCurrentWorkShift(new WorkShift(START_TIME));
		
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