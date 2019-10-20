# Kotlin 协程

## 什么是协程

协程时一套由 Kotlin 提供的线程框架。

类似于 Java 的 Executor 和 Android 的 AsyncTask，Kotlin 的协程也对 Thread 相关的 API 做了一套封装，让我们不用过多关心线程也可以很方便的写出并发操作。

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

```java
fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext, // 上下文
    start: CoroutineStart = CoroutineStart.DEFAULT,    // 启动模式
    block: suspend CoroutineScope.() -> Unit           // 协程体
): Job
```

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

## 启动模式

```kotlin
enum class CoroutineStart {
    DEFAULT,        // 立即执行协程体
    LAZY,           // 只有在需要的情况下运行
    ATOMIC,         // 立即执行协程体，但在开始运行之前无法取消
    UNDISPATCHED    // 立即在当前线程执行协程体，直到第一个 suspend 调用
}
```

- DEFAULT： 饿汉式启动，launch 调用后，会立即进入待调度状态，一旦调度器 OK 就可以开始执行。
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

CoroutineContext 只是一种类似于 Map 的数据结构，里面存储的是 (Key, Element) 的键值对。
同时 Element 继承至 CoroutineContext，并且包含了 Key。
Key 其实是代表了 Element 的类型，或者说功能。

| 功能（Key） | Element                   |
| ----------- | ------------------------- |
| 协程名字    | CoroutineName             |
| 拦截挂起点  | ContinuationInterceptor   |
| 异常处理    | CoroutineExceptionHandler |

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
     |       |- MainCoroutineDispatcher
     |       |- ExecutorCoroutineDipatcher
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

dispatch() 方法会在拦截器的方法 interceptContinuation() 中调用，进而实现协程的调度。

```kotlin
object Dispatchers {
    val Default: CoroutineDispatcher    // 线程池
    val Main: MainCoroutineDispatcher   // UI 线程
    val Unconfined: CoroutineDispatcher // 直接执行
    val IO: CoroutineDispatcher         // 线程池
}
```

- Default：使用默认的公共线程池【线程数量 = max(2, CPU 核心数)】。
- Main：在主线程中运行（单一线程）
- Unconfined：就是不指定线程，直接执行
- IO：适用于 IO 密集型的任务【线程数量 = max(64, CPU 核心数)】。共享 Default 的线程，因此协程使用 withContext 从 Default 切换到 IO 并不会触发线程切换。

`F_DispatcherTest.kt`

自定义调度器：

```kotlin
val myDispatcher = Executors.newSingleThreadExecutor{ r -> Thread(r, "MyThread") }
        .asCoroutineDispatcher()

// 需要手动关闭
myDispatcher.close()
```

## 作用域

协程的作用域涉及到协程的取消和异常的传播。

异常处理器是 CoroutineExceptionHandler，它本身也是一个 CoroutineContext。

`G_ScopeTest`

- GlobeScope：单独启动一个协程作用域，内部的子协程遵从默认的作用域规则。
- coroutineScope：继承外部 Job 的上下文创建作用域，在其内部的取消操作和未捕获异常都是双向传播的。
它更适合一系列对等的协程并发的完成一项工作，任何一个子协程异常退出，那么整体都将退出，简单来说就是”一损俱损“。这也是协程内部再启动子协程的默认作用域。
- supervisorScope：同样是继承外部 Job 的上下文创建作用域，但是取消和异常是单向传播的，只能由父协程向子协程传播，反过来则不行。
它更适合一些独立不相干的任务，任何一个任务出问题，并不会影响其他任务的工作，简单来说就是”自作自受“。
需要注意的是，supervisorScope 内部启动的子协程内部再启动子协程，如无明确指出，则遵守默认作用域规则，也即 supervisorScope 只作用域其直接子协程。

## 参考

- [破解 Kotlin 协程](https://juejin.im/user/5cea6293e51d45775e33f4dd)