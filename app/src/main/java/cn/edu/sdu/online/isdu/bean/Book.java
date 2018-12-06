package cn.edu.sdu.online.isdu.bean;

import java.util.List;

/**
 ****************************************************
 * @author Cola_Mentos
 * Last Modifier: Cola_Mentos
 * Last Modify Time: 2018/6/23
 *
 * 图书馆书籍信息
 ****************************************************
 */

public class Book {

    private String bookName; // 书名
    private String idNumber; // 索书号
    private String bookPlace; // 借阅地点
    private String borrowDate; // 借阅日期
    private String backDate; // 应还日期
    private int remainDays; // 剩余天数
    private int borrowTimes; // 续借次数
    private String author;//作者
    private String press;//出版社
    private int all;//总量
    private int canBor;//可借量
    private List<String> borPlace;//可借地点
    private String id;//图书条码号
    private String checkCode;//续借验证码

    public Book(){}

    public void setBookName(String bookName) {
        this.bookName=bookName;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void setBackDate(String backDate) {
        this.backDate = backDate;
    }

    public void setBookPlace(String bookPlace) {
        this.bookPlace = bookPlace;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setBorrowTimes(int borrowTimes) {
        this.borrowTimes = borrowTimes;
    }

    public void setRemainDays(int remainDays) {
        this.remainDays = remainDays;
    }

    public String getBookName() {
        return bookName;
    }

    public String getBookPlace() {
        return bookPlace;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public int getBorrowTimes() {
        return borrowTimes;
    }

    public int getRemainDays() {
        return remainDays;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public String getBackDate() {
        return backDate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPress() {
        return press;
    }

    public void setPress(String press) {
        this.press = press;
    }

    public int getAll() {
        return all;
    }

    public void setAll(int all) {
        this.all = all;
    }

    public int getCanBor() {
        return canBor;
    }

    public void setCanBor(int canBor) {
        this.canBor = canBor;
    }

    public String getBorPlace() {
        String r="";
        for(int i=0 ; i<borPlace.size() ; i++){
            r=r+borPlace.get(i)+"\n";
        }
        return r;
    }

    public void setBorPlace(List<String> borPlace) {
        this.borPlace = borPlace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }
}
