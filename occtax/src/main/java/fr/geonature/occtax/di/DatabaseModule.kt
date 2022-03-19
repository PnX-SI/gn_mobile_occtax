package fr.geonature.occtax.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.ContentProviderAuthority
import javax.inject.Singleton

/**
 * Database module.
 *
 * @author S. Grimault
 */
@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Singleton
    @Provides
    @ContentProviderAuthority
    fun provideContentProviderAuthority(@ApplicationContext appContext: Context): String {
        return "${appContext.packageName}.provider"
    }
}