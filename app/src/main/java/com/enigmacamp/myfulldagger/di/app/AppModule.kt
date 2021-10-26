package com.enigmacamp.myfulldagger.di.app

import android.app.Application
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.enigmacamp.myfulldagger.BuildConfig
import com.enigmacamp.myfulldagger.data.SharedPref
import com.enigmacamp.myfulldagger.data.api.AuthApi
import com.enigmacamp.myfulldagger.data.api.interceptor.AuthTokenInterceptor
import com.enigmacamp.myfulldagger.data.db.AppDatabase
import com.enigmacamp.myfulldagger.data.db.CustomerDao
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
class AppModule {
    private val BASE_URL: String = BuildConfig.API_URL

    @Singleton
    @Provides
    fun provideOkHttpClient(authTokenIntercept: AuthTokenInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authTokenIntercept)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofitInstance(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun authApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }


    @Singleton
    @Provides
    fun provideRoomDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "enigma_database"
        ).build()
    }

    @Provides
    fun provideCustomerDao(db: AppDatabase): CustomerDao {
        return db.customerDao()
    }

    @Provides
    @Singleton
    fun provideSharedPref(application: Application): SharedPref {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val pref = EncryptedSharedPreferences.create(
            "SharedPref",
            masterKeyAlias,
            application,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return SharedPref(pref)
    }
}