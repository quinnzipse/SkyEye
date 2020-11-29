package dev.quinnzipse.skyeye.network

data class StateResponse(
    val states: List<List<Any>>,
    val time: Int
)