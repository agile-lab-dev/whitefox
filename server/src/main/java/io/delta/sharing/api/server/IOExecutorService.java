package io.delta.sharing.api.server;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.Executors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class IOExecutorService extends DelegateExecutorService {

  @Inject
  public IOExecutorService(
      @ConfigProperty(name = "delta.sharing.api.server.nThreads") int numThreads) {
    super(Executors.newWorkStealingPool(numThreads));
  }
}
