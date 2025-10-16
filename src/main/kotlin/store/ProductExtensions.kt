package store

import java.time.LocalDate
import java.time.temporal.ChronoUnit



fun Product.getStockRatio(): Double {
    if (this.optimalStock == 0) return 0.0
    return (this.currentStock.toDouble() / this.optimalStock) * 100
}


fun Product.isStockLow(threshold: Double): Boolean {
    if (this.optimalStock == 0) return false // 적정 재고가 0이면 부족하다고 판단하지 않음
    return (this.currentStock.toDouble() / this.optimalStock) <= threshold
}


fun Product.daysUntilExpiry(): Long? {
    return this.expiryDate?.let {
        ChronoUnit.DAYS.between(LocalDate.now(), it)
    }
}


fun Product.isExpiringSoon(warningDays: Int): Boolean {
    val daysLeft = this.daysUntilExpiry()
    return daysLeft != null && daysLeft >= 0 && daysLeft <= warningDays
}


fun Product.calculateSalesEfficiency(sold: Int): Double {
    if (this.currentStock + sold == 0) return 0.0
    return sold.toDouble() / (this.currentStock + sold) * 100
}


fun Product.calculateStockTurnover(sold: Int): Double {
    val initialStock = this.currentStock + sold
    val averageStock = (initialStock + this.currentStock) / 2.0
    if (averageStock == 0.0) return if (sold > 0) Double.POSITIVE_INFINITY else 0.0
    return sold / averageStock * 100
}