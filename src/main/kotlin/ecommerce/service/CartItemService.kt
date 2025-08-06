package ecommerce.service

import ecommerce.exception.NotFoundException
import ecommerce.model.CartItem
import ecommerce.repository.CartItemRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class CartItemService(
    private val memberRepository: MemberRepository,
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val optionRepository: OptionRepository,
) {
    fun getCartItemsByMemberId(
        memberId: Long,
        pageNumber: Int,
        pageSize: Int,
        sortBy: String = "name",
    ): Page<CartItem> {
        val member =
            memberRepository.findByIdOrNull(memberId)
                ?: throw NotFoundException(MESSAGE_PRODUCT_NOT_FOUND)
        val pages =
            when (sortBy) {
                "name" -> cartItemRepository.findAllByMemberOrderByProductNameAsc(member, PageRequest.of(pageNumber, pageSize))
                "price" -> cartItemRepository.findAllByMemberOrderByProductPriceAsc(member, PageRequest.of(pageNumber, pageSize))
                else -> throw NotFoundException(MESSAGE_UNKNOWN_SORT_BY)
            }
        return pages
    }

    fun addCartItem(
        memberId: Long,
        productId: Long,
        optionId: Long,
        quantity: Int = 1,
    ): CartItem {
        val member =
            memberRepository.findByIdOrNull(memberId)
                ?: throw NotFoundException(MESSAGE_PRODUCT_NOT_FOUND)
        val product =
            productRepository.findByIdOrNull(productId)
                ?: throw NotFoundException(MESSAGE_PRODUCT_NOT_FOUND)
        val option =
            optionRepository.findByIdOrNull(optionId)
                ?: throw NotFoundException(MESSAGE_PRODUCT_NOT_FOUND)
        val target = cartItemRepository.findByMemberAndProductAndOption(member, product, option)
        if (target.isPresent) {
            target.get().changeQuantity(quantity)
            return target.get()
        }
        val cartItem = CartItem(member = member, product = product, option = option, quantity = quantity)
        return cartItemRepository.save(cartItem)
    }

    fun updateQuantity(
        memberId: Long,
        cartItemId: Long,
        quantity: Int,
    ): String {
        memberRepository.findByIdOrNull(memberId)
            ?: throw NotFoundException(MESSAGE_PRODUCT_NOT_FOUND)
        val cartItem = cartItemRepository.findById(cartItemId)
        when (cartItem.isPresent) {
            true -> {
                cartItem.get().quantity = quantity
                return MESSAGE_UPDATE_SUCCESS
            }
            false -> throw NotFoundException(MESSAGE_PRODUCT_NOT_FOUND_IN_CART)
        }
    }

    fun removeCartItem(
        memberId: Long,
        cartItemId: Long,
    ): String {
        memberRepository.findByIdOrNull(memberId)
            ?: throw NotFoundException(MESSAGE_PRODUCT_NOT_FOUND)
        val cartItem = cartItemRepository.findById(cartItemId)
        when (cartItem.isPresent) {
            true -> {
                cartItemRepository.delete(cartItem.get())
                return MESSAGE_REMOVE_SUCCESS
            }
            false -> throw NotFoundException(MESSAGE_PRODUCT_NOT_FOUND_IN_CART)
        }
    }

    companion object {
        const val MESSAGE_PRODUCT_NOT_FOUND = "Product not found"
        const val MESSAGE_PRODUCT_NOT_FOUND_IN_CART = "Product not found in Cart"
        const val MESSAGE_REMOVE_SUCCESS = "Item removed from cart"
        const val MESSAGE_UPDATE_SUCCESS = "Item updated in cart"
        const val MESSAGE_UNKNOWN_SORT_BY = "Unknown sort by method"
    }
}
