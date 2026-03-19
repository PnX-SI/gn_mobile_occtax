package fr.geonature.occtax.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.tinylog.kotlin.Logger
import java.time.Clock
import java.time.ZoneId
import javax.inject.Singleton

/**
 * Clock module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
class ClockModule {

    /**
     * Gets the default system clock.
     *
     * @return the configured system clock to use
     */
    @Singleton
    @Provides
    fun provideClock(): Clock {
        val clock = Clock.systemDefaultZone()

        Logger.info { "system current time zone: ${ZoneId.of(clock.zone.id)}" }

        return clock
    }
}