package ru.netology.homeworkfjddiploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.netology.homeworkfjddiploma.entity.MyUser;

@Repository
public interface MyUserRepository extends JpaRepository<MyUser, Long> {
    MyUser findByLogin(String login);
}
