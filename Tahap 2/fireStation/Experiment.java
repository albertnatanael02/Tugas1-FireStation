package fireStation;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

/**
 * Kelas utama untuk mengeksekusi eksperimen pengujian parameter Algoritma Genetika (GA)
 * pada masalah penempatan stasiun pemadam kebakaran (fire station).
 * Mengevaluasi berbagai kombinasi representasi kromosom, operator seleksi, crossover,
 * dan mutasi pada sejumlah peta uji, lalu mencatat hasil detail dan ringkasannya ke file CSV.
 */
public class Experiment {

    /** Jumlah individu dalam populasi untuk setiap generasi GA. */
    private static final int POPULATION_SIZE = 10;

    /** Batas maksimum iterasi generasi dalam satu kali eksekusi GA. */
    private static final int MAX_GENERATIONS = 100;

    /** Probabilitas terjadinya rekombinasi (crossover) antar induk. */
    private static final double CROSSOVER_RATE = 0.8;

    /** Probabilitas terjadinya perubahan acak (mutasi) pada gen individu. */
    private static final double MUTATION_RATE = 0.1;

    /** Jumlah pengulangan eksekusi per konfigurasi untuk memperoleh validitas statistik. */
    private static final int REPETITIONS = 30;

    /**
     * Titik masuk utama program untuk menjalankan seluruh skenario eksperimen.
     * Memuat dataset peta uji, mengevaluasi seluruh kombinasi parameter secara sistematis,
     * mencatat statistik cost dan waktu eksekusi ke CSV, serta menampilkan kombinasi terbaik.
     *
     * @param args Argumen baris perintah (tidak digunakan dalam eksperimen ini).
     * @throws Exception Jika terjadi kesalahan pembacaan file peta atau penulisan file output CSV.
     */
    public static void main(String[] args) throws Exception {

        // Daftar file peta uji berukuran n=40 dengan karakteristik distribusi acak, klaster, dan labirin
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

        // Inisialisasi stream penulis untuk menyimpan hasil mentah per repetisi dan rekapitulasi per konfigurasi
        BufferedWriter detailWriter = new BufferedWriter(
            new FileWriter("experiment_detail.csv")
        );

        BufferedWriter summaryWriter = new BufferedWriter(
            new FileWriter("experiment_summary.csv")
        );

        detailWriter.write(
            "TestCase,Representation,Selection,Crossover,Mutation," +
            "Repetition,Seed,BestCost,RuntimeMs"
        );
        detailWriter.newLine();

        summaryWriter.write(
            "TestCase,Representation,Selection,Crossover,Mutation," +
            "BestCost,AverageCost,WorstCost,AverageRuntimeMs"
        );
        summaryWriter.newLine();

        // Konfigurasi skema pengkodean kromosom (true = biner, false = integer) beserta daftar operator GA
        boolean[] representations = {true, false};

        GeneticAlgorithm.SelectionType[] selections = {
            GeneticAlgorithm.SelectionType.TOURNAMENT,
            GeneticAlgorithm.SelectionType.ROULETTE
        };

        GeneticAlgorithm.CrossoverType[] crossovers = {
            GeneticAlgorithm.CrossoverType.SINGLE_POINT,
            GeneticAlgorithm.CrossoverType.TWO_POINT
        };

        // Variabel pelacak konfigurasi parameter dengan rata-rata cost terendah dari seluruh eksperimen
        String overallBestCombination = "";
        double overallBestAverageCost = Double.MAX_VALUE;

        // Iterasi utama untuk memuat dan menguji setiap peta testcase secara berurutan
        for (int testcase = 0; testcase < testCases.length; testcase++) {

            MapData map = new MapData(testCases[testcase]);

            System.out.println("\n=================================");
            System.out.println(
                "TESTCASE " + (testcase + 1) +
                " : " + testCases[testcase]
            );
            System.out.println("=================================");

            // Iterasi pengujian berdasarkan jenis representasi kromosom
            for (boolean binary : representations) {

                String representationName;

                if (binary) {
                    representationName = "Binary";
                } else {
                    representationName = "Integer";
                }

                // Menentukan himpunan operator mutasi yang kompatibel dengan tipe representasi aktif
                GeneticAlgorithm.MutationType[] mutations;

                if (binary) {

                    mutations = new GeneticAlgorithm.MutationType[] {
                        GeneticAlgorithm.MutationType.SWAP,
                        GeneticAlgorithm.MutationType.BIT_FLIP
                    };

                } else {

                    mutations = new GeneticAlgorithm.MutationType[] {
                        GeneticAlgorithm.MutationType.RANDOM_RESETTING,
                        GeneticAlgorithm.MutationType.RELOCATION
                    };
                }

                // Perulangan bersarang untuk mengevaluasi seluruh kombinasi seleksi, crossover, dan mutasi
                for (GeneticAlgorithm.SelectionType selection :
                     selections) {

                    for (GeneticAlgorithm.CrossoverType crossover :
                         crossovers) {

                        for (GeneticAlgorithm.MutationType mutation :
                             mutations) {

                            // Inisialisasi akumulator statistik untuk 30 kali pengulangan pada konfigurasi ini
                            double bestCost = Double.MAX_VALUE;

                            double worstCost =
                                Double.NEGATIVE_INFINITY;

                            double totalCost = 0;

                            double totalRuntime = 0;

                            System.out.println(
                                "\nMenjalankan: " +
                                "Testcase " + (testcase + 1) +
                                " | " +
                                representationName + " | " +
                                selection + " | " +
                                crossover + " | " +
                                mutation
                            );

                            // Menjalankan GA berulang kali dengan seed deterministik agar eksperimen dapat direproduksi
                            for (int repetition = 1;
                                 repetition <= REPETITIONS;
                                 repetition++) {

                                int seed = 1000 + repetition;

                                Random random = new Random(seed);

                                GeneticAlgorithm ga =
                                    new GeneticAlgorithm(
                                        map,
                                        binary,
                                        selection,
                                        crossover,
                                        mutation,
                                        GeneticAlgorithm
                                            .ReplacementType.ELITISM,
                                        POPULATION_SIZE,
                                        MAX_GENERATIONS,
                                        CROSSOVER_RATE,
                                        MUTATION_RATE,
                                        random
                                    );

                                // Mengukur durasi eksekusi algoritma dalam nanodetik dan mengonversinya ke milidetik
                                long startTime =
                                    System.nanoTime();

                                Individual result =
                                    ga.run(false);

                                long endTime =
                                    System.nanoTime();

                                double runtime =
                                    (endTime - startTime)
                                    / 1_000_000.0;

                                double cost = result.cost;

                                // Memperbarui nilai ekstrem (best/worst) serta mengakumulasi metrik untuk rata-rata
                                if (cost < bestCost) {
                                    bestCost = cost;
                                }

                                if (cost > worstCost) {
                                    worstCost = cost;
                                }

                                totalCost += cost;
                                totalRuntime += runtime;

                                // Mencatat hasil individual dari repetisi saat ini ke dalam file detail CSV
                                detailWriter.write(
                                    (testcase + 1) + "," +
                                    representationName + "," +
                                    selection + "," +
                                    crossover + "," +
                                    mutation + "," +
                                    repetition + "," +
                                    seed + "," +
                                    cost + "," +
                                    runtime
                                );

                                detailWriter.newLine();

                                System.out.println(
                                    "Repetition " +
                                    repetition +
                                    " | Best Cost: " +
                                    cost +
                                    " | Runtime: " +
                                    runtime +
                                    " ms"
                                );
                            }

                            // Menghitung metrik rata-rata setelah seluruh repetisi selesai dan menyimpannya ke file summary
                            double averageCost =
                                totalCost / REPETITIONS;

                            double averageRuntime =
                                totalRuntime / REPETITIONS;

                            summaryWriter.write(
                                (testcase + 1) + "," +
                                representationName + "," +
                                selection + "," +
                                crossover + "," +
                                mutation + "," +
                                bestCost + "," +
                                averageCost + "," +
                                worstCost + "," +
                                averageRuntime
                            );

                            summaryWriter.newLine();

                            System.out.println(
                                "Ringkasan konfigurasi:"
                            );

                            System.out.println(
                                "Best Cost: " +
                                bestCost
                            );

                            System.out.println(
                                "Average Cost: " +
                                averageCost
                            );

                            System.out.println(
                                "Worst Cost: " +
                                worstCost
                            );

                            System.out.println(
                                "Average Runtime: " +
                                averageRuntime +
                                " ms"
                            );

                            // Memperbarui catatan kombinasi parameter terbaik apabila rata-rata cost saat ini lebih rendah
                            if (averageCost <
                                overallBestAverageCost) {

                                overallBestAverageCost =
                                    averageCost;

                                overallBestCombination =
                                    "TestCase " +
                                    (testcase + 1) +
                                    " | " +
                                    representationName +
                                    " | " +
                                    selection +
                                    " | " +
                                    crossover +
                                    " | " +
                                    mutation;
                            }
                        }
                    }
                }
            }
        }

        // Menutup stream file untuk memastikan seluruh data buffer tertulis sempurna ke penyimpanan
        detailWriter.close();
        summaryWriter.close();

        System.out.println(
            "\n========================================"
        );

        System.out.println(
            "===== EKSPERIMEN SELESAI ====="
        );

        System.out.println(
            "Best Combination:"
        );

        System.out.println(
            overallBestCombination
        );

        System.out.println(
            "Best Average Cost: " +
            overallBestAverageCost
        );

        System.out.println(
            "========================================"
        );

        System.out.println(
            "File experiment_detail.csv berhasil dibuat."
        );

        System.out.println(
            "File experiment_summary.csv berhasil dibuat."
        );
    }
}