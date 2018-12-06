package cn.edu.sdu.online.isdu.bean;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.util.Logger;

/**
 * @author Cola_Mentos
 * @date 2018/7/11
 */

public class Grade {

    /**
     * kcm:课程名称
     * pscj:平时成绩
     * qmcj:期末成绩
     * cj:成绩
     * jd:绩点
     * zgf:班级最高分
     * zdf:班级最低分
     * pm:排名
     * zrs:总人数
     * dd:等第
     * xf:学分
     */

    private double  jd, xf;
    private int pm, zrs, kxh;
    private String dd,kcm,cj, zgf, zdf, pscj, qmcj;
    public static double zjd=0,zxf=0,zfz=0;
    public static double[] z_jd = new double[10];
    public static double[] z_xf = new double[10];
    public static double[] z_fz = new double[10];



    public Grade(){

    }

    public void setPscj(String pscj) {
        this.pscj = pscj;
    }

    public void setKxh(int kxh) {
        this.kxh = kxh;
    }

    public void setQmcj(String qmcj) {
        this.qmcj = qmcj;
    }

    public void setCj(String cj) {
        this.cj = cj;
    }

    public void setJd(double jd) {
        this.jd = jd;
    }

    public void setZgf(String zgf) {
        this.zgf = zgf;
    }

    public void setZdf(String zdf) {
        this.zdf = zdf;
    }

    public void setPm(int pm) {
        this.pm = pm;
    }

    public void setZrs(int zrs) {
        this.zrs = zrs;
    }

    public void setDd(String dd) {
        this.dd = dd;
    }

    public void setKcm(String kcm) { this.kcm = kcm; }

    public double getXf() {
        return xf;
    }

    public String getPscj() {
        return pscj;
    }

    public String getQmcj() {
        return qmcj;
    }

    public String getCj() {
        return cj;
    }

    public double getJd() {
        return jd;
    }

    public String getZdf() {
        return zdf;
    }

    public String getZgf() {
        return zgf;
    }

    public int getPm() {
        return pm;
    }

    public int getZrs() {
        return zrs;
    }

    public String getDd() {
        return dd;
    }

    public String getKcm() { return kcm; }

    public int getKxh() {
        return kxh;
    }

    public static boolean judgeEva(String json){
        try{
            JSONObject jsonObject = new JSONObject(json);
            if (!jsonObject.isNull("status")){
                if (jsonObject.getString("status").equals("failed")){
                    return false;
                }
                else {
                    return true;
                }
            }
            else {
                return false;
            }
        }catch (Exception e){
            Logger.log(e);
        }
        return false;
    }

    public static List<Grade> loadFromString(String json){
        List<Grade> list = new ArrayList<>();
        Grade grade;
        try{
            JSONObject jsonObject = new JSONObject(json);
            zfz=0;
            zxf=0;
            zjd=0;
            if (jsonObject.getJSONArray("obj") != null
                    && jsonObject.getJSONArray("obj").length() > 0){
                JSONArray jsonArray = jsonObject.getJSONArray("obj");
                for (int i = 0;i < jsonArray.length();++i){
                    grade = new Grade();
                    JSONObject obj = jsonArray.getJSONObject(i);
                    grade.kcm = obj.getString("kcm");
                    grade.cj = obj.getString("cj");
                    if (grade.cj.charAt(0) == '-') {
                        grade.cj = grade.cj.substring(1);
                    }
                    grade.zgf = obj.getString("zgf");
                    grade.jd = obj.getDouble("wfzjd");
                    grade.dd = obj.getString("wfzdj");
                    grade.zdf = obj.getString("zdf");
                    grade.pm = obj.getInt("pm");
                    grade.zrs = obj.getInt("xkrs");
                    grade.xf = obj.getDouble("xf");
                    grade.pscj = !obj.isNull("psjc") ? obj.getString("psjc") : "";
                    grade.qmcj = !obj.isNull("qmcj") ? obj.getString("qmcj") : "";
                    grade.kxh = obj.getInt("kxh");
                    if (grade.kxh < 600) {
                        zfz += grade.xf*grade.jd;
                        zxf += grade.xf;
                    }
                    zjd = zfz/zxf;
                    list.add(grade);
                }
            }

        }catch (Exception e){
            Logger.log(e);
        }
        return list;
    }

    public static List<Grade> pastGradeLoadFromString(String json,int term){
        List<Grade> list = new ArrayList<>();
        Grade grade;
        try{
            JSONObject jsonObject = new JSONObject(json);
            z_fz[term] = 0;
            z_xf[term] = 0;
            z_jd[term] = 0;
            if (jsonObject.getJSONArray("obj") != null
                    && jsonObject.getJSONArray("obj").length() > 0){
                JSONArray jsonArray = jsonObject.getJSONArray("obj");
                for (int i = 0;i < jsonArray.length();++i){
                    grade = new Grade();
                    JSONObject obj = jsonArray.getJSONObject(i);
                    grade.kcm = obj.getString("kcm");
                    grade.cj = obj.getString("kscj");
                    if (grade.cj.charAt(0) == '-') {
                        grade.cj = grade.cj.substring(1);
                    }
                    grade.zgf = "";
                    grade.jd = obj.getDouble("wfzjd");
                    grade.dd = obj.getString("wfzdj");
                    grade.zdf = "";
                    grade.pm = 0;
                    grade.zrs = 0;
                    grade.xf = obj.getDouble("xf");
                    grade.pscj = !obj.isNull("pscj") ? obj.getString("pscj") : "";
                    grade.qmcj = !obj.isNull("qmcj") ? obj.getString("qmcj") : "";
                    grade.kxh = obj.getInt("kxh");

                    if (grade.kxh < 600) {
                        z_fz[term] += grade.xf*grade.jd;
                        z_xf[term] += grade.xf;
                    }
                    z_jd[term] = z_xf[term] == 0 ? 0 : z_fz[term]/z_xf[term];
                    list.add(grade);
                }
            }

        }catch (Exception e){
            Logger.log(e);
        }
        return list;
    }
}
