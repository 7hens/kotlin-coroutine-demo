package cn.thens.demo.x01

import cn.thens.demo.ITest
import kotlinx.coroutines.*
import org.junit.Test
import java.lang.IllegalStateException
import java.lang.RuntimeException

/**
 * @author 7hens
 */
class G_ScopeTest: ITest {
    @Test
    fun exceptionHandler() = runBlocking {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            log("exception: " + throwable.message)
        }

        log(1)
        GlobalScope.launch(exceptionHandler) {
            throw IllegalStateException("bomb")
        }.join()
        log(2)
    }

    @Test
    fun globalScope() = runBlocking {
        log(1)
        GlobalScope.launch {
            log(2)
            throw RuntimeException("bomb")
        }
        log(4)
    }

    @Test
    fun coroutineScope() = runBlocking {
        log(1)
        coroutineScope {
            launch {
                log(2)
                throw RuntimeException("bomb")
            }
        }
        log(4)
    }

    @Test
    fun supervisorScope() = runBlocking {
        log(1)
        supervisorScope {
            launch {
                log(2)
                throw RuntimeException("bomb")
            }
        }
        log(4)
    }
}