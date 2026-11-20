import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class MapData {

    // Ukuran peta dan jumlah fire station
    int m, n, p;

    // Menyimpan isi peta
    char[][] grid;

    // Menyimpan posisi sel kosong dan rumah
    ArrayList<int[]> emptyCells;
    ArrayList<int[]> houses;

    // Menyimpan jarak setiap rumah ke setiap sel kosong
    int[][] distances;

    public MapData(String fileName) throws FileNotFoundException {

        Scanner sc = new Scanner(new File(fileName));

        // Membaca ukuran peta dan jumlah fire station
        m = sc.nextInt();
        n = sc.nextInt();
        p = sc.nextInt();

        sc.nextLine();

        grid = new char[m][n];
        emptyCells = new ArrayList<>();
        houses = new ArrayList<>();

        // Membaca isi peta
        for (int i = 0; i < m; i++) {

            String line = sc.nextLine();

            for (int j = 0; j < n; j++) {

                grid[i][j] = line.charAt(j);

                // Mencatat posisi sel kosong
                if (grid[i][j] == '.') {
                    emptyCells.add(new int[]{i, j});
                }

                // Mencatat posisi rumah
                else if (grid[i][j] == 'H') {
                    houses.add(new int[]{i, j});
                }
            }
        }

        sc.close();

        // Menghitung jarak menggunakan BFS
        calculateDistances();
    }

    private void calculateDistances() {

        distances = new int[houses.size()][emptyCells.size()];

        // BFS dilakukan untuk setiap rumah
        for (int h = 0; h < houses.size(); h++) {

            int[][] distanceMap = new int[m][n];

            // -1 berarti belum dikunjungi
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

            // Arah pergerakan: atas, bawah, kiri, kanan
            int[] dx = {-1, 1, 0, 0};
            int[] dy = {0, 0, -1, 1};

            while (!queue.isEmpty()) {

                int[] current = queue.poll();

                int x = current[0];
                int y = current[1];

                // Memeriksa empat arah
                for (int d = 0; d < 4; d++) {

                    int nx = x + dx[d];
                    int ny = y + dy[d];

                    // Memastikan posisi masih di dalam peta
                    if (nx < 0 || nx >= m || ny < 0 || ny >= n) {
                        continue;
                    }

                    // Hanya sel kosong yang boleh dilewati
                    if (grid[nx][ny] != '.') {
                        continue;
                    }

                    // Memastikan sel belum pernah dikunjungi
                    if (distanceMap[nx][ny] != -1) {
                        continue;
                    }

                    distanceMap[nx][ny] = distanceMap[x][y] + 1;

                    queue.add(new int[]{nx, ny});
                }
            }

            // Memindahkan hasil BFS ke matriks distances
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
