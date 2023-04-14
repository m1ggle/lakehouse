package com.ronglian.lackehouse.mock.db.controller;

import com.ronglian.lackehouse.mock.db.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class UerInfoController {
    @Autowired
    private UserInfoService userInfoService;


    public String userInfoTest(){
        userInfoService.genUserInfos(true);

        return null;
    }

}
