package com.zilch.payments.security.auth.config;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

import com.zilch.payments.security.auth.exceptions.InvalidJwtTokenException;
import com.zilch.payments.security.auth.exceptions.UserAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component
public class JwtTokenUtil {

  private static final String BEARER_HEADER = "Bearer ";

  private static final String USER_ROLES = "roles";

  // IMPORTANT: THIS SECRET MUST BE STORED IN A VAULT
  private final String jwtTokenSecret;

  private final long jwtTokenValidity;

  public JwtTokenUtil(
      @Value("${zilch.security.jwt.secret}") String jwtTokenSecret,
      @Value("${zilch.security.jwt.token-validity-milliseconds}") long jwtTokenValidity) {
    this.jwtTokenSecret = jwtTokenSecret;
    this.jwtTokenValidity = jwtTokenValidity;
  }

  public String generateToken(Collection<? extends GrantedAuthority> authorities, String userName) {
    if (!ObjectUtils.isEmpty(userName) && !ObjectUtils.isEmpty(authorities)) {
      return Jwts.builder()
          .claim(USER_ROLES, authorities)
          .setSubject(userName)
          .setIssuedAt(new Date(System.currentTimeMillis()))
          .setExpiration(new Date(System.currentTimeMillis() + jwtTokenValidity))
          .signWith(getSigningKey(), HS256)
          .compact();
    } else {
      throw new UserAuthenticationException(userName);
    }
  }

  public Optional<String> resolveToken(HttpServletRequest request) {
    final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!ObjectUtils.isEmpty(authorizationHeader)
        && StringUtils.startsWith(authorizationHeader, BEARER_HEADER)) {
      return Optional.of(authorizationHeader.substring(BEARER_HEADER.length()));
    }
    return Optional.empty();
  }

  public boolean isValidToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException exe) {
      throw new InvalidJwtTokenException();
    }
  }

  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (JwtException | IllegalArgumentException exe) {
      throw new InvalidJwtTokenException();
    }
  }

  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtTokenSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
