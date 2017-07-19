import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ParamSearcher {
    static double TEMPER = 0.1;
    static public void main(String[] args) throws IOException {
        State state = new State(new int[]{4, 99, 17, 53, 47, 30, 71, 64});
//        State state = new State(new int[]{30, 60, 10, 50, 50});
        System.out.println("Start evaluate v1.5.");
        long time = System.currentTimeMillis();
        double score = state.evaluate();
        time = System.currentTimeMillis() - time;
        System.out.println("Initial score: " + score);
        State best = state;
        double bestScore = score;
        final long HOUR_10 = 10 * 3600 * 1000;
        final int MAX_ITER = (int)(HOUR_10 / time);
        System.out.println("Iter Num: " + MAX_ITER);
        Random rand = new Random(0);
        PrintWriter pw = new PrintWriter(
                new BufferedWriter(
                        new FileWriter(System.getenv("DATA_PATH")
                                + "statistics/eval_log.txt")));
        pw.println(state.csv());
        for(int i = 0; i < MAX_ITER; i++) {
            int degree = (MAX_ITER - i) * (State.PARAM_MAX / 2) / MAX_ITER + 10;
            State next = state.generateNext(degree);
            double nextScore = next.evaluate();
            pw.println(next.csv());
            if((i + 1) % 10 == 0) pw.flush();
            String output = String.format("Iter %d/%d: score=%f ", i, MAX_ITER, nextScore);
            if(nextScore > bestScore) {
                best = next;
                bestScore = nextScore;
                output += "Update! ";
            }
            double temperature = temper((double)i / MAX_ITER);
            double prob = prob(score, nextScore, temperature);
            output += String.format("T:%f P:%f ", temperature, prob);
            if(rand.nextDouble() <= prob) {
                state = next;
                score = nextScore;
                output += "swap ";
            }
            output += String.format("Now: %s, Best: %s", state, best);
            System.out.println(output);
        }
        pw.close();
        System.out.printf("Best: %s\n", best);
    }

    static double temper(double r) {
        return Math.pow(TEMPER, r);
    }

    static double prob(double curScore, double nextScore, double temper) {
        if(curScore <= nextScore) return 1;
        else return Math.pow(Math.E, (nextScore - curScore) * 100 / temper);
    }
}

class State implements Comparable<State> {
    static Random rand = new Random(0);
    static HashMap<Long, State> history = new HashMap<>();
    double score;
    int[] params;
    long id;
    public static final int PARAM_MAX = 101;
    public static final int PARAM_SIZE = 8;
    State(int[] params) {
        this.params = params;
        for(int p: params) {
            id *= PARAM_MAX;
            id += p;
        }
    }

    State generateNext(int degree) {
        int i = rand.nextInt(PARAM_SIZE);
        int[] next = params.clone();
        next[i] += (rand.nextInt(degree) + 1) * (rand.nextBoolean() ? -1 : 1);
        next[i] = (next[i] + PARAM_MAX) % PARAM_MAX;
        return new State(next);
    }

    double evaluate() {
        if(history.containsKey(id)) return history.get(id).score;
        PoisonedWine.LOG_COEF = 0.4 + 0.002 * params[0];
        PoisonedWine.TEST_SUB = 0.0 + 0.05 * params[1];
        PoisonedWine.ROUND_COEF = 1.0 + 0.01 * params[2];
        PoisonedWine.WID_COEF = 0.95 + 0.002 * params[3];
        PoisonedWine.TEST_COEF = 0.0 + 0.002 * params[4];
        PoisonedWine.Y_OFFSET = -0.1 + 0.002 * params[5];
        PoisonedWine.Y_COEF = 1.8 + 0.004 * params[6];
        PoisonedWine.X_COEF = 0.9 + 0.002 * params[7];
        history.put(id, this);
        return score = PoisonedWineVis.evaluate();
    }

    @Override
    public int compareTo(State o) {
        if(id == o.id) return 0;
        if(score != o.score) return Double.compare(o.score, score);
        return Long.compare(id, o.id);
    }

    public String csv() {
        String s = "";
        for(int i = 0; i < params.length; i++) s += params[i] + ",";
        s += score;
        return s;
    }

    @Override
    public String toString() {
        return Arrays.toString(params) + ":" + score;
    }
}
