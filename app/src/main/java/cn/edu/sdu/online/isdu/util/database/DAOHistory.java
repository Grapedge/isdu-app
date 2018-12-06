package cn.edu.sdu.online.isdu.util.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import cn.edu.sdu.online.isdu.bean.History;

//浏览历史数据库工具类
public class DAOHistory {
    private DBOHistory dbo_history;
    private SQLiteDatabase database;
    public DAOHistory(Context context){
        dbo_history=new DBOHistory(context);
    }
    public void close(){
        dbo_history.close();
        database.close();
    }
    //在浏览体帖子时调用以纪录浏览历史
    public void newHistory(History history){
        ContentValues values=new ContentValues();
        database=dbo_history.getWritableDatabase();
        values.put("title",history.getTitle());
        values.put("subject",history.getSubject());
        values.put("time",history.getTime());
        values.put("url",history.getUrl());
        database.insert("tb_history",null,values);
        values.clear();
    }

    public ArrayList<History> getHistory(){
        ArrayList<History> historyList=new ArrayList<>();
        database=dbo_history.getReadableDatabase();
        Cursor c=database.rawQuery("SELECT * FROM tb_history",null);
        if(c.moveToLast()){
            do{
                historyList.add(new History(
                        c.getString(c.getColumnIndex("title")),
                        c.getString(c.getColumnIndex("subject")),
                        c.getLong(c.getColumnIndex("time")),
                        c.getString(c.getColumnIndex("url"))));

            }while (c.moveToPrevious());
        }
        c.close();
        return historyList;
    }

    public void clear(){
        database=dbo_history.getWritableDatabase();
        database.delete("tb_history",null,null);
    }
}
