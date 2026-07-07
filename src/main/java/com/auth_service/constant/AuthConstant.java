package com.auth_service.constant;

public final class AuthConstant {

    public static final String REGISTRATION_MAIL_TEXT = """
            
            Hello, this is registration mail.
            
            Go to link below to pass email verification.
            
            %s
            
            """;

    public static final String RECONFIRMATION_EMAIL_TEXT = """
            
            Hello, this is reconfirmation mail.
            
            Go to link below to verify your email.
            
            %s
            
            """;

    public static final String REGISTRATION_THEME = "Successful registration";

    public static final String EMAIL_RECONFIRMATION = "Email reconfirmation";


    private AuthConstant() {
    }
}
