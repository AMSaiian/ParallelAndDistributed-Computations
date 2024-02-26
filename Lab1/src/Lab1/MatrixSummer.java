package Lab1;

class MatrixSummer {
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

    public Double[][] getResult() throws InterruptedException {
        return _result;
    }

    public void runParallelComputation() throws InterruptedException {
        Thread[] threads = new Thread[_numThreads];

        for (int i = 0; i < _numThreads; i++) {
            // Обираємо рядки матриці для паралельного сумування
            final int startRow = i * (_left.length / _numThreads);
            final int endRow = (i == _numThreads - 1)
                    ? _left.length
                    : startRow + (_left.length / _numThreads);
            // Поток на основі Anonymous Runnable, який шукає суму в певному переліку своїх рядків
            threads[i] = new Thread(() -> {
                // Підсумовування елементів у виділених для потоку рядках
                for (int row = startRow; row < endRow; row++) {
                    for (int column = 0; column < _left[row].length; column++) {
                        performSum(row, column);
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

    private void performSum(int row, int column) {
        Double leftOperand = _left[row][column];
        Double rightOperand = _isSum ? _right[row][column] : -_right[row][column];
        _result[row][column] = KahanSummer.sum(leftOperand, rightOperand);
    }

    private void log() {
        Printer.printMatrix(LOG_ELEMENTS, LOG_ELEMENTS, _result, _logMessage);
    }
}
