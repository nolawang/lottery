package com.wch.lottery.service.impl;

import java.util.*;

public class BallServiceImpl {

    public static void main(String[] args) {
        BallServiceImpl ballService = new BallServiceImpl();
        for(int i=0;i<5;i++){
            ballService.genBalls();
            System.out.println("\n");
        }

    }

    private int[][]hisRecords = new int[][]{
            {12,15,16,22,29,32},
            {10,14,17,22,26,27},
            {8,17,24,26,27,31},
            {5,9,14,20,24,30},
            {2,4,10,17,22,25},
            {1,3,11,12,19,26}};

    private int[] balls = new int[]
            {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,
                    18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};

    private Set<Integer> blueForbidden = new HashSet<Integer>(){};
    /**
     * 已经生成的红球号码
     */
    private Set<Integer> genRedBallSet = new HashSet<>();

    public int[] genBalls(){
        blueForbidden.add(5);
        blueForbidden.add(6);
        int redResults[] = new int[6];
        int blueResult = 0;
        //生成2个数
        int count = 0;
        Map<Integer,Integer> areaCountMap = new HashMap<>();
        int resultNum = 0;
        while(count < 6){
            final long l = System.currentTimeMillis();
            int i = (int)l%33;
//            Random rd = new Random(1000);
//            int i = rd.nextInt(33);
            int num = balls[i];
            int k = num/10;
            if(genRedBallSet.contains(i)){
                continue;
            }
            if(areaCountMap.get(k)!=null){
                if(areaCountMap.get(k)>=3){
                    continue;
                }
                areaCountMap.put(k,areaCountMap.get(k)+1);

            }else{
                areaCountMap.put(k,1);
            }
            genRedBallSet.add(i);
            count++;

            redResults[resultNum] = num;
            resultNum++;
        }
        Arrays.sort(redResults);

        if(!check1(redResults)){

        }

        for(int i=0;i<redResults.length;i++){
            System.out.print(redResults[i]);
            System.out.print(",");
        }
        while (true){
            if(blueResult == 0){
                Random r = new Random();
                blueResult = r.nextInt(17);
            }else{
                if(blueForbidden.contains(blueResult)){
                    continue;
                }
                break;
            }

        }
        System.out.print(blueResult);

        return null;
    }

    /**
     * 近3期相同号码不超过4个
     * @param ranBalls
     * @return
     */
    public boolean check1(int []ranBalls){
        int count = 0;
        Set<Integer> set = new HashSet<>();
        for(int i=0;i<ranBalls.length;i++){
            set.add(ranBalls[i]);
        }
        for(int i=0;i<hisRecords.length;i++){
            for(int j=0;j<hisRecords[i].length;j++){
                if(count>=4){
                    return false;
                }
                if(set.contains(hisRecords[i][j])){
                    count++;
                }
            }
        }
        return true;
    }

}
