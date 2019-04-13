package com.example.lenovo.myapplication;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class GetPathFromUri {
    public static String getPath(Context context, Uri uri)
    {
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion < 19)
        {
            return getPathBelow19(context, uri);
        }
        return getPathAbove19(context, uri);
    }

    private static String getPathBelow19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }
    private static String getPathAbove19(Context context, Uri uri) {
        String filePath = null;
        if(DocumentsContract.isDocumentUri(context, uri)) {
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) {
                String id = documentId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
                filePath = getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                filePath = uri.getPath();
            }
            return filePath;
    }
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectArgs) {
        String path = null;
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(uri, projection, selection, selectArgs, null);
            if(cursor != null && cursor.moveToFirst())
            {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        }
        catch(Exception e)
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
        return path;
    }
}
