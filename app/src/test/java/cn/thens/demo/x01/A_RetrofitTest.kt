package cn.thens.demo.x01

import android.os.Handler
import android.os.Looper
import cn.thens.demo.ITest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class A_RetrofitTest : ITest {

    interface GitHubService {
        @GET("users/{user}")
        fun getUser(@Path("user") user: String): Call<User>

        @GET("users/{user}")
        suspend fun getUserWithCoroutine(@Path("user") user: String): User
    }

    data class User(val name: String)

    val gitHubService by lazy {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        retrofit.create(GitHubService::class.java)
    }

    fun requestUserInfo() {
        val handler = Handler(Looper.getMainLooper())
        gitHubService.getUser("android").enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                handler.post {
                    val user = response.body()
                    log(user)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                handler.post {
                    log(t)
                }
            }
        })
    }

    fun requestUserInfoWithCoroutine() {
        GlobalScope.launch(Dispatchers.Main) {
            // 进入协程：主线程
            try {
                val user = gitHubService.getUserWithCoroutine("android") // 网络请求：IO 线程
                // 更新UI：主线程
                log(user)
            } catch (e: Exception) {
                // 错误处理：主线程
                log(e)
            }
        }
    }
}