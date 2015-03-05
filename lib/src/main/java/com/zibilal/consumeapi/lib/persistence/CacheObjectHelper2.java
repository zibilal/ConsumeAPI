package com.zibilal.consumeapi.lib.persistence;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.zibilal.consumeapi.lib.network.Response;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;

/**
 * Created by bmuhamm on 5/7/14.
 */
public abstract class CacheObjectHelper2 {

    private CacheSqliteHelper mSqlHelper;
    private SQLiteDatabase mDatabase;

    public CacheObjectHelper2(CacheSqliteHelper helper){
        mSqlHelper=helper;
    }

    public void open(){
        mDatabase=mSqlHelper.getWritableDatabase();
    }

    public void close(){
        mSqlHelper.close();
    }

    public abstract List<CacheObject> getCache(String keyword);

    public abstract int addCache(String keyword, Response response);

    public abstract String getKeywordColumn();

    public HashMap<String, String> allColumns(Class<? extends CacheObject> clss) throws Exception {

       Field[] fields = clss.getDeclaredFields();

        HashMap<String, String> columns = new HashMap<String, String>();
        columns.put(getKeywordColumn(), "text" );

        for(Field field : fields) {
            ColumnCache columnCache = field.getAnnotation(ColumnCache.class);
            try {
                if (columnCache != null) {
                    String sname = columnCache.columName();
                    columns.put(sname, mapType(field));
                } else {
                    columns.put(field.getName(), mapType(field));
                }
            } catch (Exception e) {
                continue;
            }
        }

        return columns;
    }

    public SQLiteDatabase getDatabase(){
        return mDatabase;
    }

    public CacheSqliteHelper getHelper(){
        return mSqlHelper;
    }

    private String mapType(Field f) throws Exception {
        if(f.getType().equals(Integer.class) || f.getType().equals(Long.class)) {
            return "integer";
        } else if(f.getType().equals(Float.class) || f.getType().equals(Double.class)) {
            return "real";
        } else if(f.getType().equals(String.class)) {
            return "text";
        } else
            throw new Exception("Unsupported field format " + f.getType());
    }

    public  Field getFieldByName(String name, CacheObject object) {
        Class<? extends  CacheObject> clss = object.getClass();
        Field[] fields = clss.getDeclaredFields();
        for(Field f : fields) {
            String str = f.getName().toLowerCase();
            if(name.toLowerCase().equals(str))
                return f;
        }

        return null;
    }

    public  long saveObject(CacheObject object, String keyword) throws Exception{
        ContentValues values = new ContentValues();
        Class<? extends  CacheObject> clss = object.getClass();
        Method[] methods = clss.getDeclaredMethods();

        values.put(getKeywordColumn(), keyword);

        for(Method m : methods) {
            if(m.getName().startsWith("get")) {
                Object o = m.invoke(object, null);
                String fieldName = m.getName().substring("get".length(), m.getName().length());
                Field f = getFieldByName(fieldName, object);
                if(f != null ) {
                    ColumnCache columnCache = f.getAnnotation(ColumnCache.class);
                    String key;
                    if (columnCache != null) {
                        key = columnCache.columName();
                    } else {
                        key = f.getName();
                    }

                    if (o instanceof Integer) {
                        values.put(key, (Integer) o);
                    } else if (o instanceof String) {
                        values.put(key, (String) o);
                    } else if (o instanceof Long) {
                        values.put(key, (Long) o);
                    }
                }
            }
        }
//
//        String tableName = mSqlHelper.getTableName();
//        long insertid = mDatabase.insert(mSqlHelper.getTableName(), null, values);
        return 0;
    }

    private  void setValue(String columnName, CacheObject cObj, Object value) throws Exception {
        Class<? extends CacheObject> clss = cObj.getClass();
        Field[] fields=clss.getDeclaredFields();

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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public  CacheObject cursorToCacheObject(Cursor cursor, Class<? extends CacheObject> clss) throws Exception {
        int collen = cursor.getColumnCount();

        CacheObject cObj = clss.newInstance();

        for(int i=0; i < collen; i++) {
            String colName = cursor.getColumnName(i);
            int type = cursor.getType(i);
            Object obj;
            switch (type){
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

        return cObj;
    }
}
