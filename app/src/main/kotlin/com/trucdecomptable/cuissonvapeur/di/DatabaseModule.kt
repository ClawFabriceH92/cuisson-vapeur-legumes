package com.trucdecomptable.cuissonvapeur.di

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import com.trucdecomptable.cuissonvapeur.data.local.CuissonVapeurDatabase
import com.trucdecomptable.cuissonvapeur.data.local.dao.CartDao
import com.trucdecomptable.cuissonvapeur.data.local.dao.CookingSessionDao
import com.trucdecomptable.cuissonvapeur.data.local.dao.FavoriteDao
import com.trucdecomptable.cuissonvapeur.data.local.dao.SettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CuissonVapeurDatabase =
        Room.databaseBuilder(
            context,
            CuissonVapeurDatabase::class.java,
            CuissonVapeurDatabase.DATABASE_NAME,
        ).build()

    @Provides
    fun provideFavoriteDao(db: CuissonVapeurDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideCartDao(db: CuissonVapeurDatabase): CartDao = db.cartDao()

    @Provides
    fun provideSettingsDao(db: CuissonVapeurDatabase): SettingsDao = db.settingsDao()

    @Provides
    fun provideCookingSessionDao(db: CuissonVapeurDatabase): CookingSessionDao =
        db.cookingSessionDao()

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
