package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.UserNotFoundException;
import com.yasirkhan.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return
                userRepository.findByUsername(username).orElseThrow(
                        () -> new UserNotFoundException(
                                "User with Username: " + username + " Not Found."));
    }
}
