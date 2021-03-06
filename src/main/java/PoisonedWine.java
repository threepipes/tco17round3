import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

// --- sub start ---
class BR {
    public static BufferedReader br;
    public static int readInt() {
      try {
        return Integer.parseInt(br.readLine());
      } catch (Exception e) {
        return -1;
      }
    }
}

class PoisonTest {
    static PoisonedWineVis vis;
    public static int[] useTestStrips(String[] tests) {
        if(vis != null) {
            return vis.useTestStrips(tests);
        } else {
            System.out.println("?");
            System.out.println(tests.length);
            for (String t : tests)
                System.out.println(t);
            System.out.flush();

            int n = BR.readInt();
            int[] res = new int[n];
            for (int i = 0; i < n; ++i)
                res[i] = BR.readInt();
            return res;
        }
    }
}
// --- sub end ---

// -------8<------- start of solution submitted to the website -----8<-------
public class PoisonedWine {
    // params
    final int upperWineLeft = 500;
    final int VAL_MAX = 1000;
    static double LOG_COEF      = 0.3 + 0.002 * 50;  // 0.30-0.50:0.002*100
    static double TEST_SUB      = 0.0 + 0.05  * 46; // 0-5:0.05*100
    static double ROUND_COEF    = 1.0 + 0.01  * 27; // 1.0-2.0:0.01*100
    static double WID_COEF      = 0.95+ 0.002 * 83; // 0.95-1.15:0.002*100
    static double TEST_COEF     = 0.0 + 0.002 * 26; // 0.0-0.2:0.002*100
    static double STRIP_COEF    = 0.5 + 0.01  * 24; // -0.1-0.1:0.002*100
    static double Y_COEF        = 1.8 + 0.004 * 35; // 1.8-2.2:0.004*100
    static double X_COEF        = 0.9 + 0.002 * 71; // 0.9-1.1:0.002*100
    static double SHUFFLE_COEF  = 0.0 + 0.1   * 39;
//    static double LOG_COEF      = 0.3 + 0.002 * 78;  // 0.30-0.50:0.002*100
//    static double TEST_SUB      = 0.0 + 0.05  * 78; // 0-5:0.05*100
//    static double ROUND_COEF    = 1.0 + 0.01  * 11; // 1.0-2.0:0.01*100
//    static double WID_COEF      = 0.95+ 0.002 * 63; // 0.95-1.15:0.002*100
//    static double TEST_COEF     = 0.0 + 0.002 * 44; // 0.0-0.2:0.002*100
//    static double STRIP_COEF    = 0.5 + 0.01  * 26; // -0.1-0.1:0.002*100
//    static double Y_COEF        = 1.8 + 0.004 * 27; // 1.8-2.2:0.004*100
//    static double X_COEF        = 0.9 + 0.002 * 37; // 0.9-1.1:0.002*100
//    static double SHUFFLE_COEF  = 0.0 + 0.1   * 30;
    final double Y_OFFSET = 0.082;
    static double ROUND_OFFSET = 1;
    static int randSeed = 0;
    // --- sub start ---
    public StringBuilder logger = new StringBuilder();
    // --- sub end ---
    Random rand = new Random(randSeed);
    void shuffle(int[] a, int len) {
        for(int i = 0; i < len; i++) {
            int i1 = i;
            int i2 = rand.nextInt(len);
            int tmp = a[i1];
            a[i1] = a[i2];
            a[i2] = tmp;
        }
    }

    long calcTime1(int w) {
        return (long) (S * (S + Math.log(w) / Math.log(2)) * w * P * P / 10000);
    }

    long calcTime2(int w) {
        return (long)(13L * S * S * R * w * Math.log(w) / Math.log(2) / 300000);
    }

    int upperWine() {
        int left = Math.min(upperWineLeft, W);
        int right = W;
        int ans = -1;
        while(left <= right) {
            int mid = (left + right) / 2;
            if(calcTime1(mid) + calcTime2(mid) < 50000) {
                ans = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return ans;
    }

    final double REG_BASE = 37.54114071;
    final double REG_WINE_COEF = -0.000656549;
    final double REG_POIS_COEF = -0.005272914;
    final double REG_ROUD_COEF = 7.676318797;
    final double REG_STRP_COEF = -0.306667327;

    int W, P, S, R;
    int curW;
    double est = -1;
    int[] rangeLen, rangePoison;
    int rangeN;
    int maxW;
    int findOneRest;
    public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
        W = numBottles;
        P = numPoison;
        S = testStrips;
        R = testRounds;
        rangeLen = new int[S + 1];
        rangePoison = new int[S + 1];
        rangeLen[0] = W;
        rangePoison[0] = P;
        rangeN = 1;
//        maxW = upperWine();
        double log2W = Math.log(W) / Math.log(2);
        long time1 = (long) (S * (S + log2W) * W * P * P / 10000);
        long time2 = (long)(13L * S * S * R * W * log2W / 300000);
        // --- sub start ---
        logger.append(String.format("preinfo,%d,%d",
                PoisonedWineVis.seedL, maxW)).append("\n");
        // --- sub end ---
        // --- cut start ---
        System.out.println("val: " + ((long)W * S * R * R * P / 1000));
        System.out.println("estimate calc time: " + time1 + " + " + time2);
        System.out.println("upper wine: " + maxW);
        // --- cut end ---
        double reg = -1; // 重回帰分析結果(初手のみ)
//        initWidProb();
        int[] bottle = new int[numBottles];
        for(int i = 0; i < numBottles; i++) {
            bottle[i] = i;
        }
//        long timesec = System.currentTimeMillis();
        shuffle(bottle, bottle.length);
        for(int test = 0; test < testRounds && testStrips > 0 && numBottles > P; test++) {
            boolean changeRange = false;
            // --- sub start ---
            double regProb = REG_BASE
                    + REG_WINE_COEF * numBottles
                    + REG_POIS_COEF * P
                    + REG_ROUD_COEF * (testRounds)
                    + REG_STRP_COEF * testStrips;
            logger.append(String.format("preinfo,%d,%d",
                    PoisonedWineVis.seedL, maxW)).append("\n");
            // --- sub end ---
            if(P == 1 && (numBottles < (1 << testStrips) || testRounds - test == 1)) {
                // 1発でワインを求められるケース
                // --- cut start ---
                System.out.println("find one!");
                System.out.println("estimate calc time: " + (S * S * W * P * P));
                // --- cut end ---
                numBottles = findOnePoison(bottle, numBottles, numBottles, testStrips);
                testStrips = findOneRest;
                continue;
            }
            final long VAL = (long)numBottles * testStrips * (testRounds - test)
                    * (testRounds - test) * P / 1000;
            curW = numBottles;
            int n;
            int round = testRounds - test;
            double bestWidth = Math.min(
                    Y_OFFSET + Y_COEF * numBottles /
                            ((numPoison * X_COEF - LOG_COEF * Math.log10(round)
                                    * (testStrips * STRIP_COEF - TEST_SUB))
                                    * (round * ROUND_COEF + ROUND_OFFSET)),
                    numBottles / Math.min(numBottles, testStrips)
            );// * Math.pow(WID_COEF, test);
//            if(bestWidth < 1) {
//                System.err.println("");
//            }
            if(bestWidth < 1) {
                bestWidth = numBottles / Math.min(numBottles, testStrips);
            }
            if(numBottles == 1 && bestWidth > numBottles / 2) {
                bestWidth = numBottles / 2;
                if(bestWidth < 1) bestWidth = 1;
            }
            if(false && VAL < VAL_MAX && bestWidth < 1) {
                    //(VAL < 5000 || P < 10) && numBottles < 4000 && (numBottles < 1000 || P * (testRounds - test) < 33)) {
                if(widProb == null) {
                    initComb();
                    initWidProb();
                }
                // --- cut start ---
                long time = -1;
                if(estCache.isEmpty()) time = System.currentTimeMillis();
                if(numPoison >= 15 && testRounds - test > 1 && numBottles > 1000)
                    System.out.printf("dist begin w:%d t:%d p:%d s:%d\n",
                            numBottles, testRounds - test, numPoison, testStrips);
//                System.out.println("estimate calc time: " + (S * W * P * P));
                // --- cut end ---
//                if(numBottles > 200) {
//                n = (int)(estimateBestWidthCont(testStrips, testRounds - test) * curW);
//                } else {
                    n = estimateBestWidth(numBottles, testStrips, testRounds - test);
//                }
                // --- sub start ---
                double pb = probNoPoison(numBottles, numPoison, n);
                // --- sub end ---
                // --- cut start ---
                if(time >= 0) time = System.currentTimeMillis() - time;
                System.out.printf("wine:%4d poi:%3d str:%2d n:%d round:%d (prob:%f rd:%f) est:%f time:%d\n",
                        numBottles, numPoison, testStrips, n,
                        testRounds - test, pb, regProb - pb * 100, preEst, time);
                // --- cut end ---
//                if(rangePoison[0] == 1 && findOneExp(rangeLen[0], testStrips) >= preEst) {
//                    numBottles = findOnePoison(bottle, rangeLen[0], numBottles, testStrips);
//                    testStrips = findOneRest;
//                    rangeLen[0] = numBottles;
//                    rangePoison[0] = numPoison;
//                    rangeN = 1;
//                    continue;
//                }

                // --- sub start ---
                logger.append(String.format("probdiffD,%d,%f,%f,%f",
                        PoisonedWineVis.seedL, regProb - pb * 100, regProb, pb)).append("\n");
                // --- sub end ---
            } else if(false && maxW > 0 && bestWidth < 1) {
                if(widProb == null) {
                    initComb();
                    initWidProb();
                }
                // --- cut start ---
                System.out.println("cont begin");
                // --- cut end ---
                /**/
                double maxEst = 0;
                changeRange = true;
                int maxId = 0;
                n = -1;
                int sumBottle = 0;
                int sumPoison = 0;
                for(int i = 0; i < rangeN; i++) {
                    sumBottle += rangeLen[i];
                    sumPoison += rangePoison[i];
                    P = sumPoison;
                    curW = sumBottle;
                    double width = estimateBestWidthCont(testStrips, testRounds - test);
//                    int width = estimateBestWidth(sumBottle, testStrips, testRounds - test);
                    if(maxEst < preEst) {
                        maxEst = preEst;
                        maxId = i;
                        n = (int) Math.ceil(width * sumBottle);
                    }
                }
                P = numPoison;
                curW = numBottles;
                // range[1]～range[id]をrange[0]に統合
                for(int i = 1; i <= maxId; i++) {
                    rangeLen[0] += rangeLen[i];
                    rangePoison[0] += rangePoison[i];
                }
                for(int i = maxId + 1; i < rangeN; i++) {
                    rangeLen[i - maxId] = rangeLen[i];
                    rangePoison[i - maxId] = rangePoison[i];
                }
                rangeN -= maxId;
//                shuffle(bottle, rangeLen[0]);
                // --- sub start ---
                double pb = probNoPoison(numBottles, numPoison, n);
                // --- sub end ---
                // --- cut start ---
                System.out.printf("maxEst:%f maxId:%d sumBottle:%d sumPoison:%d\n",
                        maxEst, maxId, sumBottle, sumPoison, n);
                System.out.printf("wine:%4d poi:%3d str:%2d n:%d round:%d (prob:%f)\n",
                        numBottles, numPoison, testStrips, n,
                        testRounds - test, pb);
                // --- cut end ---
                // --- sub start ---
                logger.append(String.format("probdiffC,%d,%f,%f,%f",
                        PoisonedWineVis.seedL, regProb - pb * 100, regProb, pb)).append("\n");
                // --- sub end ---
                /**/
            } else {
                // --- cut start ---
//                System.out.println("regression");
                // --- cut end ---
//                int round = testRounds - test;
//                double bestWidth = Math.min(
//                        2 * numBottles / ((numPoison - 0.46 * Math.log10(round) * (testStrips - 3)) * (round * 1.1 + 1)),
//                        numBottles / testStrips
//                );
//                double bestWidth = -1;
                if(bestWidth < 1) {
                    // 重回帰分析結果
                    double prob = REG_BASE
                            + REG_WINE_COEF * numBottles
                            + REG_POIS_COEF * P
                            + REG_ROUD_COEF * (testRounds - test)
                            + REG_STRP_COEF * testStrips;
                    if (prob >= 90) prob = 90;
                    if (reg < 0) reg = prob;
                    bestWidth = searchWidByProb(numBottles, numPoison, (int) Math.round(prob));
                }
                n = (int) bestWidth;
                // --- sub start ---
                logger.append(String.format("reg,%d,%f,,", PoisonedWineVis.seedL, regProb)).append("\n");
                // --- sub end ---
            }
            int newN = (int) (n * Math.pow(WID_COEF, test - testRounds * TEST_COEF));
            if(newN * testStrips <= numBottles) n = newN;
//            if(testStrips == 1 && n > numBottles / 2) n = numBottles / 2;

            if(!changeRange) {
                rangeLen[0] = numBottles;
                rangePoison[0] = numPoison;
                rangeN = 1;
            }
            if(n <= 0) n = (numBottles + testStrips - 1) / testStrips;
            List<String> tests = new ArrayList<>();
            for (int i = 0; i < testStrips; ++i) {
                if(i * n >= numBottles) break;
                String t = "" + bottle[i * n];
                for (int j = 1; j < n && i * n + j < numBottles; ++j)
                    t += "," + bottle[i * n + j];
                tests.add(t);
            }
            int[] testRes = PoisonTest.useTestStrips(tests.toArray(new String[0]));
            int next = 0;
            int used = 0;
            List<Integer> warning = new ArrayList<>();
            for (int i = 0; i * n < numBottles; ++i) {
                if (i >= testRes.length || testRes[i] == 1) {
                    if (i < testRes.length) {
                        // 毒を含む場合
                        int j;
                        for (j = 0; j < n && i * n + j < numBottles; ++j)
                            warning.add(bottle[i * n + j]);
//                        if(changeRange) {
                            rangeLen[0] -= j;
                            rangePoison[0]--;
                            rangeLen[rangeN] = j;
                            rangePoison[rangeN++] = 1;
//                        }
                        used++;
                    } else for (int j = 0; j < n && i * n + j < numBottles; ++j) {
                        // 今回考えられていない範囲
                        bottle[next++] = bottle[i * n + j];
                    }
                } else {// if(changeRange) {
                    // 毒がなかった場合
                    final int num = Math.min(n, numBottles - i * n);
                    rangeLen[0] -= num;
                }
            }
            // bottleの先頭 n * used は毒密度が高いので後ろに回す
            // 区間ごとに毒密度管理するとか -> たぶん地獄
            for(int w: warning) bottle[next++] = w;
            testStrips -= used;
            numBottles = next;
            if(rangePoison[0] == 0) {
                // 先頭範囲の毒が0になった場合

                for(int i = rangeLen[0]; i < numBottles; i++) {
                    bottle[i - rangeLen[0]] = bottle[i];
                }
                numBottles -= rangeLen[0];
                for(int i = 1; i < rangeN; i++) {
                    rangeLen[i - 1] = rangeLen[i];
                    rangePoison[i - 1] = rangePoison[i];
                }
                rangeN--;
            }
            // --- cut start ---
            int pcnt = 0;
            for(int i = 0; i < numBottles; i++) {
                if(PoisonedWineVis.bottles[bottle[i]]) pcnt++;
            }
            if(pcnt != P) {
                System.err.println("Wrong!!");
            }
//            dump(rangeLen, rangeN);
//            dump(rangePoison, rangeN);
            // --- cut end ---
            if(rangeN > 1 && rangeLen[0] <= rangeLen[1] * SHUFFLE_COEF) {
                rangeLen[0] = numBottles;
                rangePoison[0] = numPoison;
                rangeN = 1;
                shuffle(bottle, numBottles);
            }
        }
        int[] ret = new int[numBottles];
        for (int i = 0; i < numBottles; ++i)
            ret[i] = bottle[i];
        int saved = W - numBottles;

        // --- cut start ---
        if(est >= 0)
            System.out.printf("est:%.20f real:%d diff:%.20f diff/acc:%.20f \n",
                est, saved, est - saved, (est - saved) / (W - P));
        System.out.println("saved: " + saved);
        System.out.printf("end seed:%d reg:%f cacheEst:%d cacheDeath:%d\n",
                PoisonedWineVis.seedL, reg, estCache.size(), deathCache.size());
        // --- cut end ---
//        System.err.println("time: " + (System.currentTimeMillis() - timesec));
        return ret;
    }

    void dump(int[] a, int len) {
        for(int i = 0; i < len; i++) {
            System.out.printf("%4d ", a[i]);
        }
        System.out.println();
    }

    int[][] widProb;
    void initWidProb() {
        widProb = new int[W + 1][100];
        for(int wine = P; wine <= W; wine++) {
            for(int prob = 1; prob < 100; prob++) {
                widProb[wine][prob] = searchWidByProb(wine, P, prob);
            }
        }
    }

    int searchWidByProb(int wine, int poison, int probability) {
        double prob = probability / 100.0;
        int left = 1;
        int right = wine - poison;
        int ans = -1;
        while(left <= right) {
            int mid = (left + right) / 2;
            if(probNoPoison(wine, poison, mid) >= prob) {
                ans = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return ans;
    }

    double probNoPoison(int wine, int poison, int n) {
        // n本選んで毒が入っていない確率
        if(n > wine - poison) return poison > 0 ? 1 : 0;
        return perm[wine - poison]
                .multiply(perm[wine - n], MathContext.DECIMAL32)
                .divide(perm[wine - poison - n], MathContext.DECIMAL32)
                .divide(perm[wine], MathContext.DECIMAL32).doubleValue();
    }

    int getWidInterval(int wine, int strip) {
        final int WID_MAX = Math.min(wine - P, wine / strip);
        if(WID_MAX <= 50) return 1;
        return WID_MAX / 50;
    }

    int estimateBestWidth(int wine, int strip, int round) {
        if(strip == 0) return 1;
        int left = Math.max(1, Math.min(widProb[wine][90], wine / strip));
        int right = Math.max(Math.min(widProb[wine][30], wine / strip), left);
        int wid = left;
        double expMax = 0;
        while(left <= right) {
            int wid1 = (left * 2 + right) / 3;
            int wid2 = (left + right * 2) / 3;
            double exp1 = estDfs(wine, Math.min(strip, wine / wid1), round, wid1);
            double exp2 = estDfs(wine, Math.min(strip, wine / wid2), round, wid2);
            if(exp1 < exp2) {
                left = wid1 + 1;
                wid = wid2;
            } else {
                right = wid2 - 1;
                wid = wid1;
            }
            // --- cut start ---
//            if(round >= 2 && wine > 1000 && P > 8)
//                System.out.printf("wid1:%d exp1:%f wid2:%d exp2:%f\n",
//                        wid1, exp1, wid2, exp2);
            expMax = Math.max(exp1, exp2);
            // --- cut end ---
        }
        preEst = expMax;
        if(est < 0) est = expMax;
        return wid;
    }

    HashMap<Long, Double> estCache = new HashMap<>();
    double estDfs(int wine, int strip, int round, int wid) {
        if(round == 0 || wine <= P || strip == 0) return 0;
        final long id = (long) wine * W * S * R + (long) strip * W * R
                + round * W + wid;
        if(estCache.containsKey(id)) return estCache.get(id);
        double res = 0;
        final int useStrips = Math.min(strip, wine / wid);
        final int saveStrips = strip - useStrips;
        for(int death = 0; death <= useStrips && death <= P; death++) {
            final double probOcc = calcDeath(wine, wid)[useStrips][death];
            final int s0 = useStrips - death;
            final int nextStrips = s0 + saveStrips;
//            final int otherSaved = death == P ? Math.max(0, wine - useStrips * wid) : 0;
            final int NEXT_WINE = wine - s0 * wid;// - otherSaved;
            if(round == 1 || nextStrips == 0) {
                res += probOcc * (s0 * wid);// + otherSaved);
                continue;
            }
            double maxStrategy = 0;
            int maxWid = 0;
            int left = Math.max(1, Math.min(widProb[wine][90], NEXT_WINE / nextStrips));
            int right = Math.max(Math.min(widProb[wine][30], NEXT_WINE / nextStrips), left);
            while(left <= right) {
                int wid1 = (left * 2 + right) / 3;
                int wid2 = (left + right * 2) / 3;
                double exp1 = estDfs(NEXT_WINE, nextStrips, round - 1, wid1);
                double exp2 = estDfs(NEXT_WINE, nextStrips, round - 1, wid2);
                if(exp1 < exp2) {
                    left = wid1 + 1;
                    maxStrategy = exp2;
                    maxWid = wid2;
                } else {
                    right = wid2 - 1;
                    maxStrategy = exp1;
                    maxWid = wid1;
                }
//                System.out.printf("wid1:%d exp1:%f wid2:%d exp2:%f\n",
//                        wid1, exp1, wid2, exp2);
            }
//            System.out.printf("D death %d -> exp: %f (maxst: %f) maxWid:%d\n",
//                    death, probOcc * maxStrategy, maxStrategy, maxWid);
            res += probOcc * (maxStrategy + s0 * wid);// + otherSaved);
        }
        estCache.put(id, res);
        return res;
    }

    // 使い方: calcDeath(wine, wid)[strip][death] -> 生起確率
    HashMap<Integer, double[][]> deathCache = new HashMap<>();
    double[] probCache = new double[201];
    double[][] calcDeath(int wine, int wid) {
        int id = wine * W + wid;
        if(deathCache.containsKey(id)) return deathCache.get(id);
        /*
         ワイン数，毒数，strip，1stripあたりのwine数のとき
         death本のstripが破壊される確率
         O(strip ^ 2 * poison ^ 2)
          */
        final int strip = Math.min(S, wine / wid);
        int deathMax = Math.min(P, strip);
        double[][][] dp = new double[strip + 1][P + 1][deathMax + 1];
        dp[0][P][0] = 1;
        for(int i = 0; i < strip; i++) {
            final int wineRem = wine - i * wid;
            for(int j = 0; j <= P && j <= wineRem; j++) {
                for(int l = 0; l <= Math.min(j, wid); l++) {
                    probCache[l] = prob(wineRem, j, wid, l);
                }
                for(int k = 0; k <= deathMax; k++) {
                    if(dp[i][j][k] == 0) continue;
                    dp[i + 1][j][k] += probCache[0] * dp[i][j][k];
                    for(int l = 1; l <= Math.min(j, wid); l++) {
                        dp[i + 1][j - l][k + 1] += probCache[l] * dp[i][j][k];
                    }
                }
            }
        }
        double[][] res = new double[strip + 1][strip + 1];
        for(int s = 1; s <= strip; s++) {
            for(int d = 0; d <= s; d++) {
                for(int p = 0; p <= P; p++) {
                    res[s][d] += d > P ? 0 : dp[s][p][d];
                }
            }
        }
        deathCache.put(id, res);
        return res;
    }

    double preEst = 0;
    double estimateBestWidthCont(int strip, int round) {
        /*
        O(strip * poison ^ 2 * wine)
         */
        double bestWid = 0;
        double bestExp = 0;
        double left = 1.0 / curW;//Math.max(1.0 / curW, (double) widProb[curW][80] / curW);
        double right = 1.0 / strip;
        for(int i = 0; i < 20; i++) {
            double wid1 = (left * 2 + right) / 3;
            double wid2 = (left + right * 2) / 3;
            double exp1 = getValueFromEstCache(strip, round, wid1);
            double exp2 = getValueFromEstCache(strip, round, wid2);
            if(exp1 < exp2) {
                left = wid1;
                bestWid = wid2;
                bestExp = exp2;
            } else {
                right = wid2;
                bestWid = wid1;
                bestExp = exp1;
            }
            if((int) (wid1 * curW) == (int) (wid2 * curW)) break;
            // --- cut start ---
//            System.out.printf("wid1:%f exp1:%f wid2:%f exp2:%f\n",
//                    wid1 * curW, exp1 * curW, wid2 * curW, exp2 * curW);
            // --- cut end ---
        }
        preEst = bestExp * curW;
        if(est < 0) est = bestExp * curW;
        return bestWid;
    }

    double[][][] estDfsContCache;
    double estDfsCont(int strip, int round, double wid) {
        if(wid < 1e-9) return 0;
        double res = 0;
        final int useStrips = Math.min(strip, (int)(1 / wid));
        final int saveStrips = strip - useStrips;
        for(int death = 0; death <= useStrips && death <= P; death++) {
            final double probOcc = probDeathCont(wid, useStrips, death);
            //calcDeathCont(wid)[useStrips][death];
            if(probOcc < 1e-8) continue;
            final int s0 = useStrips - death;
            final int nextStrips = s0 + saveStrips;
            final double NEXT_WINE = 1 - s0 * wid;
            if(round == 1 || nextStrips == 0) {
                res += probOcc * s0 * wid;
                continue;
            }
            double maxStrategy = 0;
            double maxWid = 0;
            double left = 0;
            double right = 1.0 / nextStrips;
            for(int i = 0; i < 13; i++) {
                double wid1 = (left * 2 + right) / 3;
                double wid2 = (left + right * 2) / 3;
                double exp1 = getValueFromEstCache(nextStrips, round - 1, wid1);
                double exp2 = getValueFromEstCache(nextStrips, round - 1, wid2);
                if(exp1 < exp2) {
                    left = wid1;
                    maxStrategy = exp2;
                    maxWid = wid2;
                } else {
                    right = wid2;
                    maxStrategy = exp1;
                    maxWid = wid1;
                }
//                System.out.printf("C wid1:%f exp1:%f wid2:%f exp2:%f\n",
//                        wid1 * NEXT_WINE * W, exp1, wid2 * W * NEXT_WINE, exp2);
            }
//            System.out.printf("C death %d -> exp: %f (maxst: %f) maxWid:%f\n",
//                    death, probOcc * (maxStrategy * NEXT_WINE + s0 * wid) * W, maxStrategy * W,
//                    maxWid * NEXT_WINE * W);
            res += probOcc * (maxStrategy * NEXT_WINE + s0 * wid);
        }
        return res;
    }

    double getValueFromEstCache(int strip, int round, double wid) {
        if(estDfsContCache == null) initEstDfsCont();
        int id = Arrays.binarySearch(widTable, wid);
        if(id < 0) id = -id - 1;
        if(id >= widTable.length) id--;
        return estDfsContCache[round][strip][id];
    }

    double probDeathCont(double wid, int useStrips, int death) {
        if(deathCont == null) initDeathCont();
        int id = Arrays.binarySearch(widTable, wid);
        if(id < 0) id = -id - 1;
        if(id >= widTable.length) id--;
        return deathCont[id][useStrips][death];
    }

    double[][] calcDeathCont(double wid) {
        assert 0 < wid && wid <= 1;
        /*
        幅や毒の存在位置を連続値として考えるときのcalcDeath
        内容はほぼ同じ
        wid:(0, 1]の実数
         O(strip ^ 2 * poison ^ 2)
         毒同士が衝突する確率が十分少ないとき，
         こちらでもほぼ正しい値が出てる
         widについて，1/wine刻みで前計算できればまあまあ使える
         strip:10, poison:40, wine:1000 -> 160000000なので厳しいか
         どちらかというと解析用
         */
        final int strip = Math.min(S, (int)(1 / wid + 0.5));
        int deathMax = Math.min(P, strip);
        double[][][] dp = new double[strip + 1][P + 1][deathMax + 1];
        dp[0][P][0] = 1;
        for(int i = 0; i < strip; i++) {
            final double widAllRem = 1 - i * wid;
            for(int j = 0; j <= P; j++) {
                for(int l = 0; l <= j; l++) {
                    probCache[l] = probCont(j, wid / widAllRem, l);
                }
                for(int k = 0; k <= deathMax; k++) {
                    if(dp[i][j][k] == 0) continue;
                    dp[i + 1][j][k] += probCache[0] * dp[i][j][k];
                    for(int l = 1; l <= j; l++) {
                        dp[i + 1][j - l][k + 1] += probCache[l] * dp[i][j][k];
                    }
                }
            }
        }
        double[][] res = new double[strip + 1][strip + 1];
        for(int s = 1; s <= strip; s++) {
            for(int d = 0; d <= s; d++) {
                for(int p = 0; p <= P; p++) {
                    res[s][d] += d > P ? 0 : dp[s][p][d];
                }
            }
        }
        return res;
    }

    double[][][] deathCont;
    double[] widTable;
    final int CONT_WINE_MAX = 3000;
    void initDeathCont() {
        // --- cut start ---
        System.out.println("start init deathcont");
        long time = System.currentTimeMillis();
        // --- cut end ---
        double[][][] map = new double[maxW][][];
        widTable = new double[maxW];
        for(int i = 1; i <= maxW; i++) {
            double wid = (double) i / maxW;
            widTable[i - 1] = wid;
            map[i - 1] = calcDeathCont(wid);
        }
        deathCont = map;
        // --- cut start ---
        System.out.printf("fin init deathcont: %dms\n", System.currentTimeMillis() - time);
        // --- cut end ---
    }

    void initEstDfsCont() {
        // --- cut start ---
        System.out.println("start init estcont");
        long time = System.currentTimeMillis();
        // --- cut end ---
        estDfsContCache = new double[R + 1][S + 1][maxW + 1];
        for(int round = 1; round <= R; round++) {
            for(int strip = 1; strip <= S; strip++) {
                for(int w = 1; w <= maxW; w++) {
                    estDfsContCache[round][strip][w]
                            = estDfsCont(strip, round, (double) w / maxW);
                }
            }
        }
        // --- cut start ---
        System.out.printf("fin init estcont: %dms\n", System.currentTimeMillis() - time);
        // --- cut end ---
    }

    double findOneExp(int wine, int strip) {
        if((1 << strip) >= wine) return wine - 1;
        return wine - (double) wine / (1 << strip);
    }

    /**
     * 引数3のfindOnePoisonを実行して，ボトルを整形する
     * @param bottle ワインリスト
     * @param range 調査したい(毒が1以下と判明している)範囲
     * @param trueBottleNum 実際に現在残っているワイン
     * @param strip
     * @return 調査後のワインリストサイズ
     */
    int findOnePoison(int[] bottle, int range, int trueBottleNum, int strip) {
        List<Integer> bad = findOnePoison(bottle, range, strip);
        int idx = 0;
        for(int i = 0; i < trueBottleNum - range; i++) {
            bottle[idx++] = bottle[range + i];
        }
        for(int b: bad) {
            bottle[idx++] = b;
        }
        return idx;
    }

    /**
     * 範囲内に毒が1つ以下の場合，ワインから毒を見つける有名な某問題の手法を用いる
     * 毒可能性のあるワインのリストを返す
     * 当然ながらtestRoundが残っていること前提
     * (注意: bottleの内容は変更しないので，goodbottleに基づいてbottleを修正すること)
     * @param bottle ワインリスト
     * @param wine 先頭からwine本のワインについて考える
     * @param strip
     * @return 危険ワインリスト
     */
    List<Integer> findOnePoison(int[] bottle, int wine, int strip) {
        String[] test = new String[strip];
        for(int i = 1; i <= wine; i++) {
            // iの下位stripビットを見て，立ってるビットに相当するtestStripにワインを加える
            // 1-indexedに注意(毒が0である場合を考慮するため)
            for(int j = 0; j < strip; j++) {
                if((i & 1 << j) == 0) continue;
                if(test[j] == null) test[j] = "" + bottle[i - 1];
                else test[j] += "," + bottle[i - 1];
            }
        }
        int used = strip;
        int nextStrip = strip;
        for(int i = 0; i < strip; i++) {
            if(test[i] == null) used--;
        }
        if(used < strip) {
            String[] resized = new String[used];
            System.arraycopy(test, 0, resized, 0, used);
            test = resized;
        }
        int[] result = PoisonTest.useTestStrips(test);
        int bit = 0;
        for(int i = 0; i < test.length; i++) {
            if(result[i] == 1) {
                bit |= 1 << i;
                nextStrip--;
            }
        }
        List<Integer> bad = new ArrayList<>();
        // 下位stripビットのビットパターンがbitに等しい場合毒の可能性
        int mask = (1 << strip) - 1;
        for(int i = 1; i <= wine; i++) {
            if((i & mask) == bit) bad.add(bottle[i - 1]);
        }
        findOneRest = nextStrip;
        return bad;
    }

//    HashMap<Long, Double> probCombCache = new HashMap<>();
    /**
     * stripにwid本のワインを使ったとき，毒がhit本引っかかる確率
     * @param wine ワイン
     * @param poison 毒
     * @param wid stripに使うワインの数
     * @param hit stripに引っかかる毒の数
     * @return hit本の毒が引っかかる確率
     */
    double prob(int wine, int poison, int wid, int hit) {
        // このif文も消したい
        if(comb[wine-wid][poison-hit] == null)
            return 0;
//        final long id = (long) wine * W * P * P
//                + (long) poison * W * P
//                + (long) wid * P + hit;
//        if(probCombCache.containsKey(id)) return probCombCache.get(id);
        return comb[wid][hit].multiply(comb[wine-wid][poison-hit], MathContext.DECIMAL32)
                .divide(comb[wine][poison], MathContext.DECIMAL32).doubleValue();
//        probCombCache.put(id, res);
//        return res;
    }

    double probCont(int poison, double wid, int hit) {
        return BigDecimal.valueOf(wid).pow(hit, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(1 - wid).pow(poison - hit, MathContext.DECIMAL32))
                .multiply(comb[poison][hit], MathContext.DECIMAL32).doubleValue();
    }

    void init() {
//        initComb();
        initPerm();
//        initWidProb(); // Poisonがないと初期化不可
    }

    final int WINE_MAX = 10000;
    final int POISONE_MAX = 200;
    BigDecimal[][] comb;
    void initComb() {
        comb = new BigDecimal[WINE_MAX + 1][POISONE_MAX + 1];
        for(int i = 0; i <= WINE_MAX; i++){
            for(int j = 0; j <= Math.min(i, POISONE_MAX); j++){
                if(j == 0 || j == i){
                    comb[i][j] = new BigDecimal(1);
                }else{
                    comb[i][j] = comb[i - 1][j - 1].add(comb[i - 1][j], MathContext.DECIMAL64);
                }
            }
        }
    }

    BigDecimal[] perm;
    void initPerm() {
        perm = new BigDecimal[WINE_MAX + 1];
        perm[0] = new BigDecimal(1, MathContext.DECIMAL64);
        for(int i = 1; i <= WINE_MAX; i++) {
            perm[i] = perm[i - 1].multiply(
                    new BigDecimal(i, MathContext.DECIMAL64), MathContext.DECIMAL64);
        }
    }

    public PoisonedWine() {
        // --- cut start ---
        PoisonTest.vis = null;
        // --- cut end ---
        init();
    }
    // ---8<------- end of solution submitted to the website -------8<-------
    // --- cut start ---
    public void testProb() {
        // 幅nで死なない確率
        // 50%未満の幅は得策ではないため，その枝刈りに使う予定
        System.out.println("testFunc");
        for(int wine = 1000; wine < 10000; wine += 1000) {
            System.out.println("Wine: " + wine);
            for(int poison = 1; poison < wine / 50; poison += 10) {
                System.out.println("Poison: " + poison);
                double pre = 1;
                for(int n = 5; n < wine / poison && pre > 0.2; n++) {
                    pre = probNoPoison(wine, poison, n);
                    System.out.println("n=" + n + " -> "
                            + String.format("%.20f  (save prob: %.20f)", pre, n * pre));

                }
            }
        }
    }

    double[] testProbDeath(int wine, int wid, int strip, int poison) {
        // wine <= 20
        // wid * strip < wine
        int m = wine;
        int p = poison;
        int eat = wid;
        int[] a = new int[strip];
        double[] res = new double[strip + 1];
        int base = (1 << eat) - 1;
        for(int i = 0; i < strip; i++) {
            a[i] = base << i * eat;
        }
        int all = 0;
        for(int i = 0; i < (1 << m); i++) {
            if(Integer.bitCount(i) != p) continue;
            all++;
            int d = 0;
            for(int j = 0; j < strip; j++) {
                if((a[j] & i) > 0) d++;
            }
            res[d]++;
        }
        for(int i = 0; i <= strip; i++) res[i] /= all;
        return res;
    }


    public static void main(String[] args) {
    try {
        BR.br = new BufferedReader(new InputStreamReader(System.in));

        int numBottles = BR.readInt();
        int testStrips = BR.readInt();
        int testRounds = BR.readInt();
        int numPoison = BR.readInt();

        PoisonedWine pw = new PoisonedWine();
        int[] ret = pw.testWine(numBottles, testStrips, testRounds, numPoison);

        System.out.println(ret.length);
        for (int i = 0; i < ret.length; ++i) {
            System.out.println(ret[i]);
        }
    }
    catch (Exception e) {}
    }
    // --- cut end ---

    // --- sub start ---
    public PoisonedWine(PoisonedWineVis vis) {
        PoisonTest.vis = vis;
        init();
    }
    // --- sub end ---
}