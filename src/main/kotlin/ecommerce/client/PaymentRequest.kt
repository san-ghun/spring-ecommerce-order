package ecommerce.client

data class PaymentRequest(
    val amount: Int,
    val currency: String,
    val paymentMethod: String,
)
