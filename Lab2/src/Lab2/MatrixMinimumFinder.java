package Lab2;

import java.util.concurrent.CountDownLatch;

class MatrixMinimumFinder {
    private final Double[][] _matrix;
    private volatile Double _min = Double.MAX_VALUE;

    private final String _logMessage;
    private final int _numThreads;

    private final CountDownLatch _awaiter;

    public MatrixMinimumFinder(Double[][] matrix, String logMessage, int numThreads) {
        this._matrix = matrix;
        this._logMessage = logMessage;
        this._numThreads = numThreads;
        this._awaiter = new CountDownLatch(_numThreads);
    }

    public Double getResult() throws InterruptedException {
        return _min;
    }

    public void runParallelComputation() throws InterruptedException {
        Thread[] threads = new Thread[_numThreads];

        for (int i = 0; i < _numThreads; i++) {
            // Обираємо рядки матриці для паралельного пошуку
            final int startRow = i * (_matrix.length / _numThreads);
            final int endRow = (i == _numThreads - 1)
                    ? _matrix.length
                    : startRow + (_matrix.length / _numThreads);
            // Поток на основі Anonymous Runnable, який шукає мінімум в певному переліку своїх рядків
            threads[i] = new Thread(() -> {
                Double localMin = Double.MAX_VALUE;
                // Пошук локального мінімуму у виділених для потоку рядках
                for (int row = startRow; row < endRow; row++) {
                    for (int col = 0; col < _matrix[row].length; col++) {
                        if (_matrix[row][col] < localMin) {
                            localMin = _matrix[row][col];
                        }
                    }
                }

                updateMin(localMin);
                _awaiter.countDown();
            });

            threads[i].start();
        }

        _awaiter.await();

        log();
    }

    private synchronized void updateMin(Double localMin) {
        if (localMin < _min)
            _min = localMin;
    }

    private void log() {
        Printer.printScalar(_min, _logMessage);
    }
}