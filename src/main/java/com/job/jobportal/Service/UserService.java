package com.job.jobportal.Service;

import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserInfoRepository userRepo;

    public List<UserInfo> getalluser() {
        try {
            return userRepo.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all users: " + e.getMessage(), e);
        }
    }

    public UserInfo getuser(String useremail) {
        try {
            List<UserInfo> users = userRepo.findByUseremail(useremail);
            if (users.isEmpty()) {
                throw new RuntimeException("User not found with email: " + useremail);
            }
            return users.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user by email: " + e.getMessage(), e);
        }
    }

    public UserInfo save(UserInfo userData) {
        try {
            return userRepo.save(userData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    public UserInfo findById(String id) {
        try {
            Optional<UserInfo> user = userRepo.findById(id);
            return user.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user by id: " + e.getMessage(), e);
        }
    }

    public void deleteuser(UserInfo user) {
        try {
            if (!userRepo.existsById(user.getId())) {
                throw new RuntimeException("User not found with id: " + user.getId());
            }
            userRepo.delete(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    public boolean isUserExists(String email) {
        try {
            return !userRepo.findByUseremail(email).isEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to check user existence: " + e.getMessage(), e);
        }
    }

    public List<UserInfo> findUsersByRole(UserInfo.Role role) {
        try {
            return userRepo.findByUserrole(role);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch users by role: " + e.getMessage(), e);
        }
    }
}