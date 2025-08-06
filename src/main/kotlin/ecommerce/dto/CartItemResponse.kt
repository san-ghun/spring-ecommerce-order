package ecommerce.dto

import java.time.LocalDateTime

data class CartItemResponse(
    val productName: String,
    val optionName: String,
    val quantity: Int,
    val createdAt: LocalDateTime,
)
