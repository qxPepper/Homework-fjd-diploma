package ru.netology.homeworkfjddiploma.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRequest {
    private String login;
    private String password;
}
