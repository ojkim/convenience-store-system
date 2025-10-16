package store

import kotlin.math.roundToInt


data class SystemConfig(
    val stockThreshold: Double,
    val expiryWarningDays: Int,
    val discountPolicy: Map<Int, Double>
)


class InventoryManager(
    private var products: List<Product>,
    private val config: SystemConfig
) {
    // 판매 전 초기 재고 상태를 분석에 사용하기 위해 복사본을 저장합니다.
    private val initialProductsState = products.map { it.copy() }


    private fun updateStockBasedOnSales(sales: Map<String, Int>) {
        products.forEach { product ->
            sales[product.name]?.let { soldCount ->
                product.currentStock -= soldCount
            }
        }
    }


    fun generateAndPrintReport(sales: Map<String, Int>) {
        updateStockBasedOnSales(sales)

        println("\n=== 24시간 학교 편의점 스마트 재고 관리 시스템 ===")
        printEmergencyStockReport()
        printExpiryManagementReport()
        printBestsellerReport(sales)
        printSalesReport(sales)
        printManagementAnalysisReport(sales)
        printOverallStatusReport()
        println("시스템 처리 완료: 100%")
    }

    private fun printEmergencyStockReport() {
        println("\n긴급 재고 알림 (재고율 ${(config.stockThreshold * 100).toInt()}% 이하)")
        val lowStockProducts = products.filter { it.isStockLow(config.stockThreshold) }
        if (lowStockProducts.isEmpty()) {
            println("재고 부족 상품이 없습니다.")
            return
        }
        lowStockProducts.forEach {
            val needed = it.optimalStock - it.currentStock
            println(
                "- ${it.name}(${it.category.displayName}): 현재 ${it.currentStock}개, 적정재고 ${it.optimalStock}개 (${needed}개 발주 필요) [재고율: ${"%.1f".format(it.getStockRatio())}%]"
            )
        }
    }

    private fun printExpiryManagementReport() {
        println("\n▲ 유통기한 관리 (${config.expiryWarningDays}일 이내 임박 상품)")
        val expiringProducts = products.filter { it.isExpiringSoon(config.expiryWarningDays) }
        if (expiringProducts.isEmpty()) {
            println("유통기한 임박 상품이 없습니다.")
            return
        }
        expiringProducts.sortedBy { it.daysUntilExpiry() }.forEach {
            val daysLeft = it.daysUntilExpiry() ?: 0
            val discountRate = config.discountPolicy[daysLeft.toInt()] ?: 0.0
            val discountedPrice = it.price * (1 - discountRate)
            val dayString = if (daysLeft <= 0) "당일까지" else "${daysLeft}일 남음"

            println(
                "- ${it.name}: $dayString, 할인률 ${(discountRate * 100).toInt()}% 적용 (₩${it.price} -> ₩${discountedPrice.roundToInt()})"
            )
        }
    }

    private fun printBestsellerReport(sales: Map<String, Int>) {
        println("\n오늘의 베스트셀러 TOP 5")
        val bestsellers = sales.entries
            .mapNotNull { (name, count) ->
                initialProductsState.find { it.name == name }?.let { product ->
                    Triple(product, count, product.price * count)
                }
            }
            .sortedByDescending { it.third }
            .take(5)

        if (bestsellers.isEmpty()) {
            println("판매 기록이 없습니다.")
            return
        }
        bestsellers.forEachIndexed { index, (product, count, revenue) ->
            println("${index + 1}위: ${product.name} (${count}개 판매, 매출 ₩${revenue})")
        }
    }

    private fun printSalesReport(sales: Map<String, Int>) {
        println("\n매출 현황")
        var totalRevenue = 0
        var totalSoldCount = 0
        sales.forEach { (name, count) ->
            initialProductsState.find { it.name == name }?.let { product ->
                val revenue = product.price * count
                totalRevenue += revenue
                totalSoldCount += count
                println("* ${product.name}: ₩$revenue (${count}개 × ₩${product.price})")
            }
        }
        println("오늘 총 매출: ₩$totalRevenue (${totalSoldCount}개 판매)")
    }

    private fun printManagementAnalysisReport(sales: Map<String, Int>) {
        println("\n경영 분석 리포트 (입력 데이터 기반 분석)")
        val soldProducts = initialProductsState.mapNotNull { p -> sales[p.name]?.let { s -> p to s } }

        val turnoverReport = soldProducts.map { (p, s) -> p to p.calculateStockTurnover(s) }
        val efficiencyReport = soldProducts.map { (p, s) -> p to p.calculateSalesEfficiency(s) }

        val maxTurnover = turnoverReport.maxByOrNull { it.second }
        val minTurnover = initialProductsState.map { p -> p to p.calculateStockTurnover(sales[p.name] ?: 0) }
            .minByOrNull { it.second }
        val maxEfficiency = efficiencyReport.maxByOrNull { it.second }

        println("- 재고 회전율 최고: ${maxTurnover?.first?.name ?: "N/A"} (판매 ${sales[maxTurnover?.first?.name] ?: 0}개, ${"%.0f".format(maxTurnover?.second ?: 0.0)}% 회전)")
        println("- 재고 회전율 최저: ${minTurnover?.first?.name ?: "N/A"} (판매 ${sales[minTurnover?.first?.name] ?: 0}개, ${"%.0f".format(minTurnover?.second ?: 0.0)}% 회전)")
        println("- 판매 효율 1위: ${maxEfficiency?.first?.name ?: "N/A"} (판매 ${sales[maxEfficiency?.first?.name] ?: 0}개, ${"%.0f".format(maxEfficiency?.second ?: 0.0)}% 효율)")

        val excessiveStock = products.filter { it.currentStock > it.optimalStock * 1.5 }.sortedByDescending { it.getStockRatio() }
        println("- 재고 과다 품목: ${if (excessiveStock.isEmpty()) "없음" else excessiveStock.joinToString(", ") { "${it.name} (${it.currentStock}개)" }}")

        val recommendedOrders = products.filter { it.isStockLow(config.stockThreshold) }
        println("- 발주 권장: 총 ${recommendedOrders.size}개 품목, ${recommendedOrders.sumOf { it.optimalStock - it.currentStock }}개 수량")
    }

    private fun printOverallStatusReport() {
        println("\n■ 종합 운영 현황 (시스템 처리 결과)")
        println("- 전체 등록 상품: ${products.size}종")
        println("- 현재 총 재고: ${products.sumOf { it.currentStock }}개")
        println("- 현재 재고가치: ₩${products.sumOf { it.price * it.currentStock }}")
        println("- 재고 부족 상품: ${products.count { it.isStockLow(config.stockThreshold) }}종 (${(config.stockThreshold * 100).toInt()}% 이하)")
        println("- 유통기한 임박: ${products.count { it.isExpiringSoon(config.expiryWarningDays) }}종 (${config.expiryWarningDays}일 이내)")
    }
}