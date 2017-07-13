import java.io.*;

public class Statistics {
    public static void main(String[] args) {
        new Statistics().statistics();
    }

    private PrintWriter out;

    /**
     * PoisonedWineに関する統計をとるためのもの
     */
    void statistics() {
        statBestWidthChange();
    }

    void setWriter(String filename) {
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * wine, strip, round, poison を連続的に変化させたとき，
     * ベスト幅はどのように変化するかの統計をとる
     */
    void statBestWidthChange() {
        setWriter(System.getenv("DATA_PATH")
                + String.format("statistics/bestwid_poison.csv"));
        for(int poison = 1; poison <= 100; poison++) {
            PoisonedWine pw = new PoisonedWine();
            int wine = 2000;
            pw.P = poison;
            pw.W = wine;
            pw.S = 20;
            pw.R = 10;
            pw.initWidProb();
            System.out.println("Starting poison: " + poison);
            long time = System.currentTimeMillis();
            for(int strip = 1; strip <= pw.S; strip++) {
                for(int round = 1; round <= pw.R; round++) {
                    int wid = pw.estimateBestWidth(wine, strip, round);
                    double alive = pw.probNoPoison(wine, poison, wid);
                    String log = String.format("%d,%d,%d,%d,%f",
                            poison, strip, round, wid, alive);
                    out.println(log);
                }
            }
            System.out.println("time: " + (System.currentTimeMillis() - time));
            out.close();
        }
    }

    void writeToFile(String filename, String data) {
        try(PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            pw.print(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
