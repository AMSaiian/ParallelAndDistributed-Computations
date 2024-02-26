package Lab1;

public class VectorSummer {
    private static final int LOG_ELEMENTS = 5;
    public static final boolean SUM = true;
    public static final boolean SUBTRACTION = false;
    private final Double[] _left;
    private final Double[] _right;
    private final Double[] _result;

    private final String _logMessage;
    private final int _numThreads;
    private final boolean _isSum;

    public VectorSummer(Double[] leftVector, Double[] rightVector, boolean action, String logMessage, int numThreads) {
        this._left = leftVector;
        this._right = rightVector;
        this._isSum = action;
        this._logMessage = logMessage;
        this._numThreads = numThreads;
        this._result = new Double[leftVector.length];
    }

    public Double[] getResult() throws InterruptedException {
        return _result;
    }

    public void runParallelComputation() throws InterruptedException {
        Thread[] threads = new Thread[_numThreads];

        for (int i = 0; i < _numThreads; i++) {
            // Обираємо елементи масиву для паралельного сумування
            final int startIndex = i * (_left.length / _numThreads);
            final int endIndex = (i == _numThreads - 1)
                    ? _left.length
                    : startIndex + (_left.length / _numThreads);
            // Поток на основі Anonymous Runnable, який шукає суму в певному переліку своїх елементів
            threads[i] = new Thread(() -> {
                // Підсумовування елементів у виділених для потоку індексах
                for (int index = startIndex; index < endIndex; index++) {
                    performSum(index);
                }
            });

            threads[i].start();
        }

        for (int i = 0; i < _numThreads; i++) {
            threads[i].join();
        }

        log();
    }

    private void performSum(int index) {
        Double leftOperand = _left[index];
        Double rightOperand = _isSum ? _right[index] : -_right[index];
        _result[index] = KahanSummer.sum(leftOperand, rightOperand);
    }

    private void log() {
        Printer.printVector(LOG_ELEMENTS, _result, _logMessage);
    }
}
