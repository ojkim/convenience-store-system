package store

import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.system.exitProcess

// 전역 변수로 데이터 저장
private val products = mutableListOf<Product>()
private val todaySales = mutableMapOf<String, Int>()
// config를 nullable로 변경하고, 기본 설정을 가질 수 있도록 초기화
private var config: SystemConfig = SystemConfig(0.3, 3, mapOf(3 to 0.0, 2 to 0.3, 1 to 0.5, 0 to 0.7))

fun main() {
    println("=== 24시간 학교 편의점 스마트 재고 관리 시스템 ===")
    println("기본 설정으로 시작합니다. (재고 기준 30%, 유통기한 3일)")

    while (true) {
        printMainMenu()
        when (readInt("> 원하는 작업의 번호를 입력하세요: ")) {
            1 -> addOrUpdateProducts()
            2 -> recordSales()
            3 -> showInventoryStatus()
            4 -> showCurrentSales()
            5 -> {
                if (products.isEmpty()) {
                    println("오류: 먼저 상품을 하나 이상 등록해야 보고서를 생성할 수 있습니다.\n")
                    continue
                }
                println("\n시스템 분석을 시작합니다...")
                // copy()를 사용하여 현재 상태의 데이터를 전달
                val inventoryManager = InventoryManager(products.map { it.copy() }, config)
                inventoryManager.generateAndPrintReport(todaySales)
            }
            6 -> changeSystemConfig() // 시스템 설정 변경 기능 호출
            7 -> {
                println("시스템을 종료합니다. 이용해주셔서 감사합니다.")
                exitProcess(0)
            }
            else -> println("오류: 1~7 사이의 번호를 입력해주세요.\n")
        }
    }
}

/**
 * 메인 메뉴를 출력하는 함수
 */
private fun printMainMenu() {
    println("\n--- 메인 메뉴 ---")
    println("1. 상품 정보 관리 (추가/수정)")
    println("2. 판매 기록 입력")
    println("3. 현재 재고 확인")
    println("4. 현재 매출 현황")
    println("5. 일일 최종 보고서 생성")
    println("6. 시스템 설정 변경")
    println("7. 시스템 종료")
}

/**
 * 시스템 설정을 변경하는 함수
 */
private fun changeSystemConfig() {
    println("\n--- 시스템 설정 변경 ---")
    val stockThreshold = readDouble("새로운 재고 부족 알림 기준(%)을 입력하세요 (현재: ${(config.stockThreshold * 100).toInt()}%): ", (config.stockThreshold * 100)) / 100.0
    val expiryWarningDays = readInt("새로운 유통기한 임박 알림 기준(일)을 입력하세요 (현재: ${config.expiryWarningDays}일): ", config.expiryWarningDays)
    val discountPolicy = mapOf(3 to 0.0, 2 to 0.3, 1 to 0.5, 0 to 0.7)

    config = SystemConfig(stockThreshold, expiryWarningDays, discountPolicy)
    println("설정이 변경되었습니다: 재고 기준 ${ (config.stockThreshold * 100).toInt() }%, 유통기한 기준 ${config.expiryWarningDays}일")
}

// --- 이하 함수들은 이전과 거의 동일합니다 (초기 설정 함수만 이름 변경) ---

private fun showInventoryStatus() {
    println("\n--- 현재 재고 현황 ---")
    if (products.isEmpty()) {
        println("등록된 상품이 없습니다.")
        return
    }
    println("%-20s | %-8s | %-12s".format("상품명", "현재 재고", "유통기한"))
    println("-".repeat(45))
    products.forEach {
        val expiry = it.expiryDate?.toString() ?: "N/A"
        println("%-20s | %-8s | %-12s".format(it.name, "${it.currentStock}개", expiry))
    }
}

private fun showCurrentSales() {
    println("\n--- 현재 매출 현황 ---")
    if (todaySales.isEmpty()) {
        println("판매 기록이 없습니다.")
        return
    }

    var totalRevenue = 0
    println("%-20s | %-8s | %-8s | %-10s".format("상품명", "판매가", "판매 수량", "매출액"))
    println("-".repeat(55))

    todaySales.forEach { (name, quantity) ->
        val product = products.find { it.name == name }
        if (product != null) {
            val revenue = product.price * quantity
            totalRevenue += revenue
            println("%-20s | ₩%-7d | %-8s | ₩%-9d".format(name, product.price, "${quantity}개", revenue))
        }
    }
    println("-".repeat(55))
    println("현재까지의 총 매출: ₩$totalRevenue")
}

private fun addOrUpdateProducts() {
    println("\n--- 상품 정보 관리 ---")
    println("상품 정보를 입력하세요. 입력을 마치려면 상품명에 '완료'를 입력하세요.")

    while (true) {
        val name = readString("상품명 ('완료' 입력 시 종료): ")
        if (name.equals("완료", ignoreCase = true) || name.isBlank()) break

        val price = readInt(" > ${name}의 가격: ")
        val category = readCategory(" > ${name}의 카테고리 (1:과자, 2:음료, 3:식품, 4:생활용품): ")
        val optimalStock = readInt(" > ${name}의 적정 재고: ")
        val currentStock = readInt(" > ${name}의 현재 재고: ")
        val expiryDate = readDate(" > ${name}의 유통기한 (YYYY-MM-DD, 없으면 Enter): ")

        val existingProduct = products.find { it.name == name }
        if (existingProduct != null) {
            products.remove(existingProduct)
            println(" >> '${name}' 상품 정보가 업데이트되었습니다.\n")
        } else {
            println(" >> '${name}' 상품이 새로 추가되었습니다.\n")
        }
        products.add(Product(name, price, category, optimalStock, currentStock, expiryDate))
    }
}

private fun recordSales() {
    if (products.isEmpty()) {
        println("오류: 먼저 상품을 등록해야 판매 기록을 입력할 수 있습니다.\n")
        return
    }
    println("\n--- 판매 기록 입력 ---")
    println("판매된 상품명과 수량을 입력하세요. 입력을 마치려면 상품명에 '완료'를 입력하세요.")
    val productNames = products.map { it.name }.toSet()

    while (true) {
        val name = readString("판매된 상품명 ('완료' 입력 시 종료): ")
        if (name.equals("완료", ignoreCase = true) || name.isBlank()) break

        if (name !in productNames) {
            println("오류: 등록되지 않은 상품입니다. 다시 입력해주세요.")
            continue
        }

        val quantity = readInt(" > '${name}'의 판매 수량: ")
        todaySales[name] = (todaySales[name] ?: 0) + quantity
        println(" >> '${name}' ${quantity}개 판매 기록이 추가되었습니다.\n")
    }
}

//--- 사용자 입력을 위한 Helper 함수들 (이전과 동일) ---
private fun readString(prompt: String): String {
    print(prompt)
    return readlnOrNull() ?: ""
}

private fun readInt(prompt: String, defaultValue: Int? = null): Int {
    while (true) {
        print(prompt)
        val input = readlnOrNull()
        if (defaultValue != null && input.isNullOrBlank()) return defaultValue
        try {
            return input!!.toInt()
        } catch (e: NumberFormatException) {
            println("오류: 유효한 숫자를 입력해야 합니다.")
        }
    }
}

private fun readDouble(prompt: String, defaultValue: Double? = null): Double {
    while (true) {
        print(prompt)
        val input = readlnOrNull()
        if (defaultValue != null && input.isNullOrBlank()) return defaultValue
        try {
            return input!!.toDouble()
        } catch (e: NumberFormatException) {
            println("오류: 유효한 숫자를 입력해야 합니다.")
        }
    }
}

private fun readCategory(prompt: String): ProductCategory {
    while (true) {
        when (val choice = readInt(prompt)) {
            1 -> return ProductCategory.SNACK
            2 -> return ProductCategory.BEVERAGE
            3 -> return ProductCategory.FOOD
            4 -> return ProductCategory.HOUSEHOLD
            else -> println("오류: 1~4 사이의 숫자를 선택해주세요.")
        }
    }
}

private fun readDate(prompt: String): LocalDate? {
    while (true) {
        print(prompt)
        val input = readlnOrNull()
        if (input.isNullOrBlank()) return null
        try {
            return LocalDate.parse(input)
        } catch (e: DateTimeParseException) {
            println("오류: 날짜 형식이 올바르지 않습니다 (예: 2025-10-15).")
        }
    }
}