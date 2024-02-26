package Lab1;

public class Printer {
    public synchronized static void printMatrix(int rowAmount, int columnAmount, Double[][] matrix, String logMessage) {
        // Через різний порядок дробових чисел для форматування виводу необхідно
        // обрахувати оптимальну ширину виводу для кожного стовпця
        int[] maxLengths = new int[columnAmount];

        for (int j = 0; j < columnAmount; j++) {
            int maxLength = 0;
            for (int i = 0; i < rowAmount; i++) {
                int length = String.valueOf(matrix[i][j]).length();
                if (length > maxLength) {
                    maxLength = length;
                }
            }
            maxLengths[j] = maxLength;
        }

        System.out.println(logMessage);
        for (int i = 0; i < rowAmount; i++) {
            for (int j = 0; j < columnAmount; j++) {
                String element = String.format("%" + maxLengths[j] + "s", matrix[i][j]);
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }

    public synchronized static void printVector(int elementAmount, Double[] vector, String logMessage) {
        // Через різний порядок дробових чисел для форматування виводу необхідно
        // обрахувати оптимальну ширину виводу для кожного елементу
        int maxLength = 0;
        for (int i = 0; i < elementAmount; i ++) {
            int elementLength = String.valueOf(vector[i]).length();
            if (elementLength > maxLength) {
                maxLength = elementLength;
            }
        }

        System.out.println(logMessage);
        for (int i = 0; i < elementAmount; i++) {
            String element = String.format("%" + maxLength + "s", vector[i]);
            System.out.print(element + " ");
        }
        System.out.println();
    }

    public static synchronized void printScalar(Double scalar, String logMessage) {
        System.out.println(logMessage);
        System.out.println(scalar);
    }
}
