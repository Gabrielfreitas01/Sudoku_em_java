package sudoku.engine;

import sudoku.exception.InvalidMoveException;
import sudoku.model.Board;
import sudoku.model.Difficulty;

public class GameController {

    private final SudokuGenerator generator;
    private final SudokuValidator  validator;
    private final SudokuSolver     solver;

    private Board      board;
    private Board      solution;
    private Difficulty difficulty;
    private boolean    gameOver;

    public GameController() {
        this.validator = new SudokuValidator();
        this.solver    = new SudokuSolver(validator);
        this.generator = new SudokuGenerator(solver, validator);
    }

    public void newGame(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.gameOver   = false;
        this.board      = generator.generate(difficulty);
        this.solution   = solver.solvedCopy(board);
    }

    public MoveResult placeValue(int row, int col, int value) {
        if (board.getCell(row, col).isFixed())
            throw new InvalidMoveException(row, col, value, "Célula fixa.");

        boolean correct = solution != null && solution.getValue(row, col) == value;
        board.placeValue(row, col, value);
        if (!correct) board.incrementErrors();
        if (validator.isSolved(board)) gameOver = true;

        return new MoveResult(correct, gameOver);
    }

    public boolean clearCell(int row, int col) {
        return board.clearValue(row, col);
    }

    public int hint(int row, int col) {
        if (solution == null) return -1;
        int correct = solution.getValue(row, col);
        board.placeValue(row, col, correct);
        if (validator.isSolved(board)) gameOver = true;
        return correct;
    }

    public Board      getBoard()      { return board; }
    public Difficulty getDifficulty() { return difficulty; }
    public boolean    isGameOver()    { return gameOver; }

    public record MoveResult(boolean correct, boolean gameWon) {}
}
