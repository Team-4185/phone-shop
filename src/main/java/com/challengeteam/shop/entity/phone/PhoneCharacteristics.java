package com.challengeteam.shop.entity.phone;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class PhoneCharacteristics {

    @Column(name = "cpu", nullable = false, length = 30)
    private String cpu;

    @Column(name = "cores_number", nullable = false)
    private Integer coresNumber;

    @Column(name = "screen_size", nullable = false, length = 10)
    private String screenSize;

    @Column(name = "front_camera", nullable = false, length = 20)
    private String frontCamera;

    @Column(name = "main_camera", nullable = false, length = 30)
    private String mainCamera;

    @Column(name = "battery_capacity", nullable = false, length = 20)
    private String batteryCapacity;

    @Column(name = "storage_capacities", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<StorageCapacity> storageCapacities;

    @Column(name = "phone_colors", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<PhoneColor> phoneColors;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PhoneCharacteristics phoneCharacteristics = (PhoneCharacteristics) o;

        return Objects.equals(cpu, phoneCharacteristics.cpu)
                && Objects.equals(coresNumber, phoneCharacteristics.coresNumber)
                && Objects.equals(screenSize, phoneCharacteristics.screenSize)
                && Objects.equals(frontCamera, phoneCharacteristics.frontCamera)
                && Objects.equals(mainCamera, phoneCharacteristics.mainCamera)
                && Objects.equals(batteryCapacity, phoneCharacteristics.batteryCapacity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpu, coresNumber, screenSize, frontCamera, mainCamera, batteryCapacity);
    }

    @Override
    public String toString() {
        return "PhoneCharacteristics{" +
                "cpu='" + cpu + '\'' +
                ", coresNumber='" + coresNumber + '\'' +
                ", screenSize='" + screenSize + '\'' +
                ", frontCamera='" + frontCamera + '\'' +
                ", mainCamera='" + mainCamera + '\'' +
                ", batteryCapacity='" + batteryCapacity + '\'' +
                '}';
    }

}
