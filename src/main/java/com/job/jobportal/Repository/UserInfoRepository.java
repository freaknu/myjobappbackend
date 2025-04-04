package com.job.jobportal.Repository;

import com.job.jobportal.Model.UserInfo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInfoRepository extends MongoRepository<UserInfo, String> {
    List<UserInfo> findByUsername(String username);

    List<UserInfo> findByUseremail(String useremail);

    List<UserInfo> findByUserrole(UserInfo.Role role);

}
