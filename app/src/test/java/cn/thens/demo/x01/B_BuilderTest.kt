package cn.thens.demo.x01

import cn.thens.demo.ITest
import kotlinx.coroutines.*
import org.junit.Test

/**
 * @author 7hens
 */
class B_BuilderTest : ITest {

    @Test
    fun launch() = runBlocking {
        log(1)
        launch {
            log(2)
        }
        log(3)
    }

    @Test
    fun async() = runBlocking {
        log(1)
        val job = async {
            log(2)
            "hello"
        }
        log(job.await())
        log(3)
    }

    @Test
    fun runBlocking2() = runBlocking {
        log(1)
        val result = runBlocking {
            log(2)
            "hello"
        }
        log(result)
        log(3)
    }

    @Test
    fun withContext2() = runBlocking {
        log(1)
        val result = withContext(Dispatchers.Default) {
            log(2)
            "hello"
        }
        log(result)
        log(3)
    }
}