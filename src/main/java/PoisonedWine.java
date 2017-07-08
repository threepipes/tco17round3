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
    static double searchProb = 0.3;
    int W, P, S, R;
    public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
        W = numBottles;
        P = numPoison;
        S = testStrips;
        R = testRounds;
        int[] bottle = new int[numBottles];
        for(int i = 0; i < numBottles; i++) {
            bottle[i] = i;
        }
        for(int test = 0; test < testRounds && testStrips > 0; test++) {
            // do one round, split all bottles in batches and run one test on each batch
//            int n = numBottles / testStrips;
//            if (numBottles % testStrips > 0)
//                n++;
            int n = searchWidByProb(numBottles, numPoison, searchProb);
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
            for (int i = 0; i * n < numBottles; ++i)
                if (i >= testRes.length || testRes[i] == 1) {
                    // poison detected - throw out all bottles in this batch
                    used++;
                    for (int j = 0; j < n && i * n + j < numBottles; ++j)
                        bottle[next++] = bottle[i * n + j];
                } else {
//                System.err.println("Keeping batch " + i);
                }
            testStrips -= used;
            numBottles = next;
        }
        int[] ret = new int[numBottles];
        for (int i = 0; i < numBottles; ++i)
            ret[i] = bottle[i];
        return ret;
    }

    int searchWidByProb(int wine, int poison, double prob) {
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
        double ans = 1;
        for(int i = 0; i < n; i++) {
            ans *= (wine - poison - i) / (double)(wine - i);
        }
        return ans;
    }

    int estimateBestWidth(int wine, int strip, int round) {
        double max = 0;
        int argmax = 1;
        // 幅1刻みは愚直なので，wine数等に応じて変化させる TODO
        for(int wid = 1; wid <= wine - P; wid++) {
            double exp = estDfs(wine, Math.min(strip, wine / wid), round, wid);
            if(exp > max) {
                max = exp;
                argmax = wid;
            }
        }
        return argmax;
    }

    HashMap<Long, Double> estCache = new HashMap<>();
    double estDfs(int wine, int strip, int round, int wid) {
        if(round == 0 || wine == P) return W - wine;
        long id = (long) wine * W * S * R + (long) strip * W * R
                + round * W + wid;
        if(estCache.containsKey(id)) return estCache.get(id);
        double res = 0;
        for(int death = 0; death <= strip; death++) {
            double probOcc = calcDeath(wine, wid)[strip][death];
            double maxStrategy = 0;
            final int s0 = strip - death;
            // 幅1刻みは愚直なので，wine数等に応じて変化させる TODO
            for(int nextWid = 1; nextWid <= wine - P; nextWid++) {
                maxStrategy = Math.max(maxStrategy,
                        estDfs(wine - s0, s0, round - 1, nextWid));
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
        if(comb == null) initComb();
        int deathMax = Math.min(P, S);
        double[][][] dp = new double[S + 1][P + 1][deathMax + 1];
        dp[0][P][0] = 1;
        for(int i = 0; i < S; i++) {
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
        }
        double[][] res = new double[S + 1][S + 1];
        for(int s = 1; s <= S; s++) {
            for(int d = 0; d <= s; d++) {
                for(int p = 0; p <= P; p++) {
                    res[s][d] += dp[s][p][d];
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
        return comb[wid][hit].multiply(comb[wine-wid][poison-hit], MathContext.DECIMAL64)
                .divide(comb[wine][poison], MathContext.DECIMAL64).doubleValue();
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

    public PoisonedWine() {
        PoisonTest.vis = null;
        initComb();
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

    double testProbDeath() {
        int m = 20;
        int p = 4;
        int eat = 3;
        int hit = 3;
        int a1 = (1 << eat) - 1;
        int a2 = a1 << eat;
        int a3 = a1 << eat * 2;
        int all = 0;
        int baai = 0;
        int notA1 = 0;
        int notA12 = 0;
        for(int i = 0; i < (1 << m); i++) {
            if(Integer.bitCount(i) != p) continue;
            all++;
            int b1 = (a1 & i) > 0 ? 1 : 0;
            int b2 = (a2 & i) > 0 ? 1 : 0;
            int b3 = (a3 & i) > 0 ? 1 : 0;
//            if(b1 == 0) notA1++;
//            if(b1 + b2 == hit) baai++;
            if(b1 + b2 + b3 == hit) baai++;
        }
        double res = (double) baai / all;
//        System.out.println("not a12: " + notA12);
//        System.out.println("not a1: " + notA1);
        System.out.println("baai: " + baai);
        System.out.println("all: " + all);
        System.out.println(res);
        return res;
    }

    public PoisonedWine(PoisonedWineVis vis) {
        PoisonTest.vis = vis;
        initComb();
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