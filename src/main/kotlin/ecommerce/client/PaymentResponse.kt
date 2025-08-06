package ecommerce.client

data class PaymentResponse(
    val id: String,
    val `object`: String,
    val amount: Int,
)
