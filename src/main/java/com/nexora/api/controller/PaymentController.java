package com.nexora.api.controller;

import com.nexora.api.dto.PaymentResponse;
import com.nexora.api.dto.SubmitPaymentReceiptRequest;
import com.nexora.api.dto.ReviewPaymentRequest;
import com.nexora.api.service.PaymentService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/appointment/{appointmentId}")
    public PaymentResponse getPaymentByAppointmentId(
            @PathVariable Long appointmentId) {
        return paymentService.getPaymentByAppointmentId(appointmentId);
    }

    @PostMapping("/{id}/receipt")
    public PaymentResponse submitReceipt(
            @PathVariable Long id,
            @Valid @RequestBody SubmitPaymentReceiptRequest request) {

        return paymentService.submitReceipt(id, request);
    }

    @PatchMapping("/{id}/approve")
    public PaymentResponse approvePayment(
            @PathVariable Long id,
            @Valid @RequestBody ReviewPaymentRequest request) {

        return paymentService.approvePayment(id, request);
    }

    @PatchMapping("/{id}/reject")
    public PaymentResponse rejectPayment(
            @PathVariable Long id,
            @Valid @RequestBody ReviewPaymentRequest request) {

        return paymentService.rejectPayment(id, request);
    }
}