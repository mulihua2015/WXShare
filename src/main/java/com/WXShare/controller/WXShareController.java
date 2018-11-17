package com.WXShare.controller;

import com.WXShare.service.IWXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WXShareController {

    @Autowired
    private IWXService iwxService;

    @RequestMapping(value="/getWxData")
    public Map<String,Object> getWxData(@RequestParam("url") String url){
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("resultCode", "0");
        result.put("resultMsg", "ok");
        Map<String,Object> data =iwxService.getTicketData(url);
        result.put("data", data);
        return result;
    }
}
