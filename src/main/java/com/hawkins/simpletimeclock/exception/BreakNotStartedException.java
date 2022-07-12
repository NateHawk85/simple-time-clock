package com.hawkins.simpletimeclock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Break Not Started")
public class BreakNotStartedException extends Exception
{
}