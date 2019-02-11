package com.rajatgoyal.puzzle15.model;

import java.util.Random;

import androidx.annotation.NonNull;

public class GameMatrix {
    private static final String ROW_SEPARATOR = ",";
    private static final String COL_SEPARATOR = ";";
    private int[][] matrix;
    private int size, emptyCellRow, emptyCellCol;

    public GameMatrix(int size) {
        this.size = size;
        this.matrix = new int[size][size];
        fillSeriesMatrix();
        shuffleMatrix();
        validateMatrix();
    }

    private GameMatrix(int[][] matrix) {
        this.matrix = matrix;
        this.size = matrix.length;
        validateMatrix();
    }

    public GameMatrix(int[] arr, int size) {
        this.matrix = new int[size][size];
        this.size = size;
        for (int i = 0; i < size; i++) {
            System.arraycopy(arr, (i * size), this.matrix[i], 0, size);
        }
        validateMatrix();
    }

    public GameMatrix(String matrixString, int size) {
        this(getMatrixFromString(matrixString, size));
    }

    /**
     * get matrix from string
     *
     * @param matrixString matrix string obtained by {toString} method
     * @param size         size of matrix
     * @return matrix
     */
    private static int[][] getMatrixFromString(String matrixString, int size) {
        String[] matrixStringRows = matrixString.split(ROW_SEPARATOR);
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; ++i) {
            String[] matrixStringColumns = matrixStringRows[i].split(COL_SEPARATOR);
            for (int j = 0; j < size; ++j) {
                matrix[i][j] = Integer.parseInt(matrixStringColumns[j]);
            }
        }
        return matrix;
    }

    /**
     * @return flattened 1D matrix
     */
    public int[] get1DArray() {
        int[] arr = new int[(this.size * this.size)];
        for (int i = 0; i < this.size; i++) {
            System.arraycopy(this.matrix[i], 0, arr, i * this.size, this.size);
        }
        return arr;
    }

    public int[][] getMatrix() {
        return this.matrix;
    }

    public int getSize() {
        return this.size;
    }

    private int getEmptyCellRow() {
        return this.emptyCellRow;
    }

    private int getEmptyCellCol() {
        return this.emptyCellCol;
    }

    /**
     * Fill the matrix in ascending order
     */
    private void fillSeriesMatrix() {
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                this.matrix[i][j] = i * this.size + j;
            }
        }
        this.emptyCellCol = this.emptyCellRow = 0;
    }

    /**
     * shuffle a matrix in random order
     */
    private void shuffleMatrix() {
        int pos_x = this.size - 1, pos_y = this.size - 1;
        int temp, temp_x, temp_y, swap;

        Random rand = new Random();

        for (int index = this.size * this.size - 1; index > 1; index--) {
            temp = rand.nextInt(index);
            temp_x = temp / this.size;
            temp_y = (temp + this.size) % this.size;

            swap(temp_x, temp_y, pos_x, pos_y);

            if (this.matrix[temp_x][temp_y] == 0) {
                emptyCellRow = pos_x;
                emptyCellCol = pos_y;
            } else if (this.matrix[pos_x][pos_y] == 0) {
                emptyCellRow = temp_x;
                emptyCellCol = temp_y;
            }
            if (pos_y == 0) {
                pos_x--;
                pos_y = this.size - 1;
            } else {
                pos_y--;
            }
        }
    }

    /**
     * Calculate number of inversions in the matrix
     *
     * @return number of inversions
     */
    private int getInversions() {
        int arr[] = new int[this.size * this.size];
        for (int i = 0; i < this.size; i++) {
            System.arraycopy(this.matrix[i], 0, arr, this.size * i, this.size);
        }
        int count = 0;
        for (int i = 0; i < this.size * this.size; i++) {
            if (arr[i] == 0) continue;
            for (int j = i + 1; j < this.size * this.size; j++) {
                if (arr[j] != 0 && arr[j] < arr[i]) count++;
            }
        }
        return count;
    }


    /**
     * Check if the matrix is valid according to following rules:
     * If n is even, then the matrix is solvable if:
     * 1. blank is on even row counting from the bottom and no of inversions is odd.
     * 2. blank is on odd row from the bottom and no of inversions is even.
     * If n is odd, then the matrix is solvable if no. of inversions is even.
     *
     * @return validity of matrix
     */
    private boolean isValid() {
        int inv = getInversions();
        int emptyCellRow = getEmptyCellRow();

        return (emptyCellRow % 2 == 0 && inv % 2 != 0) || (emptyCellRow % 2 != 0 && inv % 2 == 0);
    }

    /**
     * If puzzle is not solvable, make it solvable by decreasing one inversion
     * which can be done easily by swapping two last positions
     */
    private void validateMatrix() {
        int[][] matrix = this.matrix;
        int n1 = this.size - 1;
        int n2 = this.size - 2;
        int n3 = this.size - 3;
        if (!isValid()) {
            if (matrix[n1][n1] != 0) {
                if (matrix[n1][n2] != 0) {
                    swap(n1, n2, n1, n1);
                } else {
                    swap(n1, n3, n1, n1);
                }
            } else {
                swap(n1, n3, n1, n2);
            }
        }
    }

    /**
     * swap elements at given positions
     *
     * @param r1 element 1 row
     * @param c1 element 1 column
     * @param r2 element 2 row
     * @param c2 element 2 column
     */
    public void swap(int r1, int c1, int r2, int c2) {
        int temp = this.matrix[r1][c1];
        this.matrix[r1][c1] = this.matrix[r2][c2];
        this.matrix[r2][c2] = temp;
    }

    /**
     * get element at given position
     *
     * @param r element row
     * @param c element column
     * @return element
     */
    public int get(int r, int c) {
        return this.matrix[r][c];
    }

    /**
     * set element at given position
     *
     * @param r     element row
     * @param c     element column
     * @param value element value
     */
    public void set(int r, int c, int value) {
        this.matrix[r][c] = value;
    }

    public boolean isSolved() {
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (this.matrix[i][j] != ((this.size * i + j + 1) % (this.size * this.size)))
                    return false;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.size; ++i) {
            for (int j = 0; j < this.size; ++j) {
                stringBuilder.append(this.matrix[i][j]).append(COL_SEPARATOR);
            }
            stringBuilder.append(ROW_SEPARATOR);
        }
        return stringBuilder.toString();
    }
}
