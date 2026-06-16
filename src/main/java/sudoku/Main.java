package sudoku;

import sudoku.ui.SudokuApp;

/**
 * Ponto de entrada da aplicação Sudoku JavaFX.
 *
 * A separação entre Main e Application (SudokuApp) é necessária
 * porque o module system do Java 11+ requer que a classe main
 * não estenda javafx.application.Application diretamente quando
 * executada fora do módulo JavaFX.
 */
public class Main {
    public static void main(String[] args) {
        SudokuApp.launch(SudokuApp.class, args);
    }
}
