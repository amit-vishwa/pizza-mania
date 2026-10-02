package com.pizzamania.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

import lombok.Getter;
import jakarta.annotation.PreDestroy;

@Component
@Getter
public class CognitoConfiguration {

    private Logger logger = LoggerFactory.getLogger(CognitoConfiguration.class);

    @Value("${aws.region}")
    private String region;

    private CognitoIdentityProviderClient cognitoIdP;

    public CognitoIdentityProviderClient getCognitoIdentityClient() {
        if (null == cognitoIdP) {
            logger.debug("Configuring Cognito");
            cognitoIdP = CognitoIdentityProviderClient.builder().region(Region.of(region)).build();
            logger.debug("Cognito initialized successfully");
        }
        return cognitoIdP;
    }

    @PreDestroy
    void closeCognitoIdentityClient() {
        if (cognitoIdP != null) {
            cognitoIdP.close();
        }
    }

    public AdminGetUserResponse adminGetUser(AdminGetUserRequest userRequest) {
        CognitoIdentityProviderClient cognitoIdentityProvider = getCognitoIdentityClient();
        return cognitoIdentityProvider.adminGetUser(userRequest);
    }

    public UserType adminCreateUser(AdminCreateUserRequest userRequest) {
        CognitoIdentityProviderClient cognitoIdentityProvider = getCognitoIdentityClient();
        var userResult = cognitoIdentityProvider.adminCreateUser(userRequest);
        if (userResult != null) {
            return userResult.user();
        }
        return null;
    }

    public void adminSetUserPassword(AdminSetUserPasswordRequest adminSetUserPasswordRequest) {
        CognitoIdentityProviderClient cognitoIdentityProvider = getCognitoIdentityClient();
        cognitoIdentityProvider.adminSetUserPassword(adminSetUserPasswordRequest);
    }

    public AdminDeleteUserResponse adminDeleteUser(AdminDeleteUserRequest userAttributesRequest) {
        CognitoIdentityProviderClient cognitoIdentityProvider = getCognitoIdentityClient();
        return cognitoIdentityProvider.adminDeleteUser(userAttributesRequest);
    }

    public AdminUpdateUserAttributesResponse adminUpdateUserAttributes(AdminUpdateUserAttributesRequest userRequest) {
        CognitoIdentityProviderClient cognitoIdentityProvider = getCognitoIdentityClient();
        return cognitoIdentityProvider.adminUpdateUserAttributes(userRequest);
    }

    public ListUsersResponse listAllUsers(ListUsersRequest userRequest) {
        CognitoIdentityProviderClient cognitoIdentityProvider = getCognitoIdentityClient();
        return cognitoIdentityProvider.listUsers(userRequest);
    }

}
