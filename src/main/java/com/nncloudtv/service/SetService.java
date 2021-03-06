package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.web.json.cms.Set;

@Service
public class SetService {
    
    protected static final Logger log = Logger.getLogger(SetService.class.getName());
    
    private SysTagManager sysTagMngr;
    private SysTagDisplayManager sysTagDisplayMngr;
    private SysTagMapManager sysTagMapMngr;
    private ContainerService containerService;
    
    @Autowired
    public SetService(SysTagManager sysTagMngr, SysTagDisplayManager sysTagDisplayMngr,
                        SysTagMapManager sysTagMapMngr, ContainerService containerService) {
        this.sysTagMngr = sysTagMngr;
        this.sysTagDisplayMngr = sysTagDisplayMngr;
        this.sysTagMapMngr = sysTagMapMngr;
        this.containerService = containerService;
    }
    
    /** build Set from SysTag and SysTagDisplay */
    public Set composeSet(SysTag set, SysTagDisplay setMeta) {
        
        Set setResp = new Set();
        setResp.setId(set.getId());
        setResp.setMsoId(set.getMsoId());
        setResp.setDisplayId(setMeta.getId());
        setResp.setChannelCnt(setMeta.getCntChannel());
        setResp.setLang(setMeta.getLang());
        setResp.setSeq(set.getSeq());
        setResp.setTag(setMeta.getPopularTag());
        setResp.setName(setMeta.getName());
        setResp.setSortingType(set.getSorting());
        
        return setResp;
    }
    
    public static Set normalize(Set set) {
        
        set.setName(NnStringUtil.revertHtml(set.getName()));
        
        return set;
    }
    
    /** find Sets that owned by Mso with specify display language
     *  @param msoId required, result Sets that belong to this specified Mso
     *  @param lang optional, result Sets has specified display language
     *  @return list of Sets */
    public List<Set> findByMsoIdAndLang(Long msoId, String lang) {
        
        List<Set> results = new ArrayList<Set>();
        Set result = null;
        
        if (msoId == null) {
            return new ArrayList<Set>();
        }
        
        //List<SysTag> results = dao.findByMsoIdAndType(msoId, SysTag.TYPE_SET);
        List<SysTag> sets = sysTagMngr.findByMsoIdAndType(msoId, SysTag.TYPE_SET);
        if (sets == null || sets.size() == 0) {
            return new ArrayList<Set>();
        }
        
        SysTagDisplay setMeta = null;
        for (SysTag set : sets) {
            
            if (lang != null) {
                setMeta = sysTagDisplayMngr.findBySysTagIdAndLang(set.getId(), lang);
            } else {
                setMeta = sysTagDisplayMngr.findBySysTagId(set.getId());
            }
            
            if (setMeta != null) {
                result = composeSet(set, setMeta);
                results.add(result);
            } else {
                if (lang == null) {
                    log.warning("invalid structure : SysTag's Id=" + set.getId() + " exist but not found any of SysTagDisPlay");
                } else {
                    log.info("SysTag's Id=" + set.getId() + " exist but not found match SysTagDisPlay for lang=" + lang);
                }
            }
        }
        
        return results;
    }
    
    /** find Sets that owned by Mso
     *  @param msoId required, result Sets that belong to this specified Mso
     *  @return list of Sets */
    public List<Set> findByMsoId(Long msoId) {
        
        if (msoId == null) {
            return new ArrayList<Set>();
        }
        
        return findByMsoIdAndLang(msoId, null);
    }
    
    /** find Set by SysTag's Id
     *  @param setId required, SysTag's ID with type = Set
     *  @return object Set or null if not exist */
    public Set findById(Long setId) {
        
        if (setId == null) {
            return null;
        }
        
        SysTag set = sysTagMngr.findById(setId);
        if (set == null || set.getType() != SysTag.TYPE_SET) {
            return null;
        }
        
        SysTagDisplay setMeta = sysTagDisplayMngr.findBySysTagId(set.getId());
        if (setMeta == null) {
            log.warning("invalid structure : SysTag's Id=" + set.getId() + " exist but not found any of SysTagDisPlay");
            return null;
        }
        
        return composeSet(set, setMeta);
    }
    
    /** Get Channels from Set ordered by Seq, the Channels populate additional information (TimeStart, TimeEnd, Seq, AlwaysOnTop)
     *    retrieve from SysTagMap.
     *  @param setId required, Set ID
     *  @return list of Channels */
    public List<NnChannel> getChannelsOrderBySeq(Long setId) {
        
        if (setId == null) {
            return new ArrayList<NnChannel>();
        }
        
        List<NnChannel> results = containerService.getChannelsOrderBySeq(setId);
        if (results == null) {
            return new ArrayList<NnChannel>();
        }
        
        return results;
    }
    
    /**
     * Get Channels from Set ordered by UpdateTime, Channel with AlwaysOnTop set to True will put in the head of results,
     *   the Channels populate additional information (TimeStart, TimeEnd, Seq, AlwaysOnTop) retrieve from SysTagMap.
     * @param setId required, Set ID
     * @return list of Channels */
    public List<NnChannel> getChannelsOrderByUpdateTime(Long setId) {
        
        if (setId == null) {
            return new ArrayList<NnChannel>();
        }
        
        List<NnChannel> results = containerService.getChannelsOrderByUpdateTime(setId);
        if (results == null) {
            return new ArrayList<NnChannel>();
        }
        
        return results;
    }
    
    /** check if input Channel's IDs represent all Channels in the Set
     *  @param setId required, SysTag's ID with type = Set
     *  @param channelIds required, Channel's IDs to be tested
     *  @return true if full match, false for not */
    public boolean isContainAllChannels(Long setId, List<Long> channelIds) {
        
        if (setId == null || channelIds == null) {
            return false;
        }
        
        // it must same as setChannels's result
        List<SysTagMap> setChannels = sysTagMapMngr.findBySysTagId(setId);
        if (setChannels == null) {
            if (channelIds.size() == 0) {
                return true;
            } else {
                return false;
            }
        }
        
        int index;
        for (SysTagMap channel : setChannels) {
            index = channelIds.indexOf(channel.getChannelId());
            if (index > -1) {
                // pass
            } else {
                // input missing this Channel ID 
                return false;
            }
        }
        
        if (setChannels.size() != channelIds.size()) {
            // input contain duplicate or other Channel Id
            return false;
        }
        
        return true;
    }
    
    /**
     * Add Channel to Set with additional setting.
     * @param setId required, Set ID
     * @param channelId required, Channel ID
     * @param timeStart optional, set a period start that Channel appear in the Set
     * @param timeEnd optional, set a period end that Channel appear in the Set
     * @param alwaysOnTop optional, indicate the Channel is set on top in Set or not
     */
    public void addChannelToSet(Long setId, Long channelId, Short timeStart, Short timeEnd, Boolean alwaysOnTop) {
        
        if (setId == null || channelId == null) {
            return ;
        }
        
        containerService.addChannel(setId, channelId, timeStart, timeEnd, alwaysOnTop, null);
    }
    
    public Set create(Set set) {
        
        SysTag newSet = new SysTag();
        newSet.setType(SysTag.TYPE_SET);
        newSet.setMsoId(set.getMsoId());
        newSet.setSeq(set.getSeq());
        newSet.setSorting(set.getSortingType());
        newSet.setFeatured(true);
        
        SysTagDisplay newSetMeta = new SysTagDisplay();
        newSetMeta.setCntChannel(0);
        newSetMeta.setLang(set.getLang());
        newSetMeta.setName(set.getName());
        newSetMeta.setPopularTag(set.getTag());
        
        SysTag savedSet = sysTagMngr.save(newSet);
        newSetMeta.setSystagId(savedSet.getId());
        SysTagDisplay savedSetMeta = sysTagDisplayMngr.save(newSetMeta);
        
        Set result = composeSet(savedSet, savedSetMeta);
        
        return result;
    }
    
    public void delete(Long setId) {
        
        if (setId == null) {
            return ;
        }
        
        SysTag set = sysTagMngr.findById(setId);
        if (set == null || set.getType() != SysTag.TYPE_SET) {
            return ;
        }
        
        set.setType(SysTag.TYPE_DESTROYED);
        sysTagMngr.save(set);
        //containerService.delete(setId);
    }
    
    public static boolean isValidSortingType(Short sortingType) {
        return SysTagManager.isValidSortingType(sortingType);
    }

}
