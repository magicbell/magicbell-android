package com.magicbell.sdk.common.error

class MappingException(className: String) : MagicBellError("There was an error while mapping $className")