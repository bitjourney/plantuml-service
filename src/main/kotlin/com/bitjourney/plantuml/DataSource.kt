package com.bitjourney.plantuml

data class DataSource(val source: String, val configArray: List<String>) {

    // returns a rough count of the memory sizes
    fun weight(): Int {
        return source.length + configArray.sumBy { it.length }
    }
}
