package com.hawkins.simpletimeclock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Work shift has not started")
public class WorkShiftNotStartedException extends Exception
{
}