// File: com/iesvdc/acceso/quicksales/data/datasource/network/models/AmountRequest.kt

package com.iesvdc.acceso.quicksales.data.datasource.network.models

import com.iesvdc.acceso.quicksales.domain.serializers.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
class AmountRequest(
    @Serializable(with = BigDecimalSerializer::class)
    val cantidad: BigDecimal
)
