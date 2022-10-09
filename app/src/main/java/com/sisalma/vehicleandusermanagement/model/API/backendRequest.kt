package com.sisalma.vehicleandusermanagement.model.API

import androidx.appcompat.app.AppCompatActivity
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.JsonObject
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import okhttp3.Cookie
import okhttp3.HttpUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

data class LoginBody(var intent: String, var username: String, var password: String)
data class LoginResponse(val success: Boolean = true ,var msg: String, var uid: Int)

data class IntentOnly(var intent: String, val changeData: String = "Empty")
data class UserBody(var intent: String, var changeData: List<UserData>)
data class UserData(var password: String)

data class GroupBody(var intent: String, var changeMember: List<ChangeMemberForm>)
data class ChangeMemberForm(val UID: Int = 0, val VID: Int)

data class ResponseError(val success:Boolean = false, var errMsg: String)
data class ResponseSuccessMsg(val success:Boolean = true, var msg: JsonObject)
data class ResponseSuccess(val success:Boolean = true, var msg: JsonObject)
data class ResponseListSuccess(val success:Boolean = true, var msg: JsonObject)

interface BackendService {
    @POST("loginOps")
    suspend fun loginEndpoint(@Body body: LoginBody): NetworkResponse<LoginResponse, ResponseError>
}

interface UserBackend {
    @POST("userOps")
    suspend fun getKnownVehicle(@Body body: IntentOnly): NetworkResponse<ResponseSuccess, ResponseError>

    @POST("userOps")
    suspend fun editUserData(@Body body: UserBody): NetworkResponse<ResponseSuccess, ResponseError>
}

interface VehicleBackend {
    @POST("vehicleOps")
    suspend fun transferOwnership(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>

    @POST("vehicleOps")
    suspend fun addFriend(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>

    @POST("vehicleOps")
    suspend fun removeFriend(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>

    @POST("vehicleOps")
    suspend fun getVehicleSummary(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>
}
class CustomCookies(val cache: CookieCache,val persistence: CookiePersistor): PersistentCookieJar(cache,persistence){
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cache.addAll(cookies)
        persistence.saveAll(cookies)
    }
}
class APIEndpoint (context: AppCompatActivity){
    //val BASE_URL = "https://dev-api.sisalma.com/"
    val BASE_URL = "http://192.168.30.181:5000/"
    val cookieJar = CustomCookies(SetCookieCache(),SharedPrefsCookiePersistor(context.applicationContext))
    val customOkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(10,TimeUnit.SECONDS)
        .readTimeout(20,TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .baseUrl(BASE_URL)
        .client(customOkHttpClient)
        .build()

    val loginService by lazy {
        retrofit.create(BackendService::class.java)
    }
    val userService by lazy {
        retrofit.create(UserBackend::class.java)
    }
    val vehicleService by lazy {
        retrofit.create(VehicleBackend::class.java)
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