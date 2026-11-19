package com.atirath.trading.modules.auth.service;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.modules.auth.model.AdminAccount;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.model.UserCredential;
import com.atirath.trading.modules.auth.repository.AdminAccountRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final AdminAccountRepository adminAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<AdminAccount> adminOpt = adminAccountRepository.findByEmailIgnoreCase(identifier);
        if (adminOpt.isPresent()) {
            AdminAccount admin = adminOpt.get();
            boolean active = "ACTIVE".equalsIgnoreCase(admin.getAdminStatus());
            return new User(
                    admin.getAdminId().toString(),
                    admin.getPasswordHash(),
                    active,
                    true,
                    true,
                    active,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        UserAccount account = userAccountRepository.findByEmailOrMobile(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with identifier: " + identifier));

        UserCredential credential = userCredentialRepository.findByUserAccount_UserId(account.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Credentials not found for account: " + identifier));

        return buildUserDetails(account, credential);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        Optional<UserAccount> accountOpt = userAccountRepository.findById(userId);
        if (accountOpt.isPresent()) {
            UserAccount account = accountOpt.get();
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

        Optional<AdminAccount> adminOpt = adminAccountRepository.findById(userId);
        if (adminOpt.isPresent()) {
            AdminAccount admin = adminOpt.get();
            boolean active = "ACTIVE".equalsIgnoreCase(admin.getAdminStatus());
            return new User(
                    admin.getAdminId().toString(),
                    admin.getPasswordHash(),
                    active,
                    true,
                    true,
                    active,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        throw new UsernameNotFoundException("Account not found with id: " + userId);
    }

    @Transactional(readOnly = true)
    public UserDetails loadAdminById(Long adminId) {
        AdminAccount admin = adminAccountRepository.findById(adminId)
                .orElse(null);

        String password = admin != null ? admin.getPasswordHash() : "";
        boolean active = admin != null && "ACTIVE".equalsIgnoreCase(admin.getAdminStatus());

        return new User(
                adminId.toString(),
                password,
                active,
                true,
                true,
                active,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
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
