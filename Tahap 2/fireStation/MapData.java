package fireStation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * Merepresentasikan data lingkungan peta untuk masalah optimasi penempatan stasiun
 * pemadam kebakaran (fire station). Bertanggung jawab membaca dan memvalidasi tata letak
 * grid dari file input, memetakan posisi rumah serta sel kosong, dan melakukan pra-komputasi
 * matriks jarak lintasan terpendek menggunakan algoritma Breadth-First Search (BFS).
 */
public class MapData {

    /** Dimensi baris (m), dimensi kolom (n), dan target jumlah stasiun pemadam kebakaran (p). */
    int m, n, p;

    /** Representasi karakter dua dimensi dari tata letak peta ('.' = kosong, 'H' = rumah, dll.). */
    char[][] grid;

    /** Daftar koordinat [baris, kolom] untuk seluruh sel kosong (kandidat lokasi) dan rumah. */
    ArrayList<int[]> emptyCells;
    ArrayList<int[]> houses;

    /** Matriks jarak terpendek pre-computed dengan ukuran [jumlah_rumah][jumlah_sel_kosong]. */
    int[][] distances;

    /**
     * Memuat dimensi dan tata letak peta dari file teks eksternal, memvalidasi format
     * grid serta batasan parameter (jumlah stasiun dan keberadaan rumah), lalu langsung
     * menghitung matriks jarak terpendek antar sel.
     *
     * @param fileName Path menuju file teks yang berisi definisi peta masalah.
     * @throws FileNotFoundException    Jika file peta pada path tersebut tidak ditemukan.
     * @throws IllegalArgumentException Jika panjang baris grid tidak sesuai kolom {@code n}, nilai {@code p} tidak valid, atau tidak ada rumah pada peta.
     */
    public MapData(String fileName) throws FileNotFoundException {

        Scanner sc = new Scanner(new File(fileName));

        // Membaca parameter dimensi grid (m x n) serta jumlah stasiun (p) pada baris pertama
        m = sc.nextInt();
        n = sc.nextInt();
        p = sc.nextInt();
        sc.nextLine();

        grid = new char[m][n];
        emptyCells = new ArrayList<>();
        houses = new ArrayList<>();

        // Memvalidasi panjang tiap baris dan mengelompokkan koordinat sel kosong ('.') serta rumah ('H')
        for (int i = 0; i < m; i++) {
            String line = sc.nextLine().trim();
            if (line.length() != n) {
                sc.close();
                throw new IllegalArgumentException(
                    "Jumlah karakter baris " + (i + 1) + " tidak sesuai dengan n=" + n
                );
            }

            for (int j = 0; j < n; j++) {
                grid[i][j] = line.charAt(j);
                if (grid[i][j] == '.') {
                    emptyCells.add(new int[]{i, j});
                } else if (grid[i][j] == 'H') {
                    houses.add(new int[]{i, j});
                }
            }
        }
        sc.close();

        // Memastikan jumlah stasiun (p) masuk akal terhadap kapasitas sel kosong dan peta memiliki target rumah
        if (p <= 0 || p > emptyCells.size()) {
            throw new IllegalArgumentException("Jumlah fire station p tidak valid: " + p);
        }
        if (houses.isEmpty()) {
            throw new IllegalArgumentException("Peta tidak memiliki rumah (H): " + fileName);
        }

        // Melakukan pra-komputasi jarak agar evaluasi fitness GA berjalan dalam waktu konstan O(1)
        calculateDistances();
    }

    /**
     * Menghitung jarak lintasan terpendek (jarak Manhattan berbasis grid dengan rintangan)
     * dari setiap rumah menuju seluruh sel kosong menggunakan Breadth-First Search (BFS).
     * Sel kosong yang terisolasi atau tidak dapat dijangkau akan diberi penalti jarak sebesar {@code m * n}.
     */
    private void calculateDistances() {
        distances = new int[houses.size()][emptyCells.size()];

        // Menjalankan penelusuran BFS independen dengan titik awal masing-masing rumah
        for (int h = 0; h < houses.size(); h++) {
            int[][] distanceMap = new int[m][n];
            // Menginisialisasi seluruh sel sebagai belum dikunjungi (-1)
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    distanceMap[i][j] = -1;
                }
            }

            Queue<int[]> queue = new LinkedList<>();
            int startX = houses.get(h)[0];
            int startY = houses.get(h)[1];
            queue.add(new int[]{startX, startY});
            distanceMap[startX][startY] = 0;

            // Vektor pergeseran 4-arah ortogonal: atas, bawah, kiri, dan kanan
            int[] dx = {-1, 1, 0, 0};
            int[] dy = {0, 0, -1, 1};

            while (!queue.isEmpty()) {
                int[] current = queue.poll();
                int x = current[0];
                int y = current[1];

                // Mengeksplorasi tetangga pada keempat arah pergerakan yang valid dan belum dikunjungi
                for (int d = 0; d < 4; d++) {
                    int nx = x + dx[d];
                    int ny = y + dy[d];
                    if (nx < 0 || nx >= m || ny < 0 || ny >= n) continue;
                    if (grid[nx][ny] != '.') continue;
                    if (distanceMap[nx][ny] != -1) continue;

                    distanceMap[nx][ny] = distanceMap[x][y] + 1;
                    queue.add(new int[]{nx, ny});
                }
            }

            // Menyalin hasil jarak BFS ke matriks distances dengan nilai penalti (m * n) untuk sel tak terjangkau
            for (int e = 0; e < emptyCells.size(); e++) {
                int x = emptyCells.get(e)[0];
                int y = emptyCells.get(e)[1];
                distances[h][e] = distanceMap[x][y] == -1 ? m * n : distanceMap[x][y];
            }
        }
    }
}