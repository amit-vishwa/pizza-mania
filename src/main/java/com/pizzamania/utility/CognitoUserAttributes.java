package com.pizzamania.utility;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CognitoUserAttributes implements Serializable {
    /**
     * generated serial id
     */
    private static final long serialVersionUID = -6464007245156066968L;
    private String userPoolId;
    private String username;
    private String email;

}
