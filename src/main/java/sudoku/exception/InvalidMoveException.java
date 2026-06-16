package sudoku.exception;

public class InvalidMoveException extends RuntimeException {
    private final int row, col, value;

    public InvalidMoveException(int row, int col, int value, String message) {
        super(message);
        this.row = row; this.col = col; this.value = value;
    }

    public int getRow()   { return row; }
    public int getCol()   { return col; }
    public int getValue() { return value; }
}
