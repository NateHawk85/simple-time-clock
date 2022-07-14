package com.hawkins.simpletimeclock.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.enums.Role;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Paths.get;

@Repository
public class SimpleDatabaseRepository
{
	private static final String USERS_DB_PATH = "src/main/resources/";
	private static final String USERS_DB_FILE = "users_db.json";
	private static final String USERS_DB_FULL_PATH = USERS_DB_PATH + USERS_DB_FILE;
	private static final File DATABASE_FILE = get(USERS_DB_FULL_PATH).toFile();
	
	private final ObjectMapper objectMapper;
	
	public SimpleDatabaseRepository(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
	}
	
	public void write(Map<String, User> users)
	{
		try
		{
			writeUsersToFile(users);
		} catch (IOException ignored)
		{
			throw new RuntimeException("Issue communicating with database.");
		}
	}
	
	public Map<String, User> read()
	{
		try
		{
			return objectMapper.readValue(DATABASE_FILE, new TypeReference<Map<String, User>>() {});
		} catch (IOException ignored)
		{
			throw new RuntimeException("Issue communicating with database.");
		}
		
	}
	
	private void writeUsersToFile(Map<String, User> users) throws IOException
	{
		objectMapper.writerWithDefaultPrettyPrinter()
				.writeValue(DATABASE_FILE, users);
	}
	
	@PostConstruct
	public void initializeDB() throws IOException
	{
		if (!DATABASE_FILE.canRead())
		{
			writeUsersToFile(buildDefaultUsers());
		}
	}
	
	private Map<String, User> buildDefaultUsers()
	{
		Map<String, User> defaultUsers = new HashMap<>();
		
		User anna = new User("123");
		anna.setName("Anna");
		anna.setRole(Role.NonAdministrator);
		User bob = new User("1234");
		bob.setName("Bob");
		bob.setRole(Role.Administrator);
		User charlie = new User("987654321");
		charlie.setName("Charlie");
		charlie.setRole(Role.NonAdministrator);
		
		defaultUsers.put(anna.getUserId(), anna);
		defaultUsers.put(bob.getUserId(), bob);
		defaultUsers.put(charlie.getUserId(), charlie);
		
		return defaultUsers;
	}
}