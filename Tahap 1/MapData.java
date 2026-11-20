import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * Merepresentasikan data lingkungan peta masalah penempatan stasiun pemadam kebakaran.
 * Bertanggung jawab membaca konfigurasi grid dari file input, memetakan koordinat rumah
 * serta kandidat sel kosong, dan melakukan pra-komputasi matriks jarak terpendek antar sel
 * menggunakan algoritma Breadth-First Search (BFS).
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
     * Memuat dimensi dan tata letak peta dari file teks eksternal, mengelompokkan
     * posisi rumah dan sel kosong, lalu langsung menghitung matriks jarak terpendeknya.
     *
     * @param fileName Path atau nama file teks yang berisi definisi peta masalah.
     * @throws FileNotFoundException Jika file peta yang ditentukan tidak ditemukan.
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

        // Memindai setiap sel peta untuk memisahkan koordinat kandidat lokasi ('.') dan rumah ('H')
        for (int i = 0; i < m; i++) {

            String line = sc.nextLine();

            for (int j = 0; j < n; j++) {

                grid[i][j] = line.charAt(j);

                if (grid[i][j] == '.') {
                    emptyCells.add(new int[]{i, j});
                }

                else if (grid[i][j] == 'H') {
                    houses.add(new int[]{i, j});
                }
            }
        }

        sc.close();

        // Melakukan pra-komputasi jarak agar evaluasi fitness GA berjalan dalam waktu konstan O(1)
        calculateDistances();
    }

    /**
     * Menghitung jarak lintasan terpendek (jarak Manhattan berbasis grid dengan rintangan)
     * dari setiap rumah menuju seluruh sel kosong menggunakan algoritma Breadth-First Search (BFS).
     * Sel yang tidak dapat dijangkau akan diberi nilai penalti sebesar (m * n).
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

                // Mengeksplorasi tetangga pada keempat arah pergerakan yang valid
                for (int d = 0; d < 4; d++) {

                    int nx = x + dx[d];
                    int ny = y + dy[d];

                    // Melewati sel jika berada di luar batas grid peta
                    if (nx < 0 || nx >= m || ny < 0 || ny >= n) {
                        continue;
                    }

                    // Membatasi penelusuran hanya melalui jalur sel kosong ('.') yang dapat dilalui
                    if (grid[nx][ny] != '.') {
                        continue;
                    }

                    // Melewati sel yang sudah memiliki catatan jarak terpendek sebelumnya
                    if (distanceMap[nx][ny] != -1) {
                        continue;
                    }

                    distanceMap[nx][ny] = distanceMap[x][y] + 1;

                    queue.add(new int[]{nx, ny});
                }
            }

            // Memetakan hasil jarak BFS ke matriks distances; beri penalti (m * n) jika sel terisolasi
            for (int e = 0; e < emptyCells.size(); e++) {

                int x = emptyCells.get(e)[0];
                int y = emptyCells.get(e)[1];

                if (distanceMap[x][y] == -1) {
                    distances[h][e] = m * n;
                } else {
                    distances[h][e] = distanceMap[x][y];
                }
            }
        }
    }
}