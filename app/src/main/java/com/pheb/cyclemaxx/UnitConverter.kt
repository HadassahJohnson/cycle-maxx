package com.pheb.cyclemaxx

import kotlin.math.round

object UnitConverter {
    private const val KG_TO_LBS = 2.20462
    private const val CM_TO_INCHES = 0.393701

    fun kgToLbs(kg: Double): Double = kg * KG_TO_LBS
    fun lbsToKg(lbs: Double): Double = lbs / KG_TO_LBS

    fun cmToInches(cm: Double): Double = cm * CM_TO_INCHES
    fun inchesToCm(inches: Double): Double = inches / CM_TO_INCHES

    fun format(value: Double): String = String.format("%.1f", value)
}