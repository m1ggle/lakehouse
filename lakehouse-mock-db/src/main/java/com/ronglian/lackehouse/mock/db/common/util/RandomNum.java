package com.ronglian.lackehouse.mock.db.common.util;

import java.util.Random;

public class RandomNum {
    public static final int getRandInt(int fromNum, int toNum){

        return   fromNum + new Random().nextInt(toNum - fromNum + 1 > 0 ? toNum - fromNum + 1 : 10) ;
    }



    public static final  int getRandInt(int fromNum,int toNum,Long seed){

        return   fromNum+ new Random(seed).nextInt(toNum-fromNum+1);
    }
}
