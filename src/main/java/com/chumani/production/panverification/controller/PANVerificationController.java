package com.chumani.production.panverification.controller;

import com.chumani.production.panverification.entity.PANVerificationRecord;
import com.chumani.production.panverification.service.PANVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pan")
public class PANVerificationController {

    @Autowired
    private PANVerificationService service;

    @PostMapping("/verify")
    public ResponseEntity<PANVerificationRecord> verifyPAN(@RequestParam String panNumber, @RequestParam String name) {
        PANVerificationRecord result = service.verifyPAN(panNumber, name);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{referenceNumber}")
    public ResponseEntity<PANVerificationRecord> getStatus(@PathVariable String referenceNumber) {
        return service.getByReferenceNumber(referenceNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<PANVerificationRecord>> getHistory(@RequestParam String panNumber) {
        List<PANVerificationRecord> history = service.getHistory(panNumber);
        return ResponseEntity.ok(history);
    }
}