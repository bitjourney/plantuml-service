package com.bitjourney.plantuml

import net.sourceforge.plantuml.Option

data class DataSource(val source: String, val option: Option) {

    // returns a rough count of the memory sizes
    fun weight(): Int {
        return source.length
    }
}
