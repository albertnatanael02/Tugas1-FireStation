package fireStation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Experiment2A {

    private static class CandidateResult {
        double value;
        ExperimentSupport.Stats stats;
        CandidateResult(double value, ExperimentSupport.Stats stats) {
            this.value = value;
            this.stats = stats;
        }
    }
    
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

        double[][] selected = new double[3][4];
        double[][] topTwoPopulation = new double[3][2];
        double[][] topTwoCrossover = new double[3][2];

        try (BufferedWriter detail = new BufferedWriter(new FileWriter("experiment2a_detail.csv"));
             BufferedWriter summary = new BufferedWriter(new FileWriter("experiment2a_summary.csv"))) {

            ExperimentSupport.writeDetailHeader(detail);
            summary.write("Model,Parameter,CandidateValue,RunCount,BestCost,AverageCost,WorstCost,AverageRuntimeMs,Selected");
            summary.newLine();

            for (int m = 0; m < models.length; m++) {
                ExperimentSupport.Model model = models[m];
                ExperimentSupport.Config config = new ExperimentSupport.Config(10, 0.8, 0.1, 0.10);

                System.out.println("\n========== " + model.name + " ==========");

                // 1. Population Size
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

                // 2. Crossover Probability
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

                // 3. Mutation Probability
                double[] mutationValues = {0.01, 0.03, 0.05, 0.15, 0.20};
                List<CandidateResult> mutationResults = testCandidates(
                    cases, model, config, "MutationRate", mutationValues, detail, "2A");
                mutationResults.sort(Comparator.comparingDouble(a -> a.stats.averageCost()));
                config.mutation = mutationResults.get(0).value;
                selected[m][2] = config.mutation;
                writeCandidateSummary(summary, model.name, "MutationRate", mutationResults, config.mutation);
                System.out.println("Mutation terbaik: " + config.mutation);

                // 4. Elitism Percentage
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

        // File ini menjadi input konfigurasi untuk Experiment2B.
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
            ExperimentSupport.Config config = new ExperimentSupport.Config(
                baseConfig.population, baseConfig.crossover, baseConfig.mutation, baseConfig.elitism);

            if (parameter.equals("Population")) config.population = (int) value;
            else if (parameter.equals("CrossoverRate")) config.crossover = value;
            else if (parameter.equals("MutationRate")) config.mutation = value;
            else if (parameter.equals("ElitismRate")) config.elitism = value;

            ExperimentSupport.Stats combined = new ExperimentSupport.Stats();
            System.out.println("Uji " + model.name + " | " + parameter + "=" + value);

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
