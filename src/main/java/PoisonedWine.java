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
    public static int[] useTestStrips(String[] tests, PoisonedWineVis vis) {
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
    static double LOG_COEF      = 0.3 + 0.002 * 50;  // 0.30-0.50:0.002*100
    static double TEST_SUB      = 0.0 + 0.05  * 94; // 0-5:0.05*100
    static double ROUND_COEF    = 1.0 + 0.01  * 24; // 1.0-2.0:0.01*100
    static double WID_COEF      = 0.95+ 0.002 * 51; // 0.95-1.15:0.002*100
    static double TEST_COEF     = 0.0 + 0.002 * 79; // 0.0-0.2:0.002*100
    static double STRIP_COEF    = 0.5 + 0.01  * 50; // -0.1-0.1:0.002*100
    static double Y_COEF        = 1.8 + 0.004 * 87; // 1.8-2.2:0.004*100
    static double X_COEF        = 0.9 + 0.002 * 59; // 0.9-1.1:0.002*100
    static double SHUFFLE_COEF  = 2;
    final double Y_OFFSET = 0.082;
    final int upperWineLeft = 500;  // param:300,5000,1000
    static int VAL_MAX = 1000;      // 0-4000:40*100
    static double ROUND_OFFSET = 1; // * 0.0-5.0:0.05*100
    // --- sub start ---
    public StringBuilder logger = new StringBuilder();
    // --- sub end ---
    static int randSeed = 3;
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
    static double BASE_ADD = 0;
    static double WINE_COEF_ADD = 0;
    static double POIS_COEF_ADD = 0;
    static double ROUD_COEF_ADD = 0;
    static double STRP_COEF_ADD = 0;

    static final double BASE = 37.54114071 + BASE_ADD;
    static final double WINE_COEF = -0.000656549 + WINE_COEF_ADD;
    static final double POIS_COEF = -0.005272914 + POIS_COEF_ADD;
    static final double ROUD_COEF = 7.676318797 + ROUD_COEF_ADD;
    static final double STRP_COEF = -0.306667327 + STRP_COEF_ADD;

    int W, P, S, R;
    int curW;
    double est = -1;
    int[] rangeLen, rangePoison;
    int rangeN;
    int maxW;
    StringBuilder sb = new StringBuilder();
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
        double reg = -1; // 重回帰分析結果(初手のみ)
//        initWidProb();
        int[] bottle = new int[numBottles];
        for(int i = 0; i < numBottles; i++) {
            bottle[i] = i;
        }
        shuffle(bottle, bottle.length);
        for(int test = 0; test < testRounds && testStrips > 0 && numBottles > P; test++) {
            boolean changeRange = false;
            if(P == 1 && (numBottles < (1 << testStrips) || testRounds - test == 1)) {
                numBottles = findOnePoison(bottle, numBottles, numBottles, testStrips);
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
            // TODO 提出版では削除
            if(bestWidth < 1) {
                bestWidth = numBottles / Math.min(numBottles, testStrips);
            }
            if(numBottles == 1 && bestWidth > numBottles / 2) {
                bestWidth = numBottles / 2;
                if(bestWidth < 1) bestWidth = 1;
            }
            if(false && VAL < VAL_MAX && bestWidth < 1) {
                if(widProb == null) {
                    initComb();
                    initWidProb();
                }
                n = estimateBestWidth(numBottles, testStrips, testRounds - test);
            } else {
                if(bestWidth < 1) {
                    // 重回帰分析結果(ただし線形結合よりいい立式を考えた方がいい)
                    double prob = BASE
                            + WINE_COEF * numBottles
                            + POIS_COEF * P
                            + ROUD_COEF * (testRounds - test)
                            + STRP_COEF * testStrips;
                    if (prob >= 90) prob = 90;
                    if (reg < 0) reg = prob;
                    bestWidth = searchWidByProb(numBottles, numPoison, (int) Math.round(prob));
                }
                n = (int) bestWidth;
            }
            int newN = (int) (n * Math.pow(WID_COEF, test - testRounds * TEST_COEF));
            if(newN * testStrips <= numBottles) n = newN;

            if(!changeRange) {
                rangeLen[0] = numBottles;
                rangePoison[0] = numPoison;
                rangeN = 1;
            }
            if(n <= 0) n = (numBottles + testStrips - 1) / testStrips;
            List<String> tests = new ArrayList<>();
            for (int i = 0; i < testStrips; ++i) {
                if(i * n >= numBottles) break;
                sb.setLength(0);
                sb.append(bottle[i * n]);
//                String t = "" + bottle[i * n];
                for (int j = 1; j < n && i * n + j < numBottles; ++j)
                    sb.append(",").append(bottle[i * n + j]);
//                    t += "," + bottle[i * n + j];
                tests.add(sb.toString());
            }
            /*
             rangeの統合については，上ですでに終わっているものとする TODO
             -> rangeの先頭が今回のループで考えられた範囲と保証
              */
            int[] testRes = PoisonTest.useTestStrips(tests.toArray(new String[0]), vis);
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
                } else {
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
        // 使うstripを変化させるとか，毒密度の偏りとか TODO
        if(strip == 0) return 1;
        int left = Math.max(1, Math.min(widProb[wine][80], wine / strip));
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
            final int NEXT_WINE = wine - s0 * wid;
            if(round == 1 || nextStrips == 0) {
                res += probOcc * s0 * wid;
                continue;
            }
            double maxStrategy = 0;
            int maxWid = 0;
            int left = Math.max(1, Math.min(widProb[wine][80], NEXT_WINE / nextStrips));
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
            res += probOcc * (maxStrategy + s0 * wid);
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
//        System.out.println("start init deathcont");
//        long time = System.currentTimeMillis();
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
//        System.out.printf("fin init deathcont: %dms\n", System.currentTimeMillis() - time);
        // --- cut end ---
    }

    void initEstDfsCont() {
        // --- cut start ---
//        System.out.println("start init estcont");
//        long time = System.currentTimeMillis();
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
//        System.out.printf("fin init estcont: %dms\n", System.currentTimeMillis() - time);
        // --- cut end ---
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
        for(int i = 0; i < strip; i++) {
            if(test[i] == null) used--;
        }
        if(used < strip) {
            String[] resized = new String[used];
            System.arraycopy(test, 0, resized, 0, used);
            test = resized;
        }
        int[] result = PoisonTest.useTestStrips(test, vis);
        int bit = 0;
        for(int i = 0; i < test.length; i++) {
            if(result[i] == 1) bit |= 1 << i;
        }
        List<Integer> bad = new ArrayList<>();
        // 下位stripビットのビットパターンがbitに等しい場合毒の可能性
        int mask = (1 << strip) - 1;
        for(int i = 1; i <= wine; i++) {
            if((i & mask) == bit) bad.add(bottle[i - 1]);
        }
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
//        init();
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
    PoisonedWineVis vis;
    public PoisonedWine(PoisonedWineVis vis) {
        this.vis = vis;
        PoisonTest.vis = vis;
        init();
    }
    // --- sub end ---
}