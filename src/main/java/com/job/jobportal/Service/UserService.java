package com.job.jobportal.Service;

import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Repository.UserInfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserInfoRepository userRepo;

    public UserService(UserInfoRepository userRepo) {
        this.userRepo = userRepo;
    }

    public List<UserInfo> getAllUsers() {
        return userRepo.findAll();
    }

    public UserInfo getUser(String useremail) {
        return userRepo.findByUseremail(useremail)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found with email: " + useremail));
    }

    public UserInfo save(UserInfo userData) {
        return userRepo.save(userData);
    }

    public UserInfo findById(String id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(UserInfo user) {
        if (!userRepo.existsById(user.getId())) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }
        userRepo.delete(user);
    }

    public boolean isUserExists(String email) {
        return !userRepo.findByUseremail(email).isEmpty();
    }

    public List<UserInfo> findUsersByRole(UserInfo.Role role) {
        return userRepo.findByUserrole(role);
    }
}