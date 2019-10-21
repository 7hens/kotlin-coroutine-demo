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
        log(async {
            log(2)
            3
        }.await())
        log(4)
    }

    @Test
    fun runBlocking2() = runBlocking {
        log(1)
        log(runBlocking(Dispatchers.IO) {
            log(2)
            3
        })
        log(4)
    }

    @Test
    fun withContext2() = runBlocking {
        log(1)
        log(withContext(Dispatchers.Default) {
            log(2)
            3
        })
        log(4)
    }
}