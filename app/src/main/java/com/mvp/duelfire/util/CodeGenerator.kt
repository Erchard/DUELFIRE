package com.mvp.duelfire.util

import kotlin.random.Random

object CodeGenerator {
    fun fourDigitCode(): String = Random.nextInt(1000, 10000).toString()
}
