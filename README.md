# Kotlin 协程

## 什么是协程

协程是一套由 Kotlin 提供的线程框架。

类似于 Java 的 Executor 和 Android 的 AsyncTask，
Kotlin 的协程也对 Thread 相关的 API 做了一套封装，让我们不用过多关心线程也可以很方便的写出并发操作。

Kotlin 协程的最大好处在于，你可以把运行的不同线程的代码写在同一个代码块里（用看起来同步的方式写出异步代码）。

```kotlin
launch(Dispatchers.Main) {      // 开始：主线程
    val token = api.getToken()  // 网络请求：后台线程
    val user = api.getUser()    // 网络请求：后台线程
    tvName.text = user.name     // 更新 UI：主线程
}
```

`A_RetrofitTest.kt`

## 新建协程

```kotlin
GlobalScope.launch {
    // do what you want
}
```

`B_BuilderTest.kt`

Coroutine Builders

- launch: 在后台新建一个协程，并立即执行，返回 Job。
- async: 同 launch，但是返回 Deferred。
- runBlocking：新建一个协程，同时阻塞当前线程，直到协程结束。一般在测试时使用。
- withContext: 将当前协程切换到指定的 context 中运行（不会新建协程）。

> 从 withContext 的功能中可以看到，一个协程可以被切换到不同的线程里执行，也就是说线程和协程并非包含关系，而是并列关系。

```java
fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext, // 上下文
    start: CoroutineStart = CoroutineStart.DEFAULT,    // 启动模式
    block: suspend CoroutineScope.() -> Unit           // 协程体
): Job
```

**Job**

Job 和 Thread 的功能基本上是一致的。
Deferred 是 Job 的子类，可以通过 await() 来获取返回值。

```kotlin
interface Job : CoroutineContext.Element {
    val isActive: Boolean
    val isCompleted: Boolean
    val isCancelled: Boolean
    fun start(): Boolean
    fun cancel(): Unit
    suspend fun join()
}
```

```java
public class Thread implements Runnable {
    public final boolean isAlive();
    public native boolean isInterrupted();
    public synchronized void start();
    public final void stop();
    public void interrupt();
    public final void join();
}
```

Job 的状态。

| **State**                 | [isActive] | [isCompleted] | [isCancelled] |
| ------------------------- | ---------- | ------------- | ------------- |
| _New_ (可选的初始状态)    | `false`    | `false`       | `false`       |
| _Active_ (默认的初始状态) | `true`     | `false`       | `false`       |
| _Completing_ (中间状态)   | `true`     | `false`       | `false`       |
| _Cancelling_ (中间状态)   | `false`    | `false`       | `true`        |
| _Cancelled_ (最终状态)    | `false`    | `true`        | `true`        |
| _Completed_ (最终状态)    | `false`    | `true`        | `false`       |

```plain
                                        wait children
+-----+  start  +--------+  complete   +-------------+  finish  +-----------+
| New | ------> | Active | --------->  | Completing  | -------> | Completed |
+-----+         +--------+             +-------------+          +-----------+
                 |  cancel / fail       |
                 |     +----------------+
                 |     |
                 V     V
             +------------+                           finish  +-----------+
             | Cancelling | --------------------------------> | Cancelled |
             +------------+                                   +-----------+
```

## 启动模式

```kotlin
enum class CoroutineStart {
    DEFAULT,        // 立即执行协程体
    LAZY,           // 只有在需要的情况下运行
    ATOMIC,         // 立即执行协程体，但在开始运行之前无法取消
    UNDISPATCHED    // 立即在当前线程执行协程体，直到第一个 suspend 调用
}
```

- DEFAULT：饿汉式启动，launch 调用后，会立即进入待调度状态，一旦调度器 OK 就可以开始执行。
- LAZY：懒汉式启动， launch 后并不会有任何调度行为，协程体也自然不会进入执行状态，直到调用了 start() 或者 join()。
- ATOMIC：在遇到第一个挂起点之前，不会停止协程。
- UNDISPATCHED：立即执行，在遇到第一个挂起点之前，不会切线程（不经过任何调度器）。

> ATOMIC 和 UNDISPATCHED 都处于实验性阶段，不建议使用。

`C_StartModeTest.kt`

## 协程上下文

```kotlin
public interface CoroutineContext {
    public operator fun <E : Element> get(key: Key<E>): E?
    public fun <R> fold(initial: R, operation: (R, Element) -> R): R
    public operator fun plus(context: CoroutineContext): CoroutineContext = ...
    public fun minusKey(key: Key<*>): CoroutineContext

    public interface Key<E : Element>

    public interface Element : CoroutineContext {
        public val key: Key<*>
    }
}
```

CoroutineContext 本质上是一种类似于 Map 的数据结构，里面存储的是 (Key, Element) 的键值对。
同时 Element 继承至 CoroutineContext，并且包含了 Key。
Key 其实是代表了 Element 的类型，或者说功能。

| 功能（Key）  | Element                     |
| ------------ | --------------------------- |
| 协程名字     | `CoroutineName`             |
| 拦截挂起点   | `ContinuationInterceptor`   |
| 异常处理     | `CoroutineExceptionHandler` |
| 当前任务     | `Job`, `Deferred`           |
| 协程局部数据 | `ThreadLocalElement`        |

CoroutineContext 的继承关系图。

```plain
CoroutineContext
 |- EmptyCoroutineContext
 |- CombinedContext
 |- Element
     |- AbstractContineContextElement
     |   |- CoroutineName
     |- ContinuationInterceptor
     |   |- CoroutineDispatcher
     |- CoroutineExceptionHandler
     |- Job
         |- Deferred
```

`D_ContextTest.kt`

CoroutineName 可以用来给协程添加名字，方便调试。

如果需要多个上下文，可以直接使用`+`号，生成一个 CombinedContext。

```kotlin
Dispatchers.Main + CoroutineName("Hello")
```

> 从 CombinedContext 源码可以知道，`+` 号右边的上下文优先级高于左边的。

## 拦截器

```kotlin
public interface ContinuationInterceptor : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<ContinuationInterceptor>

    fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T>

    fun releaseInterceptedContinuation(continuation: Continuation<*>)
}
```

`E_InterceptorTest.kt`

Continuation 是一个回调，表示一个协程里面，两个相邻的恢复点和挂起点之间的代码片段。
拦截器就是用来拦截 Continuation 的。正是因为有了拦截器，协程才有了切换线程的功能。

## 调度器

```kotlin
abstract class CoroutineDispatcher : ContinuationInterceptor {
    abstract fun dispatch(context: CoroutineContext, block: Runnable)
}
```

调度器的 dispatch() 方法会在拦截器的 interceptContinuation() 方法中调用，进而实现协程的调度。

```kotlin
object Dispatchers {
    val Default: CoroutineDispatcher    // 使用线程池，线程数量为 max(2, CPU 核心数)
    val Main: MainCoroutineDispatcher   // 在主线程中运行（单一线程）
    val Unconfined: CoroutineDispatcher // 不指定线程，直接执行
    val IO: CoroutineDispatcher         // 使用线程池，线程数量为 max(64, CPU 核心数)
}
```

IO 调度器会共享 Default 的线程池，因此协程使用 withContext 从 Default 切换到 IO 并不会触发线程切换。

`F_DispatcherTest.kt`

自定义调度器：

```kotlin
val myDispatcher = Executors.newSingleThreadExecutor{ r -> Thread(r, "MyThread") }
        .asCoroutineDispatcher()

// 在 JVM 中需要手动关闭
myDispatcher.close()
```

默认情况下，`launch()`会使用父协程的调度器，如果父协程没有，则使用`Default`调度器。

## 作用域

```kotlin
interface CoroutineScope {
    val coroutineContext: CoroutineContext
}
```

协程作用域只是一个简单的接口，用来提供上下文的值。

协程的创建必须依赖于作用域——`launch`和`async`是`CoroutineScope`的扩展函数 。

一个新的协程就是一个新的作用域（拥有新的上下文），它们的关系可以并列，也可以包含，组成了一个作用域的树形结构。

```kotlin
GlobalScope.launch {
    log(1)
    launch {
        log(2)
        launch {
            log(3)
        }
    }
    launch {
        log(4)
        GlobalScope.launch {
            log(5)
        }
    }
}
```

作用域树

```plain
(1)
 |- (2)
 |   |- (3)
 |- (4)

(5)
```

默认情况下，每个父协程都要等待它的子协程全部完成后，才能结束自己。
当一个父协程被取消的时候，所有它的子协程也会被递归的取消。

但 **GlobalScope** 比较特殊，它创建的协程会拥有一个完全独立的顶级作用域（因为它的上下文默认为空）。
在 GlobalScope 中启动的协程类似于守护线程，不能让进程保活。
慎用。

> 从这里可以看到协程和线程的另一个不同：协程是结构化的，而线程不是。

`G_ScopeTest`

## 异常处理

协程里的异常都是可以 try-catch 的。
对于没有 try-catch 的异常，你可以添加一个`CoroutineExceptionHandler`的上下文来处理，
它和`Thread.UncaughtExceptionHandler`很相似。

**取消协程**

协程的取消是通过在挂起点抛出`CancellationException`异常来实现的。
这个异常并不会影响父协程，所以当你使用`Job.cancel()`来取消一个协程时，并不会引起它的父协程的取消。
但如果协程抛出了`CancellationException`以外的异常，那么它的父协程将会被取消，而且它的根作用域内的所有协程都会被取消。

当协程被取消时，你可以在它的挂起点捕捉到`CancellationException`。
当该异常被捕捉时，协程会继续执行到下一个挂起点，然后又会抛出`CancellationException`。
也就是说，协程只允许你短暂地捕获`CancellationException`。

**SupervisorJob**

默认情况下，协程的异常是双向传播的。一个协程的异常，会引起整个作用域内所有协程的取消。
但在很多时候，我们希望子协程的异常不要影响到父协程，这时候可以使用`SupervisorJob`。

`SupervisorJob`类似于`Job`，唯一的不同是，`SupervisorJob`的异常只能单向传播——从父协程传向子协程。

```kotlin
GlobalScope.launch {
    log(1)
    val supervisorJob = SupervisorJob()
    launch(supervisorJob) {
        log(2)
        throw Exception("3.e")
    }
    yield()
    log(4)
}
```

## 参考

- [破解 Kotlin 协程](https://juejin.im/user/5cea6293e51d45775e33f4dd)
