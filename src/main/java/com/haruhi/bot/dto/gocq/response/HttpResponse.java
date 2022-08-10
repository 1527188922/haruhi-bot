package com.haruhi.bot.dto.gocq.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HttpResponse implements Serializable {
    private int retcode;
    private String msg;
    private String status;
    private RespData data;
    private String wording;

    @Data
    public static class RespData implements Serializable {
        private List<String> slices;
    }
}
