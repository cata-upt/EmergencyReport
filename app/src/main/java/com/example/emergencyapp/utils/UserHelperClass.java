package com.example.emergencyapp.utils;

import com.example.emergencyapp.exceptions.UserException;

public class UserHelperClass {
    String name,  username, email, password;

    public UserHelperClass() {
    }

    public UserHelperClass(String name, String username, String email, String password) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public boolean validateUser() throws UserException {
        return validateName(this.name) &&
                validateUsername(this.username) &&
                validateEmail(this.email) &&
                validatePassword(this.password);
    }

    private boolean validateName(String name) throws UserException {
        if(name.isEmpty()){
            throw new UserException("The name is not valid!");
        }
        return true;
    }

    private boolean validateUsername(String username) throws UserException {
        String noWhiteSpace = "(?=\\s+$)";

        if(username.isEmpty() || username.matches(noWhiteSpace)){
            throw new UserException("The username is not valid!");
        }
        return true;
    }

    private boolean validateEmail(String email) throws UserException {
        String emailRegex = "[a-zA-Z0-9+_.-]+@[a-z]+\\.+[a-z]+$";

        if(email.isEmpty() || !email.matches(emailRegex)){
            throw new UserException("The email is not valid!");
        }
        return true;
    }

    private boolean validatePassword(String password) throws UserException {
        String passwordRegex = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";

        if(password.isEmpty()){
            throw new UserException("The password cannot be null!");
        }else if(!password.matches(passwordRegex)){
            throw new UserException("The password needs to have at least a number, a capital letter and a special character!");
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
