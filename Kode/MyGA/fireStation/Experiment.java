
package fireStation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Experiment {

    // Parameter eksperimen
    private static final int POPULATION_SIZE = 10;
    private static final int MAX_GENERATIONS = 100;
    private static final double CROSSOVER_RATE = 0.8;
    private static final double MUTATION_RATE = 0.1;
    private static final int REPETITIONS = 10;

    public static void main(String[] args) throws Exception {

        // Membaca data peta
        MapData map = new MapData("fireStation/firestation_input.txt");

        // Menyiapkan file CSV
        BufferedWriter detailWriter = new BufferedWriter(
            new FileWriter("experiment_detail.csv")
        );

        BufferedWriter summaryWriter = new BufferedWriter(
            new FileWriter("experiment_summary.csv")
        );

        // Header CSV detail
        detailWriter.write(
            "Representation,Selection,Crossover,Mutation," +
            "Repetition,Seed,BestCost,RuntimeMs"
        );
        detailWriter.newLine();

        // Header CSV summary
        summaryWriter.write(
            "Representation,Selection,Crossover,Mutation," +
            "BestCost,AverageCost,WorstCost,AverageRuntimeMs"
        );
        summaryWriter.newLine();

        // Dua jenis representasi
        boolean[] representations = {true, false};

        // Semua metode selection
        GeneticAlgorithm.SelectionType[] selections = {
            GeneticAlgorithm.SelectionType.TOURNAMENT,
            GeneticAlgorithm.SelectionType.ROULETTE
        };

        // Semua metode crossover
        GeneticAlgorithm.CrossoverType[] crossovers = {
            GeneticAlgorithm.CrossoverType.SINGLE_POINT,
            GeneticAlgorithm.CrossoverType.TWO_POINT
        };

        // Menjalankan eksperimen
        for (boolean binary : representations) {

            String representationName;

            if (binary) {
                representationName = "Binary";
            } else {
                representationName = "Integer";
            }

            // Mutation disesuaikan dengan representasi
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

            for (GeneticAlgorithm.SelectionType selection : selections) {

                for (GeneticAlgorithm.CrossoverType crossover : crossovers) {

                    for (GeneticAlgorithm.MutationType mutation : mutations) {

                        // Menyimpan statistik konfigurasi
                        double bestCost = Double.MAX_VALUE;
                        double worstCost = Double.MIN_VALUE;
                        double totalCost = 0;
                        double totalRuntime = 0;

                        System.out.println(
                            "\nMenjalankan: " +
                            representationName + " | " +
                            selection + " | " +
                            crossover + " | " +
                            mutation
                        );

                        // Mengulang setiap konfigurasi
                        for (int repetition = 1;
                             repetition <= REPETITIONS;
                             repetition++) {

                            // Seed sama untuk setiap konfigurasi
                            int seed = 1000 + repetition;

                            Random random = new Random(seed);

                            // Membuat Genetic Algorithm
                            GeneticAlgorithm ga = new GeneticAlgorithm(
                                map,
                                binary,
                                selection,
                                crossover,
                                mutation,
                                GeneticAlgorithm.ReplacementType.ELITISM,
                                POPULATION_SIZE,
                                MAX_GENERATIONS,
                                CROSSOVER_RATE,
                                MUTATION_RATE,
                                random
                            );

                            // Mengukur waktu eksekusi GA
                            long startTime = System.nanoTime();

                            Individual result = ga.run(false);

                            long endTime = System.nanoTime();

                            double runtime =
                                (endTime - startTime) / 1_000_000.0;

                            double cost = result.cost;

                            // Menghitung statistik
                            if (cost < bestCost) {
                                bestCost = cost;
                            }

                            if (cost > worstCost) {
                                worstCost = cost;
                            }

                            totalCost += cost;
                            totalRuntime += runtime;

                            // Menyimpan hasil setiap percobaan
                            detailWriter.write(
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
                                "Repetition " + repetition +
                                " | Best Cost: " + cost +
                                " | Runtime: " + runtime + " ms"
                            );
                        }

                        // Menghitung rata-rata
                        double averageCost =
                            totalCost / REPETITIONS;

                        double averageRuntime =
                            totalRuntime / REPETITIONS;

                        // Menyimpan ringkasan konfigurasi
                        summaryWriter.write(
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

                        System.out.println("Ringkasan konfigurasi:");
                        System.out.println("Best Cost: " + bestCost);
                        System.out.println("Average Cost: " + averageCost);
                        System.out.println("Worst Cost: " + worstCost);
                        System.out.println(
                            "Average Runtime: " + averageRuntime + " ms"
                        );
                    }
                }
            }
        }

        // Menutup file CSV
        detailWriter.close();
        summaryWriter.close();

        System.out.println("\n===== EKSPERIMEN SELESAI =====");
        System.out.println("File experiment_detail.csv berhasil dibuat.");
        System.out.println("File experiment_summary.csv berhasil dibuat.");
    }
}
