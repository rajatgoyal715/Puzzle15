package com.rajatgoyal.puzzle15.model;

import java.util.Random;

import androidx.annotation.NonNull;

public class GameMatrix {
    private static final String ROW_SEPARATOR = ";";
    private static final String COL_SEPARATOR = ",";
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
     * Calculate number of inversions in the matrix
     *
     * @return number of inversions
     */
    private int getInversions() {
        int arr[] = getArray();
        int count = 0;
        for (int i = 0; i < this.size * this.size; i++) {
            if (arr[i] == 0) continue;
            for (int j = i + 1; j < this.size * this.size; j++) {
                if (arr[j] != 0 && arr[j] < arr[i]) count++;
            }
        }
        return count;
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
                set(i, j, i * this.size + j);
            }
        }
    }

    /**
     * shuffle a matrix in random order
     */
    private void shuffleMatrix() {
        int pos_x = this.size - 1, pos_y = this.size - 1;
        int temp, temp_x, temp_y;

        Random rand = new Random();

        for (int index = this.size * this.size - 1; index > 1; index--) {
            temp = rand.nextInt(index);
            temp_x = temp / this.size;
            temp_y = (temp + this.size) % this.size;

            swap(temp_x, temp_y, pos_x, pos_y);

            if (pos_y == 0) {
                pos_x--;
                pos_y = this.size - 1;
            } else {
                pos_y--;
            }
        }
    }

    /**
     * @return flattened 1D matrix
     */
    public int[] getArray() {
        int[] arr = new int[(this.size * this.size)];
        for (int i = 0; i < this.size; i++) {
            System.arraycopy(this.matrix[i], 0, arr, i * this.size, this.size);
        }
        return arr;
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
        int n1 = this.size - 1;
        int n2 = this.size - 2;
        int n3 = this.size - 3;
        if (!isValid()) {
            if (get(n1, n1) != 0) {
                if (get(n1, n2) != 0) {
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
     * @param row1 element 1 row
     * @param col1 element 1 column
     * @param row2 element 2 row
     * @param col2 element 2 column
     */
    public void swap(int row1, int col1, int row2, int col2) {
        int temp = get(row1, col1);
        set(row1, col1, get(row2, col2));
        set(row2, col2, temp);
    }

    /**
     * get element at given position
     *
     * @param row element row
     * @param col element column
     * @return element
     */
    public int get(int row, int col) {
        return this.matrix[row][col];
    }

    /**
     * set element at given position
     *
     * @param row   element row
     * @param col   element column
     * @param value element value
     */
    public void set(int row, int col, int value) {
        this.matrix[row][col] = value;

        if (value == 0) {
            emptyCellRow = row;
            emptyCellCol = col;
        }
    }

    /**
     * @return matrix is solved or not
     */
    public boolean isSolved() {
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (get(i, j) != this.size * i + j + 1)
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
                stringBuilder.append(get(i, j));
                if (j < this.size - 1) stringBuilder.append(COL_SEPARATOR);
            }
            if (i < this.size - 1) stringBuilder.append(ROW_SEPARATOR);
        }
        return stringBuilder.toString();
    }

    /**
     * @param row    element row
     * @param column element column
     * @return element at given row and column is empty(zero) or not
     */
    public boolean isEmpty(int row, int column) {
        return get(row, column) == 0;
    }
}
