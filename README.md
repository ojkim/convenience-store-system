# 24시간 학교 편의점 스마트 재고 관리 시스템

## 📜 프로젝트 설명

이 프로젝트는 편의점 아르바이트생 '탄지로'가 겪는 문제들을 해결하기 위해 개발된 **콘솔 기반의 스마트 재고 관리 시스템**입니다. 수기로 인한 재고 관리의 부정확성, 유통기한 임박 상품의 비체계적인 관리, 매출 데이터 분석의 부재라는 세 가지 핵심 문제를 해결하는 것을 목표로 합니다.

사용자는 메뉴 기반 인터페이스를 통해 상품 정보를 등록 및 수정하고, 판매량을 기록하며, 실시간으로 재고와 매출 현황을 확인할 수 있습니다. 모든 데이터 입력이 완료되면, 시스템은 긴급 재고 알림, 유통기한 관리, 베스트셀러 분석 등이 포함된 종합적인 **일일 최종 보고서**를 생성하여 편의점 운영 효율을 극대화합니다.

-요구사항 중 중요하다 생각했던것들

[상품 타입별로 다른 특성을 어떻게 표현할까?]
-상속(Inheritance)을 사용

[공통 기능들을 어떻게 정의할까?]
-인터페이스(Interface)를 사용

[기존 상품 클래스를 수정하지 않고 새 기능을 어떻게 추가할까?]
-코틀린의 확장 함수(Extension Functions)를 사용

[많은 상품 데이터를 어떻게 효율적으로 처리할까?]
-컬렉션(Collections)과 함수형 프로그래밍 스타일을 사용

[복잡한 분석 로직을 어떻게 깔끔하게 구조화할까?]
-하나의 큰 메서드를 잘게 쪼개는 방식을 사용 `InventoryManager`의 `generateAndPrintReport()` 메서드를 보면, 이 함수가 모든 일을 직접 처리하지 않음

## 📝 사용 방법

1.  **프로그램 실행**: 아래 명령어로 프로그램을 실행합니다.
    ```shell
    java -jar build/libs/convenience-store-system-1.0-SNAPSHOT.jar
    ```
2.  **상품 등록/수정**: 메인 메뉴에서 `1번`을 선택하여 판매 중인 상품의 정보를 입력
3.  **판매량 기록**: `2번`을 선택하여 오늘 판매된 상품들의 수량을 기록
4.  **중간 현황 확인 (선택 사항)**: `3번`과 `4번` 메뉴를 통해 현재까지의 재고와 매출을 실시간으로 확인가능
5.  **최종 보고서 생성**: 모든 정보 입력이 끝나면 `5번`을 선택하여 종합 분석 보고서를 확인가능
6.  **시스템 종료**: `7번`을 선택하여 프로그램을 종료
## 🧱 클래스 구조 및 핵심 코드 설명

이 시스템은 각자의 역할이 명확하게 구분된 5개의 핵심 클래스로 구성되어 있습니다.

### **`Main.kt`**
**역할:** 프로그램의 시작점이자 사용자 인터페이스(UI)를 총괄합니다. `while` 루프와 `when` 문을 통해 사용자가 메뉴를 선택하고 원하는 기능을 반복적으로 실행할 수 있도록 전체 흐름을 제어합니다.

```kotlin
// Main.kt
fun main() {
    while (true) {
        printMainMenu()
        when (readInt("> 원하는 작업의 번호를 입력하세요: ")) {
            1 -> addOrUpdateProducts()
            2 -> recordSales()
            // ... (각 메뉴 번호에 따라 해당 함수 호출) ...
            7 -> exitProcess(0)
        }
    }
}
```
### **`Product.kt`**
**역할:** 시스템에서 사용되는 핵심 데이터 모델을 정의합니다. 상품의 모든 정보(이름, 가격, 재고 등)를 간결하게 표현하기 위해 `data class`를 사용하며, 상품 카테고리는 `enum class`로 정의하여 실수를 방지하고 코드의 가독성을 높입니다.
```kotlin
// Product.kt
enum class ProductCategory(val displayName: String) {
    SNACK("과자류"), BEVERAGE("음료류"), FOOD("식품류"), HOUSEHOLD("생활용품")
}

data class Product(
    val name: String,
    val price: Int,
    val category: ProductCategory,
    var currentStock: Int,
    // ...
)
```
### **`InventoryManager.kt`**
**역할:** 시스템의 두뇌로서 모든 비즈니스 로직을 처리합니다. Main.kt로부터 전달받은 데이터를 바탕으로 재고 부족 상품, 유통기한 임박 상품 등을 분석하고, 최종적으로 구조화된 보고서를 출력하는 핵심적인 역할을 수행합니다.
```kotlin
// InventoryManager.kt
class InventoryManager(private var products: List<Product>, private val config: SystemConfig) {
    fun generateAndPrintReport(sales: Map<String, Int>) {
        updateStockBasedOnSales(sales)

        printEmergencyStockReport()
        printExpiryManagementReport()
        printBestsellerReport(sales)
        // ... (모든 보고서 출력 함수 순차 호출)
    }
}
```
### **`ProductExtensions.kt`**
**역할:** 확장 함수를 사용하여 `Product` 원본 클래스를 수정하지 않으면서도 스마트 분석 기능을 추가합니다. 코드의 재사용성과 유지보수성을 높이며, `Product` 객체 스스로 재고율을 계산하는 등 객체 지향적인 설계를 가능하게 합니다.
```kotlin
// ProductExtensions.kt
// Product 클래스에 isStockLow() 라는 새로운 함수가 생긴 것처럼 사용 가능
fun Product.isStockLow(threshold: Double): Boolean {
    if (this.optimalStock == 0) return false
    return (this.currentStock.toDouble() / this.optimalStock) <= threshold
}
```
### **`AdvancedProduct.kt`**
**역할:** 향후 시스템 확장을 대비한 설계 기반을 제공합니다. `Product` 인터페이스를 통해 모든 상품이 가져야 할 필수 기능을 규정함으로써, 나중에 식품, 생활용품 등 각기 다른 특징을 가진 상품 유형을 추가하더라도 일관된 방식으로 처리할 수 있도록 보장합니다.
```kotlin
// AdvancedProduct.kt
// 모든 상품 타입이 반드시 구현해야 하는 기능을 정의
interface IProduct {
    val name: String
    val price: Int
    var currentStock: Int
    fun getDisplayInfo(): String
}
```


## 🚀 사용 시나리오 

1. 프로젝트 빌드 및 실행
```kotlin
# 1. 빌드
    ./gradlew build

# 2. 실행 (Windows 환경)
java -jar build/libs/convenience-store-system-1.0-SNAPSHOT.jar
```
2. 메인 메뉴 및 기능별 코드

프로그램이 시작되면 메인 메뉴가 나타납니다. 각 번호에 해당하는 기능과 핵심 코드는 다음과 같습니다.

`1. 상품 정보 관리 (추가/수정)`

사용자로부터 상품 정보를 입력받아 `products` 리스트에 `Product` 객체를 추가하거나 기존 정보를 업데이트합니다.
```kotlin
// Main.kt - addOrUpdateProducts()
while (true) {
    val name = readString("상품명 ('완료' 입력 시 종료): ")
    if (name.equals("완료", ignoreCase = true)) break
    // ... 가격, 재고 등 정보 입력 ...
    products.removeIf { it.name == name }
    products.add(Product(name, price, category, optimalStock, currentStock, expiryDate))
}
```
`2. 판매 기록 입력`

판매된 상품명과 수량을 입력받아 `todaySales` 맵에 판매 기록을 누적합니다.
```kotlin
// Main.kt - recordSales()
while (true) {
    val name = readString("판매된 상품명 ('완료' 입력 시 종료): ")
    if (name.equals("완료", ignoreCase = true)) break
    // ... 수량 입력 ...
    todaySales[name] = (todaySales[name] ?: 0) + quantity
}
```
`3. 현재 재고 확인`

현재 `products` 리스트에 저장된 모든 상품의 재고 상태를 순회하며 출력합니다.
```kotlin
// Main.kt - showInventoryStatus()
products.forEach {
    val expiry = it.expiryDate?.toString() ?: "N/A"
    println("%-20s | %-8s | %-12s".format(it.name, "${it.currentStock}개", expiry))
}
```

`4. 현재 매출 현황`

`todaySales` 맵의 기록을 바탕으로 각 상품의 매출액과 총매출을 계산하여 출력합니다.
```kotlin
// Main.kt - showCurrentSales()
var totalRevenue = 0
todaySales.forEach { (name, quantity) ->
    val product = products.find { it.name == name }
    if (product != null) {
        val revenue = product.price * quantity
        totalRevenue += revenue
        // ... 상품별 매출 출력 ...
    }
}
println("현재까지의 총 매출: ₩$totalRevenue")
```
`5. 일일 최종 보고서 생성`

`InventoryManager` 클래스의 인스턴스를 생성하고, 현재까지 축적된 상품 및 판매 데이터를 전달하여 종합 보고서 생성을 요청합니다.
```kotlin
// Main.kt - main() 내부
5 -> {
    val inventoryManager = InventoryManager(products.map { it.copy() }, config)
    inventoryManager.generateAndPrintReport(todaySales)
}
```

`6. 시스템 설정 변경`

재고 부족 기준, 유통기한 임박 기준 등 시스템의 핵심 설정을 담고 있는 `config` 객체를 사용자 입력에 따라 새로 생성하여 교체합니다.
```kotlin
// Main.kt - changeSystemConfig()
val stockThreshold = readDouble("새로운 재고 부족 알림 기준(%): ") / 100.0
val expiryWarningDays = readInt("새로운 유통기한 임박 알림 기준(일): ")
config = SystemConfig(stockThreshold, expiryWarningDays, config.discountPolicy)
```

`7. 시스템 종료`

`exitProcess(0)`를 호출하여 프로그램을 안전하게 종료합니다.
```kotlin
// Main.kt - main() 내부
7 -> {
    println("시스템을 종료합니다.")
    exitProcess(0)
}
```

## 🛠️ 적용 기술
언어: Kotlin (2.2)

JDK: JDK 24

빌드 도구: Gradle





