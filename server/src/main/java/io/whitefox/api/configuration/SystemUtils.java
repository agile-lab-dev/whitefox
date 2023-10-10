package io.whitefox.api.configuration;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;

import java.time.Clock;

public class SystemUtils {
    @Singleton
    @Produces
    Clock clock() {
        return Clock.systemUTC();
    }
}
