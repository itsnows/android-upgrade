package com.upgradelibrary;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.upgradelibrary.data.UpgradeRepository;
import com.upgradelibrary.data.bean.UpgradeBuffer;
import com.upgradelibrary.data.bean.UpgradeVersion;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/9 9:56
 * <p>
 * UpgradeRepositoryTest
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class UpgradeRepositoryTest {
    private static final String TAG = UpgradeRepositoryTest.class.getSimpleName();
    private UpgradeRepository repository;


    @Before
    void attach() {
        repository = new UpgradeRepository(InstrumentationRegistry.getContext());
    }

    @After
    void detach() {
        repository = null;
    }

    @Test
    void testGetUpgradeVersion() {
        UpgradeVersion upgradeVersion = repository.getUpgradeVersion(1);
        if (upgradeVersion == null || upgradeVersion.isIgnored()) {
            Log.i(TAG, "未忽略的版本");
        } else {
            Log.i(TAG, "已忽略的版本");
        }
    }

    @Test
    void testPetUpgradeVersion() {
        UpgradeVersion upgradeVersion = new UpgradeVersion(1, false);
        repository.putUpgradeVersion(upgradeVersion);
    }

    @Test
    void testGetUpgradeBuffer() {
        UpgradeBuffer upgradeBuffer = repository.getUpgradeBuffer("http://www.baidu.com");
        if (upgradeBuffer == null) {
            Log.i(TAG, "未缓存的版本");
        } else {
            Log.i(TAG, "已缓存的版本");
        }
    }

    @Test
    void testPutUpgradeBuffer() {
        UpgradeBuffer upgradeBuffer = new UpgradeBuffer("http://www.baidu.com",
                null,
                1024L,
                1024L,
                new CopyOnWriteArrayList<UpgradeBuffer.BufferPart>(),
                System.currentTimeMillis());
        repository.putUpgradeBuffer(upgradeBuffer);
    }

}
