package com.publicissapient.kpidashboard.common.constant;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserRoleTest {

    @Test
    public void testUserRoleEnumValues() {
        assertEquals(UserRole.ROLE_USER, UserRole.valueOf("ROLE_USER"));
        assertEquals(UserRole.ROLE_ADMIN, UserRole.valueOf("ROLE_ADMIN"));
        assertEquals(UserRole.ROLE_API, UserRole.valueOf("ROLE_API"));
    }

    @Test
    public void testUserRoleEnumToString() {
        assertEquals("ROLE_USER", UserRole.ROLE_USER.toString());
        assertEquals("ROLE_ADMIN", UserRole.ROLE_ADMIN.toString());
        assertEquals("ROLE_API", UserRole.ROLE_API.toString());
    }

}
