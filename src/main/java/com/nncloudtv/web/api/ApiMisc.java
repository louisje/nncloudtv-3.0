package com.nncloudtv.web.api;

import java.security.SignatureException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.AmazonLib;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.NnUserManager;

@Controller
@RequestMapping("api")
public class ApiMisc extends ApiGeneric {
	
	@RequestMapping(value = "s3/attributes", method = RequestMethod.GET)
	public @ResponseBody Map<String, String> s3Attributes(HttpServletRequest req, HttpServletResponse resp) {
		
		// TODO: verify authentication
		
		String prefix = req.getParameter("prefix");
		String type = req.getParameter("type");
		String acl = req.getParameter("acl");
		long size = 0;
		
		try {
			String sizeStr = req.getParameter("size");
			Long sizeL = Long.valueOf(sizeStr);
			size = sizeL.longValue();
		} catch (NumberFormatException e) {
		}
		
		Map<String, String> result = new TreeMap<String, String>();
		
		if (size == 0 || prefix == null || type == null || acl == null ||
		    (!type.equals("audio") && !type.equals("image")) ||
		    (!acl.equals("public-read"))) {
			
			badRequest(resp, BAD_PARAMETER);
			return result;
		} 
		
		String bucket = MsoConfigManager.getS3UploadBucket();
		String policy = AmazonLib.buildS3Policy(bucket, acl, type, size);
		String signature = "";
		try {
			signature = AmazonLib.calculateRFC2104HMAC(policy);
		} catch (SignatureException e) {
		}
		
		result.put("bucket", bucket);
		result.put("policy", policy);
		result.put("signature", signature);
		result.put("id", AmazonLib.AWS_ID);
		
		return result;
	}
	
	@RequestMapping(value = "login", method = RequestMethod.DELETE)
	public @ResponseBody String logout(HttpServletRequest req, HttpServletResponse resp) {
		
		CookieHelper.deleteCookie(resp, CookieHelper.USER);
		CookieHelper.deleteCookie(resp, CookieHelper.GUEST);
		
		return "OK";
	}
	
	@RequestMapping(value = "login", method = RequestMethod.POST)
	public @ResponseBody NnUser login(HttpServletRequest req, HttpServletResponse resp) {
		
		String token = req.getParameter("token");
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		
		NnUserManager userMngr = new NnUserManager();
		NnUser user = null;
		
		if (token != null) {
			
			log.info("token = " + token);
			
			user = userMngr.findByToken(token);
			
		} else if (email != null && password != null) {
			
			log.info("email = " + email + ", password = xxxxxx");
			
			user = userMngr.findAuthenticatedUser(email, password, req);
			if (user != null) {
				CookieHelper.setCookie(resp, CookieHelper.USER, user.getToken());
			}
			
		} else {
			badRequest(resp, MISSING_PARAMETER);
		}
		
		return user;
	}
	
	@RequestMapping("echo")
	public @ResponseBody Map<String, String> echo(HttpServletRequest req, HttpServletResponse resp) {
		
		log = Logger.getLogger(ApiMisc.class.getName());
		
		@SuppressWarnings("unchecked")
		Map<String, String[]> names = req.getParameterMap();
		Map<String, String> result = new TreeMap<String, String>();
		
		for (String name : names.keySet()) {
			
			String value = names.get(name)[0];
			log.info(name + " = " + value);
			result.put(name, value);
		}
		
		if (result.isEmpty()) {
			
			badRequest(resp, MISSING_PARAMETER);
			
			return null;
		}
		
		if(!req.getMethod().equalsIgnoreCase("GET"))
			resp.setStatus(201);
		
		return result;
	}
	
}