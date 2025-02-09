package org.apache.datasketches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.datasketches.Utils.IterationPlan;
import static org.apache.datasketches.Utils.computeIterationPlans;
import org.apache.datasketches.hll.HllSketch;
import org.apache.datasketches.hll.TgtHllType;
import org.apache.datasketches.memory.WritableMemory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import static org.apache.datasketches.Params.LG_K;
import static org.apache.datasketches.Params.LG_K_DEFAULT;
import static org.apache.datasketches.Params.LG_MAX_BP_U;
import static org.apache.datasketches.Params.LG_MAX_BP_U_DEFAULT;
import static org.apache.datasketches.Params.LG_MAX_T;
import static org.apache.datasketches.Params.LG_MAX_T_DEFAULT;
import static org.apache.datasketches.Params.LG_MAX_U;
import static org.apache.datasketches.Params.LG_MAX_U_DEFAULT;
import static org.apache.datasketches.Params.LG_MIN_BP_U;
import static org.apache.datasketches.Params.LG_MIN_BP_U_DEFAULT;
import static org.apache.datasketches.Params.LG_MIN_T;
import static org.apache.datasketches.Params.LG_MIN_T_DEFAULT;
import static org.apache.datasketches.Params.LG_MIN_U;
import static org.apache.datasketches.Params.LG_MIN_U_DEFAULT;
import static org.apache.datasketches.Params.OFFHEAP;
import static org.apache.datasketches.Params.OFFHEAP_DEFAULT;
import static org.apache.datasketches.Params.TGT_HLL_TYPE;
import static org.apache.datasketches.Params.TGT_HLL_TYPE_DEFAULT;
import static org.apache.datasketches.Params.TRIALS;
import static org.apache.datasketches.Params.UNIQUES;
import static org.apache.datasketches.Params.U_PPO;
import static org.apache.datasketches.Params.U_PPO_DEFAULT;
import static org.apache.datasketches.Params.V_IN;
import static org.apache.datasketches.Params.V_IN_DEFAULT;
import org.openjdk.jmh.runner.options.VerboseMode;
import static org.apache.datasketches.Params.parseSystemPropertyParams;

@Warmup(iterations = 0)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1)
@Threads(value = 1)
public class HllUpdateSpeedBenchmark {

  @State(Scope.Benchmark)
  public static class Params {
    @Param({LG_K_DEFAULT})
    public int lgK;

    @Param({OFFHEAP_DEFAULT})
    public boolean offheap;

    @Param({TGT_HLL_TYPE_DEFAULT})
    public TgtHllType tgtHllType;

    @Param({LG_MIN_U_DEFAULT})
    public int lgMinU;

    @Param({LG_MAX_U_DEFAULT})
    public int lgMaxU;

    @Param({U_PPO_DEFAULT})
    public int uPPO;

    @Param({LG_MIN_T_DEFAULT})
    public int lgMinT;

    @Param({LG_MAX_T_DEFAULT})
    public int lgMaxT;

    @Param({LG_MIN_BP_U_DEFAULT})
    public int lgMinBpU;

    @Param({LG_MAX_BP_U_DEFAULT})
    public int lgMaxBpU;

    @Param({V_IN_DEFAULT})
    public long vIn;

    @Param({"1"})
    public int uniques;

    @Param({"1"})
    public int trials;

    public HllSketch sketch;

    @Setup(Level.Trial)
    public void setup() {
      if (offheap) {
        final int bytes = HllSketch.getMaxUpdatableSerializationBytes(lgK, tgtHllType);
        sketch = new HllSketch(lgK, tgtHllType, WritableMemory.allocateDirect(bytes));
      }
      else {
        sketch = new HllSketch(lgK, tgtHllType);
      }
    }
  }

  @Benchmark
  public HllSketch test(Params p) {
    for (int i = p.trials; i-- > 0;) {
      for (int u = p.uniques; u-- > 0;) {
        p.sketch.update(++p.vIn);
      }
      p.sketch.reset();
    }
    return p.sketch;
  }

  /*
   * ============================== HOW TO RUN THIS TEST: ====================================
   *
   * Note the performance is different with different parameters.
   *
   * You can run this test:
   *
   * a) Via the command line:
   *  $ mvn clean install
   *  $ java -cp target/datasketches-jmh-benchmarks.jar org.apache.datasketches.HllUpdateSpeedBenchmark
   *
   *  You also can modify default parameters through the command line
   *  e.g.
   *  $ java -cp target/datasketches-jmh-benchmarks.jar \
   *    -DlgK=14
   *    -Doffheap=false,true \
   *    -DtgtHllType=HLL_4,HLL_6,HLL_8 \
   *    org.apache.datasketches.HllUpdateSpeedBenchmark
  */
  public static void main(String[] args) throws RunnerException {
    final int lgMinU = Integer.parseInt(parseSystemPropertyParams(LG_MIN_U, LG_MIN_U_DEFAULT)[0]);
    final int lgMaxU = Integer.parseInt(parseSystemPropertyParams(LG_MAX_U, LG_MAX_U_DEFAULT)[0]);
    final int uPPO = Integer.parseInt(parseSystemPropertyParams(U_PPO, U_PPO_DEFAULT)[0]);
    final int lgMinT = Integer.parseInt(parseSystemPropertyParams(LG_MIN_T, LG_MIN_T_DEFAULT)[0]);
    final int lgMaxT = Integer.parseInt(parseSystemPropertyParams(LG_MAX_T, LG_MAX_T_DEFAULT)[0]);
    final int lgMinBpU = Integer.parseInt(parseSystemPropertyParams(LG_MIN_BP_U, LG_MIN_BP_U_DEFAULT)[0]);
    final int lgMaxBpU = Integer.parseInt(parseSystemPropertyParams(LG_MAX_BP_U, LG_MAX_BP_U_DEFAULT)[0]);

    final Map<Integer, IterationPlan> iterationPlans = computeIterationPlans(
      lgMinU,
      lgMaxU,
      uPPO,
      lgMinBpU,
      lgMaxBpU,
      lgMinT,
      lgMaxT
    );

    final Collection<RunResult> runResults = new ArrayList<>();
    for (IterationPlan iterationPlan : iterationPlans.values()) {
      Options opt = new OptionsBuilder()
        .include(HllUpdateSpeedBenchmark.class.getName() + "\\.test$")
        .operationsPerInvocation(iterationPlan.uniques * iterationPlan.trials)
        .param(UNIQUES, String.valueOf(iterationPlan.uniques))
        .param(TRIALS, String.valueOf(iterationPlan.trials))
        .param(V_IN, String.valueOf(iterationPlan.vIn))
        .param(LG_K, parseSystemPropertyParams(LG_K, LG_K_DEFAULT))
        .param(OFFHEAP, parseSystemPropertyParams(OFFHEAP, OFFHEAP_DEFAULT))
        .param(TGT_HLL_TYPE, parseSystemPropertyParams(TGT_HLL_TYPE, TGT_HLL_TYPE_DEFAULT))
        .param(LG_MIN_U, parseSystemPropertyParams(LG_MIN_U, LG_MIN_U_DEFAULT))
        .param(LG_MAX_U, parseSystemPropertyParams(LG_MAX_U, LG_MAX_U_DEFAULT))
        .param(U_PPO, parseSystemPropertyParams(U_PPO, U_PPO_DEFAULT))
        .param(LG_MIN_T, parseSystemPropertyParams(LG_MIN_T, LG_MIN_T_DEFAULT))
        .param(LG_MAX_T, parseSystemPropertyParams(LG_MAX_T, LG_MAX_T_DEFAULT))
        .param(LG_MIN_BP_U, parseSystemPropertyParams(LG_MIN_BP_U, LG_MIN_BP_U_DEFAULT))
        .param(LG_MAX_BP_U, parseSystemPropertyParams(LG_MAX_BP_U, LG_MAX_BP_U_DEFAULT))
        .verbosity(VerboseMode.SILENT)
        .build();

      runResults.addAll(new Runner(opt).run());
    }

    ResultFormatFactory.getInstance(
      ResultFormatType.TEXT,
      System.out
    ).writeOut(runResults);
  }
}
