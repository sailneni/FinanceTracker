package com.financetracker.service;

import com.financetracker.dto.TransactionNotificationRequest;
import com.financetracker.dto.TransactionRequest;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.UserInfo;
import com.financetracker.publisher.TransactionEventPublisher;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionEventPublisher eventPublisher;

    private UserInfo getCurrentUser() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        return userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        UserInfo user = getCurrentUser();

        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setType(request.getType());
        tx.setCategory(request.getCategory());
        tx.setAmount(request.getAmount());
        tx.setDescription(request.getDescription());
        tx.setDateTime(
                request.getDateTime() != null ? request.getDateTime() : LocalDateTime.now()
        );

        Transaction saved = transactionRepository.save(tx);
        //Adding RabbitMQ related notification service
        TransactionNotificationRequest notify = new TransactionNotificationRequest();
        notify.setToEmail(user.getEmail());
        notify.setAmount(saved.getAmount());
        notify.setCategory(saved.getCategory().toString());
        notify.setType(saved.getType().toString());

        eventPublisher.publishTransactionEvent(notify);

        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        UserInfo user = getCurrentUser();

        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        tx.setType(request.getType());
        tx.setCategory(request.getCategory());
        tx.setAmount(request.getAmount());
        tx.setDescription(request.getDescription());
        tx.setDateTime(request.getDateTime() != null ? request.getDateTime() : tx.getDateTime());

        return toResponse(transactionRepository.save(tx));
    }

    public TransactionResponse getById(Long id) {
        UserInfo user = getCurrentUser();

        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return toResponse(tx);
    }

    public List<TransactionResponse> getAllForCurrentUser() {
        UserInfo user = getCurrentUser();
        return transactionRepository.findByUser(user)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        UserInfo user = getCurrentUser();

        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        transactionRepository.delete(tx);
    }

    private TransactionResponse toResponse(Transaction tx) {
        TransactionResponse resp = new TransactionResponse();
        resp.setId(tx.getId());
        resp.setType(tx.getType());
        resp.setCategory(tx.getCategory());
        resp.setAmount(tx.getAmount());
        resp.setDescription(tx.getDescription());
        resp.setDateTime(tx.getDateTime());
        return resp;
    }
}
