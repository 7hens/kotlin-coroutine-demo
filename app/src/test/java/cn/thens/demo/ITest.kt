package cn.thens.demo

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author 7hens
 */
interface ITest {
    fun log(msg: Any?) {
        val now = SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA).format(Date())
        val threadName = Thread.currentThread().name
        println("$now [$threadName] $msg")
    }
}