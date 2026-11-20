PANDUAN EKSPERIMEN TAHAP 2

1. STRUKTUR FOLDER
   Letakkan folder fireStation dan file stage2_cases.csv pada root project.
   Folder TestCases harus berada pada root project juga.

2. ISI stage2_cases.csv
   File ZIP awal belum berisi folder TestCases, sehingga path pada manifest adalah TEMPLATE.
   Ganti setiap path dengan file input Tahap 2 yang benar-benar tersedia dan berbeda dari Tahap 1.
   Harus ada tepat 12 peta:
   - N20: 2 acak, 1 cluster, 1 labirin
   - N40: 2 acak, 1 cluster, 1 labirin
   - N60: 2 acak, 1 cluster, 1 labirin

   Evaluation budget:
   - N20: 50.000 (mengacu Small 20x20 pada tabel tugas)
   - N40: 200.000 (mengacu Normal 40x40 pada tabel tugas)
   - N60: WAJIB diisi sendiri, karena tabel tugas tidak mencantumkan ukuran 60x60.
     Ganti ISI_BUDGET_N60 pada empat baris N60 dengan budget yang disepakati kelompok/dosen.

3. NILAI HYPERPARAMETER YANG DICOBA DI EXPERIMENT2A
   Population: 50, 100, 250, 500, 1000
   Crossover rate: 0.5, 0.6, 0.7, 0.9, 1.0
   Mutation rate: 0.01, 0.03, 0.05, 0.15, 0.20
   Elitism rate: 0.05, 0.15, 0.20, 0.25, 0.30

   Baseline Tahap 1 yang dipakai saat parameter lain belum dituning:
   Population=10, Crossover=0.8, Mutation=0.1, Elitism=0.10.
   Nilai kandidat di atas adalah usulan operasional karena instruksi tugas tidak menentukan angka persis.

4. EVALUATION BUDGET
   Program menghitung generasi dengan rumus:
   Generations = (EvaluationBudget / Population) - 1
   Alasannya: populasi awal membutuhkan Population evaluasi, lalu setiap generasi menghasilkan
   Population offspring. Dengan demikian total evaluasi kira-kira Population*(Generations+1)
   dan tidak melampaui budget.

5. CARA COMPILE DAN RUN
   Jalankan PowerShell dari root project (folder yang berisi folder fireStation):

   javac fireStation\*.java
   java fireStation.Experiment2A
   java fireStation.Experiment2B

   Experiment2B harus dijalankan setelah Experiment2A selesai karena membaca:
   experiment2a_best_config.csv

6. OUTPUT
   Experiment2A:
   - experiment2a_detail.csv
   - experiment2a_summary.csv
   - experiment2a_best_config.csv

   Experiment2B:
   - experiment2b_detail.csv
   - experiment2b_summary.csv

   Detail CSV mencatat nama file peta, model, seluruh hyperparameter, generasi,
   repetisi, seed, best cost, dan runtime.

7. MODEL YANG DIBAWA DARI TAHAP 1
   M1: Binary + Roulette + Single Point + Swap
   M2: Binary + Roulette + Two Point + Swap
   M3: Binary + Roulette + Single Point + Bit Flip

8. PEMILIHAN KONFIGURASI
   Untuk setiap model, kandidat diperingkat berdasarkan Average Cost gabungan seluruh 12 peta
   dan 30 repetisi per peta. Average Cost terendah dipilih.
   Experiment2B mengambil dua population dan dua crossover dengan Average Cost terendah
   dari hasil 2A per model; mutation dan elitism memakai nilai terbaik 2A per model.

CATATAN PENTING
- Jangan jalankan sebelum path manifest benar-benar diganti dengan file yang tersedia.
- Jangan gunakan kembali test case Tahap 1.
- Seed dibuat berbeda untuk setiap run dan dicatat di CSV.
- Runtime dipengaruhi spesifikasi komputer dan kondisi JVM.
- Rumus total eksperimen yang tertulis di instruksi perlu dicocokkan lagi dengan jumlah test case.
  Implementasi ini menjalankan 12 peta x 30 repetisi untuk setiap konfigurasi.
