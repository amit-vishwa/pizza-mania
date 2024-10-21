package com.pizzamania.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.dto.CartDto;
import com.pizzamania.dto.PurchaseDetailDto;
import com.pizzamania.dto.PurchaseDto;
import com.pizzamania.enumeration.ProcessEnum;
import com.pizzamania.exception.CRUDFailureException;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.exception.ResourceAlreadyExistsException;
import com.pizzamania.exception.ResourceNotFoundException;
import com.pizzamania.mapper.PurchaseDetailMapper;
import com.pizzamania.mapper.PurchaseMapper;
import com.pizzamania.mapper.RoleMapper;
import com.pizzamania.mapper.UserMapper;
import com.pizzamania.model.Purchase;
import com.pizzamania.model.PurchaseDetail;
import com.pizzamania.repository.UserManagementSearchRepository;
import com.pizzamania.security.dto.RoleDto;
import com.pizzamania.security.dto.UserDto;
import com.pizzamania.security.enums.AppRole;
import com.pizzamania.security.model.User;
import com.pizzamania.security.repository.RoleRepository;
import com.pizzamania.security.repository.UserRepository;
import com.pizzamania.security.response.LoginResponse;
import com.pizzamania.security.utils.JwtUtils;
import com.pizzamania.utility.CognitoClient;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;
import com.pizzamania.utility.SecurityContextUtil;
import com.pizzamania.utility.Utility;

import jakarta.transaction.Transactional;

@Service
public class UserManagementServiceImpl implements UserManagementService {

	private static Logger log = LoggerFactory.getLogger(UserManagementServiceImpl.class);

	@Lazy
	@Autowired
	PurchaseDetailManagementService purchaseDetailManagementService;

	@Autowired
	UserManagementSearchRepository userManagementSearchRepository;

	@Lazy
	@Autowired
	PurchaseManagementService purchaseManagementService;

	@Lazy
	@Autowired
	CartManagementService cartManagementService;

	@Autowired
	PurchaseDetailMapper purchaseDetailMapper;

	@Autowired
	SecurityContextUtil securityContextUtil;

	@Autowired
	UserRepository userManagementRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PurchaseMapper purchaseMapper;

	@Autowired
	CognitoClient cognitoClient;

	@Autowired
	UserMapper userMapper;

	@Autowired
	RoleMapper roleMapper;

	@Autowired
	JwtUtils jwtUtils;

	@Override
	public Page<UserDto> searchUsers(SearchCriteria<UserDto> searchCriteria) {
		log.info("Searching for user records with provided search criteria");
		User user = userMapper.userDtoToEntity(searchCriteria.getModel());
		SearchCriteria<User> newSearchCriteria = new SearchCriteria<User>(user, searchCriteria.getPaging(),
				searchCriteria.getSortFields(), searchCriteria.getDataFilterList());
		Page<User> responsePage = userManagementSearchRepository.search(newSearchCriteria);
		if (Utility.hasEntries(responsePage)) {
			List<UserDto> users = new ArrayList<UserDto>();
			responsePage.getPageEntries().forEach(response -> {
				users.add(userMapper.userEntityToDto(response));
			});
			return new Page<UserDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
					responsePage.getTotalRecords(), users);
		}
		return new Page<UserDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
				responsePage.getTotalRecords(), new ArrayList<UserDto>());
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<UserDto> addUsers(List<UserDto> requestList) throws CRUDFailureException, ResourceNotFoundException,
			EntityMissingException, ResourceAlreadyExistsException {
		log.info("Inserting " + requestList.size() + " users records in the database");
		List<User> users = new ArrayList<User>();
		for (UserDto request : requestList) {
			if (isValidCreateUserRequest(request)) {
				User user = createUserObject(request);
				setUserRole(user, request.getRole().getRoleName());
				if (GlobalConstants.EXTERNAL_USER.equalsIgnoreCase(user.getUserType())
						&& !Utility.hasValue(addCognitoUser(user))) {
					throw new CRUDFailureException("Failed to create cognito user with username " + user.getUserName());
				}
				users.add(userManagementRepository.save(user));
			}
		}
		return Utility.listHasValues(users)
				? users.stream().map(user -> userMapper.userEntityToDto(user)).collect(Collectors.toList())
				: null;
	}

	private boolean isValidCreateUserRequest(UserDto request)
			throws EntityMissingException, ResourceAlreadyExistsException {
		if (Utility.hasValue(request) && Utility.hasValue(request.getUserName())
				&& Utility.hasValue(request.getUserPass())) {
			UserDto user = new UserDto();
			user.setUserName(request.getUserName());
			user.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
			SearchCriteria<UserDto> searchCriteria = new SearchCriteria<UserDto>(user);
			Page<UserDto> response = searchUsers(searchCriteria);
			if (!Utility.hasEntries(response)) {
				return true;
			}
			throw new ResourceAlreadyExistsException("User already exists with username " + request.getUserName());
		}
		throw new EntityMissingException("Username and password cannot be null!");
	}

	private User createUserObject(UserDto request) {
		request.setUserPass(passwordEncoder.encode(request.getUserPass()));
		request.setUserType(
				GlobalConstants.INTERNAL_USER.equalsIgnoreCase(request.getUserType()) ? request.getUserType()
						: GlobalConstants.EXTERNAL_USER);
		if (!Utility.hasValue(request.getRole())
				|| (Utility.hasValue(request.getRole()) && !AppRole.ROLE_USER.equals(request.getRole().getRoleName())
						&& !AppRole.ROLE_ADMIN.equals(request.getRole().getRoleName())
						&& !AppRole.ROLE_MANAGER.equals(request.getRole().getRoleName()))) {
			RoleDto role = new RoleDto();
			role.setRoleId(1);
			role.setRoleName(AppRole.ROLE_USER);
			request.setRole(role);
		}
		request.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
		request.setCreatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
		request.setCreatedByUser(
				Utility.hasValue(request.getCreatedByUser()) ? request.getCreatedByUser() : getLoggedInUserName());
		request.setCreatedByProcess(Utility.hasValue(request.getCreatedByProcess()) ? request.getCreatedByProcess()
				: ProcessEnum.ADD_USERS.getProcessCode());
		return userMapper.userDtoToEntity(request);
	}

	private void setUserRole(User user, AppRole roleName) throws ResourceNotFoundException {
		user.setRole(roleRepository.findByRoleName(roleName)
				.orElseThrow(() -> new ResourceNotFoundException("Role not exists with role name " + roleName)));
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<UserDto> editUsers(List<UserDto> requestList)
			throws ResourceNotFoundException, EntityMissingException, CRUDFailureException {
		log.info("Updating " + requestList.size() + " users records in the database");
		return updateUserRecords(requestList, false);
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<UserDto> deleteUsers(List<UserDto> requestList)
			throws ResourceNotFoundException, EntityMissingException, CRUDFailureException {
		log.info("Deleting " + requestList.size() + " user records");
		return updateUserRecords(requestList, true);
	}

	private List<UserDto> updateUserRecords(List<UserDto> requestList, boolean isDeleteRequest)
			throws ResourceNotFoundException, EntityMissingException, CRUDFailureException {
		List<User> users = new ArrayList<User>();
		for (UserDto request : requestList) {
			if (Utility.hasValue(request) && Utility.hasValue(request.getUserId())) {
				UserDto user = new UserDto();
				user.setUserId(request.getUserId());
				user.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
				SearchCriteria<UserDto> searchCriteria = new SearchCriteria<UserDto>(user);
				Page<UserDto> response = searchUsers(searchCriteria);
				if (Utility.hasEntries(response)) {
					saveUpdatedData(request, isDeleteRequest, response.getPageEntries().get(0), users);
				} else {
					throw new ResourceNotFoundException("User not exists with user id " + request.getUserId());
				}
			} else {
				throw new EntityMissingException("User id cannot be null!");
			}
		}
		return Utility.listHasValues(users)
				? users.stream().map(user -> userMapper.userEntityToDto(user)).collect(Collectors.toList())
				: null;
	}

	private void saveUpdatedData(UserDto request, boolean isDeleteRequest, UserDto response, List<User> users)
			throws ResourceNotFoundException, CRUDFailureException {
		User userCopy = userMapper.userDtoToEntity(response);
		boolean isRecordUpdated = false;
		if (isDeleteRequest) {
			if (userCopy.getUserType().equals(GlobalConstants.INTERNAL_USER)) {
				userCopy.setLastUpdatedByProcess(ProcessEnum.DELETE_USERS.getProcessCode());
				userCopy.setRecordStatus(GlobalConstants.DEACTIVE_RECORD_STATUS);
				isRecordUpdated = true;
			} else {
				deleteExternalUserSafely(userCopy, request.getUserPass(), getLoggedInUserName(),
						ProcessEnum.DELETE_USERS.getProcessCode());
			}
		} else {
			if (Utility.hasValue(request.getRole())) {
				setUserRole(userCopy, request.getRole().getRoleName());
				userCopy.setLastUpdatedByProcess(ProcessEnum.EDIT_USERS.getProcessCode());
				isRecordUpdated = true;
			}
		}
		if (isRecordUpdated) {
			userCopy.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
			userCopy.setLastUpdatedByUser(getLoggedInUserName());
			users.add(userManagementRepository.save(userCopy));
		}
	}

	@Override
	public UserDetails getUserDetails() {
		return securityContextUtil.getUserDetails();
	}

	public AdminGetUserResult getCognitoUser(String email) {
		log.info("Get Cognito User");
		return cognitoClient.getUser(email.toLowerCase());
	}

	private String addCognitoUser(User user) {
		log.info("Adding New Cognito User");
		AdminGetUserResult adminGetUserResult = getCognitoUser(user.getUserName());
		if (null == adminGetUserResult)
			return cognitoClient.createUser(user).getUsername();
		log.info("Cognito user exists with username : " + adminGetUserResult.getUsername());
		return adminGetUserResult.getUsername();
	}

	private boolean deleteCognitoUser(String email) {
		log.info("Delete Cognito User");
		return cognitoClient.deleteUser(email.toLowerCase());
	}

	private String getLoggedInUserName() {
		return securityContextUtil.getUserName();
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public String deleteUserDetails(String password) throws CRUDFailureException {
		UserDetails userdetails = getUserDetails();
		User user = userManagementRepository
				.findByUserNameAndRecordStatus(userdetails.getUsername(), GlobalConstants.ACTIVE_RECORD_STATUS)
				.orElse(null);
		if (Utility.hasValue(user)) {
			deleteExternalUserSafely(user, password, user.getUserName(),
					ProcessEnum.DELETE_USER_DETAILS.getProcessCode());
			return "User details deleted successfully!";
		}
		return null;
	}

	private void deleteExternalUserSafely(User user, String password, String updatedByUser, String process)
			throws CRUDFailureException {
		if (isCorrectPassword(password, user.getUserPass())) {
			if (deleteCognitoUser(user.getUserName())) {
				user.setLastUpdatedByUser(updatedByUser);
				user.setRecordStatus(GlobalConstants.DEACTIVE_RECORD_STATUS);
				user.setLastUpdatedByProcess(process);
				user.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
				deleteAssociatedRecords(user.getUserId(), updatedByUser, process);
				userManagementRepository.save(user);
			} else {
				throw new CRUDFailureException("Failed to delete cognito user with username " + user.getUserName());
			}
		} else {
			throw new AccessDeniedException("Password does not match!");
		}
	}

	private void deleteAssociatedRecords(Integer userId, String updatedByUser, String process) {
		List<PurchaseDto> deletedCartsPurchaseList = deleteAssociatedCarts(userId, updatedByUser, process);
		if (Utility.listHasValues(deletedCartsPurchaseList)) {
			List<PurchaseDetailDto> deletedPurchasesDetailsList = deleteAssociatedPurchases(deletedCartsPurchaseList,
					updatedByUser, process);
			if (Utility.listHasValues(deletedPurchasesDetailsList)) {
				deleteAssociatedPurchaseDetails(deletedPurchasesDetailsList, updatedByUser, process);
			}
		}
	}

	private List<PurchaseDto> deleteAssociatedCarts(Integer userId, String updatedByUser, String process) {
		CartDto cart = new CartDto();
		cart.setUserId(userId);
		cart.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
		SearchCriteria<CartDto> searchCriteria = new SearchCriteria<CartDto>(cart);
		Page<CartDto> cartResponse = cartManagementService.searchItems(searchCriteria);
		if (Utility.hasEntries(cartResponse)) {
			List<PurchaseDto> purchaseDtoList = new ArrayList<PurchaseDto>();
			cartResponse.getPageEntries().forEach(cartDto -> {
				cartDto.setRecordStatus(GlobalConstants.DEACTIVE_RECORD_STATUS);
				cartDto.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
				cartDto.setLastUpdatedByUser(updatedByUser);
				cartDto.setLastUpdatedByProcess(process);
				purchaseDtoList.addAll(cartDto.getPurchases());
			});
			cartManagementService.addItems(cartResponse.getPageEntries());
			return purchaseDtoList;
		}
		return null;
	}

	private List<PurchaseDetailDto> deleteAssociatedPurchases(List<PurchaseDto> purchaseDtoList, String updatedByUser,
			String process) {
		List<Purchase> purchaseList = new ArrayList<Purchase>();
		List<PurchaseDetailDto> purchaseDetailList = new ArrayList<PurchaseDetailDto>();
		purchaseDtoList.forEach(purchaseDto -> {
			if (GlobalConstants.ACTIVE_RECORD_STATUS.equals(purchaseDto.getRecordStatus())) {
				purchaseDto.setRecordStatus(GlobalConstants.DEACTIVE_RECORD_STATUS);
				purchaseDto.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
				purchaseDto.setLastUpdatedByUser(updatedByUser);
				purchaseDto.setLastUpdatedByProcess(process);
				purchaseList.add(purchaseMapper.purchaseDtoToEntity(purchaseDto));
				purchaseDetailList.addAll(purchaseDto.getPurchaseDetailsDto());
			}
		});
		purchaseManagementService.saveAllPurchases(purchaseList);
		return purchaseDetailList;
	}

	private void deleteAssociatedPurchaseDetails(List<PurchaseDetailDto> purchaseDetailDtoList, String updatedByUser,
			String process) {
		List<PurchaseDetail> purchaseDetailList = new ArrayList<PurchaseDetail>();
		purchaseDetailDtoList.forEach(purchaseDetailDto -> {
			if (GlobalConstants.ACTIVE_RECORD_STATUS.equals(purchaseDetailDto.getRecordStatus())) {
				purchaseDetailDto.setRecordStatus(GlobalConstants.DEACTIVE_RECORD_STATUS);
				purchaseDetailDto.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
				purchaseDetailDto.setLastUpdatedByUser(updatedByUser);
				purchaseDetailDto.setLastUpdatedByProcess(process);
				purchaseDetailList.add(purchaseDetailMapper.purchaseDetailDtoToEntity(purchaseDetailDto));
			}
		});
		purchaseDetailManagementService.saveAllPurchaseDetails(purchaseDetailList);
	}

	public boolean isCorrectPassword(String password, String encodedPassword) {
		return (Utility.hasValue(password) && Utility.hasValue(encodedPassword))
				? passwordEncoder.matches(password, encodedPassword)
				: false;
	}

	@Override
	public Integer getLoggedInUserId() {
		String username = getLoggedInUserName();
		User userById = userManagementRepository.findUserIdByUserName(username).orElse(null);
		return Utility.hasValue(userById) ? userById.getUserId() : null;
	}

	@Override
	public LoginResponse authenticateUser(UserDto request) {
		request.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
		List<UserDto> users = searchUsers(new SearchCriteria<UserDto>(request)).getPageEntries();
		if (Utility.listHasValues(users) && isCorrectPassword(request.getUserPass(), users.get(0).getUserPass())
				&& Utility.hasValue(getCognitoUser(request.getUserName()))) {
			String jwtToken = jwtUtils.generateTokenFromUsername(users.get(0));
			return new LoginResponse(users.get(0).getUserName(), users.get(0).getRole().getRoleName().toString(),
					jwtToken);
		}
		return null;
	}

}
