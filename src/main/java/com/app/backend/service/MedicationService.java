package com.app.backend.service;

import com.app.backend.dto.*;
import com.app.backend.entity.MedicationRecord;
import com.app.backend.entity.Prescription;
import com.app.backend.entity.PrescriptionItem;
import com.app.backend.entity.DoctorInfo;
import com.app.backend.entity.PatientInfo;
import com.app.backend.entity.User;
import com.app.backend.repository.DoctorInfoRepository;
import com.app.backend.repository.PatientInfoRepository;
import com.app.backend.repository.UserRepository;
import com.app.backend.repository.MedicationRecordRepository;
import com.app.backend.repository.PrescriptionItemRepository;
import com.app.backend.repository.PrescriptionRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final MedicationRecordRepository medicationRecordRepository;
    private final DoctorInfoRepository doctorInfoRepository;
    private final PatientInfoRepository patientInfoRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public List<PrescriptionDto> getPatientPrescriptions(Long patientId) {
        try {
            QueryWrapper<Prescription> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("patient_id", patientId)
                       .eq("status", "ACTIVE")
                       .orderByDesc("created_at");

            List<Prescription> prescriptions = prescriptionRepository.selectList(queryWrapper);

            return prescriptions.stream().map(this::convertToDto).collect(Collectors.toList());
        } catch (Exception e) {
            // If table doesn't exist or other error, return empty list
            return new ArrayList<>();
        }
    }

    public List<PrescriptionDto> getMyPrescriptions(Long userId) {
        try {
            // Get patientId from userId
            QueryWrapper<PatientInfo> patientQuery = new QueryWrapper<>();
            patientQuery.eq("user_id", userId).eq("is_deleted", 0);
            PatientInfo patient = patientInfoRepository.selectOne(patientQuery);
            
            if (patient == null) {
                return new ArrayList<>();
            }
            
            return getPatientPrescriptions(patient.getPatientId());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<MedicationRecordDto> getMedicationRecords(Long patientId, String startDate, String endDate) {
        QueryWrapper<MedicationRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        
        if (startDate != null) {
            queryWrapper.ge("taken_at", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("taken_at", endDate);
        }
        
        queryWrapper.orderByDesc("taken_at");
        
        List<MedicationRecord> records = medicationRecordRepository.selectList(queryWrapper);
        
        return records.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public MedicationAdherenceStatsDto getAdherenceStats(Long patientId) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);
        
        // Get all prescriptions for the patient
        QueryWrapper<Prescription> prescriptionQuery = new QueryWrapper<>();
        prescriptionQuery.eq("patient_id", patientId)
                         .eq("status", "ACTIVE");
        List<Prescription> prescriptions = prescriptionRepository.selectList(prescriptionQuery);
        
        // Calculate expected doses based on actual prescription time range
        int expectedCount = 0;
        for (Prescription prescription : prescriptions) {
            List<String> reminderTimes = parseReminderTimes(prescription.getReminderTimes());
            if (reminderTimes.isEmpty()) {
                continue;
            }
            
            LocalDateTime prescriptionStart = prescription.getStartDate();
            LocalDateTime prescriptionEnd = prescription.getEndDate();
            
            // If no start date, use visit date
            if (prescriptionStart == null) {
                prescriptionStart = prescription.getVisitDate();
            }
            // If no end date, assume 30 days from start
            if (prescriptionEnd == null && prescriptionStart != null) {
                prescriptionEnd = prescriptionStart.plusDays(30);
            }
            
            if (prescriptionStart == null) {
                continue;
            }
            
            // Calculate overlap with the 30-day period
            LocalDateTime effectiveStart = prescriptionStart.isBefore(start) ? start : prescriptionStart;
            LocalDateTime effectiveEnd = prescriptionEnd == null ? end : (prescriptionEnd.isBefore(end) ? prescriptionEnd : end);
            
            if (effectiveStart.isAfter(effectiveEnd)) {
                continue;
            }
            
            // Calculate number of days in the overlap
            long days = java.time.Duration.between(effectiveStart, effectiveEnd).toDays() + 1;
            
            // Count medication items for this prescription
            QueryWrapper<PrescriptionItem> itemQuery = new QueryWrapper<>();
            itemQuery.eq("prescription_id", prescription.getId());
            List<PrescriptionItem> items = prescriptionItemRepository.selectList(itemQuery);
            int itemCount = items != null && !items.isEmpty() ? items.size() : 1;
            
            expectedCount += reminderTimes.size() * (int) days * itemCount;
        }
        
        // Get actual taken records
        QueryWrapper<MedicationRecord> recordQuery = new QueryWrapper<>();
        recordQuery.eq("patient_id", patientId)
                  .eq("status", "TAKEN")
                  .ge("taken_at", start)
                  .le("taken_at", end);
        
        List<MedicationRecord> takenRecords = medicationRecordRepository.selectList(recordQuery);
        int takenCount = takenRecords.size();
        
        int missedCount = Math.max(expectedCount - takenCount, 0);
        int rate = expectedCount > 0 ? Math.round((float) takenCount / expectedCount * 100) : 0;
        
        MedicationAdherenceStatsDto stats = new MedicationAdherenceStatsDto();
        stats.setExpectedCount(expectedCount);
        stats.setTakenCount(takenCount);
        stats.setMissedCount(missedCount);
        stats.setRate(rate);
        
        return stats;
    }

    public MedicationRecordDto createMedicationRecord(Long patientId, MedicationRecordCreateRequest request) {
        MedicationRecord record = new MedicationRecord();
        record.setRecordId(UUID.randomUUID().toString());
        record.setPrescriptionId(request.getPrescriptionId());
        record.setPatientId(patientId);
        record.setMedicationName(request.getMedicationName());
        record.setPlannedTime(request.getPlannedTime());
        record.setTakenAt(request.getTakenAt());
        record.setDosage(request.getDosage());
        record.setFrequency(request.getFrequency());
        record.setStatus(request.getStatus());
        record.setNotes(request.getNotes());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        
        medicationRecordRepository.insert(record);
        
        return convertToDto(record);
    }

    public PrescriptionDto createPrescription(PrescriptionCreateRequest request) {
        try {
            Prescription prescription = new Prescription();
            prescription.setPrescriptionId(UUID.randomUUID().toString());
            prescription.setRecordId(request.getRecordId());
            prescription.setPatientId(request.getPatientId());
            prescription.setDoctorId(request.getDoctorId());
            prescription.setTitle(request.getTitle());
            prescription.setTreatmentPlan(request.getTreatmentPlan());
            prescription.setVisitDate(request.getVisitDate());
            prescription.setStartDate(request.getStartDate());
            prescription.setEndDate(request.getEndDate());
            prescription.setInstructions(request.getInstructions());
            prescription.setStatus("ACTIVE");

            // Set default reminder times
            List<String> defaultReminderTimes = List.of("08:00", "20:00");
            try {
                prescription.setReminderTimes(objectMapper.writeValueAsString(defaultReminderTimes));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize reminder times", e);
            }

            prescription.setCreatedAt(LocalDateTime.now());
            prescription.setUpdatedAt(LocalDateTime.now());

            prescriptionRepository.insert(prescription);

            // Create prescription items
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                for (PrescriptionItemCreateRequest itemRequest : request.getItems()) {
                    PrescriptionItem item = new PrescriptionItem();
                    item.setPrescriptionId(prescription.getPrescriptionId());
                    item.setMedicationName(itemRequest.getMedicationName());
                    item.setDosage(itemRequest.getDosage());
                    item.setFrequency(itemRequest.getFrequency());
                    item.setDuration(itemRequest.getDuration());
                    item.setNote(itemRequest.getNote());
                    item.setQuantity(itemRequest.getQuantity());
                    item.setUnit(itemRequest.getUnit());
                    item.setCreatedAt(LocalDateTime.now());
                    item.setUpdatedAt(LocalDateTime.now());
                    prescriptionItemRepository.insert(item);
                }
            }

            return convertToDto(prescription);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create prescription: " + e.getMessage() + ". Please ensure the prescriptions and prescription_items tables exist in the database.", e);
        }
    }

    public void updatePrescriptionReminders(String prescriptionId, PrescriptionUpdateRequest request) {
        QueryWrapper<Prescription> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("prescription_id", prescriptionId);

        Prescription prescription = prescriptionRepository.selectOne(queryWrapper);
        if (prescription != null) {
            try {
                String reminderTimesJson = objectMapper.writeValueAsString(request.getReminderTimes());
                prescription.setReminderTimes(reminderTimesJson);
                prescription.setUpdatedAt(LocalDateTime.now());
                prescriptionRepository.updateById(prescription);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to update reminder times", e);
            }
        }
    }

    public void deletePrescription(String prescriptionId) {
        // Delete prescription items first
        QueryWrapper<PrescriptionItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("prescription_id", prescriptionId);
        prescriptionItemRepository.delete(itemQuery);

        // Delete prescription
        QueryWrapper<Prescription> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("prescription_id", prescriptionId);
        prescriptionRepository.delete(queryWrapper);
    }

    private PrescriptionDto convertToDto(Prescription prescription) {
        PrescriptionDto dto = new PrescriptionDto();
        dto.setId(prescription.getId());
        dto.setPrescriptionId(prescription.getPrescriptionId());
        dto.setRecordId(prescription.getRecordId());
        dto.setPatientId(prescription.getPatientId());
        dto.setDoctorId(prescription.getDoctorId());
        dto.setTitle(prescription.getTitle());
        dto.setTreatmentPlan(prescription.getTreatmentPlan());
        dto.setVisitDate(prescription.getVisitDate());
        dto.setStartDate(prescription.getStartDate());
        dto.setEndDate(prescription.getEndDate());
        dto.setInstructions(prescription.getInstructions());
        dto.setStatus(prescription.getStatus());
        dto.setReminderTimes(parseReminderTimes(prescription.getReminderTimes()));
        dto.setCreatedAt(prescription.getCreatedAt());
        dto.setUpdatedAt(prescription.getUpdatedAt());
        
        // Get doctor and patient names
        if (prescription.getDoctorId() != null) {
            DoctorInfo doctor = doctorInfoRepository.selectById(prescription.getDoctorId());
            if (doctor != null && doctor.getUserId() != null) {
                User user = userRepository.selectById(doctor.getUserId());
                if (user != null) {
                    dto.setDoctorName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            }
        }
        if (prescription.getPatientId() != null) {
            PatientInfo patient = patientInfoRepository.selectById(prescription.getPatientId());
            if (patient != null && patient.getUserId() != null) {
                User user = userRepository.selectById(patient.getUserId());
                if (user != null) {
                    dto.setPatientName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            }
        }
        
        // Get prescription items
        QueryWrapper<PrescriptionItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("prescription_id", prescription.getPrescriptionId());
        List<PrescriptionItem> items = prescriptionItemRepository.selectList(itemQuery);
        dto.setItems(items.stream().map(this::convertToDto).collect(Collectors.toList()));
        
        return dto;
    }

    private PrescriptionItemDto convertToDto(PrescriptionItem item) {
        PrescriptionItemDto dto = new PrescriptionItemDto();
        dto.setId(item.getId());
        dto.setPrescriptionId(item.getPrescriptionId());
        dto.setMedicationName(item.getMedicationName());
        dto.setDosage(item.getDosage());
        dto.setFrequency(item.getFrequency());
        dto.setDuration(item.getDuration());
        dto.setNote(item.getNote());
        dto.setQuantity(item.getQuantity());
        dto.setUnit(item.getUnit());
        return dto;
    }

    private MedicationRecordDto convertToDto(MedicationRecord record) {
        MedicationRecordDto dto = new MedicationRecordDto();
        dto.setId(record.getId());
        dto.setRecordId(record.getRecordId());
        dto.setPrescriptionId(record.getPrescriptionId());
        dto.setPatientId(record.getPatientId());
        dto.setMedicationName(record.getMedicationName());
        dto.setPlannedTime(record.getPlannedTime());
        dto.setTakenAt(record.getTakenAt());
        dto.setDosage(record.getDosage());
        dto.setFrequency(record.getFrequency());
        dto.setStatus(record.getStatus());
        dto.setNotes(record.getNotes());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }

    private List<String> parseReminderTimes(String reminderTimesJson) {
        if (reminderTimesJson == null || reminderTimesJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(reminderTimesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
