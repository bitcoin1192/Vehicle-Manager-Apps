package com.sisalma.vehicleandusermanagement.model.API

import androidx.appcompat.app.AppCompatActivity
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.OkHttpClient

data class LoginBody(var intent: String, var username: String, var password: String)
data class LoginResponse(val success: Boolean = true ,var msg: String, var uid: Int)

data class GroupBody(var intent: String, var changeMember: List<ChangeMemberForm>)
data class ChangeMemberForm(val UID: Int = 0, val VID: Int)

data class ResponseError(val success:Boolean = false, var errmsg: String)
data class ResponseSuccess(val success:Boolean = true, var msg: String)

interface backendService {
    @POST("loginOps")
    suspend fun loginEndpoint(@Body body: LoginBody): NetworkResponse<LoginResponse, ResponseError>

    @POST("groupOps")
    suspend fun vehicleEndpoint(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>
}

class APIEndpoint (context: AppCompatActivity){
    val BASE_URL = "https://dev-api.sisalma.com/"
    val cookieJar = PersistentCookieJar(SetCookieCache(),SharedPrefsCookiePersistor(context.applicationContext))
    val customOkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .baseUrl(BASE_URL)
        .client(customOkHttpClient)
        .build()

    val loginService by lazy {
        retrofit.create(backendService::class.java)
    }
}
open class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}