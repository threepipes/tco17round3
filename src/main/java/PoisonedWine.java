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
    public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
        // do one round, split all bottles in batches and run one test on each batch
        int n = numBottles / testStrips;
        if (numBottles % testStrips > 0)
            n++;
//        System.err.println("Batch size " + n);
        String[] tests = new String[testStrips];
        for (int i = 0; i < testStrips; ++i) {
            tests[i] = "" + (i * n);
            for (int j = 1; j < n && i * n + j < numBottles; ++j)
                tests[i] += "," + (i * n + j);
        }
        int[] testRes = PoisonTest.useTestStrips(tests);
        ArrayList<Integer> bad =  new ArrayList<Integer>();
        for (int i = 0; i < testRes.length; ++i)
            if (testRes[i] == 1) {
                // poison detected - throw out all bottles in this batch
                for (int j = 0; j < n && i * n + j < numBottles; ++j)
                    bad.add(i * n + j);
            } else {
//                System.err.println("Keeping batch " + i);
            }

        int[] ret = new int[bad.size()];
        for (int i = 0; i < bad.size(); ++i)
            ret[i] = bad.get(i).intValue();
        return ret;
    }

    double probNoPoison(int wine, int poison, int n) {
        // n本選んで毒が入っていない確率
        double ans = 1;
        for(int i = 0; i < n; i++) {
            ans *= (wine - poison - i) / (double)(wine - i);
        }
        return ans;
    }
    // ---8<------- end of solution submitted to the website -------8<-------

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

    double calcDeath(int wine, int poison, int wid, int strip, int death) {
        /*
         ワイン数，毒数，strip，1stripあたりのwine数のとき
         death本のstripが破壊される確率
         strip -> maxStripとして，
         prob[strip][death]の配列で返すように修正可能
         O(strip ^ 2 * poison ^ 2)
          */
        if(comb == null) initComb();
        if(death > poison) return 0;
        int deathMax = Math.min(poison, strip);
        double[][][] dp = new double[strip + 1][poison + 1][deathMax + 1];
        dp[0][poison][0] = 1;
        for(int i = 0; i < strip; i++) {
            for(int j = 0; j <= poison; j++) {
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
        double ans = 0;
        for(int i = 0; i <= poison; i++) {
            ans += dp[strip][i][death];
        }
        return ans;
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
        // パラメータが大きいとcombがinfになってしまい，NaNを返す: TODO
//        return comb(wid, hit)
//                * comb(wine - wid, poison - hit)
//                / comb(wine, poison);
        return comb[wid][hit].multiply(comb[wine-wid][poison-hit], MathContext.DECIMAL64)
                .divide(comb[wine][poison], MathContext.DECIMAL64).doubleValue();
    }

//    double comb(int n, int r) {
//        return comb[n][r];
//    }

    final int WINE_MAX = 10000;
    final int POISONE_MAX = 200;
//    double[][] comb;
//    void initComb() {
//        comb = new double[WINE_MAX + 1][POISONE_MAX + 1];
//        for(int i = 0; i <= WINE_MAX; i++){
//            for(int j = 0; j <= Math.min(i, POISONE_MAX); j++){
//                if(j == 0 || j == i){
//                    comb[i][j] = 1;
//                }else{
//                    comb[i][j] = comb[i - 1][j - 1] + comb[i - 1][j];
//                }
//            }
//        }
//    }

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

    public PoisonedWine(PoisonedWineVis vis) {
        PoisonTest.vis = vis;
    }

    public PoisonedWine() {
        PoisonTest.vis = null;
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