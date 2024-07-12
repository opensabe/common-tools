package io.github.opensabe.common.utils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.github.opensabe.common.utils.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xliao
 */

public class CommonUtil {
    private static Log log = LogFactory.getLog(CommonUtil.class);
    private static final BigDecimal ssqAmount = new BigDecimal("2");
    private static String[] citys = new String[]
            {"11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34", "35", "36", "37", "41", "42", "43", "44",
                    "45", "46", "50", "51", "52", "53", "54", "61", "62", "63", "64", "65", "71", "81", "82", "91"};
    private static String regexp = "^((\\d{17}|\\d{14})(\\d|x|X))$";
    private static Pattern pattern = Pattern.compile(regexp);
    private static int[] iWeight = new int[]
            {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1};
    private static String[] cCheck = new String[]
            {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
    private static String[] CANDIDATECHAR = new String[]
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

    private static final char[] bcdLookup =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final Pattern namePattern = Pattern.compile("^[\u0391-\uFFE5]{2,10}$");
    private static final int IDENTITYNO_MAX_BYTES = 30;
    private static final int ACCOUNTNAME_MAX_BYTES = 30;
    private static final int MOBILE_MAX_BYTES = 20;
    //错误码状态
    public static final int PAY_RESULT_SUCCESS = 0;
    public static final int PAY_RESULT_EXCEPTION = 1;

    /**
     * 活动ini的key 正则校验
     * 校验规则：不能包含汉字，只有大小写字母、数字和特殊符号，总长度不超过100
     */
//    private static final String ACTIVITY_INI_KEY_PARRERN = "^[A-Za-z0-9._-]+$";
//    private static final int ACTIVITY_INI_KEY_LENGTH = 100;

    private CommonUtil() {

    }

    /**
     * 校验身份证号码是否合法
     *
     * @param identityNo
     * @return
     */
    public static boolean isValidIdentityNo(String identityNo) {
        if (StringUtils.isBlank(identityNo.trim())) {
            return false;
        }
        int length = identityNo.length();
        if (!pattern.matcher(identityNo).find()) {
            return false;
        }
        if (length == 15) {
            String city = identityNo.substring(0, 2);
            if (StringUtils.indexOfAny(city, citys) < 0) // 检查城市是否合法
            {
                return false;
            }
            String birth = identityNo.substring(6, 12);
            Date transDate = DateUtil.getIntervalDateFormat(birth, "yyMMdd", 0);
            if (transDate == null) {
                return false;
            }
            String transBirth = DateUtil.formatDate(transDate, DateUtil.FMT_DATE_YYMMDD);
            if (!birth.equalsIgnoreCase(transBirth)) {
                return false;
            }
            return true;
        } else if (length == 18) {
            String city = identityNo.substring(0, 2);
            if (StringUtils.indexOfAny(city, citys) < 0) // 检查城市是否合法
            {
                return false;
            }
            String birth = identityNo.substring(6, 14);
            Date transDate = DateUtil.getIntervalDateFormat(birth, "yyyyMMdd", 0);
            if (transDate == null) {
                return false;
            }
            String transBirth = DateUtil.formatDate(transDate, DateUtil.FMT_DATE_SPECIAL);
            if (!birth.equalsIgnoreCase(transBirth)) {
                return false;
            }
            int total = 0;
            for (int i = 0; i < 17; i++) {
                total += Integer.valueOf(identityNo.substring(i, i + 1)) * iWeight[i];
                // System.out.println(Integer.valueOf(identityNo.substring(i,
                // i+1)) +"*"+iWeight[i]);
            }
            int mo = total % 11;
            String lastOne = cCheck[mo];
            return identityNo.substring(17).equalsIgnoreCase(lastOne);
        } else {
            return false;
        }
    }

    /**
     * 判断是否为正整数，含0
     *
     * @param srcToCheck
     * @return
     */
    public static boolean ifPositiveNumeric(String srcToCheck) {
        if (srcToCheck == null)
            return false;
        int size = srcToCheck.length();
        for (int i = 0; i < size; i++)
            if (!Character.isDigit(srcToCheck.charAt(i)))
                return false;

        return true;

    }

    public static boolean isValidMobile(String mobile) {
        Pattern p = Pattern.compile("^(13|14|15|17|18)\\d{9}$");
        Matcher m = p.matcher(mobile);
        if (!m.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isValidQQ(String qq) {
        Pattern p = Pattern.compile("^[1-9]\\d{4,11}$");
        Matcher m = p.matcher(qq);
        if (!m.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isValidEmail(String email) {
        Pattern p = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
        Matcher m = p.matcher(email);
        if (!m.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为浮点型的0 0 --> true 0.0 --> true 0.000 --> true 1 --> false
     *
     * @param srcToCheck
     * @return
     */
    public static boolean ifFloatZero(String srcToCheck) {
        String regEx = "[0]\\.[0]+|[0]";

        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(srcToCheck);
        if (!m.matches()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 检查是否是正浮点数 该方法用于判断金额是否是非0，且是否是0.00或则0.0或则 0 格式
     *
     * @param srcToCheck
     * @return
     */
    public static boolean ifPositiveFloat(String srcToCheck) {
        try {
            String regEx = "^[1-9]*[1-9][0-9]*$|^(([0]\\.[0-9]{1,2})|([1-9]{1,}[0-9]*\\.[0-9]{1,2}))$";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(srcToCheck);
            if (!m.matches()) {
                return false;
            }

            if (srcToCheck.split("\\.")[0].length() > 10) {
                return false;
            }

            BigDecimal big = new BigDecimal(srcToCheck);
            if (big.compareTo(new BigDecimal(0)) <= 0) {
                return false;
            }
            return true;

        } catch (Throwable e) {
            log.fatal(e);
            return false;
        }
    }

    /**
     * 获得完整的错误信息
     *
     * @param e
     * @return
     */
    public static String getFullStackError(Throwable e) {

        StringWriter sw = new StringWriter();
        PrintWriter ps = new PrintWriter(sw);
        e.printStackTrace(ps);
        return sw.toString();
    }

    /**
     * 判断list是否为null
     */
    @SuppressWarnings("rawtypes")
    public static boolean ifListNotNull(List list) {
        return null != list && list.size() > 0;
    }

    /**
     * 判断list是否为null
     */
    @SuppressWarnings("rawtypes")
    public static boolean ifListNull(List list) {
        return !ifListNotNull(list);
    }

    /**
     * 对序列号进行初始化，如果>8位则不管，如果<8位，则左补0
     *
     * @param numberToFormat
     * @return
     */
    public static String formatSequence(long numberToFormat) {
        DecimalFormat format = new DecimalFormat("00000000");
        return format.format(numberToFormat);
    }

    /**
     * 对序列号进行初始化，如果>9位则不管，如果<9位，则左补0
     *
     * @param numberToFormat
     * @return
     */
    public static String formatSpecialSequence(long numberToFormat) {
        DecimalFormat format = new DecimalFormat("000000000");
        return format.format(numberToFormat);
    }

    public static String getEncryptNickName(String nickName) {
        try {
            String head = CommonUtil.trimTailStr(nickName, 2, false);
            String tail = "";
            if (nickName.length() > 2) {
                tail = CommonUtil.getStrTail(nickName, 1);
            }
            return head + "**" + tail;
        } catch (Throwable e) {
            log.error("截取昵称出错！" + nickName, e);
            return "";
        }
    }

    public static String getEncryptAccountId(String accountId, int limit) {
        try {
            int at = StringUtils.indexOf(accountId, "@");
            if (at < 0) {
                return accountId;
            }
            String prefix = accountId.substring(0, at);
            if (prefix.length() <= limit) {
                return prefix + "**";
            }
            return CommonUtil.getStrHead(prefix, limit) + "**";
        } catch (Throwable e) {
            log.error("截取urs账号出错！" + accountId, e);
            return "";
        }
    }

    public static void main(String[] args) {
        //		String url = "http://caipiao.163.com/t/hit/g_2014112914CP11035571.html";
        //		System.out.println(getOrderIdFormUrl(url));
        String mss = getMd5Sign("000000", "utf-8");
        System.out.println(mss);
    }

    /**
     * 将BigDecimal 格式化为 0.00的格式
     *
     * @param big
     * @return
     */
    public static String formatBigDecimal(BigDecimal big) {
        if (big == null) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(big);
    }

    public static String formatBigDecimalOne(BigDecimal big) {
        if (big == null) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(big);
    }

    /**
     * 检查该BigDecimal是否是空或0
     *
     * @param big
     * @return
     */
    public static boolean isBigDecimalZeroOrNull(BigDecimal big) {
        if (big == null || big.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        return false;
    }

    /**
     * 将String 格式化为 0.00的格式
     *
     * @param big
     * @return
     */
    public static String formatBigDecimal(String big) {

        if (big == null) {
            return "";
        }
        DecimalFormat d = new DecimalFormat("0.00");

        BigDecimal temp = new BigDecimal(big);

        return d.format(temp);
    }

    public static String formatBigDecimal(int big) {
        return formatBigDecimal(String.valueOf(big));
    }

    /**
     * 对比两个字符串是否相等
     *
     * @return
     */
    public static boolean if2StrEqualsIgnoreCase(String str1, String str2) {

        if (str1.equalsIgnoreCase(str2)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将首字小写
     *
     * @param sourceStr
     * @return
     */
    public static String firstToLowCase(String sourceStr) {
        if (null == sourceStr || sourceStr.length() == 0) {
            return sourceStr;
        }
        char chars[] = sourceStr.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * 将首字大写
     *
     * @param sourceStr
     * @return
     */
    public static String firstToUpperCase(String sourceStr) {
        if (null == sourceStr || sourceStr.length() == 0) {
            return sourceStr;
        }
        char chars[] = sourceStr.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * 进行MD5加扰
     *
     * @return
     */
    public static String getMd5Sign(String srcToConvert, String encoding) {
        String cryptograph = null;
        try {
            byte passToConvertByte[] = srcToConvert.getBytes(encoding);
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            byte gottenPassByte[] = messagedigest.digest(passToConvertByte);
            cryptograph = "";
            for (int i = 0; i < gottenPassByte.length; i++) {
                String temp = Integer.toHexString(gottenPassByte[i] & 0x000000ff);
                if (temp.length() < 2)
                    temp = "0" + temp;
                cryptograph += temp;
            }
        } catch (Throwable e) {
            cryptograph = null;
        }
        return cryptograph;
    }

    /**
     * @param str
     * @return
     */
    public static String getMD5CodeOfWoZhongLa(String str) {
        MessageDigest md5 = null;
        // 用Integer.toHexString(int);//也可以
        // 这样就不用查表法
        char[] table =
                {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        byte[] tmp = null;

        StringBuilder build = new StringBuilder();
        try {
            md5 = MessageDigest.getInstance("MD5");

            tmp = md5.digest(str.getBytes());
            for (int i = 0; i < table.length; i++) {
                build.append(table[(tmp[i] >> 4) & 0x0F]).append(table[tmp[i] & 0x0F]);
            }

            return build.toString();

        } catch (Throwable e) {
            throw new RuntimeException("MD5加密过程出错: " + e);
        }
    }// toMD5 end

    // 计算MD5的通用算法,传入算法的通用编码方式
    public static String getMD5Code(String str, String encoding) {
        MessageDigest md5 = null;
        // 用Integer.toHexString(int);//也可以
        // 这样就不用查表法
        char[] table =
                {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        byte[] tmp = null;

        StringBuilder build = new StringBuilder();
        try {
            md5 = MessageDigest.getInstance("MD5");

            tmp = md5.digest(str.getBytes(encoding));
            for (int i = 0; i < table.length; i++) {
                build.append(table[(tmp[i] >> 4) & 0x0F]).append(table[tmp[i] & 0x0F]);
            }

            return build.toString();

        } catch (Throwable e) {
            throw new RuntimeException("MD5加密过程出错: " + e);
        }
    }

    /**
     * 对字符串进行截取，对中文进行处理
     *
     * @param orgStr
     * @param lengthToGet
     * @return
     */
    public static String trimStr(String orgStr, int lengthToGet) {
        return trimStr(orgStr, lengthToGet, true);
    }

    /**
     * 对字符串进行截取,对中文进行处理
     *
     * @param orgStr
     * @param lengthToGet
     * @return
     */
    public static String trimStr(String orgStr, int lengthToGet, boolean displayDot) {

        if (StringUtils.isBlank(orgStr))
            return "";
        if (orgStr.length() <= lengthToGet)
            return orgStr;
        int length = displayDot ? 1 : 0;
        lengthToGet *= 2;
        for (int i = 0; i < orgStr.length(); i++) {
            length += Character.codePointAt(orgStr, i) < 256 ? 1 : 2;
            if (length > lengthToGet)
                return displayDot ? orgStr.substring(0, i) + "..." : orgStr.substring(0, i);
        }
        return orgStr;
    }

    //固定长度，三个字显示.不足则空格填充
    public static String trimOrAddSpace(String orgStr) {

        String tmp = trimStr(orgStr, 3, false);
        if (tmp.length() == 3)
            return tmp;
        if (tmp.length() == 2) {
            return orgStr.substring(0, 1) + "　" + orgStr.substring(1, 2);
        }
        return tmp;
    }

    /**
     * 对字符串尾部进行截取，保留前若干个中文字符，两个英文字符换算作一个中文字符
     *
     * @param orgStr
     * @param lengthToGet
     * @return
     */
    public static String trimTailStr(String orgStr, int lengthToGet) {
        return trimTailStr(orgStr, lengthToGet, true);
    }

    /**
     * 对字符串尾部进行截取，保留前若干个中文字符，两个英文字符换算作一个中文字符
     *
     * @param orgStr
     * @param lengthToGet
     * @return
     */
    public static String trimTailStr(String orgStr, int lengthToGet, boolean displayDot) {

        if (StringUtils.isBlank(orgStr))
            return "";
        if (orgStr.length() <= lengthToGet)
            return orgStr;
        int length = 0;
        lengthToGet *= 2;
        for (int i = 0; i < orgStr.length(); i++) {
            length += Character.codePointAt(orgStr, i) < 256 ? 1 : 2;
            if (length > lengthToGet)
                return displayDot ? orgStr.substring(0, i) + "..." : orgStr.substring(0, i);
        }
        return orgStr;
    }

    /**
     * 对字符串尾部进行截取，保留前若干个中文字符，两个英文字符换算作一个中文字符
     *
     * @param orgStr
     * @param lengthToGet
     * @return
     */
    public static String trimAllTailStr(String orgStr, int lengthToGet) {
        if (StringUtils.isBlank(orgStr))
            return "";
        if (orgStr.length() <= lengthToGet)
            return orgStr + "***";
        int length = 0;
        lengthToGet *= 2;
        for (int i = 0; i < orgStr.length(); i++) {
            length += Character.codePointAt(orgStr, i) < 256 ? 1 : 2;
            if (length > lengthToGet)
                return orgStr.substring(0, i) + "***";
        }
        return orgStr + "***";
    }

    /**
     * 为敏感字符串加上型号
     *
     * @param plainStr
     * @param headLength 头几位显示
     * @param tailLength 末几位显示
     * @param starLength 星号的数量
     * @return
     */
    public static String getSecretNumberWithStart(String plainStr, int headLength, int tailLength, int starLength) {
        if (StringUtils.isBlank(plainStr)) {
            return "";
        }
        StringBuilder starString = new StringBuilder("");
        for (int i = 1; i <= starLength; i++) {
            starString.append("*");
        }
        if (null == plainStr || "".equals(plainStr)) {
            return "";
        } else if (plainStr.length() <= tailLength) {
            return plainStr;
        } else {
            return plainStr.substring(0, headLength) + starString
                    + plainStr.substring(plainStr.length() - tailLength, plainStr.length());
        }
    }

    /**
     * 校验 金额 必须 是2元的倍数 注意 ： 这里 amount 用int 类型 且amount 必须大于2
     *
     * @return
     */
    public static String cutOffAndAddEllipsis(String plainStr, int from, int to, int frontNumOfEllipsis,
                                              int tailNumOfEllipsis) {
        if (StringUtils.isBlank(plainStr)) {
            return "";
        }
        int length = plainStr.length();
        if (from >= length || to >= length) {
            return plainStr;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < frontNumOfEllipsis; i++) {
            result.append("...");
        }
        result.append(plainStr.substring(from, to));
        for (int i = 0; i < tailNumOfEllipsis; i++) {
            result.append("***");
        }
        return result.toString();
    }

    /**
     * 校验 金额 必须 是2元的倍数 注意 ： 这里 amount 用int 类型 且amount 必须大于2
     *
     * @return
     */
    public static boolean validateAmountIs2multiple(BigDecimal amount) {
        try {
            if (amount == null) {
                return false;
            } else if (amount.compareTo(ssqAmount) < 0) {
                return false;
            }

            BigDecimal[] result = amount.divideAndRemainder(ssqAmount);
            if (result[1].compareTo(BigDecimal.ZERO) == 0) {
                return true;
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 白天游戏10:00-22:00，每10分钟一期，共72期 夜间游戏22:00-1:55，每5分钟一期，共48期
     *
     * @return
     */
    public static Timestamp translateCqsscTime(Timestamp endTime) {
        Timestamp result = null;

        Calendar cal = Calendar.getInstance();

        cal.setTime(endTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour > 10 && hour < 22) {
            result = DateUtil.getIntervalTimestamp(endTime, -10);
        } else if (hour >= 22 && hour <= 2) {
            result = DateUtil.getIntervalTimestamp(endTime, -5);
        }

        return result;
    }

    public static String conact(String... str) {
        StringBuffer sb = new StringBuffer();
        for (String s : str) {
            if (StringUtils.isNotBlank(s)) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public static String conact(char separator, String... str) {
        StringBuffer sb = new StringBuffer();
        int index = 0;
        for (String s : str) {
            if (StringUtils.isNotBlank(s)) {
                sb.append(s);
                if (index < str.length - 1) {
                    sb.append(separator);
                }
            }
            index++;
        }
        return sb.toString();
    }

    /**
     * 将字符串限制到某个长度. 如果超过长度，则取后limit位
     *
     * @param org   需要限制的字符串
     * @param limit 长度
     */
    public static String trimToLength(String org, int limit) {
        String temp = "";
        if (org.length() > limit) {
            temp = org.substring(org.length() - limit, org.length());
        } else {
            return org;
        }
        return temp;
    }

    public static String getStrTail(String str, int limit) {
        if (limit <= 0) {
            return "";
        }
        if (limit >= str.length()) {
            return "";
        } else {
            return str.substring(str.length() - limit, str.length());
        }
    }

    public static String getStrHead(String str, int limit) {
        if (limit <= 0) {
            return "";
        }
        if (limit >= str.length()) {
            return "";
        } else {
            return str.substring(0, limit);
        }

    }

    /**
     * 将null过滤为“”
     *
     * @param str
     * @return
     */
    public static String filterNullStr(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        } else {
            return str;
        }
    }

    public static String numberFormat(BigDecimal bd, String format) {
        if (bd == null || "0".equals(bd.toString())) {
            return "";
        }

        DecimalFormat bf = new DecimalFormat(format);
        return bf.format(bd);
    }

    /**
     * 生成随机码，注意要判断敏感字串
     *
     * @param length 随机码长度
     * @return
     */
    public static String generateRandomCode(int length) {
        Random r = new Random(System.currentTimeMillis());
        StringBuilder resultstr = new StringBuilder("");

        for (int i = 0; i < length; i++) {
            int tmp = r.nextInt(CANDIDATECHAR.length);
            resultstr.append(CANDIDATECHAR[tmp]);
        }
        return resultstr.toString();
    }

    /**
     * 生成12位随机码 不包含敏感词 不包含I O 0 1字符
     * 4*8
     *
     * @return
     */
    public static String generateUUID() {
        //		String uuid = RandomUuidFactory.getInstance().createUUID(16);
        //		uuid = Base36.encode(uuid, 12);

        String uuid = null;
        //		do
        //		{
        uuid = RandomUuidFactory.getInstance().createUUID(16);
        uuid = Base36.encode(uuid, 12);
        //		}
        //		while (SensitiveWordStaticValue.isContainSensitiveWord(uuid));
        return uuid;
    }

    /**
     * 生成12位随机码 不包含敏感词
     *
     * @return
     */
    public static String generateUUIDWithIO() {
        //		String uuid = RandomUuidFactory.getInstance().createUUID(16);
        //		uuid = Base36.encode(uuid, 12, Boolean.TRUE);

        String uuid = null;
        //		do
        //		{
        uuid = RandomUuidFactory.getInstance().createUUID(16);
        uuid = Base36.encode(uuid, 12, Boolean.FALSE);
        //		}
        //		while (SensitiveWordStaticValue.isContainSensitiveWord(uuid));
        return uuid;
    }

    /**
     * 加密字符串，使用*字符覆盖源字符串中的指定部分直至结尾
     *
     * @param str   源字符串
     * @param start 开始位置（包括在覆盖部分内）
     * @return
     */
    public static String encryptString(String str, int start) {
        if (StringUtils.isBlank(str)) {
            return "";
        } else {
            return encryptString(str, start, str.length());
        }
    }

    /**
     * 加密字符串，使用*字符覆盖源字符串中的指定部分
     *
     * @param str   源字符串
     * @param start 开始位置（包括在覆盖部分内），且总是start与end中较小的
     * @param end   终止位置（不包括在覆盖部分内），且总是start与end中较大的
     * @return
     */
    public static String encryptString(String str, int start, int end) {
        int min = min(start, end);
        int max = max(start, end);

        min = max(min, 0);
        max = min(max, str.length());

        if (StringUtils.isNotBlank(str)) {
            return StringUtils.overlay(str, StringUtils.repeat("*", max - min), min, max);
        } else {
            return "";
        }
    }

    public static int min(int a, int b) {
        return a < b ? a : b;
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    public static int abs(int n) {
        return n >= 0 ? n : -n;
    }

    /**
     * 加密邮箱字符串，使用*字符覆盖源字符串中的指定部分 从指定的开始位置，到@字符出现的位置之前，都被*字符覆盖
     *
     * @param str   源字符串
     * @param start 开始位置（包括在覆盖部分内）
     * @return
     */
    public static String encryptEmailString(String str, int start) {
        int at = StringUtils.indexOf(str, "@");
        int start_index = at <= start ? 0 : start;

        return encryptString(str, start_index, at);
    }

    /**
     * 身份信息转成JSON格式的字符串
     * {"name":"测试","id":"999103199907160410","mobile":"13810149999"
     * ,"bind":"1","userName":"abc"}
     *
     * @param name     姓名
     * @param id       身份证号
     * @param mobile   手机号
     * @param userName 用户名
     * @param bind     是否已绑定手机
     * @return JSON格式的字符串
     * @throws JSONException JSON转换异常
     */
    public static String identityInfo2JSON(String name, String id, String mobile, String bind, String userName)
            throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("name", StringUtils.isNotBlank(name) ? name : "");
        jsonObj.put("mobile", StringUtils.isNotBlank(mobile) ? mobile : "");
        jsonObj.put("id", StringUtils.isNotBlank(id) ? id : "");
        jsonObj.put("bind", StringUtils.isNotBlank(bind) ? bind : "");
        jsonObj.put("userName", StringUtils.isNotBlank(userName) ? userName : "");
        return jsonObj.toString();
    }

    /**
     * 将源字符串重复拼接指定次数，中间以分隔符相隔<br/>
     * 例如源字符串="abc"，分隔符="|"，指定3次，返回结果为"abc|abc|abc"
     *
     * @param source    源字符串
     * @param seperator 分隔符
     * @param times     指定次数
     * @return
     */
    public static String repeateWithSeperator(String source, String seperator, int times) {
        if (StringUtils.isBlank(source)) {
            source = "";
        }
        if (StringUtils.isBlank(seperator)) {
            seperator = "";
        }
        if (times <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(source).append(seperator);
        }
        if (builder.length() != 0 && StringUtils.isNotBlank(seperator)) {
            //builder.deleteCharAt(builder.length() - 1); // 删去最后一个分隔符
            builder.delete(builder.length() - seperator.length(), builder.length());// 删去最后一个分隔符
        }
        return builder.toString();
    }

    public static String joinWithSeperator(List<String> source, String seperator) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        if (StringUtils.isBlank(seperator)) {
            seperator = "";
        }
        StringBuilder builder = new StringBuilder();
        for (String str : source) {
            builder.append(str).append(seperator);
        }
        if (builder.length() != 0 && StringUtils.isNotBlank(seperator)) {
            //builder.deleteCharAt(builder.length() - 1);
            builder.delete(builder.length() - seperator.length(), builder.length());// 删去最后一个分隔符
        }
        return builder.toString();
    }

    public static String concatenateWithSeperator(String seperator, String... params) {
        StringBuilder s = new StringBuilder();
        for (String param : params) {
            s.append(param).append(seperator);
        }
        if (StringUtils.isNotBlank(s.toString())) {// 删去最后一个seperator
            s.delete(s.length() - seperator.length(), s.length());
        }
        return s.toString();
    }

    public static String joinIntegersWithSeperator(List<Integer> source, String seperator) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        if (StringUtils.isBlank(seperator)) {
            seperator = "";
        }
        StringBuilder builder = new StringBuilder();
        for (Integer num : source) {
            builder.append(Integer.toString(num)).append(seperator);
        }
        if (builder.length() != 0 && StringUtils.isNotBlank(seperator)) {
            //builder.deleteCharAt(builder.length() - 1);
            builder.delete(builder.length() - seperator.length(), builder.length());// 删去最后一个分隔符
        }
        return builder.toString();
    }

    public static String joinLongIntegersWithSeperator(List<Long> source, String seperator) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        if (StringUtils.isBlank(seperator)) {
            seperator = "";
        }
        StringBuilder builder = new StringBuilder();
        for (Long num : source) {
            builder.append(Long.toString(num)).append(seperator);
        }
        if (builder.length() != 0 && StringUtils.isNotBlank(seperator)) {
            //builder.deleteCharAt(builder.length() - 1);
            builder.delete(builder.length() - seperator.length(), builder.length());// 删去最后一个分隔符
        }
        return builder.toString();
    }

    /**
     * 获取子list，如果maxToIndex> target.size(),则maxToIndex=target.size();
     *
     * @param target
     * @param fromIndex
     * @param maxToIndex
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static List subList(List target, int fromIndex, int maxToIndex) {
        if (target == null) {
            return Collections.EMPTY_LIST;
        }
        return target.subList(fromIndex, target.size() > maxToIndex ? maxToIndex : target.size());
    }

    public static String getRequestUrl(HttpServletRequest request) {
        String port = "";
        if (request.getServerPort() != 80) {
            port = String.valueOf(request.getServerPort());
            port = ":" + port;
        }
        String basePath = request.getScheme() + "://" + request.getServerName() + port + request.getContextPath()
                + request.getServletPath();
        Map<String, String[]> map = request.getParameterMap();
        String url = "";
        int i = 1;
        for (String paraName : map.keySet()) {
            String[] value = map.get(paraName);
            url += spliceParameterValues(paraName, value, i);
            i++;
        }
        basePath = basePath + url;
        return basePath;

    }

    public static String spliceParameterValues(String paraName, String[] values, int flag) {
        StringBuffer sb = new StringBuffer();
        for (String str : values) {
            if (flag == 1) {
                sb.append("?" + paraName).append("=").append(str);
            } else {
                sb.append("&" + paraName).append("=").append(str);
            }
        }
        return sb.toString();
    }

    public static boolean verifyTrueName(String trueName) {
        boolean result;
        if (StringUtils.isBlank(trueName)) {
            result = false; // 空
        } else if (trueName.getBytes().length > ACCOUNTNAME_MAX_BYTES) {
            result = false; // 超过数据库字段允许长度
        } else if (!namePattern.matcher(trueName).find()) {
            result = false; // 格式不正确
        } else {
            result = true;
        }
        return result;
    }

    public static boolean verifyIdentityNo(String identityNo) {
        boolean result;
        if (StringUtils.isBlank(identityNo)) {
            result = false; // 空
        } else if (identityNo.getBytes().length > IDENTITYNO_MAX_BYTES) {
            result = false; // 超过数据库字段允许长度
        } else if (!CommonUtil.isValidIdentityNo(identityNo)) {
            result = false; // 格式不正确
        } else {
            result = true;
        }

        return result;
    }

    public static boolean verifyMobile(String mobile) {
        boolean result;
        if (StringUtils.isBlank(mobile)) {
            result = false; // 空
        } else if (mobile.getBytes().length > MOBILE_MAX_BYTES) {
            result = false; // 超过数据库字段允许长度
        } else if (!CommonUtil.isValidMobile(mobile)) {
            result = false; // 格式不正确
        } else {
            result = true;
        }
        return result;
    }

    public static String formatPositive(String str) {
        if (StringUtils.isBlank(str))
            return str;
        if (str.contains("+"))
            return str;
        return new BigDecimal(str).compareTo(BigDecimal.ZERO) > 0 ? "+" + str : str;
    }

    public static String formatPositive(String str, int scale) {
        if (StringUtils.isBlank(str))
            return str;
        if (str.contains("+"))
            return str;
        return new BigDecimal(str).setScale(scale, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) > 0 ? "+" + str : str;
    }

    public static String formatMoney(String str) {
        if (StringUtils.isBlank(str)) {
            return "0.00";
        }
        try {
            BigDecimal n = new BigDecimal(str);
            int scale = n.scale();
            if (scale < 2) {
                scale = 2;
            } else if (scale > 3) {
                scale = 3;
            }
            return n.setScale(scale, RoundingMode.DOWN).toString();
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug("格式转换字符串转数字时发生错误!str:" + str, e);
            }
            return "0.00";
        }
    }

    public static String formatMoneyDouble(String str) {
        if (StringUtils.isBlank(str)) {
            return "0.00";
        }
        try {
            BigDecimal tmp1 = new BigDecimal(str);
            BigDecimal tmp2 = new BigDecimal(2);
            BigDecimal n = tmp1.multiply(tmp2);
            int scale = n.scale();
            if (scale < 2) {
                scale = 2;
            } else if (scale > 3) {
                scale = 3;
            }
            return n.setScale(scale, RoundingMode.DOWN).toString();
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug("格式转换字符串转数字时发生错误", e);
            }
            return "0.00";
        }
    }

    //判断字符串是否是数字
    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 去掉字符串中的换行符号
     * base64编码较长的字符串时，得到的字符串有换行符号
     * 有些场景下要求必须去掉
     *
     * @param str
     * @return
     */
    public static String filterLineBreak(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            int asc = str.charAt(i);
            if (asc != 10 && asc != 13) {
                sb.append(str.subSequence(i, i + 1));
            }
        }
        return new String(sb);
    }

    /**
     * 以亿或万表达数据
     *
     * @param src
     * @return 100, 000, 000
     */
    public static String formatMoneyNumber(String src) {
        if (StringUtils.isBlank(src))
            return "";
        try {
            Double d = Double.valueOf(src);
            long s = d.longValue();
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            nf.setMaximumFractionDigits(0);
            String result = nf.format(s);
            if (result.contains("￥")) {
                return result.substring(1) + "元";
            }
            return result + "元";
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return "";
        }

    }

    /**
     * 金额转换
     * 100000000
     *
     * @param money:要转换的金额
     * @param isShowYuan：如果超过一万是否显示元 isShowYuan=true(123456789->1亿2345万6789元)；isShowYuan=false(123456789->1亿2345万)
     * @return
     */
    public static String bigDecimalMoney(BigDecimal money, boolean isShowYuan) {
        if (money == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        if (money.compareTo(new BigDecimal("0")) < 0) {
            sb.append("-");
            money = money.multiply(new BigDecimal("-1"));
        }
        BigDecimal yiYuan = money.divideToIntegralValue(new BigDecimal("100000000")).setScale(0);
        money = money.subtract(yiYuan.multiply(new BigDecimal("100000000")));
        BigDecimal wanYuan = money.divideToIntegralValue(new BigDecimal("10000")).setScale(0);
        money = money.subtract(wanYuan.multiply(new BigDecimal("10000")));

        if (yiYuan.compareTo(new BigDecimal("0")) > 0) {
            sb.append(yiYuan).append("亿");
        }
        if (wanYuan.compareTo(new BigDecimal("0")) > 0) {
            sb.append(wanYuan).append("万");
        }
        if (isShowYuan || (yiYuan.compareTo(new BigDecimal("0")) <= 0 && wanYuan.compareTo(new BigDecimal("0")) <= 0)) {
            sb.append(money);
        }
        return sb.toString();
    }

    /**
     * 从[min, max)产生不重复的n个随机数。如果n过大，那么就返回[min, max)
     */
    public static void getRandomNumberSet(List<String> usableList, int n, HashSet<String> set) {
        if (n <= 0) {
            return;
        }

        if (n >= usableList.size()) {
            for (String i : usableList) {
                set.add(i);
            }
            return;
        }

        while (n > 0) {
            int index = (int) (Math.random() * usableList.size());
            set.add(usableList.get(index));// 将不同的数存入HashSet中
            usableList.remove(index);
            n--;
        }
    }

    /**
     * 将传入对象过滤null，返回字符串
     */
    public static String formatToString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    public static final String getIpStr() {
        try {
            InetAddress inet = InetAddress.getLocalHost();
            if (inet == null) {
                return null;
            }
            String ip = inet.getHostAddress();
            return ip;
        } catch (Throwable e) {
            return "";
        }
    }

    /**
     * 输入20140321类型的字符串
     */
    public static String getDateStr(String dateStr, int offset) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date d = format.parse(dateStr);

            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.DAY_OF_YEAR, offset);

            return format.format(cal.getTime());
        } catch (Throwable e) {
            log.fatal(dateStr + "不能进行前后偏移操作!", e);
            throw new BusinessException(dateStr + "不能进行前后偏移操作!");
        }
    }

    /**
     * String排序顺序：
     * null最大，""最小
     */
    public static int compareIncludeNull(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return 0;
        }
        if (str1 == null && str2 != null) {
            return 1;
        }
        if (str1 != null && str2 == null) {
            return -1;
        }
        return str1.compareTo(str2);
    }

    public static String getOrderIdFormUrl(String url) {
        if (StringUtils.isNotBlank(url) && url.indexOf("_") >= 0 && url.indexOf(".html") >= 0
                && url.indexOf("_") < url.indexOf(".html")) {
            String orderId = url.substring(url.lastIndexOf("_") + 1, url.indexOf(".html"));
            if (StringUtils.isNotBlank(orderId)) {
                return orderId;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /*
     * 将16进制字符串转换为字符数组
     */
    public static final byte[] hexStrToBytes(String s) {
        byte[] bytes;

        bytes = new byte[s.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }

        return bytes;
    }

    /*
    * 将字符数组转换为16进制字符串
    */
    public static final String bytesToHexStr(byte[] bcd) {
        StringBuffer s = new StringBuffer(bcd.length * 2);

        for (int i = 0; i < bcd.length; i++) {
            s.append(bcdLookup[(bcd[i] >>> 4) & 0x0f]);
            s.append(bcdLookup[bcd[i] & 0x0f]);
        }

        return s.toString();
    }

    public static boolean containsStrArr(String source, String[] strArr) {
        if (strArr == null || strArr.length == 0 || source == null) {
            return false;
        }
        for (String str : strArr) {
            if (source.contains(str)) {
                return true;
            }
        }
        return false;
    }
}
