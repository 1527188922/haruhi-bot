package com.haruhi.bot.dto.searchImage.response;

import java.io.Serializable;
import java.util.Date;

@lombok.Data
public class Data implements Serializable {
    private Long member_id;
    private String[] ext_urls;
    private String pixiv_id;
    private String title;
    private String member_name;
    private String twitter_user_id;
    private String twitter_user_handle;
    private Date created_at;


}
