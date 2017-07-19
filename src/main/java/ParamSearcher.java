import java.io.*;
import java.util.*;

public class ParamSearcher {
    public static final String logPath = System.getenv("DATA_PATH") + "statistics/eval_log.csv";
    static public void main(String[] args) throws IOException {
        new SimulatedAnnealing().sa();
    }

    static List<State> getBests(int n) throws IOException {
        List<State> list = load(logPath);
        Collections.sort(list);
        return list.subList(0, n);
    }

    static List<State> load(String filename) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        List<State> list = new ArrayList<>();
        for(String row = in.readLine(); row != null; row = in.readLine()) {
            list.add(new State(row));
        }
        return list;
    }
}

class BruteForceCheck {
    static void brute(List<State> candidate, int checkParam) {
        State allBest = null;
        for(State state: candidate) {
            state.evaluate();
            State best = state.copy();
            System.out.println("Start: " + best);
            for (int i = 0; i <= 100; i++) {
                state.params[checkParam] = i;
                double score = state.evaluate();
                System.out.println(state);
                if (score > best.score) {
                    best = state.copy();
                }
            }
            System.out.println(best);
            if(allBest == null || allBest.score < best.score) {
                allBest = best;
            }
        }
        System.out.println("The best: " + allBest);
    }
}

class SimulatedAnnealing {
    State initialState = new State(new int[]{
            50, 68, 27, 86, 64,
            50, 28, 65, 24,
    });
    final double TEMPER = 0.01;

    void sa() throws IOException {
        State state = initialState;
        System.out.println("Start evaluate v2.1.");
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
                new BufferedWriter(new FileWriter(ParamSearcher.logPath)));
        pw.println(state.csv());
        for(int i = 0; i < MAX_ITER; i++) {
            int degree = (MAX_ITER - i) * (State.PARAM_MAX / 3) / MAX_ITER + 10;
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

    double temper(double r) {
        return Math.pow(TEMPER, r);
    }

    double prob(double curScore, double nextScore, double temper) {
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
    public static final int PARAM_SIZE = 9;
    State(int[] params) {
        this.params = params;
        for(int p: params) {
            id *= PARAM_MAX;
            id += p;
        }
    }

    State(String csvRow) {
        String[] paramList = csvRow.split(",");
        params = new int[PARAM_SIZE];
        for(int i = 0; i < PARAM_SIZE; i++) {
            params[i] = Integer.parseInt(paramList[i]);
        }
        score = Double.parseDouble(paramList[PARAM_SIZE]);
    }

    State copy() {
        State newState = new State(params.clone());
        newState.score = score;
        newState.id = id;
        return newState;
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
        PoisonedWine.LOG_COEF = 0.3 + 0.002 * params[0];
        PoisonedWine.TEST_SUB = 0.0 + 0.05 * params[1];
        PoisonedWine.ROUND_COEF = 1.0 + 0.01 * params[2];
        PoisonedWine.WID_COEF = 0.95 + 0.002 * params[3];
        PoisonedWine.TEST_COEF = 0.0 + 0.002 * params[4];
        PoisonedWine.STRIP_COEF = 0.5 + 0.01 * params[5];
        PoisonedWine.Y_COEF = 1.8 + 0.004 * params[6];
        PoisonedWine.X_COEF = 0.9 + 0.002 * params[7];
        PoisonedWine.SHUFFLE_COEF = 0.0 + 0.1 * params[8];
//        PoisonedWine.BASE_ADD = PoisonedWine.BASE * (params[8] - 50) / 20;
//        PoisonedWine.WINE_COEF_ADD = PoisonedWine.WINE_COEF * (params[9] - 50) / 20;
//        PoisonedWine.POIS_COEF_ADD = PoisonedWine.POIS_COEF * (params[10] - 50) / 20;
//        PoisonedWine.ROUD_COEF_ADD = PoisonedWine.ROUD_COEF * (params[11] - 50) / 20;
//        PoisonedWine.STRP_COEF_ADD = PoisonedWine.STRP_COEF * (params[12] - 50) / 20;

//        PoisonedWine.VAL_MAX = 40 * params[8];
        history.put(id, this);
        return score = PoisonedWineVis.evaluate();
    }

    @Override
    public int compareTo(State o) {
//        if(id == o.id) return 0;
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
