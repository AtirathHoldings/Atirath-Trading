package com.atirath.trading.modules.auth.repository;

import com.atirath.trading.modules.auth.model.AccountDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountDocumentRepository extends JpaRepository<AccountDocument, Long> {

    List<AccountDocument> findByUserAccount_UserId(Long userId);
}
