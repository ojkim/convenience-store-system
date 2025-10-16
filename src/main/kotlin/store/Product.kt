package store


import java.time.LocalDate


enum class ProductCategory(val displayName: String) {
    SNACK("과자류"),
    BEVERAGE("음료류"),
    FOOD("식품류"),
    HOUSEHOLD("생활용품")
}

/**

 * @property name 상품명
 * @property price 정가
 * @property category 상품 카테고리
 * @property optimalStock 시스템이 권장하는 적정 재고량
 * @property currentStock 현재 재고량
 * @property expiryDate 유통기한 (nullable, 없는 상품도 있음)
 */
data class Product(
    val name: String,
    val price: Int,
    val category: ProductCategory,
    val optimalStock: Int,
    var currentStock: Int, // 판매 후 수량이 변경되어야 하므로 var로 선언
    val expiryDate: LocalDate?
)