import org.junit.Test;

import java.util.Random;

public class PoisonedWineTest {
//    @Test
//    public void testTestFuncs() {
//        PoisonedWine pw = new PoisonedWine();
//        pw.testProb();
//        for(int wine = 100; wine <= 200; wine += 10) {
//            for(int poison = wine / 2 - 10; poison <= wine / 2; poison++) {
//                for(int wid = 5; wid <= 10; wid++) {
//                    for(int strip = 2; strip * wid <= wine; strip++) {
//                        for(int death = 0; death <= 0; death++) {
//                            double prob = pw.calcDeath(wine, poison, wid, strip, death);
//                            System.out.printf("wine=%d poi=%2d wid=%2d str=%2d death=%2d prob=%e\n",
//                                    wine, poison, wid, strip, death, prob);
//                            prob = (Math.pow(pw.probNoPoison(wine, poison, wid), strip + 1));
//                            if(wine - wid * strip < poison) prob = 0;
//                            System.out.printf("                                           %e\n", prob);
//                        }
//                    }
//                }
//            }
//        }
//        double prob = 0;
//        for(int i = 0; i <= 3; i++) {
//            prob += pw.calcDeath(10000, 200, 10, 3, i);
//        }
//        System.out.println(prob);
//        System.out.println(pw.testProbDeath());
//        int wine = 10;
//        int poison = 2;
//        int strip = 2;
//        int wid = 3;
//        for(int death = 0; death <= 2; death++) {
//            for (int scale = 1; scale <= 10; scale++) {
//                double prob = pw.calcDeath(
//                        wine * scale,
//                        poison * scale,
//                        wid * scale,
//                        strip,
//                        death);
//                System.out.printf("death=%d scale=%d prob=%f\n", death, scale, prob);
//            }
//        }
//    }

//    @Test
//    public void testEstDP() {
//        PoisonedWine pw = new PoisonedWine();
//        pw.W = 137;
//        pw.curW = pw.W;
//        pw.P = 1;
//        pw.S = 9;
//        pw.R = 4;
//        int wid = pw.estimateBestWidth(pw.W, pw.S, pw.R);
//        System.out.println("wid: " + wid);
//    }

    int[] oneCase = {
            10, 18, 31, 32, 55, 80,
            111, 166, 198, 210, 220,
            228, 267, 272, 295, 301,
            341, 437, 461,
    };

    int[] tleCase = {
            381, 474, 330, 305, 359, 489, 365, 459
    };

    int[] zeroCase = {
        58, 223, 268,
    };

    int[] lightWeight = {
            104, 265, 590
    };

    int[] lowPoison = {
            4, 19, 47, 61, 126, 159, 223,
            289, 323, 446, 463,
            12, 142, 167,
            183, 264, 287, 432,
            428,
            448, 451, 84, 154, 209,
    };

    int[] tmp = {
            79
    };

    @Test
    public void testSeed() {
        double sum = 0;
        int[] seeds = lowPoison;
        int testcase = seeds.length;
        for(int i = 0; i < testcase; i++) {
            PoisonedWine.randSeed = 2;
            long seed = seeds[i % seeds.length];
            PoisonedWineVis f = new PoisonedWineVis("" + seed);
            sum += f.testScore;
            System.out.println("seed: " + seed +
                    " test: " + i + " score: " + f.testScore);
            System.out.println("now: " + (sum / (i + 1)));
        }
        System.out.println("Result: " + (sum / testcase));
    }


//    @Test
//    public void testCalcDeath() {
//        /**/
//        Random rand = new Random(1);
//        int wine = 20;
//        for(int test = 0; test < 30; test++) {
//            PoisonedWine pw = new PoisonedWine();
//            int poison = rand.nextInt(7) + 1;
//            int strip = rand.nextInt(5) + 1;
//            int wid = rand.nextInt(wine / strip - 1) + 1;
//            double[] brute = pw.testProbDeath(wine, wid, strip, poison);
//            pw.P = poison;
//            pw.S = strip;
//            double[][] calcA = pw.calcDeath(wine, wid);
//            System.out.printf("poi:%d str:%d wid:%d\n", poison, strip, wid);
//            try {
//                double[] calc = calcA[strip];
//                for (int death = 0; death <= strip; death++) {
//                    System.out.printf("b:%f c:%f reldiff:%f\n",
//                            brute[death], calc[death],
//                            (brute[death] - calc[death]) / Math.max(brute[death], calc[death]));
//                }
//            } catch (ArrayIndexOutOfBoundsException e) {
//                System.err.println("");
//            }
//        }
//        /**/
//        /*/
//        PoisonedWine pw = new PoisonedWine();
//        int wine = 8;
//        int poison = 1;
//        int strip = 2;
//        int wid = 3;
//        double[] brute = pw.testProbDeath(wine, wid, strip, poison);
//        for(int i = 0; i < brute.length; i++) {
//            System.out.printf("%d: %f\n", i, brute[i]);
//        }
//        pw.P = poison;
//        pw.S = strip;
//        double[][] calc = pw.calcDeath(wine, wid);
//        for(int i = 0; i <= strip; i++) {
//            System.out.printf("%d: %f\n", i, calc[strip][i]);
//        }
//        /**/
//    }
}
