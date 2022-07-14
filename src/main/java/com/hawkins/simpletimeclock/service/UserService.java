package com.hawkins.simpletimeclock.service;

import com.hawkins.simpletimeclock.domain.Break;
import com.hawkins.simpletimeclock.domain.ReportDataFilters;
import com.hawkins.simpletimeclock.domain.User;
import com.hawkins.simpletimeclock.domain.WorkShift;
import com.hawkins.simpletimeclock.enums.BreakType;
import com.hawkins.simpletimeclock.enums.Role;
import com.hawkins.simpletimeclock.exception.*;
import com.hawkins.simpletimeclock.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class UserService
{
	private final UserRepository userRepository;
	private final Clock clock;
	
	public UserService(UserRepository userRepository, Clock clock)
	{
		this.userRepository = userRepository;
		this.clock = clock;
	}
	
	public User createUser(String userId) throws UserAlreadyExistsException
	{
		User user = new User(userId);
		user.setRole(Role.NonAdministrator);
		
		return userRepository.create(user);
	}
	
	public User findUser(String userId) throws UserNotFoundException
	{
		return userRepository.find(userId);
	}
	
	public Map<String, User> findUserActivity(String adminUserId, ReportDataFilters filters) throws AccessDeniedException, UserNotFoundException
	{
		User adminUser = userRepository.find(adminUserId);
		
		if (adminUser.getRole() != Role.Administrator)
		{
			throw new AccessDeniedException();
		}
		
		Map<String, User> filteredUsers = userRepository.findAllUsers().entrySet().stream()
				.filter(passesUserIdFilter(filters))
				.filter(passesRoleFilter(filters))
				.filter(passesPriorWorkShiftFilter(filters))
				.filter(passesPriorBreaksFilter(filters))
				.filter(passesOnBreakFilter(filters))
				.filter(passesOnLunchFilter(filters))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		filterShiftsAndBreaksForUsers(filters, filteredUsers);
		
		return filteredUsers;
	}
	
	public User updateUser(String userId, String name, Role role) throws UserNotFoundException
	{
		User user = userRepository.find(userId);
		if (name != null)
		{
			user.setName(name);
		}
		if (role != null)
		{
			user.setRole(role);
		}
		
		return userRepository.update(user);
	}
	
	public void startShift(String userId) throws UserNotFoundException, WorkShiftInProgressException
	{
		User user = userRepository.find(userId);
		validateUserNotWorking(user);
		
		user.setCurrentWorkShift(new WorkShift(clock.now()));
		
		userRepository.update(user);
	}
	
	public void endShift(String userId) throws UserNotFoundException, WorkShiftNotStartedException, BreakInProgressException
	{
		User user = userRepository.find(userId);
		validateUserIsWorking(user);
		validateUserNotOnBreak(user);
		
		user.getCurrentWorkShift().setEndTime(clock.now());
		user.getPriorWorkShifts().add(user.getCurrentWorkShift());
		user.setCurrentWorkShift(null);
		
		userRepository.update(user);
	}
	
	public void startBreak(String userId, BreakType breakType) throws UserNotFoundException, BreakInProgressException, WorkShiftNotStartedException
	{
		User user = userRepository.find(userId);
		validateUserIsWorking(user);
		
		if (breakType == BreakType.Break)
		{
			if (user.getCurrentBreak() != null)
			{
				throw new BreakInProgressException();
			}
			
			user.setCurrentBreak(new Break(breakType, clock.now()));
		}
		if (breakType == BreakType.Lunch)
		{
			if (user.getCurrentLunchBreak() != null)
			{
				throw new BreakInProgressException();
			}
			
			user.setCurrentLunchBreak(new Break(breakType, clock.now()));
		}
		
		userRepository.update(user);
	}
	
	public void endBreak(String userId) throws UserNotFoundException, BreakNotStartedException
	{
		User user = userRepository.find(userId);
		
		if (user.getCurrentBreak() != null)
		{
			user.getCurrentBreak().setEndTime(clock.now());
			user.getPriorBreaks().add(user.getCurrentBreak());
			user.setCurrentBreak(null);
		} else if (user.getCurrentLunchBreak() != null)
		{
			user.getCurrentLunchBreak().setEndTime(clock.now());
			user.getPriorBreaks().add(user.getCurrentLunchBreak());
			user.setCurrentLunchBreak(null);
		} else
		{
			throw new BreakNotStartedException();
		}
		
		userRepository.update(user);
	}
	
	private void validateUserIsWorking(User user) throws WorkShiftNotStartedException
	{
		if (user.getCurrentWorkShift() == null)
		{
			throw new WorkShiftNotStartedException();
		}
	}
	
	private void validateUserNotWorking(User user) throws WorkShiftInProgressException
	{
		if (user.getCurrentWorkShift() != null)
		{
			throw new WorkShiftInProgressException();
		}
	}
	
	private void validateUserNotOnBreak(User user) throws BreakInProgressException
	{
		if (user.getCurrentBreak() != null || user.getCurrentLunchBreak() != null)
		{
			throw new BreakInProgressException();
		}
	}
	
	private void filterShiftsAndBreaksForUsers(ReportDataFilters filters, Map<String, User> filteredUsers)
	{
		filteredUsers.entrySet().forEach(entry -> {
			List<WorkShift> filteredShifts = entry.getValue().getPriorWorkShifts().stream()
					.filter(shift -> filters.getShiftBeginsBefore() == null || shift.getStartTime().isBefore(filters.getShiftBeginsBefore()))
					.filter(shift -> filters.getShiftBeginsAfter() == null || shift.getStartTime().isAfter(filters.getShiftBeginsAfter()))
					.collect(Collectors.toList());
			List<Break> filteredBreaks = entry.getValue().getPriorBreaks().stream()
					.filter(workBreak -> filters.getBreakBeginsBefore() == null || workBreak.getStartTime().isBefore(filters.getBreakBeginsBefore()))
					.filter(workBreak -> filters.getBreakBeginsAfter() == null || workBreak.getStartTime().isAfter(filters.getBreakBeginsAfter()))
					.collect(Collectors.toList());
			
			entry.getValue().getPriorWorkShifts().clear();
			entry.getValue().getPriorWorkShifts().addAll(filteredShifts);
			entry.getValue().getPriorBreaks().clear();
			entry.getValue().getPriorBreaks().addAll(filteredBreaks);
		});
	}
	
	private Predicate<Map.Entry<String, User>> passesUserIdFilter(ReportDataFilters filters)
	{
		return entry -> filters.getUserIdToView() == null || filters.getUserIdToView().equals(entry.getValue().getUserId());
	}
	
	private Predicate<Map.Entry<String, User>> passesRoleFilter(ReportDataFilters filters)
	{
		return entry -> filters.getRoleToView() == null || filters.getRoleToView().equals(entry.getValue().getRole());
	}
	
	private Predicate<Map.Entry<String, User>> passesPriorWorkShiftFilter(ReportDataFilters filters)
	{
		return entry -> entry.getValue().getPriorWorkShifts().size() >= filters.getPriorWorkShiftsThreshold();
	}
	
	private Predicate<Map.Entry<String, User>> passesPriorBreaksFilter(ReportDataFilters filters)
	{
		return entry -> entry.getValue().getPriorBreaks().size() >= filters.getPriorBreaksThreshold();
	}
	
	private Predicate<Map.Entry<String, User>> passesOnBreakFilter(ReportDataFilters filters)
	{
		return entry -> !filters.isCurrentlyOnBreak() || entry.getValue().getCurrentBreak() != null;
	}
	
	private Predicate<Map.Entry<String, User>> passesOnLunchFilter(ReportDataFilters filters)
	{
		return entry -> !filters.isCurrentlyOnLunch() || entry.getValue().getCurrentLunchBreak() != null;
	}
}