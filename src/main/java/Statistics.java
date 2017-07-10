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
        for(int poison = 1; poison <= 20; poison++) {
            setWriter(System.getenv("DATA_PATH")
                    + String.format("statistics/bestwid_%2d.csv", poison));
            PoisonedWine pw = new PoisonedWine();
            pw.P = poison;
            pw.W = 2000; // Wの初期値によっても変わる?最適値には関係ない気も
            pw.S = 20;
            pw.R = 10;
            pw.initWidProb();
            System.out.println("Starting poison: " + poison);
            for (int wine = 50; wine <= pw.W; wine++) {
                if(wine % 10 == 0) System.out.println("wine: " + wine);
                for(int strip = 1; strip <= pw.S; strip++) {
                    for(int round = 1; round <= pw.R; round++) {
                        int wid = pw.estimateBestWidth(wine, strip, round);
                        double alive = pw.probNoPoison(wine, poison, wid);
                        String log = String.format("%d,%d,%d,%d,%d,%f",
                                poison, wine, strip, round, wid, alive);
                        out.println(log);
                    }
                }
            }
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
