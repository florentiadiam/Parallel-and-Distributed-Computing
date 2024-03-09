import java.util.Scanner;

public class matrixmultiplicationJava {
    
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int lin, col, blockSize;
        int op;
        
        op = 1;
        do {
            System.out.println("\n1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.print("Selection?: ");
            op = scanner.nextInt();
            if (op == 0)
                break;
            System.out.print("Dimensions: lins=cols ? ");
            lin = scanner.nextInt();
            col = lin;
    
            switch (op) {
                case 1:
                    for (int j=0;j<10; j++){
                        OnMult(lin, col);
                    }
                    break;
                case 2:
                    for (int j=0;j<10; j++){
                        OnMultLine(lin, col);
                    }
                    break;
                case 3:
                    for (int j=0;j<10; j++){
                        System.out.print("Block Size? ");
                        blockSize = scanner.nextInt();
                        OnMultBlock(lin, col, blockSize);
                    }
                    break;
            }
        } while (op != 0);
        
        scanner.close();
    }
    
    public static void OnMult(int m_ar, int m_br) {
        long Time1, Time2;
        double temp;
        int i, j, k;
    
        double[] pha, phb, phc;
        
        pha = new double[m_ar * m_ar];
        phb = new double[m_ar * m_ar];
        phc = new double[m_ar * m_ar];
    
        for (i = 0; i < m_ar; i++)
            for (j = 0; j < m_ar; j++)
                pha[i * m_ar + j] = 1.0;
    
        for (i = 0; i < m_br; i++)
            for (j = 0; j < m_br; j++)
                phb[i * m_br + j] = (double) (i + 1);
    
        Time1 = System.currentTimeMillis();
    
        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_br; j++) {
                temp = 0;
                for (k = 0; k < m_ar; k++) {
                    temp += pha[i * m_ar + k] * phb[k * m_br + j];
                }
                phc[i * m_ar + j] = temp;
            }
        }
    
        Time2 = System.currentTimeMillis();
        System.out.println("Time: " + ((double) (Time2 - Time1) / 1000) + " seconds");
    
        // Display 10 elements of the result matrix to verify correctness
        System.out.println("Result matrix: ");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < Math.min(10, m_br); j++)
                System.out.print(phc[j] + " ");
        }
        System.out.println();
    }
    
    public static void OnMultLine(int m_ar, int m_br) {
        long Time1, Time2;
        double temp=0;
        int i, j, k;
    
        double[] pha, phb, phc;
        
        pha = new double[m_ar * m_ar];
        phb = new double[m_ar * m_ar];
        phc = new double[m_ar * m_ar];
    
        for (i = 0; i < m_ar; i++)
            for (j = 0; j < m_ar; j++)
                pha[i * m_ar + j] = 1.0;
    
        for (i = 0; i < m_br; i++)
            for (j = 0; j < m_br; j++)
                phb[i * m_br + j] = (double) (i + 1);

        Time1 = System.currentTimeMillis();

        for (i = 0; i < m_ar; i++) {
            for (k = 0; k < m_br; k++) {
                temp = 0;
                for (j = 0; j < m_ar; j++) {
                    temp += pha[i * m_ar + j] * phb[j * m_br + k];
                }
                phc[i * m_ar + k] = temp;
            }
        }

        Time2 = System.currentTimeMillis();

        System.out.println("Time: " + ((double) (Time2 - Time1) / 1000) + " seconds");
    
        // Display 10 elements of the result matrix to verify correctness
        System.out.println("Result matrix: ");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < Math.min(10, m_br); j++)
                System.out.print(phc[j] + " ");
        }
        System.out.println();
    }
    
    
    public static void OnMultBlock(int m_ar, int m_br, int bkSize) {
        long Time1, Time2;
        double temp;
        int i, j, k, ii, jj, kk;
    
        double[] pha, phb, phc;
    
        pha = new double[m_ar * m_ar];
        phb = new double[m_ar * m_ar];
        phc = new double[m_ar * m_ar];
    
        for (i = 0; i < m_ar; i++)
            for (j = 0; j < m_ar; j++)
                pha[i * m_ar + j] = 1.0;
    
        for (i = 0; i < m_br; i++)
            for (j = 0; j < m_br; j++)
                phb[i * m_br + j] = (double) (i + 1);
    
        Time1 = System.currentTimeMillis();
    
        for (ii = 0; ii < m_ar; ii += bkSize) {
            for (jj = 0; jj < m_br; jj += bkSize) {
                for (kk = 0; kk < m_ar; kk += bkSize) {
                    for (i = ii; i < Math.min(ii + bkSize, m_ar); i++) {
                        for (j = jj; j < Math.min(jj + bkSize, m_br); j++) {
                            temp = 0;
                            for (k = kk; k < Math.min(kk + bkSize, m_ar); k++) {
                                temp += pha[i * m_ar + k] * phb[k * m_br + j];
                            }
                            phc[i * m_ar + j] += temp;
                        }
                    }
                }
            }
        }
    
        Time2 = System.currentTimeMillis();
        System.out.println("Time: " + ((double) (Time2 - Time1) / 1000) + " seconds");
    
        // Display 10 elements of the result matrix to verify correctness
        System.out.println("Result matrix: ");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < Math.min(10, m_br); j++)
                System.out.print(phc[j] + " ");
        }
        System.out.println();
    }
    
}
