# spring-ecommerce-order

## Features List

### Step 2

1. Configure the application to get and read API key from Stripe
    - [x] store the API key in `.env` file, and also let the file be git ignored
    - [x] set custom configuration for test run with gradle by adding API key as a environment variable to run **test**
        - _keep this step to share with related entities to let them run and test_
            - Configure **IntelliJ Run Configuration** to load variables from `.env` or manually set them.
    - [x] add `StripeProperties` with `@ConfigurationProperties` for stripe to read API key from application properties
    - [x] apply `@EnableConfigurationProperties` with `StripeProperties` on `Application`
2. Implement a client `StripeClient` to enable applications to communicate with external API, **Stripe Payment Create API**
    - [x] has `@Component` annotation to be managed by framework
    - [x] takes an input property `StripeProperties` to read API key
    - [x] has a property `RestClient` to perform HTTP request to communicate with the Stripe API
    - [x] has a method `createCheckoutSession()` to create a `PaymentIntent`
        - which is an object that tracks the full lifecycle of a payment in Stripe
        - [x] the method takes `PaymentRequest` as input
        - [x] return a `PaymentResponse` to check `status` of the `PaymentIntent`
3. Refactor `CartItem` to have an **`option`** to place an order
    - [x] has a property `option` which is from the selected `product`
    - [x] has a property `quantity` for ordering quantity 
        - to extract from `option` quantity, if order process with success - `@Transactional`
4. Implement business logic for payment processing - `@Transactional`
    - [ ] extract order quantity from `option` quantity, when the order processed with success
    - [ ] if the ordered product exists as a `CartItem` it should be removed from table and transferred to `Order`
    - [ ] MUST handle the error safely, when the payment approval API call fails
        - [ ] inform to the user cleary for the reason of failure of payment
        - [ ] display a proper error message why the payment failed
            - when the order failure due to payment rejection 
            - e.g., expired session, invalid payment method, insufficient balance, etc.

### Step 1

1. Refactor model into entity
    - [x] `Product` into entity
    - [x] `Member` into entity
    - [x] `CartItem` into entity
    - [x] introduce new entity `Cart`
    - [x] find way to deal with cascade
    - [x] find way to deal with one-to-one &one-to-many & many-to-one & many-to-many

2. Refactor Repository
    - [x] `CartItemRepository` using `JpaRepository`
    - [x] `ProductRepository` using `JpaRepository`
    - [x] `MemberRepository` using `JpaRepository`
    - [x] `CartRepository` using `JpaRepository`
    - [x] refactor tests, using `@DataJpaTest`
3. Refactor Service
    - ex. validation wit `require()`

4. Add feature **pagination** for `Product` and `CartItem`
    - [x] Apply interface `PagingAndSortingRepository` to product repository and cart item repository
    - [x] Apply `Pagable` to cast output into `Page` type
    - [x] Refactor `ProductController` to return with `Page` response

5. Add `options` to product information.
    - Property
        - [x] `id`
        - [x] `name` - up to 50 characters
        - [x] `quantity` - 1 to 100_000_000
    - Method
        - [x] decrease the quantity of the option by a specific amount
        - [x] A `product` must always have at least one `option`
    - DTO
        - [x] implement `ProductResponse` to organize product response with pagination
        - [x] implement `OptionResponse` to organize options response for a product
