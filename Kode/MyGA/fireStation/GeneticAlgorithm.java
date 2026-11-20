package fireStation;

import java.util.Random;

public class GeneticAlgorithm {

    // Pilihan metode Selection
    public enum SelectionType {
        TOURNAMENT,
        ROULETTE
    }

    // Pilihan metode Crossover
    public enum CrossoverType {
        SINGLE_POINT,
        TWO_POINT
    }

    // Pilihan metode Mutation
    public enum MutationType {
        SWAP,
        BIT_FLIP,
        RANDOM_RESETTING,
        RELOCATION
    }

    // Pilihan metode Replacement
    public enum ReplacementType {
        ELITISM,
        GENERATIONAL
    }

    MapData map;
    Population population;
    Random random;

    boolean binaryRepresentation;

    SelectionType selectionType;
    CrossoverType crossoverType;
    MutationType mutationType;
    ReplacementType replacementType;

    int populationSize;
    int maxGenerations;

    double crossoverRate;
    double mutationRate;

    public GeneticAlgorithm(
            MapData map,
            boolean binaryRepresentation,
            SelectionType selectionType,
            CrossoverType crossoverType,
            MutationType mutationType,
            ReplacementType replacementType,
            int populationSize,
            int maxGenerations,
            double crossoverRate,
            double mutationRate,
            Random random) {

        this.map = map;
        this.binaryRepresentation = binaryRepresentation;

        this.selectionType = selectionType;
        this.crossoverType = crossoverType;
        this.mutationType = mutationType;
        this.replacementType = replacementType;

        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;

        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;

        this.random = random;

        // Memastikan konfigurasi operator sesuai representasi
        validateConfiguration();

        population = new Population(
            map, populationSize, binaryRepresentation, random
        );
    }

    // Memeriksa apakah kombinasi operator valid
    private void validateConfiguration() {

        if (populationSize < 2) {
            throw new IllegalArgumentException(
                "Population size minimal 2."
            );
        }

        if (binaryRepresentation) {

            if (mutationType != MutationType.SWAP &&
                mutationType != MutationType.BIT_FLIP) {

                throw new IllegalArgumentException(
                    "Binary hanya bisa menggunakan Swap atau Bit Flip."
                );
            }

        } else {

            if (mutationType != MutationType.RANDOM_RESETTING &&
                mutationType != MutationType.RELOCATION) {

                throw new IllegalArgumentException(
                    "Integer hanya bisa menggunakan Random Resetting atau Relocation."
                );
            }
        }
    }

    // Menyalin individu agar solusi terbaik tidak berubah
    private Individual copyIndividual(Individual source) {

        return Individual.createChild(
            map,
            source.chromosome.clone(),
            binaryRepresentation
        );
    }

    // Memilih parent berdasarkan metode yang dipilih
    private Individual selectParent() {

        switch (selectionType) {

            case TOURNAMENT:
                return population.tournamentSelection(random);

            case ROULETTE:
                return population.rouletteWheelSelection(random);

            default:
                throw new IllegalStateException(
                    "Selection tidak dikenal."
                );
        }
    }

    // Melakukan crossover berdasarkan representasi dan metode
    private Individual[] performCrossover(
            Individual parent1,
            Individual parent2) {

        if (binaryRepresentation) {

            switch (crossoverType) {

                case SINGLE_POINT:
                    return population.singlePointCrossover(
                        parent1, parent2, map, random
                    );

                case TWO_POINT:
                    return population.twoPointCrossover(
                        parent1, parent2, map, random
                    );
            }

        } else {

            switch (crossoverType) {

                case SINGLE_POINT:
                    return population.integerOnePointCrossover(
                        parent1, parent2, map, random
                    );

                case TWO_POINT:
                    return population.integerTwoPointCrossover(
                        parent1, parent2, map, random
                    );
            }
        }

        throw new IllegalStateException(
            "Crossover tidak dikenal."
        );
    }

    // Melakukan mutation berdasarkan metode yang dipilih
    private void performMutation(Individual child) {

        if (binaryRepresentation) {

            switch (mutationType) {

                case SWAP:
                    child.swapMutation(random);
                    break;

                case BIT_FLIP:
                    child.bitFlipMutation(random, map);
                    break;
            }

        } else {

            switch (mutationType) {

                case RANDOM_RESETTING:
                    child.randomResettingMutation(random, map);
                    break;

                case RELOCATION:
                    child.relocationMutation(random, map);
                    break;
            }
        }

        child.calculateFitness(map);
    }
    
    // Menjalankan GA dengan output progress
    public Individual run() {
        return run(true);
    }

    // Menjalankan GA dengan pilihan menampilkan progress
    public Individual run(boolean showProgress) {

        Individual bestOverall = copyIndividual(
            population.getBestIndividual()
        );

        for (int generation = 0;
            generation < maxGenerations;
            generation++) {

            Individual[] offspring =
                new Individual[populationSize];

            int index = 0;

            while (index < populationSize) {

                Individual parent1 = selectParent();
                Individual parent2 = selectParent();

                while (parent1 == parent2) {
                    parent2 = selectParent();
                }

                Individual[] children;

                if (random.nextDouble() < crossoverRate) {

                    children = performCrossover(
                        parent1, parent2
                    );

                } else {

                    children = new Individual[] {
                        copyIndividual(parent1),
                        copyIndividual(parent2)
                    };
                }

                for (int i = 0; i < children.length; i++) {

                    if (random.nextDouble() < mutationRate) {
                        performMutation(children[i]);
                    } else {
                        children[i].calculateFitness(map);
                    }

                    offspring[index] = children[i];
                    index++;

                    if (index >= populationSize) {
                        break;
                    }
                }
            }

            if (replacementType == ReplacementType.ELITISM) {

                population.elitismReplacement(
                    offspring, 1, random
                );

            } else {

                population.generationalReplacement(offspring);
            }

            Individual best = population.getBestIndividual();

            if (best.cost < bestOverall.cost) {
                bestOverall = copyIndividual(best);
            }

            // Hanya mencetak progress jika showProgress bernilai true
            if (showProgress) {
                System.out.println(
                    "Generation " + (generation + 1) +
                    " | Best Cost: " + best.cost +
                    " | Fitness: " + best.fitness
                );
            }
        }

        return bestOverall;
    }
}