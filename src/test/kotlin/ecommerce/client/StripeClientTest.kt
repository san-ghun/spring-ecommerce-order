package ecommerce.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StripeClientTest(
    @Autowired val client: StripeClient,
) {
    @Test
    fun test1() {
        val amount = 100
        val currency = "USD"
        val paymentMethod = "pm_card_visa"
        val actual =
            client.createCheckoutSession(
                PaymentRequest(
                    amount = amount,
                    currency = currency,
                    paymentMethod = paymentMethod,
                ),
            )

        assertThat(actual).isNotNull()
        assertThat(actual?.amount).isEqualTo(amount)
        assertThat(actual?.`object`).isEqualTo("payment_intent")
    }
}
