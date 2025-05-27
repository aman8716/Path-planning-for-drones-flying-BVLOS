import java.util.Scanner;

public class RiskMapGenerator {
    static float[][] shelter = new float[10][10];
    static int[][] noFly = new int[10][10];
    static float[][] obstacle = new float[10][10];
    static float[][] fatality = new float[10][10];
    static float[][] casualty = new float[10][10];
    static float[][] risk = new float[10][10];
    static float beta, alpha, E_imp, v_imp;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Generating Risk Map (N X N)");
        System.out.print("\nEnter N (<10): ");
        int n = scanner.nextInt();

        // Input for no fly zone
        System.out.println("\nEnter values for no fly zone....\n(-1: flight not allowed; 0: flight allowed)");
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) 
            {
                System.out.printf("\nLocation (%d, %d): ", i, j);
                noFly[i][j] = scanner.nextInt();
            }
        }

        // Input for obstacle heights
        System.out.println("\nEnter heights for obstacles....");
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                System.out.printf("\nLocation (%d, %d): ", i, j);
                obstacle[i][j] = scanner.nextFloat();
            }
        }

        // Input for threshold height
        System.out.print("\nEnter threshold height: ");
        float h_threshold = scanner.nextFloat();

        // Input for sheltering factors
        System.out.println("\nEnter sheltering factors....\n(0: No obstacles, 2.5: Sparse tree, 5: Vehicles & low buildings, 7.5: High buildings, 10: Industrial building)");
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                System.out.printf("\nLocation (%d, %d): ", i, j);
                shelter[i][j] = scanner.nextFloat();
            }
        }

        // Calculation of v_imp value
        System.out.print("\nEnter values of u (19.4444m/s): ");
        double u = scanner.nextDouble();

        System.out.print("\nEnter values of g (9.8m/s^2): ");
        double g = scanner.nextDouble();

        v_imp = (float) Math.sqrt((u * u) + (2 * g * h_threshold));
        System.out.printf("The value of final impact velocity is: %f\n", v_imp);

        // Calculation of E_imp value
        System.out.print("\nEnter the mass of uav(3.75kg): ");
        double m = scanner.nextDouble();
        E_imp = (float) (0.5 * m * v_imp * v_imp);
        System.out.printf("The value of kinetic energy of impact E_imp: %f\n", E_imp);

        // Risk map generation
        System.out.print("\nEnter event(crash) probability: ");
        float event = scanner.nextFloat();

        System.out.println("Calculating P_impact....");
        float impact = p_impact(scanner);
        System.out.printf("\nP_impact: %f\n", impact);

        System.out.println("\nCalculating P_fatality....");
        System.out.print("\nEnter value of impact energy beta (34J): ");
        beta = scanner.nextFloat();
        System.out.print("\nEnter value of impact energy alpha (100KJ): ");
        alpha = scanner.nextFloat();

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                fatality[i][j] = p_fatality(i, j);
                System.out.printf("\nP_fatality(%d, %d): %f\n", i, j, fatality[i][j]);
                casualty[i][j] = event * impact * p_fatality(i, j);

                if (noFly[i][j] == -1 || obstacle[i][j] >= h_threshold)
                    risk[i][j] = -1;
                else
                    risk[i][j] = casualty[i][j];
                System.out.printf("\nRisk (%d, %d): %f\n", i, j, risk[i][j]);
            }
        }

        System.out.println("\nRisk map generation completed.");
    }

    static float p_impact(Scanner scanner) {
        double pi = Math.PI;
        System.out.print("\nEnter population density (6900 people/km^2): ");
        float population = scanner.nextFloat();

        System.out.print("Enter average radius of a person(0.248m): ");
        float r_p = scanner.nextFloat();

        System.out.print("\nEnter radius of UAV(0.88m): ");
        float r_uav = scanner.nextFloat();

        System.out.print("\nEnter average height of a person(1.587m): ");
        float h_p = scanner.nextFloat();

        System.out.print("Enter impact angle(in radians): ");
        double radian = scanner.nextDouble();

        double a_exp = 2 * (r_p + r_uav) * h_p / Math.tan(radian) + pi * Math.pow((r_uav + r_p), 2);
        return population * (float) a_exp;
    }

    static float p_fatality(int x, int y) {
        double temp = Math.pow((beta / E_imp), (3 / shelter[x][y]));
        float k = (float) (temp < 1 ? temp : 1);
        float fatality = (float)((1 - k)/(1 - 2 * k + Math.sqrt(alpha / beta)* Math.pow(beta / E_imp, (3 / shelter[x][y]))));
        return fatality;
    }
}