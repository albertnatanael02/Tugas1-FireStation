package fireStation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Experiment2B {

    private static final String MANIFEST = "stage2_cases.csv";
    private static final String CONFIG_FILE = "experiment2a_best_config.csv";

    private static class ModelConfig {
        String model;
        int[] populations = new int[2];
        double[] crossovers = new double[2];
        double mutation;
        double elitism;
    }

    public static void main(String[] args) throws Exception {
        String[] testCases = {
            "TestCases/n40/n40_acak_01.txt",
            "TestCases/n40/n40_acak_03.txt",
            "TestCases/n40/n40_klaster_04.txt",
            "TestCases/n40/n40_labirin_03.txt"
        };

        List<ExperimentSupport.TestCase> cases = new ArrayList<>();

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

    private static ModelConfig findConfig(List<ModelConfig> configs, String name) {
        for (ModelConfig c : configs) if (c.model.equals(name)) return c;
        throw new IllegalArgumentException("Konfigurasi model tidak ditemukan: " + name);
    }
}
