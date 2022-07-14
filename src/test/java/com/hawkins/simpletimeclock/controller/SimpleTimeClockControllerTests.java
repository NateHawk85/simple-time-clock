package com.hawkins.simpletimeclock.controller;

import com.hawkins.simpletimeclock.domain.ReportDataFilters;
import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.enums.BreakType;
import com.hawkins.simpletimeclock.enums.Role;
import com.hawkins.simpletimeclock.exception.*;
import com.hawkins.simpletimeclock.service.ContextURIService;
import com.hawkins.simpletimeclock.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SimpleTimeClockController.class)
public class SimpleTimeClockControllerTests
{
	private static final String USER_ID = "987654321";
	private static final String NAME = "Anna";
	private static final String CONTEXT_BASE_URI = "http://localhost:8080/simple-time-clock";
	
	@Captor
	private ArgumentCaptor<ReportDataFilters> filtersCaptor;
	
	@MockBean
	private UserService userService;
	@MockBean
	private ContextURIService contextURIService;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private SimpleTimeClockController controller;
	
	private Map<String, User> users;
	
	@BeforeEach
	public void setUp() throws AccessDeniedException, UserNotFoundException
	{
		users = new HashMap<>();
		when(contextURIService.fullContextPath()).thenReturn(CONTEXT_BASE_URI);
		lenient().when(userService.findUserActivity(anyString(), any())).thenReturn(users);
	}
	
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(SimpleTimeClockController.class.getAnnotation(RestController.class));
	}
	
	//region createUser
	
	@Test
	public void createUser_EndpointExists() throws Exception
	{
		mockMvc.perform(post("/user/987654321"))
				.andExpect(status().isCreated());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void createUser_CallsUserService(String userId) throws UserAlreadyExistsException
	{
		controller.createUser(userId);
		
		verify(userService).createUser(userId);
	}
	
	@Test
	public void createUser_When_UserServiceReturnsUser_Then_ReturnsWhatUserServiceReturnsInBody() throws UserAlreadyExistsException
	{
		User expectedUser = new User();
		when(userService.createUser(anyString())).thenReturn(expectedUser);
		ResponseEntity<User> actual = controller.createUser(USER_ID);
		
		assertEquals(expectedUser, actual.getBody());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void createUser_When_UserServiceReturnsUser_Then_SetsCorrectLocationHeaderBasedOnUserId(String userId) throws UserAlreadyExistsException
	{
		ResponseEntity<User> actual = controller.createUser(userId);
		
		assertEquals(singletonList(CONTEXT_BASE_URI + "/user/" + userId), actual.getHeaders().get(HttpHeaders.LOCATION));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {CONTEXT_BASE_URI, "https://github.com"})
	public void createUser_When_UserServiceReturnsUser_Then_SetsCorrectLocationHeaderBasedOnContextURI(String contextUri) throws UserAlreadyExistsException
	{
		when(contextURIService.fullContextPath()).thenReturn(contextUri);
		
		ResponseEntity<User> actual = controller.createUser(USER_ID);
		
		assertEquals(singletonList(contextUri + "/user/" + USER_ID), actual.getHeaders().get(HttpHeaders.LOCATION));
	}
	
	@Test
	public void createUser_When_UserServiceThrowsUserAlreadyExistsException_Then_ThrowsSameException() throws UserAlreadyExistsException
	{
		when(userService.createUser(anyString())).thenThrow(new UserAlreadyExistsException());
		
		assertThrows(UserAlreadyExistsException.class, () -> controller.createUser(USER_ID));
	}
	
	//endregion
	
	//region findUserActivity
	
	@Test
	public void findUserActivity_EndpointExists() throws Exception
	{
		mockMvc.perform(get("/admin/987654321/userActivity"))
				.andExpect(status().isOk());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void findUserActivity_CallsUserService(String userId) throws AccessDeniedException, UserNotFoundException
	{
		controller.findUserActivity(userId, "1234", 2, 3, true, false, Role.Administrator);
		
		verify(userService).findUserActivity(eq(userId), filtersCaptor.capture());
		ReportDataFilters filters = filtersCaptor.getValue();
		assertEquals("1234", filters.getUserIdToView());
		assertEquals(2, filters.getPriorWorkShiftsThreshold());
		assertEquals(3, filters.getPriorBreaksThreshold());
		assertTrue(filters.isCurrentlyOnBreak());
		assertFalse(filters.isCurrentlyOnLunch());
		assertEquals(Role.Administrator, filters.getRoleToView());
	}
	
	@Test
	public void findUserActivity_SetsFiltersOnValuesPassedToUserService() throws AccessDeniedException, UserNotFoundException
	{
		controller.findUserActivity(USER_ID, "12345", 0, 1, false, true, Role.NonAdministrator);
		
		verify(userService).findUserActivity(eq(USER_ID), filtersCaptor.capture());
		ReportDataFilters filters = filtersCaptor.getValue();
		assertEquals("12345", filters.getUserIdToView());
		assertEquals(0, filters.getPriorWorkShiftsThreshold());
		assertEquals(1, filters.getPriorBreaksThreshold());
		assertFalse(filters.isCurrentlyOnBreak());
		assertTrue(filters.isCurrentlyOnLunch());
		assertEquals(Role.NonAdministrator, filters.getRoleToView());
	}
	
	@Test
	public void findUserActivity_SetsFiltersOnNullValuesPassedToUserService() throws AccessDeniedException, UserNotFoundException
	{
		controller.findUserActivity(USER_ID, null, 0, 0, false, false, null);
		
		verify(userService).findUserActivity(eq(USER_ID), filtersCaptor.capture());
		ReportDataFilters filters = filtersCaptor.getValue();
		assertNull(filters.getUserIdToView());
		assertEquals(0, filters.getPriorWorkShiftsThreshold());
		assertEquals(0, filters.getPriorBreaksThreshold());
		assertFalse(filters.isCurrentlyOnBreak());
		assertFalse(filters.isCurrentlyOnLunch());
		assertNull(filters.getRoleToView());
	}
	
	@Test
	public void findUserActivity_When_UserServiceReturnsUser_Then_ReturnsWhatUserServiceReturnsInBody() throws AccessDeniedException, UserNotFoundException
	{
		when(userService.findUserActivity(anyString(), any())).thenReturn(users);
		ResponseEntity<Map<String, User>> actual = controller.findUserActivity(USER_ID, "1234", 2, 3, true, false, Role.Administrator);
		
		assertEquals(users, actual.getBody());
	}
	
	@Test
	public void findUserActivity_When_UserServiceThrowsUserNotFoundException_Then_ThrowsSameException() throws AccessDeniedException, UserNotFoundException
	{
		when(userService.findUserActivity(anyString(), any())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> controller.findUserActivity(USER_ID, "1234", 2, 3, true, false, Role.Administrator));
	}
	
	@Test
	public void findUserActivity_When_UserServiceThrowsAccessDeniedException_Then_ThrowsSameException() throws AccessDeniedException, UserNotFoundException
	{
		when(userService.findUserActivity(anyString(), any())).thenThrow(new AccessDeniedException());
		
		assertThrows(AccessDeniedException.class, () -> controller.findUserActivity(USER_ID, "1234", 2, 3, true, false, Role.Administrator));
	}
	
	//endregion
	
	//region findUser
	
	@Test
	public void updateUser_EndpointExists() throws Exception
	{
		mockMvc.perform(post("/user/987654321/update"))
				.andExpect(status().isAccepted());
	}
	
	@Test
	public void updateUser_EndpointExistsWithParameters() throws Exception
	{
		mockMvc.perform(post("/user/987654321/update?name=Bob&role=Administrator"))
				.andExpect(status().isAccepted());
	}
	
	@Test
	public void updateUser_CallsUserService() throws UserNotFoundException
	{
		controller.updateUser(USER_ID, NAME, Role.Administrator);
		
		verify(userService).updateUser(USER_ID, NAME, Role.Administrator);
	}
	
	@Test
	public void updateUser_When_UserServiceReturnsUser_Then_ReturnsWhatUserServiceReturnsInBody() throws UserNotFoundException
	{
		User expectedUser = new User();
		when(userService.updateUser(anyString(), anyString(), any())).thenReturn(expectedUser);
		ResponseEntity<User> actual = controller.updateUser(USER_ID, NAME, Role.Administrator);
		
		assertEquals(expectedUser, actual.getBody());
	}
	
	@Test
	public void updateUser_When_UserServiceThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userService.updateUser(anyString(), anyString(), any())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> controller.updateUser(USER_ID, NAME, Role.Administrator));
	}
	
	//endregion
	
	//region startShift
	
	@Test
	public void startShift_EndpointExists() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post("/user/987654321/startShift"))
				.andExpect(status().isAccepted());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void startShift_CallsUserService(String userId) throws UserNotFoundException, WorkShiftInProgressException
	{
		controller.startShift(userId);
		
		verify(userService).startShift(userId);
	}
	
	@Test
	public void startShift_When_UserServiceThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException, WorkShiftInProgressException
	{
		doThrow(new UserNotFoundException()).when(userService).startShift(anyString());
		
		assertThrows(UserNotFoundException.class, () -> controller.startShift(USER_ID));
	}
	
	@Test
	public void startShift_When_UserServiceThrowsWorkShiftInProgressException_Then_ThrowsSameException()
			throws UserNotFoundException, WorkShiftInProgressException
	{
		doThrow(new WorkShiftInProgressException()).when(userService).startShift(anyString());
		
		assertThrows(WorkShiftInProgressException.class, () -> controller.startShift(USER_ID));
	}
	
	//endregion
	
	//region endShift
	
	@Test
	public void endShift_EndpointExists() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post("/user/987654321/endShift"))
				.andExpect(status().isAccepted());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void endShift_CallsUserService(String userId) throws UserNotFoundException, WorkShiftNotStartedException, BreakInProgressException
	{
		controller.endShift(userId);
		
		verify(userService).endShift(userId);
	}
	
	@Test
	public void endShift_When_UserServiceThrowsUserNotFoundException_Then_ThrowsSameException()
			throws UserNotFoundException, WorkShiftNotStartedException, BreakInProgressException
	{
		doThrow(new UserNotFoundException()).when(userService).endShift(anyString());
		
		assertThrows(UserNotFoundException.class, () -> controller.endShift(USER_ID));
	}
	
	@Test
	public void endShift_When_UserServiceThrowsWorkShiftNotStartedException_Then_ThrowsSameException()
			throws UserNotFoundException, WorkShiftNotStartedException, BreakInProgressException
	{
		doThrow(new WorkShiftNotStartedException()).when(userService).endShift(anyString());
		
		assertThrows(WorkShiftNotStartedException.class, () -> controller.endShift(USER_ID));
	}
	
	@Test
	public void endShift_When_UserServiceThrowsBreakInProgressException_Then_ThrowsSameException()
			throws UserNotFoundException, WorkShiftNotStartedException, BreakInProgressException
	{
		doThrow(new BreakInProgressException()).when(userService).endShift(anyString());
		
		assertThrows(BreakInProgressException.class, () -> controller.endShift(USER_ID));
	}
	
	//endregion
	
	//region startBreak
	
	@Test
	public void startBreak_EndpointExists() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post("/user/987654321/startBreak"))
				.andExpect(status().isAccepted());
	}
	
	@Test
	public void startBreak_EndpointExistsWithOptionalParameters() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post("/user/987654321/startBreak?breakType=Break"))
				.andExpect(status().isAccepted());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void startBreak_CallsUserService_NullBreakType(String userId) throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		controller.startBreak(userId, null);
		
		verify(userService).startBreak(userId, BreakType.Break);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void startBreak_CallsUserService_Break(String userId) throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		controller.startBreak(userId, BreakType.Break);
		
		verify(userService).startBreak(userId, BreakType.Break);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void startBreak_CallsUserService_Lunch(String userId) throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		controller.startBreak(userId, BreakType.Lunch);
		
		verify(userService).startBreak(userId, BreakType.Lunch);
	}
	
	@Test
	public void startBreak_When_UserServiceThrowsUserNotFoundException_Then_ThrowsSameException()
			throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		doThrow(new UserNotFoundException()).when(userService).startBreak(anyString(), any());
		
		assertThrows(UserNotFoundException.class, () -> controller.startBreak(USER_ID, BreakType.Break));
	}
	
	@Test
	public void startBreak_When_UserServiceThrowsBreakInProgressException_Then_ThrowsSameException()
			throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		doThrow(new BreakInProgressException()).when(userService).startBreak(anyString(), any());
		
		assertThrows(BreakInProgressException.class, () -> controller.startBreak(USER_ID, BreakType.Break));
	}
	
	@Test
	public void startBreak_When_UserServiceThrowsWorkShiftNotStartedException_Then_ThrowsSameException()
			throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		doThrow(new WorkShiftNotStartedException()).when(userService).startBreak(anyString(), any());
		
		assertThrows(WorkShiftNotStartedException.class, () -> controller.startBreak(USER_ID, BreakType.Break));
	}
	
	//endregion
	
	//region endBreak
	
	@Test
	public void endBreak_EndpointExists() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post("/user/987654321/endBreak"))
				.andExpect(status().isAccepted());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void endBreak_CallsUserService(String userId) throws UserNotFoundException, BreakNotStartedException
	{
		controller.endBreak(userId);
		
		verify(userService).endBreak(userId);
	}
	
	@Test
	public void endBreak_When_UserServiceThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException, BreakNotStartedException
	{
		doThrow(new UserNotFoundException()).when(userService).endBreak(anyString());
		
		assertThrows(UserNotFoundException.class, () -> controller.endBreak(USER_ID));
	}
	
	@Test
	public void endBreak_When_UserServiceThrowsBreakNotStartedException_Then_ThrowsSameException() throws UserNotFoundException, BreakNotStartedException
	{
		doThrow(new BreakNotStartedException()).when(userService).endBreak(anyString());
		
		assertThrows(BreakNotStartedException.class, () -> controller.endBreak(USER_ID));
	}
	
	//endregion
}