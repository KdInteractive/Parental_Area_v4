package com.cide.interactive.parentalArea.Providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;
import com.cide.interactive.parentalArea.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Locale;

import static com.cide.interactive.kuriolib.FileUtils.copyFromRawToObb;
import static com.cide.interactive.kuriolib.FileUtils.deleteFile;
import static com.cide.interactive.kuriolib.FileUtils.deleteFileFromObb;

public class CronLabProvider extends ContentProvider {
    private static final HashMap<String, String> MIME_TYPES = new HashMap<>();

    private static final String CIDE_SERIAL_FILE = "serial.txt";
    private static final String ONLINE_PAGE_REDIRECT = "conf_url_blocked_page.txt";
    private static final String OFFLINE_PAGE_REDIRECT = "conf_url_no_conection.txt";
    private static final String WORDS_FORBIDDEN_URL = "private_file_words_list.txt";
    private static final String DISABLED_INTERNET_ACCESS = "private_disabled_internet.txt";
    private static final String WHITE_LIST_PACKAGE = "private_file_w_list_package.txt";
    private static final String INVALID_SERIAL = "invalid_serial.txt";
    private static final String WHITE_LIST_ONLY_MODE = "private_white_list_only_mode.txt";
    private static final String TOP_URL_CATEGORIES_FILE = "private_file_top_url.txt";
    private static final String CONTENT_CATEGORIES_FILE = "private_file_content.txt";
    private static final String BLACK_LIST_CRON_LAB = "private_file_b_list.txt";
    private static final String WHITE_LIST_CRON_LAB = "private_file_w_list.txt";
    private static final String APP_WHITE_LIST_CRON_LAB = "private_file_app_w_list.txt";

    static {
        MIME_TYPES.put(".txt", "text/plain");
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection,
                        String[] selectionArgs, String sort) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public String getType(Uri uri) {
        String path = uri.toString();

        for (String extension : MIME_TYPES.keySet()) {
            if (path.endsWith(extension)) {
                return (MIME_TYPES.get(extension));
            }
        }
        return null;
    }

    @Override
    public boolean onCreate() {

        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {

        Context context = getContext();
        int userId = context.getUserId();

        if (userId == 0) {
            return null;
        }

        String fileName = uri.getEncodedPath();

        if (fileName.contains(ONLINE_PAGE_REDIRECT)) {
            String file = copyFromRawToObb(context, R.raw.conf_url_blocked_page);
            return ParcelFileDescriptor.open(new File(file), ParcelFileDescriptor.MODE_READ_ONLY);
        } else if (fileName.contains(OFFLINE_PAGE_REDIRECT)) {
            String file = copyFromRawToObb(context, R.raw.offline_page_us);
            return ParcelFileDescriptor.open(new File(file), ParcelFileDescriptor.MODE_READ_ONLY);
        } else if (fileName.contains(WORDS_FORBIDDEN_URL)) {
            String file = copyFromRawToObb(context, R.raw.blocked_words);
            return ParcelFileDescriptor.open(new File(file), ParcelFileDescriptor.MODE_READ_ONLY);
        } else if (fileName.contains(DISABLED_INTERNET_ACCESS)) {
            // no file if web is on, if off create the file
            ChildInfoCore childInfo = ChildInfoCore.getCurrentChildInfo(context, false);
            if(!childInfo.isWebAccessOn()) {
                String file = copyFromRawToObb(getContext(), R.raw.blocked_page_simple);
                return ParcelFileDescriptor.open(new File(file), ParcelFileDescriptor.MODE_READ_ONLY);
            } else {
                deleteFileFromObb(context, R.raw.conf_url_blocked_page);
            }
        } else if (fileName.contains(INVALID_SERIAL)) {
            String file = copyFromRawToObb(context, R.raw.invalid_serial_us);
            return ParcelFileDescriptor.open(new File(file), ParcelFileDescriptor.MODE_READ_ONLY);
        } else if (fileName.contains(CIDE_SERIAL_FILE)) {
            String file = createSerialFile(context);
            return ParcelFileDescriptor.open(new File(file), ParcelFileDescriptor.MODE_READ_ONLY);
            // return ParcelFileDescriptor.open(new File(GlobalSetting.SERIAL_PATH), ParcelFileDescriptor.MODE_READ_ONLY);
        } else if (fileName.contains(WHITE_LIST_ONLY_MODE)) {
            if (ChildInfoCore.isChild(context, userId)) {
                ChildInfoCore childInfo = ChildInfoCore.getCurrentChildInfo(context, false);
                if(!childInfo.isWebAccessOn()) {
                    String file = copyFromRawToObb(context, R.raw.conf_url_blocked_page);
                    return ParcelFileDescriptor.open(
                            new File(file), ParcelFileDescriptor.MODE_READ_ONLY);
                }
            }
        } else {
            //filters / Blacklist and white list from DB
            ChildInfoCore childInfo = ChildInfoCore.getCurrentChildInfo(context, false);
            String locale = Locale.getDefault().getISO3Language();
            String filePath = context.getObbDir().getAbsolutePath() + File.separator + locale + '-';

            if (fileName.contains(TOP_URL_CATEGORIES_FILE)) {
                filePath = filePath + TOP_URL_CATEGORIES_FILE;
                childInfo.saveFilterForCronLab(context, filePath);
                return ParcelFileDescriptor.open(
                        new File(filePath), ParcelFileDescriptor.MODE_READ_ONLY);

            } else if (fileName.contains(CONTENT_CATEGORIES_FILE)) {
                filePath = filePath + CONTENT_CATEGORIES_FILE;
                childInfo.saveFilterForCronLab(context, filePath);
                return ParcelFileDescriptor.open(
                        new File(filePath), ParcelFileDescriptor.MODE_READ_ONLY);

            } else if (fileName.contains(BLACK_LIST_CRON_LAB)) {
                filePath = filePath + BLACK_LIST_CRON_LAB;
                childInfo.saveBlackListForCronLab(filePath);
                return ParcelFileDescriptor.open(
                        new File(filePath), ParcelFileDescriptor.MODE_READ_ONLY);

            } else if (fileName.contains(WHITE_LIST_CRON_LAB)) {
                filePath = filePath + WHITE_LIST_CRON_LAB;
                childInfo.saveWhiteListForCronLab(filePath);
                return ParcelFileDescriptor.open(
                        new File(filePath), ParcelFileDescriptor.MODE_READ_ONLY);
            } else if (fileName.contains(WHITE_LIST_PACKAGE)) {
                filePath = filePath + APP_WHITE_LIST_CRON_LAB;
                childInfo.saveAppWhiteListForCronLab(filePath);
                return ParcelFileDescriptor.open(
                        new File(filePath), ParcelFileDescriptor.MODE_READ_ONLY);
            }
        }
        throw new FileNotFoundException(uri.getPath());
    }

    public static String createSerialFile(Context context) {

        String locale = Locale.getDefault().getISO3Language();
        String newName = context.getObbDir().getAbsolutePath() + File.separator + locale + '-' + "sn.txt";
        File newFile = new File(newName);
        try {
            newFile.createNewFile();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(newName));
            outputStreamWriter.write(Utils.getSerialId());
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("createSerialFile", e.getMessage());
        }

        return newName;
    }
}
