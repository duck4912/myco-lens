package com.example.projekt;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MushroomDao_Impl implements MushroomDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Mushroom> __insertionAdapterOfMushroom;

  public MushroomDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMushroom = new EntityInsertionAdapter<Mushroom>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `mushrooms` (`id`,`nazwa`,`jadalnosc`,`opis`,`nazwaObrazka`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Mushroom entity) {
        statement.bindLong(1, entity.id);
        if (entity.nazwa == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.nazwa);
        }
        if (entity.jadalnosc == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.jadalnosc);
        }
        if (entity.opis == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.opis);
        }
        if (entity.nazwaObrazka == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.nazwaObrazka);
        }
      }
    };
  }

  @Override
  public void insertMushroom(final Mushroom mushroom) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfMushroom.insert(mushroom);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Mushroom> getAllMushrooms() {
    final String _sql = "SELECT * FROM mushrooms";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfNazwa = CursorUtil.getColumnIndexOrThrow(_cursor, "nazwa");
      final int _cursorIndexOfJadalnosc = CursorUtil.getColumnIndexOrThrow(_cursor, "jadalnosc");
      final int _cursorIndexOfOpis = CursorUtil.getColumnIndexOrThrow(_cursor, "opis");
      final int _cursorIndexOfNazwaObrazka = CursorUtil.getColumnIndexOrThrow(_cursor, "nazwaObrazka");
      final List<Mushroom> _result = new ArrayList<Mushroom>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Mushroom _item;
        final String _tmpNazwa;
        if (_cursor.isNull(_cursorIndexOfNazwa)) {
          _tmpNazwa = null;
        } else {
          _tmpNazwa = _cursor.getString(_cursorIndexOfNazwa);
        }
        final String _tmpJadalnosc;
        if (_cursor.isNull(_cursorIndexOfJadalnosc)) {
          _tmpJadalnosc = null;
        } else {
          _tmpJadalnosc = _cursor.getString(_cursorIndexOfJadalnosc);
        }
        final String _tmpOpis;
        if (_cursor.isNull(_cursorIndexOfOpis)) {
          _tmpOpis = null;
        } else {
          _tmpOpis = _cursor.getString(_cursorIndexOfOpis);
        }
        final String _tmpNazwaObrazka;
        if (_cursor.isNull(_cursorIndexOfNazwaObrazka)) {
          _tmpNazwaObrazka = null;
        } else {
          _tmpNazwaObrazka = _cursor.getString(_cursorIndexOfNazwaObrazka);
        }
        _item = new Mushroom(_tmpNazwa,_tmpJadalnosc,_tmpOpis,_tmpNazwaObrazka);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Mushroom getMushroomById(final int id) {
    final String _sql = "SELECT * FROM mushrooms WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfNazwa = CursorUtil.getColumnIndexOrThrow(_cursor, "nazwa");
      final int _cursorIndexOfJadalnosc = CursorUtil.getColumnIndexOrThrow(_cursor, "jadalnosc");
      final int _cursorIndexOfOpis = CursorUtil.getColumnIndexOrThrow(_cursor, "opis");
      final int _cursorIndexOfNazwaObrazka = CursorUtil.getColumnIndexOrThrow(_cursor, "nazwaObrazka");
      final Mushroom _result;
      if (_cursor.moveToFirst()) {
        final String _tmpNazwa;
        if (_cursor.isNull(_cursorIndexOfNazwa)) {
          _tmpNazwa = null;
        } else {
          _tmpNazwa = _cursor.getString(_cursorIndexOfNazwa);
        }
        final String _tmpJadalnosc;
        if (_cursor.isNull(_cursorIndexOfJadalnosc)) {
          _tmpJadalnosc = null;
        } else {
          _tmpJadalnosc = _cursor.getString(_cursorIndexOfJadalnosc);
        }
        final String _tmpOpis;
        if (_cursor.isNull(_cursorIndexOfOpis)) {
          _tmpOpis = null;
        } else {
          _tmpOpis = _cursor.getString(_cursorIndexOfOpis);
        }
        final String _tmpNazwaObrazka;
        if (_cursor.isNull(_cursorIndexOfNazwaObrazka)) {
          _tmpNazwaObrazka = null;
        } else {
          _tmpNazwaObrazka = _cursor.getString(_cursorIndexOfNazwaObrazka);
        }
        _result = new Mushroom(_tmpNazwa,_tmpJadalnosc,_tmpOpis,_tmpNazwaObrazka);
        _result.id = _cursor.getInt(_cursorIndexOfId);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
