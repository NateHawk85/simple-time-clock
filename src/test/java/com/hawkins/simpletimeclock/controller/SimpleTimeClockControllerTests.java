package com.hawkins.simpletimeclock.controller;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserAlreadyExistsException;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import com.hawkins.simpletimeclock.service.ContextURIService;
import com.hawkins.simpletimeclock.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SimpleTimeClockController.class)
public class SimpleTimeClockControllerTests
{
	private static final String USER_ID = "987654321";
	private static final String CONTEXT_BASE_URI = "http://localhost:8080/simple-time-clock";
	
	@MockBean
	private UserService userService;
	@MockBean
	private ContextURIService contextURIService;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private SimpleTimeClockController controller;
	
	private User user;
	
	@BeforeEach
	public void setUp() throws UserNotFoundException, UserAlreadyExistsException
	{
		user = new User(USER_ID);
		when(userService.createUser(anyString())).thenReturn(user);
		when(userService.findUser(anyString())).thenReturn(user);
		when(contextURIService.fullContextPath()).thenReturn(CONTEXT_BASE_URI);
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
		ResponseEntity<User> actual = controller.createUser(USER_ID);
		
		assertEquals(user, actual.getBody());
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
	
	//region findUser
	
	@Test
	public void findUser_EndpointExists() throws Exception
	{
		mockMvc.perform(get("/user/987654321"))
				.andExpect(status().isOk());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {USER_ID, "123456789"})
	public void findUser_CallsUserService(String userId) throws UserNotFoundException
	{
		controller.findUser(userId);
		
		verify(userService).findUser(userId);
	}
	
	@Test
	public void findUser_When_UserServiceReturnsUser_Then_ReturnsWhatUserServiceReturnsInBody() throws UserNotFoundException
	{
		ResponseEntity<User> actual = controller.findUser(USER_ID);
		
		assertEquals(user, actual.getBody());
	}
	
	@Test
	public void findUser_When_UserServiceThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userService.findUser(anyString())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> controller.findUser(USER_ID));
	}
	
	//endregion
}