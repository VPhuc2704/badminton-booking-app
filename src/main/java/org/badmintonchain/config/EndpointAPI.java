package org.badmintonchain.config;

public class EndpointAPI {
    public static final String[] PUBLIC_API_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/logout",
            "/api/auth/refresh",
            "/api/auth/verify",
            "/api/courts/**"
    };

    public  static final String[] PRIVATE_ENDPOINTS = {
            "/api/bookings/**"
    };

    public  static final String[] MANAGEMENT_API_ENDPOINTS  = {
            "/api/admin/courts",
    };

}
