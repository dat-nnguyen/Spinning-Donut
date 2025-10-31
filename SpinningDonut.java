public class SpinningDonut {
    public static void main(String[] args) throws InterruptedException {
        double A = 0.0, B = 0.0;

        // terminal size
        int termWidth = 80;
        int termHeight = 24;

        //donut size
        int width = 70;
        int height = 30;

        double K1 = 30.0;
        double K2 = 5.0;
        double R1 = 1.0;
        double R2 = 2.0;

        char[] luminance = {'.', ',', '-', '~', ':', ';', '=', '!', '*', '#', '$', '@'};
        char[] screen = new char[width * height];
        double[] zBuffer = new double[width * height];

        // ANSI colors
        String[] colors = {
                "\033[90m", "\033[31m", "\033[33m", "\033[32m",
                "\033[36m", "\033[34m", "\033[37m", "\033[90m"
        };
        // to center the donut
        int xOffset = (termWidth - width) / 2;
        int yOffset = (termHeight - height) / 2;

        // Precompute cos/sin theta
        double thetaStep = 0.03;
        int stepsTheta = (int)(2 * Math.PI / thetaStep) + 1;
        double[] cosThetaArr = new double[stepsTheta];
        double[] sinThetaArr = new double[stepsTheta];
        for (int i = 0; i < stepsTheta; i++) {
            double t = i * thetaStep;
            cosThetaArr[i] = Math.cos(t);
            sinThetaArr[i] = Math.sin(t);
        }

        // Precompute cos/sin phi
        double phiStep = 0.01;
        int stepsPhi = (int)(2 * Math.PI / phiStep) + 1;
        double[] cosPhiArr = new double[stepsPhi];
        double[] sinPhiArr = new double[stepsPhi];
        for (int i = 0; i < stepsPhi; i++) {
            double p = i * phiStep;
            cosPhiArr[i] = Math.cos(p);
            sinPhiArr[i] = Math.sin(p);
        }

        while (true) {
            // Reset screen zBuffer
            for (int i = 0; i < width * height; i++) {
                screen[i] = ' ';
                zBuffer[i] = 0;
            }

            // render donut
            for (int iTheta = 0; iTheta < stepsTheta; iTheta++) {
                double cosTheta = cosThetaArr[iTheta];
                double sinTheta = sinThetaArr[iTheta];
                for (int iPhi = 0; iPhi < stepsPhi; iPhi++) {
                    double cosPhi = cosPhiArr[iPhi];
                    double sinPhi = sinPhiArr[iPhi];

                    double cosA = Math.cos(A), sinA = Math.sin(A);
                    double cosB = Math.cos(B), sinB = Math.sin(B);

                    double circleX = R2 + R1 * cosTheta;
                    double circleY = R1 * sinTheta;

                    double x = circleX * (cosB * cosPhi + sinA * sinB * sinPhi) - circleY * cosA * sinB;
                    double y = circleX * (sinPhi * cosA) + circleY * sinA;
                    double z = circleX * (-sinB * cosPhi + cosB * sinA * sinPhi) + circleY * cosA * cosB + K2;

                    double ooz = 1 / z;
                    int xp = (int)(width / 2 + K1 * ooz * x);
                    int yp = (int)(height / 2 - K1 * ooz * y);

                    // ambient light
                    double L = cosPhi * cosTheta * sinB
                            - cosA * cosTheta * sinPhi
                            - sinA * sinTheta
                            + cosB * (cosA * sinTheta - cosTheta * sinA * sinPhi);
                    L = L + 0.3; // ambient light
                    if (L > 1) L = 1;
                    if (L < 0) L = 0;

                    if (xp >= 0 && xp < width && yp >= 0 && yp < height) {
                        int idx = xp + yp * width;
                        if (ooz > zBuffer[idx]) {
                            zBuffer[idx] = ooz;
                            int lumIndex = (int)(L * (luminance.length - 1));
                            lumIndex = Math.max(0, Math.min(lumIndex, luminance.length - 1));
                            screen[idx] = luminance[lumIndex];
                        }
                    }
                }
            }

            System.out.print("\033[H"); // move pointer
            for (int i = 0; i < yOffset; i++) System.out.println(); // upper padding
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < xOffset; j++) System.out.print(" "); // left padding
                for (int j = 0; j < width; j++) {
                    char c = screen[i * width + j];
                    int lumIdx = 0;
                    for (int k = 0; k < luminance.length; k++) {
                        if (c == luminance[k]) {
                            lumIdx = k;
                            break;
                        }
                    }
                    int colorIndex = lumIdx * (colors.length - 1) / (luminance.length - 1);
                    System.out.print(colors[colorIndex] + c);
                }
                System.out.println();
            }
            System.out.print("\033[0m"); // reset color
            System.out.flush();

            // spinning donut
            A += 0.06;
            B += 0.03;
            Thread.sleep(40);
        }
    }
}
