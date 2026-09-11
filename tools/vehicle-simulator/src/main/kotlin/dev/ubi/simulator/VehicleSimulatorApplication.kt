package dev.ubi.simulator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VehicleSimulatorApplication

fun main(args: Array<String>) {
    runApplication<VehicleSimulatorApplication>(*args)
}
