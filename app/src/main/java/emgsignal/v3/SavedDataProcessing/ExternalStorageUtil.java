package emgsignal.v3.SavedDataProcessing;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class ExternalStorageUtil {
    // Check whether the external storage is mounted or not.
    public static boolean isExternalStorageMounted() {

        String dirState = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(dirState))
        {
            return true;
        }else
        {
            return false;
        }
    }

    // Check whether the external storage is read only or not.
    public static boolean isExternalStorageReadOnly() {

        String dirState = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(dirState))
        {
            return true;
        }else
        {
            return false;
        }
    }


    // Get private external storage base directory.
    public static String getPrivateExternalStorageBaseDir(Context context, String dirType)
    {
        String ret = "";
        if(isExternalStorageMounted()) {
            File file = context.getExternalFilesDir(dirType);
            ret = file.getAbsolutePath();
        }
        return ret;
    }

    // Get private cache external storage base directory.
    public static String getPrivateCacheExternalStorageBaseDir(Context context)
    {
        String ret = "";
        if(isExternalStorageMounted()) {
            File file = context.getExternalCacheDir();
            ret = file.getAbsolutePath();
        }
        return ret;
    }


    // Get public external storage base directory.
    public static String getPublicExternalStorageBaseDir()
    {
        String ret = "";
        if(isExternalStorageMounted()) {
            File file = Environment.getExternalStorageDirectory();
            ret = file.getAbsolutePath();
        }
        return ret;
    }

    // Get public external storage base directory.
    public static String getPublicExternalStorageBaseDir(String dirType)
    {
        String ret = "";
        if(isExternalStorageMounted()) {
            File file = Environment.getExternalStoragePublicDirectory(dirType);
            ret = file.getAbsolutePath();
        }
        return ret;
    }

    // Get external storage disk space, return MB
    public static long getExternalStorageSpace() {
        long ret = 0;
        if (isExternalStorageMounted()) {
            StatFs fileState = new StatFs(getPublicExternalStorageBaseDir());

            // Get total block count.
            long count = fileState.getBlockCountLong();

            // Get each block size.
            long size = fileState.getBlockSizeLong();

            // Calculate total space size
            ret = count * size / 1024 / 1024;
        }
        return ret;
    }

    // Get external storage left free disk space, return MB
    public static long getExternalStorageLeftSpace() {
        long ret = 0;
        if (isExternalStorageMounted()) {
            StatFs fileState = new StatFs(getPublicExternalStorageBaseDir());

            // Get free block count.
            long count = fileState.getFreeBlocksLong();

            // Get each block size.
            long size = fileState.getBlockSizeLong();

            // Calculate free space size
            ret = count * size / 1024 / 1024;
        }
        return ret;
    }

    // Get external storage available disk space, return MB
    public static long getExternalStorageAvailableSpace() {
        long ret = 0;
        if (isExternalStorageMounted()) {
            StatFs fileState = new StatFs(getPublicExternalStorageBaseDir());

            // Get available block count.
            long count = fileState.getAvailableBlocksLong();

            // Get each block size.
            long size = fileState.getBlockSizeLong();

            // Calculate available space size
            ret = count * size / 1024 / 1024;
        }
        return ret;
    }
}
