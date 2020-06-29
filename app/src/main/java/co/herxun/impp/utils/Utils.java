package co.herxun.impp.utils;

import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;

public class Utils {

    public static int px2Dp(Context ct, int px) {
        return (int) (px * ct.getResources().getDisplayMetrics().density);
    }

    public AttributeSet getEdittextAttr(Context ct) {
        try {
            int res = ct.getResources().getIdentifier("my_edittext", "layout", ct.getPackageName());
            XmlPullParser parser = ct.getResources().getXml(res);
            int state = 0;
            do {
                try {
                    state = parser.next();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                if (state == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("EditText")) {
                        return Xml.asAttributeSet(parser);
                    }
                }
            } while (state != XmlPullParser.END_DOCUMENT);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 顶级域名判断；如果要忽略大小写，可以直接在传入参数的时候toLowerCase()再做判断
     * 
     * @param str
     * @return
     */
    public static boolean isTopURL(String str) {
        // // 转换为小写
         str = str.toLowerCase();
        // String domainRules =
        // "com.cn|net.cn|org.cn|gov.cn|com.hk|公司|中国|网络|com|net|org|int|edu|gov|mil|arpa|Asia|biz|info|name|pro|coop|aero|museum|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cf|cg|ch|ci|ck|cl|cm|cn|co|cq|cr|cu|cv|cx|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|eh|es|et|ev|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gh|gi|gl|gm|gn|gp|gr|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|in|io|iq|ir|is|it|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|ml|mm|mn|mo|mp|mq|mr|ms|mt|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nt|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|pt|pw|py|qa|re|ro|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|st|su|sy|sz|tc|td|tf|tg|th|tj|tk|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|va|vc|ve|vg|vn|vu|wf|ws|ye|yu|za|zm|zr|zw";
        // String regex = "^((https|http|ftp|rtsp|mms)?://)" +
        // "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //
        // ftp的user@
        // + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
        // + "|" // 允许IP和DOMAIN（域名）
        // + "(([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]+\\.)?" // 域名- www.
        // + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名
        // + "(" + domainRules + "))" // first level domain- .com or .museum
        // + "(:[0-9]{1,4})?" // 端口- :80
        // + "((/?)|" // a slash isn't required if there is no file name
        // + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
        // Pattern pattern = Pattern.compile(regex);
        // Matcher isUrl = pattern.matcher(str);
        // return isUrl.matches();
        return str.startsWith("http");
    }

    /**
     * 功能：检测当前URL是否可连接或是否有效, 描述：最多连接网络 5 次, 如果 5 次都不成功，视为该地址不可用
     * 
     * @param urlStr
     *            指定URL网络地址
     * @return URL
     */
    public static boolean isConnect(String urlStr) {
        int counts = 0;
        if (urlStr == null || urlStr.length() <= 0) {
            return false;
        }
        while (counts < 5) {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(3000);
                int state = con.getResponseCode();
                if (state == 200) {
                    break;
                } else {
                    counts++;
                }
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }
}
