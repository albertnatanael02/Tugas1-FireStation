package fireStation;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class Experiment {

    // Parameter eksperimen
    private static final int POPULATION_SIZE = 10;
    private static final int MAX_GENERATIONS = 100;
    private static final double CROSSOVER_RATE = 0.8;
    private static final double MUTATION_RATE = 0.1;
    private static final int REPETITIONS = 30;

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

        // Menyiapkan file CSV
        BufferedWriter detailWriter = new BufferedWriter(
            new FileWriter("experiment_detail.csv")
        );

        BufferedWriter summaryWriter = new BufferedWriter(
            new FileWriter("experiment_summary.csv")
        );

        // Header CSV detail
        detailWriter.write(
            "TestCase,Representation,Selection,Crossover,Mutation," +
            "Repetition,Seed,BestCost,RuntimeMs"
        );
        detailWriter.newLine();

        // Header CSV summary
        summaryWriter.write(
            "TestCase,Representation,Selection,Crossover,Mutation," +
            "BestCost,AverageCost,WorstCost,AverageRuntimeMs"
        );
        summaryWriter.newLine();

        boolean[] representations = {true, false};

        GeneticAlgorithm.SelectionType[] selections = {
            GeneticAlgorithm.SelectionType.TOURNAMENT,
            GeneticAlgorithm.SelectionType.ROULETTE
        };

        GeneticAlgorithm.CrossoverType[] crossovers = {
            GeneticAlgorithm.CrossoverType.SINGLE_POINT,
            GeneticAlgorithm.CrossoverType.TWO_POINT
        };

        // Menyimpan statistik overall
        String overallBestCombination = "";
        double overallBestAverageCost = Double.MAX_VALUE;

        // ==========================================================
        // LOOP TESTCASE
        // ==========================================================

        for (int testcase = 0; testcase < testCases.length; testcase++) {

            MapData map = new MapData(testCases[testcase]);

            System.out.println("\n=================================");
            System.out.println(
                "TESTCASE " + (testcase + 1) +
                " : " + testCases[testcase]
            );
            System.out.println("=================================");

            for (boolean binary : representations) {

                String representationName;

                if (binary) {
                    representationName = "Binary";
                } else {
                    representationName = "Integer";
                }

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

                for (GeneticAlgorithm.SelectionType selection :
                     selections) {

                    for (GeneticAlgorithm.CrossoverType crossover :
                         crossovers) {

                        for (GeneticAlgorithm.MutationType mutation :
                             mutations) {

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

                            // ======================================
                            // REPETITION
                            // ======================================

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

                                // Statistik
                                if (cost < bestCost) {
                                    bestCost = cost;
                                }

                                if (cost > worstCost) {
                                    worstCost = cost;
                                }

                                totalCost += cost;
                                totalRuntime += runtime;

                                // Detail CSV
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

                            // ======================================
                            // SUMMARY
                            // ======================================

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

                            // ======================================
                            // CEK OVERALL BEST
                            // ======================================

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