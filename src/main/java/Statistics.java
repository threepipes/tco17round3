import javax.xml.crypto.Data;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        List<DataSet> list = new ArrayList<>();
        for(int i = 1; i <= 100; i++) list.add(new DataSet(i));
        setWriter(System.getenv("DATA_PATH")
                + String.format("statistics/bestwid_poison.csv"));
        list.parallelStream().forEach(DataSet::setResult);
        list.forEach(e -> e.logList.forEach(d -> out.println(d)));
        out.close();
    }

    void writeToFile(String filename, String data) {
        try(PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            pw.print(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DataSet {
    int poison;
    List<String> logList;
    DataSet(int p) {
        poison = p;
        logList = new ArrayList<>();
    }
    void setResult() {
        PoisonedWine pw = new PoisonedWine();
        int wine = 2000;
        pw.P = poison;
        pw.W = wine;
        pw.S = 20;
        pw.R = 10;
        pw.initWidProb();
        long time = System.currentTimeMillis();
        for(int strip = 1; strip <= pw.S; strip++) {
            for(int round = 1; round <= pw.R; round++) {
                int wid = pw.estimateBestWidth(wine, strip, round);
                double alive = pw.probNoPoison(wine, poison, wid);
                String log = String.format("%d,%d,%d,%d,%f",
                        poison, strip, round, wid, alive);
                logList.add(log);
            }
        }
        System.out.println("fin poison: " + poison +
                " time: " + (System.currentTimeMillis() - time));
    }
}