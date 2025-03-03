import kotlinx.coroutines.*
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

fun main() = runBlocking {
    val orderBook = OrderBook()
    
    val matchingJob = launch {
        while (isActive) {
            orderBook.matchOrders()
            delay(100) // Match orders every 100ms
        }
    }

    val buyOrderJob = launch {
        while (isActive) {
            val price = BigDecimal(Random.nextDouble(90.0, 110.0).toString())
            val quantity = Random.nextInt(1, 10)
            orderBook.addOrder(Order(type = OrderType.BUY, price = price, quantity = quantity))
            delay(Random.nextLong(50, 150))
        }
    }

    val sellOrderJob = launch {
        while (isActive) {
            val price = BigDecimal(Random.nextDouble(90.0, 110.0).toString())
            val quantity = Random.nextInt(1, 10)
            orderBook.addOrder(Order(type = OrderType.SELL, price = price, quantity = quantity))
            delay(Random.nextLong(50, 150))
        }
    }

    // Coroutine for displaying the order book
    val displayJob = launch {
        while (isActive) {
            orderBook.displayOrderBook()
            delay(1000) // Display order book every second
        }
    }

    // Run for 10 seconds
    delay(10000)

    // Cancel all coroutines
    matchingJob.cancel()
    buyOrderJob.cancel()
    sellOrderJob.cancel()
    displayJob.cancel()
}
