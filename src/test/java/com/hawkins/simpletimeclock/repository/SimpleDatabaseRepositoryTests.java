package com.hawkins.simpletimeclock.repository;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimpleDatabaseRepositoryTests
{
	@Test
	public void class_HasCorrectAnnotations()
	{
		assertNotNull(SimpleDatabaseRepository.class.getAnnotation(Repository.class));
	}
}