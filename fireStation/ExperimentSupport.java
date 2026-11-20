package fireStation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExperimentSupport {

    public static final int REPETITIONS = 30;
    public static final long BASE_SEED = 2026112000L;


    public static class TestCase {
        String size;
        String type;
        String path;
        long evaluationBudget;
        MapData map;

        TestCase(String size, String type, String path, long budget) throws Exception {
            this.size = size;
            this.type = type;
            this.path = path;
            this.evaluationBudget = budget;
            this.map = new MapData(path);
        }
    }

    public static class Model {
        String name;
        GeneticAlgorithm.SelectionType selection;
        GeneticAlgorithm.CrossoverType crossover;
        GeneticAlgorithm.MutationType mutation;

        Model(String name, GeneticAlgorithm.SelectionType selection,
              GeneticAlgorithm.CrossoverType crossover,
              GeneticAlgorithm.MutationType mutation) {
            this.name = name;
            this.selection = selection;
            this.crossover = crossover;
            this.mutation = mutation;
        }
    }

    public static class Config {
        int population;
        double crossover;
        double mutation;
        double elitism;

        Config(int population, double crossover, double mutation, double elitism) {
            this.population = population;
            this.crossover = crossover;
            this.mutation = mutation;
            this.elitism = elitism;
        }
    }

    public static class Stats {
        long runs;
        double totalCost;
        double bestCost = Double.MAX_VALUE;
        double worstCost = Double.NEGATIVE_INFINITY;
        double totalRuntime;

        void add(double cost, double runtime) {
            runs++;
            totalCost += cost;
            totalRuntime += runtime;
            if (cost < bestCost) bestCost = cost;
            if (cost > worstCost) worstCost = cost;
        }

        double averageCost() { return totalCost / runs; }
        double averageRuntime() { return totalRuntime / runs; }
    }

    public static Model[] getModels() {
        return new Model[] {
            new Model("M1", GeneticAlgorithm.SelectionType.ROULETTE,
                GeneticAlgorithm.CrossoverType.SINGLE_POINT,
                GeneticAlgorithm.MutationType.SWAP),
            new Model("M2", GeneticAlgorithm.SelectionType.ROULETTE,
                GeneticAlgorithm.CrossoverType.TWO_POINT,
                GeneticAlgorithm.MutationType.SWAP),
            new Model("M3", GeneticAlgorithm.SelectionType.ROULETTE,
                GeneticAlgorithm.CrossoverType.SINGLE_POINT,
                GeneticAlgorithm.MutationType.BIT_FLIP)
        };
    }

    // Format manifest: Size,Type,Path,EvaluationBudget
    public static List<TestCase> loadTestCases(String manifest) throws Exception {
        List<TestCase> cases = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(manifest));
        String line;
        int lineNumber = 0;
        int[][] counts = new int[3][3];

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("Size,")) continue;
            String[] parts = line.split(",", -1);
            if (parts.length != 4) {
                reader.close();
                throw new IllegalArgumentException("Format manifest salah pada baris " + lineNumber);
            }

            String size = parts[0].trim().toUpperCase();
            String type = parts[1].trim().toLowerCase();
            String path = parts[2].trim();
            long budget = Long.parseLong(parts[3].trim());
            if (budget <= 0) throw new IllegalArgumentException("Evaluation budget harus positif: " + path);

            int sizeIndex = size.equals("N20") ? 0 : size.equals("N40") ? 1 : size.equals("N60") ? 2 : -1;
            int typeIndex = type.equals("acak") ? 0 : type.equals("cluster") || type.equals("klaster") ? 1 : type.equals("labirin") || type.equals("maze") ? 2 : -1;
            if (sizeIndex < 0 || typeIndex < 0) {
                throw new IllegalArgumentException("Size/type tidak dikenal pada baris " + lineNumber + ": " + line);
            }
            counts[sizeIndex][typeIndex]++;
            cases.add(new TestCase(size, type, path, budget));
        }
        reader.close();

        if (cases.size() != 12) {
            throw new IllegalArgumentException("Manifest harus berisi 12 peta (4 per ukuran). Ditemukan: " + cases.size());
        }
        for (int s = 0; s < 3; s++) {
            if (counts[s][0] != 2 || counts[s][1] != 1 || counts[s][2] != 1) {
                throw new IllegalArgumentException("Setiap ukuran wajib memiliki 2 acak, 1 cluster, dan 1 labirin.");
            }
        }
        return cases;
    }

    public static int generationsFromBudget(long budget, int population) {
        // Evaluasi awal sebanyak population, lalu population evaluasi per generasi.
        long generations = budget / population - 1;
        if (generations < 1) {
            throw new IllegalArgumentException("Budget " + budget + " terlalu kecil untuk population " + population);
        }
        if (generations > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) generations;
    }

    public static long seedForRepetition(int repetition) {
        return BASE_SEED + repetition;
    }

    public static Stats runConfiguration(TestCase testCase, Model model, Config config,
                                         BufferedWriter detail, String experimentName,
                                         String parameterName, String parameterValue) throws Exception {
        Stats stats = new Stats();
        int generations = generationsFromBudget(testCase.evaluationBudget, config.population);

        for (int repetition = 1; repetition <= REPETITIONS; repetition++) {
            long seed = seedForRepetition(repetition);
            Random random = new Random(seed);

            GeneticAlgorithm ga = new GeneticAlgorithm(
                testCase.map,
                true,
                model.selection,
                model.crossover,
                model.mutation,
                GeneticAlgorithm.ReplacementType.ELITISM,
                config.population,
                generations,
                config.crossover,
                config.mutation,
                config.elitism,
                random
            );

            long start = System.nanoTime();
            Individual result = ga.run(false);
            long end = System.nanoTime();

            double runtime = (end - start) / 1_000_000.0;
            stats.add(result.cost, runtime);

            detail.write(experimentName + "," + testCase.size + "," + testCase.type + "," +
                testCase.path + "," + model.name + "," + model.selection + "," +
                model.crossover + "," + model.mutation + "," + parameterName + "," +
                parameterValue + "," + config.population + "," + config.crossover + "," +
                config.mutation + "," + config.elitism + "," + generations + "," +
                repetition + "," + seed + "," + result.cost + "," + runtime);
            detail.newLine();
        }
        return stats;
    }

    public static void writeDetailHeader(BufferedWriter writer) throws Exception {
        writer.write("Experiment,Size,Type,TestCase,Model,Selection,Crossover,Mutation,TestedParameter,TestedValue," +
            "Population,CrossoverRate,MutationRate,ElitismRate,Generations,Repetition,Seed,BestCost,RuntimeMs");
        writer.newLine();
    }

    public static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
