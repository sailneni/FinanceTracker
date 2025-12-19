package com.financetracker.repository;

import com.financetracker.entity.Category;
import com.financetracker.entity.TansactionType;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(UserInfo user);

    List<Transaction> findByUserAndType(UserInfo user, TansactionType type);

    List<Transaction> findByUserAndCategory(UserInfo user, Category category);

    List<Transaction> findByUserAndDateTimeBetween(
            UserInfo user,
            LocalDateTime start,
            LocalDateTime end
    );
}
