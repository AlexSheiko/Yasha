package com.yasha.yasha.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class SearchSuggestionProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String query = uri.getLastPathSegment();
        if (SearchManager.SUGGEST_URI_PATH_QUERY.equals(query)) {
            // user hasn’t entered anything
            // thus return a default cursor
            return null;
        } else {
            // query contains the users search
            // return a cursor with appropriate data
            return findUsers(query);
        }
    }

    private Cursor findUsers(String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereMatches("username", username);

        List<ParseUser> matches = null;
        try {
            matches = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (matches != null) {
            return createCursor(matches);
        }
        return null;
    }

    private Cursor createCursor(List<ParseUser> users) {
        String[] columns = new String[]{
                "_id",
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                SearchManager.SUGGEST_COLUMN_TEXT_1};

        MatrixCursor matrixCursor = new MatrixCursor(columns);

        for (int i = 0; i < users.size(); i++) {
            ParseUser user = users.get(i);
            matrixCursor.addRow(new Object[]{
                    i,
                    user.getObjectId(),
                    user.getUsername(),
            });
        }

        MatrixCursor c2 = matrixCursor;

        return matrixCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
