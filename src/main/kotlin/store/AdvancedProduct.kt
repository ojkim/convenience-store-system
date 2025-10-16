package store

import java.time.LocalDate


interface IProduct {
    val name: String
    val price: Int
    val category: ProductCategory
    var currentStock: Int

    fun isStockLow(threshold: Double): Boolean
    fun getDisplayInfo(): String
}


abstract class AbstractProduct(
    override val name: String,
    override val price: Int,
    override val category: ProductCategory,
    override var currentStock: Int,
    open val optimalStock: Int
) : IProduct {

    override fun isStockLow(threshold: Double): Boolean {
        if (optimalStock <= 0) return false
        return (currentStock.toDouble() / optimalStock) < threshold
    }

    override fun getDisplayInfo(): String {
        return "상품명: $name, 가격: ${price}원, 재고: ${currentStock}개"
    }
}


class FoodProduct(
    name: String,
    price: Int,
    currentStock: Int,
    optimalStock: Int,
    val expiryDate: LocalDate
) : AbstractProduct(name, price, ProductCategory.FOOD, currentStock, optimalStock) {

    override fun getDisplayInfo(): String {
        return super.getDisplayInfo() + ", 유통기한: $expiryDate"
    }
}