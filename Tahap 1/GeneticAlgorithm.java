import java.util.Random;

/**
 * Mesin utama Algoritma Genetika (GA) untuk mencari solusi optimal pada data peta.
 * Mengelola siklus evolusi mulai dari seleksi induk, rekombinasi (crossover), mutasi,
 * hingga pembaruan generasi populasi berdasarkan konfigurasi operator yang ditentukan.
 */
public class GeneticAlgorithm {

    /** Jenis metode pemilihan induk (parent selection). */
    public enum SelectionType {
        TOURNAMENT,
        ROULETTE
    }

    /** Jenis operator rekombinasi genetik (crossover) antar induk. */
    public enum CrossoverType {
        SINGLE_POINT,
        TWO_POINT
    }

    /** Jenis operator mutasi gen (SWAP & BIT_FLIP untuk biner; RANDOM_RESETTING & RELOCATION untuk integer). */
    public enum MutationType {
        SWAP,
        BIT_FLIP,
        RANDOM_RESETTING,
        RELOCATION
    }

    /** Strategi penggantian individu populasi pada setiap pergantian generasi. */
    public enum ReplacementType {
        ELITISM,
        GENERATIONAL
    }

    /** Referensi data peta dan populasi aktif yang berevolusi. */
    MapData map;
    Population population;

    /** Generator bilangan acak terpusat untuk menjamin reproduksibilitas eksperimen. */
    Random random;

    /** Penanda skema pengkodean kromosom (true = biner, false = integer). */
    boolean binaryRepresentation;

    /** Konfigurasi operator genetika yang dipilih untuk sesi eksekusi ini. */
    SelectionType selectionType;
    CrossoverType crossoverType;
    MutationType mutationType;
    ReplacementType replacementType;

    /** Parameter ukuran populasi dan batas maksimum iterasi generasi. */
    int populationSize;
    int maxGenerations;

    /** Parameter probabilitas terjadinya crossover dan mutasi (rentang 0.0 - 1.0). */
    double crossoverRate;
    double mutationRate;

    /**
     * Menginisialisasi mesin Algoritma Genetika, memvalidasi kesesuaian konfigurasi,
     * dan membangkitkan populasi awal secara acak.
     *
     * @param map                  Objek data peta sebagai lingkungan evaluasi masalah.
     * @param binaryRepresentation True jika menggunakan representasi biner, false jika integer.
     * @param selectionType        Metode seleksi induk yang digunakan.
     * @param crossoverType        Metode crossover yang digunakan.
     * @param mutationType         Metode mutasi yang sesuai dengan tipe representasi.
     * @param replacementType      Strategi penggantian generasi populasi.
     * @param populationSize       Jumlah individu dalam populasi (minimal 2).
     * @param maxGenerations       Batas maksimum generasi evolusi.
     * @param crossoverRate        Probabilitas terjadinya crossover antar induk.
     * @param mutationRate         Probabilitas terjadinya mutasi pada keturunan.
     * @param random               Instans Random untuk operasi stokastik.
     */
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

        // Memastikan parameter valid sebelum mengalokasikan memori populasi awal
        validateConfiguration();

        population = new Population(
            map, populationSize, binaryRepresentation, random
        );
    }

    /**
     * Memvalidasi batasan ukuran populasi serta kompatibilitas antara jenis
     * representasi kromosom dan operator mutasi yang dipilih.
     *
     * @throws IllegalArgumentException Jika ukuran populasi < 2 atau operator mutasi tidak sesuai representasi.
     */
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

    /**
     * Membuat salinan mendalam (deep copy) dari sebuah individu dengan meng-clone
     * array kromosomnya agar modifikasi pada salinan tidak memengaruhi individu asal.
     *
     * @param source Individu sumber yang akan disalin.
     * @return Objek Individual baru yang independen dengan susunan gen identik.
     */
    private Individual copyIndividual(Individual source) {

        return Individual.createChild(
            map,
            source.chromosome.clone(),
            binaryRepresentation
        );
    }

    /**
     * Memilih satu individu induk dari populasi saat ini berdasarkan strategi
     * seleksi yang dikonfigurasi.
     *
     * @return Individu terpilih yang akan digunakan dalam proses reproduksi.
     * @throws IllegalStateException Jika tipe seleksi tidak dikenali.
     */
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

    /**
     * Melakukan rekombinasi genetik (crossover) antara dua individu induk untuk
     * menghasilkan keturunan baru sesuai skema representasi kromosom.
     *
     * @param parent1 Individu induk pertama.
     * @param parent2 Individu induk kedua.
     * @return Array berisi individu keturunan (offspring) hasil persilangan.
     * @throws IllegalStateException Jika tipe crossover tidak dikenali.
     */
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

    /**
     * Menerapkan operasi mutasi pada individu keturunan sesuai tipe representasi
     * dan operator mutasi aktif, kemudian mengevaluasi ulang nilai fitness-nya.
     *
     * @param child Individu keturunan yang akan dimutasi.
     */
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
    
    /**
     * Menjalankan proses evolusi Algoritma Genetika dengan menampilkan log
     * perkembangan tiap generasi ke konsol secara default.
     *
     * @return Individu terbaik yang ditemukan sepanjang seluruh generasi.
     */
    public Individual run() {
        return run(true);
    }

    /**
     * Mengeksekusi siklus utama Algoritma Genetika selama jumlah generasi maksimum.
     * Mencakup proses seleksi induk, crossover, mutasi, penggantian populasi,
     * serta pelacakan solusi terbaik global.
     *
     * @param showProgress True untuk mencetak ringkasan cost dan fitness tiap generasi ke konsol.
     * @return Individu terbaik global dengan nilai cost terendah dari seluruh generasi.
     */
    public Individual run(boolean showProgress) {

        // Mencatat kandidat solusi terbaik dari populasi awal sebagai acuan pembanding
        Individual bestOverall = copyIndividual(
            population.getBestIndividual()
        );

        for (int generation = 0;
            generation < maxGenerations;
            generation++) {

            Individual[] offspring =
                new Individual[populationSize];

            int index = 0;

            // Mengisi populasi keturunan generasi baru hingga mencapai kapasitas populationSize
            while (index < populationSize) {

                Individual parent1 = selectParent();
                Individual parent2 = selectParent();

                // Mencegah perkawinan sedarah (self-crossover) dengan memastikan kedua induk berbeda instans
                while (parent1 == parent2) {
                    parent2 = selectParent();
                }

                Individual[] children;

                // Menentukan apakah pasangan induk melakukan crossover atau langsung disalin ke generasi berikutnya
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

                // Mengevaluasi setiap keturunan baru, menerapkan mutasi jika memenuhi probabilitas, lalu menyimpannya
                for (int i = 0; i < children.length; i++) {

                    if (random.nextDouble() < mutationRate) {
                        performMutation(children[i]);
                    } else {
                        children[i].calculateFitness(map);
                    }

                    offspring[index] = children[i];
                    index++;

                    // Berhenti lebih awal jika kuota populasi generasi baru sudah terpenuhi (antisipasi ukuran ganjil)
                    if (index >= populationSize) {
                        break;
                    }
                }
            }

            // Memperbarui populasi dengan strategi elitism (mempertahankan 1 elit) atau penggantian penuh
            if (replacementType == ReplacementType.ELITISM) {

                population.elitismReplacement(
                    offspring, 1, random
                );

            } else {

                population.generationalReplacement(offspring);
            }

            // Memperbarui rekor solusi terbaik global apabila generasi saat ini menghasilkan cost lebih rendah
            Individual best = population.getBestIndividual();

            if (best.cost < bestOverall.cost) {
                bestOverall = copyIndividual(best);
            }

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