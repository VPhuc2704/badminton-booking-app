package org.badmintonchain.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;
//    @Value("${jwt.expiration}")
//    private Long expiration;
    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration; // 15 minutes
    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration; // 7 days

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDetails user) {
        return createToken(user,  accessTokenExpiration );
    }

    public String generateRefreshToken(UserDetails user) {
        return createToken(user,  refreshTokenExpiration);
    }

    // Tạo token với secret key
    public String createToken(UserDetails userDetails, Long expirationToken) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("email", userDetails.getUsername());
        claims.put("role",  userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        Date currentDate = new Date();
        Date  expiryDate = new Date(currentDate.getTime() + expirationToken);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(currentDate)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, getSigningKey())
                .compact();

        return token;
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Date getExpirationFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            System.err.println("Token đã hết hiệu lực: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Token không hợp lệ: " + e.getMessage());
            return false;
        }
    }
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            return username.equals(userDetails.getUsername()) &&
                    expiration.after(new Date());
        } catch (ExpiredJwtException e) {
            // Token hết hạn
            System.err.println("Token đã hết hiệu lực: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Lỗi khác
            System.err.println("Token không hợp lệ: " + e.getMessage());
            return false;
        }
    }

}
