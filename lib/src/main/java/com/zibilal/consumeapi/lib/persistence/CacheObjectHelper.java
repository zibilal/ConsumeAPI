package com.zibilal.consumeapi.lib.persistence;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
//import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bmuhamm on 5/30/14.
 */
public class CacheObjectHelper {

    private static final String TAG="CacheObjectHelper";

    private SQLiteDatabase mDatabase;
    private CacheSqliteHelper mSqlHelper;

    public CacheObjectHelper(CacheSqliteHelper sqliteHelper) {
        mSqlHelper = sqliteHelper;
    }

    public CacheObjectHelper(){}

    public void setSqlHelper(CacheSqliteHelper sqlHelper) {
        mSqlHelper = sqlHelper;
    }

    public void open() {
        mDatabase = mSqlHelper.getWritableDatabase();
    }

    public void close() {
        mSqlHelper.close();
    }

    public CacheSqliteHelper getSqlHelper() {
        return mSqlHelper;
    }

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    public int deleteAll(String className) {
        return mDatabase.delete(className, "persisted=0", null);
    }
    public int delete(String className, String key, String val) {
        return mDatabase.delete(className, key + "='" + val + "'", null);
    }

    public int delete(String className, String whereClause) {
        return mDatabase.delete(className, whereClause, null);
    }

    public int delete(CacheObject object) {
        Class<? extends  CacheObject> cls = object.getClass();
        Field[] fields = cls.getDeclaredFields();
        String key="";
        Object obj=null;
        HashMap<String, Object> vals = new HashMap<String, Object>();
        try {
            for(Field f: fields) {
                f.setAccessible(true);
                ColumnCache columnCache = f.getAnnotation(ColumnCache.class);
                if(columnCache != null) {
                    boolean isPrimary = columnCache.isPrimaryKey();
                    boolean isKeyword = columnCache.isKeyword();
                    if(isKeyword || isPrimary){
                        key = columnCache.columName();
                        obj = f.get(object);
                        //Log.d(TAG, "The obj = " + obj);
                        vals.put(key, obj);
                    }
                }
            }
        } catch (Exception e) {
            //Log.e(TAG, "Illegal Access Exception is occured");
        }

        /*if(key != null && obj != null) {
            if(obj instanceof String) {
                String s = (String) obj;
                return mDatabase.delete(cls.getSimpleName(), key + "= '" + s + "'", null);
            } else  {
                String s = obj.toString();
                return mDatabase.delete(cls.getSimpleName(), key + "=" + s, null);
            }
        } else
            return 0;*/

        if(vals.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            for(Map.Entry<String, Object> e : vals.entrySet()) {
                Object theobj = e.getValue();
                if(buffer.length() > 0) {
                    buffer.append(" AND ");
                }
                if(theobj instanceof String) {
                    String temp = (String) theobj;
                    buffer.append(e.getKey() + "= '" + temp + "'");
                } else {
                    String temp = obj.toString();
                    buffer.append(e.getKey() + "=" + temp);
                }
            }
            //Log.d(TAG, "the where clause ==> " + buffer.toString());
            return mDatabase.delete(cls.getSimpleName(), buffer.toString(), null);
        } else
            return 0;
    }

    public long saveObject(CacheObject object) {

        if(! mDatabase.isOpen()) {
            open();
        }

        ContentValues values = new ContentValues();
        Class<? extends  CacheObject> cls = object.getClass();
        Field[] fields = cls.getDeclaredFields();

        for(Field f : fields) {
            f.setAccessible(true);
            ColumnCache columnCache = f.getAnnotation(ColumnCache.class);
            String key;
            if(columnCache != null) {
                boolean isPrimary=columnCache.isPrimaryKey();
                boolean autoIncrement=columnCache.autoincrement();
                boolean isNotSave = columnCache.isNotSave();
                if( (isPrimary && autoIncrement) || isNotSave)
                    continue;
                else
                    key = columnCache.columName();
            } else {
                key = f.getName();
            }

            try {
                f.setAccessible(true);
                Object o = f.get(object);

                if(o instanceof  Integer ) {
                    values.put(key, (Integer) o);
                } else if(o instanceof String) {
                    values.put(key, (String) o);
                } else if(o instanceof Long) {
                    values.put(key, (Long) o);
                }
            } catch (IllegalAccessException e) {
                //Log.d(TAG, e.getMessage());
                continue;
            }
        }

        long insertid = mDatabase.insert(cls.getSimpleName(), null, values);
        return insertid;
    }

    private void setValue(String columnName, CacheObject cObj, Object value) throws Exception {
        Class<? extends  CacheObject> clss = cObj.getClass();
        Field[] fields = clss.getDeclaredFields();
        Field selectedField = null;

        for(Field field: fields) {
            ColumnCache columnCache = field.getAnnotation(ColumnCache.class);
            String name;
            if(columnCache!=null) {
                name = columnCache.columName();
            } else {
                name = field.getName();
            }
            if(name != null && name.equals(columnName)) {
                selectedField = field;
                selectedField.setAccessible(true);
                break;
            }
        }

        if(selectedField != null) {
            selectedField.set(cObj, value);
        }
    }

    public static final int TYPE_TEXT=0;
    public static final int TYPE_NUMBER=1;

    public List<CacheObject> getAllDataBy(Class<? extends CacheObject> cls, String whereClause, String orderByClause, boolean isDescending) {
        List<CacheObject> objs = null;
        String whereQuery=whereClause;
        String selectQuery = "select * from " + cls.getSimpleName();
        selectQuery += " " + whereQuery + (orderByClause!=null && orderByClause.length() > 0? " " + orderByClause:"") +
                (isDescending?" DESC":"");
        //Log.d(TAG, "Database = " + mDatabase + " --->> the query = " + selectQuery);
        Cursor cursor = mDatabase.rawQuery(selectQuery, null);
        if(cursor != null && cursor.moveToFirst()) {
            try {
                do{
                    if(objs == null) objs = new ArrayList<CacheObject>();
                    CacheObject obj = cursorToCacheObject(cursor, cls);
                    objs.add(obj);
                } while(cursor.moveToNext());
            } catch (Exception e) {
            }
        }

        return objs;
    }

    public int getCountQuery(Class<? extends CacheObject> cls, String whereClause) {
        String selectQuery = "select count(*) from " + cls.getSimpleName() + " where " + whereClause;
        Cursor cursor = mDatabase.rawQuery(selectQuery, null);
        if(cursor != null && cursor.moveToFirst()) {
            return cursor.getInt(0);
        } else {
            return 0;
        }
    }

    public List<CacheObject> getAllDataBy(Class<? extends CacheObject> cls,String key, String value, int type) {
        List<CacheObject> objs = null;
        String whereQuery="";
        String selectQuery = "select * from " + cls.getSimpleName();
        if(key != null && value!=null) {
            String v = type==TYPE_TEXT? "'" + value + "'" : value;
            whereQuery="where " + key + "=" + v + "";
        }
        selectQuery += " " + whereQuery;
        //Log.d(TAG, "Database = " + mDatabase + " --->> the query = " + selectQuery);
        Cursor cursor = mDatabase.rawQuery(selectQuery, null);
        if(cursor != null && cursor.moveToFirst()) {
            try {
                do{
                   if(objs == null) objs = new ArrayList<CacheObject>();
                   CacheObject obj = cursorToCacheObject(cursor, cls);
                    objs.add(obj);
                } while(cursor.moveToNext());
            } catch (Exception e) {
            }
        }

        return objs;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public  CacheObject cursorToCacheObject(Cursor cursor, Class<? extends CacheObject> clss) throws Exception {
        int collen = cursor.getColumnCount();

        CacheObject cObj = clss.newInstance();

        for(int i=0; i < collen; i++) {
            String colName = cursor.getColumnName(i);
            int type = cursor.getType(i);
            //Log.d(TAG, "Cursor type = " + type + " , Column name = " + colName);
            if(type != Cursor.FIELD_TYPE_NULL) {
                Object obj;
                switch (type) {
                    case Cursor.FIELD_TYPE_INTEGER:
                        obj = cursor.getInt(i);
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        obj = cursor.getFloat(i);
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        obj = cursor.getString(i);
                        break;
                    default:
                        throw new Exception("Wrong cursor type...");
                }

                setValue(colName, cObj, obj);
            }
        }

        return cObj;
    }

}
