package com.jeju.evtravel.di

import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.service.auth.CourseBookmarkService
import com.jeju.evtravel.service.auth.FirebaseAuthService
import com.jeju.evtravel.service.auth.PlaceBookmarkService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun providePlaceBookmarkService(db: FirebaseFirestore): PlaceBookmarkService {
        return PlaceBookmarkService(db)
    }

    @Provides
    @Singleton
    fun provideCourseBookmarkService(db: FirebaseFirestore): CourseBookmarkService {
        return CourseBookmarkService(db)
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthService(): FirebaseAuthService {
        return FirebaseAuthService
    }
}