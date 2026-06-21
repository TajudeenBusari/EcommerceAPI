/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of User Service module of the EcommerceMicroservices project.
 */
package com.tjtechy.security.config;


import userutils.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

/***
 * Ideally, this class should be in the user service module since it depends on the User entity,
 * but we need it in the security module to implement UserDetails and use it in the authentication process.
 * Moreover, if it placed in the shared-domain module, we will need to add spring security dependency to the shared-domain
 * module which is not ideal because the shared-domain module should be independent of any specific framework or library.
 */
public class MyUserPrincipal implements UserDetails {
    private final User user;

    public MyUserPrincipal(User user) {
        this.user = user;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //Convert a user's role from space-delimited to a list of SimpleGrantedAuthority objects.
        // This is necessary because Spring Security uses GrantedAuthority to represent user roles and permissions.
        return Arrays.stream(StringUtils.tokenizeToStringArray(this.user.getRole().name(), " "))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUserName();
    }

    /**
     * @return we don't have this but return true for this method so that authentication will not fail
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    @Override
    public boolean isEnabled() {
        return this.user.isEnabled();
    }

    /**
     * use this method to get the actual User entity
     */

    public User user() {
        return this.user;
    }
}