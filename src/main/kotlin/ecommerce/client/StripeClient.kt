package ecommerce.client

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
) {
    private val restClient = RestClient.create()

    fun createCheckoutSession(req: PaymentRequest): PaymentResponse? {
        val body =
            listOf(
                "amount=${req.amount}",
                "currency=${req.currency}",
                "payment_method=${req.paymentMethod}",
                "confirm=true",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        return try {
            val response =
                restClient.post()
                    .uri("https://api.stripe.com/v1/payment_intents")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(PaymentResponse::class.java)

            response.body
        } catch (e: Exception) {
            throw IllegalArgumentException("Stripe error: ${e.message}")
        }
    }
}
