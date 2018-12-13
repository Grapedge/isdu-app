package cn.edu.sdu.online.isdu.net.pack;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/15
 *
 ****************************************************
 */

public class ServerInfo {

    private static final String ipAddr = "202.194.15.132";
    private static final String ipAddr133 = "202.194.15.133";
//    public static final String ipAddr133 = "192.168.1.111";
    private static final int port = 8384;
    public static final String url = "http://" + ipAddr + ":" + port + "/";
    public static final String envVarUrl = "http://" + ipAddr + ":8380/env_variables.html";

    public static final String avatarUrl = "http://" + ipAddr + ":8380/isdu/avatar";

    public static final String calanderUrl = "http://202.194.15.133:8380/isdu/term/xl-2018-2019-1.png";

    public static String getComments() {
        return "http://" + ipAddr133 + ":8384/comment/get";
    }

    public static final String deleteComment = "http://" + ipAddr133 + ":8384/comment/delete";

    public static String getLikeNumber(int postId) {
        return "http://" + ipAddr133 + ":8384/post/getLike?postId=" + postId;
    }
    public static final String likePost = "http://" + ipAddr133 + ":8384/post/like/";
    public static String getIsLike(int postId, String uid) {
        return "http://" + ipAddr133 + ":8384/post/isLike?postId=" + postId + "&userId=" + uid;
    }
    public static final String collectPost = "http://" + ipAddr133 + ":8384/post/collect";
    public static String getIsCollect(int postId, String uid) {
        return "http://" + ipAddr133 + ":8384/post/isCollect?postId=" + postId + "&userId=" + uid;
    }

    /*等接口*/
    public static final String likeConfession = "http://" + ipAddr133 + ":8384/post/like/";


    public static final String collectConfession= "http://" + ipAddr133 + ":8384/post/collect";

    /*等接口*/

    public static String getUrlLogin(String num, String pwd) {
        return url + "/user/signIn?j_username=" + num + "&j_password=" + pwd;
    }
    public static String searchUser(String studentNumber){
        return url + "user/findBySN?studentNumber="+studentNumber;
    }
    public static String searchUserbyNickName(String NickName){
        return url + "user/findByNN?nickname="+NickName;
    }
    public static String getUserInfo(String id, String key) {
        return url + "/user/getInformation?id=" + id + "&key=" + key;
    }
    public static final String urlUpdate = url + "/user/update";

    public static String getNewsUrl(int index) {
        String site;
        switch (index) {
            case 0:
                site = "sduOnline";
                break;
            case 1:
                site = "underGraduate";
                break;
            case 2:
                site = "sduYouth";
                break;
            case 3: default:
                site = "sduView";
                break;
        }
        return "https://sduonline.cn/isdu/news/api/index.php?site=" + site;
    }

    public static final String uploadPostUrl = "http://" + ipAddr133 + ":8384/post/upload";

    public static String postCommentUrl = "http://" + ipAddr133 + ":8384/comment/add";

/*等接口*/
    public static String confessionCommentUrl = "http://" + ipAddr133 + ":8384/comment/add";

    /*这个要不起*/
    public static String lifeCommentUrl = "http://" + ipAddr133 + ":8384/comment/add";

    /**
     *
     * @param index 版块ID
     * @param id 所处位置的ID
     */
    public static String getNewsUrl(int index, int id) {
        String site;
        switch (index) {
            case 0:
                site = "sduOnline";
                break;
            case 1:
                site = "underGraduate";
                break;
            case 2:
                site = "sduYouth";
                break;
            case 3: default:
                site = "sduView";
                break;
        }
        return "https://sduonline.cn/isdu/news/api/index.php?site=" + site + "&content&id=" + id;
    }


    public static String getGradeUrl(String id){
        return url+"/academic/curTerm?id="+id;
    }

    public static String getPastGradeUrl(String id,String term) {
        return url + "/academic/termScore?id=" + id + "&termId=" + term;
    }


    public static String getExamUrl(int id) {
        return url + "/academic/schedule?id=" + id;
    }

    public static String getScheduleUrl(int id) {
        return url + "academic/table?id=" + id;

    }

    public static String getSchoolBusUrl(String from, String to, int isWeekend) {
        return "https://sduonline.cn/isdu/schoolbus/api/?start=" + from + "&end=" + to + "&isWeekend=" + isWeekend;
    }

    public static String getStudyRooms(String campus) {
        return "http://sduonline.cn/isdu/studyroom/api/?campus=" + campus;
    }

    public static String getLibrarySearUrl(String loc) {
        return "https://sduonline.cn/isdu/library/api/?room=" + loc;
    }

    public static String getStudyRooms(String campus, String building, String date) {
        return "http://sduonline.cn/isdu/studyroom/api/?campus=" + campus + "&building=" + building + "&date=" + date;
    }

    public static String getPostList(int uid, int startId) {
        return "http://" + ipAddr133 + ":8384/post/getPostList10?startId=" + startId + "&userId=" + uid;
    }


    public static String getPost(int id) {
        return "http://" + ipAddr133 + ":8384/post/detail?id=" + id;
    }

    /*这个接口要改啊啊啊啊  啊啊啊啊啊啊*/
    public static String getLifeList(int uid, int startId) {
        return "http://" + ipAddr133 + ":8384/post/getPostList10?startId=" + startId + "&userId=" + uid;
    }


    public static String getLife(int id) {
        return "http://" + ipAddr133 + ":8384/post/detail?id=" + id;
    }

    public static String getConfession(int id) {
        return "http://" + ipAddr133 + ":8384/post/detail?id=" + id;
    }
/*这两个注释之间的都要改*/





    public static final String deletePost = "http://" + ipAddr133 + ":8384/post/delete";


    /*这个接口*/
    public static final String deleteLife = "http://" + ipAddr133 + ":8384/post/delete";

    public static final String deleteConfession = "http://" + ipAddr133 + ":8384/post/delete";
    /*上面等接口*/

    public static String getLibraryInfoUrl(String id){
        return url + "library/info?id=" + id;
    }
    public static String getBindUrl(String uid,String cardNumber, String pwd) {
        return url + "library/bind?id=" + uid + "&cardNumber=" + cardNumber + "&password=" + pwd;
    }
    public static String getBookListUrl(String uid) {
        return url + "library/borrowed?id=" + uid ;
    }
    public static String searchBookUrl(String bookName){
        return url + "library/search?name=" + bookName ;
    }
    public static String renewBookUrl(int uid){
        return url + "library/renew?id=" + uid ;
    }
    public static String renewOneBookUrl(int uid, String bookId, String checkCode){
        return url + "library/renewOne?id=" + uid + "&bookId="+ bookId + "&verifyId=" + checkCode;
    }

    public static final String getCollectList = "http://" + ipAddr133 + ":8384/user/getCollect";

    public static String getLikeMe(String uid) {
        return "http://202.194.15.133:8384/user/getLikeMe?userId=" + uid;
    }

    public static String getMyLike(String uid) {
        return "http://202.194.15.133:8384/user/getLike?userId=" + uid;
    }

    public static String userLike(String userId, String beLikeUserId) {
        return "http://202.194.15.133:8384/user/like?userId=" + userId + "&beLikeUserId=" + beLikeUserId;
    }

    public static String queryPost(String key) {
        return "http://" + ipAddr133 + ":8384/post/query?key=" + key;
    }

    public static String getHotPostList(double value) {
        return "http://" + ipAddr133 + ":8384/post/getHotTen?startValue=" + value;
    }

    public static String getSchoolAboutList(int id) {
        return "http://" + ipAddr133 + ":8384/post/getSchoolAboutTen?startId=" + id;
    }

    public static String getNotice(String uid) {
        return "http://" + ipAddr133 + ":8384/notice/getNotice?userId=" + uid;
    }

    public static String getMyComment10(String uid, int startId) {
        return "http://" + ipAddr133 + ":8384/user/getComment10?userId=" + uid + "&startId=" + startId;
    }

    public static String getRecommend10(int startValue) {
        return "http://" + ipAddr133 + ":8384/post/getRecommendTen?startValue=" + startValue;
//        return "http://" + ipAddr133 + ":8384/post/getSyncPostTen?startId=" + startValue;
    }

    public static final String buildInfo = "http://" + ipAddr + ":8380/isdu/build/config.html";

    public static String getFeedbackUrl() {
        return "http://" + ipAddr133 + ":8384/addFeedBack";
    }

    public static String getLikePost(String uid, int startId) {
        return "http://" + ipAddr133 + ":8384//post/getLikePostTen?userId=" + uid + "&startId=" + startId;
    }

    public static String getPostIntroduction(int postId) {
        return "http://" + ipAddr133 + ":8384/post/getPostIntroduction?postId=" + postId;
    }

    public static String getUserVerification(String uid) {
        return url + "user/verification?id=" + uid;
    }

    public static String getSyncPostTen(int startId) {
        return "http://" + ipAddr133 + ":8384/post/getSyncPostTen?startId=" + startId;
    }

    public static String getTagedPostTen(int startId) {
        return "http://" + ipAddr133 + ":8384/post/getTagedPost?startId=" + startId;
    }

    public static String setPostTag(int postId, String tag, String userId) {
        return "http://" + ipAddr133 + ":8384/post/setTag?postId=" + postId + "&tag=" + tag + "&userId=" + userId;
    }
    /*jiekou */
    public static String setConfessionTag(int postId, String tag, String userId) {
        return "http://" + ipAddr133 + ":8384/post/setTag?postId=" + postId + "&tag=" + tag + "&userId=" + userId;
    }
}
