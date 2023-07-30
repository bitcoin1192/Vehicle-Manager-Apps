package com.sisalma.vehicleandusermanagement.model.API

import android.app.Application
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
import okhttp3.internal.cacheGet
import java.util.concurrent.TimeUnit

data class LoginBody(var intent: String, var username: String, var password: String, var macaddress: String?, var simnumber: String?)
data class LoginResponse(val success: Boolean = true ,var msg: String, var uid: Int)

data class IntentOnly(var intent: String, val changeData: String = "Empty")
data class UserBody(var intent: String, var changeData: List<UserData>)
data class UserData(var password: String, var query: String, var macaddress: String, var name:String)

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
    @POST("userOps")
    suspend fun searchUserUID(@Body body: UserBody): NetworkResponse<ResponseSuccess, ResponseError>
    @POST("userOps")
    suspend fun addVehicle(@Body body: UserBody): NetworkResponse<ResponseSuccess, ResponseError>
    @POST("userOps")
    suspend fun cookiesCheck(@Body body: IntentOnly): NetworkResponse<ResponseSuccess, ResponseError>
    @POST("userOps")
    suspend fun logout(@Body body: IntentOnly): NetworkResponse<ResponseSuccess, ResponseError>

}

interface VehicleBackend {
    @POST("vehicleOps")
    suspend fun transferOwnership(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>

    @POST("vehicleOps")
    suspend fun addFriend(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>

    @POST("vehicleOps")
    suspend fun removeFriend(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>

    @POST("vehicleOps")
    suspend fun lockRequestVehicle(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>

    @POST("vehicleOps")
    suspend fun getVehicleSummary(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>
    @POST("vehicleOps")
    suspend fun getVehicleData(@Body body: GroupBody): NetworkResponse<ResponseSuccess, ResponseError>
}
class CustomCookies(val cache: CookieCache,val persistence: CookiePersistor): PersistentCookieJar(cache,persistence){
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cache.clear()
        cache.addAll(cookies)
        persistence.clear()
        persistence.saveAll(cookies)
    }
}
class APIEndpoint private constructor(){
    companion object{
        // the volatile modifier guarantees that any thread that
        // reads a field will see the most recently written value
        @Volatile private var api: APIEndpoint? = null
        fun getInstance(context: Application) = api ?: synchronized(this){
            api ?: APIEndpoint().also {
                it.cookieJar = CustomCookies(SetCookieCache(),SharedPrefsCookiePersistor(context))
                api = it
            }
        }
    }

    val BASE_URL = "https://dev-api.sisalma.com/"
    //val BASE_URL = "http://192.168.30.250:8080/"
    //val BASE_URL = "http://192.168.137.1:8080/"
    //val BASE_URL = "http://10.21.159.239:5000/"
    lateinit var cookieJar: CustomCookies
    fun getHTTPClient():Retrofit{
        val customOkHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(15,TimeUnit.SECONDS)
            .readTimeout(20,TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .baseUrl(BASE_URL)
            .client(customOkHttpClient)
            .build()
    }

    val loginService by lazy {
        getHTTPClient().create(BackendService::class.java)
    }
    val userService by lazy {
        getHTTPClient().create(UserBackend::class.java)
    }
    val vehicleService by lazy {
        getHTTPClient().create(VehicleBackend::class.java)
    }
}
/*open class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
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
*/