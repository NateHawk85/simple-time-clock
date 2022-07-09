package com.hawkins.simpletimeclock.controller;

import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.exception.UserNotFoundException;
import com.hawkins.simpletimeclock.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SimpleTimeClockController.class)
public class SimpleTimeClockControllerTests
{
	private static final String USER_ID = "987654321";
	
	@MockBean
	private UserService userService;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private SimpleTimeClockController controller;
	
	private User user;
	
	@BeforeEach
	public void setUp() throws UserNotFoundException
	{
		user = new User(USER_ID);
		when(userService.findUser(anyString())).thenReturn(user);
	}
	
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(SimpleTimeClockController.class.getAnnotation(RestController.class));
	}
	
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
	public void findUser_When_UserServiceReturnsUser_Then_ReturnsWhatUserServiceReturns() throws UserNotFoundException
	{
		User actual = controller.findUser(USER_ID);
		
		assertEquals(user, actual);
	}
	
	@Test
	public void findUser_When_UserServiceThrowsUserNotFoundException_Then_ThrowsSameException() throws UserNotFoundException
	{
		when(userService.findUser(anyString())).thenThrow(new UserNotFoundException());
		
		assertThrows(UserNotFoundException.class, () -> controller.findUser(USER_ID));
	}
}