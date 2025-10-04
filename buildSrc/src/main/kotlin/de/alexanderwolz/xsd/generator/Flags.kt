package de.alexanderwolz.xsd.generator

enum class Flags(val value: String) {

    EXTENSION("-extension"),
    MARK_GENERATED("-mark-generated"),
    AUTO_NAME_RESOLUTION("-XautoNameResolution"),
    GENERATE_EQUALS("-Xequals"),
    GENERATE_HASH_CODE("-XhashCode"),
    GENERATE_TO_STRING("-XtoString");

    companion object {
        val DEFAULTS = listOf(
            EXTENSION, AUTO_NAME_RESOLUTION
        )
    }
}
