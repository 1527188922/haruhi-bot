package com.haruhi.bot.dto.searchImage.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class Results implements Serializable {
    private Header header;
    private com.haruhi.bot.dto.searchImage.response.Data data;
}
