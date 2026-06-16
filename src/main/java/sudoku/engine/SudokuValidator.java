package sudoku.engine;

import sudoku.model.Board;

public class SudokuValidator {

    public boolean isValidPlacement(Board board, int row, int col, int value) {
        return !isInRow(board, row, value)
            && !isInColumn(board, col, value)
            && !isInBox(board, row, col, value);
    }

    public boolean isBoardValid(Board board) {
        for (int i = 0; i < Board.SIZE; i++) {
            if (!isRowValid(board, i))    return false;
            if (!isColumnValid(board, i)) return false;
        }
        for (int br = 0; br < Board.SIZE; br += Board.BOX_SIZE)
            for (int bc = 0; bc < Board.SIZE; bc += Board.BOX_SIZE)
                if (!isBoxValid(board, br, bc)) return false;
        return true;
    }

    public boolean isSolved(Board board) {
        return board.isComplete() && isBoardValid(board);
    }

    private boolean isInRow(Board b, int row, int value) {
        for (int c = 0; c < Board.SIZE; c++) if (b.getValue(row, c) == value) return true;
        return false;
    }

    private boolean isInColumn(Board b, int col, int value) {
        for (int r = 0; r < Board.SIZE; r++) if (b.getValue(r, col) == value) return true;
        return false;
    }

    private boolean isInBox(Board b, int row, int col, int value) {
        int sr = (row / Board.BOX_SIZE) * Board.BOX_SIZE;
        int sc = (col / Board.BOX_SIZE) * Board.BOX_SIZE;
        for (int r = sr; r < sr + Board.BOX_SIZE; r++)
            for (int c = sc; c < sc + Board.BOX_SIZE; c++)
                if (b.getValue(r, c) == value) return true;
        return false;
    }

    private boolean isRowValid(Board b, int row) {
        boolean[] seen = new boolean[Board.SIZE + 1];
        for (int c = 0; c < Board.SIZE; c++) {
            int v = b.getValue(row, c);
            if (v == 0) continue;
            if (seen[v]) return false;
            seen[v] = true;
        }
        return true;
    }

    private boolean isColumnValid(Board b, int col) {
        boolean[] seen = new boolean[Board.SIZE + 1];
        for (int r = 0; r < Board.SIZE; r++) {
            int v = b.getValue(r, col);
            if (v == 0) continue;
            if (seen[v]) return false;
            seen[v] = true;
        }
        return true;
    }

    private boolean isBoxValid(Board b, int sr, int sc) {
        boolean[] seen = new boolean[Board.SIZE + 1];
        for (int r = sr; r < sr + Board.BOX_SIZE; r++)
            for (int c = sc; c < sc + Board.BOX_SIZE; c++) {
                int v = b.getValue(r, c);
                if (v == 0) continue;
                if (seen[v]) return false;
                seen[v] = true;
            }
        return true;
    }
}
