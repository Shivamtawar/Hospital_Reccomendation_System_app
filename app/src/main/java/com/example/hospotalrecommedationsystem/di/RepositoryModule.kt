package com.example.hospotalrecommedationsystem.di


import com.example.hospotalrecommedationsystem.data.repository.HospitalRepository
import com.example.hospotalrecommedationsystem.data.repository.HospitalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHospitalRepository(
        hospitalRepositoryImpl: HospitalRepositoryImpl
    ): HospitalRepository
}