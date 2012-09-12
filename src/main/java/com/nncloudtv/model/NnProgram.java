package com.nncloudtv.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.*;

import com.nncloudtv.lib.NnStringUtil;

/**
 * Programs under a NnChannel.
 * 
 * Terminology: 
 * Program: aka NnProgram. where video file is stored.
 * Episode: aka NnEpisode. Only 9x9 programs has "episode". It is "super-program", store each sub-episode's metadata.    
 */
@PersistenceCapable(table="nnprogram", detachable="true")
public class NnProgram implements Serializable {
    private static final long serialVersionUID = 5553891672235566066L;

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    private long channelId;

    @Persistent
    private long episodeId;
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String name;
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=500)
    private String comment;
    
    @Persistent
    private short contentType;
    public static final short CONTENTTYPE_DIRECTLINK = 0;
    public static final short CONTENTTYPE_YOUTUBE = 1;
    public static final short CONTENTTYPE_SCRIPT = 2;
    public static final short CONTENTTYPE_RADIO = 3;
    public static final short CONTENTTYPE_REFERENCE = 4;
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String intro;
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String imageUrl;
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String fileUrl;

    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String audioFileUrl;
    
    /**
     * used in 2 places:
     * 1. from channel parsing service, it's where the physical file stores, to avoid duplication.
     * 2. for "favorite" feature, used when the program type is "reference". in this case, programId will be stored  
     */
    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String storageId;
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String errorCode;

    @Persistent
    private short status;
    //general
    public static short STATUS_OK = 0;
    public static short STATUS_ERROR = 1;
    public static short STATUS_NEEDS_REVIEWED = 2;
    //quality
    public static short STATUS_BAD_QUALITY = 101;    
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String duration;

    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String startTime;

    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String endTime;
    
    @Persistent
    private boolean isPublic; 

    //used by maplestage channels, 9x9 channels, youtube special sorting channels
    //please not it is a string instead of digit, make 1 00000001, 8 digits total 
    @Persistent
    @Column(jdbcType="VARCHAR", length=8)
    private String seq;

    //used with seq
    @Persistent
    @Column(jdbcType="VARCHAR", length=8)    
    private String subSeq;
    
    @Persistent
    private Date createDate;
        
    @Persistent
    private Date updateDate;

    @Persistent
    private Date publishDate;
    
    public NnProgram(long channelId, String name, String intro, String imageUrl) {
        this.channelId = channelId;
        this.name = name;
        this.intro = intro;
        this.imageUrl = imageUrl;
        Date now = new Date();        
        this.createDate = now;
        this.updateDate = now;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        if (name != null)
            name = NnStringUtil.revertHtml(name);
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getIntro() {
        if (intro != null)
            intro = NnStringUtil.revertHtml(intro);
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {        
        this.status = status;
    }

    public String getStorageId() {
        return storageId;
    }

    //used in favorite program, to reference the real 9x9 program (maplestage, youtube channel do not apply here)
    /*
    public String getReferenceStorageId() {
        return this.getChannelId() + ";" + "00000000";
    }

    //compatibility with old scheme. 
    public String getReferenceStorageIdOldScheme() {
        return this.getChannelId() + ";" + "00000001";        
    }
    */
    
    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getAudioFileUrl() {
        return audioFileUrl;
    }

    public void setAudioFileUrl(String audioFileUrl) {
        this.audioFileUrl = audioFileUrl;
    }

    public String getComment() {
        if (comment != null)
            comment = NnStringUtil.revertHtml(comment);
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public short getContentType() {
        return contentType;
    }

    public void setContentType(short contentType) {
        this.contentType = contentType;
    }

    public String getSeq() {
        return seq;
    }
    
    public void setSeq(String seq) {
        this.seq = seq;
    }
    
    public void setSeq(int seq) {
        this.seq = String.format("%08d", seq);
    }

    public String getSubSeq() {
        return subSeq;
    }

    public void setSubSeq(String subSeq) {
        this.subSeq = subSeq;
    }

    public void setStartTime(int startTime) {
        this.startTime = String.format("%d", startTime);
    }
    
    public void setEndTime(int endTime) {
        this.endTime = String.format("%d", endTime);
    }
    
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public long getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(long episodeId) {
        this.episodeId = episodeId;
    }
    
}
