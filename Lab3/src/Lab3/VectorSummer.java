package Lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class VectorSummer implements Callable<Double[]> {
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

    private void performSum(int index) {
        Double leftOperand = _left[index];
        Double rightOperand = _isSum ? _right[index] : -_right[index];
        _result[index] = leftOperand + rightOperand;
    }

    private void log() {
        Printer.printVector(LOG_ELEMENTS, _result, _logMessage);
    }

    @Override
    public Double[] call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(_numThreads);
        ArrayList<Callable<Object>> tasks = new ArrayList<>();

        // Створення задач для тред-пулу
        for (int i = 0; i < _numThreads; i++) {
            // Обираємо елементи масиву для паралельного сумування
            final int startIndex = i * (_left.length / _numThreads);
            final int endIndex = (i == _numThreads - 1)
                    ? _left.length
                    : startIndex + (_left.length / _numThreads);
            // Задача на основі Anonymous Callable, яка шукає суму в певному переліку своїх елементів
            tasks.add(() -> {
                // Підсумовування елементів у виділених для задачі індексах
                for (int index = startIndex; index < endIndex; index++) {
                    performSum(index);
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
