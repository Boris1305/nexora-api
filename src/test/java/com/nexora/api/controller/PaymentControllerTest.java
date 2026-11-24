
package com.nexora.api.controller;

import com.nexora.api.dto.PaymentResponse;
import com.nexora.api.enums.PaymentStatus;
import com.nexora.api.service.PaymentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    private static final OffsetDateTime NOW =
            OffsetDateTime.parse("2026-11-12T10:00:00-03:00");

    private PaymentResponse createResponse(PaymentStatus status) {

        String reviewNotes = switch (status) {
            case APPROVED -> "Pago verificado correctamente";
            case REJECTED -> "Comprobante ilegible";
            default -> null;
        };

        return new PaymentResponse(
                1L,
                1L,
                new BigDecimal("50000.00"),
                status,
                "comprobantes/pago-001.jpg",
                NOW.plusHours(48),
                status == PaymentStatus.PENDING ? null : NOW,
                status == PaymentStatus.APPROVED
                        || status == PaymentStatus.REJECTED ? NOW : null,
                reviewNotes,
                NOW,
                NOW
        );
    }

    @Test
    void shouldReturnPaymentById() throws Exception {
        PaymentResponse payment = createResponse(PaymentStatus.PENDING);

        when(paymentService.getPaymentById(1L)).thenReturn(payment);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.appointmentId").value(1))
                .andExpect(jsonPath("$.amount").value(50000.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnPaymentByAppointmentId() throws Exception {
        PaymentResponse payment = createResponse(PaymentStatus.SUBMITTED);

        when(paymentService.getPaymentByAppointmentId(1L))
                .thenReturn(payment);

        mockMvc.perform(get("/api/payments/appointment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(1))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.receiptReference")
                        .value("comprobantes/pago-001.jpg"));
    }

    @Test
    void shouldSubmitReceipt() throws Exception {
        PaymentResponse payment = createResponse(PaymentStatus.SUBMITTED);

        when(paymentService.submitReceipt(eq(1L), any()))
                .thenReturn(payment);

        String requestBody = """
                {
                    "receiptReference": "comprobantes/pago-001.jpg"
                }
                """;

        mockMvc.perform(post("/api/payments/1/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.receiptReference")
                        .value("comprobantes/pago-001.jpg"));

        verify(paymentService).submitReceipt(eq(1L), any());
    }

    @Test
    void shouldRejectReceiptWithInvalidData() throws Exception {
        String requestBody = """
                {
                    "receiptReference": ""
                }
                """;

        mockMvc.perform(post("/api/payments/1/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).submitReceipt(eq(1L), any());
    }

    @Test
    void shouldApprovePayment() throws Exception {
        PaymentResponse payment = createResponse(PaymentStatus.APPROVED);

        when(paymentService.approvePayment(eq(1L), any()))
                .thenReturn(payment);

        String requestBody = """
                {
                    "reviewNotes": "Pago verificado correctamente"
                }
                """;

        mockMvc.perform(patch("/api/payments/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewNotes")
                        .value("Pago verificado correctamente"));

        verify(paymentService).approvePayment(eq(1L), any());
    }

    @Test
    void shouldRejectPayment() throws Exception {
        PaymentResponse payment = createResponse(PaymentStatus.REJECTED);

        when(paymentService.rejectPayment(eq(1L), any()))
                .thenReturn(payment);

        String requestBody = """
                {
                    "reviewNotes": "Comprobante ilegible"
                }
                """;

        mockMvc.perform(patch("/api/payments/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reviewNotes")
                        .value("Comprobante ilegible"));

        verify(paymentService).rejectPayment(eq(1L), any());
    }

    @Test
    void shouldRejectReviewNotesExceedingMaximumLength() throws Exception {
        String longNotes = "A".repeat(501);

        String requestBody = """
                {
                    "reviewNotes": "%s"
                }
                """.formatted(longNotes);

        mockMvc.perform(patch("/api/payments/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).approvePayment(eq(1L), any());
    }
}
