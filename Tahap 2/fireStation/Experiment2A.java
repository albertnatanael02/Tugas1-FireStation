package fireStation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Kelas utama untuk menjalankan eksperimen tahap 2A (penyetelan hyperparameter sekuensial)
 * pada beberapa model Algoritma Genetika untuk masalah penempatan stasiun pemadam kebakaran.
 * Mengoptimalkan parameter ukuran populasi, probabilitas crossover, probabilitas mutasi,
 * dan rasio elitisme secara berurutan, lalu menyimpan hasilnya serta konfigurasi terbaik
 * untuk digunakan sebagai input pada Experiment2B.
 */
public class Experiment2A {

    /**
     * Struktur data internal untuk menyimpan nilai kandidat parameter beserta
     * statistik agregat performa (cost dan waktu komputasi) yang dihasilkan.
     */
    private static class CandidateResult {
        double value;
        ExperimentSupport.Stats stats;

        /**
         * Membentuk objek hasil evaluasi untuk satu kandidat nilai parameter.
         *
         * @param value Nilai numerik kandidat parameter yang diuji.
         * @param stats Objek statistik gabungan dari seluruh pengujian pada kandidat tersebut.
         */
        CandidateResult(double value, ExperimentSupport.Stats stats) {
            this.value = value;
            this.stats = stats;
        }
    }
    
    /**
     * Titik masuk utama program untuk mengeksekusi penyetelan hyperparameter tahap 2A.
     * Memuat seluruh peta uji N40, menyetel empat parameter utama per model secara bertahap,
     * mencatat log detail dan ringkasan evaluasi ke CSV, serta mengekspor konfigurasi terbaik.
     *
     * @param args Argumen baris perintah (tidak digunakan).
     * @throws Exception Jika terjadi kegagalan pembacaan peta uji atau penulisan file output CSV.
     */
    public static void main(String[] args) throws Exception {
        String[] testCases = {
            "TestCases/n40/n40_acak_01.txt",
            "TestCases/n40/n40_acak_02.txt",
            "TestCases/n40/n40_acak_03.txt",
            "TestCases/n40/n40_acak_04.txt",
            "TestCases/n40/n40_klaster_01.txt",
            "TestCases/n40/n40_klaster_02.txt",
            "TestCases/n40/n40_klaster_03.txt",
            "TestCases/n40/n40_klaster_04.txt",
            "TestCases/n40/n40_labirin_01.txt",
            "TestCases/n40/n40_labirin_02.txt",
            "TestCases/n40/n40_labirin_03.txt",
            "TestCases/n40/n40_labirin_04.txt"
        };

        List<ExperimentSupport.TestCase> cases = new ArrayList<>();

        // Mengelompokkan setiap file testcase berdasarkan pola nama peta dengan anggaran 5000 evaluasi
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
                "N40",
                type,
                path,
                5000
            ));
        }
        ExperimentSupport.Model[] models = ExperimentSupport.getModels();

        // Matriks penyimpan parameter terpilih [model][parameter] serta dua peringkat teratas untuk populasi & crossover
        double[][] selected = new double[3][4];
        double[][] topTwoPopulation = new double[3][2];
        double[][] topTwoCrossover = new double[3][2];

        try (BufferedWriter detail = new BufferedWriter(new FileWriter("experiment2a_detail.csv"));
             BufferedWriter summary = new BufferedWriter(new FileWriter("experiment2a_summary.csv"))) {

            ExperimentSupport.writeDetailHeader(detail);
            summary.write("Model,Parameter,CandidateValue,RunCount,BestCost,AverageCost,WorstCost,AverageRuntimeMs,Selected");
            summary.newLine();

            // Menjalankan proses penyetelan parameter secara berurutan (greedy tuning) untuk setiap model GA
            for (int m = 0; m < models.length; m++) {
                ExperimentSupport.Model model = models[m];
                ExperimentSupport.Config config = new ExperimentSupport.Config(10, 0.8, 0.1, 0.10);

                System.out.println("\n========== " + model.name + " ==========");

                // Tahap 1: Penyetelan ukuran populasi (Population Size) dan penyimpanan dua kandidat terbaik
                double[] populationValues = {50, 100, 250, 500, 1000};
                List<CandidateResult> populationResults = testCandidates(
                    cases, model, config, "Population", populationValues, detail, "2A");
                populationResults.sort(Comparator.comparingDouble(a -> a.stats.averageCost()));
                config.population = (int) populationResults.get(0).value;
                selected[m][0] = config.population;
                topTwoPopulation[m][0] = populationResults.get(0).value;
                topTwoPopulation[m][1] = populationResults.get(1).value;
                writeCandidateSummary(summary, model.name, "Population", populationResults, config.population);
                System.out.println("Population terbaik: " + config.population);

                // Tahap 2: Penyetelan probabilitas crossover menggunakan ukuran populasi optimal dari tahap 1
                double[] crossoverValues = {0.5, 0.6, 0.7, 0.9, 1.0};
                List<CandidateResult> crossoverResults = testCandidates(
                    cases, model, config, "CrossoverRate", crossoverValues, detail, "2A");
                crossoverResults.sort(Comparator.comparingDouble(a -> a.stats.averageCost()));
                config.crossover = crossoverResults.get(0).value;
                selected[m][1] = config.crossover;
                topTwoCrossover[m][0] = crossoverResults.get(0).value;
                topTwoCrossover[m][1] = crossoverResults.get(1).value;
                writeCandidateSummary(summary, model.name, "CrossoverRate", crossoverResults, config.crossover);
                System.out.println("Crossover terbaik: " + config.crossover);

                // Tahap 3: Penyetelan probabilitas mutasi menggunakan konfigurasi terbaik dari dua tahap sebelumnya
                double[] mutationValues = {0.01, 0.03, 0.05, 0.15, 0.20};
                List<CandidateResult> mutationResults = testCandidates(
                    cases, model, config, "MutationRate", mutationValues, detail, "2A");
                mutationResults.sort(Comparator.comparingDouble(a -> a.stats.averageCost()));
                config.mutation = mutationResults.get(0).value;
                selected[m][2] = config.mutation;
                writeCandidateSummary(summary, model.name, "MutationRate", mutationResults, config.mutation);
                System.out.println("Mutation terbaik: " + config.mutation);

                // Tahap 4: Penyetelan persentase elitisme dengan mempertahankan seluruh parameter optimal sebelumnya
                double[] elitismValues = {0.05, 0.15, 0.20, 0.25, 0.30};
                List<CandidateResult> elitismResults = testCandidates(
                    cases, model, config, "ElitismRate", elitismValues, detail, "2A");
                elitismResults.sort(Comparator.comparingDouble(a -> a.stats.averageCost()));
                config.elitism = elitismResults.get(0).value;
                selected[m][3] = config.elitism;
                writeCandidateSummary(summary, model.name, "ElitismRate", elitismResults, config.elitism);
                System.out.println("Elitism terbaik: " + config.elitism);
            }
        }

        // Mengekspor rekap konfigurasi terbaik per model untuk menjadi acuan kombinasi pada Experiment2B
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("experiment2a_best_config.csv"))) {
            writer.write("Model,BestPopulation,SecondPopulation,BestCrossover,SecondCrossover,BestMutation,BestElitism");
            writer.newLine();
            for (int m = 0; m < models.length; m++) {
                writer.write(models[m].name + "," + (int) topTwoPopulation[m][0] + "," +
                    (int) topTwoPopulation[m][1] + "," + topTwoCrossover[m][0] + "," +
                    topTwoCrossover[m][1] + "," + selected[m][2] + "," + selected[m][3]);
                writer.newLine();
            }
        }

        System.out.println("\nEksperimen 2A selesai.");
        System.out.println("Output: experiment2a_detail.csv");
        System.out.println("Output: experiment2a_summary.csv");
        System.out.println("Output konfigurasi 2B: experiment2a_best_config.csv");
    }

    /**
     * Mengevaluasi seluruh nilai kandidat untuk satu parameter tertentu di atas semua
     * dataset peta uji, lalu menggabungkan statistik performanya per nilai kandidat.
     *
     * @param cases          Daftar seluruh peta uji (testcase) yang digunakan dalam evaluasi.
     * @param model          Model Algoritma Genetika yang sedang diuji.
     * @param baseConfig     Konfigurasi dasar parameter sebelum diubah oleh nilai kandidat.
     * @param parameter      Nama parameter yang sedang diuji ("Population", "CrossoverRate", "MutationRate", atau "ElitismRate").
     * @param values         Array berisi daftar nilai kandidat yang akan dievaluasi.
     * @param detail         Writer untuk mencatat hasil setiap eksekusi ke file CSV detail.
     * @param experimentName Label identitas eksperimen untuk pencatatan log.
     * @return Daftar objek {@link CandidateResult} yang memuat statistik gabungan tiap nilai kandidat.
     * @throws Exception Jika terjadi kesalahan selama eksekusi simulasi atau penulisan file log.
     */
    private static List<CandidateResult> testCandidates(
            List<ExperimentSupport.TestCase> cases,
            ExperimentSupport.Model model,
            ExperimentSupport.Config baseConfig,
            String parameter,
            double[] values,
            BufferedWriter detail,
            String experimentName) throws Exception {

        List<CandidateResult> results = new ArrayList<>();

        for (double value : values) {
            // Menyalin konfigurasi dasar dan menimpa hanya nilai parameter yang sedang diuji
            ExperimentSupport.Config config = new ExperimentSupport.Config(
                baseConfig.population, baseConfig.crossover, baseConfig.mutation, baseConfig.elitism);

            if (parameter.equals("Population")) config.population = (int) value;
            else if (parameter.equals("CrossoverRate")) config.crossover = value;
            else if (parameter.equals("MutationRate")) config.mutation = value;
            else if (parameter.equals("ElitismRate")) config.elitism = value;

            ExperimentSupport.Stats combined = new ExperimentSupport.Stats();
            System.out.println("Uji " + model.name + " | " + parameter + "=" + value);

            // Mengagregasikan metrik performa dari seluruh peta uji untuk kandidat nilai aktif
            for (ExperimentSupport.TestCase testCase : cases) {
                ExperimentSupport.Stats oneCase = ExperimentSupport.runConfiguration(
                    testCase, model, config, detail, experimentName, parameter,
                    String.valueOf(value));
                combined.runs += oneCase.runs;
                combined.totalCost += oneCase.totalCost;
                combined.totalRuntime += oneCase.totalRuntime;
                if (oneCase.bestCost < combined.bestCost) combined.bestCost = oneCase.bestCost;
                if (oneCase.worstCost > combined.worstCost) combined.worstCost = oneCase.worstCost;
            }

            results.add(new CandidateResult(value, combined));
            System.out.println("  Mean Cost=" + combined.averageCost() +
                " | Best=" + combined.bestCost + " | Runs=" + combined.runs);
        }
        return results;
    }

    /**
     * Menuliskan ringkasan statistik evaluasi seluruh kandidat untuk satu parameter
     * ke dalam file CSV summary, sekaligus menandai nilai kandidat yang terpilih sebagai terbaik.
     *
     * @param writer    Writer target untuk file CSV ringkasan.
     * @param model     Nama model Algoritma Genetika yang dievaluasi.
     * @param parameter Nama parameter yang diringkas.
     * @param results   Daftar hasil evaluasi kandidat (umumnya sudah terurut berdasarkan averageCost).
     * @param selected  Nilai kandidat terbaik yang dipilih pada tahap tersebut.
     * @throws Exception Jika terjadi kegagalan penulisan ke stream file CSV.
     */
    private static void writeCandidateSummary(BufferedWriter writer, String model,
            String parameter, List<CandidateResult> results, double selected) throws Exception {
        for (CandidateResult result : results) {
            ExperimentSupport.Stats s = result.stats;
            writer.write(model + "," + parameter + "," + result.value + "," + s.runs + "," +
                s.bestCost + "," + s.averageCost() + "," + s.worstCost + "," +
                s.averageRuntime() + "," + (result.value == selected));
            writer.newLine();
        }
    }
}