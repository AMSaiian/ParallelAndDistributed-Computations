package Lab2;

import java.util.concurrent.CountDownLatch;

public class ScalarMultiplier {
    private static final int LOG_ELEMENTS = 5;
    private final Double _scalar;
    private final Double[][] _matrix;
    private final Double[][] _result;

    private final String _logMessage;
    private final int _numThreads;
    private final CountDownLatch _awaiter;

    public ScalarMultiplier(Double scalar, Double[][] matrix, String logMessage, int numThreads) {
        this._scalar = scalar;
        this._matrix = matrix;
        this._numThreads = numThreads;
        this._logMessage = logMessage;
        this._result = new Double[matrix.length][matrix[0].length];
        this._awaiter = new CountDownLatch(_numThreads);
    }

    public Double[][] getResult() throws InterruptedException {
        return _result;
    }

    public void runParallelComputation() throws InterruptedException {
        Thread[] threads = new Thread[_numThreads];

        for (int i = 0; i < _numThreads; i++) {
            // Обираємо рядки матриці для паралельного множення
            final int start = i * (_matrix.length / _numThreads);
            final int end = (i == _numThreads - 1)
                    ? _matrix.length
                    : start + (_matrix.length / _numThreads);
            // Поток на основі Anonymous Runnable,
            // який шукає добутки скаляра на матрицю в певному переліку своїх рядків
            threads[i] = new Thread(() -> {
                // Множення скаляра на рядки матриці
                for (int row = start; row < end; row++) {
                    for (int column = 0; column < _matrix[row].length; column++) {
                        _result[row][column] = _matrix[row][column] * _scalar;
                    }
                }
                _awaiter.countDown();
            });

            threads[i].start();
        }

        _awaiter.await();

        log();
    }

    private void log() {
        Printer.printMatrix(LOG_ELEMENTS, LOG_ELEMENTS, _result, _logMessage);
    }
}
