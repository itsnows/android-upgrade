package com.upgradelibrary.data.source;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/3/5 16:37
 * <p>
 * HistoricalPersistenceContract
 */

public class HistoricalPersistenceContract {
    public static final String FILE_NAME = "upgrade";

    public static abstract class IgnoreVersionEntity {
        public static final String KEY_NAME = "upgrade_version";
        public static final String KEY_NAME_VERSION_CODE = "version_code";
        public static final String KEY_NAME_IGNORE_STATUS = "ignore_status";
    }

    public static abstract class UpgradeBufferEntity {
        public static final String KEY_NAME = "upgrade_buffer";
        public static final String KEY_NAME_DOWNLOAD_URL = "download_url";
        public static final String KEY_NAME_FILE_MD5 = "file_md5";
        public static final String KEY_NAME_FILE_LENGTH = "file_length";
        public static final String KEY_NAME_BUFFER_LENGTH = "buffer_length";
        public static final String KEY_NAME_SHUNT_PART = "shunt_part";
        public static final String KEY_NAME_SHUNT_PART_START_LENGTH = "shunt_part_start_length";
        public static final String KEY_NAME_SHUNT_PART_END_LENGTH = "shunt_part_end_length";
        public static final String KEY_NAME_LAST_MODIFIED = "last_modified";
    }

}
