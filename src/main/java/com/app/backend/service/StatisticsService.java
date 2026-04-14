package com.app.backend.service;

import com.app.backend.dto.*;
import com.app.backend.entity.*;
import com.app.backend.repository.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final RegistrationRecordRepository registrationRecordRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientInfoRepository patientInfoRepository;
    private final DoctorInfoRepository doctorInfoRepository;
    private final HospitalDepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public StatisticsService(RegistrationRecordRepository registrationRecordRepository,
                             MedicalRecordRepository medicalRecordRepository,
                             PatientInfoRepository patientInfoRepository,
                             DoctorInfoRepository doctorInfoRepository,
                             HospitalDepartmentRepository departmentRepository,
                             UserRepository userRepository) {
        this.registrationRecordRepository = registrationRecordRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientInfoRepository = patientInfoRepository;
        this.doctorInfoRepository = doctorInfoRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        stats.put("totalPatients", patientInfoRepository.selectCount(new QueryWrapper<PatientInfo>().eq("is_deleted", 0)));
        stats.put("totalDoctors", doctorInfoRepository.selectCount(new QueryWrapper<DoctorInfo>().eq("is_deleted", 0)));
        stats.put("totalDepartments", departmentRepository.selectCount(new QueryWrapper<HospitalDepartment>().eq("is_deleted", 0)));
        stats.put("totalRegistrations", registrationRecordRepository.selectCount(new QueryWrapper<RegistrationRecord>().eq("is_deleted", 0)));
        stats.put("totalMedicalRecords", medicalRecordRepository.selectCount(new QueryWrapper<MedicalRecord>().eq("is_deleted", 0)));
        
        // Today's stats
        LocalDate today = LocalDate.now();
        stats.put("todayRegistrations", registrationRecordRepository.selectCount(
            new QueryWrapper<RegistrationRecord>()
                .eq("is_deleted", 0)
                .ge("create_time", today.atStartOfDay())
                .lt("create_time", today.plusDays(1).atStartOfDay())
        ));
        stats.put("todayMedicalRecords", medicalRecordRepository.selectCount(
            new QueryWrapper<MedicalRecord>()
                .eq("is_deleted", 0)
                .ge("create_time", today.atStartOfDay())
                .lt("create_time", today.plusDays(1).atStartOfDay())
        ));
        
        // Registration status distribution
        List<Map<String, Object>> registrationStatusStats = getRegistrationStatusDistribution();
        stats.put("registrationStatusStats", registrationStatusStats);
        
        // Department distribution
        List<Map<String, Object>> departmentStats = getDepartmentDistribution();
        stats.put("departmentStats", departmentStats);
        
        // Weekly registration trend
        List<Map<String, Object>> weeklyTrend = getWeeklyRegistrationTrend();
        stats.put("weeklyTrend", weeklyTrend);
        
        // Monthly registration trend
        List<Map<String, Object>> monthlyTrend = getMonthlyRegistrationTrend();
        stats.put("monthlyTrend", monthlyTrend);
        
        // Top doctors by registration count
        List<Map<String, Object>> topDoctors = getTopDoctorsByRegistrations();
        stats.put("topDoctors", topDoctors);
        
        // Payment status distribution
        List<Map<String, Object>> paymentStats = getPaymentStatusDistribution();
        stats.put("paymentStats", paymentStats);
        
        return stats;
    }

    private List<Map<String, Object>> getRegistrationStatusDistribution() {
        QueryWrapper<RegistrationRecord> qw = new QueryWrapper<RegistrationRecord>()
            .select("registration_status as status", "COUNT(*) as count")
            .eq("is_deleted", 0)
            .groupBy("registration_status");
        
        List<Map<String, Object>> results = registrationRecordRepository.selectMaps(qw);
        
        // Convert status numbers to labels
        Map<Integer, String> statusLabels = new HashMap<>();
        statusLabels.put(0, "待就诊");
        statusLabels.put(1, "已就诊");
        statusLabels.put(2, "已取消");
        
        return results.stream().map(item -> {
            Map<String, Object> result = new HashMap<>();
            Integer status = (Integer) item.get("status");
            result.put("name", statusLabels.getOrDefault(status, "未知状态"));
            result.put("value", item.get("count"));
            return result;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDepartmentDistribution() {
        List<RegistrationRecord> registrations = registrationRecordRepository.selectList(
            new QueryWrapper<RegistrationRecord>()
                .eq("is_deleted", 0)
                .in("registration_status", 0, 1)
        );
        Map<Long, Long> deptCountMap = registrations.stream()
            .filter(item -> item.getDeptId() != null)
            .collect(Collectors.groupingBy(RegistrationRecord::getDeptId, Collectors.counting()));

        Map<Long, String> deptNameMap = departmentRepository.selectList(
            new QueryWrapper<HospitalDepartment>().eq("is_deleted", 0)
        ).stream().collect(Collectors.toMap(HospitalDepartment::getDeptId, HospitalDepartment::getDeptName));

        return deptCountMap.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(10)
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("dept_name", deptNameMap.getOrDefault(entry.getKey(), String.valueOf(entry.getKey())));
                item.put("count", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getWeeklyRegistrationTrend() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Long count = registrationRecordRepository.selectCount(
                new QueryWrapper<RegistrationRecord>()
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

    private List<Map<String, Object>> getMonthlyRegistrationTrend() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(11);
        
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (int i = 0; i < 12; i++) {
            LocalDate monthDate = startDate.plusMonths(i);
            Long count = registrationRecordRepository.selectCount(
                new QueryWrapper<RegistrationRecord>()
                    .eq("is_deleted", 0)
                    .apply("YEAR(create_time) = {0} AND MONTH(create_time) = {1}", 
                          monthDate.getYear(), monthDate.getMonthValue())
            );
            
            Map<String, Object> item = new HashMap<>();
            item.put("month", monthDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            item.put("count", count);
            trend.add(item);
        }
        
        return trend;
    }

    private List<Map<String, Object>> getTopDoctorsByRegistrations() {
        List<RegistrationRecord> registrations = registrationRecordRepository.selectList(
            new QueryWrapper<RegistrationRecord>()
                .eq("is_deleted", 0)
                .in("registration_status", 0, 1)
        );
        Map<Long, Long> doctorCountMap = registrations.stream()
            .filter(item -> item.getDoctorId() != null)
            .collect(Collectors.groupingBy(RegistrationRecord::getDoctorId, Collectors.counting()));

        Map<Long, DoctorInfo> doctorMap = doctorInfoRepository.selectList(
            new QueryWrapper<DoctorInfo>().eq("is_deleted", 0)
        ).stream().collect(Collectors.toMap(DoctorInfo::getDoctorId, item -> item));

        Set<Long> userIds = doctorMap.values().stream()
            .map(DoctorInfo::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty()
            ? Collections.emptyMap()
            : userRepository.selectList(new QueryWrapper<User>().in("id", userIds).eq("is_deleted", 0)).stream()
                .collect(Collectors.toMap(User::getId, item -> item));

        return doctorCountMap.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(8)
            .map(entry -> {
                DoctorInfo doctor = doctorMap.get(entry.getKey());
                User user = doctor == null ? null : userMap.get(doctor.getUserId());
                Map<String, Object> item = new HashMap<>();
                item.put("real_name", user != null ? user.getRealName() : null);
                item.put("username", user != null ? user.getUsername() : null);
                item.put("count", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getPaymentStatusDistribution() {
        QueryWrapper<RegistrationRecord> qw = new QueryWrapper<RegistrationRecord>()
            .select("pay_status as status", "COUNT(*) as count")
            .eq("is_deleted", 0)
            .groupBy("pay_status");
        
        List<Map<String, Object>> results = registrationRecordRepository.selectMaps(qw);
        
        Map<Integer, String> statusLabels = new HashMap<>();
        statusLabels.put(0, "未支付");
        statusLabels.put(1, "已支付");
        statusLabels.put(2, "已退款");
        
        return results.stream().map(item -> {
            Map<String, Object> result = new HashMap<>();
            Integer status = (Integer) item.get("status");
            result.put("name", statusLabels.getOrDefault(status, "未知状态"));
            result.put("value", item.get("count"));
            return result;
        }).collect(Collectors.toList());
    }
}
