package droidkit.sqlite;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import droidkit.BuildConfig;
import droidkit.test.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteQueryTest {

    static final SQLiteUser[] USERS = new SQLiteUser[]{
            new SQLiteUser("Liam", 20, 70.5, new byte[]{0, 0, 0, 0, 0}),
            new SQLiteUser("Olivia", 22, 60.5, new byte[]{0, 1, 0, 0, 0}),
            new SQLiteUser("Jacob", 25, 55.8, new byte[]{0, 0, 1, 0, 0}),
            new SQLiteUser("Isabella", 21, 45.0, new byte[]{0, 0, 0, 1, 0}),
            new SQLiteUser("Ethan", 28, 80.75, new byte[]{0, 0, 0, 0, 1}),
            new SQLiteUser("Mia", 23, 50.3, new byte[]{1, 1, 0, 0, 0}),
            new SQLiteUser("Alexander", 30, 66.7, new byte[]{0, 1, 1, 0, 0}),
            new SQLiteUser("Abigail", 19, 45.2, new byte[]{0, 0, 1, 1, 0}),
            new SQLiteUser("James", 15, 40.4, new byte[]{0, 0, 0, 1, 1}),
            new SQLiteUser("Charlotte", 18, 48.1, new byte[]{1, 1, 1, 0, 0}),
    };

    static {
        SQLite.useInMemoryDb();
    }

    private SQLite mSQLite;

    @Before
    public void setUp() throws Exception {
        mSQLite = SQLite.of(RuntimeEnvironment.application);
        mSQLite.beginTransaction();
        for (final SQLiteUser user : USERS) {
            mSQLite.execSQL("INSERT INTO users(name, age, weight, avatar, enabled) VALUES(?, ?, ?, ?, ?)",
                    user.mName, user.mAge, user.mWeight, user.mAvatar, (user.mAge > 18));
        }
        mSQLite.endTransaction(true);
    }

    @Test
    public void testDistinct() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).
                distinct();
        Assert.assertEquals("SELECT DISTINCT * FROM users", query.toString());
        Assert.assertEquals(USERS.length, query.list().size());
    }

    @Test
    public void testEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class)
                .equalTo("name", "James");
        Assert.assertEquals("SELECT * FROM users WHERE name = ?", query.toString());
        Assert.assertArrayEquals(new String[]{"James"}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals("James", users.get(0).getName());
    }

    @Test
    public void testNotEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).notEqualTo("age", 25);
        Assert.assertEquals("SELECT * FROM users WHERE age <> ?", query.toString());
        Assert.assertArrayEquals(new Object[]{25}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(9, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertNotEquals(25, user.getAge());
        }
    }

    @Test
    public void testLessThan() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).lessThan("weight", 50);
        Assert.assertEquals("SELECT * FROM users WHERE weight < ?", query.toString());
        Assert.assertArrayEquals(new Object[]{50}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(4, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getWeight() < 50);
        }
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).lessThanOrEqualTo("weight", 70.5);
        Assert.assertEquals("SELECT * FROM users WHERE weight <= ?", query.toString());
        Assert.assertArrayEquals(new Object[]{70.5}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(9, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getWeight() <= 70.5);
        }
    }

    @Test
    public void testGreaterThan() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).greaterThan("age", 25);
        Assert.assertEquals("SELECT * FROM users WHERE age > ?", query.toString());
        Assert.assertArrayEquals(new Object[]{25}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(2, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getAge() > 25);
        }
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).greaterThanOrEqualTo("age", 25);
        Assert.assertEquals("SELECT * FROM users WHERE age >= ?", query.toString());
        Assert.assertArrayEquals(new Object[]{25}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(3, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getAge() >= 25);
        }
    }

    @Test
    public void testLike() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).like("name", "Ja%");
        Assert.assertEquals("SELECT * FROM users WHERE name LIKE ?", query.toString());
        Assert.assertArrayEquals(new Object[]{"Ja%"}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(2, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getName().startsWith("Ja"));
        }
    }

    @Test
    public void testBetween() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).between("age", 22, 28);
        Assert.assertEquals("SELECT * FROM users WHERE age BETWEEN ? AND ?", query.toString());
        Assert.assertArrayEquals(new Object[]{22, 28}, query.bindArgs());
        Assert.assertEquals(4, query.list().size());
    }

    @Test
    public void testIsTrue() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).isTrue("enabled");
        Assert.assertEquals("SELECT * FROM users WHERE enabled = ?", query.toString());
        Assert.assertArrayEquals(new Object[]{1}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(8, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.isEnabled());
        }
    }

    @Test
    public void testIsFalse() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).isFalse("enabled");
        Assert.assertEquals("SELECT * FROM users WHERE enabled = ?", query.toString());
        Assert.assertArrayEquals(new Object[]{0}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(2, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertFalse(user.isEnabled());
        }
    }

    @Test
    public void testIsNull() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).isNull("name");
        Assert.assertEquals("SELECT * FROM users WHERE name IS NULL", query.toString());
    }

    @Test
    public void testNotNull() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).notNull("name");
        Assert.assertEquals("SELECT * FROM users WHERE name NOT NULL", query.toString());
    }

    @Test
    public void testAppendWhere() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).appendWhere("_id IN(?, ?, ?)", 1, 2, 3);
        Assert.assertEquals("SELECT * FROM users WHERE _id IN(?, ?, ?)", query.toString());
        Assert.assertArrayEquals(new Object[]{1, 2, 3}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(3, users.size());
    }

    @Test
    public void testGroupBy() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).groupBy("_id");
        Assert.assertEquals("SELECT * FROM users GROUP BY _id", query.toString());
        mSQLite.execSQL(query.toString());
    }

    @Test
    public void testHaving() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class)
                .groupBy("name")
                .having("age > ?", 20);
        Assert.assertEquals("SELECT * FROM users GROUP BY name HAVING age > ?", query.toString());
        Assert.assertArrayEquals(new Object[]{20}, query.bindArgs());
        mSQLite.execSQL(query.toString());
    }

    @Test
    public void testOrderBy() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).orderBy("name");
        Assert.assertEquals("SELECT * FROM users ORDER BY name ASC", query.toString());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(USERS.length, users.size());
        Assert.assertEquals("Abigail", users.get(0).getName());
    }

    @Test
    public void testOrderByDesc() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).orderBy("name", false);
        Assert.assertEquals("SELECT * FROM users ORDER BY name DESC", query.toString());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(USERS.length, users.size());
        Assert.assertEquals("Olivia", users.get(0).getName());
    }

    @Test
    public void testLimit() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).limit(5);
        Assert.assertEquals("SELECT * FROM users LIMIT 5", query.toString());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(5, users.size());
        Assert.assertEquals("Liam", users.get(0).getName());
        Assert.assertEquals("Ethan", users.get(4).getName());
    }

    @Test
    public void testLimitOffset() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).offsetLimit(5, 2);
        Assert.assertEquals("SELECT * FROM users LIMIT 5, 2", query.toString());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(2, users.size());
        Assert.assertEquals("Mia", users.get(0).getName());
        Assert.assertEquals("Alexander", users.get(1).getName());

    }

    @Test
    public void testList() throws Exception {
        final List<SQLiteUser> users = mSQLite.where(SQLiteUser.class).list();
        Assert.assertEquals(USERS.length, users.size());
        for (int i = 0; i < USERS.length; ++i) {
            Assert.assertEquals(USERS[i].mName, users.get(i).getName());
        }
    }

    @Test
    public void testRemove() throws Exception {
        final int affectedRows = mSQLite.where(SQLiteUser.class)
                .equalTo("name", "Mia")
                .remove();
        Assert.assertEquals(1, affectedRows);
        final List<SQLiteUser> users = mSQLite.where(SQLiteUser.class).list();
        for (final SQLiteUser user : users) {
            Assert.assertNotEquals("Mia", user.getName());
        }
    }

    @After
    public void tearDown() throws Exception {
        mSQLite.execSQL("DELETE FROM users;");
    }

}