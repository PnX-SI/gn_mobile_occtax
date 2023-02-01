package fr.geonature.occtax.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.geonature.datasync.auth.ICookieManager
import javax.inject.Singleton

/**
 * "Occtax" Api module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object OcctaxApiModule {

    @Singleton
    @Provides
    fun provideOcctaxAPIClient(cookieManager: ICookieManager): IOcctaxAPIClient {
        return OcctaxAPIClientImpl(cookieManager)
    }
}