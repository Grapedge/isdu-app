package cn.edu.sdu.online.isdu.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/15
 *
 * 安全加密工具
 ****************************************************
 */

public class Security {

    public static String encodeByMD5(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] res = digest.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < res.length; i++) {
                int val = res[i] & 0xff;
                if (val < 16)
                    sb.append("0" + Integer.toHexString(val));
                else
                    sb.append(Integer.toHexString(val));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Logger.log(e);
        }
        return "";
    }

}
