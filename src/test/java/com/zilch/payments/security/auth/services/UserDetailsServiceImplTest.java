package com.zilch.payments.security.auth.services;

import static com.zilch.payments.TestFixture.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.zilch.payments.security.auth.dto.ZilchUserDetails;
import com.zilch.payments.security.auth.exceptions.UserAuthenticationException;
import com.zilch.payments.security.auth.model.ZilchRole;
import com.zilch.payments.security.auth.model.ZilchUser;
import com.zilch.payments.security.auth.repositories.ZilchUserRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock private ZilchUserRepository zilchUserRepository;

  @InjectMocks private UserDetailsServiceImpl userDetailsServiceImpl;

  @DisplayName("Test Load User with null or empty usernames")
  @NullAndEmptySource
  @ParameterizedTest
  void givenNullOrEmptyUserName_whenLoadUser_thenThrowUserAuthenticationException(String userName) {
    UserAuthenticationException userAuthenticationException =
        assertThrows(
            UserAuthenticationException.class,
            () -> userDetailsServiceImpl.loadUserByUsername(userName));

    assertThat(
        userAuthenticationException.getMessage(),
        is(equalTo("User authentication failed for : " + userName)));

    verify(zilchUserRepository, never()).findByUserName(userName);
  }

  @DisplayName("Test Load User with non-existing username")
  @Test
  void givenUserNameNotFoundInDB_whenLoadUser_thenThrowUserAuthenticationException() {
    String userName = "testuser84";
    when(zilchUserRepository.findByUserName(userName)).thenReturn(Optional.empty());
    UserAuthenticationException userAuthenticationException =
        assertThrows(
            UserAuthenticationException.class,
            () -> userDetailsServiceImpl.loadUserByUsername(userName));

    assertThat(
        userAuthenticationException.getMessage(),
        is(equalTo("User authentication failed for : " + userName)));
    verify(zilchUserRepository).findByUserName(userName);
  }

  @DisplayName("Test Load User with existing username")
  @Test
  void givenValidUserName_whenLoadUser_thenReturnUserDetails() {
    ZilchUser zilchUserFromDb =
        new ZilchUser(
            ZILCH_TEST_USER_ID,
            ZILCH_TEST_USER_NAME,
            ZILCH_TEST_USER_PASSWORD,
            Set.of(new ZilchRole(ROLE_ID, ROLE_USER)),
            OffsetDateTime.now(),
            1);
    when(zilchUserRepository.findByUserName(ZILCH_TEST_USER_NAME))
        .thenReturn(Optional.of(zilchUserFromDb));
    ZilchUserDetails actualZilchUserDetails =
        (ZilchUserDetails) userDetailsServiceImpl.loadUserByUsername(ZILCH_TEST_USER_NAME);
    ZilchUserDetails expectedZilchUserDetails =
        new ZilchUserDetails(
            zilchUserFromDb.getUserId(),
            zilchUserFromDb.getUserName(),
            zilchUserFromDb.getPassword(),
            zilchUserFromDb.getRoles());

    assertThat(actualZilchUserDetails, is(equalTo(expectedZilchUserDetails)));

    verify(zilchUserRepository).findByUserName(ZILCH_TEST_USER_NAME);
  }
}
