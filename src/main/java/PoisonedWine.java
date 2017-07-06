import java.io.*;
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

    public void testFuncs() {
        System.out.println("testFunc");
        for(int wine = 100; wine < 1000; wine += 100) {
            System.out.println("Wine: " + wine);
            for(int poison = 1; poison < wine / 50; poison++) {
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