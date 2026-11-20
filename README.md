# Tugas1-FireStation
# Fire Station Placement - Genetic Algorithm

## 1. Deskripsi Project

Project ini mengimplementasikan **Genetic Algorithm (GA)** untuk menyelesaikan permasalahan penempatan pos pemadam kebakaran (*Fire Station Placement*).

Tujuannya adalah menempatkan sejumlah pos pemadam kebakaran pada sel kosong sehingga rata-rata jarak setiap rumah ke pos terdekat menjadi sekecil mungkin.

Project membandingkan dua representasi kromosom dan beberapa operator Genetic Algorithm untuk mengetahui kombinasi yang memberikan hasil terbaik.

## 2. Struktur Folder

```text
Tugas1-FireStation/
├── TestCases/
│   ├── Experiment.java
│   ├── GeneticAlgorithm.java
│   ├── Individual.java
│   ├── Main.java
│   ├── MapData.java
│   ├── Population.java
│   ├── README.md
│   └── firestation_input.txt
```

Seluruh file Java berada di dalam folder `TestCases` dan menggunakan package `fireStation`.

## 3. Persyaratan

- Java JDK 17 atau versi yang kompatibel.
- Terminal atau Command Prompt.
- Seluruh file Java berada dalam package `fireStation`.

## 4. Cara Menjalankan Program

Buka terminal pada folder utama project.

### A. Compile Program

```bash
javac TestCases/*.java
```

### B. Menjalankan Pengujian Program

```bash
java -cp TestCases fireStation.Main
```

Program ini digunakan untuk menguji komponen Genetic Algorithm, seperti representasi kromosom, selection, crossover, mutation, dan replacement.

### C. Menjalankan Eksperimen

```bash
java -cp TestCases fireStation.Experiment
```

Program akan menjalankan seluruh kombinasi eksperimen secara otomatis dan menghasilkan dua file CSV:

- `experiment_detail.csv`: menyimpan hasil setiap percobaan.
- `experiment_summary.csv`: menyimpan rekap hasil dari setiap kombinasi operator.

## 5. Konfigurasi Eksperimen

Eksperimen tahap pertama menggunakan konfigurasi berikut:

| Parameter | Nilai |
|---|---:|
| Population Size | 10 |
| Maximum Generations | 100 |
| Crossover Rate | 0.8 |
| Mutation Rate | 0.1 |
| Replacement | Elitism |
| Repetitions | 10 |
| Seed | 1001–1010 |

Replacement menggunakan Elitism agar perbandingan operator dilakukan dengan metode penggantian populasi yang sama.

## 6. Kombinasi Operator yang Diuji

Eksperimen membandingkan dua jenis representasi kromosom.

### A. Binary Representation

- Selection:
  - Tournament Selection
  - Roulette Wheel Selection
- Crossover:
  - Single-Point Crossover
  - Two-Point Crossover
- Mutation:
  - Swap Mutation
  - Bit Flip Mutation

Total: 8 kombinasi.

### B. Integer Representation

- Selection:
  - Tournament Selection
  - Roulette Wheel Selection
- Crossover:
  - One-Point Crossover
  - Two-Point Crossover
- Mutation:
  - Random Resetting Mutation
  - Relocation Mutation

Total: 8 kombinasi.

Dengan demikian, terdapat **16 kombinasi operator**. Setiap kombinasi dijalankan sebanyak 10 kali, sehingga total terdapat **160 percobaan**.

## 7. Hasil Eksperimen

Hasil eksperimen disimpan dalam dua file.

### A. experiment_detail.csv

Berisi hasil setiap percobaan, termasuk:

- Representation
- Selection
- Crossover
- Mutation
- Repetition
- Seed
- BestCost
- RuntimeMs

File ini digunakan untuk melihat hasil masing-masing pengulangan.

### B. experiment_summary.csv

Berisi hasil rekap dari 10 pengulangan untuk setiap kombinasi operator.

Informasi yang disimpan:

- BestCost: biaya terbaik dari seluruh pengulangan.
- AverageCost: rata-rata biaya dari seluruh pengulangan.
- WorstCost: biaya terburuk dari seluruh pengulangan.
- AverageRuntimeMs: rata-rata waktu eksekusi dalam milidetik.

**Catatan:** Semakin kecil nilai Cost, semakin baik solusi yang dihasilkan. Waktu eksekusi yang lebih kecil menunjukkan proses yang lebih cepat.

## 8. Cara Mengulangi Eksperimen

Untuk menjalankan eksperimen kembali:

1. Buka terminal pada folder utama project.
2. Compile ulang seluruh file Java.
3. Jalankan:

```bash
java -cp TestCases fireStation.Experiment
```

4. Tunggu sampai seluruh percobaan selesai.
5. Buka `experiment_detail.csv` untuk melihat hasil setiap percobaan.
6. Buka `experiment_summary.csv` untuk membandingkan seluruh kombinasi operator.

Jika eksperimen dijalankan kembali dengan konfigurasi dan seed yang sama, hasilnya dapat direproduksi karena penggunaan seed tetap.

## 9. Hyperparameter Tuning

Setelah mendapatkan kombinasi operator terbaik dari eksperimen tahap pertama, kombinasi tersebut dapat digunakan sebagai konfigurasi dasar untuk melakukan *hyperparameter tuning*.

Parameter yang dapat diuji antara lain:

| Parameter | Nilai Pengujian |
|---|---|
| Population Size | 10, 20, 50 |
| Maximum Generations | 50, 100, 200 |
| Crossover Rate | 0.6, 0.8, 1.0 |
| Mutation Rate | 0.05, 0.1, 0.2 |

Pengujian dilakukan dengan mengubah satu parameter dalam satu waktu, sedangkan parameter lainnya dipertahankan tetap. Hal ini bertujuan untuk mengamati pengaruh setiap parameter terhadap hasil Genetic Algorithm.

---

**Project:** Fire Station Placement using Genetic Algorithm  
**Language:** Java  
**Experiment Type:** Operator Comparison and Hyperparameter Tuning