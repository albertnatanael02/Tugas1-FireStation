package fireStation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Kelas utama untuk mengeksekusi eksperimen tahap 2B (uji kombinasi parameter terbaik).
 * Membaca dua kandidat terbaik untuk ukuran populasi dan probabilitas crossover beserta
 * parameter optimal mutasi dan elitisme dari hasil {@code Experiment2A}, lalu mengevaluasi
 * seluruh kombinasi silangnya pada dataset peta uji yang ditentukan dan mencatat hasilnya ke CSV.
 */
public class Experiment2B {

    /** Nama file manifest daftar skenario uji tahap 2. */
    private static final String MANIFEST = "stage2_cases.csv";

    /** Nama file CSV konfigurasi optimal hasil keluaran Experiment2A. */
    private static final String CONFIG_FILE = "experiment2a_best_config.csv";

    /**
     * Struktur data internal untuk menampung konfigurasi parameter hasil seleksi
     * Experiment2A bagi setiap model Algoritma Genetika.
     */
    private static class ModelConfig {
        String model;
        int[] populations = new int[2];
        double[] crossovers = new double[2];
        double mutation;
        double elitism;
    }

    /**
     * Titik masuk utama program untuk menjalankan evaluasi kombinasi (grid search 2x2)
     * antara dua ukuran populasi terbaik dan dua probabilitas crossover terbaik per model,
     * serta menyimpan log detail dan ringkasan statistiknya ke file CSV.
     *
     * @param args Argumen baris perintah (tidak digunakan).
     * @throws Exception Jika terjadi kesalahan pembacaan file konfigurasi/peta atau penulisan output CSV.
     */
    public static void main(String[] args) throws Exception {
        String[] testCases = {
            "TestCases/n60/n60_acak_01.txt",
            "TestCases/n60/n60_acak_03.txt",
            "TestCases/n60/n60_klaster_04.txt",
            "TestCases/n60/n60_labirin_03.txt"
        };

        List<ExperimentSupport.TestCase> cases = new ArrayList<>();

        // Mengelompokkan setiap file testcase berdasarkan karakteristik petanya dengan anggaran 5000 evaluasi
        for (String path : testCases) {
            String type;

            if (path.contains("_acak_")) {
                type = "acak";
            } else if (path.contains("_klaster_")) {
                type = "klaster";
            } else {
                type = "labirin";
            }

            cases.add(new ExperimentSupport.TestCase(
                "N20",
                type,
                path,
                5000
            ));
        }
        List<ModelConfig> configs = loadBestConfigs();
        ExperimentSupport.Model[] models = ExperimentSupport.getModels();

        try (BufferedWriter detail = new BufferedWriter(new FileWriter("experiment2b_detail.csv"));
             BufferedWriter summary = new BufferedWriter(new FileWriter("experiment2b_summary.csv"))) {

            ExperimentSupport.writeDetailHeader(detail);
            summary.write("Model,Population,CrossoverRate,MutationRate,ElitismRate,RunCount,BestCost,AverageCost,WorstCost,AverageRuntimeMs");
            summary.newLine();

            // Mengevaluasi seluruh kombinasi silang 2x2 (Populasi x Crossover) untuk setiap model GA
            for (int m = 0; m < models.length; m++) {
                ExperimentSupport.Model model = models[m];
                ModelConfig mc = findConfig(configs, model.name);

                for (int population : mc.populations) {
                    for (double crossover : mc.crossovers) {
                        ExperimentSupport.Config config = new ExperimentSupport.Config(
                            population, crossover, mc.mutation, mc.elitism);
                        ExperimentSupport.Stats combined = new ExperimentSupport.Stats();

                        System.out.println("Uji 2B: " + model.name + " | Pop=" + population +
                            " | Cross=" + crossover + " | Mutation=" + mc.mutation +
                            " | Elitism=" + mc.elitism);

                        // Menjalankan konfigurasi aktif pada seluruh peta uji dan menggabungkan statistiknya
                        for (ExperimentSupport.TestCase testCase : cases) {
                            ExperimentSupport.Stats oneCase = ExperimentSupport.runConfiguration(
                                testCase, model, config, detail, "2B", "PopXCross",
                                population + "x" + crossover);
                            combined.runs += oneCase.runs;
                            combined.totalCost += oneCase.totalCost;
                            combined.totalRuntime += oneCase.totalRuntime;
                            if (oneCase.bestCost < combined.bestCost) combined.bestCost = oneCase.bestCost;
                            if (oneCase.worstCost > combined.worstCost) combined.worstCost = oneCase.worstCost;
                        }

                        summary.write(model.name + "," + population + "," + crossover + "," +
                            mc.mutation + "," + mc.elitism + "," + combined.runs + "," +
                            combined.bestCost + "," + combined.averageCost() + "," +
                            combined.worstCost + "," + combined.averageRuntime());
                        summary.newLine();
                        System.out.println("  Mean Cost=" + combined.averageCost() +
                            " | Runs=" + combined.runs);
                    }
                }
            }
        }

        System.out.println("\nEksperimen 2B selesai.");
        System.out.println("Output: experiment2b_detail.csv dan experiment2b_summary.csv");
    }

    /**
     * Membaca dan mem-parsing daftar konfigurasi parameter terbaik dari file CSV
     * hasil keluaran {@code Experiment2A} untuk ketiga model (M1, M2, dan M3).
     *
     * @return Daftar objek {@link ModelConfig} yang telah divalidasi kelengkapannya.
     * @throws Exception Jika file konfigurasi tidak ditemukan, format kolom salah, atau jumlah model bukan 3.
     */
    private static List<ModelConfig> loadBestConfigs() throws Exception {
        List<ModelConfig> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty() || line.startsWith("Model,")) continue;
            String[] p = line.split(",");
            if (p.length != 7) throw new IllegalArgumentException("Format " + CONFIG_FILE + " tidak valid.");
            ModelConfig c = new ModelConfig();
            c.model = p[0];
            c.populations[0] = Integer.parseInt(p[1]);
            c.populations[1] = Integer.parseInt(p[2]);
            c.crossovers[0] = Double.parseDouble(p[3]);
            c.crossovers[1] = Double.parseDouble(p[4]);
            c.mutation = Double.parseDouble(p[5]);
            c.elitism = Double.parseDouble(p[6]);
            result.add(c);
        }
        reader.close();
        if (result.size() != 3) throw new IllegalArgumentException("Harus ada konfigurasi untuk M1, M2, dan M3. Jalankan Experiment2A terlebih dahulu.");
        return result;
    }

    /**
     * Mencari objek konfigurasi parameter yang sesuai dengan nama model tertentu.
     *
     * @param configs Daftar konfigurasi model yang telah dimuat dari CSV.
     * @param name    Nama identitas model yang dicari.
     * @return Objek {@link ModelConfig} yang cocok dengan nama model.
     * @throws IllegalArgumentException Jika konfigurasi untuk nama model tersebut tidak ditemukan.
     */
    private static ModelConfig findConfig(List<ModelConfig> configs, String name) {
        for (ModelConfig c : configs) if (c.model.equals(name)) return c;
        throw new IllegalArgumentException("Konfigurasi model tidak ditemukan: " + name);
    }
}