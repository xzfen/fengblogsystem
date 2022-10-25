package net.feng.blog.utils;

public interface Constants {

    String FROM_PC = "p_";
    String FROM_MOBILE = "m_";

    //app的下载路径
    String APP_DOWNLOAD_PATH = "/portal/app/";

    interface User{
        String ROLE_ADMIN="role_admin";
        String ROLE_NORMAL="role_normal";
        String DEFAULT_AVATAR="";//需要准备一个头像图片
        String DEFAULT_STATE="1";
        //redis的key
        String KEY_CAPTCHA_CONTENT="key_captcha_content_";
        String KEY_EMAIL_CODE_CONTENT="key_email_code_content_";
        String KEY_EMAIL_SEND_IP="key_email_send_ip_";
        String KEY_EMAIL_SEND_ADDRESS="key_email_send_address_";
        String KEY_TOKEN="key_token_";
        String KEY_COMMIT_TOKEN_RECORD="key_commit_token_record_";

        String COOKIE_TOKEN_KEY="sob_blog_token";
        String COLUMN_USER_STATE_NORMAL = "1"; //用户状态码，1为正常
        String COLUMN_USER_STATE_DELETE = "0"; //用户状态码，0为删除
        String KEY_PC_LOGIN_ID = "key_pc_login_id_";
        String KEY_PC_LOGIN_STATE_FALSE = "false";
        String KEY_PC_LOGIN_STATE_TRUE = "true";
        int QR_CODE_STATE_CHECK_WAITING_TIME = 30;
        String LAST_REQUEST_LOGIN_ID = "l_r_l_i";
        String LAST_CAPTCHA_ID = "l_cap_i";
    }

    interface Settings{
        String ADMIN_ACCOUNT_INIT_STATE="admin_account_init_state";
        String WEB_INFO_TITLE = "web_info_title";
        String WEB_INFO_DESCRIPTION = "web_info_description";
        String WEB_INFO_KEYWORDS = "web_info_keywords";
        String WEB_SIZE_VIEW_COUNT = "web_size_view_count";
    }
    /*
    * 单位是秒
    * */
    interface TimeValueInSecond{
        int SECOND_10 = 10;
        int SEC_15 = 15;
        int MIN = 60;
        int MIN_5 = 5*MIN;
        int HOUR = 60*MIN;
        int HOUR_2 = 2*HOUR;
        int DAY = 24*HOUR;
        int MONTH = 30*DAY;
    }
    /*
     * 单位是毫秒
     * */
    interface TimeValueInMillisecond{
        long MIN = 60*1000;
        long HOUR = 60*MIN;
        long HOUR_2 = 2*HOUR;
        long DAY = 24*HOUR;
        long MONTH = 30*DAY;
    }

    interface Page{
        int DEFAULT_PAGE = 1;
        int DEFAULT_SIZE = 2;
    }

    interface ImageType {
        String PREFIX = "image/";
        String TYPE_JPG = "jpg";
        String TYPE_JPEG = "jpeg";
        String TYPE_PNG = "png";
        String TYPE_GIF = "gif";
        String TYPE_JPG_WITH_PREFIX = PREFIX + "jpg";
        String TYPE_JPEG_WITH_PREFIX = PREFIX + "jpeg";
        String TYPE_PNG_WITH_PREFIX = PREFIX + "png";
        String TYPE_GIF_WITH_PREFIX = PREFIX + "gif";
    }

    interface Article {
        int TITLE_MAX_LENGTH = 128;
        int SUMMARY_MAX_LENGTH = 256;
        //0表示删除、1表示已经发布、2表示草稿、3表示置顶
        String STATE_DELETE = "0";
        String STATE_PUBLISH = "1";
        String STATE_DRAFT = "2";
        String STATE_TOP = "3";
        String KEY_ARTICLE_LIST_FIRST_PAGE = "key_article_list_first_page";
    }

    interface Comment {
        //0表示删除、1表示已经发布、2表示草稿、3表示置顶
        String STATE_PUBLISH = "1";
        String STATE_TOP = "3";
        String KEY_COMMENT_FIRST_PAGE_CACHE = "key_comment_first_page_cache_";
    }
}
