package droidkit.sqlite;

import android.net.Uri;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.AllTypesBean;
import droidkit.sqlite.util.SQLiteTestEnv;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteSchemaTest {

    @Before
    public void setUp() throws Exception {
        SQLiteTestEnv.registerProvider();
    }

    @Test
    public void testResolveTable() throws Exception {
        Assert.assertEquals(AllTypesBean.TABLE, SQLiteSchema.resolveTable(AllTypesBean.class));
    }

    @Test
    public void testResolveUri() throws Exception {
        final Uri expected = new Uri.Builder()
                .scheme("content")
                .authority(BuildConfig.APPLICATION_ID)
                .path(AllTypesBean.TABLE)
                .build();
        final Uri actual = SQLiteSchema.resolveUri(AllTypesBean.class);
        Assert.assertEquals(expected, actual);
    }

}