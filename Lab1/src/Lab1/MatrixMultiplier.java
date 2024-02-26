package Lab1;

public class MatrixMultiplier {
    private static final int LOG_ELEMENTS = 5;
    private final Double[][] _left;
    private final Double[][] _right;
    private final Double[][] _result;

    private final String _logMessage;
    private final int _numThreads;

    public MatrixMultiplier(Double[][] leftMatrix, Double[][] rightMatrix, String logMessage, int numThreads) {
        this._left = leftMatrix;
        this._right = rightMatrix;
        this._numThreads = numThreads;
        this._logMessage = logMessage;
        this._result = new Double[leftMatrix[0].length][rightMatrix.length];
    }

    public Double[][] getResult() throws InterruptedException {
        return _result;
    }

    public void runParallelComputation() throws InterruptedException {
        Thread[] threads = new Thread[_numThreads];

        for (int i = 0; i < _numThreads; i++) {
            // Обираємо рядки матриці для паралельного множення
            final int start = i * (_left.length / _numThreads);
            final int end = (i == _numThreads - 1)
                    ? _left.length
                    : start + (_left.length / _numThreads);
            // Поток на основі Anonymous Runnable, який шукає добутки матриць в певному переліку своїх рядків
            threads[i] = new Thread(() -> {
                // Множення елементів матриць
                for (int row = start; row < end; row++) {
                    for (int column = 0; column < _right[0].length; column++) {
                        Double[] resultParts = new Double[_left[0].length];
                        for (int element = 0; element < _left[row].length; element++) {
                            resultParts[element] = _left[row][element] * _right[element][column];
                        }
                        _result[row][column] = KahanSummer.sum(resultParts);
                    }
                }

            });

            threads[i].start();
        }

        for (int i = 0; i < _numThreads; i++) {
            threads[i].join();
        }

        log();
    }

    private void log() {
        Printer.printMatrix(LOG_ELEMENTS, LOG_ELEMENTS, _result, _logMessage);
    }
}
