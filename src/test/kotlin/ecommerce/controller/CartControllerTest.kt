package ecommerce.controller

import ecommerce.controller.api.CartController
import ecommerce.dto.AuthResponse
import ecommerce.dto.CartAddItemForm
import ecommerce.dto.CartUpdateQuantityForm
import ecommerce.dto.LoginForm
import ecommerce.exception.NotFoundException
import ecommerce.model.Member
import ecommerce.repository.MemberRepository
import ecommerce.repository.ProductRepository
import ecommerce.service.CartItemService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class CartControllerTest(
    @Autowired private val controller: CartController,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val memberRepository: MemberRepository,
    @Autowired private val cartItemService: CartItemService,
) {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
    }

    @Test
    fun addToCart() {
        val savedProduct = productRepository.findAll().last()
        val savedMember = memberRepository.findAll().last()
        val form =
            CartAddItemForm(
                productId = savedProduct.id,
                optionId = savedProduct.options.first().id,
                quantity = 1,
            )
        val expected = CartController.MESSAGE_ADD_SUCCESS
        val response = controller.addToCart(form, savedMember)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(expected)
    }

    @Test
    fun `addToCart() - return 200 OK when credential is valid`() {
        val product = productRepository.findAll().first()
        val email = "min@htc.com"
        val password = "min1234"
        val quantity = 1
        val expected = CartController.MESSAGE_ADD_SUCCESS

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $accessToken")
            .body(
                CartAddItemForm(
                    productId = product.id,
                    optionId = product.options.first().id,
                    quantity = quantity,
                ),
            )
            .contentType(ContentType.JSON)
            .`when`().post("/api/cart")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.OK.value())
            .body(equalTo(expected))
    }

    @Test
    fun `addToCart() - return 401 Unauthorized when credential is invalid`() {
        val product = productRepository.findAll().first()
        val quantity = 1

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(LOGIN_EMAIL, LOGIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        val contaminatedToken = accessToken + 123
        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $contaminatedToken")
            .body(
                CartAddItemForm(
                    productId = product.id,
                    optionId = product.options.first().id,
                    quantity = quantity,
                ),
            )
            .contentType(ContentType.JSON)
            .`when`().post("/api/cart")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.UNAUTHORIZED.value())
    }

    @Test
    fun `Form validation failure when 'productId' is blank`() {
        val productId = 0L
        val quantity = 1
        val expected = "Product ID is missing"
        RestAssured
            .given().log().all()
            .body(CartAddItemForm(productId, 1L, quantity))
            .contentType(ContentType.JSON)
            .`when`().post("/api/cart")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors.productId", equalTo(expected))
    }

    @Test
    fun `Form validation failure when 'quantity' is less than 1`() {
        val product = productRepository.findAll().first()
        val quantity = 0
        val expected = "Product quantity is too small"
        RestAssured
            .given().log().all()
            .body(
                CartAddItemForm(
                    productId = product.id,
                    optionId = product.options.first().id,
                    quantity = quantity,
                ),
            )
            .contentType(ContentType.JSON)
            .`when`().post("/api/cart")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors.quantity", equalTo(expected))
    }

    @Test
    fun `viewCart() - empty cart`() {
        val member = Member(email = "testview@test.com", password = "test1234")
        val savedMember = memberRepository.save(member)
        val expected = 0
        val pageNumber = 0
        val pageSize = 5
        val sortBy = "price"
        val response = controller.viewCart(savedMember, pageNumber, pageSize, sortBy)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.content?.size).isEqualTo(expected)
    }

    @Test
    fun `viewCart() - 1 item in cart`() {
        val savedProduct = productRepository.findAll().first()
        val member = Member(email = "testview2@test.com", password = "test1234")
        val savedMember = memberRepository.save(member)
        val form = CartAddItemForm(savedProduct.id, savedProduct.options.first().id, 1)
        controller.addToCart(form, savedMember)
        val expected = 1
        val pageNumber = 0
        val pageSize = 10
        val sortBy = "price"
        val response = controller.viewCart(savedMember, pageNumber, pageSize, sortBy)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.content?.size).isEqualTo(expected)
    }

    @Test
    fun `viewCart() - view all cart items in a cart`() {
        val member = memberRepository.findAll().first()

        val expected = 5
        val pageNumber = 0
        val pageSize = 5
        val sortBy = "price"
        val response = controller.viewCart(member, pageNumber, pageSize, sortBy)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.size).isEqualTo(expected)
    }

    @Test
    fun `updateQuantity() - return 200 OK when update success`() {
        val savedProduct = productRepository.findAll().first()
        val member = Member(email = "test@test.com", password = "test1234")
        val savedMember = memberRepository.save(member)
        cartItemService.addCartItem(
            savedMember.id,
            savedProduct.id,
            savedProduct.options.first().id,
        )

        val quantity = 10
        val form = CartUpdateQuantityForm(quantity)
        val expected = CartItemService.MESSAGE_UPDATE_SUCCESS
        val response = controller.updateQuantity(savedProduct.id, form, savedMember)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(expected)
    }

    @Test
    fun `updateQuantity() - throws exception when nothing to update`() {
        val productId = PRODUCT_ID
        val quantity = 10
        val form = CartUpdateQuantityForm(quantity)
        assertThrows<NotFoundException> { controller.updateQuantity(productId, form, LOGIN_MEMBER) }
    }

    @Test
    fun `removeFromCart() - return 200 OK when remove success`() {
        val savedProduct = productRepository.findAll().first()
        val member = Member(email = "test@test.com", password = "test1234")
        val savedMember = memberRepository.save(member)
        cartItemService.addCartItem(
            savedMember.id,
            savedProduct.id,
            savedProduct.options.first().id,
        )

        val expected = CartItemService.MESSAGE_REMOVE_SUCCESS
        val response = controller.removeFromCart(savedProduct.id, savedMember)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(expected)
    }

    @Test
    fun `removeFromCart() - throws exception when nothing to remove`() {
        val productId = PRODUCT_ID
        assertThrows<NotFoundException> { controller.removeFromCart(productId, LOGIN_MEMBER) }
    }

    @Test
    fun `Interceptor - allow admin user to access admin endpoint`() {
        val savedMember = memberRepository.findByEmail(LOGIN_EMAIL) ?: throw NotFoundException("Something went wrong")

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(savedMember.email, savedMember.password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $accessToken")
            .contentType(ContentType.JSON)
            .`when`().get("/api/admin/cart-stats/top5-products")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
    }

    @Test
    fun `Interceptor - block non-admin user to access admin endpoint`() {
        val savedMember = memberRepository.findByEmail(NON_ADMIN_EMAIL) ?: throw NotFoundException("Not found")

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(savedMember.email, savedMember.password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $accessToken")
            .contentType(ContentType.JSON)
            .`when`().get("/api/admin/cart-stats/top5-products")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.FORBIDDEN.value())
    }

    companion object {
        private const val PRODUCT_ID = 1L
        private const val LOGIN_EMAIL = "san@htc.com"
        private const val LOGIN_PASSWORD = "san1234"
        private const val NON_ADMIN_EMAIL = "min@htc.com"
        private val LOGIN_MEMBER = Member(email = LOGIN_EMAIL, password = LOGIN_PASSWORD)
    }
}
