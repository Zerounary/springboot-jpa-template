package com.app.backend.util;

/**
 * 健康风险评估工具类
 * 根据血压、血糖、心率、体温数据计算患者风险等级
 */
public class HealthRiskAssessment {

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        HIGH("高风险"),
        MEDIUM("中风险"),
        LOW("低风险");

        private final String displayName;

        RiskLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 计算健康风险等级
     *
     * @param systolicPressure 收缩压
     * @param diastolicPressure 舒张压
     * @param bloodGlucose 血糖
     * @param heartRate 心率
     * @param bodyTemperature 体温
     * @return 风险等级
     */
    public static RiskLevel assessRisk(Integer systolicPressure, Integer diastolicPressure,
                                       Double bloodGlucose, Integer heartRate, Double bodyTemperature) {
        // 检查血压是否异常（≥ 140/90）
        boolean isHighBloodPressure = (systolicPressure != null && systolicPressure >= 140) ||
                                      (diastolicPressure != null && diastolicPressure >= 90);

        // 检查血糖是否异常（≥ 7.0）
        boolean isHighBloodGlucose = bloodGlucose != null && bloodGlucose >= 7.0;

        // 检查心率体温是否异常
        boolean isHeartRateAbnormal = heartRate != null && (heartRate < 60 || heartRate > 100);
        boolean isTemperatureAbnormal = bodyTemperature != null && (bodyTemperature < 36.0 || bodyTemperature > 37.5);
        boolean isHeartRateOrTempAbnormal = isHeartRateAbnormal || isTemperatureAbnormal;

        // 应用风险评估算法
        if (isHighBloodPressure) {
            if (isHighBloodGlucose) {
                return RiskLevel.HIGH;
            } else {
                if (isHeartRateOrTempAbnormal) {
                    return RiskLevel.MEDIUM;
                } else {
                    return RiskLevel.LOW;
                }
            }
        } else {
            if (isHeartRateOrTempAbnormal) {
                return RiskLevel.MEDIUM;
            } else {
                return RiskLevel.LOW;
            }
        }
    }

    /**
     * 计算健康风险等级（简化版，仅基于最新健康监测数据）
     *
     * @param systolicPressure 收缩压
     * @param diastolicPressure 舒张压
     * @param bloodGlucose 血糖
     * @param heartRate 心率
     * @param bodyTemperature 体温
     * @return 风险等级显示名称
     */
    public static String assessRiskDisplayName(Integer systolicPressure, Integer diastolicPressure,
                                             Double bloodGlucose, Integer heartRate, Double bodyTemperature) {
        return assessRisk(systolicPressure, diastolicPressure, bloodGlucose, heartRate, bodyTemperature).getDisplayName();
    }
}
