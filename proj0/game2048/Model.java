package game2048;

import java.util.Formatter;
import java.util.Observable;
//import java.util.ArrayList;
//import java.util.List;


/** The state of a game of 2048.
 *  @author TODO:　Huili
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private final Board _board;
    /** Current score. */
    private int _score;
    /** Maximum score so far.  Updated when game ends. */
    private int _maxScore;
    /** True iff game is ended. */
    private boolean _gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        _board = new Board(size);
        _score = _maxScore = 0;
        _gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        _board = new Board(rawValues);
        this._score = score;
        this._maxScore = maxScore;
        this._gameOver = gameOver;
    }

    /** Same as above, but gameOver is false. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore) {
        this(rawValues, score, maxScore, false);
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     * */
    public Tile tile(int col, int row) {
        return _board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return _board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (_gameOver) {
            _maxScore = Math.max(_score, _maxScore);
        }
        return _gameOver;
    }

    /** Return the current score. */
    public int score() {
        return _score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return _maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        _score = 0;
        _gameOver = false;
        _board.clear();
        setChanged();
    }

    /** Allow initial game board to announce a hot start to the GUI. */
    public void hotStartAnnounce() {
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        _board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE.
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     */

    /** Check the c r inside the board. */
    public boolean inBounds(int c, int r) {
        return (0 <= c && c < _board.size() && 0 <= r && r < _board.size());
    }

    /** Remove the gaps. */
    public boolean tiltRemoveNull(int c, int r, Side side) {
        int nextCol = c + side.originaldCol();
        int nextRow = r + side.originaldRow();
        boolean changed = false;

        while (inBounds(nextCol, nextRow)) {
            Tile currTile = _board.tile(c, r);
            Tile nextTile = _board.tile(nextCol, nextRow);

            if (currTile == null && nextTile != null) {
                _board.move(c , r, nextTile);
                changed = true;
            }

            c = nextCol;
            r = nextRow;
            nextCol = c + side.originaldCol();
            nextRow = r + side.originaldRow();
        }
        return changed;
    }

    /** Merge the tiles with the same value. */
    public void tiltMerge(int c, int r, Side side) {
        int nextCol = c + side.originaldCol();
        int nextRow = r + side.originaldRow();
        while (inBounds(nextCol, nextRow)) {
            Tile currTile = _board.tile(c, r);
            Tile nextTile = _board.tile(nextCol, nextRow);

            if (currTile != null && nextTile != null
                    && currTile.value() == nextTile.value() && _board.move(c , r, nextTile)) {
                tiltRemoveNull(nextCol, nextRow, side);
                _score += _board.tile(c, r).value();
            }

            c = nextCol;
            r = nextRow;
            nextCol = c + side.originaldCol();
            nextRow = r + side.originaldRow();
        }
    }


    public void tilt(Side side) {
        // TODO: Fill in this function.
        side = Side.opposite(side);

        int originalCol = side.originalCol() * (_board.size() - 1);
        int originalRow = side.originalRow() * (_board.size() - 1);
        for (int k = 0; k < _board.size(); k++) {

            while (tiltRemoveNull(originalCol, originalRow, side)) {
            }

            tiltMerge(originalCol, originalRow, side);
            if (side.originaldCol() == 0) {
                originalCol += side.originaldRow();
            } else {
                originalRow -= side.originaldCol();
            }
        }
        checkGameOver();
    }
//    /** Redundancy method for tile */
//    public void tilt(Side side) {
//        // TODO: Fill in this function.
//        List<Tile> change_tile = new ArrayList<Tile>();
//
//        if (side == Side.NORTH) {
//            for (int c = 0; c < _board.size(); c += 1) {
//                int up = 0;
//                for (int r = _board.size() - 1; r >= 0; r -= 1) {
//                    Tile t = _board.tile(c, r);
//                    if (t == null) {
//                        up += 1;
//                    } else {
//                        _board.move(c, r + up, t);
//                        if (r + up == _board.size() - 1) {
//                            continue;
//                        }
//                        Tile curr_t = _board.tile(c, r + up);
//                        Tile next_t = _board.tile(c, r + up + 1);
//                        if (!change_tile.contains(next_t)
//                                && t.value() == next_t.value()
//                                && _board.move(c, r + up + 1, curr_t)) {
//                            change_tile.add(_board.tile(c, r + up + 1));
//                            _score += _board.tile(c, r + up + 1).value();
//                            up += 1;
//                        }
//                    }
//                }
//            }
//        }
//        if (side == Side.SOUTH) {
//            for (int c = 0; c < _board.size(); c += 1) {
//                int down = 0;
//                for (int r = 0; r < _board.size(); r += 1) {
//                    Tile t = _board.tile(c, r);
//                    if (t == null) {
//                        down += 1;
//                    }
//                    else {
//                        _board.move(c, r - down, t);
//                        if (r - down == 0) {
//                            continue;
//                        }
//                        Tile curr_t = _board.tile(c, r - down);
//                        Tile next_t = _board.tile(c, r - down - 1);
//                        if (!change_tile.contains(next_t)
//                                && t.value() == next_t.value()
//                                && _board.move(c, r - down - 1, curr_t)) {
//                            change_tile.add(_board.tile(c, r - down - 1));
//                            _score += _board.tile(c, r - down - 1).value();
//                            down += 1;
//                        }
//                    }
//                }
//            }
//        }
//        if (side == Side.EAST) {
//            for (int r = 0; r < _board.size(); r += 1) {
//                int right = 0;
//                for (int c = _board.size() - 1; c >= 0; c -= 1) {
//                    Tile t = _board.tile(c, r);
//                    if (t == null) {
//                        right += 1;
//                    }
//                    else {
//                        _board.move(c + right, r, t);
//                        if (c + right == _board.size() - 1) {
//                            continue;
//                        }
//                        Tile curr_t = _board.tile(c + right, r);
//                        Tile next_t = _board.tile(c + right + 1, r);
//                        if (!change_tile.contains(next_t)
//                                && t.value() == _board.tile(c + right + 1, r).value()
//                                && _board.move(c + right + 1, r, curr_t)) {
//                            change_tile.add(_board.tile(c + right + 1, r));
//                            _score += _board.tile(c + right +1, r).value();
//                            right += 1;
//                        }
//                    }
//                }
//            }
//        }
//        if (side == Side.WEST) {
//            for (int r = 0; r < _board.size(); r += 1) {
//                int left = 0;
//                for (int c = 0; c < _board.size(); c += 1) {
//                    Tile t = _board.tile(c, r);
//                    if (t == null) {
//                        left += 1;
//                    } else {
//                        _board.move(c - left, r, t);
//                        if (c - left == 0) {
//                            continue;
//                        }
//                        Tile curr_t = _board.tile(c - left, r);
//                        Tile next_t = _board.tile(c - left - 1, r);
//                        if (!change_tile.contains(next_t) && c - left > 0
//                                && t.value() == _board.tile(c - left - 1, r).value()
//                                && _board.move(c - left - 1, r, curr_t)) {
//                            change_tile.add(_board.tile(c - left - 1, r));
//                            _score += _board.tile(c - left - 1, r).value();
//                            left += 1;
//                        }
//                    }
//                }
//            }
//        }
//        checkGameOver();
//    }


    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        _gameOver = checkGameOver(_board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.

        for (int i = 0; i < b.size(); i += 1) {
            for (int j = 0; j < b.size(); j += 1) {
                if (b.tile(i,j) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by this.MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < b.size(); i += 1) {
            for (int j = 0; j < b.size(); j += 1) {
                if (b.tile(i,j) != null) {
                    if (b.tile(i,j).value() == MAX_PIECE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        if (emptySpaceExists(b)) {
            return true;
        }
        for (int i = 1; i < b.size(); i += 1) {
            for (int j = 1; j < b.size(); j += 1) {
                if (b.tile(i,j).value() == b.tile(i-1,j).value()) {
                    return true;
                }
                if (b.tile(i,j).value() == b.tile(i,j-1).value()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns the model as a string, used for debugging. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    /** Returns whether two models are equal. */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    /** Returns hash code of Model’s string. */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
