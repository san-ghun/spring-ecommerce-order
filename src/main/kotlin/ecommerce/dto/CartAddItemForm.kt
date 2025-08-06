package ecommerce.dto

import jakarta.validation.constraints.Min

data class CartAddItemForm(
    @field:Min(value = 1, message = "Product ID is missing")
    var productId: Long,
    @field:Min(value = 1, message = "Option ID is missing")
    var optionId: Long,
    @field:Min(value = 1, message = "Product quantity is too small")
    var quantity: Int,
)
