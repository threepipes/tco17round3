import java.util.Random;

public class CheckFuncValues {
    public static void main(String[] args) {
        new CheckFuncValues().checkEstWid();
    }

    void checkCalcDeathCont() {
        Random rand = new Random(1);
        int wine = 200;
        for(int test = 0; test < 30; test++) {
            PoisonedWine pw = new PoisonedWine();
            int poison = rand.nextInt(20) + 1;
//            int poison = 50;
            int strip = rand.nextInt(5) + 1;
            int wid = rand.nextInt(wine / strip - 1) + 1;
            pw.P = poison;
            pw.S = strip;
            double[][] calcA = pw.calcDeath(wine, wid);
            double[][] calcC = pw.calcDeathCont((double) wid / wine);
            System.out.printf("poi:%d str:%d wid:%d\n", poison, strip, wid);
            try {
                for(int s = 1; s <= strip; s++) {
                    for (int death = 0; death <= strip; death++) {
                        System.out.printf("strip:%d death:%d c:%f a:%f diff:%f\n",
                                s, death,
                                calcC[s][death], calcA[s][death],
                                calcC[s][death] - calcA[s][death]);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("");
            }
        }
    }

    void checkEstWid() {
        Random rand = new Random(10);
        for(int test = 0; test < 1; test++) {
            PoisonedWine pw = new PoisonedWine();
            int wine = 200;
            int strip = rand.nextInt(5) + 1;
            int poison = rand.nextInt(5) + 1;
            int round = 2;
            pw.S = strip;
            pw.P = poison;
            pw.W = wine;
            pw.initWidProb();
            System.out.println("test " + test + " begin");
            System.out.printf("s:%d p:%d\n",
                    strip, poison);
            double estMaxC = 0;
            double estMaxD = 0;
            int maxWidC = 0;
            int maxWidD = 0;
            for(int wid = 1; wid <= wine - poison; wid++) {
//                int wid = rand.nextInt(wine / strip - 1) + 1;

                double estDist = pw.estDfs(wine, strip, round, wid);
                double estCont = pw.estDfsCont(strip, round, (double) wid / wine) * wine;
                System.out.printf("w:%d D:%f C:%f\n", wid, estDist, estCont);
                if(estMaxC < estCont) {
                    estMaxC = estCont;
                    maxWidC = wid;
                }
                if(estMaxD < estDist) {
                    estMaxD = estDist;
                    maxWidD = wid;
                }
//            double res = pw.estimateBestWidthCont(strip, round);
//            int wid = pw.estimateBestWidth(wine, strip, round);
//            System.out.printf("s:%d p:%d contwid:%f cw*wine:%f wid:%d\n",
//                    strip, poison, res, res * wine, wid);
            }
            System.out.printf("maxD:%f mwD:%d maxC:%f mwC:%d\n",
                    estMaxD, maxWidD, estMaxC, maxWidC);
        }
    }
}
