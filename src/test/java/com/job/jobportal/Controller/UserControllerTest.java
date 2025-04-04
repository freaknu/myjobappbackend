package com.job.jobportal.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSources;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.job.jobportal.Repository.UserInfoRepository;

// @SpringBootTest
public class UserControllerTest {
    @Autowired
    private UserInfoRepository userrepo;

    @Disabled
    @Test
    public void testFindByUsername() {
        assertNotNull(userrepo.findAll());
    }

    // public void healthcheck() {
    // assertEquals(getClass(), getClass());
    // }
    @Disabled
    @Test
    public void testadd() {
        assertTrue(3 > 2);
    }

    @ParameterizedTest
    @CsvSource({
            "2,3,5",
            "4,5,9",
            "12,3,14"
    })
    @Disabled
    public void test1(int a, int b, int result) {
        assertEquals(result, a + b, "Failed for" + a + " " + b + " " + result);
    }
}
