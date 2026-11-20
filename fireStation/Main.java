package fireStation;

import java.io.FileNotFoundException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {

        MapData map = new MapData("fireStation/firestation_input.txt");

        Random random = new Random(12345);

        // Membuat population berisi 10 individu
        Population population = new Population(map, 10, true, random);

        System.out.println("Jumlah rumah: " + map.houses.size());
        System.out.println("Jumlah sel kosong: " + map.emptyCells.size());

        System.out.println("\nDaftar Population:");
        population.displayPopulation();

        Individual best = population.getBestIndividual();

        System.out.println("Individu terbaik:");
        System.out.println("Cost: " + best.cost);
        System.out.println("Fitness: " + best.fitness);

        // Menguji Tournament Selection
        Individual parent = population.tournamentSelection(random);

        System.out.println("\nHasil Tournament Selection:");
        System.out.println("Cost Parent: " + parent.cost);
        System.out.println("Fitness Parent: " + parent.fitness);

        // Menguji Roulette Wheel Selection
        Individual rouletteParent = population.rouletteWheelSelection(random);

        System.out.println("\nHasil Roulette Wheel Selection:");
        System.out.println("Cost Parent: " + rouletteParent.cost);
        System.out.println("Fitness Parent: " + rouletteParent.fitness);

        // Menguji Single-Point Crossover
        Individual parent1 = population.tournamentSelection(random);
        Individual parent2 = population.rouletteWheelSelection(random);

        Individual[] children = population.singlePointCrossover(
            parent1, parent2, map, random
        );

        System.out.println("\nHasil Single-Point Crossover:");

        for (int i = 0; i < children.length; i++) {
            System.out.println("Child " + (i + 1));
            System.out.println("Cost: " + children[i].cost);
            System.out.println("Fitness: " + children[i].fitness);
        }

        // Menguji Two-Point Crossover
        Individual[] children2 = population.twoPointCrossover(
            parent1, parent2, map, random
        );

        System.out.println("\nHasil Two-Point Crossover:");

        for (int i = 0; i < children2.length; i++) {
            System.out.println("Child " + (i + 1));
            System.out.println("Cost: " + children2[i].cost);
            System.out.println("Fitness: " + children2[i].fitness);
        }

        Individual mutationTest = Individual.createChild(
            map, children2[0].chromosome.clone(), true
        );

        System.out.println("\nSebelum Swap Mutation:");
        System.out.println("Cost: " + mutationTest.cost);
        System.out.println("Fitness: " + mutationTest.fitness);

        mutationTest.swapMutation(random);
        mutationTest.calculateFitness(map);

        System.out.println("Sesudah Swap Mutation:");
        System.out.println("Cost: " + mutationTest.cost);
        System.out.println("Fitness: " + mutationTest.fitness);

        Individual mutationTest2 = Individual.createChild(
            map, children2[0].chromosome.clone(), true
        );

        System.out.println("\nSebelum Bit Flip Mutation:");
        System.out.println("Cost: " + mutationTest2.cost);
        System.out.println("Fitness: " + mutationTest2.fitness);

        mutationTest2.bitFlipMutation(random, map);

        System.out.println("\nSesudah Bit Flip Mutation:");
        System.out.println("Cost: " + mutationTest2.cost);
        System.out.println("Fitness: " + mutationTest2.fitness);

        // Pengujian Random Resetting Mutation
        Individual integerMutation1 = new Individual(map, false, random);

        System.out.println("\nSebelum Random Resetting Mutation:");
        System.out.println("Cost: " + integerMutation1.cost);
        System.out.println("Fitness: " + integerMutation1.fitness);

        integerMutation1.randomResettingMutation(random, map);

        System.out.println("\nSesudah Random Resetting Mutation:");
        System.out.println("Cost: " + integerMutation1.cost);
        System.out.println("Fitness: " + integerMutation1.fitness);


        // Pengujian Relocation Mutation
        Individual integerMutation2 = new Individual(map, false, random);

        System.out.println("\nSebelum Relocation Mutation:");
        System.out.println("Cost: " + integerMutation2.cost);
        System.out.println("Fitness: " + integerMutation2.fitness);

        integerMutation2.relocationMutation(random, map);

        System.out.println("\nSesudah Relocation Mutation:");
        System.out.println("Cost: " + integerMutation2.cost);
        System.out.println("Fitness: " + integerMutation2.fitness);

        System.out.println("\nSebelum Elitism Replacement:");
        System.out.println("Best Cost: " + population.getBestIndividual().cost);

        // Menggunakan dua offspring hasil Two-Point Crossover
        population.elitismReplacement(children2, 1, random);

        System.out.println("\nSesudah Elitism Replacement:");
        System.out.println("Best Cost: " + population.getBestIndividual().cost);

        System.out.println("\nPopulasi Baru:");
        population.displayPopulation();

        // Membuat 10 offspring untuk Generational Replacement
        Individual[] generationChildren = new Individual[10];

        for (int i = 0; i < generationChildren.length; i += 2) {

            parent1 = population.tournamentSelection(random);
            parent2 = population.rouletteWheelSelection(random);

            while (parent1 == parent2) {
                parent2 = population.rouletteWheelSelection(random);
            }

            children = population.twoPointCrossover(
                parent1, parent2, map, random
            );

            generationChildren[i] = children[0];
            generationChildren[i + 1] = children[1];
        }

        // Pengujian Generational Replacement
        System.out.println("\nSebelum Generational Replacement:");
        System.out.println("Best Cost: " + population.getBestIndividual().cost);

        population.generationalReplacement(generationChildren);

        System.out.println("\nSesudah Generational Replacement:");
        System.out.println("Best Cost: " + population.getBestIndividual().cost);

        System.out.println("\nPopulasi Baru:");
        population.displayPopulation();

        // Menguji Integer One-Point Crossover
        Individual integerParent1 = new Individual(map, false, random);
        Individual integerParent2 = new Individual(map, false, random);

        Individual[] integerChildren = population.integerOnePointCrossover(
            integerParent1, integerParent2, map, random
        );

        System.out.println("\nHasil Integer One-Point Crossover:");

        for (int i = 0; i < integerChildren.length; i++) {
            System.out.println("Child " + (i + 1));
            System.out.println("Cost: " + integerChildren[i].cost);
            System.out.println("Fitness: " + integerChildren[i].fitness);
        }

        // Menguji Integer Two-Point Crossover
        Individual[] integerChildren2 = population.integerTwoPointCrossover(
            integerParent1, integerParent2, map, random
        );

        System.out.println("\nHasil Integer Two-Point Crossover:");

        for (int i = 0; i < integerChildren2.length; i++) {
            System.out.println("Child " + (i + 1));
            System.out.println("Cost: " + integerChildren2[i].cost);
            System.out.println("Fitness: " + integerChildren2[i].fitness);
        }

        // Pengujian Genetic Algorithm
        System.out.println("\n===== GENETIC ALGORITHM BINARY =====");

        // Menggunakan Binary Representation dan Elitism
        GeneticAlgorithm ga = new GeneticAlgorithm(
            map,
            true,
            GeneticAlgorithm.SelectionType.TOURNAMENT,
            GeneticAlgorithm.CrossoverType.SINGLE_POINT,
            GeneticAlgorithm.MutationType.SWAP,
            GeneticAlgorithm.ReplacementType.ELITISM,
            10,
            100,
            0.8,
            0.1,
            new Random(12345)
        );

        Individual bestSolution = ga.run(false);

        System.out.println("\nHasil Terbaik Genetic Algorithm:");
        System.out.println("Cost: " + bestSolution.cost);
        System.out.println("Fitness: " + bestSolution.fitness);

        System.out.println("\n===== GENETIC ALGORITHM INTEGER =====");

        // Menggunakan Integer Representation dan Elitism
        GeneticAlgorithm ga2 = new GeneticAlgorithm(
            map,
            false,
            GeneticAlgorithm.SelectionType.TOURNAMENT,
            GeneticAlgorithm.CrossoverType.SINGLE_POINT,
            GeneticAlgorithm.MutationType.RELOCATION,
            GeneticAlgorithm.ReplacementType.ELITISM,
            10,
            100,
            0.8,
            0.1,
            new Random(12345)
        );

        Individual bestSolution2 = ga2.run(false);

        System.out.println("\nHasil Terbaik Genetic Algorithm Integer:");
        System.out.println("Cost: " + bestSolution2.cost);
        System.out.println("Fitness: " + bestSolution2.fitness);
    }
}