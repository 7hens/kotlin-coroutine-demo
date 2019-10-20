package cn.thens.demo.x01

import cn.thens.demo.ITest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * @author 7hens
 */
class E_InterceptorTest : ITest {

    @Test
    fun interceptor() = runBlocking {
        log(1)
        launch(createInterceptor("a") + createInterceptor("b")) {
            log(2)
            delay(100L)
            log(3)
            log(withContext(createInterceptor("c")) {
                log(4)
                "hello"
            })
            log(5)
        }
        log(6)
        delay(1000L)
    }

    private fun createInterceptor(name: String): ContinuationInterceptor {
        return object : ContinuationInterceptor {
            override val key: CoroutineContext.Key<*> = ContinuationInterceptor

            override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
                log("$name: intercept")
                return object : Continuation<T> {
                    override val context: CoroutineContext = continuation.context

                    override fun resumeWith(result: Result<T>) {
                        log("$name: intercept.result: $result")
                        continuation.resumeWith(result)
                    }
                }
            }

            override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
                log("$name: release")
                super.releaseInterceptedContinuation(continuation)
            }
        }
    }

}