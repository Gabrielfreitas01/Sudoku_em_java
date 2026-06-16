package sudoku.engine;

import sudoku.model.Board;

public class SudokuSolver {

    private final SudokuValidator validator;

    public SudokuSolver(SudokuValidator validator) {
        this.validator = validator;
    }

    public boolean solve(Board board) {
        int[] empty = findEmpty(board);
        if (empty == null) return true;

        int row = empty[0], col = empty[1];
        for (int num = 1; num <= Board.SIZE; num++) {
            if (validator.isValidPlacement(board, row, col, num)) {
                board.getCell(row, col).setValue(num);
                if (solve(board)) return true;
                board.getCell(row, col).setValue(0);
            }
        }
        return false;
    }

    public Board solvedCopy(Board board) {
        Board copy = board.copy();
        return solve(copy) ? copy : null;
    }

    private int[] findEmpty(Board board) {
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                if (board.getCell(r, c).isEmpty()) return new int[]{r, c};
        return null;
    }
}
