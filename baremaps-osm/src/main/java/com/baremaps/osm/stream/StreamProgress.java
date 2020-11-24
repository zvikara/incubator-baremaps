package com.baremaps.osm.stream;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamProgress<T> implements Consumer<T> {

  private static Logger logger = LoggerFactory.getLogger(StreamProgress.class);

  private final long size;

  private final AtomicLong position;

  private final AtomicLong timestamp;

  private final Function<T, Long> increment;

  public StreamProgress(long size) {
    this(size, e -> 1l);
  }

  public StreamProgress(long size, Function<T, Long> increment) {
    this.size = size;
    this.position = new AtomicLong(0);
    this.timestamp = new AtomicLong(System.currentTimeMillis());
    this.increment = increment;
  }

  @Override
  public void accept(T e) {
    long i = increment.apply(e);
    long p = position.addAndGet(i);
    long t = System.currentTimeMillis();
    long l = timestamp.get();
    if (t - l >= 5000) {
      timestamp.set(t);
      double progress = Math.round(p * 10000d / size) / 100d;
      logger.info("Progress: {}%", progress);
    }
    if (p == size) {
      double progress = 100d;
      logger.info("Progress: {}%", progress);
    }
  }

}
