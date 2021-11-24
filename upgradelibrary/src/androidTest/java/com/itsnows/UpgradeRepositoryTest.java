package com.itsnows;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.itsnows.android.upgrade.UpgradeLogger;
import com.itsnows.android.upgrade.model.UpgradeRepository;
import com.itsnows.android.upgrade.model.bean.UpgradeBuffer;
import com.itsnows.android.upgrade.model.bean.UpgradeVersion;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author itsnows, xue.com.fei@gmail.com
 * @since 2018/2/9 9:56
 * <p>
 * UpgradeRepositoryTest
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class UpgradeRepositoryTest {
    private static final String TAG = UpgradeRepositoryTest.class.getSimpleName();
    private UpgradeRepository repository;

    @Before
    public void attach() {
        repository = UpgradeRepository.getInstance(InstrumentationRegistry.getContext());
    }

    @After
    public void detach() {
        repository = null;
    }

    @Test
    public void testGetUpgradeVersion() {
        UpgradeVersion upgradeVersion = repository.getUpgradeVersion(1);
        if (upgradeVersion == null || upgradeVersion.isIgnored()) {
            UpgradeLogger.i(TAG, "未忽略的版本");
        } else {
            UpgradeLogger.i(TAG, "已忽略的版本");
        }
    }

    @Test
    public void testPetUpgradeVersion() {
        UpgradeVersion upgradeVersion = new UpgradeVersion(1, false);
        repository.putUpgradeVersion(upgradeVersion);
    }

    @Test
    public void testGetUpgradeBuffer() {
        UpgradeBuffer upgradeBuffer = repository.getUpgradeBuffer("http://www.baidu.com");
        if (upgradeBuffer == null) {
            UpgradeLogger.i(TAG, "未缓存的版本");
        } else {
            UpgradeLogger.i(TAG, "已缓存的版本");
        }
    }

    @Test
    public void testPutUpgradeBuffer() {
        UpgradeBuffer upgradeBuffer = new UpgradeBuffer("http://www.baidu.com",
                null,
                null,
                1024L,
                1024L,
                new CopyOnWriteArrayList<UpgradeBuffer.BufferPart>(),
                System.currentTimeMillis());
        repository.putUpgradeBuffer(upgradeBuffer);
    }

}
