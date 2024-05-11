package com.example.emergencyapp.utils;

import com.example.emergencyapp.exceptions.UserException;

public class UserHelper {

    public static boolean validateUser(User user) throws UserException {
        return validateName(user.getName()) &&
                validateUsername(user.getUsername()) &&
                validateEmail(user.getEmail()) &&
                validatePassword(user.getPassword());
    }

    private static boolean validateName(String name) throws UserException {
        if(name.isEmpty()){
            throw new UserException("The name is not valid!");
        }
        return true;
    }

    private static boolean validateUsername(String username) throws UserException {
        String noWhiteSpace = "(?=\\s+$)";

        if(username.isEmpty() || username.matches(noWhiteSpace)){
            throw new UserException("The username is not valid!");
        }
        return true;
    }

    private static boolean validateEmail(String email) throws UserException {
        String emailRegex = "[a-zA-Z0-9+_.-]+@[a-z]+\\.+[a-z]+$";

        if(email.isEmpty() || !email.matches(emailRegex)){
            throw new UserException("The email is not valid!");
        }
        return true;
    }

    private static boolean validatePassword(String password) throws UserException {

        if(password.isEmpty()){
            throw new UserException("The password cannot be null!");
        }else{
            boolean validPass = true;

            if (password.length() < 8) validPass = false;
            if (!password.matches(".*[0-9].*")) validPass = false;
            if (!password.matches(".*[a-z].*")) validPass = false;
            if (!password.matches(".*[A-Z].*")) validPass = false;
            if (!password.matches(".*[!#&*~%@$^].*")) validPass = false;
            if (password.matches(".*\\s.*")) validPass = false;

            if(!validPass) {
                throw new UserException("The password needs to have 8 characters, at least a number, a capital letter and a special character!");
            }
        }
        return true;
    }
}
