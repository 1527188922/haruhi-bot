package com.haruhi.bot.dto.gocq.response;

import lombok.Data;

@Data
public class HttpResponse<T> {
    private int retcode;
    private String status;
    private T data;
}
