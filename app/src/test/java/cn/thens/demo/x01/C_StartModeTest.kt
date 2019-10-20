package cn.thens.demo.x01

import cn.thens.demo.ITest
import kotlinx.coroutines.*
import org.junit.Test

/**
 * @author 7hens
 */
class C_StartModeTest : ITest {
    @Test
    fun default() = runBlocking {
        log(1)
        val job = launch(start = CoroutineStart.DEFAULT) {
            log(2)
        }
        log(3)
        job.join()
        log(4)
    }

    @Test
    fun lazy() = runBlocking {
        log(1)
        val job = launch(start = CoroutineStart.LAZY) {
            log(2)
        }
        log(3)
        job.join()
        log(4)
    }

    @Test
    fun atomic() = runBlocking {
        log(1)
        val job = launch(start = CoroutineStart.ATOMIC) {
            log(2)
            delay(1000L)
            log(3)
        }
        job.cancel()
        log(4)
        job.join()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun undispatched() = runBlocking {
        log(1)
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            log(2)
            delay(1000L)
            log(3)
        }
        log(4)
        job.join()
        log(5)
    }
}