package sudoku.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import sudoku.engine.GameController;
import sudoku.engine.GameController.MoveResult;
import sudoku.exception.InvalidMoveException;
import sudoku.model.Board;
import sudoku.model.Cell;
import sudoku.model.Difficulty;

/**
 * Interface gráfica do Sudoku em JavaFX.
 *
 * Responsabilidade: apenas UI. Toda lógica permanece no GameController.
 *
 * Estrutura da cena:
 *
 *   ┌─────────────────────────────────────┐
 *   │           HEADER (título + stats)   │
 *   ├─────────────────────────────────────┤
 *   │  GRID 9x9  │   PAINEL LATERAL       │
 *   │            │   [numpad 1-9]         │
 *   │            │   [apagar] [dica]      │
 *   │            │   [novo jogo]          │
 *   ├─────────────────────────────────────┤
 *   │           FEEDBACK (mensagem)       │
 *   └─────────────────────────────────────┘
 */
public class SudokuApp extends Application {

    // ── Constantes de layout ─────────────────────────────────────────────
    private static final double CELL_SIZE   = 60;
    private static final double GAP_THIN    = 1;
    private static final double GAP_THICK   = 3;

    // ── Paleta de cores ──────────────────────────────────────────────────
    private static final String BG_APP       = "#F0F4F8";
    private static final String BG_HEADER    = "#1A3A5C";
    private static final String BG_GRID      = "#FFFFFF";
    private static final String BG_SIDE      = "#EBF4FF";
    private static final String BG_FEEDBACK  = "#F7FAFC";

    private static final String COLOR_FIXED  = "#1A202C";   // números do puzzle
    private static final String COLOR_PLAYER = "#2B6CB0";   // números do jogador
    private static final String COLOR_ERROR  = "#C53030";   // jogada errada
    private static final String COLOR_HINT   = "#276749";   // dica

    private static final String CELL_NORMAL   = "-fx-background-color: white;";
    private static final String CELL_FIXED    = "-fx-background-color: #EBF8FF;";
    private static final String CELL_SELECTED = "-fx-background-color: #BEE3F8;";
    private static final String CELL_RELATED  = "-fx-background-color: #EBF4FF;";
    private static final String CELL_ERROR_BG = "-fx-background-color: #FFF5F5;";
    private static final String CELL_HINT_BG  = "-fx-background-color: #F0FFF4;";

    // ── Estado da UI ─────────────────────────────────────────────────────
    private final GameController controller = new GameController();
    private final Label[][]  cellLabels = new Label[9][9];
    private int selectedRow = -1;
    private int selectedCol = -1;

    // ── Componentes reutilizados ─────────────────────────────────────────
    private Label    lblMoves;
    private Label    lblErrors;
    private Label    lblDifficulty;
    private Label    lblFeedback;
    private Timeline feedbackTimer;

    // ═════════════════════════════════════════════════════════════════════
    // INICIALIZAÇÃO
    // ═════════════════════════════════════════════════════════════════════

    @Override
    public void start(Stage stage) {
        stage.setTitle("Sudoku — Java Edition");
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_APP + ";");

        root.setTop(buildHeader());
        root.setCenter(buildMainArea());
        root.setBottom(buildFeedbackBar());

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> handleKeyPress(e.getText()));
        stage.setScene(scene);
        stage.show();

        // Inicia automaticamente com dificuldade Fácil
        startNewGame(Difficulty.FACIL);
    }

    // ═════════════════════════════════════════════════════════════════════
    // CONSTRUÇÃO DA CENA
    // ═════════════════════════════════════════════════════════════════════

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + BG_HEADER + ";");
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(0);

        Label title = new Label("SUDOKU");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblDifficulty = statsLabel("Fácil");
        lblMoves      = statsLabel("Movimentos: 0");
        lblErrors     = statsLabel("Erros: 0");

        header.getChildren().addAll(title, spacer, lblDifficulty, sep(), lblMoves, sep(), lblErrors);
        return header;
    }

    private HBox buildMainArea() {
        HBox area = new HBox(20);
        area.setPadding(new Insets(24));
        area.setAlignment(Pos.CENTER);
        area.getChildren().addAll(buildGrid(), buildSidePanel());
        return area;
    }

    /** Grade 9x9 com bordas grossas separando sub-grades 3x3. */
    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setStyle(
            "-fx-background-color: #1A3A5C;" +
            "-fx-border-color: #1A3A5C;" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 4;"
        );

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Label lbl = new Label();
                lbl.setMinSize(CELL_SIZE, CELL_SIZE);
                lbl.setMaxSize(CELL_SIZE, CELL_SIZE);
                lbl.setAlignment(Pos.CENTER);
                lbl.setFont(Font.font("Arial", FontWeight.BOLD, 22));

                // Bordas das células: grossas nos limites de sub-grade
                double top    = (row % 3 == 0 && row != 0) ? GAP_THICK : GAP_THIN;
                double left   = (col % 3 == 0 && col != 0) ? GAP_THICK : GAP_THIN;
                double bottom = (row == 8) ? 0 : GAP_THIN;
                double right  = (col == 8) ? 0 : GAP_THIN;

                lbl.setPadding(new Insets(top, right, bottom, left));

                // Captura row/col em variáveis finais para o lambda
                final int r = row, c = col;
                lbl.setOnMouseClicked(e -> selectCell(r, c));

                cellLabels[row][col] = lbl;
                grid.add(lbl, col, row);
            }
        }
        return grid;
    }

    /** Painel lateral: numpad + botões de ação. */
    private VBox buildSidePanel() {
        VBox panel = new VBox(14);
        panel.setStyle(
            "-fx-background-color: " + BG_SIDE + ";" +
            "-fx-border-color: #CBD5E0;" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(200);

        panel.getChildren().addAll(
            sectionLabel("Inserir número"),
            buildNumPad(),
            new Region() {{ setMinHeight(8); }},
            sectionLabel("Ações"),
            buildActionButtons(),
            new Region() {{ setMinHeight(8); }},
            sectionLabel("Nova Partida"),
            buildDifficultyButtons()
        );
        return panel;
    }

    /** Grade 3x3 com os botões 1-9. */
    private GridPane buildNumPad() {
        GridPane numpad = new GridPane();
        numpad.setHgap(6);
        numpad.setVgap(6);
        numpad.setAlignment(Pos.CENTER);

        for (int i = 1; i <= 9; i++) {
            final int num = i;
            Button btn = numButton(String.valueOf(i));
            btn.setOnAction(e -> inputNumber(num));
            numpad.add(btn, (i - 1) % 3, (i - 1) / 3);
        }
        return numpad;
    }

    private HBox buildActionButtons() {
        Button btnClear = actionButton("⌫ Apagar", "#718096", "#4A5568");
        Button btnHint  = actionButton("💡 Dica",  "#276749", "#1A4731");
        btnClear.setOnAction(e -> clearSelected());
        btnHint.setOnAction(e -> hintSelected());

        HBox row = new HBox(8, btnClear, btnHint);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    private VBox buildDifficultyButtons() {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        for (Difficulty d : Difficulty.values()) {
            Button btn = diffButton(d);
            btn.setOnAction(e -> startNewGame(d));
            box.getChildren().add(btn);
        }
        return box;
    }

    private HBox buildFeedbackBar() {
        lblFeedback = new Label(" ");
        lblFeedback.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        lblFeedback.setTextFill(Color.web("#4A5568"));

        HBox bar = new HBox(lblFeedback);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(10, 24, 14, 24));
        bar.setStyle("-fx-background-color: " + BG_FEEDBACK + ";");
        return bar;
    }

    // ═════════════════════════════════════════════════════════════════════
    // LÓGICA DA UI
    // ═════════════════════════════════════════════════════════════════════

    private void startNewGame(Difficulty d) {
        selectedRow = -1;
        selectedCol = -1;
        controller.newGame(d);
        lblDifficulty.setText(d.getLabel());
        refreshBoard();
        showFeedback("Novo jogo iniciado! Boa sorte 🎯", "#2B6CB0");
    }

    private void selectCell(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        refreshBoard();
    }

    private void inputNumber(int num) {
        if (selectedRow < 0) { showFeedback("Selecione uma célula primeiro.", "#718096"); return; }

        Cell cell = controller.getBoard().getCell(selectedRow, selectedCol);
        if (cell.isFixed()) { showFeedback("Essa célula é fixa.", "#718096"); return; }

        try {
            MoveResult result = controller.placeValue(selectedRow, selectedCol, num);
            refreshBoard();
            updateStats();

            if (result.gameWon()) {
                showVictoryDialog();
            } else if (result.correct()) {
                showFeedback("✔ Correto!", "#276749");
                flashCell(selectedRow, selectedCol, CELL_HINT_BG);
            } else {
                showFeedback("✖ Incorreto. Tente de novo.", "#C53030");
                flashCell(selectedRow, selectedCol, CELL_ERROR_BG);
            }
        } catch (InvalidMoveException ex) {
            showFeedback(ex.getMessage(), "#718096");
        }
    }

    private void clearSelected() {
        if (selectedRow < 0) { showFeedback("Selecione uma célula primeiro.", "#718096"); return; }
        if (controller.clearCell(selectedRow, selectedCol)) {
            refreshBoard();
            showFeedback("Célula apagada.", "#718096");
        } else {
            showFeedback("Essa célula é fixa.", "#718096");
        }
    }

    private void hintSelected() {
        if (selectedRow < 0) { showFeedback("Selecione uma célula primeiro.", "#718096"); return; }

        Cell cell = controller.getBoard().getCell(selectedRow, selectedCol);
        if (cell.isFixed()) { showFeedback("Essa célula já é fixa.", "#718096"); return; }

        int val = controller.hint(selectedRow, selectedCol);
        refreshBoard();
        updateStats();
        showFeedback("💡 Dica: " + val + " na posição (" + (selectedRow+1) + "," + (selectedCol+1) + ")", "#276749");
        flashCell(selectedRow, selectedCol, CELL_HINT_BG);

        if (controller.isGameOver()) showVictoryDialog();
    }

    private void handleKeyPress(String text) {
        if (text == null || text.isEmpty()) return;
        if (text.matches("[1-9]")) {
            inputNumber(Integer.parseInt(text));
        } else if (text.equals("0") || text.equalsIgnoreCase("Delete") || text.equalsIgnoreCase("Back Space")) {
            clearSelected();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // RENDERIZAÇÃO DO TABULEIRO
    // ═════════════════════════════════════════════════════════════════════

    private void refreshBoard() {
        Board board = controller.getBoard();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Cell cell = board.getCell(row, col);
                Label lbl  = cellLabels[row][col];

                // Texto
                lbl.setText(cell.isEmpty() ? "" : String.valueOf(cell.getValue()));

                // Cor do número
                if (cell.isFixed()) {
                    lbl.setTextFill(Color.web(COLOR_FIXED));
                    lbl.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                } else {
                    lbl.setTextFill(Color.web(COLOR_PLAYER));
                    lbl.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
                }

                // Fundo da célula
                lbl.setStyle(buildCellStyle(row, col, cell));
            }
        }
    }

    /**
     * Monta o CSS inline de cada célula baseado no estado atual:
     * selecionada, relacionada (mesma linha/coluna/sub-grade), fixa ou normal.
     */
    private String buildCellStyle(int row, int col, Cell cell) {
        String bg;
        if (row == selectedRow && col == selectedCol) {
            bg = CELL_SELECTED;
        } else if (isRelated(row, col)) {
            bg = CELL_RELATED;
        } else if (cell.isFixed()) {
            bg = CELL_FIXED;
        } else {
            bg = CELL_NORMAL;
        }

        // Bordas: grossas nos limites de sub-grade
        double top    = (row % 3 == 0 && row != 0) ? GAP_THICK : GAP_THIN;
        double left   = (col % 3 == 0 && col != 0) ? GAP_THICK : GAP_THIN;
        double bottom = GAP_THIN;
        double right  = GAP_THIN;

        return bg + String.format(
            "-fx-border-color: #CBD5E0; -fx-border-width: %.0f %.0f %.0f %.0f;",
            top, right, bottom, left
        );
    }

    /** Uma célula é "relacionada" se está na mesma linha, coluna ou sub-grade da selecionada. */
    private boolean isRelated(int row, int col) {
        if (selectedRow < 0) return false;
        boolean sameLine = (row == selectedRow || col == selectedCol);
        boolean sameBox  = (row / 3 == selectedRow / 3 && col / 3 == selectedCol / 3);
        return sameLine || sameBox;
    }

    // ═════════════════════════════════════════════════════════════════════
    // FEEDBACK VISUAL
    // ═════════════════════════════════════════════════════════════════════

    /** Pisca o fundo de uma célula (animação de feedback). */
    private void flashCell(int row, int col, String flashStyle) {
        Label lbl = cellLabels[row][col];
        String originalStyle = lbl.getStyle();
        lbl.setStyle(flashStyle);

        Timeline restore = new Timeline(new KeyFrame(
            Duration.millis(600),
            e -> lbl.setStyle(originalStyle)
        ));
        restore.play();
    }

    /** Exibe mensagem na barra inferior com fade-out automático após 3s. */
    private void showFeedback(String message, String hexColor) {
        if (feedbackTimer != null) feedbackTimer.stop();

        lblFeedback.setText(message);
        lblFeedback.setTextFill(Color.web(hexColor));
        lblFeedback.setOpacity(1.0);

        FadeTransition fade = new FadeTransition(Duration.millis(500), lblFeedback);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        feedbackTimer = new Timeline(new KeyFrame(Duration.seconds(3), e -> fade.play()));
        feedbackTimer.play();
    }

    private void updateStats() {
        Board b = controller.getBoard();
        lblMoves.setText("Movimentos: " + b.getMoves());
        lblErrors.setText("Erros: " + b.getErrors());
    }

    private void showVictoryDialog() {
        Board b = controller.getBoard();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Parabéns!");
        alert.setHeaderText("🎉 Puzzle resolvido!");
        alert.setContentText(
            "Dificuldade: " + controller.getDifficulty() + "\n" +
            "Movimentos: "  + b.getMoves() + "\n" +
            "Erros: "       + b.getErrors()
        );

        ButtonType novoJogo = new ButtonType("Novo Jogo");
        ButtonType fechar   = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(novoJogo, fechar);

        alert.showAndWait().ifPresent(btn -> {
            if (btn == novoJogo) startNewGame(controller.getDifficulty());
        });
    }

    // ═════════════════════════════════════════════════════════════════════
    // HELPERS DE UI
    // ═════════════════════════════════════════════════════════════════════

    private Label statsLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", 14));
        lbl.setTextFill(Color.web("#BEE3F8"));
        return lbl;
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#718096"));
        return lbl;
    }

    private Label sep() {
        Label s = new Label("  |  ");
        s.setTextFill(Color.web("#4A6FA5"));
        s.setFont(Font.font("Arial", 14));
        return s;
    }

    private Button numButton(String text) {
        Button btn = new Button(text);
        btn.setMinSize(50, 50);
        btn.setMaxSize(50, 50);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        btn.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-border-color: #CBD5E0;" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #EBF4FF;" +
            "-fx-border-color: #2B6CB0;" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-border-color: #CBD5E0;" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    private Button actionButton(String text, String bg, String bgHover) {
        Button btn = new Button(text);
        btn.setPrefWidth(88);
        btn.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        btn.setTextFill(Color.WHITE);
        String style = String.format(
            "-fx-background-color: %s; -fx-background-radius: 6; -fx-cursor: hand;", bg);
        String hoverStyle = String.format(
            "-fx-background-color: %s; -fx-background-radius: 6; -fx-cursor: hand;", bgHover);
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e  -> btn.setStyle(style));
        return btn;
    }

    private Button diffButton(Difficulty d) {
        String[] colors = {"#2B6CB0", "#276749", "#C53030"};
        String[] hovers = {"#2C5282", "#1A4731", "#9B2C2C"};
        int idx = d.ordinal();
        Button btn = new Button(d.getLabel());
        btn.setPrefWidth(160);
        btn.setFont(Font.font("Arial", 14));
        btn.setTextFill(Color.WHITE);
        String style = String.format(
            "-fx-background-color: %s; -fx-background-radius: 6; -fx-cursor: hand;", colors[idx]);
        String hover = String.format(
            "-fx-background-color: %s; -fx-background-radius: 6; -fx-cursor: hand;", hovers[idx]);
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(style));
        return btn;
    }
}
