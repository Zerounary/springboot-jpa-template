package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.MedicationAdherenceStatsDto;
import com.app.backend.dto.MedicationRecordCreateRequest;
import com.app.backend.dto.MedicationRecordDto;
import com.app.backend.dto.PrescriptionDto;
import com.app.backend.dto.PrescriptionUpdateRequest;
import com.app.backend.service.MedicationService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping("/prescriptions")
    public ApiResponse<List<PrescriptionDto>> getPrescriptions(HttpServletRequest request) {
        Long patientId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        List<PrescriptionDto> prescriptions = medicationService.getPatientPrescriptions(patientId);
        return ApiResponse.ok(prescriptions);
    }

    @GetMapping("/medication-records")
    public ApiResponse<List<MedicationRecordDto>> getMedicationRecords(
            HttpServletRequest request,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long patientId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        List<MedicationRecordDto> records = medicationService.getMedicationRecords(patientId, startDate, endDate);
        return ApiResponse.ok(records);
    }

    @PostMapping("/medication-records")
    public ApiResponse<MedicationRecordDto> createMedicationRecord(
            HttpServletRequest request,
            @Valid @RequestBody MedicationRecordCreateRequest createRequest) {
        Long patientId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        MedicationRecordDto record = medicationService.createMedicationRecord(patientId, createRequest);
        return ApiResponse.ok(record);
    }

    @GetMapping("/medication-adherence-stats")
    public ApiResponse<MedicationAdherenceStatsDto> getAdherenceStats(HttpServletRequest request) {
        Long patientId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        MedicationAdherenceStatsDto stats = medicationService.getAdherenceStats(patientId);
        return ApiResponse.ok(stats);
    }

    @PutMapping("/prescriptions/{prescriptionId}/reminders")
    public ApiResponse<Void> updatePrescriptionReminders(
            HttpServletRequest request,
            @PathVariable String prescriptionId,
            @Valid @RequestBody PrescriptionUpdateRequest updateRequest) {
        medicationService.updatePrescriptionReminders(prescriptionId, updateRequest);
        return ApiResponse.ok();
    }
}
