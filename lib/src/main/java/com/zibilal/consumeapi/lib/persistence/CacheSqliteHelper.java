package com.zibilal.consumeapi.lib.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by bmuhamm on 5/7/14.
 */
public class CacheSqliteHelper extends SQLiteOpenHelper {

    private static final String TAG="CacheSqliteHelper";

    private List<Class<? extends  CacheObject>> classes;

    public List<String> getCreateQueries() throws Exception{
        if(classes == null)
            throw new Exception("Accepted classes have not been initialized");
        List<String> sqls = new ArrayList<String>();
        if(classes != null) {
            for(Class<? extends CacheObject> c : classes) {
                String str = createSqlCreate(c);
                //Log.d(TAG, "Create QUERY = " + str);
                sqls.add(str);
            }
            return sqls;
        }

        return null;
    }
    public List<String> getDropQueries() throws Exception {
        if(classes == null)
            throw new Exception("Accepted classes have not been initialized");
        List<String> sqls = new ArrayList<String>();
        if(classes != null) {
            for(Class<? extends CacheObject> c : classes) {
                String str = createSqlDrop(c);
                sqls.add(str);
            }
            return sqls;
        }
        return null;
    }

    public void setTables(List<Class<? extends CacheObject>> classes) {
        this.classes=classes;
    }

    public CacheSqliteHelper(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName,null, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            List<String> createQueries = getCreateQueries();
            for(String str : createQueries) {
                sqLiteDatabase.execSQL(str);
            }
        } catch (Exception e) {
            //Log.e(TAG, "Exception occured = " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        try {
            List<String> dropQueries = getDropQueries();
            for(String str : dropQueries) {
                sqLiteDatabase.execSQL(str);
            }
            onCreate(sqLiteDatabase);
        } catch (Exception e) {
            //Log.e(TAG, "Exception occured = " + e.getMessage());
        }
    }

    public String createSqlCreate(Class<? extends CacheObject> cls) {

        String baseQuery = "create table " + cls.getSimpleName() + "( [columns] )";

        StringBuilder columns = new StringBuilder();

        Field[] fields = cls.getDeclaredFields();

        for(Field f : fields) {
            f.setAccessible(true);
            ColumnCache columnCache = f.getAnnotation(ColumnCache.class);
            try{
                if(columnCache!=null){
                    if(!columnCache.isNotSave()) {
                        String sname = columnCache.columName() + " " + mapType(f) + " ";
                        if (columnCache.isPrimaryKey()) {
                            sname = sname + " primary key ";
                        }
                        if (columnCache.autoincrement()) {
                            sname = sname + " autoincrement ";
                        }
                        if (columnCache.isNotNull()) {
                            sname = sname + " not null ";
                        }

                        sname = sname + ",";
                        columns.append(sname);
                    } else {
                        continue;
                    }
                } else {
                    String temp = f.getName() + " " + mapType(f) + " ,";
                    columns.append(temp);
                }
            } catch (Exception e) {
                //Log.d(TAG, e.getMessage());
                continue;
            }
        }

        if(columns.length() > 1) {
            String result = columns.substring(0, columns.length() - 1);
            baseQuery = baseQuery.replace("[columns]", result);
            //Log.d(TAG, "Create query --->> " + baseQuery);
            return baseQuery;
        } else {
            return null;
        }
    }

    public String createSqlDrop(Class<? extends CacheObject> cls) {
        return "drop table if exists " + cls.getSimpleName();
    }

    public HashMap<String, String> allColumnsOfTable(Class<? extends CacheObject> cls) {
        Field[] fields = cls.getDeclaredFields();
        HashMap<String, String> columns = new HashMap<String, String>();

        for(Field field : fields) {
            field.setAccessible(true);
            ColumnCache columnCache = field.getAnnotation(ColumnCache.class);
            try{
                if(columnCache != null) {
                    String sname = columnCache.columName();
                    columns.put(sname, mapType(field));
                } else {
                    columns.put(field.getName(), mapType(field));
                }
            } catch (Exception e) {
                //Log.d(TAG, e.getMessage());
                continue;
            }
        }

        return columns;
    }

    private String mapType(Field f) throws Exception {
        if(f.getType().equals(Integer.class) || f.getType().equals(Long.class) || f.getType().equals(int.class) || f.getType().equals(long.class)) {
            return "integer";
        } else if(f.getType().equals(Float.class) || f.getType().equals(Double.class)) {
            return "real";
        } else if(f.getType().equals(String.class)) {
            return "text";
        } else
            throw new Exception("Unsupported field format " + f.getType());
    }
}
