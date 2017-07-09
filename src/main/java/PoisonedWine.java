import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

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

    static double searchProb = 0.3;
    int W, P, S, R;
    int curW;
    double est = -1;
    public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
        W = numBottles;
        P = numPoison;
        S = testStrips;
        R = testRounds;
        System.out.println("val: " + ((long)W * S * R * R * P / 1000));
        initWidProb();
        int[] bottle = new int[numBottles];
        for(int i = 0; i < numBottles; i++) {
            bottle[i] = i;
        }
//        shuffle(bottle);
        for(int test = 0; test < testRounds && testStrips > 0; test++) {
            final long VAL = (long)numBottles * testStrips * (testRounds - test)
                    * (testRounds - test) * P / 1000;
            curW = numBottles;
            int n;
            if(VAL < 9000 || P < 10) {
                n = estimateBestWidth(numBottles, testStrips, testRounds - test);
                System.out.printf("wine:%4d poi:%3d str:%2d n:%d round:%d\t(prob:%f)\n",
                        numBottles, numPoison, testStrips, n, testRounds - test, probNoPoison(numBottles, numPoison, n));
            } else {
                // probはround, stripによって変更したい
                n = searchWidByProb(numBottles, numPoison, 40);
            }
//            int n = searchWidByProb(numBottles, numPoison, searchProb);
            if(n == -1) n = (numBottles + testStrips - 1) / testStrips;
//        System.err.println("Batch size " + n);
//            String[] tests = new String[testStrips];
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
                    // poison detected - throw out all bottles in this batch
                    if(i < testRes.length) {
                        for (int j = 0; j < n && i * n + j < numBottles; ++j)
                            warning.add(bottle[i * n + j]);
                        used++;
                    } else for (int j = 0; j < n && i * n + j < numBottles; ++j)
                        bottle[next++] = bottle[i * n + j];
                } else {
//                System.err.println("Keeping batch " + i);
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
        if(est >= 0)
            System.out.printf("est:%.20f real:%d diff:%.20f diff/acc:%.20f \n",
                est, saved, est - saved, (est - saved) / (W - P));
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
//        double ans = 1;
//        for(int i = 0; i < n; i++) {
//            ans *= (wine - poison - i) / (double)(wine - i);
//        }
//        return ans;
        if(n > wine - poison) return poison > 0 ? 1 : 0;
        return perm[wine - poison]
                .multiply(perm[wine - n], MathContext.DECIMAL64)
                .divide(perm[wine - poison - n], MathContext.DECIMAL64)
                .divide(perm[wine], MathContext.DECIMAL64).doubleValue();
    }

    int getWidInterval(int wine, int strip) {
        final int WID_MAX = Math.min(wine - P, wine / strip);
        if(WID_MAX <= 50) return 1;
        return WID_MAX / 50;
    }

    int estimateBestWidth(int wine, int strip, int round) {
        double max = 0;
        int argmax = 1;
        // どういった場合にどういった幅がベストか確認し，ある程度幅を絞る必要がある TODO
        if(strip == 0) return 1;
        int widMin = widProb[wine][80];
        int widMax = round == 1 ? widProb[wine][50] : widProb[wine][30];
        final int interval = Math.max(1, (widMax - widMin) / 20);
        System.out.printf("wid range: %d - %d (%d) int:%d\n",
                widMin, widMax, widMax - widMin, interval);
        for(int prob = widMin; prob <= wine - P && prob <= widMax; prob += interval) {
            int wid = prob;
//            int wid = prob == 8 ?
//                    wine / strip :
//                    widProb[wine][prob * 10];
            if(wid <= 0) continue;
            double exp = estDfs(wine, Math.min(strip, wine / wid), round, wid);
            if(exp > max) {
                max = exp;
                argmax = wid;
            }
            if(interval > 1) System.out.println("fin wid: " + wid);
        }
        if(est < 0) est = max;
        System.out.println(max);
        return argmax;
    }

    HashMap<Long, Double> estCache = new HashMap<>();
    double estDfs(int wine, int strip, int round, int wid) {
        if(round == 0 || wine == P || strip == 0) return W - wine;
        long id = (long) wine * W * S * R + (long) strip * W * R
                + round * W + wid;
        if(estCache.containsKey(id)) return estCache.get(id);
        double res = 0;
        final int useStrips = Math.min(strip, wine / wid);
        final int saveStrips = strip - useStrips;
        for(int death = 0; death <= useStrips && death <= P; death++) {
            double probOcc = calcDeath(wine, wid)[useStrips][death];
            double maxStrategy = 0;
            final int s0 = useStrips - death;
            final int nextStrips = s0 + saveStrips;
            final int NEXT_WINE = wine - s0 * wid;
            if(round == 1 || nextStrips == 0) {
                res += probOcc * (W - NEXT_WINE);
                continue;
            }
            final int widMin = widProb[wine][80];
            int widMax = round == 2 ? widProb[wine][50] : widProb[wine][30];
            widMax = Math.min(widMax, wine / wid);
            if(widMax < widMin) widMax = widMin;
            final int interval = Math.max(1, (widMax - widMin) / 20);
            for(int prob = widMin; prob <= widMax; prob += interval) {
                int nextWid = prob;
//                int nextWid = prob == 8 ?
//                        wine / strip :
//                        widProb[wine][prob * 10];
                if(nextWid <= 0) continue;
                maxStrategy = Math.max(maxStrategy,
                        estDfs(NEXT_WINE, nextStrips, round - 1, nextWid));
            }
            res += probOcc * maxStrategy;
        }
        estCache.put(id, res);
        return res;
    }

    // 使い方: calcDeath(wine, wid)[strip][death] -> 生起確率
    HashMap<Integer, double[][]> deathCache = new HashMap<>();
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
            for(int j = 0; j <= P; j++) {
                for(int k = 0; k <= deathMax; k++) {
                    if(dp[i][j][k] == 0) continue;
                    int wineRem = wine - i * wid;
                    dp[i + 1][j][k] += prob(wineRem, j, wid, 0) * dp[i][j][k];
                    for(int l = 1; l <= Math.min(j, wid); l++) {
                        dp[i + 1][j - l][k + 1] += prob(wineRem, j, wid, l) * dp[i][j][k];
                    }
                }
            }

//            for(int j = 0; j <= P; j++) {
//                for(int k = 0; k <= deathMax; k++) {
//                    System.out.printf("%2.5f ", dp[i + 1][j][k]);
//                }
//                System.out.println();
//            }
//            System.out.println();
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
     * stripにwid本のワインを使ったとき，毒がhit本引っかかる確率
     * @param wine ワイン
     * @param poison 毒
     * @param wid stripに使うワインの数
     * @param hit stripに引っかかる毒の数
     * @return hit本の毒が引っかかる確率
     */
    double prob(int wine, int poison, int wid, int hit) {
        if(comb[wid][hit] == null || comb[wine-wid][poison-hit] == null || comb[wine][poison] == null)
            return 0;
        return comb[wid][hit].multiply(comb[wine-wid][poison-hit], MathContext.DECIMAL64)
                .divide(comb[wine][poison], MathContext.DECIMAL64).doubleValue();
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
        PoisonTest.vis = null;
        init();
    }
    // ---8<------- end of solution submitted to the website -------8<-------

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
}