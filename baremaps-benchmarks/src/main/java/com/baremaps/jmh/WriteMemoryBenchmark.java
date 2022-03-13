package com.baremaps.jmh;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class WriteMemoryBenchmark {

  @Param({
      "1000",
      "10000",
      "100000",
      "1000000",
      "10000000",
      "100000000",
      "1000000000",
  })
  private int bytes;

  private byte value = 1;

  private Path file;

  @Setup
  public void setup() throws IOException {
    file = Files.createTempFile("benchmark_", ".tmp");
  }

  @TearDown
  public void tearDown() throws IOException {
    Files.delete(file);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 3)
  public void writeByteArray() {
    byte[] array = new byte[bytes];
    for (int i = 0; i < bytes; i++) {
      array[i] = (byte) i;
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 3)
  public void writeByteBuffer() {
    ByteBuffer buffer = ByteBuffer.allocate(bytes);
    for (int i = 0; i < bytes; i++) {
      buffer.put(i, (byte) i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 3)
  public void writeDirectByteBuffer() {
    ByteBuffer buffer = ByteBuffer.allocateDirect(bytes);
    for (int i = 0; i < bytes; i++) {
      buffer.put(i, (byte) i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 3)
  public void writeMemoryMappedBuffer() throws IOException {
    try (FileChannel channel =
        FileChannel.open(
            file,
            StandardOpenOption.CREATE,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE)) {
      MappedByteBuffer buffer = channel.map(MapMode.READ_WRITE, 0, bytes);
      for (int i = 0; i < bytes; i++) {
        buffer.put(i, (byte) i);
      }
      buffer.force();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 3)
  public void writeBufferedOutputStream() throws IOException {
    try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(file), 50_000)) {
      for (int i = 0; i < bytes; i++) {
        outputStream.write((byte) i);
      }
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(WriteMemoryBenchmark.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }
}
