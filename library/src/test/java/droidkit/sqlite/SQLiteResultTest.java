package droidkit.sqlite;

import android.database.Cursor;
import android.os.StrictMode;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import droidkit.BuildConfig;
import droidkit.database.CursorUtils;
import droidkit.io.IOUtils;
import droidkit.test.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteResultTest {

    static {
        SQLite.useInMemoryDb();
        StrictMode.enableDefaults();
    }

    private SQLite mSQLite;

    @Before
    public void setUp() throws Exception {
        mSQLite = SQLite.of(RuntimeEnvironment.application);
        mSQLite.beginTransaction();
        for (int i = 1; i <= 10; ++i) {
            mSQLite.execSQL("INSERT INTO users(name) VALUES(?);", ("User #" + i));
        }
        mSQLite.endTransaction(true);
    }

    @Test
    public void testSize() throws Exception {
        final Cursor cursor = mSQLite.rawQuery("SELECT * FROM users;");
        try {
            Assert.assertEquals(cursor.getCount(), mSQLite.where(SQLiteUser.class).list().size());
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testRemove() throws Exception {
        final List<SQLiteUser> users = mSQLite.where(SQLiteUser.class).list();
        users.remove(4);
        Assert.assertEquals(9, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertNotEquals("User #5", user.getName());
        }
        final Cursor cursor = mSQLite.rawQuery("SELECT * FROM users;");
        try {
            if (cursor.moveToFirst()) {
                do {
                    Assert.assertNotEquals("User #5", CursorUtils.getString(cursor, "name"));
                } while (cursor.moveToNext());
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testList() throws Exception {
        final List<SQLiteUser> users = mSQLite.where(SQLiteUser.class).list();
        Assert.assertEquals(10, users.size());
        for (int i = 0; i < 10; ++i) {
            Assert.assertEquals(("User #" + (i + 1)), users.get(i).getName());
        }
    }

    @After
    public void tearDown() throws Exception {
        mSQLite.execSQL("DELETE FROM users;");
    }

}