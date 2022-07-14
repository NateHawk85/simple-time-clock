package com.hawkins.simpletimeclock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Only Administrators may view report data")
public class AccessDeniedException extends Exception
{
}