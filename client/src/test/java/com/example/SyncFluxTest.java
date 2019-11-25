package com.example;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class SyncFluxTest {

  private static final Logger logger = LoggerFactory.getLogger(SyncFluxTest.class);

  @Test
  void test() throws Exception {
    final Flux<Long> generator = Flux.interval(Duration.ofSeconds(1L), Duration.ofMillis(100L))
        .doOnNext(g -> logger.info("start item: {}", g));

    final Flux<Long> writer = generator.doOnNext(g -> logger.info("writer : {}", g));
    final Flux<Long> reader = generator.doOnNext(g -> logger.info("reader : {}", g))
        .flatMap(g -> g % 7 == 3 ?
            Mono.delay(Duration.ofMillis(320L)).map(l -> g) :
            Mono.delay(Duration.ofMillis(50L)).map(l -> g));

    final Flux<Tpl> flux = writer
        .join(reader, l -> Mono.delay(Duration.ofMillis(400L)), Mono::just, Tpl::new)
        .doOnNext(t -> logger.info("join: {}", t))
        .filter(Tpl::isSame);
    final CountDownLatch latch = new CountDownLatch(1);
    try(AutoCloseable ignore =   flux
        .take(30L)
        .doAfterTerminate(latch::countDown)
        .subscribe(tpl -> logger.info("value: {}", tpl))::dispose) {
      latch.await();
    }
  }

  static class Tpl {
    final long left;
    final long right;

    Tpl(final long left, final long right) {
      this.left = left;
      this.right = right;
    }

    boolean isSame() {
      return left == right;
    }

    @Override
    public String toString() {
      return String.format("[%d, %d]", left, right);
    }
  }
}
