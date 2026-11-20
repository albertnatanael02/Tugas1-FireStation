import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

/**
 * Kelas utama untuk menjalankan eksperimen pengujian parameter Algoritma Genetika (GA).
 * Mengevaluasi berbagai kombinasi representasi kromosom, metode seleksi, crossover,
 * dan mutasi pada sejumlah peta uji (testcase), lalu mencatat hasilnya ke dalam file CSV.
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
     * Titik masuk utama program untuk mengeksekusi seluruh skenario pengujian.
     * Menjalankan kombinasi parameter secara sistematis, menghitung statistik performa
     * (biaya terbaik, rata-rata, terburuk, serta waktu komputasi), dan menyimpannya ke disk.
     *
     * @param args Argumen baris perintah (tidak digunakan dalam eksperimen ini).
     * @throws Exception Jika terjadi kesalahan pembacaan file peta atau penulisan file output CSV.
     */
    public static void main(String[] args) throws Exception {

        // Daftar path file peta uji berukuran n=40 dengan karakteristik acak, klaster, dan labirin
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

        // Inisialisasi writer untuk mencatat log mentah setiap repetisi dan ringkasan agregat per konfigurasi
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

        // Konfigurasi jenis representasi kromosom (true = biner, false = integer) beserta daftar operator GA yang diuji
        boolean[] representations = {true, false};

        GeneticAlgorithm.SelectionType[] selections = {
            GeneticAlgorithm.SelectionType.TOURNAMENT,
            GeneticAlgorithm.SelectionType.ROULETTE
        };

        GeneticAlgorithm.CrossoverType[] crossovers = {
            GeneticAlgorithm.CrossoverType.SINGLE_POINT,
            GeneticAlgorithm.CrossoverType.TWO_POINT
        };

        // Variabel pelacak konfigurasi dengan rata-rata cost terendah di seluruh skenario
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

            // Iterasi pengujian berdasarkan skema pengkodean kromosom
            for (boolean binary : representations) {

                String representationName;

                if (binary) {
                    representationName = "Binary";
                } else {
                    representationName = "Integer";
                }

                // Menyesuaikan himpunan operator mutasi yang kompatibel dengan tipe representasi kromosom
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

                // Perulangan bersarang untuk menguji seluruh kombinasi operator seleksi, crossover, dan mutasi
                for (GeneticAlgorithm.SelectionType selection :
                     selections) {

                    for (GeneticAlgorithm.CrossoverType crossover :
                         crossovers) {

                        for (GeneticAlgorithm.MutationType mutation :
                             mutations) {

                            // Inisialisasi akumulator metrik evaluasi untuk 30 kali pengulangan pada konfigurasi aktif
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

                            // Menjalankan eksekusi GA berulang kali dengan seed deterministik agar hasil eksperimen dapat direproduksi
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

                                // Memperbarui nilai ekstrem (terbaik/terburuk) serta mengakumulasi nilai untuk perhitungan rata-rata
                                if (cost < bestCost) {
                                    bestCost = cost;
                                }

                                if (cost > worstCost) {
                                    worstCost = cost;
                                }

                                totalCost += cost;
                                totalRuntime += runtime;

                                // Mencatat metrik hasil eksekusi individual ke dalam file detail CSV
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

                            // Menghitung metrik rata-rata setelah seluruh repetisi selesai dan menyimpannya ke file ringkasan
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

                            // Memperbarui rekor kombinasi parameter terbaik apabila rata-rata cost saat ini lebih rendah
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

        // Menutup stream file untuk memastikan seluruh buffer data tersimpan sempurna ke penyimpanan
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