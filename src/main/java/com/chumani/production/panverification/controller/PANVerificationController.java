package com.chumani.production.panverification.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chumani.production.panverification.dto.PANVerificationRequest;
import com.chumani.production.panverification.dto.PANVerificationResponse;
import com.chumani.production.panverification.service.PANVerificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pan")
public class PANVerificationController {

    @Autowired
    private PANVerificationService service;

    @PostMapping("/verify")
    public ResponseEntity<PANVerificationResponse> verifyPAN(@Valid @RequestBody PANVerificationRequest request) {
        PANVerificationResponse result = service.verifyPAN(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{referenceNumber}")
    public ResponseEntity<PANVerificationResponse> getStatus(@PathVariable String referenceNumber) {
        return service.getVerificationStatus(referenceNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<PANVerificationResponse>> getHistory(@RequestParam String panNumber) {
        List<PANVerificationResponse> history = service.getVerificationHistory(panNumber);
        return ResponseEntity.ok(history);
    }
}
