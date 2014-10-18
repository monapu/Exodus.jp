/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 * Copyright (C) 2014  wingy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wingy.chan.core.manager;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.wingy.chan.ChanApplication;
import org.wingy.chan.chan.ChanUrls;
import org.wingy.chan.core.model.Board;
import org.wingy.chan.core.net.BoardsRequest;
import org.wingy.chan.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardManager {
    private static final String TAG = "BoardManager";
    private static final Comparator<Board> savedOrder = new Comparator<Board>() {
        @Override
        public int compare(Board lhs, Board rhs) {
            return lhs.order < rhs.order ? -1 : 1;
        }
    };

    private List<Board> allBoards;
    private Map<String, Board> allBoardsByValue = new HashMap<>();

    private List<BoardChangeListener> listeners = new ArrayList<>();

    public BoardManager() {
        loadBoards();
    }

    public Board getBoardByValue(String value) {
        return allBoardsByValue.get(value);
    }

    public List<Board> getSavedBoards() {
        List<Board> saved = new ArrayList<>(allBoards.size());

        for (Board b : allBoards)
            saved.add(b);

        Collections.sort(saved, savedOrder);

        return saved;
    }

    public void saveBoard(Board board) {
        allBoards.add(board);
        storeBoards();
    }

    public void removeBoard(String board) {
        for (Board b : allBoards) {
            if (b.key == board) {
                allBoards.remove(b);
                ChanApplication.getDatabaseManager().removeBoard(b);
                updateByValueMap();
                notifyChanged();
                return;
            }
        }
    }

    public boolean getBoardExists(String board) {
        for (Board e : getSavedBoards()) {
            if (e.value.equals(board)) {
                return true;
            }
        }

        return false;
    }

    public void updateSavedBoards() {
        ChanApplication.getDatabaseManager().updateBoards(allBoards);

        notifyChanged();
    }

    public void addListener(BoardChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(BoardChangeListener listener) {
        listeners.remove(listener);
    }

    private void updateByValueMap() {
        allBoardsByValue.clear();
        for (Board test : allBoards) {
            allBoardsByValue.put(test.value, test);
        }
    }

    private void notifyChanged() {
        for (BoardChangeListener l : listeners) {
            l.onBoardsChanged();
        }
    }

    private void storeBoards() {
        Logger.d(TAG, "Storing boards in database");

        updateByValueMap();

        ChanApplication.getDatabaseManager().setBoards(allBoards);
        notifyChanged();
    }

    private void loadBoards() {
        allBoards = ChanApplication.getDatabaseManager().getBoards();
        if (allBoards.size() == 0) {
            Logger.d(TAG, "Loading default boards");
            allBoards = getDefaultBoards();
            storeBoards();
        }
        updateByValueMap();
    }

    private List<Board> getDefaultBoards() {
        List<Board> list = new ArrayList<>();
        list.add(new Board("Video Games", "v", true));
        list.add(new Board("Animu and Mango", "a", true));
        list.add(new Board("Comics & Cartoons", "co", true));
        list.add(new Board("Politically Incorrect", "pol", true));
        list.add(new Board("GamerGate", "gg", true));
        list.add(new Board("Technology", "tech", true));
        list.add(new Board("Traditional Games", "tg", true));
        list.add(new Board("Sports", "sp", true));
        list.add(new Board("Fitness", "fit", true));
        return list;
    }

    public interface BoardChangeListener {
        public void onBoardsChanged();
    }
}
