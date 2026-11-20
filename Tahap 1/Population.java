import java.util.ArrayList;
import java.util.Random;

public class Population {

    ArrayList<Individual> individuals;

    public Population(MapData map, int populationSize, boolean binaryRepresentation, Random random) {

        individuals = new ArrayList<Individual>();

        // Membuat individu sebanyak populationSize
        for (int i = 0; i < populationSize; i++) {

            Individual individu = new Individual(map, binaryRepresentation, random);

            individuals.add(individu);
        }
    }

    // Mengambil individu dengan fitness terbaik
    public Individual getBestIndividual() {

        Individual best = individuals.get(0);

        for (int i = 1; i < individuals.size(); i++) {

            if (individuals.get(i).fitness > best.fitness) {
                best = individuals.get(i);
            }
        }

        return best;
    }

    // Menampilkan seluruh individu
    public void displayPopulation() {

        for (int i = 0; i < individuals.size(); i++) {

            System.out.println("Individual " + (i + 1));
            System.out.println("Cost: " + individuals.get(i).cost);
            System.out.println("Fitness: " + individuals.get(i).fitness);
            System.out.println();
        }
    }
    // Tournament Selection
    public Individual tournamentSelection(Random random) {

        int tournamentSize = 3;

        // Memilih individu pertama secara acak
        Individual best = individuals.get(random.nextInt(individuals.size()));

        // Membandingkan dengan individu lainnya
        for (int i = 1; i < tournamentSize; i++) {

            Individual candidate = individuals.get(random.nextInt(individuals.size()));

            if (candidate.fitness > best.fitness) {
                best = candidate;
            }
        }

        return best;
    }


    // Roulette Wheel Selection
    public Individual rouletteWheelSelection(Random random) {

        // Menghitung total fitness seluruh individu
        double totalFitness = 0;

        for (int i = 0; i < individuals.size(); i++) {
            totalFitness += individuals.get(i).fitness;
        }

        // Menghasilkan angka acak dari 0 sampai total fitness
        double randomValue = random.nextDouble() * totalFitness;

        // Mencari individu berdasarkan akumulasi fitness
        double cumulativeFitness = 0;

        for (int i = 0; i < individuals.size(); i++) {

            cumulativeFitness += individuals.get(i).fitness;

            if (randomValue <= cumulativeFitness) {
                return individuals.get(i);
            }
        }

        // Mengembalikan individu terakhir jika tidak ada yang terpilih
        return individuals.get(individuals.size() - 1);
    }


    public Individual[] singlePointCrossover(
            Individual parent1, Individual parent2,
            MapData map, Random random) {

        int length = parent1.chromosome.length;

        int point = 1 + random.nextInt(length - 1);

        int[] child1 = new int[length];
        int[] child2 = new int[length];

        // Menyalin bagian chromosome sebelum titik potong
        for (int i = 0; i < point; i++) {
            child1[i] = parent1.chromosome[i];
            child2[i] = parent2.chromosome[i];
        }

        // Menukar bagian setelah titik potong
        for (int i = point; i < length; i++) {
            child1[i] = parent2.chromosome[i];
            child2[i] = parent1.chromosome[i];
        }

        // Memperbaiki jumlah Fire Station
        Individual.repairChromosome(child1, map, random);
        Individual.repairChromosome(child2, map, random);

        Individual offspring1 = Individual.createChild(
            map, child1, true
        );

        Individual offspring2 = Individual.createChild(
            map, child2, true
        );

        return new Individual[] { offspring1, offspring2 };
    }

    public Individual[] twoPointCrossover(
        Individual parent1, Individual parent2,
        MapData map, Random random) {

    int length = parent1.chromosome.length;

    // Menentukan dua titik potong yang berbeda
    int point1 = random.nextInt(length - 1);
    int point2 = point1 + 1 + random.nextInt(length - point1 - 1);

    int[] child1 = new int[length];
    int[] child2 = new int[length];

    // Menyalin kromosom dari kedua parent
    for (int i = 0; i < length; i++) {
        if (i >= point1 && i < point2) {
            child1[i] = parent2.chromosome[i];
            child2[i] = parent1.chromosome[i];
        } else {
            child1[i] = parent1.chromosome[i];
            child2[i] = parent2.chromosome[i];
        }
    }

        // Memastikan jumlah fire station tetap sesuai p
        Individual.repairChromosome(child1, map, random);
        Individual.repairChromosome(child2, map, random);

        // Membuat individu baru
        Individual offspring1 = Individual.createChild(map, child1, true);
        Individual offspring2 = Individual.createChild(map, child2, true);

        return new Individual[] { offspring1, offspring2 };
    }


    public Individual[] integerOnePointCrossover(
            Individual parent1,
            Individual parent2,
            MapData map,
            Random random) {

        // Membuat dua kromosom anak
        int[] child1 = new int[map.p];
        int[] child2 = new int[map.p];

        // Menentukan titik potong
        int point = 1 + random.nextInt(map.p - 1);

        // Mengambil gen dari kedua parent
        for (int i = 0; i < map.p; i++) {

            if (i < point) {
                child1[i] = parent1.chromosome[i];
                child2[i] = parent2.chromosome[i];
            } else {
                child1[i] = parent2.chromosome[i];
                child2[i] = parent1.chromosome[i];
            }
        }

        // Memperbaiki posisi yang duplikat
        repairIntegerChromosome(child1, map, random);
        repairIntegerChromosome(child2, map, random);

        // Membuat individu anak
        Individual offspring1 = Individual.createChild(
            map, child1, false
        );

        Individual offspring2 = Individual.createChild(
            map, child2, false
        );

        return new Individual[] { offspring1, offspring2 };
    }

    public void repairIntegerChromosome(
        int[] chromosome,
        MapData map,
        Random random) {

        for (int i = 0; i < chromosome.length; i++) {

            boolean duplicate = false;

            // Memeriksa apakah gen sudah muncul sebelumnya
            for (int j = 0; j < i; j++) {
                if (chromosome[i] == chromosome[j]) {
                    duplicate = true;
                    break;
                }
            }

            // Jika duplikat, cari posisi kosong yang belum digunakan
            if (duplicate) {

                int newPosition;
                boolean alreadyUsed;

                do {
                    newPosition = random.nextInt(map.emptyCells.size());
                    alreadyUsed = false;

                    for (int j = 0; j < chromosome.length; j++) {
                        if (chromosome[j] == newPosition) {
                            alreadyUsed = true;
                            break;
                        }
                    }

                } while (alreadyUsed);

                chromosome[i] = newPosition;
            }
        }
    }

    public Individual[] integerTwoPointCrossover(
        Individual parent1,
        Individual parent2,
        MapData map,
        Random random) {

        int[] child1 = new int[map.p];
        int[] child2 = new int[map.p];

        // Menentukan dua titik potong
        int point1 = random.nextInt(map.p - 1);
        int point2 = point1 + 1 + random.nextInt(map.p - point1 - 1);

        // Menggabungkan gen dari kedua parent
        for (int i = 0; i < map.p; i++) {

            if (i >= point1 && i < point2) {
                child1[i] = parent2.chromosome[i];
                child2[i] = parent1.chromosome[i];
            } else {
                child1[i] = parent1.chromosome[i];
                child2[i] = parent2.chromosome[i];
            }
        }

        // Memperbaiki posisi yang duplikat
        repairIntegerChromosome(child1, map, random);
        repairIntegerChromosome(child2, map, random);

        Individual offspring1 = Individual.createChild(
            map, child1, false
        );

        Individual offspring2 = Individual.createChild(
            map, child2, false
        );

        return new Individual[] { offspring1, offspring2 };
}
    public void elitismReplacement(
        Individual[] offspring, int eliteCount, Random random) {

        // Mengurutkan populasi berdasarkan fitness tertinggi
        for (int i = 0; i < individuals.size() - 1; i++) {
            for (int j = i + 1; j < individuals.size(); j++) {
                if (individuals.get(j).fitness > individuals.get(i).fitness) {
                    Individual temp = individuals.get(i);
                    individuals.set(i, individuals.get(j));
                    individuals.set(j, temp);
                }
            }
        }

        // Membuat populasi baru
        ArrayList<Individual> newPopulation = new ArrayList<>();

        // Mempertahankan individu terbaik
        for (int i = 0; i < eliteCount; i++) {
            newPopulation.add(individuals.get(i));
        }

        // Mengisi populasi menggunakan offspring yang tersedia
        while (newPopulation.size() < individuals.size()) {
            int index = random.nextInt(offspring.length);
            newPopulation.add(offspring[index]);
        }

        // Mengganti populasi lama
        individuals = newPopulation;
    }

    public void generationalReplacement(Individual[] offspring) {

        // Menghapus seluruh populasi lama
        individuals.clear();

        // Memasukkan offspring sebagai populasi baru
        for (int i = 0; i < offspring.length; i++) {
            individuals.add(offspring[i]);
        }
    }
}
