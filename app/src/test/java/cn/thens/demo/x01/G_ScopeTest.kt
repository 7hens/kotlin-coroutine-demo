package cn.thens.demo.x01

import cn.thens.demo.ITest
import kotlinx.coroutines.*
import org.junit.Test

/**
 * @author 7hens
 */
class G_ScopeTest : ITest {
    @Test
    fun newGlobalScope() = runBlocking {
        log(1)
        GlobalScope.launch {
            repeat(10) {
                log("2.$it")
                delay(100L)
            }
        }
        log(3)
        delay(220L)
    }

    @Test
    fun newCoroutineScope() = runBlocking {
        log(1)
        coroutineScope {
            log(2)
            launch {
                repeat(10) {
                    log("3.$it")
                    delay(100L)
                }
            }
            log(4)
        }
        log(5)
        delay(220L)
    }

    @Test
    fun newSupervisorScope() = runBlocking {
        log(1)
        supervisorScope {
            log(2)
            launch {
                repeat(10) {
                    log("3.$it")
                    delay(100L)
                }
            }
            log(4)
        }
        log(5)
        delay(220L)
    }

    @Test
    fun throwInGlobalScope() = runBlocking {
        log(1)
        GlobalScope.launch {
            log(2)
            delay(100)
            throw Exception("3.e")
        }
        delay(200)
        log(4)
    }

    @Test
    fun throwInCoroutineScope() = runBlocking {
        log(1)
        log(this::class.java)
        coroutineScope {
            log(2)
            log(this::class.java)
            launch {
                log(3)
                log(this::class.java)
                delay(100)
                throw Exception("4.e")
            }
            delay(200)
            log(5)
        }
        log(6)
    }

    @Test
    fun throwInSupervisorScope() = runBlocking {
        log(1)
        log(this::class.java)
        supervisorScope {
            log(2)
            log(this::class.java)
            launch {
                log(3)
                log(this::class.java)
                delay(100)
                throw Exception("4.e")
            }
            delay(200)
            log(5)
        }
        log(6)
    }
}