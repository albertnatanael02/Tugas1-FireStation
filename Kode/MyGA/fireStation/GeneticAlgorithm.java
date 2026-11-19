
package fireStation;

import java.util.Random;

public class GeneticAlgorithm {

    MapData map;
    Population population;
    Random random;

    boolean binaryRepresentation;
    boolean useElitism;

    int populationSize;
    int maxGenerations;

    double crossoverRate;
    double mutationRate;

    public GeneticAlgorithm(
            MapData map,
            boolean binaryRepresentation,
            boolean useElitism,
            int populationSize,
            int maxGenerations,
            double crossoverRate,
            double mutationRate,
            Random random) {

        this.map = map;
        this.binaryRepresentation = binaryRepresentation;
        this.useElitism = useElitism;
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.random = random;

        population = new Population(
            map, populationSize, binaryRepresentation, random
        );
    }


    private Individual copyIndividual(Individual source) {
        return Individual.createChild(
            map,
            source.chromosome.clone(),
            binaryRepresentation
        );
    }
    public Individual run() {
        Individual bestOverall = copyIndividual(
            population.getBestIndividual()
        );
        for (int generation = 0; generation < maxGenerations; generation++) {

            Individual[] offspring = new Individual[populationSize];

            int index = 0;

            while (index < populationSize) {

                // Memilih parent
                Individual parent1 = population.tournamentSelection(random);
                Individual parent2 = population.rouletteWheelSelection(random);

                // Memastikan parent berbeda
                while (parent1 == parent2) {
                    parent2 = population.rouletteWheelSelection(random);
                }

                Individual[] children;

                // Melakukan crossover berdasarkan probabilitas
                if (random.nextDouble() < crossoverRate) {

                    if (binaryRepresentation) {
                        children = population.twoPointCrossover(
                            parent1, parent2, map, random
                        );
                    } else {
                        children = population.integerTwoPointCrossover(
                            parent1, parent2, map, random
                        );
                    }

                } else {
                    children = new Individual[] {
                        Individual.createChild(
                            map, parent1.chromosome.clone(), binaryRepresentation
                        ),
                        Individual.createChild(
                            map, parent2.chromosome.clone(), binaryRepresentation
                        )
                    };
                }

                // Melakukan mutation
                for (int i = 0; i < children.length; i++) {

                    if (random.nextDouble() < mutationRate) {

                        if (binaryRepresentation) {
                            children[i].swapMutation(random);
                        } else {
                            children[i].randomResettingMutation(random, map);
                        }
                    }

                    children[i].calculateFitness(map);

                    offspring[index] = children[i];
                    index++;

                    if (index >= populationSize) {
                        break;
                    }
                }
            }

            // Melakukan replacement
            if (useElitism) {
                population.elitismReplacement(
                    offspring, 1, random
                );
            } else {
                population.generationalReplacement(offspring);
            }

            // Menampilkan hasil setiap generasi
            Individual best = population.getBestIndividual();

            Individual currentBest = population.getBestIndividual();

            if (currentBest.cost < bestOverall.cost) {
                bestOverall = copyIndividual(currentBest);
            }
            System.out.println(
                "Generation " + (generation + 1) +
                " | Best Cost: " + best.cost +
                " | Fitness: " + best.fitness
            );
        }

        return bestOverall;
    }
}
