package cn.thens.demo.x01

import cn.thens.demo.ITest
import kotlinx.coroutines.*
import org.junit.Test

/**
 * @author 7hens
 */
class H_ExceptionTest : ITest {
    @Test
    fun exceptionHandler() = runBlocking {
        log(1)
        GlobalScope.launch {
            launch(createExceptionHandler()) {
                log(2)
//            throw CancellationException("3.e")
                throw Exception("3.e")
            }
            try {
                delay(100L)
            } catch (e: Exception) {
                log("4.$e")
            }
            try {
                delay(100L)
            } catch (e: Exception) {
                log("4.2.$e")
            }
            try {
                delay(100L)
            } catch (e: Exception) {
                log("4.3.$e")
            }
        }
        try {
            delay(200L)
        } catch (e: Exception) {
            log("5.$e")
        }
        log(6)
    }

    @Test
    fun throwInRunBlocking() = runBlocking(createExceptionHandler()) {
        log(1)
        launch {
            log(2)
            throw Exception("3.e")
        }
        try {
            delay(100L)
        } catch (e: Exception) {
            log("4.$e")
        }
    }

    @Test
    fun testJob() {
        val scope = CoroutineScope(Job())
        runBlocking {
            log(1)
            scope.launch {
                log(2)
                throw Exception("3.e")
            }
            scope.launch {
                delay(100)
                log(4)
            }
            yield()
            log(5)
        }
        Thread.sleep(1000)
    }

    private fun createExceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { coroutineContext, throwable ->
            log("ERROR: " + throwable.message)
        }
    }
}
