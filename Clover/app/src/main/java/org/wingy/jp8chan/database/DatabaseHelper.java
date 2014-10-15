/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
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
package org.wingy.jp8chan.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.wingy.jp8chan.core.model.Board;
import org.wingy.jp8chan.core.model.Loadable;
import org.wingy.jp8chan.core.model.Pin;
import org.wingy.jp8chan.core.model.SavedReply;
import org.wingy.jp8chan.utils.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "ChanDB";
    private static final int DATABASE_VERSION = 13;

    public Dao<Pin, Integer> pinDao;
    public Dao<Loadable, Integer> loadableDao;
    public Dao<SavedReply, Integer> savedDao;
    public Dao<Board, Integer> boardsDao;

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;

        try {
            pinDao = getDao(Pin.class);
            loadableDao = getDao(Loadable.class);
            savedDao = getDao(SavedReply.class);
            boardsDao = getDao(Board.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Pin.class);
            TableUtils.createTable(connectionSource, Loadable.class);
            TableUtils.createTable(connectionSource, SavedReply.class);
            TableUtils.createTable(connectionSource, Board.class);
        } catch (SQLException e) {
            Logger.e(TAG, "Error creating db", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Logger.i(TAG, "Upgrading database from " + oldVersion + " to " + newVersion);
        if (oldVersion < 12) {
            try {
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN perPage INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN pages INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN maxFileSize INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN maxWebmSize INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN maxCommentChars INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN bumpLimit INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN imageLimit INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN cooldownThreads INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN cooldownReplies INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN cooldownImages INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN cooldownRepliesIntra INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN cooldownImagesIntra INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN spoilers INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN customSpoilers INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN userIds INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN codeTags INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN preuploadCaptcha INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN countryFlags INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN trollFlags INTEGER;");
                boardsDao.executeRawNoArgs("ALTER TABLE board ADD COLUMN mathTags INTEGER;");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                Map<String, Object> fieldValues = new HashMap<>();
                fieldValues.put("value", "f");
                List<Board> list = boardsDao.queryForFieldValues(fieldValues);
                if (list != null) {
                    boardsDao.delete(list);
                    Logger.i(TAG, "Deleted f board");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (oldVersion < 13) {
            try {
                boardsDao.executeRawNoArgs("ALTER TABLE pin ADD COLUMN isError SMALLINT;");
                boardsDao.executeRawNoArgs("ALTER TABLE pin ADD COLUMN thumbnailUrl VARCHAR;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void reset() {
        Logger.i(TAG, "Resetting database!");

        if (context.deleteDatabase(DATABASE_NAME)) {
            Logger.i(TAG, "Deleted database");
        }
    }

    private void reset(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.dropTable(connectionSource, Pin.class, true);
            TableUtils.dropTable(connectionSource, Loadable.class, true);
            TableUtils.dropTable(connectionSource, SavedReply.class, true);
            TableUtils.dropTable(connectionSource, Board.class, true);

            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
