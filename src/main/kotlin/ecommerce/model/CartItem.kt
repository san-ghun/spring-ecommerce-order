package ecommerce.model

import ecommerce.dto.CartItemResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "cart_items")
class CartItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    var option: Option,
    @Column(nullable = false)
    var quantity: Int = 1,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    fun changeQuantity(quantity: Int) {
        this.quantity += quantity
    }

    companion object {
        fun to(cartItem: CartItem): CartItemResponse {
            return CartItemResponse(
                productName = cartItem.product.name,
                optionName = cartItem.option.name,
                quantity = cartItem.quantity,
                createdAt = cartItem.createdAt,
            )
        }
    }
}
