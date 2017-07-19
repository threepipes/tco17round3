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
    static double LOG_COEF      = 0.4 + 0.002 * 0;  // 0.40-0.60:0.002*100
    static double TEST_SUB      = 0.0 + 0.05  * 94; // 0-5:0.05*100
    static double ROUND_COEF    = 1.0 + 0.01  * 24; // 1.0-2.0:0.01*100
    static double WID_COEF      = 0.95+ 0.002 * 51; // 0.95-1.15:0.002*100
    static double TEST_COEF     = 0.0 + 0.002 * 79; // 0.0-0.2:0.002*100
    static double Y_OFFSET      =-0.1 + 0.002 * 73; // -0.1-0.1:0.002*100
    static double Y_COEF        = 1.8 + 0.004 * 87; // 1.8-2.2:0.004*100
    static double X_COEF        = 0.9 + 0.002 * 59; // 0.9-1.1:0.002*100
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
        int[] bottle = new int[numBottles];
        for(int i = 0; i < numBottles; i++) {
            bottle[i] = i;
        }
        shuffle(bottle, bottle.length);
        for(int test = 0; test < testRounds && testStrips > 0 && numBottles > P; test++) {
            boolean changeRange = false;
            if(P == 1 && (numBottles < (1 << testStrips) || testRounds - test == 1)) {
                // 1発でワインを求められるケース
                numBottles = findOnePoison(bottle, numBottles, numBottles, testStrips);
                testStrips = findOneRest;
                continue;
            }
            curW = numBottles;
            int round = testRounds - test;
            double bestWidth = Math.min(
                    Y_OFFSET + Y_COEF * numBottles /
                            ((numPoison * X_COEF - LOG_COEF * Math.log10(round) * (testStrips - TEST_SUB))
                                    * (round * ROUND_COEF + ROUND_OFFSET)),
                    numBottles / Math.min(numBottles, testStrips)
            );// * Math.pow(WID_COEF, test);
            if(bestWidth < 1) {
                bestWidth = numBottles / Math.min(numBottles, testStrips);
            }
            if(numBottles == 1 && bestWidth > numBottles / 2) {
                bestWidth = numBottles / 2;
                if(bestWidth < 1) bestWidth = 1;
            }
            int n = (int) bestWidth;
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
            if(rangeN > 1 && rangeLen[0] <= rangeLen[1] * 2) {
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

    public PoisonedWine() {}

    // --- cut start ---
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
    }
    // --- sub end ---
}