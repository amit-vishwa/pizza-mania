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

import com.pizzamania.constant.CognitoConstants;
import com.pizzamania.security.model.User;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

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
			AttributeType preferredUsername = AttributeType.builder().name(CognitoConstants.PREFERRED_USERNAME)
					.value(user.getUserName()).build();
			AttributeType emailAttribute = AttributeType.builder().name(CognitoConstants.EMAIL)
					.value(user.getUserName().toLowerCase()).build();
			AttributeType emailVerifiedAttribute = AttributeType.builder().name(CognitoConstants.EMAIL_VERIFIED)
					.value(CognitoConstants.EMAIL_VERIFIED_VALUE).build();
			userAttributes.add(preferredUsername);
			userAttributes.add(emailAttribute);
			userAttributes.add(emailVerifiedAttribute);
			AdminCreateUserRequest userRequest = AdminCreateUserRequest.builder().userPoolId(userPoolId)
					.username(user.getUserName().toLowerCase()).userAttributes(userAttributes).build();
			UserType cognitoUser = cognitoConfiguration.adminCreateUser(userRequest);
			return cognitoUser;
		} catch (CognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("CreateUser: CognitoIdentityProviderException {}",
					awsCognitoIdentityProviderException.toString());
			return UserType.builder().build();
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
	public AdminGetUserResponse getUser(String email) {
		try {
			AdminGetUserRequest userRequest = AdminGetUserRequest.builder().userPoolId(userPoolId).username(email).build();
			AdminGetUserResponse userResult = cognitoConfiguration.adminGetUser(userRequest);
			return userResult;
		} catch (CognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("CreateUser: CognitoIdentityProviderException {}",
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
	public AdminGetUserResponse updateUser(User user) {
		try {
			List<AttributeType> userAttributes = new ArrayList<>();
			if (null != user.getUserName()) {
				AttributeType preferredUsername = AttributeType.builder().name(CognitoConstants.PREFERRED_USERNAME)
						.value(user.getUserName()).build();
				userAttributes.add(preferredUsername);
			}

			AdminUpdateUserAttributesRequest userAttributesRequest = AdminUpdateUserAttributesRequest.builder()
					.userPoolId(userPoolId).username(user.getUserName().toLowerCase())
					.userAttributes(userAttributes).build();

			// If the action is successful, the method sends back an HTTP 200
			// response with an empty HTTP body.
			AdminUpdateUserAttributesResponse userAttributesResult = cognitoConfiguration
					.adminUpdateUserAttributes(userAttributesRequest);
			log.info("User attributes update result:  {}", userAttributesResult);

			AdminGetUserRequest userRequest = AdminGetUserRequest.builder().userPoolId(userPoolId)
					.username(user.getUserName().toLowerCase()).build();
			AdminGetUserResponse userResult = cognitoConfiguration.adminGetUser(userRequest);
			log.info("User updated details:  {}", userResult);
			return userResult;

		} catch (CognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("UpdateUser: CognitoIdentityProviderException {}",
					awsCognitoIdentityProviderException.awsErrorDetails().errorMessage());
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
			AdminDeleteUserRequest userRequest = AdminDeleteUserRequest.builder().userPoolId(userPoolId)
					.username(email).build();

			// If the action is successful, the method sends back an HTTP 200
			// response with an empty HTTP body.
			AdminDeleteUserResponse userResult = cognitoConfiguration.adminDeleteUser(userRequest);
			log.info("User deletion result: {}", userResult);
			return true;
		} catch (CognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("DeleteUser: CognitoIdentityProviderException {}",
					awsCognitoIdentityProviderException.toString());
			return false;
		} catch (Exception e) {
			log.error("Cognito delete user general error " + e.getMessage());
			return false;
		}
	}

	public AdminGetUserResponse updateUserEmail(CognitoUserAttributes cognitoUserAttributes) {

		AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = constructAdminObjects(
				cognitoUserAttributes);
		AdminUpdateUserAttributesResponse userAttributesResult = cognitoConfiguration
				.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
		log.info("User attributes update result:  {}", userAttributesResult);

		AdminGetUserRequest userRequest = AdminGetUserRequest.builder().userPoolId(userPoolId)
				.username(cognitoUserAttributes.getEmail()).build();
		AdminGetUserResponse userResult = cognitoConfiguration.adminGetUser(userRequest);
		log.info("User updated details:  {}", userResult);

		return userResult;
	}

	private AdminUpdateUserAttributesRequest constructAdminObjects(CognitoUserAttributes cognitoUserAttributes) {
		List<AttributeType> userAttributes = createUserAttributes(cognitoUserAttributes);
		AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = AdminUpdateUserAttributesRequest.builder()
				.username(cognitoUserAttributes.getUsername()).userPoolId(userPoolId)
				.userAttributes(userAttributes).build();
		return adminUpdateUserAttributesRequest;

	}

	private List<AttributeType> createUserAttributes(CognitoUserAttributes cognitoUserAttributes) {
		List<AttributeType> userAttributes = new ArrayList<>();
		AttributeType emailAttribute = AttributeType.builder().name(CognitoConstants.EMAIL)
				.value(cognitoUserAttributes.getEmail().toLowerCase()).build();
		AttributeType emailVerifiedAttribute = AttributeType.builder().name(CognitoConstants.EMAIL_VERIFIED)
				.value(CognitoConstants.EMAIL_VERIFIED_VALUE).build();
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
	public List<AdminGetUserResponse> bulkUserEmailUpate() {
		try {

			List<Callable<AdminGetUserResponse>> callableTasksList = new ArrayList<>();
			List<AdminGetUserResponse> adminGetUserResultList = new ArrayList<>();
			ExecutorService service = Executors.newFixedThreadPool(5);
			ListUsersResponse usersResult = listAllUsersfromCognito();
			Pattern pattern = Pattern.compile(".*[A-Z].*");
			List<UserType> filteredUsers = new ArrayList<>();
			if (!ObjectUtils.isEmpty(usersResult)) {
				for (UserType user : usersResult.users()) {
					for (AttributeType attributeType : user.attributes()) {
						if (attributeType.name().equals(CognitoConstants.EMAIL)
								&& pattern.matcher(attributeType.value()).matches()) {
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
						Callable<AdminGetUserResponse> callableTask = () -> {
							return updateUserEmail(cognitoUserAttributes);
						};
						callableTasksList.add(callableTask);
					}
					List<Future<AdminGetUserResponse>> futures = service.invokeAll(callableTasksList);

					for (Future<AdminGetUserResponse> data : futures) {
						adminGetUserResultList.add(data.get());
					}
				}
			}

			service.shutdown();
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			return adminGetUserResultList;
		} catch (CognitoIdentityProviderException awsCognitoIdentityProviderException) {
			log.error("UpdateUser: CognitoIdentityProviderException {}",
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
			cognitoUserAttribute.setUsername(userType.username());
			for (AttributeType attributeType : userType.attributes()) {
				if (attributeType.name().equals(CognitoConstants.EMAIL)) {
					cognitoUserAttribute.setEmail(attributeType.value().toLowerCase());
				}
			}
			cognitoUserAttributesList.add(cognitoUserAttribute);
		}
		return cognitoUserAttributesList;
	}

	public ListUsersResponse listAllUsersfromCognito() {
		ListUsersResponse listUsersResult;
		try {
			ListUsersRequest usersRequest = ListUsersRequest.builder().userPoolId(userPoolId).build();
			listUsersResult = cognitoConfiguration.listAllUsers(usersRequest);
			List<UserType> users = new ArrayList<>(listUsersResult.users());
			if (!ObjectUtils.isEmpty(listUsersResult.paginationToken())) {
				do {
					usersRequest = usersRequest.toBuilder().paginationToken(listUsersResult.paginationToken()).build();
					listUsersResult = cognitoConfiguration.listAllUsers(usersRequest);
					users.addAll(listUsersResult.users());
				} while ((Objects.nonNull(listUsersResult.paginationToken())));
			}
			return listUsersResult.toBuilder().users(users).build();

		} catch (CognitoIdentityProviderException e) {
			log.error("Failed to fetch user details " + e.getMessage());
			return null;
		}

	}
}
