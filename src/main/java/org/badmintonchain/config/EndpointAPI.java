package org.badmintonchain.config;

public class EndpointAPI {
    public static final String[] PUBLIC_API_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/logout",
            "/api/auth/refresh",
            "/api/auth/verify",
            "/api/courts/**",
            "/api/quotations/send"
    };

    public  static final String[] PRIVATE_ENDPOINTS = {
            "/api/bookings/**",
            "/api/users/*"

    };

    public  static final String[] MANAGEMENT_API_ENDPOINTS  = {
            "/api/admin/courts",
            "/api/admin/bookings/**",
            "/api/admin/users/**",
            "/api/admin/services/*"

    };

}
