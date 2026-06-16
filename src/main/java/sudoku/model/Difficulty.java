package sudoku.model;

public enum Difficulty {
    FACIL("Fácil", 30),
    MEDIO("Médio", 45),
    DIFICIL("Difícil", 55);

    private final String label;
    private final int cellsToRemove;

    Difficulty(String label, int cellsToRemove) {
        this.label = label;
        this.cellsToRemove = cellsToRemove;
    }

    public String getLabel()       { return label; }
    public int getCellsToRemove()  { return cellsToRemove; }

    @Override public String toString() { return label; }
}
