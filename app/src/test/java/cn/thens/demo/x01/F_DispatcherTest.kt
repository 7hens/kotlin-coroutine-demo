package cn.thens.demo.x01

import cn.thens.demo.ITest
import kotlinx.coroutines.*
import org.junit.Test
import java.util.concurrent.Executors

/**
 * @author 7hens
 */
class F_DispatcherTest : ITest {

    @Test
    fun io() = runBlocking(Dispatchers.Default) {
        log(1)
        withContext(Dispatchers.IO) {
            log(2)
            delay(100L)
            log(3)
        }
        log(4)
    }

    @Test
    fun unconfined() = runBlocking {
        log(1)
        withContext(Dispatchers.Unconfined) {
            log(2)
            delay(100L)
            log(3)
        }
        launch(Dispatchers.Default) {
            log(4)
            withContext(Dispatchers.Unconfined) {
                log(5)
            }
        }
        log(6)
    }

    @Test
    fun custom() = runBlocking {
        log(1)
        Executors.newFixedThreadPool(10).asCoroutineDispatcher().use { dispatcher ->
            (0..100).forEach { i ->
                launch(dispatcher) {
                    log("2.$i")
                }.join()
            }
        }
        log(3)
    }

    @Test
    fun scope() = runBlocking {
        log(1)
        GlobalScope.launch {
            log(2)
        }
        GlobalScope.launch(coroutineContext) {
            log(3)
        }
        launch {
            log(4)
        }
        log(5)
    }
}