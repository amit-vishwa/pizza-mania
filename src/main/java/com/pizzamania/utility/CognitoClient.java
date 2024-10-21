package com.pizzamania.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserResult;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.pizzamania.constant.CognitoConstants;
import com.pizzamania.security.model.User;

@Component
public class CognitoClient {

	private static final Logger log = LoggerFactory.getLogger(CognitoClient.class);

	@Value("${environment}")
	private String environment;

	@Value("${aws.region}")
	private String region;

	@Value("${aws.cognito.userPoolId}")
	private String userPoolId;

	@Value("${securitylayer.user.type.external}")
	private String userType;

	@Autowired
	private CognitoConfiguration cognitoConfiguration;

	/**
	 * createUser - Creates a cognito User with temporary password with attributes
	 *
	 * @return UserType - object that contains all the cognito user data
	 * @params email, username, firstName, lastName, password
	 */
	public UserType createUser(User user) {
		try {
			List<AttributeType> userAttributes = new ArrayList<>();
			AttributeType preferredUsername = new AttributeType().withName(CognitoConstants.PREFERRED_USERNAME)
					.withValue(user.getUserName());
			AttributeType emailAttribute = new AttributeType().withName(CognitoConstants.EMAIL)
					.withValue(user.getUserName().toLowerCase());
			AttributeType emailVerifiedAttribute = new AttributeType().withName(CognitoConstants.EMAIL_VERIFIED)
					.withValue(CognitoConstants.EMAIL_VERIFIED_VALUE);
			userAttributes.add(preferredUsername);
			userAttributes.add(emailAttribute);
			userAttributes.add(emailVerifiedAttribute);
			AdminCreateUserRequest userRequest = new AdminCreateUserRequest().withUserPoolId(userPoolId)
					.withUsername(user.getUserName().toLowerCase()).withUserAttributes(userAttributes);
			UserType cognitoUser = cognitoConfiguration.adminCreateUser(userRequest);
			return cognitoUser;
		} catch (AWSCognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("CreateUser: AWSCognitoIdentityProviderException {}",
					awsCognitoIdentityProviderException.toString());
			return new UserType();
		} catch (Exception e) {
			log.error("Cognito create user general error " + e.getMessage());
			return null;
		}
	}

	/**
	 * getUser - Fetches user details based on the provided email ID
	 *
	 * @return AdminGetUserResult - That contains user metadata
	 * @params email
	 */
	public AdminGetUserResult getUser(String email) {
		try {
			AdminGetUserRequest userRequest = new AdminGetUserRequest().withUserPoolId(userPoolId).withUsername(email);
			AdminGetUserResult userResult = cognitoConfiguration.adminGetUser(userRequest);
			return userResult;
		} catch (AWSCognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("CreateUser: AWSCognitoIdentityProviderException {}",
					awsCognitoIdentityProviderException.toString());
			return null;
		} catch (Exception e) {
			log.error("Cognito get user general error " + e.getMessage());
			return null;
		}
	}

	/**
	 * updateUser - Updates the attributes of a specific User
	 *
	 * @return AdminGetUserResult - object that contains all the updated cognito
	 *         user data
	 * @params email, username, password, firstName, lastName
	 */
	public AdminGetUserResult updateUser(User user) {
		try {
			List<AttributeType> userAttributes = new ArrayList<>();
			if (null != user.getUserName()) {
				AttributeType preferredUsername = new AttributeType().withName(CognitoConstants.PREFERRED_USERNAME)
						.withValue(user.getUserName());
				userAttributes.add(preferredUsername);
			}

			AdminUpdateUserAttributesRequest userAttributesRequest = new AdminUpdateUserAttributesRequest()
					.withUserPoolId(userPoolId).withUsername(user.getUserName().toLowerCase())
					.withUserAttributes(userAttributes);

			// If the action is successful, the method sends back an HTTP 200
			// response with an empty HTTP body.
			AdminUpdateUserAttributesResult userAttributesResult = cognitoConfiguration
					.adminUpdateUserAttributes(userAttributesRequest);
			log.info("User attributes update result:  {}", userAttributesResult);

			AdminGetUserRequest userRequest = new AdminGetUserRequest().withUserPoolId(userPoolId)
					.withUsername(user.getUserName().toLowerCase());
			AdminGetUserResult userResult = cognitoConfiguration.adminGetUser(userRequest);
			log.info("User updated details:  {}", userResult);
			return userResult;

		} catch (AWSCognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("UpdateUser: AWSCognitoIdentityProviderException {}",
					awsCognitoIdentityProviderException.getErrorMessage());
			return null;
		} catch (Exception e) {
			log.error("Cognito update user general error " + e.getMessage());
			return null;
		}
	}

	/**
	 * deleteUser - Deletes a specific User.
	 *
	 * @param email
	 * @return boolean value confirming user is deleted or not
	 */
	public Boolean deleteUser(String email) {
		try {
			AdminDeleteUserRequest userRequest = new AdminDeleteUserRequest().withUserPoolId(userPoolId)
					.withUsername(email);

			// If the action is successful, the method sends back an HTTP 200
			// response with an empty HTTP body.
			AdminDeleteUserResult userResult = cognitoConfiguration.adminDeleteUser(userRequest);
			log.info("User deletion result: {}", userResult);
			return true;
		} catch (AWSCognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("DeleteUser: AWSCognitoIdentityProviderException {}",
					awsCognitoIdentityProviderException.toString());
			return false;
		} catch (Exception e) {
			log.error("Cognito delete user general error " + e.getMessage());
			return false;
		}
	}

	public AdminGetUserResult updateUserEmail(CognitoUserAttributes cognitoUserAttributes) {

		AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = constructAdminObjects(
				cognitoUserAttributes);
		AdminUpdateUserAttributesResult userAttributesResult = cognitoConfiguration
				.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
		log.info("User attributes update result:  {}", userAttributesResult);

		AdminGetUserRequest userRequest = new AdminGetUserRequest().withUserPoolId(userPoolId)
				.withUsername(cognitoUserAttributes.getEmail());
		AdminGetUserResult userResult = cognitoConfiguration.adminGetUser(userRequest);
		log.info("User updated details:  {}", userResult);

		return userResult;
	}

	private AdminUpdateUserAttributesRequest constructAdminObjects(CognitoUserAttributes cognitoUserAttributes) {
		List<AttributeType> userAttributes = createUserAttributes(cognitoUserAttributes);
		AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
				.withUsername(cognitoUserAttributes.getUsername()).withUserPoolId(userPoolId)
				.withUserAttributes(userAttributes);
		return adminUpdateUserAttributesRequest;

	}

	private List<AttributeType> createUserAttributes(CognitoUserAttributes cognitoUserAttributes) {
		List<AttributeType> userAttributes = new ArrayList<>();
		AttributeType emailAttribute = new AttributeType().withName(CognitoConstants.EMAIL)
				.withValue(cognitoUserAttributes.getEmail().toLowerCase());
		AttributeType emailVerifiedAttribute = new AttributeType().withName(CognitoConstants.EMAIL_VERIFIED)
				.withValue(CognitoConstants.EMAIL_VERIFIED_VALUE);
		userAttributes.add(emailAttribute);
		userAttributes.add(emailVerifiedAttribute);
		return userAttributes;
	}

	/**
	 * bulkUserEmailUpate - Fetches all user details based on the userpoolid and
	 * update the email to lowercase
	 *
	 * @return List<AdminGetUserResult>- That contains user metadata
	 * @params
	 */
	public List<AdminGetUserResult> bulkUserEmailUpate() {
		try {

			List<Callable<AdminGetUserResult>> callableTasksList = new ArrayList<>();
			List<AdminGetUserResult> adminGetUserResultList = new ArrayList<>();
			ExecutorService service = Executors.newFixedThreadPool(5);
			ListUsersResult usersResult = listAllUsersfromCognito();
			Pattern pattern = Pattern.compile(".*[A-Z].*");
			List<UserType> filteredUsers = new ArrayList<>();
			if (!ObjectUtils.isEmpty(usersResult)) {
				for (UserType user : usersResult.getUsers()) {
					for (AttributeType attributeType : user.getAttributes()) {
						if (attributeType.getName().equals(CognitoConstants.EMAIL)
								&& pattern.matcher(attributeType.getValue()).matches()) {
							filteredUsers.add(user);
						}
					}
				}
				if (!ObjectUtils.isEmpty(filteredUsers)) {
					// filteredUsers =
					// removeDuplicateUsersFromCognito(filteredUsers,usersResult.getUsers());
					List<CognitoUserAttributes> cognitoUserAttributesList = convertUserObjectToCognitoAttributes(
							filteredUsers);
					for (CognitoUserAttributes cognitoUserAttributes : cognitoUserAttributesList) {
						Callable<AdminGetUserResult> callableTask = () -> {
							return updateUserEmail(cognitoUserAttributes);
						};
						callableTasksList.add(callableTask);
					}
					List<Future<AdminGetUserResult>> futures = service.invokeAll(callableTasksList);

					for (Future<AdminGetUserResult> data : futures) {
						adminGetUserResultList.add(data.get());
					}
				}
			}

			service.shutdown();
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			return adminGetUserResultList;
		} catch (AWSCognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("UpdateUser: AWSCognitoIdentityProviderException {}",
					awsCognitoIdentityProviderException.toString());
			return null;
		} catch (Exception e) {
			log.error("Cognito update user general error " + e.getMessage());
			return null;
		}
	}

	private List<CognitoUserAttributes> convertUserObjectToCognitoAttributes(List<UserType> filteredUsers) {
		List<CognitoUserAttributes> cognitoUserAttributesList = new ArrayList<>();
		for (UserType userType : filteredUsers) {
			CognitoUserAttributes cognitoUserAttribute = new CognitoUserAttributes();
			cognitoUserAttribute.setUsername(userType.getUsername());
			for (AttributeType attributeType : userType.getAttributes()) {
				if (attributeType.getName().equals(CognitoConstants.EMAIL)) {
					cognitoUserAttribute.setEmail(attributeType.getValue().toLowerCase());
				}
			}
			cognitoUserAttributesList.add(cognitoUserAttribute);
		}
		return cognitoUserAttributesList;
	}

	public ListUsersResult listAllUsersfromCognito() {
		ListUsersResult listUsersResult;
		try {
			ListUsersRequest usersRequest = new ListUsersRequest().withUserPoolId(userPoolId);
			listUsersResult = cognitoConfiguration.listAllUsers(usersRequest);
			List<UserType> users = listUsersResult.getUsers();
			if (!ObjectUtils.isEmpty(listUsersResult.getPaginationToken())) {
				do {
					usersRequest.setPaginationToken(listUsersResult.getPaginationToken());
					listUsersResult = cognitoConfiguration.listAllUsers(usersRequest);
					users.addAll(listUsersResult.getUsers());
				} while ((Objects.nonNull(listUsersResult.getPaginationToken())));
			}
			listUsersResult.setUsers(users);
			return listUsersResult;

		} catch (AWSCognitoIdentityProviderException e) {
			log.error("Failed to fetch user details " + e.getMessage());
			return null;
		}

	}
}
