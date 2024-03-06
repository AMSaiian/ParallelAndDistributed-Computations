package Lab2;

import java.util.concurrent.CountDownLatch;

public class VectorMultiplier {
    private static final int LOG_ELEMENTS = 5;
    private final Double[] _left;
    private final Double[][] _right;
    private final Double[] _result;

    private final String _logMessage;
    private final int _numThreads;
    private final CountDownLatch _awaiter;

    public VectorMultiplier(Double[] leftMatrix, Double[][] rightMatrix, String logMessage, int numThreads) {
        this._left = leftMatrix;
        this._right = rightMatrix;
        this._numThreads = numThreads;
        this._logMessage = logMessage;
        this._result = new Double[leftMatrix.length];
        this._awaiter = new CountDownLatch(_numThreads);
    }

    public Double[] getResult() throws InterruptedException {
        return _result;
    }

    public void runParallelComputation() throws InterruptedException {
        Thread[] threads = new Thread[_numThreads];

        for (int i = 0; i < _numThreads; i++) {
            // Обираємо стовпці матриці для паралельного множення
            final int start = i * (_left.length / _numThreads);
            final int end = (i == _numThreads - 1)
                    ? _left.length
                    : start + (_left.length / _numThreads);
            // Поток на основі Anonymous Runnable,
            // який шукає добутки вектора на матрицю в певному переліку своїх стовпців
            threads[i] = new Thread(() -> {
                // Множення вектора на стовпці матриці
                for (int column = start; column < end; column++) {
                    Double[] resultParts = new Double[_left.length];
                    for (int element = 0; element < _left.length; element++) {
                        resultParts[element] = _left[element] * _right[element][column];
                    }
                    _result[column] = KahanSummer.sum(resultParts);
                }
                _awaiter.countDown();
            });

            threads[i].start();
        }

        _awaiter.await();

        log();
    }

    private void log() {
        Printer.printVector(LOG_ELEMENTS, _result, _logMessage);
    }
}
