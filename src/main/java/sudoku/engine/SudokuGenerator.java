package sudoku.engine;

import sudoku.model.Board;
import sudoku.model.Difficulty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuGenerator {

    private final SudokuSolver   solver;
    private final SudokuValidator validator;
    private final Random          random;

    public SudokuGenerator(SudokuSolver solver, SudokuValidator validator) {
        this.solver    = solver;
        this.validator = validator;
        this.random    = new Random();
    }

    public Board generate(Difficulty difficulty) {
        Board full = buildFullBoard();
        return buildPuzzle(full, difficulty.getCellsToRemove());
    }

    private Board buildFullBoard() {
        Board board = new Board();
        fillDiagonalBoxes(board);
        solver.solve(board);
        return board;
    }

    private void fillDiagonalBoxes(Board board) {
        for (int s = 0; s < Board.SIZE; s += Board.BOX_SIZE)
            fillBox(board, s, s);
    }

    private void fillBox(Board board, int sr, int sc) {
        List<Integer> nums = shuffled();
        int idx = 0;
        for (int r = sr; r < sr + Board.BOX_SIZE; r++)
            for (int c = sc; c < sc + Board.BOX_SIZE; c++)
                board.getCell(r, c).setValue(nums.get(idx++));
    }

    private Board buildPuzzle(Board full, int cellsToRemove) {
        Board puzzle = full.copy();
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                positions.add(new int[]{r, c});
        Collections.shuffle(positions, random);

        int removed = 0;
        for (int[] pos : positions) {
            if (removed >= cellsToRemove) break;
            puzzle.getCell(pos[0], pos[1]).setValue(0);
            removed++;
        }
        return toPlayableBoard(full, puzzle);
    }

    private Board toPlayableBoard(Board solution, Board puzzle) {
        int[][]     solGrid    = solution.toIntGrid();
        int[][]     puzzleGrid = puzzle.toIntGrid();
        boolean[][] fixed      = new boolean[Board.SIZE][Board.SIZE];
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                fixed[r][c] = puzzleGrid[r][c] != 0;
        Board playable = new Board();
        playable.loadFromGrid(puzzleGrid, fixed);
        return playable;
    }

    private List<Integer> shuffled() {
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= Board.SIZE; i++) nums.add(i);
        Collections.shuffle(nums, random);
        return nums;
    }
}
