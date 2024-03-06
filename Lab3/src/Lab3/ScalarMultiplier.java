package Lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ScalarMultiplier implements Callable<Double[][]> {
    private static final int LOG_ELEMENTS = 5;
    private final Double _scalar;
    private final Double[][] _matrix;
    private final Double[][] _result;

    private final String _logMessage;
    private final int _numThreads;

    public ScalarMultiplier(Double scalar, Double[][] matrix, String logMessage, int numThreads) {
        this._scalar = scalar;
        this._matrix = matrix;
        this._numThreads = numThreads;
        this._logMessage = logMessage;
        this._result = new Double[matrix.length][matrix[0].length];
    }

    private void log() {
        Printer.printMatrix(LOG_ELEMENTS, LOG_ELEMENTS, _result, _logMessage);
    }

    @Override
    public Double[][] call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(_numThreads);
        ArrayList<Callable<Object>> tasks = new ArrayList<>();

        // Створюємо задачі для тред-пулу
        for (int i = 0; i < _numThreads; i++) {
            // Обираємо рядки матриці для паралельного множення
            final int start = i * (_matrix.length / _numThreads);
            final int end = (i == _numThreads - 1)
                    ? _matrix.length
                    : start + (_matrix.length / _numThreads);
            // Задача на основі Anonymous Callable,
            // яка шукає добутки скаляра на матрицю в певному переліку своїх рядків
            tasks.add(() -> {
                // Множення скаляра на рядки матриці
                for (int row = start; row < end; row++) {
                    for (int column = 0; column < _matrix[row].length; column++) {
                        _result[row][column] = _matrix[row][column] * _scalar;
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
