package de.alexanderwolz.xsd.generator

internal class Arguments() {

    private val argsList = ArrayList<String>()

    fun add(value: String) {
        argsList.add(value)
    }

    fun add(key: String, value: String?) {
        if (!key.startsWith("-")) throw IllegalArgumentException("Key must start with -")
        argsList.add(key)
        value?.let { argsList.add(value) }
    }

    fun getArgs(): Array<String> {
        return argsList.toTypedArray()
    }

}