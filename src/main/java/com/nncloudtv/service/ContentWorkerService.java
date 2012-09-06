package com.nncloudtv.service;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.web.api.NnStatusCode;
import com.nncloudtv.web.json.transcodingservice.ContentWorker;
import com.nncloudtv.web.json.transcodingservice.PostResponse;

@Service
public class ContentWorkerService {
    protected static final Logger log = Logger.getLogger(ContentWorkerService.class.getName());
    private static int TASK_CHANNEL_LOGO_PROCESS = 0;
    private static int TASK_PROGRAM_LOGO_PROCESS = 1;
    private static int TASK_PROGRAM_VIDEO_PROCESS = 2;     
        
    public void submit(int task, ContentWorker content, HttpServletRequest req) {
        DepotService service = new DepotService();
        String[] transcodingEnv = service.getTranscodingEnv(req);
        String transcodingServer = transcodingEnv[0] + "contentworker.php" + "?task=" + task;
        String callbackUrl = transcodingEnv[1];
        String devel = transcodingEnv[2];        
        content.setCallback(callbackUrl);
        content.setErrorCode(String.valueOf(NnStatusCode.SUCCESS));
        log.info("submit to " + transcodingServer + "; callbackUrl is " + callbackUrl);
        log.info(content.toString());
        if (!devel.equals("1"))
            NnNetUtil.urlPostWithJson(transcodingServer, content);
    }
    
    public void channelLogoProcess(long channelId, String imageUrl, String prefix, HttpServletRequest req) {                        
        ContentWorker content = new ContentWorker(
                channelId, imageUrl, null, prefix, false);
        this.submit(TASK_CHANNEL_LOGO_PROCESS, content, req);
    }
    
     public void programLogoProcess(long programId, String imageUrl, String prefix, HttpServletRequest req) {
         ContentWorker content = new ContentWorker(programId, imageUrl, null, prefix, false);
         this.submit(TASK_PROGRAM_LOGO_PROCESS, content, req);
     }
    
     public void programVideoProcess(long programId, String videoUrl, String prefix, boolean autoGeneratedLogo, HttpServletRequest req) {
         ContentWorker content = new ContentWorker(programId, null, videoUrl, prefix, autoGeneratedLogo);
         this.submit(TASK_PROGRAM_VIDEO_PROCESS, content, req);
     }
     
     public PostResponse channelLogoUpdate(ContentWorker content) {
         if (!content.getErrorCode().equals(String.valueOf(NnStatusCode.SUCCESS))) {
             log.info("stop processing because of error");
             return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
         }
         
         NnChannelManager channelMngr = new NnChannelManager();
         NnChannel channel = channelMngr.findById(content.getId());         
         if (channel == null) 
             return new PostResponse(String.valueOf(NnStatusCode.CHANNEL_INVALID), "CHANNEL INVALID");
         
         channel.setImageUrl(content.getImageUrl());
         channelMngr.save(channel);
         return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");        
     }
          
     public PostResponse programLogoUpdate(ContentWorker content) {
         if (!content.getErrorCode().equals(String.valueOf(NnStatusCode.SUCCESS))) {
             log.info("stop processing because of error");
             return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
         }
         
         NnProgramManager programMngr = new NnProgramManager();
         NnProgram program = programMngr.findById(content.getId());         
         if (program == null) 
             return new PostResponse(String.valueOf(NnStatusCode.PROGRAM_INVALID), "PROGRAM INVALID");
         
         program.setImageUrl(content.getImageUrl());
         programMngr.save(program);
         return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
     }
     
     public PostResponse programVideoUpdate(ContentWorker content) {
         if (!content.getErrorCode().equals(String.valueOf(NnStatusCode.SUCCESS))) {
             log.info("stop processing because of error");
             return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
         }
         
         NnProgramManager programMngr = new NnProgramManager();
         NnProgram program = programMngr.findById(content.getId());
         if (program == null) 
             return new PostResponse(String.valueOf(NnStatusCode.PROGRAM_INVALID), "PROGRAM INVALID");
         
         program.setFileUrl(content.getVideoUrl());
         if (content.getImageUrl() != null) 
             program.setImageLargeUrl(content.getImageUrl());
         programMngr.save(program);
         return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
     }
     
}
