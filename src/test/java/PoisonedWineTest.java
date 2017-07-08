import org.junit.Test;

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

    @Test
    public void testSeed() {
        PoisonedWineVis f = new PoisonedWineVis("387");
    }
}
