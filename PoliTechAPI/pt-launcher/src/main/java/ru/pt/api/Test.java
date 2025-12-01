package ru.pt.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.auth.AccountService;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.auth.security.SecurityContextHelper;

import java.util.*;


/**
 * Test контроллер для разработки и отладки
 * <p>
 * URL Pattern: /api/v1/{tenantCode}/test/*
 * tenantCode: pt, vsk, msg
 */
@RestController
@RequestMapping("/api/v1/{tenant-code}/test")
public class Test extends SecuredController {

    private final AccountService accountService;
    private final ProcessOrchestrator processOrchestrator;

    protected Test(
        SecurityContextHelper securityContextHelper, AccountService accountService,
        ProcessOrchestrator processOrchestrator
    ) {
        super(securityContextHelper);
        this.accountService = accountService;
        this.processOrchestrator = processOrchestrator;
    }

    @PostMapping("/create-client")
    public ResponseEntity<Account> createClient(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createClient(account.getName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-group")
    public ResponseEntity<Account> createGroup(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createGroup(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-account")
    public ResponseEntity<Account> createAccount(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createAccount(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }


    @PostMapping("/create-subaccount")
    public ResponseEntity<Account> createSubaccount(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createSubaccount(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-product-roles/{accountId}")
    public ResponseEntity<Object> getProductRoles(
            @PathVariable("tenant-code") String tenantCode,
            @PathVariable("accountId") long accountId) {
        Set<String> result = accountService.getProductRoles(accountId);
        return ResponseEntity.ok(result.toArray());
    }

    @GetMapping("/get-account-login")
    public ResponseEntity<ObjectNode> getAccountLogin(
            @PathVariable("tenant-code") String tenantCode,
            @RequestHeader("login") String login,
            @RequestHeader("client") String client,
            @RequestHeader(value = "accountNr", required = false) Long accountNr) {

        ObjectNode result = accountService.getAccountLogin(login, client, accountNr);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/quote/validator")
    public ResponseEntity<String> quoteValidator(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody String requestBody) {
        String result = processOrchestrator.calculate(requestBody);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/policy/validator")
    public ResponseEntity<String> saveValidator(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody String requestBody) {
        String result = processOrchestrator.save(requestBody);
        return ResponseEntity.ok(result);
    }
}
