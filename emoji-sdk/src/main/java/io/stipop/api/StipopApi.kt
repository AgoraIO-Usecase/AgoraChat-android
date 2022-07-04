package io.stipop.api

import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import com.google.gson.Gson
import io.stipop.BuildConfig
import io.stipop.Config
import io.stipop.Constants
import io.stipop.models.body.*
import io.stipop.models.response.*
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.lang.Exception
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit

@Keep
internal interface StipopApi {

    @POST("init")
    suspend fun initSdk(
            @Body initSdkBody: InitSdkBody
    ): StipopResponse

    @GET("curation/type/{type}")
    suspend fun getCurationPackages(
            @Path("type") curationType: String,
            @Query("userId") userId: String,
            @Query("lang") lang: String? = Locale.getDefault().language,
            @Query("countryCode") countryCode: String? = Locale.getDefault().country,
            @Query("pageNumber") pageNumber: Int = 1,
            @Query("limit") limit: Int = 12
    ): CurationPackageResponse

    @GET("package/{packageId}")
    suspend fun getStickerPackage(
            @Path("packageId") packageId: Int,
            @Query("userId") userId: String,
    ): StickerPackageResponse

    @GET("search/keyword")
    suspend fun getRecommendedKeywords(
            @Query("userId") userId: String,
            @Query("lang") lang: String,
            @Query("countryCode") countryCode: String,
    ): KeywordListResponse

    @GET("package/send/{userId}")
    suspend fun getRecentlySentStickers(
            @Path("userId") userId: String,
            @Query("pageNumber") pageNumber: Int,
            @Query("limit") limit: Int
    ): StickerListResponse

    @GET("mysticker/favorite/{userId}")
    suspend fun getFavoriteStickers(
            @Path("userId") userId: String,
            @Query("pageNumber") pageNumber: Int,
            @Query("limit") limit: Int
    ): FavoriteListResponse

    @GET("mysticker/{userId}")
    suspend fun getMyStickers(
            @Path("userId") userId: String,
            @Query("pageNumber") pageNumber: Int,
            @Query("limit") limit: Int
    ): MyStickerResponse

    @GET("mysticker/hide/{userId}")
    suspend fun getMyHiddenStickers(
            @Path("userId") userId: String,
            @Query("pageNumber") pageNumber: Int,
            @Query("limit") limit: Int
    ): MyStickerResponse

    @PUT("mysticker/favorite/{userId}")
    suspend fun putMyStickerFavorite(
            @Path("userId") userId: String,
            @Body favoriteBody: FavoriteBody
    ): StipopResponse

    @PUT("mysticker/order/{userId}")
    suspend fun putMyStickerOrders(
            @Path("userId") userId: String,
            @Body orderChangeBody: OrderChangeBody
    ): MyStickerOrderChangedResponse

    @PUT("mysticker/hide/{userId}/{packageId}")
    suspend fun putMyStickerVisibility(
            @Path("userId") userId: String,
            @Path("packageId") packageId: Int
    ): StipopResponse

    @GET("package")
    suspend fun getTrendingStickerPackages(
            @Query("userId") userId: String,
            @Query("lang") lang: String,
            @Query("countryCode") countryCode: String,
            @Query("pageNumber") pageNumber: Int,
            @Query("limit") limit: Int,
            @Query("q") query: String? = null
    ): StickerPackagesResponse

    @GET("search")
    suspend fun getStickers(
            @Query("userId") userId: String,
            @Query("lang") lang: String,
            @Query("countryCode") countryCode: String,
            @Query("pageNumber") pageNumber: Int,
            @Query("limit") limit: Int,
            @Query("q") query: String? = null
    ): StickersResponse

    @GET("package/new")
    suspend fun getNewStickerPackages(
            @Query("userId") userId: String,
            @Query("lang") lang: String,
            @Query("countryCode") countryCode: String,
            @Query("pageNumber") pageNumber: Int,
            @Query("limit") limit: Int,
            @Query("q") query: String? = null
    ): StickerPackagesResponse

    @POST("download/{packageId}")
    suspend fun postDownloadStickers(
            @Path("packageId") packageId: Int,
            @Query("userId") userId: String,
            @Query("isPurchase") isPurchase: String,
            @Query("countryCode") countryCode: String,
            @Query("lang") lang: String,
            @Query("price") price: Double? = null,
            @Query("entrance_point") entrancePoint: String? = null,
            @Query("event_point") eventPoint: String? = null,
    ): StipopResponse

    @POST("sdk/track/config")
    suspend fun trackConfig(@Body userIdBody: UserIdBody): StipopResponse

    @POST("sdk/track/view/picker")
    suspend fun trackViewPicker(@Body userIdBody: UserIdBody): Response<StipopResponse>

    @POST("sdk/track/view/search")
    suspend fun trackViewSearch(@Body userIdBody: UserIdBody): Response<StipopResponse>

    @POST("sdk/track/view/store")
    suspend fun trackViewStore(@Body userIdBody: UserIdBody): Response<StipopResponse>

    @POST("sdk/track/view/new")
    suspend fun trackViewNew(@Body userIdBody: UserIdBody): Response<StipopResponse>

    @POST("sdk/track/view/mysticker")
    suspend fun trackViewMySticker(@Body userIdBody: UserIdBody): Response<StipopResponse>

    @POST("sdk/track/view/package/{entrance_point}/{package_id}")
    suspend fun trackViewPackage(
            @Body userIdBody: UserIdBody,
            @Path("entrance_point") entrancePoint: String? = Constants.Point.DEFAULT,
            @Path("package_id") packageId: Int
    ): Response<StipopResponse>

    @POST("analytics/send/{stickerId}")
    suspend fun trackUsingSticker(
            @Path("stickerId") stickerId: String,
            @Query("userId") userId: String,
            @Query("q") query: String? = null,
            @Query("countryCode") countryCode: String,
            @Query("lang") lang: String,
            @Query("event_point") eventPoint: String? = null
    ): StipopResponse

    companion object {
        private val loggingInterceptor = HttpLoggingInterceptor().apply { level = Level.BODY }
        private val headers = Headers.Builder()
            .add(
                    Constants.ApiParams.ApiKey, if (Constants.Value.IS_SANDBOX) {
                Constants.Value.SANDBOX_APIKEY
            } else {
                Config.stipopConfigData.apiKey
            }
            )
            .add(
                    Constants.ApiParams.SMetadata,
                    Gson().toJson(
                            StipopMetaHeader(
                                    platform = Constants.Value.PLATFORM,
                                    sdk_version = BuildConfig.SDK_VERSION_NAME,
                                    os_version = Build.VERSION.SDK_INT.toString()
                            )
                    )
            )
            .build()


        private val client = try {
            OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(Interceptor {
                        it.proceed(it.request().newBuilder().headers(headers).build())
                    })
                    .addNetworkInterceptor {
                        it.proceed(it.request())
                    }
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .readTimeout(30000, TimeUnit.MILLISECONDS)
                    .writeTimeout(30000, TimeUnit.MILLISECONDS)
                    .build()
        }catch (e: SocketTimeoutException){
            Log.e("OkHttpClient","OkHttpClient SocketTimeoutException")
        }


        fun create(): StipopApi {
            return Retrofit.Builder()
                .baseUrl(if (Constants.Value.IS_SANDBOX) Constants.Value.SANDBOX_URL else Constants.Value.BASE_URL)
                .client(client as OkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(StipopApi::class.java)
        }
    }
}
