package com.hcindy.myapp

class TextSearchStore {
    companion object {
        val textLines = mutableListOf<String>()

        val searchLines = mutableListOf<String>()

        fun filterAndFill(text: CharSequence?) {
            searchLines.clear()
            if (text != null && text.isNotEmpty()) {
                textLines.filter { it.contains(text) }
                    .also(searchLines::addAll)
            }
        }
    }
}