package ecommerce.controller.api

import ecommerce.dto.CartAddItemForm
import ecommerce.dto.CartItemResponse
import ecommerce.dto.CartUpdateQuantityForm
import ecommerce.model.CartItem
import ecommerce.model.Member
import ecommerce.service.CartItemService
import ecommerce.ui.LoginMember
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartItemService: CartItemService,
) {
    @GetMapping
    fun viewCart(
        @LoginMember member: Member,
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "name") sortBy: String,
    ): ResponseEntity<Page<CartItemResponse>> {
        val cartItemPage =
            when (sortBy.isEmpty()) {
                true -> cartItemService.getCartItemsByMemberId(member.id, pageNumber, pageSize)
                false -> cartItemService.getCartItemsByMemberId(member.id, pageNumber, pageSize, sortBy)
            }
        val alteredPages = cartItemPage.map { CartItem.to(it) }
        return ResponseEntity.ok(alteredPages)
    }

    @PostMapping
    fun addToCart(
        @RequestBody @Valid cartForm: CartAddItemForm,
        @LoginMember member: Member,
    ): ResponseEntity<String> {
        cartItemService.addCartItem(member.id, cartForm.productId, cartForm.optionId, cartForm.quantity)
        return ResponseEntity.ok(MESSAGE_ADD_SUCCESS)
    }

    @PutMapping("/{cartItemId}")
    fun updateQuantity(
        @PathVariable cartItemId: Long,
        @RequestBody @Valid cartForm: CartUpdateQuantityForm,
        @LoginMember member: Member,
    ): ResponseEntity<String> {
        val message = cartItemService.updateQuantity(member.id, cartItemId, cartForm.quantity)
        return ResponseEntity.ok(message)
    }

    @DeleteMapping("/{cartItemId}")
    fun removeFromCart(
        @PathVariable cartItemId: Long,
        @LoginMember member: Member,
    ): ResponseEntity<String> {
        val message = cartItemService.removeCartItem(member.id, cartItemId)
        return ResponseEntity.ok(message)
    }

    companion object {
        const val MESSAGE_ADD_SUCCESS = "Item added to cart"
    }
}
