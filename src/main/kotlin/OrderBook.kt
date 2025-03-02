import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap

enum class OrderType { BUY, SELL }

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val type: OrderType,
    val price: BigDecimal,
    var quantity: Int
)

class OrderBook {
    private val buyOrders = ConcurrentHashMap<BigDecimal, MutableList<Order>>()
    private val sellOrders = ConcurrentHashMap<BigDecimal, MutableList<Order>>()

    @Synchronized
    fun addOrder(order: Order) {
        val orders = if (order.type == OrderType.BUY) buyOrders else sellOrders
        orders.getOrPut(order.price) { Collections.synchronizedList(mutableListOf()) }.add(order)
    }

    @Synchronized
    fun removeOrder(order: Order) {
        val orders = if (order.type == OrderType.BUY) buyOrders else sellOrders
        orders[order.price]?.remove(order)
        if (orders[order.price]?.isEmpty() == true) {
            orders.remove(order.price)
        }
    }

    @Synchronized
    fun matchOrders() {
        val buyPrices = buyOrders.keys.toList().sortedByDescending { it }
        val sellPrices = sellOrders.keys.toList().sortedBy { it }

        for (buyPrice in buyPrices) {
            for (sellPrice in sellPrices) {
                if (buyPrice < sellPrice) break

                val buyOrdersList = buyOrders[buyPrice] ?: continue
                val sellOrdersList = sellOrders[sellPrice] ?: continue

                matchOrdersAtPrice(buyOrdersList, sellOrdersList)
            }
        }
    }

    private fun matchOrdersAtPrice(buyOrdersList: MutableList<Order>, sellOrdersList: MutableList<Order>) {
        val buyIterator = buyOrdersList.iterator()
        while (buyIterator.hasNext()) {
            val buyOrder = buyIterator.next()
            val sellIterator = sellOrdersList.iterator()
            while(sellIterator.hasNext()){
                val sellOrder = sellIterator.next()

                val matchedQuantity = minOf(buyOrder.quantity, sellOrder.quantity)
                println("Matched: Buy ${buyOrder.id} with Sell ${sellOrder.id} for $matchedQuantity at ${sellOrder.price}")

                buyOrder.quantity -= matchedQuantity
                sellOrder.quantity -= matchedQuantity

                if (buyOrder.quantity == 0){
                    buyIterator.remove()
                }

                if (sellOrder.quantity == 0){
                    sellIterator.remove()
                }

            }
        }
    }

    fun displayOrderBook() {
        println("Buy Orders:")
        buyOrders.forEach { (price, orders) ->
            println("$price: ${orders.sumOf { it.quantity }}")
        }
        println("Sell Orders:")
        sellOrders.forEach { (price, orders) ->
            println("$price: ${orders.sumOf { it.quantity }}")
        }
    }
}