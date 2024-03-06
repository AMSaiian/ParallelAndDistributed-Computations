package Lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class MatrixMinimumFinder implements Callable<Double> {
    private final Double[][] _matrix;
    private volatile Double _min = Double.MAX_VALUE;

    private final String _logMessage;
    private final int _numThreads;

    private final ReentrantLock _locker = new ReentrantLock();

    public MatrixMinimumFinder(Double[][] matrix, String logMessage, int numThreads) {
        this._matrix = matrix;
        this._logMessage = logMessage;
        this._numThreads = numThreads;
    }


    private void updateMin(Double localMin) {
        _locker.lock();

        if (localMin < _min)
            _min = localMin;

        _locker.unlock();
    }

    private void log() {
        Printer.printScalar(_min, _logMessage);
    }

    @Override
    public Double call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(_numThreads);
        ArrayList<Callable<Object>> tasks = new ArrayList<>();

        // Створюємо задачі для тред-пулу
        for (int i = 0; i < _numThreads; i++) {
            // Обираємо рядки матриці для паралельного пошуку
            final int startRow = i * (_matrix.length / _numThreads);
            final int endRow = (i == _numThreads - 1)
                    ? _matrix.length
                    : startRow + (_matrix.length / _numThreads);
            // Задача на основі Anonymous Callable, яка шукає мінімум в певному переліку своїх рядків
            tasks.add(() -> {
                Double localMin = Double.MAX_VALUE;
                // Пошук локального мінімуму у виділених для задачі рядках
                for (int row = startRow; row < endRow; row++) {
                    for (int col = 0; col < _matrix[row].length; col++) {
                        if (_matrix[row][col] < localMin) {
                            localMin = _matrix[row][col];
                        }
                    }
                }
                updateMin(localMin);

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

        return _min;
    }
}