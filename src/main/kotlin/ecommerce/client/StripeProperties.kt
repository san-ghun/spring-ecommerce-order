package ecommerce.client

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("stripe")
data class StripeProperties(
    val secretKey: String,
)
