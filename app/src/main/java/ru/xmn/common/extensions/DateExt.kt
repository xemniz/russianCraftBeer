package ru.xmn.common.extensions

import khronos.Dates
import khronos.Duration
import khronos.minus
import java.util.*

fun Date.stampInSeconds(): Long = this.time / 1000

infix fun Date.olderThan(duration: Duration) =
        Dates.now - duration > this

infix fun Date.newerThan(duration: Duration) =
        this > Dates.now - duration

