package ecommerce.service

import ecommerce.exception.NotFoundException
import ecommerce.model.Member
import ecommerce.repository.CartItemRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class CartItemServiceTest(
    @Autowired private val cartItemService: CartItemService,
    @Autowired private val cartItemRepository: CartItemRepository,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val memberRepository: MemberRepository,
) {
    @Test
    fun getCartItemsByMemberId() {
        val member = Member(email = "test@test.com", password = "test1234")
        val registeredMember = memberRepository.save(member)
        val savedProduct = productRepository.findAll().first()
        cartItemService.addCartItem(registeredMember.id, savedProduct.id, 1)

        val pageNumber = 0
        val pageSize = 5
        val sortBy = "name"
        val cartItems = cartItemService.getCartItemsByMemberId(registeredMember.id, pageNumber, pageSize, sortBy)
        cartItems.size
        assertThat(cartItems).hasSize(1)
    }

    @Test
    fun `getCartItemsByMemberId() - test`() {
        val member = memberRepository.findAll().first()

        val pageNumber = 0
        val pageSize = 5
        val sortBy = "name"
        val cartItems = cartItemService.getCartItemsByMemberId(member.id, pageNumber, pageSize, sortBy)
        cartItems.size
        assertThat(cartItems).hasSize(pageSize)
    }

    @Test
    fun addCartItem() {
        val member = Member(email = "test@test.com", password = "test1234")
        val registeredMember = memberRepository.save(member)
        val savedProduct = productRepository.findAll().first()
        val savedItem = cartItemService.addCartItem(registeredMember.id, savedProduct.id, 1)

        assertThat(savedItem.id).isNotNull()
        assertThat(savedItem.id).isNotZero()
    }

    @Test
    fun `addCartItem - 2`() {
        val registeredMember = memberRepository.findAll().last()
        val savedProduct = productRepository.findAll().last()
        val savedItem = cartItemService.addCartItem(registeredMember.id, savedProduct.id, 1)

        registeredMember.cart.cartItems.plus(savedItem)

        assertThat(savedItem.id).isNotNull()
        assertThat(savedItem.id).isNotZero()
    }

    @Test
    fun `addToCart() - should throw exception when productId does not exist`() {
        assertThrows<NotFoundException> { cartItemService.addCartItem(MEMBER_ID, INVALID_PRODUCT_ID, 1) }
    }

    @Test
    fun `addCartItem() - update quantity when the product already exists in the cart`() {
        val member = Member(email = "test@test.com", password = "test1234")
        val registeredMember = memberRepository.save(member)
        val savedProduct = productRepository.findAll().first()
        val savedItem = cartItemService.addCartItem(registeredMember.id, savedProduct.id, savedProduct.options.first().id, 1)
        val target = cartItemService.addCartItem(registeredMember.id, savedProduct.id, savedProduct.options.first().id)

        assertThat(savedItem.id).isEqualTo(target.id)
        assertThat(target.quantity).isEqualTo(2)
    }

    @Test
    fun updateItemQuantityInCart() {
        val member = Member(email = "test@test.com", password = "test1234")
        val registeredMember = memberRepository.save(member)
        val savedProduct = productRepository.findAll().first()
        val savedItem = cartItemService.addCartItem(registeredMember.id, savedProduct.id, 1)

        val quantity = 20
        cartItemService.updateQuantity(member.id, savedItem.id, quantity)
        val cartItem = cartItemRepository.findByIdOrNull(savedItem.id)
        assertThat(cartItem).isNotNull()
        assertThat(cartItem?.quantity).isEqualTo(quantity)
    }

    @Test
    fun removeItemFromCart() {
        val member = Member(email = "test@test.com", password = "test1234")
        val registeredMember = memberRepository.save(member)
        val savedProduct = productRepository.findAll().first()
        val savedItem = cartItemService.addCartItem(registeredMember.id, savedProduct.id, 1)

        cartItemService.removeCartItem(member.id, savedItem.id)
        val cartItem = cartItemRepository.findByIdOrNull(savedItem.id)
        assertThat(cartItem).isNull()
    }

    companion object {
        const val MEMBER_ID = 1L
        const val INVALID_PRODUCT_ID = 100L
    }
}
