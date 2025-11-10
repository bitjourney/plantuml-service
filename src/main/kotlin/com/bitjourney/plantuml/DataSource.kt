package com.bitjourney.plantuml

import net.sourceforge.plantuml.FileFormat

data class DataSource(val source: String, val fileFormat: FileFormat) {

    // returns a rough count of the memory sizes
    fun weight(): Int {
        return source.length
    }
}
