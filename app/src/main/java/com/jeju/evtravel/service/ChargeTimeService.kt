// 파일명: ChargeTimeService.kt
package com.example.evcharging.service

import kotlin.math.round

class ChargeTimeService {

    // 차종별 배터리 용량(kWh)
    private val batteryCapacityMap = mapOf(
        // 국산차
        "Hyundai Ioniq 5" to 77.4,
        "Hyundai Ioniq 6" to 84.0,
        "Hyundai Kona Electric" to 64.8,
        "Kia EV6" to 77.4,
        "Kia EV9" to 99.8,
        "Kia Niro EV" to 64.8,
        "Genesis GV60" to 77.4,
        "Genesis Electrified G80" to 87.2,
        "Genesis Electrified GV70" to 77.4,
        "KG Mobility Torres EVX" to 73.4,
        "Chevrolet Bolt EV" to 66.0,

        // 수입차
        "Tesla Model 3 LR" to 82.0,
        "Tesla Model Y LR" to 84.8,
        "Tesla Model S" to 100.0,
        "Tesla Model X" to 100.0,
        "Mini Cooper SE" to 54.2,
        "Mini Aceman" to 54.2,
        "BMW i4" to 84.0,
        "BMW i5" to 81.2,
        "BMW iX3" to 74.0,
        "BMW iX" to 111.5,
        "Mercedes-Benz EQA" to 66.5,
        "Mercedes-Benz EQB" to 66.5,
        "Mercedes-Benz EQC" to 80.0,
        "Mercedes-Benz EQE" to 88.8,
        "Mercedes-Benz EQS" to 107.8,
        "Audi Q4 e-tron" to 82.0,
        "Audi e-tron GT" to 93.4,
        "Volkswagen ID.4" to 82.0,
        "Volvo C40 Recharge" to 78.0,
        "Volvo XC40 Recharge" to 78.0,
        "Polestar 2" to 78.0,
        "Porsche Taycan" to 93.4,
        "Peugeot e-208" to 50.0,
        "Peugeot e-2008" to 50.0
    )

    /**
     * 예상 충전 시간 계산
     * @param carModel 차량명
     * @param chargerKw 충전기 출력 (kW)
     * @param currentSoc 현재 SOC (%)
     * @param targetSoc 목표 SOC (%)
     * @param efficiency 충전 효율 (0~1), 기본 0.9
     * @return 예상 충전 시간(시간, 소수점 2자리)
     */
    fun estimateChargeTime(
        carModel: String,
        chargerKw: Double,
        currentSoc: Double = 0.0,
        targetSoc: Double = 100.0,

        efficiency: Double = 0.9
    ): Double {
        val batteryCapacity = batteryCapacityMap[carModel]
            ?: throw IllegalArgumentException("$carModel 정보가 없습니다.")

        if (currentSoc >= targetSoc) return 0.0

        val socFraction = (targetSoc - currentSoc) / 100.0
        val timeHours = (batteryCapacity * socFraction) / (chargerKw * efficiency)

        // 소수점 2자리 반올림
        return round(timeHours * 100) / 100
    }
}
