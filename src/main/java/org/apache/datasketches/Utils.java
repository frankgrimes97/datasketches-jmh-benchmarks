package org.apache.datasketches;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.apache.datasketches.common.Util.pwr2SeriesNext;

public class Utils {
  public static final double LN2 = log(2.0);

  public static class IterationPlan {
    public final int uniques;
    public final int trials;
    public final long vIn;

    IterationPlan(final int uniques, final int trials, final long vIn) {
      this.uniques = uniques;
      this.trials = trials;
      this.vIn = vIn;
    }
  }

  public static final Map<Integer, IterationPlan> computeIterationPlans(
      final int lgMinU,
      final int lgMaxU,
      final int uPPO,
      final int lgMinBpU,
      final int lgMaxBpU,
      final int lgMinT,
      final int lgMaxT) {

    final int maxU = 1 << lgMaxU;
    final int minU = 1 << lgMinU;

    final Map<Integer, IterationPlan> iterationPlans = new LinkedHashMap<>();
    int iteration = 0;
    int lastU = 0;
    long vIn = 0;
    while (lastU < maxU) { //Trials for each U point on X-axis, and one row on output
      iteration++;
      final int nextU = lastU == 0 ? minU : (int)pwr2SeriesNext(uPPO, lastU);
      lastU = nextU;

      final int trials = getNumberOfTrials(lastU, lgMinBpU, lgMaxBpU, lgMinT, lgMaxT);

      iterationPlans.put(Integer.valueOf(iteration), new IterationPlan(lastU, trials, vIn));
      vIn += lastU * trials;
    }
    return iterationPlans;
  }

  public static final int getNumberOfTrials(
      final int uniques,
      final int lgMinBpU,
      final int lgMaxBpU,
      final int lgMinT,
      final int lgMaxT) {

      final int minBpU = 1 << lgMinBpU;
      final int maxBpU = 1 << lgMaxBpU;
      final int maxT = 1 << lgMaxT;
      final int minT = 1 << lgMinT;
      final double slope = (double) (lgMaxT - lgMinT) / (lgMinBpU - lgMaxBpU);

      if (lgMinT == lgMaxT || uniques <= minBpU) {
        return maxT;
      }
      if (uniques >= maxBpU) {
        return minT;
      }
      final double lgCurU = log(uniques) / LN2;
      final double lgTrials = slope * (lgCurU - lgMinBpU) + lgMaxT;
      return (int) pow(2.0, lgTrials);
  }
}
