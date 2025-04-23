public class Perlin1D {
    private double[] gradients;
    private int size;

    public Perlin1D(int size, long seed) {
        this.size = size;
        this.gradients = new double[size];
        java.util.Random rand = new java.util.Random(seed);
        for (int i = 0; i < size; i++) {
            gradients[i] = rand.nextDouble() * 2 - 1; // gradients entre -1 et 1
        }
    }

    private double fade(double t) {
        // courbe de lissage : 6t^5 - 15t^4 + 10t^3
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    public double get(double x) {
        int x0 = (int) Math.floor(x) % size;
        int x1 = (x0 + 1) % size;

        double dx = x - Math.floor(x);
        double g0 = gradients[x0];
        double g1 = gradients[x1];

        double dot0 = g0 * dx;
        double dot1 = g1 * (dx - 1);

        double t = fade(dx);
        return lerp(dot0, dot1, t);
    }
}
