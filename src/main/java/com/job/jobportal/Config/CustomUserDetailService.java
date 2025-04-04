package com.job.jobportal.Config;

import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Repository.UserInfoRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CustomUserDetailService implements UserDetailsService {
    private final UserInfoRepository userRepo;

    public CustomUserDetailService(UserInfoRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String useremail) throws UsernameNotFoundException {
        List<UserInfo> users = userRepo.findByUseremail(useremail);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + useremail);
        }
        UserInfo user = users.get(0);

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getUserrole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getUseremail(),
                user.getUserpassword(),
                Collections.singletonList(authority));
    }
}