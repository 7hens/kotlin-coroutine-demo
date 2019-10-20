package cn.thens.demo.x01

import cn.thens.demo.ITest
import kotlinx.coroutines.*
import org.junit.Test
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * @author 7hens
 */
class D_ContextTest: ITest {
    @Test
    fun coroutineName() = runBlocking {
        launch {
            log(1)
            log(coroutineContext[CoroutineName])
        }
        launch(CoroutineName("hello")) {
            log(2)
            log(coroutineContext[CoroutineName])
        }
        delay(100L)
    }

    @Test
    fun combinedContext() = runBlocking {
        launch {
            log(1)
        }
        launch(CoroutineName("hello") + CoroutineName("world")) {
            log(2)
            log(coroutineContext[CoroutineName])
        }
        delay(100L)
    }
}