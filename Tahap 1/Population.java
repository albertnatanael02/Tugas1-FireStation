import java.util.ArrayList;
import java.util.Random;

/**
 * Mengelola kumpulan kandidat solusi (populasi individu) dalam Algoritma Genetika.
 * Menyediakan operasi inisialisasi populasi, evaluasi individu terbaik, seleksi induk,
 * rekombinasi (crossover) untuk representasi biner maupun integer, perbaikan kromosom,
 * serta strategi penggantian generasi (replacement).
 */
public class Population {

    /** Daftar seluruh individu aktif yang membentuk populasi pada generasi saat ini. */
    ArrayList<Individual> individuals;

    /**
     * Membentuk populasi awal secara acak sesuai ukuran dan skema representasi yang ditentukan.
     *
     * @param map                  Objek data peta sebagai lingkungan evaluasi masalah.
     * @param populationSize       Jumlah individu yang dibangkitkan dalam populasi.
     * @param binaryRepresentation True untuk pengkodean biner, false untuk pengkodean integer.
     * @param random               Instans Random untuk pembangkitan individu secara acak.
     */
    public Population(MapData map, int populationSize, boolean binaryRepresentation, Random random) {

        individuals = new ArrayList<Individual>();

        for (int i = 0; i < populationSize; i++) {

            Individual individu = new Individual(map, binaryRepresentation, random);

            individuals.add(individu);
        }
    }

    /**
     * Menelusuri seluruh populasi untuk menemukan individu dengan nilai fitness tertinggi.
     *
     * @return Instans Individual terbaik pada generasi saat ini.
     */
    public Individual getBestIndividual() {

        Individual best = individuals.get(0);

        for (int i = 1; i < individuals.size(); i++) {

            if (individuals.get(i).fitness > best.fitness) {
                best = individuals.get(i);
            }
        }

        return best;
    }

    /**
     * Mencetak ringkasan nilai cost dan fitness dari seluruh individu dalam populasi ke konsol.
     */
    public void displayPopulation() {

        for (int i = 0; i < individuals.size(); i++) {

            System.out.println("Individual " + (i + 1));
            System.out.println("Cost: " + individuals.get(i).cost);
            System.out.println("Fitness: " + individuals.get(i).fitness);
            System.out.println();
        }
    }

    /**
     * Memilih satu individu induk menggunakan mekanisme seleksi turnamen (Tournament Selection).
     * Mengambil {@code tournamentSize} sampel individu secara acak dan mengembalikan yang terbaik.
     *
     * @param random Instans Random untuk memilih peserta turnamen secara acak.
     * @return Individu pemenang turnamen dengan nilai fitness tertinggi.
     */
    public Individual tournamentSelection(Random random) {

        int tournamentSize = 3;

        Individual best = individuals.get(random.nextInt(individuals.size()));

        // Membandingkan peserta awal dengan kandidat acak lainnya sebanyak ukuran turnamen
        for (int i = 1; i < tournamentSize; i++) {

            Individual candidate = individuals.get(random.nextInt(individuals.size()));

            if (candidate.fitness > best.fitness) {
                best = candidate;
            }
        }

        return best;
    }


    /**
     * Memilih satu individu induk secara proporsional terhadap nilai fitness-nya
     * menggunakan metode seleksi roda rolet (Roulette Wheel Selection).
     *
     * @param random Instans Random untuk memutar titik seleksi pada rentang akumulasi fitness.
     * @return Individu terpilih sesuai probabilitas proporsional fitness.
     */
    public Individual rouletteWheelSelection(Random random) {

        double totalFitness = 0;

        for (int i = 0; i < individuals.size(); i++) {
            totalFitness += individuals.get(i).fitness;
        }

        double randomValue = random.nextDouble() * totalFitness;

        // Menelusuri akumulasi fitness hingga melewati titik acak yang dibangkitkan
        double cumulativeFitness = 0;

        for (int i = 0; i < individuals.size(); i++) {

            cumulativeFitness += individuals.get(i).fitness;

            if (randomValue <= cumulativeFitness) {
                return individuals.get(i);
            }
        }

        // Pengaman (fallback) akibat galat pembulatan floating-point dengan mengembalikan elemen terakhir
        return individuals.get(individuals.size() - 1);
    }


    /**
     * Melakukan persilangan satu titik (Single-Point Crossover) pada dua induk biner,
     * diikuti perbaikan kromosom agar jumlah stasiun tetap valid sebanyak {@code map.p}.
     *
     * @param parent1 Individu induk pertama.
     * @param parent2 Individu induk kedua.
     * @param map     Objek data peta untuk perbaikan kromosom dan perhitungan fitness.
     * @param random  Instans Random untuk menentukan titik potong acak.
     * @return Array berisi dua individu keturunan baru hasil persilangan.
     */
    public Individual[] singlePointCrossover(
            Individual parent1, Individual parent2,
            MapData map, Random random) {

        int length = parent1.chromosome.length;

        int point = 1 + random.nextInt(length - 1);

        int[] child1 = new int[length];
        int[] child2 = new int[length];

        // Mewariskan segmen gen sebelum titik potong secara langsung dari masing-masing induk
        for (int i = 0; i < point; i++) {
            child1[i] = parent1.chromosome[i];
            child2[i] = parent2.chromosome[i];
        }

        // Menukar segmen gen mulai dari titik potong hingga akhir kromosom antar kedua induk
        for (int i = point; i < length; i++) {
            child1[i] = parent2.chromosome[i];
            child2[i] = parent1.chromosome[i];
        }

        // Memastikan jumlah gen bernilai 1 pada kedua anak tetap memenuhi batasan tepat p stasiun
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

    /**
     * Melakukan persilangan dua titik (Two-Point Crossover) pada dua induk biner dengan
     * menukar segmen gen di antara dua titik potong, lalu memperbaiki jumlah stasiun aktif.
     *
     * @param parent1 Individu induk pertama.
     * @param parent2 Individu induk kedua.
     * @param map     Objek data peta untuk perbaikan kromosom dan evaluasi fitness.
     * @param random  Instans Random untuk menentukan dua titik potong berbeda.
     * @return Array berisi dua individu keturunan baru hasil persilangan.
     */
    public Individual[] twoPointCrossover(
        Individual parent1, Individual parent2,
        MapData map, Random random) {

    int length = parent1.chromosome.length;

    // Memilih dua indeks potong terurut (point1 < point2) untuk membatasi segmen tengah
    int point1 = random.nextInt(length - 1);
    int point2 = point1 + 1 + random.nextInt(length - point1 - 1);

    int[] child1 = new int[length];
    int[] child2 = new int[length];

    // Menukar segmen gen di antara point1 dan point2 sementara bagian luar dipertahankan
    for (int i = 0; i < length; i++) {
        if (i >= point1 && i < point2) {
            child1[i] = parent2.chromosome[i];
            child2[i] = parent1.chromosome[i];
        } else {
            child1[i] = parent1.chromosome[i];
            child2[i] = parent2.chromosome[i];
        }
    }

        // Menyelaraskan kembali jumlah stasiun aktif agar sesuai target map.p
        Individual.repairChromosome(child1, map, random);
        Individual.repairChromosome(child2, map, random);

        Individual offspring1 = Individual.createChild(map, child1, true);
        Individual offspring2 = Individual.createChild(map, child2, true);

        return new Individual[] { offspring1, offspring2 };
    }


    /**
     * Melakukan persilangan satu titik (One-Point Crossover) pada dua induk integer,
     * diikuti perbaikan untuk menghilangkan duplikasi indeks lokasi stasiun.
     *
     * @param parent1 Individu induk pertama dengan representasi integer.
     * @param parent2 Individu induk kedua dengan representasi integer.
     * @param map     Objek data peta untuk perbaikan duplikasi dan perhitungan fitness.
     * @param random  Instans Random untuk menentukan titik potong acak.
     * @return Array berisi dua individu keturunan baru bertipe integer.
     */
    public Individual[] integerOnePointCrossover(
            Individual parent1,
            Individual parent2,
            MapData map,
            Random random) {

        int[] child1 = new int[map.p];
        int[] child2 = new int[map.p];

        int point = 1 + random.nextInt(map.p - 1);

        // Menggabungkan bagian awal dan akhir kromosom kedua induk berdasarkan titik potong
        for (int i = 0; i < map.p; i++) {

            if (i < point) {
                child1[i] = parent1.chromosome[i];
                child2[i] = parent2.chromosome[i];
            } else {
                child1[i] = parent2.chromosome[i];
                child2[i] = parent1.chromosome[i];
            }
        }

        // Mengganti indeks lokasi yang terduplikasi agar seluruh stasiun berada pada sel berbeda
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

    /**
     * Memperbaiki kromosom integer pasca-crossover agar setiap gen menyimpan indeks
     * sel kosong yang unik dengan mengganti gen duplikat menggunakan posisi acak yang belum terpakai.
     *
     * @param chromosome Array kromosom integer yang diperbaiki secara in-place.
     * @param map        Objek data peta yang menyimpan daftar sel kosong tersedia.
     * @param random     Instans Random untuk memilih indeks sel kosong pengganti.
     */
    public void repairIntegerChromosome(
        int[] chromosome,
        MapData map,
        Random random) {

        for (int i = 0; i < chromosome.length; i++) {

            boolean duplicate = false;

            // Memeriksa apakah nilai gen pada indeks i sudah muncul pada indeks sebelumnya
            for (int j = 0; j < i; j++) {
                if (chromosome[i] == chromosome[j]) {
                    duplicate = true;
                    break;
                }
            }

            // Mencari indeks sel kosong baru yang unik melalui rejection sampling jika terdeteksi duplikasi
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

    /**
     * Melakukan persilangan dua titik (Two-Point Crossover) pada dua induk integer,
     * diikuti perbaikan duplikasi indeks lokasi stasiun sebelum membentuk individu anak.
     *
     * @param parent1 Individu induk pertama dengan representasi integer.
     * @param parent2 Individu induk kedua dengan representasi integer.
     * @param map     Objek data peta untuk perbaikan kromosom dan perhitungan fitness.
     * @param random  Instans Random untuk menentukan dua titik potong acak.
     * @return Array berisi dua individu keturunan baru bertipe integer.
     */
    public Individual[] integerTwoPointCrossover(
        Individual parent1,
        Individual parent2,
        MapData map,
        Random random) {

        int[] child1 = new int[map.p];
        int[] child2 = new int[map.p];

        int point1 = random.nextInt(map.p - 1);
        int point2 = point1 + 1 + random.nextInt(map.p - point1 - 1);

        // Menukar blok gen di antara point1 dan point2 antar kedua induk
        for (int i = 0; i < map.p; i++) {

            if (i >= point1 && i < point2) {
                child1[i] = parent2.chromosome[i];
                child2[i] = parent1.chromosome[i];
            } else {
                child1[i] = parent1.chromosome[i];
                child2[i] = parent2.chromosome[i];
            }
        }

        // Menjamin keunikan setiap lokasi stasiun pada kedua kromosom keturunan
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

    /**
     * Memperbarui populasi menggunakan strategi Elitism dengan mempertahankan sejumlah
     * individu terbaik dari generasi lama dan mengisi sisa kapasitas dari array keturunan.
     *
     * @param offspring  Array individu keturunan hasil reproduksi generasi saat ini.
     * @param eliteCount Jumlah individu terbaik dari generasi lama yang dipertahankan.
     * @param random     Instans Random untuk memilih sampel keturunan pengisi sisa populasi.
     */
    public void elitismReplacement(
        Individual[] offspring, int eliteCount, Random random) {

        // Mengurutkan populasi saat ini secara menurun (descending) berdasarkan nilai fitness
        for (int i = 0; i < individuals.size() - 1; i++) {
            for (int j = i + 1; j < individuals.size(); j++) {
                if (individuals.get(j).fitness > individuals.get(i).fitness) {
                    Individual temp = individuals.get(i);
                    individuals.set(i, individuals.get(j));
                    individuals.set(j, temp);
                }
            }
        }

        ArrayList<Individual> newPopulation = new ArrayList<>();

        // Memindahkan individu-individu elit terbaik secara langsung ke populasi generasi berikutnya
        for (int i = 0; i < eliteCount; i++) {
            newPopulation.add(individuals.get(i));
        }

        // Melengkapi sisa slot populasi dengan mengambil individu secara acak dari kumpulan offspring
        while (newPopulation.size() < individuals.size()) {
            int index = random.nextInt(offspring.length);
            newPopulation.add(offspring[index]);
        }

        individuals = newPopulation;
    }

    /**
     * Mengganti seluruh individu dalam populasi lama secara penuh dengan individu
     * keturunan baru (Generational Replacement).
     *
     * @param offspring Array individu keturunan baru yang akan menggantikan populasi aktif.
     */
    public void generationalReplacement(Individual[] offspring) {

        individuals.clear();

        for (int i = 0; i < offspring.length; i++) {
            individuals.add(offspring[i]);
        }
    }
}