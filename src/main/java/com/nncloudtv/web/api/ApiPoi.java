package com.nncloudtv.web.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.Poi;
import com.nncloudtv.model.PoiCampaign;
import com.nncloudtv.model.PoiEvent;
import com.nncloudtv.model.PoiPoint;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnProgramManager;
import com.nncloudtv.service.NnUserManager;
import com.nncloudtv.service.PoiCampaignManager;
import com.nncloudtv.service.PoiEventManager;
import com.nncloudtv.service.PoiPointManager;
import com.nncloudtv.service.TagManager;

@Controller
@RequestMapping("api")
public class ApiPoi extends ApiGeneric {
    
    protected static Logger log = Logger.getLogger(ApiPoi.class.getName());
    
    NnUserManager userMngr;
    NnProgramManager programMngr;
    PoiCampaignManager campaignMngr;
    PoiPointManager pointMngr;
    NnChannelManager channelMngr;
    PoiEventManager eventMngr;
    
    @Autowired
    public ApiPoi(NnUserManager userMngr, NnProgramManager programMngr, PoiCampaignManager campaignMngr,
                    PoiPointManager pointMngr, NnChannelManager channelMngr, PoiEventManager eventMngr) {
        this.userMngr = userMngr;
        this.programMngr = programMngr;
        this.campaignMngr = campaignMngr;
        this.pointMngr = pointMngr;
        this.channelMngr = channelMngr;
        this.eventMngr = eventMngr;
    }
    
    @RequestMapping(value = "users/{userId}/poi_campaigns", method = RequestMethod.GET)
    public @ResponseBody
    List<PoiCampaign> userCampaigns(HttpServletRequest req,
            HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long userId = evaluateLong(userIdStr);
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso brand = new MsoManager().findOneByName(mso);
        NnUser user = userMngr.findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != user.getId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        List<PoiCampaign> results = campaignMngr.findByUserId(user.getId());
        if (results == null) {
            log.info(printExitState(now, req, "ok"));
            return new ArrayList<PoiCampaign>();
        }
        
        for (PoiCampaign result : results) {
            result.setName(NnStringUtil.revertHtml(result.getName()));
        }
        
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "users/{userId}/poi_campaigns", method = RequestMethod.POST)
    public @ResponseBody
    PoiCampaign userCampaignCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long userId = evaluateLong(userIdStr);
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso brand = new MsoManager().findOneByName(mso);
        NnUser user = userMngr.findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != user.getId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        PoiCampaign newCampaign = new PoiCampaign();
        newCampaign.setMsoId(user.getMsoId());
        newCampaign.setUserId(user.getId());
        newCampaign.setName(name);
        
        // startDate
        Long startDateLong = null;
        String startDateStr = req.getParameter("startDate");
        if (startDateStr != null) {
            
            startDateLong = evaluateLong(startDateStr);
            if (startDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        // endDate
        Long endDateLong = null;
        String endDateStr = req.getParameter("endDate");
        if (endDateStr != null) {
            
            endDateLong = evaluateLong(endDateStr);
            if (endDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        if (startDateStr != null && endDateStr != null) {
            if (endDateLong < startDateLong) { 
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            newCampaign.setStartDate(new Date(startDateLong));
            newCampaign.setEndDate(new Date(endDateLong));
        } else if (startDateStr == null && endDateStr == null) {
            newCampaign.setStartDate(null);
            newCampaign.setEndDate(null);
        } else { // should be pair
            badRequest(resp, INVALID_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        PoiCampaign savedCampaign = campaignMngr.save(newCampaign);
        savedCampaign.setName(NnStringUtil.revertHtml(savedCampaign.getName()));
        
        log.info(printExitState(now, req, "ok"));
        return savedCampaign;
    }
    
    @RequestMapping(value = "poi_campaigns/{poiCampaignId}", method = RequestMethod.GET)
    public @ResponseBody
    PoiCampaign campaign(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiCampaignId") String poiCampaignIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiCampaignId = evaluateLong(poiCampaignIdStr);
        if (poiCampaignId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiCampaign result = campaignMngr.findCampaignById(poiCampaignId);
        if (result == null) {
            notFound(resp, "Campaign Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != result.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        result.setName(NnStringUtil.revertHtml(result.getName()));
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "poi_campaigns/{poiCampaignId}", method = RequestMethod.PUT)
    public @ResponseBody
    PoiCampaign campaignUpdate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiCampaignId") String poiCampaignIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiCampaignId = evaluateLong(poiCampaignIdStr);
        if (poiCampaignId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiCampaign campaign = campaignMngr.findCampaignById(poiCampaignId);
        if (campaign == null) {
            notFound(resp, "Campaign Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != campaign.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null) {
            name = NnStringUtil.htmlSafeAndTruncated(name);
            campaign.setName(name);
        }
        
        // startDate
        Long startDateLong = null;
        String startDateStr = req.getParameter("startDate");
        if (startDateStr != null) {
            
            startDateLong = evaluateLong(startDateStr);
            if (startDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        // endDate
        Long endDateLong = null;
        String endDateStr = req.getParameter("endDate");
        if (endDateStr != null) {
            
            endDateLong = evaluateLong(endDateStr);
            if (endDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        if (startDateStr != null && endDateStr != null) {
            if (endDateLong < startDateLong) { 
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            campaign.setStartDate(new Date(startDateLong));
            campaign.setEndDate(new Date(endDateLong));
        } else if (startDateStr == null && endDateStr == null) {
            // skip
        } else { // should be pair
            badRequest(resp, INVALID_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        PoiCampaign savedCampaign = campaignMngr.save(campaign);
        
        savedCampaign.setName(NnStringUtil.revertHtml(savedCampaign.getName()));
        
        log.info(printExitState(now, req, "ok"));
        return savedCampaign;
    }
    
    @RequestMapping(value = "poi_campaigns/{poiCampaignId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String campaignDelete(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiCampaignId") String poiCampaignIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiCampaignId = evaluateLong(poiCampaignIdStr);
        if (poiCampaignId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiCampaign campaign = campaignMngr.findCampaignById(poiCampaignId);
        if (campaign == null) {
            notFound(resp, "Campaign Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != campaign.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        campaignMngr.delete(campaign);
        
        okResponse(resp);
        log.info(printExitState(now, req, "ok"));
        return null;
    }
    
    @RequestMapping(value = "poi_campaigns/{poiCampaignId}/pois", method = RequestMethod.GET)
    public @ResponseBody
    List<Poi> campaignPois(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiCampaignId") String poiCampaignIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiCampaignId = evaluateLong(poiCampaignIdStr);
        if (poiCampaignId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiCampaign campaign = campaignMngr.findCampaignById(poiCampaignId);
        if (campaign == null) {
            notFound(resp, "Campaign Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != campaign.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // poiPointId
        Long poiPointId = null;
        String poiPointIdStr = req.getParameter("poiPointId");
        if (poiPointIdStr != null) {
            poiPointId = evaluateLong(poiPointIdStr);
            if (poiPointId == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        List<Poi> results = null;
        if (poiPointId != null) {
            // find pois with point
            results = campaignMngr.findPoisByPointId(poiPointId);
        } else {
            // find pois with campaign
            results = campaignMngr.findPoisByCampaignId(campaign.getId());
        }
        
        if (results == null) {
            log.info(printExitState(now, req, "ok"));
            return new ArrayList<Poi>();
        }
        
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "poi_campaigns/{poiCampaignId}/pois", method = RequestMethod.POST)
    public @ResponseBody
    Poi campaignPoiCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiCampaignId") String poiCampaignIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiCampaignId = evaluateLong(poiCampaignIdStr);
        if (poiCampaignId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiCampaign campaign = campaignMngr.findCampaignById(poiCampaignId);
        if (campaign == null) {
            notFound(resp, "Campaign Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != campaign.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // pointId
        Long pointId = null;
        String pointIdStr = req.getParameter("pointId");
        if (pointIdStr != null) {
            pointId = evaluateLong(pointIdStr);
            if (pointId == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        } else {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        PoiPoint point = pointMngr.findById(pointId);
        if (point == null) {
            badRequest(resp, INVALID_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        // eventId
        Long eventId = null;
        String eventIdStr = req.getParameter("eventId");
        if (eventIdStr != null) {
            eventId = evaluateLong(eventIdStr);
            if (eventId == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        } else {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        PoiEvent event = eventMngr.findById(eventId);
        if (event == null) {
            badRequest(resp, INVALID_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        // create the poi
        Poi newPoi = new Poi();
        newPoi.setCampaignId(campaign.getId());
        newPoi.setPointId(point.getId());
        newPoi.setEventId(event.getId());
        
        // startDate
        String startDateStr = req.getParameter("startDate");
        if (startDateStr != null && startDateStr.length() > 0) {
            Long startDateLong = evaluateLong(startDateStr);
            if (startDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            
            newPoi.setStartDate(new Date(startDateLong));
        } else {
            newPoi.setStartDate(null);
        }
        
        // endDate
        String endDateStr = req.getParameter("endDate");
        if (endDateStr != null && endDateStr.length() > 0) {
            Long endDateLong = evaluateLong(endDateStr);
            if (endDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            
            newPoi.setEndDate(new Date(endDateLong));
        } else {
            newPoi.setEndDate(null);
        }
        
        // hoursOfWeek
        String hoursOfWeek = req.getParameter("hoursOfWeek");
        if (hoursOfWeek != null) {
            if (hoursOfWeek.matches("[01]{168}")) {
                // valid hoursOfWeek format
            } else {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            
            newPoi.setHoursOfWeek(hoursOfWeek);
        } else {
            hoursOfWeek = "";
            for (int i = 1; i<= 7; i++) { // maybe type 111... in the code, will execute faster
                hoursOfWeek = hoursOfWeek.concat("111111111111111111111111");
            }
            
            newPoi.setHoursOfWeek(hoursOfWeek);
        }
        
        Poi result = campaignMngr.save(newPoi);
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "pois/{poiId}", method = RequestMethod.GET)
    public @ResponseBody
    Poi poi(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiId") String poiIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiId = evaluateLong(poiIdStr);
        if (poiId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Poi result = campaignMngr.findPoiById(poiId);
        if (result == null) {
            notFound(resp, "Poi Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiCampaign campaign = campaignMngr.findCampaignById(result.getCampaignId());
        if (campaign == null) {
            // ownership crashed
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != campaign.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "pois/{poiId}", method = RequestMethod.PUT)
    public @ResponseBody
    Poi poiUpdate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiId") String poiIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiId = evaluateLong(poiIdStr);
        if (poiId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Poi poi = campaignMngr.findPoiById(poiId);
        if (poi == null) {
            notFound(resp, "Poi Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiCampaign campaign = campaignMngr.findCampaignById(poi.getCampaignId());
        if (campaign == null) {
            // ownership crashed
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != campaign.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // startDate
        String startDateStr = req.getParameter("startDate");
        if (startDateStr != null && startDateStr.length() > 0) {
            Long startDateLong = evaluateLong(startDateStr);
            if (startDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            
            poi.setStartDate(new Date(startDateLong));
        }
        
        // endDate
        String endDateStr = req.getParameter("endDate");
        if (endDateStr != null && endDateStr.length() > 0) {
            Long endDateLong = evaluateLong(endDateStr);
            if (endDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            
            poi.setEndDate(new Date(endDateLong));
        }
        
        // hoursOfWeek
        String hoursOfWeek = req.getParameter("hoursOfWeek");
        if (hoursOfWeek != null) {
            if (hoursOfWeek.matches("[01]{168}")) {
                // valid hoursOfWeek format
                poi.setHoursOfWeek(hoursOfWeek);
            } else {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        Poi result = campaignMngr.save(poi);
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "pois/{poiId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String poiDelete(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiId") String poiIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiId = evaluateLong(poiIdStr);
        if (poiId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Poi poi = campaignMngr.findPoiById(poiId);
        if (poi == null) {
            notFound(resp, "Poi Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiCampaign campaign = campaignMngr.findCampaignById(poi.getCampaignId());
        if (campaign == null) {
            // ownership crashed
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != campaign.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        campaignMngr.delete(poi);
        
        okResponse(resp);
        log.info(printExitState(now, req, "ok"));
        return null;
    }
    
    @RequestMapping(value = "programs/{programId}/poi_points", method = RequestMethod.GET)
    public @ResponseBody
    List<PoiPoint> programPoints(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("programId") String programIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long programId = evaluateLong(programIdStr);
        if (programId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        NnProgram program = programMngr.findById(programId);
        if (program == null) {
            notFound(resp, "Program Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        List<PoiPoint> results = pointMngr.findByProgram(program.getId());
        if (results == null) {
            log.info(printExitState(now, req, "ok"));
            return new ArrayList<PoiPoint>();
        }
        
        for (PoiPoint result : results) {
            result.setName(NnStringUtil.revertHtml(result.getName()));
        }
        
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "programs/{programId}/poi_points", method = RequestMethod.POST)
    public @ResponseBody
    PoiPoint programPointCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("programId") String programIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long programId = evaluateLong(programIdStr);
        if (programId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        NnProgram program = programMngr.findById(programId);
        if (program == null) {
            notFound(resp, "Program Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        NnChannel channel = channelMngr.findById(program.getChannelId());
        if (channel == null) {
            // ownership crashed, it is orphan object
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        if (verifiedUserId != channel.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // targetId
        Long targetId = program.getId();
        
        // targetType
        Short targetType = PoiPoint.TYPE_SUBEPISODE;
        
        // name
        String name = req.getParameter("name");
        if (name == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        // startTime & endTime
        Integer startTime = null;
        Integer endTime = null;
        String startTimeStr = req.getParameter("startTime");
        String endTimeStr = req.getParameter("endTime");
        if (startTimeStr == null || endTimeStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        } else {
            try {
                startTime = Integer.valueOf(startTimeStr);
                endTime = Integer.valueOf(endTimeStr);
            } catch (NumberFormatException e) {
            }
            if ((startTime == null) || (startTime < 0) || (endTime == null) || (endTime <= 0) || (endTime - startTime <= 0)) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        // collision check
        
        PoiPoint newPoint = new PoiPoint();
        newPoint.setTargetId(targetId);
        newPoint.setType(targetType);
        newPoint.setName(name);
        newPoint.setStartTime(startTime);
        newPoint.setEndTime(endTime);
        
        // tag
        String tagText = req.getParameter("tag");
        String tag = null;
        if (tagText != null) {
            tag = TagManager.processTagText(tagText);
        }
        newPoint.setTag(tag);
        
        // active, default : true
        Boolean active = true;
        String activeStr = req.getParameter("active");
        if (activeStr != null) {
            active = Boolean.valueOf(activeStr);
        }
        newPoint.setActive(active);
        
        PoiPoint result = pointMngr.create(newPoint);
        result.setName(NnStringUtil.revertHtml(result.getName()));
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "poi_points/{poiPointId}", method = RequestMethod.GET)
    public @ResponseBody
    PoiPoint point(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiPointId") String poiPointIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiPointId = evaluateLong(poiPointIdStr);
        if (poiPointId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiPoint result = pointMngr.findById(poiPointId);
        if (result == null) {
            notFound(resp, "PoiPoint Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        result.setName(NnStringUtil.revertHtml(result.getName()));
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "poi_points/{poiPointId}", method = RequestMethod.PUT)
    public @ResponseBody
    PoiPoint pointUpdate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiPointId") String poiPointIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiPointId = evaluateLong(poiPointIdStr);
        if (poiPointId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiPoint point = pointMngr.findById(poiPointId);
        if (point == null) {
            notFound(resp, "PoiPoint Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long ownerUserId = pointMngr.findOwner(point);
        if (ownerUserId == null) { // no one can access orphan object
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId.longValue() != ownerUserId.longValue()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null) {
            name = NnStringUtil.htmlSafeAndTruncated(name);
            point.setName(name);
        }
        
        if (point.getType() == PoiPoint.TYPE_SUBEPISODE) {
            
            // startTime
            Integer startTime = null;
            String startTimeStr = req.getParameter("startTime");
            if (startTimeStr != null) {
                try {
                    startTime = Integer.valueOf(startTimeStr);
                } catch (NumberFormatException e) {
                }
                if ((startTime == null) || (startTime < 0)) {
                    badRequest(resp, INVALID_PARAMETER);
                    log.info(printExitState(now, req, "400"));
                    return null;
                }
            } else {
                // origin setting
                startTime = point.getStartTimeInt();
            }
            
            // endTime
            Integer endTime = null;
            String endTimeStr = req.getParameter("endTime");
            if (endTimeStr != null) {
                try {
                    endTime = Integer.valueOf(endTimeStr);
                } catch (NumberFormatException e) {
                }
                if ((endTime == null) || (endTime <= 0)) {
                    badRequest(resp, INVALID_PARAMETER);
                    log.info(printExitState(now, req, "400"));
                    return null;
                }
            } else {
                // origin setting
                endTime = point.getEndTimeInt();
            }
        
            if (endTime - startTime <= 0) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            // collision check
            point.setStartTime(startTime);
            point.setEndTime(endTime);
            
        }
        
        // tag
        String tagText = req.getParameter("tag");
        String tag = null;
        if (tagText != null) {
            tag = TagManager.processTagText(tagText);
            point.setTag(tag);
        }
        
        // active
        Boolean active;
        String activeStr = req.getParameter("active");
        if (activeStr != null) {
            active = Boolean.valueOf(activeStr);
            point.setActive(active);
        }
        
        PoiPoint result = pointMngr.save(point);
        result.setName(NnStringUtil.revertHtml(result.getName()));
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "poi_points/{poiPointId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String pointDelete(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiPointId") String poiPointIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiPointId = evaluateLong(poiPointIdStr);
        if (poiPointId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiPoint point = pointMngr.findById(poiPointId);
        if (point == null) {
            notFound(resp, "PoiPoint Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long ownerUserId = pointMngr.findOwner(point);
        if (ownerUserId == null) { // no one can access orphan object
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId.longValue() != ownerUserId.longValue()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        pointMngr.delete(point); // TODO currently delete point will delete event and poi too, will modify if logic change.
        
        okResponse(resp);
        log.info(printExitState(now, req, "ok"));
        return null;
    }
    
    @RequestMapping(value = "users/{userId}/poi_events", method = RequestMethod.POST)
    public @ResponseBody
    PoiEvent eventCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long userId = evaluateLong(userIdStr);
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso brand = new MsoManager().findOneByName(mso);
        NnUser user = userMngr.findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != user.getId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        // type
        Short type = null;
        String typeStr = req.getParameter("type");
        if (typeStr != null) {
            try {
                type = Short.valueOf(typeStr);
            } catch (NumberFormatException e) {
            }
            if (type == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            if (eventMngr.isValidEventType(type) == false) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        } else {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        // context
        String context = req.getParameter("context");
        if (context == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        PoiEvent newEvent = new PoiEvent();
        newEvent.setUserId(user.getId());
        newEvent.setMsoId(user.getMsoId());
        newEvent.setName(name);
        newEvent.setType(type);
        newEvent.setContext(context);
        
        // notifyMsg
        if (newEvent.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
             newEvent.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            String notifyMsg = req.getParameter("notifyMsg");
            if (notifyMsg == null) {
                badRequest(resp, MISSING_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            notifyMsg = NnStringUtil.htmlSafeAndTruncated(notifyMsg);
            newEvent.setNotifyMsg(notifyMsg);
        }
        
        // notifyScheduler
        if (newEvent.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            String notifyScheduler = req.getParameter("notifyScheduler");
            if (notifyScheduler == null) {
                badRequest(resp, MISSING_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            String[] timestampList = notifyScheduler.split(",");
            Long timestamp = null;
            for (String timestampStr : timestampList) {
                
                timestamp = evaluateLong(timestampStr);
                if (timestamp == null) {
                    badRequest(resp, INVALID_PARAMETER);
                    log.info(printExitState(now, req, "400"));
                    return null;
                }
            }
            newEvent.setNotifyScheduler(notifyScheduler);
        }
        
        PoiEvent result = eventMngr.create(newEvent);
        
        result.setName(NnStringUtil.revertHtml(result.getName()));
        if (result.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
                result.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            result.setNotifyMsg(NnStringUtil.revertHtml(result.getNotifyMsg()));
        }
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "poi_events/{poiEventId}", method = RequestMethod.GET)
    public @ResponseBody
    PoiEvent event(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiEventId") String poiEventIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiEventId = evaluateLong(poiEventIdStr);
        if (poiEventId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiEvent result = eventMngr.findById(poiEventId);
        if (result == null) {
            notFound(resp, "PoiEvent Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != result.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        result.setName(NnStringUtil.revertHtml(result.getName()));
        if (result.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
                result.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            result.setNotifyMsg(NnStringUtil.revertHtml(result.getNotifyMsg()));
        }
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "poi_events/{poiEventId}", method = RequestMethod.PUT)
    public @ResponseBody
    PoiEvent eventUpdate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiEventId") String poiEventIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiEventId = evaluateLong(poiEventIdStr);
        if (poiEventId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiEvent event = eventMngr.findById(poiEventId);
        if (event == null) {
            notFound(resp, "PoiEvent Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != event.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null) {
            name = NnStringUtil.htmlSafeAndTruncated(name);
            event.setName(name);
        }
        
        Boolean shouldContainNotifyMsg = false; // TODO rewrite flag control
        Boolean shouldContainNotifyScheduler = false; // TODO rewrite flag control
        // type
        Short type = null;
        String typeStr = req.getParameter("type");
        if (typeStr != null) {
            try {
                type = Short.valueOf(typeStr);
            } catch (NumberFormatException e) {
            }
            if (type == null) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            if (eventMngr.isValidEventType(type) == false) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            
            Short originType = event.getType();
            if (originType == PoiEvent.TYPE_POPUP || originType == PoiEvent.TYPE_HYPERLINK ||
                 originType == PoiEvent.TYPE_POLL) {
                if (type == PoiEvent.TYPE_INSTANTNOTIFICATION || type == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
                    shouldContainNotifyMsg = true;
                }
            }
            if (originType == PoiEvent.TYPE_POPUP || originType == PoiEvent.TYPE_HYPERLINK ||
                    originType == PoiEvent.TYPE_POLL || originType == PoiEvent.TYPE_INSTANTNOTIFICATION) {
                   if (type == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
                       shouldContainNotifyScheduler = true;
                   }
            }
            
            event.setType(type);
        }
        
        // context
        String context = req.getParameter("context");
        if (context != null) {
            event.setContext(context);
        }
        
        // notifyMsg
        if (event.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
             event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            String notifyMsg = req.getParameter("notifyMsg");
            if (shouldContainNotifyMsg == true && notifyMsg == null) {
                badRequest(resp, MISSING_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            if (notifyMsg != null) {
                notifyMsg = NnStringUtil.htmlSafeAndTruncated(notifyMsg);
                event.setNotifyMsg(notifyMsg);
            }
        }
        
        // notifyScheduler
        if (event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            String notifyScheduler = req.getParameter("notifyScheduler");
            if (shouldContainNotifyScheduler == true && notifyScheduler == null) {
                badRequest(resp, MISSING_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            if (notifyScheduler != null) {
                String[] timestampList = notifyScheduler.split(",");
                Long timestamp = null;
                for (String timestampStr : timestampList) {
                    
                    timestamp = evaluateLong(timestampStr);
                    if (timestamp == null) {
                        badRequest(resp, INVALID_PARAMETER);
                        log.info(printExitState(now, req, "400"));
                        return null;
                    }
                }
                event.setNotifyScheduler(notifyScheduler);
            }
        }
        
        PoiEvent result = eventMngr.save(event);
        
        result.setName(NnStringUtil.revertHtml(result.getName()));
        if (result.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
                result.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            result.setNotifyMsg(NnStringUtil.revertHtml(result.getNotifyMsg()));
        }
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "poi_events/{poiEventId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String eventDelete(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiEventId") String poiEventIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long poiEventId = evaluateLong(poiEventIdStr);
        if (poiEventId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        PoiEvent event = eventMngr.findById(poiEventId);
        if (event == null) {
            notFound(resp, "PoiEvent Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != event.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        eventMngr.delete(event);
        
        okResponse(resp);
        log.info(printExitState(now, req, "ok"));
        return null;
    }
    
    @RequestMapping(value = "channels/{channelId}/poi_points", method = RequestMethod.GET)
    public @ResponseBody
    List<PoiPoint> channelPoints(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("channelId") String channelIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long channelId = evaluateLong(channelIdStr);
        if (channelId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        NnChannel channel = channelMngr.findById(channelId);
        if (channel == null) {
            notFound(resp, "Channel Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        List<PoiPoint> results = pointMngr.findByChannel(channel.getId());
        if (results == null) {
            log.info(printExitState(now, req, "ok"));
            return new ArrayList<PoiPoint>();
        }
        
        for (PoiPoint result : results) {
            result.setName(NnStringUtil.revertHtml(result.getName()));
        }
        
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "channels/{channelId}/poi_points", method = RequestMethod.POST)
    public @ResponseBody
    PoiPoint channelPointCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("channelId") String channelIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long channelId = evaluateLong(channelIdStr);
        if (channelId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        NnChannel channel = channelMngr.findById(channelId);
        if (channel == null) {
            notFound(resp, "Channel Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != channel.getUserId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // targetId
        Long targetId = channel.getId();
        
        // targetType
        Short targetType = PoiPoint.TYPE_CHANNEL;
        
        // name
        String name = req.getParameter("name");
        if (name == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        // collision check
        
        PoiPoint newPoint = new PoiPoint();
        newPoint.setTargetId(targetId);
        newPoint.setType(targetType);
        newPoint.setName(name);
        newPoint.setStartTime(0);
        newPoint.setEndTime(0);
        
        // tag
        String tagText = req.getParameter("tag");
        String tag = null;
        if (tagText != null) {
            tag = TagManager.processTagText(tagText);
        }
        newPoint.setTag(tag);
        
        // active, default : true
        Boolean active = true;
        String activeStr = req.getParameter("active");
        if (activeStr != null) {
            active = Boolean.valueOf(activeStr);
        }
        newPoint.setActive(active);
        
        PoiPoint result = pointMngr.create(newPoint);
        result.setName(NnStringUtil.revertHtml(result.getName()));
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
}
