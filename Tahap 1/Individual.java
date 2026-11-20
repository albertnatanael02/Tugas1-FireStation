import java.util.Random;

public class Individual {

    // Menentukan representasi kromosom yang digunakan
    boolean binaryRepresentation;

    // Menyimpan kromosom
    int[] chromosome;

    // Menyimpan nilai cost dan fitness
    double cost;
    double fitness;

    // Constructor untuk membuat individu baru
    public Individual(MapData map, boolean binaryRepresentation, Random random) {

        this.binaryRepresentation = binaryRepresentation;

        // Membuat kromosom berdasarkan representasi
        if (binaryRepresentation) {

            // Binary: satu gen untuk setiap sel kosong
            chromosome = new int[map.emptyCells.size()];

            // Memilih tepat p lokasi fire station
            int count = 0;

            while (count < map.p) {

                int position = random.nextInt(chromosome.length);

                // Memastikan lokasi belum dipilih
                if (chromosome[position] == 0) {
                    chromosome[position] = 1;
                    count++;
                }
            }

        } else {

            // Integer: satu gen untuk setiap fire station
            chromosome = new int[map.p];

            int count = 0;

            while (count < map.p) {

                int position = random.nextInt(map.emptyCells.size());

                boolean alreadySelected = false;

                // Memeriksa apakah lokasi sudah dipilih
                for (int i = 0; i < count; i++) {
                    if (chromosome[i] == position) {
                        alreadySelected = true;
                        break;
                    }
                }

                // Menambahkan lokasi jika belum dipilih
                if (!alreadySelected) {
                    chromosome[count] = position;
                    count++;
                }
            }
        }

        // Menghitung cost dan fitness individu
        calculateFitness(map);
    }

    // Menghitung cost dan fitness
    public void calculateFitness(MapData map) {

        double totalDistance = 0;

        // Menghitung jarak untuk setiap rumah
        for (int h = 0; h < map.houses.size(); h++) {

            int minimumDistance = map.m * map.n;

            // Memeriksa semua lokasi fire station
            for (int i = 0; i < chromosome.length; i++) {

                int position;

                // Mengambil lokasi yang dipilih
                if (binaryRepresentation) {

                    // Hanya gen bernilai 1 yang merupakan fire station
                    if (chromosome[i] == 0) {
                        continue;
                    }

                    position = i;

                } else {

                    // Integer langsung menyimpan indeks lokasi
                    position = chromosome[i];
                }

                // Mengambil jarak dari rumah ke fire station
                int distance = map.distances[h][position];

                // Mencari jarak paling dekat
                if (distance < minimumDistance) {
                    minimumDistance = distance;
                }
            }

            totalDistance += minimumDistance;
        }

        // Menghitung rata-rata jarak
        cost = totalDistance / map.houses.size();

        // Fitness semakin besar jika cost semakin kecil
        fitness = 1.0 / (1.0 + cost);
    }


    public static Individual createChild(
        MapData map,
        int[] chromosome,
        boolean binaryRepresentation) {

    return new Individual(
        map, chromosome, binaryRepresentation
    );
}

    // Constructor khusus untuk membuat child dari kromosom yang sudah tersedia
    private Individual(
            MapData map,
            int[] chromosome,
            boolean binaryRepresentation) {

        this.binaryRepresentation = binaryRepresentation;
        this.chromosome = chromosome.clone();

        calculateFitness(map);
    }

    // Memperbaiki jumlah Fire Station agar tetap p
    public static void repairChromosome(
            int[] chromosome, MapData map, Random random) {

        int count = 0;

        // Menghitung jumlah Fire Station
        for (int i = 0; i < chromosome.length; i++) {
            if (chromosome[i] == 1) {
                count++;
            }
        }

        // Jika jumlahnya terlalu banyak, hapus secara acak
        while (count > map.p) {
            int position = random.nextInt(chromosome.length);

            if (chromosome[position] == 1) {
                chromosome[position] = 0;
                count--;
            }
        }

        // Jika jumlahnya kurang, tambahkan di lokasi kosong
        while (count < map.p) {
            int position = random.nextInt(chromosome.length);

            if (chromosome[position] == 0) {
                chromosome[position] = 1;
                count++;
            }
        }
    }

    public void swapMutation(Random random) {

        // Mencari posisi fire station (1)
        int position1;
        do {
            position1 = random.nextInt(chromosome.length);
        } while (chromosome[position1] != 1);

        // Mencari posisi sel kosong (0)
        int position2;
        do {
            position2 = random.nextInt(chromosome.length);
        } while (chromosome[position2] != 0);

        // Menukar posisi 1 dan 0
        chromosome[position1] = 0;
        chromosome[position2] = 1;
    }

    public void bitFlipMutation(Random random, MapData map) {

        // Memilih satu posisi secara acak
        int position = random.nextInt(chromosome.length);

        // Mengubah 0 menjadi 1 atau 1 menjadi 0
        if (chromosome[position] == 0) {
            chromosome[position] = 1;
        } else {
            chromosome[position] = 0;
        }

        // Memperbaiki kromosom agar jumlah fire station tetap p
        repairChromosome(chromosome, map, random);

        // Menghitung ulang cost dan fitness
        calculateFitness(map);
    }

    public void randomResettingMutation(Random random, MapData map) {

        // Memilih fire station yang akan dipindahkan
        int stationIndex = random.nextInt(chromosome.length);

        // Mencari posisi baru yang belum digunakan
        int newPosition;
        boolean alreadySelected;

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

        // Mengganti posisi fire station
        chromosome[stationIndex] = newPosition;

        // Menghitung ulang cost dan fitness
        calculateFitness(map);
    }

    public void relocationMutation(Random random, MapData map) {

        // Memilih fire station yang akan dipindahkan
        int stationIndex = random.nextInt(chromosome.length);

        // Membuat daftar posisi yang belum ditempati
        int[] availablePositions = new int[map.emptyCells.size()];
        int count = 0;

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

        // Memilih salah satu posisi yang tersedia
        int newPosition = availablePositions[random.nextInt(count)];

        // Memindahkan fire station
        chromosome[stationIndex] = newPosition;

        // Menghitung ulang cost dan fitness
        calculateFitness(map);
    }
}

