package Lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MatrixSummer implements Callable<Double[][]> {
    private static final int LOG_ELEMENTS = 5;
    public static final boolean SUM = true;
    public static final boolean SUBTRACTION = false;
    private final Double[][] _left;
    private final Double[][] _right;
    private final Double[][] _result;

    private final String _logMessage;
    private final int _numThreads;
    private final boolean _isSum;

    public MatrixSummer(Double[][] leftMatrix, Double[][] rightMatrix, boolean action, String logMessage, int numThreads) {
        this._left = leftMatrix;
        this._right = rightMatrix;
        this._isSum = action;
        this._logMessage = logMessage;
        this._numThreads = numThreads;
        this._result = new Double[leftMatrix.length][leftMatrix[0].length];
    }

    private void performSum(int row, int column) {
        Double leftOperand = _left[row][column];
        Double rightOperand = _isSum ? _right[row][column] : -_right[row][column];
        _result[row][column] = leftOperand + rightOperand;
    }

    private void log() {
        Printer.printMatrix(LOG_ELEMENTS, LOG_ELEMENTS, _result, _logMessage);
    }

    @Override
    public Double[][] call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(_numThreads);
        ArrayList<Callable<Object>> tasks = new ArrayList<>();

        // Створення задач для тред-пулу
        for (int i = 0; i < _numThreads; i++) {
            // Обираємо рядки матриці для паралельного сумування
            final int startRow = i * (_left.length / _numThreads);
            final int endRow = (i == _numThreads - 1)
                    ? _left.length
                    : startRow + (_left.length / _numThreads);
            // Задача на основі Anonymous Callable, яка шукає суму в певному переліку своїх рядків
            tasks.add(() -> {
                // Підсумовування елементів у виділених для потоку рядках
                for (int row = startRow; row < endRow; row++) {
                    for (int column = 0; column < _left[row].length; column++) {
                        performSum(row, column);
                    }
                }

                return null;
            });
        }

        List<Future<Object>> results = executor.invokeAll(tasks);

        // Очікування результатів виконання усіх задач за допомогою отримання результату з Future
        for (Future<Object> result : results) {
            result.get();
        }

        executor.shutdown();

        log();

        return _result;
    }
}
