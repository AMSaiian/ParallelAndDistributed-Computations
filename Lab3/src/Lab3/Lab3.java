package Lab3;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

public class Lab3 {
    public static final String FILE_PATH_PREFIX = "./src/Data/";
    public static final String MTFile = FILE_PATH_PREFIX + "MT.json";
    public static final String MXFile = FILE_PATH_PREFIX + "MX.json";
    public static final String BFile = FILE_PATH_PREFIX + "B.json";
    public static final String EFile = FILE_PATH_PREFIX + "E.json";
    public static final String MDFile = FILE_PATH_PREFIX + "MD.json";
    public static final String DFile = FILE_PATH_PREFIX + "D.json";
    public static final int VECTORS_SIZE = 100;
    public static Double[][] MT;
    public static Double[][] MX;
    public static Double[] B;
    public static Double[] E;
    public static Double[][] MD;
    public static Double[] D;

    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
        generateData();
        getData();
        long start = System.currentTimeMillis();
        makeComputation();
        long end = System.currentTimeMillis();
        System.out.println("\nElapsed Time in milli seconds: \n"+ (end - start));
        setResult();
    }

    // Генерація даних для лабораторних робіт
    private static void generateData() throws IOException {
        File file;
        file = new File(MTFile);
        if (!file.exists()) {
            file.createNewFile();
            Serializer.SerializeMatrix(DataGenerator.generateSquareMatrix(VECTORS_SIZE), MTFile);
        }

        file = new File(MXFile);
        if (!file.exists()) {
            file.createNewFile();
            Serializer.SerializeMatrix(DataGenerator.generateSquareMatrix(VECTORS_SIZE), MXFile);
        }

        file = new File(BFile);
        if (!file.exists()) {
            file.createNewFile();
            Serializer.SerializeVector(DataGenerator.generateVector(VECTORS_SIZE), BFile);
        }

        file = new File(EFile);
        if (!file.exists()) {
            file.createNewFile();
            Serializer.SerializeVector(DataGenerator.generateVector(VECTORS_SIZE), EFile);
        }
    }

    // Завантаження даних з файлів
    private static void getData() {
        MT = Serializer.DeserializeMatrix(MTFile);
        MX = Serializer.DeserializeMatrix(MXFile);
        B = Serializer.DeserializeVector(BFile);
        E = Serializer.DeserializeVector(EFile);
    }

    // Вивантаження результатів у файл
    private static void setResult() {
        Serializer.SerializeMatrix(MD, MDFile);
        Serializer.SerializeVector(D, DFile);
    }

    // Виконання паралельних обчислень згідно варіанту
    private static void makeComputation() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(6);

        // Створення об'єктів для паралельних обчислень і запуск їх виконання
        Future<Double[][]> mtPlusMx = executor.submit(
                new MatrixSummer(MT, MX, MatrixSummer.SUM, "\nMT + MX result:\n" ,4));
        Future<Double[][]> mtMinusMx = executor.submit(
                new MatrixSummer(MT, MX, MatrixSummer.SUBTRACTION, "\nMT - MX result:\n" , 4));
        Future<Double[][]> mtMultiMx = executor.submit(
                new MatrixMultiplier(MT, MX, "\nMT * MX result:\n" , 4));
        Future<Double> mtMin = executor.submit(
                new MatrixMinimumFinder(MT, "\nMT min result:\n" , 4));

        // Створення об'єктів для наступних паралельних обчислень на основі результатів вкладених обчислень
        Future<Double[][]> minMultiMtPlusMx = executor.submit(
                new ScalarMultiplier(mtMin.get(), mtPlusMx.get(), "\nmin(MT)*(MT+MX) result:\n", 4));
        Future<Double[]> bMultiMTPlusMX = executor.submit(
                new VectorMultiplier(B, mtPlusMx.get(), "\nB*(MT+MX) result:\n" , 4));
        Future<Double[]> eMultiMTMinusMX = executor.submit(
                new VectorMultiplier(E, mtMinusMx.get(), "\nE*(MT-MX) result:\n" , 4));

        // Створення об'єктів для паралельних обчислень фінальних матриць MD та вектору D
        Future<Double[][]> subtractionForMd = executor.submit(
                new MatrixSummer(minMultiMtPlusMx.get(), mtMultiMx.get(), MatrixSummer.SUBTRACTION, "\nMD = min(MT)*(MT+MX) - MT*MX result:\n" ,6));
        Future<Double[]> subtractionForD = executor.submit(
                new VectorSummer(bMultiMTPlusMX.get(), eMultiMTMinusMX.get(), VectorSummer.SUBTRACTION, "\nD = B*(MT+MX) - E*(MT-MX) result:\n", 6));

        MD = subtractionForMd.get();
        D = subtractionForD.get();

        executor.shutdown();
    }
}