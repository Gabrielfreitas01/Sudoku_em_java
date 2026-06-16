package sudoku.model;

import java.util.HashSet;
import java.util.Set;

public class Cell {

    private int value;
    private final boolean fixed;
    private final Set<Integer> notes;

    public Cell(int value, boolean fixed) {
        this.value = value;
        this.fixed = fixed;
        this.notes = new HashSet<>();
    }

    public static Cell empty() { return new Cell(0, false); }
    public static Cell fixed(int value) { return new Cell(value, true); }

    public int getValue() { return value; }

    public void setValue(int value) {
        if (fixed) throw new UnsupportedOperationException("Célula fixa não pode ser alterada.");
        this.value = value;
    }

    public boolean isEmpty() { return value == 0; }
    public boolean isFixed() { return fixed; }

    public void addNote(int note)    { notes.add(note); }
    public void removeNote(int note) { notes.remove(note); }
    public void clearNotes()         { notes.clear(); }
    public Set<Integer> getNotes()   { return new HashSet<>(notes); }

    public Cell copy() {
        Cell copy = new Cell(this.value, this.fixed);
        copy.notes.addAll(this.notes);
        return copy;
    }

    @Override
    public String toString() { return isEmpty() ? "." : String.valueOf(value); }
}
