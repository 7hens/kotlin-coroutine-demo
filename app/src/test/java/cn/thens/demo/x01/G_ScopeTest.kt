package cn.thens.demo.x01

import cn.thens.demo.ITest
import kotlinx.coroutines.*
import org.junit.Test
import java.lang.IllegalStateException
import java.lang.RuntimeException

/**
 * @author 7hens
 */
class G_ScopeTest : ITest {
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
    fun coroutineScope2() = runBlocking {
        GlobalScope.launch {
            log(0)
            try {
                coroutineScope {
                    log(1)
                    try {
                        launch {
                            log(2)
                            throw RuntimeException("3.exception")
                        }
                        delay(100L)
                        log(4)
                    } catch (e: Exception) {
                        log("5.catch: " + e.message)
                    }
                }
            } catch (e: Exception) {
                log("6.catch: " + e.message)
            }

            log(10)
            try {
                coroutineScope {
                    log(11)
                    launch(Dispatchers.IO) {
                        try {
                            log(12)
                            delay(100L)
                            log(13)
                        } catch (e: Exception) {
                            log("14.catch: " + e.message)
                        }
                    }
                    delay(10L)
                    log(15)
                    throw RuntimeException("16.exception")
                }
            } catch (e: Exception) {
                log("17.catch: " + e.message)
            }
        }
        delay(1000L)
        log("end")
    }

    @Test
    fun supervisorScope2() = runBlocking {
        GlobalScope.launch {
            log(0)
            supervisorScope {
                try {
                    log(1)
                    launch {
                        log(2)
                        throw RuntimeException("3.exception")
                    }
                    delay(100L)
                    log(4)
                } catch (e: Exception) {
                    log("5.catch: " + e.message)
                }
            }

            log(10)
            supervisorScope {
                log(11)
                launch(Dispatchers.IO) {
                    try {
                        log(12)
                        delay(100L)
                        log(13)
                    } catch (e: Exception) {
                        log("16.catch: " + e.message)
                    }
                }
                delay(10L)
                log(14)
                throw RuntimeException("18.exception")
            }
        }
        delay(1000L)
        log("end")
    }

    @Test
    fun globalScope() = runBlocking {
        GlobalScope.launch {
            log(0)
            coroutineScope {
                try {
                    log(1)
                    GlobalScope.launch {
                        log(2)
                        throw RuntimeException("3.exception")
                    }
                    delay(100L)
                    log(4)
                } catch (e: Exception) {
                    log("5.catch: " + e.message)
                }
            }

            log(10)
            coroutineScope {
                log(11)
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        log(12)
                        delay(100L)
                        log(13)
                    } catch (e: Exception) {
                        log("16.catch: " + e.message)
                    }
                }
                delay(10L)
                log(14)
                throw RuntimeException("18.exception")
            }
        }
        delay(1000L)
        log("end")
    }
}