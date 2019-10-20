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
        }
        log(3)
    }

    @Test
    fun unconfined() = runBlocking {
        log(1)
        withContext(Dispatchers.Unconfined) {
            log(2)
        }
        launch(Dispatchers.Default) {
            log(3)
            withContext(Dispatchers.Unconfined) {
                log(4)
            }
        }
        log(5)
    }

    @Test
    fun custom() = runBlocking {
        log(-1)
        Executors.newFixedThreadPool(10).asCoroutineDispatcher().use { dispatcher ->
            (0..100).forEach { i ->
                launch(dispatcher) {
                    log(i)
                }.join()
            }
        }
        log(-2)
    }
}