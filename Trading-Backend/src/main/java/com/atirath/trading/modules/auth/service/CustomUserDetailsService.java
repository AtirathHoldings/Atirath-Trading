package com.atirath.trading.modules.auth.service;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.model.UserCredential;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.auth.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final UserCredentialRepository userCredentialRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByEmailOrMobile(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with identifier: " + identifier));

        UserCredential credential = userCredentialRepository.findByUserAccount_UserId(account.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Credentials not found for account: " + identifier));

        return buildUserDetails(account, credential);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with id: " + userId));

        UserCredential credential = userCredentialRepository.findByUserAccount_UserId(account.getUserId())
                .orElse(null);

        String password = credential != null ? credential.getPasswordHash() : "";

        return new User(
                account.getUserId().toString(),
                password,
                account.getAccountStatus() == AccountStatus.ACTIVE,
                true,
                true,
                account.getAccountStatus() != AccountStatus.SUSPENDED,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getAccountType().name()))
        );
    }

    private UserDetails buildUserDetails(UserAccount account, UserCredential credential) {
        return new User(
                account.getUserId().toString(),
                credential.getPasswordHash(),
                account.getAccountStatus() == AccountStatus.ACTIVE,
                true,
                true,
                account.getAccountStatus() != AccountStatus.SUSPENDED,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getAccountType().name()))
        );
    }
}
