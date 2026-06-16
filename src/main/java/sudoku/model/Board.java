package sudoku.model;

public class Board {

    public static final int SIZE     = 9;
    public static final int BOX_SIZE = 3;

    private final Cell[][] grid;
    private int moves;
    private int errors;

    public Board() {
        this.grid = new Cell[SIZE][SIZE];
        this.moves  = 0;
        this.errors = 0;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = Cell.empty();
    }

    public Cell getCell(int row, int col) {
        validate(row, col);
        return grid[row][col];
    }

    public void setCell(int row, int col, Cell cell) {
        validate(row, col);
        grid[row][col] = cell;
    }

    public int getValue(int row, int col) { return getCell(row, col).getValue(); }

    public boolean placeValue(int row, int col, int value) {
        Cell cell = getCell(row, col);
        if (cell.isFixed()) return false;
        cell.setValue(value);
        moves++;
        return true;
    }

    public boolean clearValue(int row, int col) {
        Cell cell = getCell(row, col);
        if (cell.isFixed()) return false;
        cell.setValue(0);
        return true;
    }

    public boolean isComplete() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (grid[r][c].isEmpty()) return false;
        return true;
    }

    public int getMoves()  { return moves; }
    public int getErrors() { return errors; }
    public void incrementErrors() { errors++; }

    public Board copy() {
        Board b = new Board();
        b.moves  = this.moves;
        b.errors = this.errors;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                b.grid[r][c] = this.grid[r][c].copy();
        return b;
    }

    public int[][] toIntGrid() {
        int[][] g = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                g[r][c] = grid[r][c].getValue();
        return g;
    }

    public void loadFromGrid(int[][] values, boolean[][] fixed) {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = fixed[r][c]
                    ? Cell.fixed(values[r][c])
                    : (values[r][c] == 0 ? Cell.empty() : new Cell(values[r][c], false));
        moves = 0;
        errors = 0;
    }

    private void validate(int r, int c) {
        if (r < 0 || r >= SIZE || c < 0 || c >= SIZE)
            throw new IllegalArgumentException("Posição inválida: (" + r + "," + c + ")");
    }
}
