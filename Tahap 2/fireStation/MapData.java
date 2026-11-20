package fireStation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class MapData {

    int m, n, p;
    char[][] grid;
    ArrayList<int[]> emptyCells;
    ArrayList<int[]> houses;
    int[][] distances;

    public MapData(String fileName) throws FileNotFoundException {

        // Membaca file sesuai path yang diberikan oleh pemanggil.
        Scanner sc = new Scanner(new File(fileName));

        m = sc.nextInt();
        n = sc.nextInt();
        p = sc.nextInt();
        sc.nextLine();

        grid = new char[m][n];
        emptyCells = new ArrayList<>();
        houses = new ArrayList<>();

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

        if (p <= 0 || p > emptyCells.size()) {
            throw new IllegalArgumentException("Jumlah fire station p tidak valid: " + p);
        }
        if (houses.isEmpty()) {
            throw new IllegalArgumentException("Peta tidak memiliki rumah (H): " + fileName);
        }

        calculateDistances();
    }

    private void calculateDistances() {
        distances = new int[houses.size()][emptyCells.size()];

        for (int h = 0; h < houses.size(); h++) {
            int[][] distanceMap = new int[m][n];
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

            int[] dx = {-1, 1, 0, 0};
            int[] dy = {0, 0, -1, 1};

            while (!queue.isEmpty()) {
                int[] current = queue.poll();
                int x = current[0];
                int y = current[1];

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

            for (int e = 0; e < emptyCells.size(); e++) {
                int x = emptyCells.get(e)[0];
                int y = emptyCells.get(e)[1];
                distances[h][e] = distanceMap[x][y] == -1 ? m * n : distanceMap[x][y];
            }
        }
    }
}
