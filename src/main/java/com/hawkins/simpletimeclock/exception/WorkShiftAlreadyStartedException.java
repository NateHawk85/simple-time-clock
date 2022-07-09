package com.hawkins.simpletimeclock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// TODO - continue here
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User Already Exists")
public class WorkShiftAlreadyStartedException extends Exception
{
}