package com.example.hospotalrecommedationsystem.di



import android.content.Context
import com.example.hospotalrecommedationsystem.util.LocationUtils

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideLocationUtils(@ApplicationContext context: Context): LocationUtils {
        return LocationUtils(context)
    }
}