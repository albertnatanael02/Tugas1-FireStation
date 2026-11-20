package fireStation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Kelas utilitas terpusat yang menyediakan struktur data, pemuat dataset uji (manifest),
 * definisi model algoritma genetika, serta fungsi eksekusi dan pencatatan statistik
 * untuk mendukung skenario eksperimen tahap 2A dan 2B.
 */
public class ExperimentSupport {

    /** Jumlah pengulangan eksekusi per skenario untuk menjaga validitas statistik. */
    public static final int REPETITIONS = 30;

    /** Nilai seed dasar deterministik untuk membangkitkan urutan bilangan acak yang konsisten. */
    public static final long BASE_SEED = 2026112000L;


    /**
     * Merepresentasikan satu kasus uji peta beserta anggaran evaluasi (evaluation budget)
     * dan objek data peta yang telah dimuat.
     */
    public static class TestCase {
        String size;
        String type;
        String path;
        long evaluationBudget;
        MapData map;

        /**
         * Menginisialisasi kasus uji dan memuat data grid peta dari file eksternal.
         *
         * @param size   Kategori ukuran peta (misalnya "N20", "N40", atau "N60").
         * @param type   Karakteristik tata letak peta ("acak", "klaster", atau "labirin").
         * @param path   Lokasi file teks peta uji.
         * @param budget Batas maksimum jumlah evaluasi fungsi fitness yang diizinkan.
         * @throws Exception Jika terjadi kegagalan saat membaca atau mem-parsing file peta.
         */
        TestCase(String size, String type, String path, long budget) throws Exception {
            this.size = size;
            this.type = type;
            this.path = path;
            this.evaluationBudget = budget;
            this.map = new MapData(path);
        }
    }

    /**
     * Merepresentasikan variasi arsitektur operator Algoritma Genetika (seleksi, crossover, dan mutasi)
     * yang diidentifikasi dengan nama model tertentu.
     */
    public static class Model {
        String name;
        GeneticAlgorithm.SelectionType selection;
        GeneticAlgorithm.CrossoverType crossover;
        GeneticAlgorithm.MutationType mutation;

        /**
         * Membentuk definisi kombinasi operator untuk satu model Algoritma Genetika.
         *
         * @param name      Label identitas model (misalnya "M1", "M2", atau "M3").
         * @param selection Strategi seleksi induk yang digunakan.
         * @param crossover Operator rekombinasi kromosom yang digunakan.
         * @param mutation  Operator mutasi gen yang digunakan.
         */
        Model(String name, GeneticAlgorithm.SelectionType selection,
              GeneticAlgorithm.CrossoverType crossover,
              GeneticAlgorithm.MutationType mutation) {
            this.name = name;
            this.selection = selection;
            this.crossover = crossover;
            this.mutation = mutation;
        }
    }

    /**
     * Menyimpan kumpulan parameter numerik (hyperparameter) untuk satu konfigurasi
     * eksekusi Algoritma Genetika.
     */
    public static class Config {
        int population;
        double crossover;
        double mutation;
        double elitism;

        /**
         * Menginisialisasi konfigurasi hyperparameter Algoritma Genetika.
         *
         * @param population Jumlah individu dalam satu populasi.
         * @param crossover  Probabilitas terjadinya crossover antar induk.
         * @param mutation   Probabilitas terjadinya mutasi pada individu keturunan.
         * @param elitism    Persentase atau proporsi individu elit yang dipertahankan antar generasi.
         */
        Config(int population, double crossover, double mutation, double elitism) {
            this.population = population;
            this.crossover = crossover;
            this.mutation = mutation;
            this.elitism = elitism;
        }
    }

    /**
     * Akumulator statistik performa eksekusi (nilai cost terbaik, terburuk, rata-rata,
     * serta durasi komputasi) dari serangkaian pengulangan pengujian.
     */
    public static class Stats {
        long runs;
        double totalCost;
        double bestCost = Double.MAX_VALUE;
        double worstCost = Double.NEGATIVE_INFINITY;
        double totalRuntime;

        /**
         * Menambahkan hasil metrik dari satu kali eksekusi ke dalam akumulator statistik.
         *
         * @param cost    Nilai cost solusi terbaik yang dicapai pada eksekusi tersebut.
         * @param runtime Durasi waktu eksekusi algoritma dalam satuan milidetik (ms).
         */
        void add(double cost, double runtime) {
            runs++;
            totalCost += cost;
            totalRuntime += runtime;
            if (cost < bestCost) bestCost = cost;
            if (cost > worstCost) worstCost = cost;
        }

        /**
         * Menghitung rata-rata nilai cost dari seluruh eksekusi yang tercatat.
         *
         * @return Nilai rata-rata cost.
         */
        double averageCost() { return totalCost / runs; }

        /**
         * Menghitung rata-rata waktu eksekusi dari seluruh pengujian yang tercatat.
         *
         * @return Nilai rata-rata waktu eksekusi dalam milidetik (ms).
         */
        double averageRuntime() { return totalRuntime / runs; }
    }

    /**
     * Mengembalikan daftar tiga varian model Algoritma Genetika (M1, M2, dan M3)
     * yang menjadi objek pembanding utama dalam eksperimen.
     *
     * @return Array berisi objek {@link Model} dengan kombinasi operator yang telah ditetapkan.
     */
    public static Model[] getModels() {
        return new Model[] {
            new Model("M1", GeneticAlgorithm.SelectionType.ROULETTE,
                GeneticAlgorithm.CrossoverType.SINGLE_POINT,
                GeneticAlgorithm.MutationType.SWAP),
            new Model("M2", GeneticAlgorithm.SelectionType.ROULETTE,
                GeneticAlgorithm.CrossoverType.TWO_POINT,
                GeneticAlgorithm.MutationType.SWAP),
            new Model("M3", GeneticAlgorithm.SelectionType.ROULETTE,
                GeneticAlgorithm.CrossoverType.SINGLE_POINT,
                GeneticAlgorithm.MutationType.BIT_FLIP)
        };
    }

    /**
     * Membaca dan memvalidasi daftar skenario peta uji dari file manifest CSV berformat
     * {@code Size,Type,Path,EvaluationBudget}. Memastikan komposisi peta lengkap sebanyak
     * 12 kasus uji dengan distribusi ukuran dan tipe yang seimbang.
     *
     * @param manifest Path menuju file manifest CSV.
     * @return Daftar objek {@link TestCase} yang siap dijalankan.
     * @throws Exception Jika format file tidak valid, nilai anggaran tidak positif, atau komposisi peta tidak sesuai aturan.
     */
    public static List<TestCase> loadTestCases(String manifest) throws Exception {
        List<TestCase> cases = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(manifest));
        String line;
        int lineNumber = 0;
        // Matriks penghitung distribusi kasus uji dengan dimensi [ukuran: N20/N40/N60][tipe: acak/klaster/labirin]
        int[][] counts = new int[3][3];

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("Size,")) continue;
            String[] parts = line.split(",", -1);
            if (parts.length != 4) {
                reader.close();
                throw new IllegalArgumentException("Format manifest salah pada baris " + lineNumber);
            }

            String size = parts[0].trim().toUpperCase();
            String type = parts[1].trim().toLowerCase();
            String path = parts[2].trim();
            long budget = Long.parseLong(parts[3].trim());
            if (budget <= 0) throw new IllegalArgumentException("Evaluation budget harus positif: " + path);

            // Memetakan label ukuran dan karakteristik peta ke indeks matriks validasi
            int sizeIndex = size.equals("N20") ? 0 : size.equals("N40") ? 1 : size.equals("N60") ? 2 : -1;
            int typeIndex = type.equals("acak") ? 0 : type.equals("cluster") || type.equals("klaster") ? 1 : type.equals("labirin") || type.equals("maze") ? 2 : -1;
            if (sizeIndex < 0 || typeIndex < 0) {
                throw new IllegalArgumentException("Size/type tidak dikenal pada baris " + lineNumber + ": " + line);
            }
            counts[sizeIndex][typeIndex]++;
            cases.add(new TestCase(size, type, path, budget));
        }
        reader.close();

        // Memvalidasi total 12 peta dan komposisi wajib tiap ukuran (2 acak, 1 klaster, 1 labirin)
        if (cases.size() != 12) {
            throw new IllegalArgumentException("Manifest harus berisi 12 peta (4 per ukuran). Ditemukan: " + cases.size());
        }
        for (int s = 0; s < 3; s++) {
            if (counts[s][0] != 2 || counts[s][1] != 1 || counts[s][2] != 1) {
                throw new IllegalArgumentException("Setiap ukuran wajib memiliki 2 acak, 1 cluster, dan 1 labirin.");
            }
        }
        return cases;
    }

    /**
     * Menghitung jumlah maksimum generasi evolusi yang dapat dijalankan berdasarkan
     * anggaran total evaluasi fitness dan ukuran populasi, dengan memperhitungkan
     * evaluasi awal populasi (sebanyak {@code population} evaluasi).
     *
     * @param budget     Total anggaran evaluasi fungsi fitness yang tersedia.
     * @param population Jumlah individu dalam satu generasi populasi.
     * @return Jumlah iterasi generasi yang diizinkan.
     * @throws IllegalArgumentException Jika anggaran evaluasi tidak mencukupi minimal 1 generasi.
     */
    public static int generationsFromBudget(long budget, int population) {
        long generations = budget / population - 1;
        if (generations < 1) {
            throw new IllegalArgumentException("Budget " + budget + " terlalu kecil untuk population " + population);
        }
        if (generations > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) generations;
    }

    /**
     * Menghasilkan nilai seed deterministik untuk indeks repetisi tertentu agar
     * urutan pembangkitan bilangan acak bersifat konsisten dan dapat direproduksi.
     *
     * @param repetition Nomor urut pengulangan eksekusi (dimulai dari 1).
     * @return Nilai seed unik bertipe {@code long} untuk repetisi tersebut.
     */
    public static long seedForRepetition(int repetition) {
        return BASE_SEED + repetition;
    }

    /**
     * Mengeksekusi satu konfigurasi model dan parameter pada sebuah kasus uji sebanyak
     * {@link #REPETITIONS} kali, mencatat log setiap pengulangan ke file CSV detail,
     * dan mengembalikan statistik agregatnya.
     *
     * @param testCase       Objek kasus uji peta yang dievaluasi.
     * @param model          Model Algoritma Genetika yang dijalankan.
     * @param config         Konfigurasi hyperparameter yang diterapkan.
     * @param detail         Writer untuk menulis catatan mentah setiap repetisi ke CSV.
     * @param experimentName Nama identitas tahap eksperimen (misalnya "2A" atau "2B").
     * @param parameterName  Nama parameter yang sedang diuji dalam eksperimen.
     * @param parameterValue Nilai parameter yang sedang diuji dalam bentuk string.
     * @return Objek {@link Stats} yang merangkum hasil dari seluruh repetisi.
     * @throws Exception Jika terjadi kesalahan selama eksekusi GA atau penulisan file CSV.
     */
    public static Stats runConfiguration(TestCase testCase, Model model, Config config,
                                         BufferedWriter detail, String experimentName,
                                         String parameterName, String parameterValue) throws Exception {
        Stats stats = new Stats();
        int generations = generationsFromBudget(testCase.evaluationBudget, config.population);

        // Menjalankan pengujian berulang dengan seed berbeda per repetisi untuk memperoleh distribusi hasil
        for (int repetition = 1; repetition <= REPETITIONS; repetition++) {
            long seed = seedForRepetition(repetition);
            Random random = new Random(seed);

            GeneticAlgorithm ga = new GeneticAlgorithm(
                testCase.map,
                true,
                model.selection,
                model.crossover,
                model.mutation,
                GeneticAlgorithm.ReplacementType.ELITISM,
                config.population,
                generations,
                config.crossover,
                config.mutation,
                config.elitism,
                random
            );

            // Mengukur waktu komputasi algoritma dalam nanodetik dan mengonversinya ke milidetik
            long start = System.nanoTime();
            Individual result = ga.run(false);
            long end = System.nanoTime();

            double runtime = (end - start) / 1_000_000.0;
            stats.add(result.cost, runtime);

            detail.write(experimentName + "," + testCase.size + "," + testCase.type + "," +
                testCase.path + "," + model.name + "," + model.selection + "," +
                model.crossover + "," + model.mutation + "," + parameterName + "," +
                parameterValue + "," + config.population + "," + config.crossover + "," +
                config.mutation + "," + config.elitism + "," + generations + "," +
                repetition + "," + seed + "," + result.cost + "," + runtime);
            detail.newLine();
        }
        return stats;
    }

    /**
     * Menuliskan baris header kolom standar ke dalam file CSV laporan detail eksperimen.
     *
     * @param writer Writer target yang terhubung ke file CSV detail.
     * @throws Exception Jika terjadi kesalahan penulisan ke stream file.
     */
    public static void writeDetailHeader(BufferedWriter writer) throws Exception {
        writer.write("Experiment,Size,Type,TestCase,Model,Selection,Crossover,Mutation,TestedParameter,TestedValue," +
            "Population,CrossoverRate,MutationRate,ElitismRate,Generations,Repetition,Seed,BestCost,RuntimeMs");
        writer.newLine();
    }

    /**
     * Mengamankan nilai string agar sesuai standar format CSV dengan mengapitnya
     * menggunakan tanda kutip ganda dan melakukan escaping pada tanda kutip internal.
     *
     * @param value String mentah yang akan diformat.
     * @return String yang telah aman untuk ditulis sebagai sel CSV.
     */
    public static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}