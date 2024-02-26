package Lab1;

import java.io.File;
import java.io.IOException;

public class Lab1 {
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

    public static void main(String[] args) throws InterruptedException, IOException {
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
    private static void makeComputation() throws InterruptedException {
        // Створення об'єктів для наступних паралельних обчислень
        MatrixSummer mtPlusMx =
                new MatrixSummer(MT, MX, MatrixSummer.SUM, "\nMT + MX result:\n" ,4);
        MatrixSummer mtMinusMx =
                new MatrixSummer(MT, MX, MatrixSummer.SUBTRACTION, "\nMT - MX result:\n" , 4);
        MatrixMultiplier mtMultiMx =
                new MatrixMultiplier(MT, MX, "\nMT * MX result:\n" , 4);
        MatrixMinimumFinder mtMin =
                new MatrixMinimumFinder(MT, "\nMT min result:\n" , 4);

        // Cтворення потоків для паралельних обчислень термінальних задач (вкладених)
        Thread mtPlusMxThread = new Thread(() -> {
            try {
                mtPlusMx.runParallelComputation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread mtMinusMxThread = new Thread(() -> {
            try {
                mtMinusMx.runParallelComputation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread mtMultiMxThread = new Thread(() -> {
            try {
                mtMultiMx.runParallelComputation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread mtMinThread = new Thread(() -> {
            try {
                mtMin.runParallelComputation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Запуск і очікування результатів роботи потоків
        mtPlusMxThread.start();
        mtMinusMxThread.start();
        mtMultiMxThread.start();
        mtMinThread.start();

        mtPlusMxThread.join();
        mtMinusMxThread.join();
        mtMultiMxThread.join();
        mtMinThread.join();

        // Створення об'єктів для наступних паралельних обчислень на основі результатів вкладених обчислень
        ScalarMultiplier minMultiMtPlusMx =
                new ScalarMultiplier(mtMin.getResult(), mtPlusMx.getResult(), "\nmin(MT)*(MT+MX) result:\n", 4);
        VectorMultiplier bMultiMTPlusMX =
                new VectorMultiplier(B, mtPlusMx.getResult(), "\nB*(MT+MX) result:\n" , 4);
        VectorMultiplier eMultiMTMinusMX =
                new VectorMultiplier(E, mtMinusMx.getResult(), "\nE*(MT-MX) result:\n" , 4);

        // Cтворення потоків для паралельних обчислень вкладених задач
        Thread minMultiMtPlusMxThread = new Thread(() -> {
            try {
                minMultiMtPlusMx.runParallelComputation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread bMultiMTPlusMXThread = new Thread(() -> {
            try {
                bMultiMTPlusMX.runParallelComputation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread eMultiMTMinusMXThread = new Thread(() -> {
            try {
                eMultiMTMinusMX.runParallelComputation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Запуск і очікування результатів роботи потоків
        minMultiMtPlusMxThread.start();
        bMultiMTPlusMXThread.start();
        eMultiMTMinusMXThread.start();

        minMultiMtPlusMxThread.join();
        bMultiMTPlusMXThread.join();
        eMultiMTMinusMXThread.join();

        // Створення об'єктів для паралельних обчислень фінальних матриць MD та вектору D
        MatrixSummer subtractionForMd =
                new MatrixSummer(minMultiMtPlusMx.getResult(), mtMultiMx.getResult(), MatrixSummer.SUBTRACTION, "\nMD = min(MT)*(MT+MX) - MT*MX result:\n" ,6);
        VectorSummer subtractionForD =
                new VectorSummer(bMultiMTPlusMX.getResult(), eMultiMTMinusMX.getResult(), VectorSummer.SUBTRACTION, "\nD = B*(MT+MX) - E*(MT-MX) result:\n", 6);

        // Cтворення потоків для паралельних обчислень фінальних задач
        Thread subtractionForMdThread = new Thread(() -> {
            try {
                subtractionForMd.runParallelComputation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread subtractionForDThread = new Thread(() -> {
            try {
                subtractionForD.runParallelComputation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Запуск і очікування результатів роботи потоків
        subtractionForMdThread.start();
        subtractionForDThread.start();

        subtractionForMdThread.join();
        subtractionForDThread.join();

        MD = subtractionForMd.getResult();
        D = subtractionForD.getResult();
    }
}