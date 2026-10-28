package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.ShareTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareTransferRepository extends JpaRepository<ShareTransfer, Long> {
    List<ShareTransfer> findByToUser_UserIdOrderByTransferredAtDesc(Long userId);
    List<ShareTransfer> findByFromUser_UserIdOrderByTransferredAtDesc(Long userId);
}
