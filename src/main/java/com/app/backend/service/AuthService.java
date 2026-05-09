package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.LoginRequest;
import com.app.backend.dto.RegisterRequest;
import com.app.backend.dto.UserCreateRequest;
import com.app.backend.dto.UserDto;
import com.app.backend.entity.User;
import com.app.backend.entity.PatientInfo;
import com.app.backend.entity.DoctorInfo;
import com.app.backend.repository.PatientInfoRepository;
import com.app.backend.repository.DoctorInfoRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final PatientInfoRepository patientInfoRepository;
    private final DoctorInfoRepository doctorInfoRepository;

    public AuthService(UserService userService, PasswordService passwordService, JwtService jwtService,
                      PatientInfoRepository patientInfoRepository, DoctorInfoRepository doctorInfoRepository) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.patientInfoRepository = patientInfoRepository;
        this.doctorInfoRepository = doctorInfoRepository;
    }

    @Transactional
    public UserDto register(RegisterRequest req) {
        return userService.create(toUserCreateRequest(req));
    }

    @Transactional
    public String login(LoginRequest req) {
        Integer roleType = req.getRoleType();

        if (roleType == 1) {
            // Admin login using users table
            User user = userService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new BizException(400, "用户名或密码错误"));
            if (!passwordService.matches(req.getPassword(), user.getPasswordHash())) {
                throw new BizException(400, "用户名或密码错误");
            }
            return jwtService.createToken(user.getId(), user.getUsername(), roleType);
        } else if (roleType == 2) {
            // Doctor login using doctor_info table
            QueryWrapper<DoctorInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", req.getUsername());
            DoctorInfo doctor = doctorInfoRepository.selectOne(queryWrapper);
            if (doctor == null) {
                throw new BizException(400, "用户名或密码错误");
            }
            if (!passwordService.matches(req.getPassword(), doctor.getPasswordHash())) {
                throw new BizException(400, "用户名或密码错误");
            }
            return jwtService.createToken(doctor.getDoctorId(), doctor.getUsername(), roleType);
        } else if (roleType == 3) {
            // Patient login using patient_info table
            QueryWrapper<PatientInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", req.getUsername());
            PatientInfo patient = patientInfoRepository.selectOne(queryWrapper);
            if (patient == null) {
                throw new BizException(400, "用户名或密码错误");
            }
            if (!passwordService.matches(req.getPassword(), patient.getPasswordHash())) {
                throw new BizException(400, "用户名或密码错误");
            }
            return jwtService.createToken(patient.getPatientId(), patient.getUsername(), roleType);
        } else {
            throw new BizException(400, "无效的角色类型");
        }
    }

    @Transactional(readOnly = true)
    public UserDto currentUser(Long userId, Integer roleType) {
        if (userId == null) {
            throw new BizException(401, "未登录");
        }

        if (roleType == null) {
            throw new BizException(401, "角色类型缺失");
        }

        if (roleType == 1) {
            // Admin - query from users table
            return userService.detail(userId);
        } else if (roleType == 2) {
            // Doctor - query from doctor_info table
            QueryWrapper<DoctorInfo> doctorQuery = new QueryWrapper<>();
            doctorQuery.eq("doctor_id", userId);
            DoctorInfo doctor = doctorInfoRepository.selectOne(doctorQuery);
            if (doctor == null) {
                throw new BizException(404, "医生不存在");
            }
            UserDto dto = new UserDto();
            dto.setId(doctor.getDoctorId());
            dto.setUsername(doctor.getUsername());
            dto.setRealName(doctor.getRealName());
            dto.setRoleType(2);
            dto.setStatus(1);
            return dto;
        } else if (roleType == 3) {
            // Patient - query from patient_info table
            QueryWrapper<PatientInfo> patientQuery = new QueryWrapper<>();
            patientQuery.eq("patient_id", userId);
            PatientInfo patient = patientInfoRepository.selectOne(patientQuery);
            if (patient == null) {
                throw new BizException(404, "患者不存在");
            }
            UserDto dto = new UserDto();
            dto.setId(patient.getPatientId());
            dto.setUsername(patient.getUsername());
            dto.setRealName(patient.getRealName());
            dto.setRoleType(3);
            dto.setStatus(1);
            return dto;
        } else {
            throw new BizException(400, "无效的角色类型");
        }
    }

    private UserCreateRequest toUserCreateRequest(RegisterRequest req) {
        UserCreateRequest u = new UserCreateRequest();
        u.setUsername(req.getUsername());
        u.setPassword(req.getPassword());
        u.setNickname(req.getNickname());
        u.setRealName(req.getRealName());
        u.setPhone(req.getPhone());
        u.setRoleType(3);
        u.setStatus(1);
        return u;
    }
}
