package com.haksoy.soip.di

import android.content.Context
import androidx.room.Room
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.database.AppDatabase
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.ProgressHelper
import com.haksoy.soip.utlis.getPreferencesString
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            Constants.APP_DATABASE + "_" + context.getPreferencesString(Constants.USER_UID, "")
        ).allowMainThreadQueries().build()
    }


    @Provides
    @Singleton
    fun provideFirebaseDao(): FirebaseDao {
        return FirebaseDao()
    }

    @Provides
    @Singleton
    fun provideProgressHelper(): ProgressHelper{
        return ProgressHelper();
    }
}