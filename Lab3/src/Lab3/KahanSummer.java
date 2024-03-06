package Lab3;

public class KahanSummer {
    public static double sum(Double... elements)
    {
        Double sum = 0.0;

        // Помилка суми
        Double compensation = 0.0;

        // Сумування елементів з акумуляцією помилки
        for (Double element : elements) {

            Double adjusted = element - compensation;
            Double tempSum = sum + adjusted;
            compensation = (tempSum - sum) - adjusted;

            sum = tempSum;
        }

        return sum;
    }
}