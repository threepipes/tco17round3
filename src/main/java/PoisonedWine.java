import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

// --- cut start ---

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

// --- cut end ---

// -------8<------- start of solution submitted to the website -----8<-------
public class PoisonedWine {
    static int randSeed = 0;
    Random rand = new Random(randSeed);
    void shuffle(int[] a) {
        for(int i = 0; i < a.length; i++) {
            int i1 = i;
            int i2 = rand.nextInt(a.length);
            int tmp = a[i1];
            a[i1] = a[i2];
            a[i2] = tmp;
        }
    }

    final double BASE = 37.54114071;
    final double WINE_COEF = -0.000656549;
    final double POIS_COEF = -0.005272914;
    final double ROUD_COEF = 7.676318797;
    final double STRP_COEF = -0.306667327;

    int W, P, S, R;
    int curW;
    double est = -1;
    public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
        W = numBottles;
        P = numPoison;
        S = testStrips;
        R = testRounds;
        // --- cut start ---
        System.out.println("val: " + ((long)W * S * R * R * P / 1000));
        // --- cut end ---
        double reg = -1; // 重回帰分析結果(初手のみ)
        initWidProb();
        int[] bottle = new int[numBottles];
        for(int i = 0; i < numBottles; i++) {
            bottle[i] = i;
        }
//        long timesec = System.currentTimeMillis();
        shuffle(bottle);
        for(int test = 0; test < testRounds && testStrips > 0 && numBottles > P; test++) {
            if(P == 1 && (numBottles < (1 << testStrips) || testRounds - test == 1)) {
                // 1発でワインを求められるケース
                // 毒の偏りを利用して，もう少し他のケースでもfind oneできる場合を探す TODO
                // --- cut start ---
                System.out.println("find one!");
                // --- cut end ---
                numBottles = findOnePoison(bottle, numBottles, numBottles, testStrips);
                continue;
            }
            final long VAL = (long)numBottles * testStrips * (testRounds - test)
                    * (testRounds - test) * P / 1000;
            curW = numBottles;
            int n;
            if((VAL < 5000 || P < 10) && !(numBottles > 1000 && testRounds - test >= 3 && P >= 18 && testStrips >= 3)) {
                // --- cut start ---
                long time = -1;
                if(estCache.isEmpty()) time = System.currentTimeMillis();
                if(numPoison >= 15 && testRounds - test > 1 && numBottles > 1000)
                    System.out.printf("begin w:%d t:%d p:%d s:%d\n",
                            numBottles, testRounds - test, numPoison, testStrips);
                // --- cut end ---
                n = estimateBestWidth(numBottles, testStrips, testRounds - test);
                // --- cut start ---
                if(time >= 0) time = System.currentTimeMillis() - time;
                System.out.printf("wine:%4d poi:%3d str:%2d n:%d round:%d (prob:%f) time:%d\n",
                        numBottles, numPoison, testStrips, n,
                        testRounds - test, probNoPoison(numBottles, numPoison, n), time);
                // --- cut end ---
            } else {
                // 重回帰分析結果(ただし線形結合よりいい立式を考えた方がいい)
                double prob = BASE
                        + WINE_COEF * numBottles
                        + POIS_COEF * P
                        + ROUD_COEF * (testRounds - test)
                        + STRP_COEF * testStrips;
                if(prob >= 90) prob = 90;
                if(reg < 0) reg = prob;
                n = searchWidByProb(numBottles, numPoison, (int)Math.round(prob));
            }
            if(n == -1) n = (numBottles + testStrips - 1) / testStrips;
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
            for (int i = 0; i * n < numBottles; ++i)
                if (i >= testRes.length || testRes[i] == 1) {
                    if(i < testRes.length) {
                        for (int j = 0; j < n && i * n + j < numBottles; ++j)
                            warning.add(bottle[i * n + j]);
                        used++;
                    } else for (int j = 0; j < n && i * n + j < numBottles; ++j)
                        bottle[next++] = bottle[i * n + j];
                }
            // bottleの先頭 n * used は毒密度が高いので後ろに回す
            // 区間ごとに毒密度管理するとか -> たぶん地獄
            for(int w: warning) bottle[next++] = w;
            testStrips -= used;
            numBottles = next;
        }
        int[] ret = new int[numBottles];
        for (int i = 0; i < numBottles; ++i)
            ret[i] = bottle[i];
        int saved = W - numBottles;

        // --- cut start ---
        if(est >= 0)
            System.out.printf("est:%.20f real:%d diff:%.20f diff/acc:%.20f \n",
                est, saved, est - saved, (est - saved) / (W - P));
        System.out.printf("end seed:%d reg:%f cacheEst:%d cacheDeath:%d\n",
                PoisonedWineVis.seedL, reg, estCache.size(), deathCache.size());
        // --- cut end ---
//        System.err.println("time: " + (System.currentTimeMillis() - timesec));
        return ret;
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
        int left = Math.max(1, widProb[wine][80]);
        int right = Math.max(widProb[wine][30], left);
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
            if(round >= 2 && wine > 1000 && P > 8)
                System.out.printf("wid1:%d exp1:%f wid2:%d exp2:%f\n",
                        wid1, exp1, wid2, exp2);
            expMax = Math.max(exp1, exp2);
            // --- cut end ---
        }
        if(est < 0) est = expMax;
        return wid;
    }

    HashMap<Long, Double> estCache = new HashMap<>();
    double estDfs(int wine, int strip, int round, int wid) {
        if(round == 0 || wine == P || strip == 0) return W - wine;
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
                res += probOcc * (W - NEXT_WINE);
                continue;
            }
            double maxStrategy = 0;
            int left = Math.max(1, widProb[wine][80]);
            int right = Math.max(Math.min(widProb[wine][30], wine / wid), left);
            while(left <= right) {
                int wid1 = (left * 2 + right) / 3;
                int wid2 = (left + right * 2) / 3;
                double exp1 = estDfs(NEXT_WINE, nextStrips, round - 1, wid1);
                double exp2 = estDfs(NEXT_WINE, nextStrips, round - 1, wid2);
                if(exp1 < exp2) {
                    left = wid1 + 1;
                    maxStrategy = exp2;
                } else {
                    right = wid2 - 1;
                    maxStrategy = exp1;
                }
            }
            res += probOcc * maxStrategy;
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
        int[] result = PoisonTest.useTestStrips(test);
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

    void init() {
        initComb();
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
                for(int n = 5; n < wine / poison && pre > 0.5; n++) {
                    pre = probNoPoison(wine, poison, n);
                    System.out.println("n=" + n + " -> " + String.format("%.20f", pre));
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

    public PoisonedWine(PoisonedWineVis vis) {
        PoisonTest.vis = vis;
        init();
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
}