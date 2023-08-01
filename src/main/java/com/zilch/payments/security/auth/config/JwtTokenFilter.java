package com.zilch.payments.security.auth.config;

import com.zilch.payments.security.auth.dto.ZilchAuthenticatedPrincipal;
import com.zilch.payments.security.auth.dto.ZilchUserDetails;
import com.zilch.payments.security.auth.exceptions.InvalidJwtTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtTokenUtil jwtTokenUtil;

  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    final Optional<String> optJwtToken = jwtTokenUtil.resolveToken(request);
    try {
      if (optJwtToken.isPresent() && jwtTokenUtil.isValidToken(optJwtToken.get())) {
        Authentication authentication =
            getUsernamePasswordAuthenticationToken(request, optJwtToken.get());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (InvalidJwtTokenException exception) {
      log.debug(" : InvalidJwtTokenException : ", exception);
      SecurityContextHolder.clearContext();
      response.sendError(HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
      return;
    }
    filterChain.doFilter(request, response);
  }

  private UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(
      HttpServletRequest request, String token) {
    String userName = jwtTokenUtil.getUsernameFromToken(token);
    ZilchUserDetails zilchUserDetails =
        (ZilchUserDetails) userDetailsService.loadUserByUsername(userName);
    ZilchAuthenticatedPrincipal zilchAuthenticatedPrincipal =
        new ZilchAuthenticatedPrincipal(
            zilchUserDetails.getUserId(), zilchUserDetails.getUsername());
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
        new UsernamePasswordAuthenticationToken(
            zilchAuthenticatedPrincipal, "", zilchUserDetails.getAuthorities());
    usernamePasswordAuthenticationToken.setDetails(
        new WebAuthenticationDetailsSource().buildDetails(request));
    log.debug(" : user validated successfully : {}", userName);
    return usernamePasswordAuthenticationToken;
  }
}
