package com.app.backend.service;

import com.app.backend.entity.*;
import com.app.backend.repository.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientStatisticsService {

    private final RegistrationRecordRepository registrationRecordRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final HealthMonitorRepository healthMonitorRepository;
    private final MedicationRecordRepository medicationRecordRepository;
    private final DoctorInfoRepository doctorInfoRepository;
    private final UserRepository userRepository;
    private final PatientInfoRepository patientInfoRepository;

    public PatientStatisticsService(RegistrationRecordRepository registrationRecordRepository,
                                   MedicalRecordRepository medicalRecordRepository,
                                   HealthMonitorRepository healthMonitorRepository,
                                   MedicationRecordRepository medicationRecordRepository,
                                   DoctorInfoRepository doctorInfoRepository,
                                   UserRepository userRepository,
                                   PatientInfoRepository patientInfoRepository) {
        this.registrationRecordRepository = registrationRecordRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.healthMonitorRepository = healthMonitorRepository;
        this.medicationRecordRepository = medicationRecordRepository;
        this.doctorInfoRepository = doctorInfoRepository;
        this.userRepository = userRepository;
        this.patientInfoRepository = patientInfoRepository;
    }

    public Map<String, Object> getPatientStatistics(Long patientId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Personal counts
        stats.put("totalRegistrations", registrationRecordRepository.selectCount(
            new QueryWrapper<RegistrationRecord>().eq("patient_id", patientId).eq("is_deleted", 0)
        ));
        stats.put("totalMedicalRecords", medicalRecordRepository.selectCount(
            new QueryWrapper<MedicalRecord>().eq("patient_id", patientId).eq("is_deleted", 0)
        ));
        stats.put("totalHealthRecords", healthMonitorRepository.selectCount(
            new QueryWrapper<HealthMonitor>().eq("patient_id", patientId).eq("is_deleted", 0)
        ));
        stats.put("totalMedicationRecords", medicationRecordRepository.selectCount(
            new QueryWrapper<MedicationRecord>().eq("patient_id", patientId)
        ));
        
        // Recent activity
        LocalDate today = LocalDate.now();
        stats.put("todayRegistrations", registrationRecordRepository.selectCount(
            new QueryWrapper<RegistrationRecord>()
                .eq("patient_id", patientId)
                .eq("is_deleted", 0)
                .ge("create_time", today.atStartOfDay())
                .lt("create_time", today.plusDays(1).atStartOfDay())
        ));
        stats.put("thisMonthRecords", medicalRecordRepository.selectCount(
            new QueryWrapper<MedicalRecord>().eq("patient_id", patientId).eq("is_deleted", 0)
                .apply("YEAR(create_time) = {0} AND MONTH(create_time) = {1}", 
                      today.getYear(), today.getMonthValue())
        ));
        
        // Registration trend (last 7 days)
        List<Map<String, Object>> registrationTrend = getPatientRegistrationTrend(patientId);
        stats.put("registrationTrend", registrationTrend);
        
        // Health monitoring trend (last 7 days)
        List<Map<String, Object>> healthTrend = getPatientHealthTrend(patientId);
        stats.put("healthTrend", healthTrend);
        
        // Medication adherence (last 30 days)
        Map<String, Object> medicationAdherence = getPatientMedicationAdherence(patientId);
        stats.put("medicationAdherence", medicationAdherence);
        
        // Recent visits (last 5)
        List<Map<String, Object>> recentVisits = getPatientRecentVisits(patientId);
        stats.put("recentVisits", recentVisits);
        
        return stats;
    }

    private List<Map<String, Object>> getPatientRegistrationTrend(Long patientId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Long count = registrationRecordRepository.selectCount(
                new QueryWrapper<RegistrationRecord>()
                    .eq("patient_id", patientId)
                    .eq("is_deleted", 0)
                    .ge("create_time", date.atStartOfDay())
                    .lt("create_time", date.plusDays(1).atStartOfDay())
            );
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            item.put("count", count);
            trend.add(item);
        }
        
        return trend;
    }

    private List<Map<String, Object>> getPatientHealthTrend(Long patientId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Long count = healthMonitorRepository.selectCount(
                new QueryWrapper<HealthMonitor>()
                    .eq("patient_id", patientId)
                    .eq("is_deleted", 0)
                    .ge("create_time", date.atStartOfDay())
                    .lt("create_time", date.plusDays(1).atStartOfDay())
            );
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            item.put("count", count);
            trend.add(item);
        }
        
        return trend;
    }

    private Map<String, Object> getPatientMedicationAdherence(Long patientId) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        
        // Total scheduled medications
        Long totalScheduled = medicationRecordRepository.selectCount(
            new QueryWrapper<MedicationRecord>()
                .eq("patient_id", patientId)
                .ge("planned_time", thirtyDaysAgo.atStartOfDay())
        );
        
        // Actually taken medications
        Long takenCount = medicationRecordRepository.selectCount(
            new QueryWrapper<MedicationRecord>()
                .eq("patient_id", patientId)
                .eq("is_deleted", 0)
                .eq("status", "TAKEN")
                .ge("planned_time", thirtyDaysAgo.atStartOfDay())
        );
        
        // Missed medications
        Long missedCount = medicationRecordRepository.selectCount(
            new QueryWrapper<MedicationRecord>()
                .eq("patient_id", patientId)
                .eq("is_deleted", 0)
                .eq("status", "MISSED")
                .ge("planned_time", thirtyDaysAgo.atStartOfDay())
        );
        
        Map<String, Object> adherence = new HashMap<>();
        adherence.put("totalScheduled", totalScheduled);
        adherence.put("takenCount", takenCount);
        adherence.put("missedCount", missedCount);
        
        if (totalScheduled > 0) {
            double adherenceRate = (double) takenCount / totalScheduled * 100;
            adherence.put("adherenceRate", Math.round(adherenceRate * 10.0) / 10.0);
        } else {
            adherence.put("adherenceRate", 0.0);
        }
        
        return adherence;
    }

    private List<Map<String, Object>> getPatientRecentVisits(Long patientId) {
        List<MedicalRecord> records = medicalRecordRepository.selectList(
            new QueryWrapper<MedicalRecord>()
                .eq("patient_id", patientId)
                .eq("is_deleted", 0)
                .orderByDesc("visit_date")
                .last("LIMIT 5")
        );

        Set<Long> doctorIds = records.stream()
            .map(MedicalRecord::getDoctorId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, DoctorInfo> doctorMap = doctorIds.isEmpty()
            ? Collections.emptyMap()
            : doctorInfoRepository.selectList(new QueryWrapper<DoctorInfo>().in("doctor_id", doctorIds).eq("is_deleted", 0)).stream()
                .collect(Collectors.toMap(DoctorInfo::getDoctorId, item -> item));

        return records.stream().map(record -> {
            DoctorInfo doctor = doctorMap.get(record.getDoctorId());
            Map<String, Object> item = new HashMap<>();
            item.put("recordId", record.getRecordId());
            item.put("visitDate", record.getVisitDate());
            item.put("diagnosis", record.getDiagnosis());
            item.put("jobTitle", doctor != null ? doctor.getJobTitle() : null);
            item.put("doctorName", doctor != null ? doctor.getRealName() : null);
            return item;
        }).collect(Collectors.toList());
    }
}
