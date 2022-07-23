package com.haruhi.bot.extend;

import com.alibaba.fastjson.JSONObject;

public interface IPlugin {

    void onPrivateMessage(JSONObject var,String command);

    void onGroupMessage( JSONObject var,String command);
}
