package fireStation;

import java.util.Random;

/**
 * Mesin utama Algoritma Genetika (GA) untuk menyelesaikan optimasi penempatan stasiun
 * pemadam kebakaran pada data peta. Mengatur siklus evolusi mulai dari seleksi induk,
 * rekombinasi (crossover), mutasi, hingga penggantian populasi berbasis proporsi elitisme.
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

    /** Referensi data peta lingkungan masalah dan populasi aktif yang berevolusi. */
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

    /** Parameter jumlah individu dalam populasi dan batas maksimum iterasi generasi. */
    int populationSize;
    int maxGenerations;

    /** Parameter probabilitas crossover, probabilitas mutasi, serta proporsi individu elit. */
    double crossoverRate;
    double mutationRate;
    double elitismRate;

    /**
     * Menginisialisasi mesin Algoritma Genetika dengan rasio elitisme default sebesar 10% (0.10).
     *
     * @param map                  Objek data peta sebagai lingkungan evaluasi masalah.
     * @param binaryRepresentation True untuk pengkodean biner, false untuk pengkodean integer.
     * @param selectionType        Metode seleksi induk yang digunakan.
     * @param crossoverType        Metode crossover yang digunakan.
     * @param mutationType         Metode mutasi yang sesuai dengan tipe representasi.
     * @param replacementType      Strategi penggantian generasi populasi.
     * @param populationSize       Jumlah individu dalam populasi (minimal 2).
     * @param maxGenerations       Batas maksimum generasi evolusi.
     * @param crossoverRate        Probabilitas terjadinya crossover antar induk (0.0 - 1.0).
     * @param mutationRate         Probabilitas terjadinya mutasi pada keturunan (0.0 - 1.0).
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

        this(map, binaryRepresentation, selectionType, crossoverType,
             mutationType, replacementType, populationSize, maxGenerations,
             crossoverRate, mutationRate, 0.10, random);
    }

    /**
     * Konstruktor lengkap untuk menginisialisasi mesin Algoritma Genetika dengan
     * parameter rasio elitisme yang dapat dikustomisasi (digunakan pada eksperimen hyperparameter Tahap 2).
     *
     * @param map                  Objek data peta sebagai lingkungan evaluasi masalah.
     * @param binaryRepresentation True untuk pengkodean biner, false untuk pengkodean integer.
     * @param selectionType        Metode seleksi induk yang digunakan.
     * @param crossoverType        Metode crossover yang digunakan.
     * @param mutationType         Metode mutasi yang sesuai dengan tipe representasi.
     * @param replacementType      Strategi penggantian generasi populasi.
     * @param populationSize       Jumlah individu dalam populasi (minimal 2).
     * @param maxGenerations       Batas maksimum generasi evolusi.
     * @param crossoverRate        Probabilitas terjadinya crossover antar induk (0.0 - 1.0).
     * @param mutationRate         Probabilitas terjadinya mutasi pada keturunan (0.0 - 1.0).
     * @param elitismRate          Proporsi individu terbaik yang dipertahankan tiap generasi (0.0 - kurang dari 1.0).
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
            double elitismRate,
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
        this.elitismRate = elitismRate;

        this.random = random;

        // Memvalidasi batasan parameter dan kecocokan operator sebelum membentuk populasi awal
        validateConfiguration();

        population = new Population(
            map, populationSize, binaryRepresentation, random
        );
    }

    /**
     * Memvalidasi ukuran populasi, rentang probabilitas parameter (crossover, mutation, elitism),
     * serta kesesuaian operator mutasi dengan skema representasi kromosom yang aktif.
     *
     * @throws IllegalArgumentException Jika ada parameter di luar rentang valid atau operator mutasi tidak kompatibel.
     */
    private void validateConfiguration() {

        if (populationSize < 2) {
            throw new IllegalArgumentException(
                "Population size minimal 2."
            );
        }
        if (crossoverRate < 0.0 || crossoverRate > 1.0 ||
            mutationRate < 0.0 || mutationRate > 1.0 ||
            elitismRate < 0.0 || elitismRate >= 1.0) {
            throw new IllegalArgumentException("Rate harus berada pada rentang yang valid; elitism harus < 1.0.");
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
     * array kromosomnya agar modifikasi generasi berikutnya tidak mengubah rekor solusi terbaik.
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
     * Memilih satu individu induk dari populasi aktif sesuai metode seleksi yang dikonfigurasi.
     *
     * @return Individu terpilih untuk proses reproduksi.
     * @throws IllegalStateException Jika metode seleksi tidak dikenali.
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
     * Melakukan persilangan genetik (crossover) antara dua individu induk berdasarkan
     * skema representasi kromosom dan operator crossover yang dipilih.
     *
     * @param parent1 Individu induk pertama.
     * @param parent2 Individu induk kedua.
     * @return Array berisi individu keturunan (offspring) hasil rekombinasi.
     * @throws IllegalStateException Jika metode crossover tidak dikenali.
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
     * dan metode mutasi aktif, kemudian menghitung ulang nilai fitness-nya.
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
     * @return Individu terbaik global yang ditemukan sepanjang seluruh generasi.
     */
    public Individual run() {
        return run(true);
    }

    /**
     * Mengeksekusi siklus utama Algoritma Genetika hingga mencapai {@code maxGenerations}.
     * Mencakup proses seleksi induk, crossover, mutasi, pembaruan populasi (elitism atau
     * generational), serta pelacakan kandidat solusi terbaik global.
     *
     * @param showProgress True untuk mencetak ringkasan cost dan fitness tiap generasi ke konsol.
     * @return Individu terbaik global dengan nilai cost terendah dari seluruh generasi.
     */
    public Individual run(boolean showProgress) {

        // Menyimpan salinan individu terbaik dari populasi awal sebagai acuan pembanding global
        Individual bestOverall = copyIndividual(
            population.getBestIndividual()
        );

        for (int generation = 0;
            generation < maxGenerations;
            generation++) {

            Individual[] offspring =
                new Individual[populationSize];

            int index = 0;

            // Membentuk individu keturunan hingga memenuhi kapasitas populasi generasi baru
            while (index < populationSize) {

                Individual parent1 = selectParent();
                Individual parent2 = selectParent();

                // Memastikan kedua induk merupakan instans berbeda untuk menghindari self-crossover
                while (parent1 == parent2) {
                    parent2 = selectParent();
                }

                Individual[] children;

                // Melakukan rekombinasi jika memenuhi probabilitas crossoverRate, atau menyalin induk langsung
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

                // Memproses setiap anak: terapkan mutasi sesuai peluang mutationRate, hitung fitness, lalu simpan
                for (int i = 0; i < children.length; i++) {

                    if (random.nextDouble() < mutationRate) {
                        performMutation(children[i]);
                    } else {
                        children[i].calculateFitness(map);
                    }

                    offspring[index] = children[i];
                    index++;

                    // Menghentikan pengisian apabila kapasitas offspring generasi baru sudah penuh
                    if (index >= populationSize) {
                        break;
                    }
                }
            }

            // Memperbarui populasi; pada Elitism, jumlah elit dihitung dari elitismRate dan dibatasi [1, populationSize - 1]
            if (replacementType == ReplacementType.ELITISM) {

                int eliteCount = (int) Math.round(populationSize * elitismRate);
                eliteCount = Math.max(1, Math.min(populationSize - 1, eliteCount));
                population.elitismReplacement(
                    offspring, eliteCount, random
                );

            } else {

                population.generationalReplacement(offspring);
            }

            // Memperbarui rekor solusi terbaik global jika ditemukan individu dengan cost lebih rendah
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