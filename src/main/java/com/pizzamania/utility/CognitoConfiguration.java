package com.pizzamania.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserResult;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserType;

import lombok.Getter;

@Component
@Getter
public class CognitoConfiguration {

    private Logger logger = LoggerFactory.getLogger(CognitoConfiguration.class);

    @Value("${aws.region}")
    private String region;

    private AWSCognitoIdentityProvider cognitoIdP;

    public AWSCognitoIdentityProvider getAWSCognitoIdentityClient() {
        if (null == cognitoIdP) {
            logger.debug("Configuring Cognito");
            cognitoIdP = AWSCognitoIdentityProviderClientBuilder.standard().withRegion(region).build();
            logger.debug("Cognito initialized successfully");
        }
        return cognitoIdP;
    }

    public AdminGetUserResult adminGetUser(AdminGetUserRequest userRequest) {
        AWSCognitoIdentityProvider cognitoIdentityProvider = getAWSCognitoIdentityClient();
        return cognitoIdentityProvider.adminGetUser(userRequest);
    }

    public UserType adminCreateUser(AdminCreateUserRequest userRequest) {
        AWSCognitoIdentityProvider cognitoIdentityProvider = getAWSCognitoIdentityClient();
        AdminCreateUserResult userResult = cognitoIdentityProvider.adminCreateUser(userRequest);
        if (userResult != null) {
            return userResult.getUser();
        }
        return null;
    }

    public void adminSetUserPassword(AdminSetUserPasswordRequest adminSetUserPasswordRequest) {
        AWSCognitoIdentityProvider cognitoIdentityProvider = getAWSCognitoIdentityClient();
        cognitoIdentityProvider.adminSetUserPassword(adminSetUserPasswordRequest);
    }

    public AdminDeleteUserResult adminDeleteUser(AdminDeleteUserRequest userAttributesRequest) {
        AWSCognitoIdentityProvider cognitoIdentityProvider = getAWSCognitoIdentityClient();
        return cognitoIdentityProvider.adminDeleteUser(userAttributesRequest);
    }

    public AdminUpdateUserAttributesResult adminUpdateUserAttributes(AdminUpdateUserAttributesRequest userRequest) {
        AWSCognitoIdentityProvider cognitoIdentityProvider = getAWSCognitoIdentityClient();
        return cognitoIdentityProvider.adminUpdateUserAttributes(userRequest);
    }

    public ListUsersResult listAllUsers(ListUsersRequest userRequest) {
        AWSCognitoIdentityProvider cognitoIdentityProvider = getAWSCognitoIdentityClient();
        return cognitoIdentityProvider.listUsers(userRequest);
    }

}
