import org.junit.Test;

import java.util.Random;

public class PoisonedWineTest {

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
}
