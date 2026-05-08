package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.MedicationAdherenceStatsDto;
import com.app.backend.dto.PrescriptionCreateRequest;
import com.app.backend.dto.PrescriptionDto;
import com.app.backend.service.MedicationService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final MedicationService medicationService;

    public PrescriptionController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping
    public ApiResponse<PrescriptionDto> create(HttpServletRequest request, @Valid @RequestBody PrescriptionCreateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        // TODO: Verify that the doctor is creating prescription for their own patient
        return ApiResponse.ok(medicationService.createPrescription(req));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<List<PrescriptionDto>> getPatientPrescriptions(HttpServletRequest request, @PathVariable Long patientId) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        // TODO: Verify that the doctor has permission to view this patient's prescriptions
        return ApiResponse.ok(medicationService.getPatientPrescriptions(patientId));
    }

    @GetMapping("/patient/me")
    public ApiResponse<List<PrescriptionDto>> getMyPrescriptions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(medicationService.getMyPrescriptions(userId));
    }

    @GetMapping("/patient/{patientId}/adherence-stats")
    public ApiResponse<MedicationAdherenceStatsDto> getPatientAdherenceStats(HttpServletRequest request, @PathVariable Long patientId) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        // TODO: Verify that the doctor has permission to view this patient's stats
        return ApiResponse.ok(medicationService.getAdherenceStats(patientId));
    }

    @DeleteMapping("/{prescriptionId}")
    public ApiResponse<Void> deletePrescription(HttpServletRequest request, @PathVariable String prescriptionId) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        // TODO: Verify that the doctor has permission to delete this prescription
        medicationService.deletePrescription(prescriptionId);
        return ApiResponse.ok(null);
    }
}
