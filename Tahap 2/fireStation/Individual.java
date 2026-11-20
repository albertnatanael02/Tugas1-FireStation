package fireStation;

import java.util.Random;

/**
 * Merepresentasikan satu kandidat solusi (individu) dalam Algoritma Genetika
 * untuk optimasi penempatan p buah stasiun pemadam kebakaran (fire station).
 * Mendukung skema pengkodean biner maupun integer, lengkap dengan evaluasi kualitas
 * solusi (cost dan fitness) serta berbagai operator mutasi.
 */
public class Individual {

    /** Penanda skema pengkodean kromosom (true = biner, false = integer). */
    boolean binaryRepresentation;

    /** Susunan gen yang merepresentasikan lokasi penempatan stasiun pemadam kebakaran. */
    int[] chromosome;

    /** Rata-rata jarak terdekat dari seluruh rumah ke stasiun pemadam (semakin kecil semakin baik). */
    double cost;

    /** Nilai kebugaran individu hasil transformasi terbalik dari cost (semakin besar semakin baik). */
    double fitness;

    /**
     * Membangkitkan individu baru secara acak dengan menjamin tepat p stasiun
     * ditempatkan pada sel kosong yang unik, lalu menghitung nilai fitness awalnya.
     *
     * @param map                  Objek data peta berisi daftar sel kosong, rumah, dan matriks jarak.
     * @param binaryRepresentation True untuk pengkodean biner, false untuk pengkodean integer.
     * @param random               Instans Random untuk pemilihan posisi acak.
     */
    public Individual(MapData map, boolean binaryRepresentation, Random random) {

        this.binaryRepresentation = binaryRepresentation;

        if (binaryRepresentation) {

            // Skema biner: panjang array sepanjang total sel kosong dengan tepat p elemen bernilai 1
            chromosome = new int[map.emptyCells.size()];

            int count = 0;

            while (count < map.p) {

                int position = random.nextInt(chromosome.length);

                if (chromosome[position] == 0) {
                    chromosome[position] = 1;
                    count++;
                }
            }

        } else {

            // Skema integer: panjang array sebanyak p yang menyimpan indeks unik sel kosong
            chromosome = new int[map.p];

            int count = 0;

            while (count < map.p) {

                int position = random.nextInt(map.emptyCells.size());

                boolean alreadySelected = false;

                // Memastikan indeks sel kosong belum dipilih sebelumnya agar tidak terjadi duplikasi lokasi
                for (int i = 0; i < count; i++) {
                    if (chromosome[i] == position) {
                        alreadySelected = true;
                        break;
                    }
                }

                if (!alreadySelected) {
                    chromosome[count] = position;
                    count++;
                }
            }
        }

        calculateFitness(map);
    }

    /**
     * Mengevaluasi kualitas solusi individu dengan menghitung rata-rata jarak
     * terpendek dari setiap rumah ke stasiun terdekat (cost) serta nilai fitness-nya.
     *
     * @param map Objek data peta yang menyediakan matriks jarak antar rumah dan kandidat lokasi.
     */
    public void calculateFitness(MapData map) {

        double totalDistance = 0;

        for (int h = 0; h < map.houses.size(); h++) {

            // Inisialisasi batas atas jarak maksimum berdasarkan dimensi peta (m x n)
            int minimumDistance = map.m * map.n;

            for (int i = 0; i < chromosome.length; i++) {

                int position;

                if (binaryRepresentation) {

                    // Pada representasi biner, abaikan sel bernilai 0 dan gunakan indeks array sebagai posisi stasiun
                    if (chromosome[i] == 0) {
                        continue;
                    }

                    position = i;

                } else {

                    // Pada representasi integer, nilai gen secara langsung menunjuk indeks posisi stasiun
                    position = chromosome[i];
                }

                int distance = map.distances[h][position];

                if (distance < minimumDistance) {
                    minimumDistance = distance;
                }
            }

            totalDistance += minimumDistance;
        }

        cost = totalDistance / map.houses.size();

        fitness = 1.0 / (1.0 + cost);
    }


    /**
     * Metode pabrik (factory method) untuk membentuk objek individu anak baru
     * dari array kromosom hasil proses crossover atau penyalinan.
     *
     * @param map                  Objek data peta untuk evaluasi fitness.
     * @param chromosome           Array gen kromosom yang akan diwariskan ke individu baru.
     * @param binaryRepresentation True jika kromosom menggunakan skema biner, false jika integer.
     * @return Instans Individual baru yang telah terhitung nilai cost dan fitness-nya.
     */
    public static Individual createChild(
        MapData map,
        int[] chromosome,
        boolean binaryRepresentation) {

    return new Individual(
        map, chromosome, binaryRepresentation
    );
}

    /**
     * Konstruktor internal untuk menyalin kromosom secara mendalam (deep clone)
     * dan langsung menghitung nilai fitness individu.
     *
     * @param map                  Objek data peta untuk evaluasi fitness.
     * @param chromosome           Array gen kromosom sumber.
     * @param binaryRepresentation Penanda skema pengkodean kromosom.
     */
    private Individual(
            MapData map,
            int[] chromosome,
            boolean binaryRepresentation) {

        this.binaryRepresentation = binaryRepresentation;
        this.chromosome = chromosome.clone();

        calculateFitness(map);
    }

    /**
     * Memperbaiki kromosom biner pasca-crossover atau mutasi agar jumlah gen
     * bernilai 1 (stasiun aktif) tetap valid sebanyak tepat {@code map.p}.
     *
     * @param chromosome Array kromosom biner yang akan diperbaiki secara in-place.
     * @param map        Objek data peta yang menyimpan target jumlah stasiun ({@code map.p}).
     * @param random     Instans Random untuk memilih gen yang diubah secara acak.
     */
    public static void repairChromosome(
            int[] chromosome, MapData map, Random random) {

        int count = 0;

        for (int i = 0; i < chromosome.length; i++) {
            if (chromosome[i] == 1) {
                count++;
            }
        }

        // Menurunkan gen bernilai 1 menjadi 0 secara acak apabila jumlah stasiun melebihi batas p
        while (count > map.p) {
            int position = random.nextInt(chromosome.length);

            if (chromosome[position] == 1) {
                chromosome[position] = 0;
                count--;
            }
        }

        // Menaikkan gen bernilai 0 menjadi 1 secara acak apabila jumlah stasiun kurang dari batas p
        while (count < map.p) {
            int position = random.nextInt(chromosome.length);

            if (chromosome[position] == 0) {
                chromosome[position] = 1;
                count++;
            }
        }
    }

    /**
     * Menerapkan mutasi pertukaran (swap) pada kromosom biner dengan memindahkan
     * satu stasiun aktif (1) ke satu posisi kosong (0) sehingga jumlah p tetap terjaga.
     *
     * @param random Instans Random untuk memilih posisi gen yang ditukar.
     */
    public void swapMutation(Random random) {

        int position1;
        do {
            position1 = random.nextInt(chromosome.length);
        } while (chromosome[position1] != 1);

        int position2;
        do {
            position2 = random.nextInt(chromosome.length);
        } while (chromosome[position2] != 0);

        chromosome[position1] = 0;
        chromosome[position2] = 1;
    }

    /**
     * Menerapkan mutasi pembalikan bit (bit-flip) pada satu gen acak kromosom biner,
     * diikuti perbaikan jumlah stasiun dan perhitungan ulang nilai fitness.
     *
     * @param random Instans Random untuk memilih indeks gen yang dibalik.
     * @param map    Objek data peta untuk perbaikan kromosom dan evaluasi fitness.
     */
    public void bitFlipMutation(Random random, MapData map) {

        int position = random.nextInt(chromosome.length);

        if (chromosome[position] == 0) {
            chromosome[position] = 1;
        } else {
            chromosome[position] = 0;
        }

        repairChromosome(chromosome, map, random);

        calculateFitness(map);
    }

    /**
     * Menerapkan mutasi random resetting pada kromosom integer dengan mengganti
     * satu lokasi stasiun menggunakan indeks sel kosong baru via rejection sampling,
     * lalu menghitung ulang nilai fitness.
     *
     * @param random Instans Random untuk memilih stasiun dan posisi pengganti.
     * @param map    Objek data peta untuk batas indeks sel kosong dan evaluasi fitness.
     */
    public void randomResettingMutation(Random random, MapData map) {

        int stationIndex = random.nextInt(chromosome.length);

        int newPosition;
        boolean alreadySelected;

        // Mengulang pemilihan posisi acak hingga ditemukan sel kosong yang belum ditempati stasiun lain
        do {
            newPosition = random.nextInt(map.emptyCells.size());
            alreadySelected = false;

            for (int i = 0; i < chromosome.length; i++) {
                if (chromosome[i] == newPosition) {
                    alreadySelected = true;
                    break;
                }
            }
        } while (alreadySelected);

        chromosome[stationIndex] = newPosition;

        calculateFitness(map);
    }

    /**
     * Menerapkan mutasi relokasi pada kromosom integer dengan menyaring seluruh
     * kandidat sel kosong yang belum terpakai terlebih dahulu, memilih satu secara acak,
     * memindahkan stasiun terpilih, dan menghitung ulang nilai fitness.
     *
     * @param random Instans Random untuk memilih stasiun dan posisi tujuan baru.
     * @param map    Objek data peta berisi daftar sel kosong dan matriks jarak.
     */
    public void relocationMutation(Random random, MapData map) {

        int stationIndex = random.nextInt(chromosome.length);

        int[] availablePositions = new int[map.emptyCells.size()];
        int count = 0;

        // Mengumpulkan seluruh indeks sel kosong yang saat ini tidak ditempati oleh stasiun mana pun
        for (int i = 0; i < map.emptyCells.size(); i++) {
            boolean alreadySelected = false;

            for (int j = 0; j < chromosome.length; j++) {
                if (chromosome[j] == i) {
                    alreadySelected = true;
                    break;
                }
            }

            if (!alreadySelected) {
                availablePositions[count] = i;
                count++;
            }
        }

        int newPosition = availablePositions[random.nextInt(count)];

        chromosome[stationIndex] = newPosition;

        calculateFitness(map);
    }
}