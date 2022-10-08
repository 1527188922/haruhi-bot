package com.haruhi.bot.dto.searchImage.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class Results implements Serializable {
    public Header header;
    private com.haruhi.bot.dto.searchImage.response.Data data;
}
