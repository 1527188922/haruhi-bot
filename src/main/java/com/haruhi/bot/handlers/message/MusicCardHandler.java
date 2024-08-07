package com.haruhi.bot.handlers.message;

import com.haruhi.bot.cache.CacheMap;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.music.response.Song;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.music.AbstractMusicService;
import com.haruhi.bot.factory.MusicServiceFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.tools.rmi.ObjectNotFoundException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MusicCardHandler implements IMessageEvent {

    private static int expireTime = 60;
    private static CacheMap<String, List<Song>> cache = new CacheMap<>(expireTime, TimeUnit.SECONDS,100);

    @Override
    public int weight() {
        return 80;
    }

    @Override
    public String funName() {
        return "点歌";
    }

    private String getKey(final Message message){
        return getKey(message.getUserId(),message.getGroupId());
    }

    private String getKey(String userId, String groupId){
        String gid = Strings.isNotBlank(groupId) ? groupId : "";
        return userId + "-" + gid;
    }

    @Override
    public boolean onMessage(final Message message,final String command) {
        Integer index = null;
        try {
            index = Integer.valueOf(command.replace(" ",""));
        }catch (Exception e){}

        String key = getKey(message);
        List<Song> songs = cache.get(key);
        if(songs != null && index == null){
            String songName = getSongName(command);
            if(Strings.isNotBlank(songName)){
                // 表示再次点歌
                search(message,songName);
                return true;
            }
            // 存在缓存 但是输入的不是纯数字并且不是再次点歌 返回false，继续往下匹配
            return false;
        }else if(songs != null){
            // 存在缓存 输入了纯数字
            if(index <= 0 || index > songs.size()){
                Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),"不存在序号" + index + "的歌曲", GocqActionEnum.SEND_MSG,true);
                return true;
            }
            // 若这里删除缓存，那么一次搜索只能点一次歌
            // 若不删除，那么在缓存 存在期间，都可以通过输入纯数字持续点歌
            // cache.remove(key);
            ThreadPoolFactory.getCommandHandlerThreadPool().execute(new SendMusicCardTask(songs,index,message));
            return true;
        }else{
            // 不存在缓存
            String songName = getSongName(command);
            if(Strings.isNotBlank(songName)){
                // 匹配上命令 开始搜索歌曲
                search(message,songName);
                return true;
            }
        }
        return false;
    }
    private void search(Message message,String songName){
        Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),"开始搜索歌曲：" + songName,GocqActionEnum.SEND_MSG,true);
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new SearchMusicTask(message,songName));
    }

    private String getSongName(final String command){
        return CommonUtil.commandReplaceFirst(command, RegexEnum.MUSIC_CARD);
    }

    private class SearchMusicTask implements Runnable{

        private Message message;
        private String musicName;

        SearchMusicTask(Message message,String musicName){
            this.message = message;
            this.musicName = musicName;
        }

        @Override
        public void run() {
            try {
                List<Song> res = MusicServiceFactory.getMusicService(MusicServiceFactory.MusicType.cloudMusic).searchMusic(musicName);
                if(CollectionUtils.isEmpty(res)){
                    Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),"未找到歌曲：" + musicName,GocqActionEnum.SEND_MSG,true);
                    return;
                }
                if(res.size() == 1){
                    // 搜索结果只有一条，直接发送这个音乐卡片
                    sendMusicCard(message,res,0,true);
                    return;
                }
                // 将搜索结果保存到缓存
                cache.put(getKey(message),res);
                if(MessageEventEnum.group.getType().equals(message.getMessageType())){
                    sendGroup(message,res,musicName);
                }else if(MessageEventEnum.privat.getType().equals(message.getMessageType())){
                    sendPrivate(message,res,musicName);
                }
            } catch (Exception e) {
                log.error("搜索歌曲发生异常",e);
            }
        }
    }

    private void sendGroup(Message message,List<Song> songs,String songName){
        int size = songs.size();
        ArrayList<ForwardMsg> forwardMsgs = new ArrayList<>(size + 1);
        forwardMsgs.add(CommonUtil.createForwardMsgItem(
                MessageFormat.format("搜索【{0}】成功！接下来请在{1}秒内发送纯数字序号选择歌曲",songName,expireTime),message.getSelfId(), BotConfig.NAME)
        );
        for (int i = 0; i < size; i++) {
            Song e = songs.get(i);
            forwardMsgs.add(CommonUtil.createForwardMsgItem(
                    MessageFormat.format("{0}：{1}\n歌手：{2}\n专辑：{3}",(i + 1),e.getName(),e.getArtists(),e.getAlbumName()),message.getSelfId(),BotConfig.NAME
            ));
        }
        Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,message.getGroupId(),forwardMsgs);
    }
    private void sendPrivate(Message message,List<Song> songs,String songName){
        StringBuilder stringBuilder = new StringBuilder(MessageFormat.format("搜索【{0}】成功！接下来请在{1}秒内发送纯数字序号选择歌曲\n\n",songName,expireTime));
        int size = songs.size();
        for (int i = 0; i < size; i++) {
            Song e = songs.get(i);
            stringBuilder.append(MessageFormat.format("{0}：{1}\n歌手：{2}\n专辑：{3}\n\n",(i + 1),e.getName(),e.getArtists(),e.getAlbumName()));
        }
        Client.sendMessage(message.getUserId(),message.getGroupId(),MessageEventEnum.privat,stringBuilder.toString(),GocqActionEnum.SEND_MSG,true);
    }

    private class SendMusicCardTask implements Runnable{

        private List<Song> songs;
        private Integer index;
        private Message message;

        SendMusicCardTask(List<Song> songs,Integer index,Message message){
            this.songs = songs;
            this.index = index;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                sendMusicCard(message,songs,index - 1,true);
            } catch (ObjectNotFoundException e) {
                log.error("发送音乐卡片异常",e);
            }
        }
    }

    private void sendMusicCard(Message message, List<Song> songs, Integer index, boolean checked) throws ObjectNotFoundException {
        AbstractMusicService musicService = MusicServiceFactory.getMusicService(MusicServiceFactory.MusicType.cloudMusic);
        String musicCq = musicService.createMusicCq(songs, index,checked);
        Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),musicCq, GocqActionEnum.SEND_MSG,false);
    }


}
