package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.Active;
import droidkit.sqlite.util.SQLiteTestEnv;
import droidkit.util.Cursors;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteResultTest {

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        mProvider = SQLiteTestEnv.registerProvider();
        SQLite.beginTransaction();
        for (int i = 0; i < 10; ++i) {
            final Active bean = new Active();
            bean.setText("Bean #" + (i + 1));
            SQLite.save(bean);
        }
        SQLite.endTransaction();
    }

    @Test
    public void testGet() throws Exception {
        checkBeans(SQLite.where(Active.class).list());
    }

    @Test
    public void testAdd() throws Exception {
        final List<Active> beans = SQLite.where(Active.class).list();
        final Active newBean = new Active();
        newBean.setText("Added Bean");
        beans.add(newBean);
        checkBeans(beans);
    }

    @Test
    public void testRemove() throws Exception {
        final List<Active> beans = SQLite.where(Active.class).list();
        beans.remove(5);
        checkBeans(beans);
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

    private void checkBeans(@NonNull List<Active> beans) {
        final Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(Active.class), null, null, null, null);
        Assert.assertEquals(cursor.getCount(), beans.size());
        Assert.assertTrue(cursor.moveToFirst());
        do {
            Assert.assertEquals(Cursors.getString(cursor, "text"), beans.get(cursor.getPosition()).getText());
        } while (cursor.moveToNext());
        cursor.close();
    }

}