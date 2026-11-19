
//  import java.util.ArrayList;
// import java.util.Random;
// import java.util.stream.Collectors;

// public class Individual  implements Comparable<Individual>{
//     public int chromosome;	//kromosom adalah array of bit, integer diperlakukan seperti array berisi (bit) 0/1
//     public int fitness;		//nilai fitnessnya
//     public Random MyRand;	//random generator dikirim dari luar untuk membuat invididu acal
//     public double parentProbability;	//probabilitas individu ini terpilih sbg parent

// 	//membuat individu acak
//     public Individual(Random MyRand) {	
//         this.MyRand = MyRand;
//         this.chromosome = this.MyRand.nextInt();
// 		//System.out.println(chromosome);
//         this.fitness = 0;
//         this.parentProbability = 0;
//     }

// 	//membuat individu baru berdasarkan kromosom dari luar
//     public Individual(Random MyRand, int chromosome) {
//         this.MyRand = MyRand;
//         this.chromosome = chromosome;
//         this.fitness = 0;
//         this.parentProbability = 0;
//     }

// 	//menghitung fitness dengan masukan list of item dan kapasitas knapsack
//     public int setFitness(ArrayList<Item> listOfItems,int maxCapacity) {	
//         int value=0;
//         int weight=0;
// 		//baca setiap bit
//         for (int i=0;i<Integer.SIZE;i++) {
//             int bit = this.chromosome&(1<<i);	//dengan operasi bitwise
//             if (bit!=0) {	//bit 1 berarti barang diambil, 0 tidak diambil
//                 value = value + listOfItems.get(i).value;
//                 weight = weight + listOfItems.get(i).weight;
//             }
//         }
//         //System.out.println(value+" "+weight);
//         if (maxCapacity<weight) this.fitness = -1;	//jika melebihi kapasitas fitness = -1
//         else this.fitness = value;
//         return this.fitness;
//     }

//     public void doMutation() {
// 		//single mutation, 1 bit diubah 0 ke 1 atau 1 ke 0
//         //int pos = this.MyRand.nextInt(32);
//         //this.chromosome = this.chromosome ^ (1<<pos);
//         //this.chromosome = this.chromosome + this.MyRand.nextInt();
//         this.chromosome = ~this.chromosome;		//flip-bit mutation
//     }

// 	//single point crossover
// 	//di sini hanya menghasilkan satu anak, crossover harusnya menghasilkan dua anak
// 	//kemudian pilihannya bisa diambil anak terbaik saja, atau kedua anak masuk ke dalam populasi berikutnya
//     public Individual doCrossover(Individual other) { 
//         Individual child = new Individual(this.MyRand,0);
//         int pos = this.MyRand.nextInt(17)+7;
//         //System.out.println(pos);
//         for (int i=0;i<=pos;i++) {
//             child.chromosome = child.chromosome + (this.chromosome & (1<<i));
//         }
//         for (int i=pos+1;i<Integer.SIZE;i++) {
//             child.chromosome = child.chromosome + (other.chromosome & (1<<i));
//         }
//         //System.out.println(this);
//         //System.out.println(other);
//         //System.out.println(child);
//         //System.out.println("-----");
//         return child;
//     }

//     /*public Individual doCrossover(Individual other) {	//two points crossover
//         Individual child1 = new Individual(this.MyRand,0);
//         Individual child2 = new Individual(this.MyRand,0);
//         int rd1=3, rd2=28;
//         do {
//         	rd1 = this.MyRand.nextInt(28)+2;
//         	rd2 = this.MyRand.nextInt(28)+2;
//         } while(Math.abs(rd1-rd2)<=2);
//         int pos1 = Math.min(rd1,rd2);
//         int pos2 = Math.max(rd1,rd2);
//         for (int i=0;i<=pos1;i++) {
//             child1.chromosome = child1.chromosome + (this.chromosome & (1<<i));
//             child2.chromosome = child2.chromosome + (other.chromosome & (1<<i));
//         }
//         for (int i=pos1+1;i<=pos2;i++) {
//             child1.chromosome = child1.chromosome + (other.chromosome & (1<<i));
//             child2.chromosome = child2.chromosome + (this.chromosome & (1<<i));
//         }
//         for (int i=pos2+1;i<Integer.SIZE;i++) {
//             child1.chromosome = child1.chromosome + (this.chromosome & (1<<i));
//             child2.chromosome = child2.chromosome + (other.chromosome & (1<<i));
//         }
//         //System.out.println(this);
//         //System.out.println(other);
//         //System.out.println(pos1+" "+pos2);
//         //System.out.println(child1);
//         //System.out.println(child2);
//         int choose = this.MyRand.nextInt(2);
//         //System.out.println(choose);
//         //System.out.println("-----");
//         if (choose==0) return child1;
//         else return child2;
//         //return child;
//     }*/

//     @Override
//     public int compareTo(Individual other) {
//     	if (this.fitness>other.fitness) return -1;
//         else if (this.fitness<other.fitness) return 1;
//         else return 0;
//     }

//     @Override
// 	public String toString() {
// 		String res = new String(this.chromosome + " " + this.printBinary(this.chromosome) + " " + this.fitness);
// 		return res;
// 	}

//     public static String printBinary(int number) { //print isi kromosom
// 		String resStr = Integer.toBinaryString(number);
//         String paddedRes = String.format("%32s", resStr).replaceAll(" ","0");
//         ArrayList<String> res = new ArrayList<>();
//         int i = 0;
//         while (i < paddedRes.length()) {
//             res.add(paddedRes.substring(i, Math.min(i+4,paddedRes.length())));
//             i = i + 4;
//         }
//         return res.stream().collect(Collectors.joining("|"));
//     }

// }

import java.util.Random;

import fireStation.MapData;

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
}

