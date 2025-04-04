package com.job.jobportal.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.job.jobportal.Config.CustomUserDetailService;
import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Repository.UserInfoRepository;
import com.mongodb.assertions.Assertions;

@SpringBootTest
public class UserdetailServiceTest {
    // @Autowired
    // @InjectMocks
    // private CustomUserDetailService userdetails;

    // @Mock
    // private UserInfoRepository userrepo;

    @Autowired
    private UserInfoRepository userrepo;

    // @Disabled
    // @BeforeEach
    // public void setupmockito() {
    // MockitoAnnotations.openMocks(this);
    // }
    // @Disabled
    // @Test
    // public void loadbyusername() {
    // when(userrepo.findByUseremail("Pk2239.29.jnv@gmail.com"))
    // .thenReturn((UserInfo)
    // User.builder().username("Prabhat").password("dgsy7234hw")
    // .authorities("JOBSEEKER").build());
    // UserDetails user = userdetails.loadUserByUsername("Pk2239.29.jnv@gmail.com");
    // Assertions.assertNotNull(user);
    // }
    // @Test
    // public void useremailtest() {
    //     assertNotNull(userrepo.findByUseremail("Pk2239.29.jnv@gmail.com"));
    //     System.out.println(userrepo.findByUseremail("Pk2239.29.jnv@gmail.com"));
    // }

}
