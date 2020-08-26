package com.wch.lottery.service.impl;

import com.wch.lottery.dao.mapper.AreaPredictionRecordMapper;
import com.wch.lottery.dao.mapper.IssueMapper;
import com.wch.lottery.dao.mapper.IssuePredictionRecordMapper;
import com.wch.lottery.dao.mapper.IssueRecordMapper;
import com.wch.lottery.exception.BusiException;
import com.wch.lottery.exception.ParamException;
import com.wch.lottery.model.AreaPredictionRecord;
import com.wch.lottery.model.Issue;
import com.wch.lottery.model.IssuePredictionRecord;
import com.wch.lottery.model.IssueRecord;
import com.wch.lottery.model.body.IssueBody;
import com.wch.lottery.model.dto.IssueRecordDTO;
import com.wch.lottery.service.IssueService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class IssueServiceImpl implements IssueService {

    @Autowired
    private IssueMapper issueMapper;

    @Autowired
    private IssueRecordMapper issueRecordMapper;

    @Autowired
    private IssuePredictionRecordMapper issuePredictionRecordMapper;

    @Autowired
    private AreaPredictionRecordMapper areaPredictionRecordMapper;

    /**
     * 最大遗漏值
     */
    public static final int MAX_MISS_VALUE = 10;

    /**
     * 最小遗漏值
     */
    public static final int MIN_ISSUE_VALUE = 5;

    /**
     * 最大热点球数量
     */
    public static final int MAX_ISSUE_HOT_NUM = 2;

    /**
     * 最小热点球数量
     */
    public static final int MIN_ISSUE_HOT_NUM = 1;

    private int[] redBallPool = new int[]{1,2,3,4,5,6,7,8,9,10,
    11,12,13,14,15,16,17,18,19,20,21,22,
    23,24,25,26,27,28,29,30,31,32,33};

    private int[] blueBallPool = new int[]{1,2,3,4,5,6,7,8,9,10,
    11,12,13,14,15,16};

    public static final int AREA0_SEED_NUM = 4;

    public static final int AREA1_SEED_NUM = 18;

    public static final int AREA2_SEED_NUM = 16;

    public static final int AREA3_SEED_NUM = 14;

    public static final int AREA4_SEED_NUM = 8;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public Long add(IssueBody issueBody) throws Exception{
        if(issueBody == null){
            throw new ParamException("issueBody为空");
        }
        int index = 0;
        for(List<Integer> r : issueBody.getRecords()){

            int issueNo = issueBody.getIssueNo();
            if(index>0){
                issueNo += index;
            }
            Issue issue = new Issue(issueNo,null, Strings.join(r,','));
            index++;
            issueMapper.insertSelective(issue);
            int j = 0;
            for(int n:r){
                boolean isRed = true;
                if(j == 6){
                    isRed = false;
                }
                IssueRecord issueRecord = new IssueRecord(issueNo,(byte)n,isRed);
                issueRecordMapper.insertSelective(issueRecord);
                j++;
            }
        }
        return null;
    }

    @Override
    public int update(IssueBody issueBody) throws Exception{
        return 0;
    }

    @Override
    public Issue get(int issueNo) throws Exception {
        if(issueNo == 0){
            return issueMapper.selectMaxIssue();
        }else {
            List<Issue> issueList = issueMapper.selectByCondition(new Issue(issueNo));
            if(CollectionUtils.isEmpty(issueList)){
                throw new ParamException(String.format("issueNo%v无效",issueNo));
            }
            return issueList.get(0);
        }
    }

    /**
     * 红球
     * 	1.近5期相同号码不超过3个（排除与近三期一样的结果）
     * 	2.5个连续的以上去除（排除）
     * 	3.同数字段不可以超过3个（排除）
     * 	4.热点号近10期出现不超过6次（排除热点号）
     * 	5.近5期出现过两次以上，属于热球需要增大概率（增大几率）池子数量+1
     * 	6.遗漏值在【0,10】池子里的数量+1
     * 	7.去除遗漏值最大的号码
     *
     * 篮球
     * 	1.遗漏值去除一个最大一个最小，正态分布，取随机值
     * 	2.遗漏值在【10,20】池子里的数量+1
     * 	3.遗漏值在（20,00】池子里的数量+2
     * @param issueNo 期号
     * @param groupNum 预测数量，如果传0默认是5条
     * @return
     * @throws Exception
     */
    @Override
    public int[][] predict(int issueNo, int groupNum,boolean isAccurate,int[] cases) throws Exception {
        Issue issue = get(issueNo);

        int newIssueNo = issue.getIssueNo()+1;
        int newRedPool[] = getRedPool(newIssueNo);
        Set<Integer> blueForbiddenSet = new HashSet<>();
        int newBluePool[] = getBluePool(newIssueNo,blueForbiddenSet);


        //开始出球
        IssueRecord redMaxMissRecord = issueRecordMapper.selectMaxMissRecord(true);
        IssueRecord blueMaxMissRecord = issueRecordMapper.selectMaxMissRecord(false);
        List<IssueRecord> issueBlueRecordList = issueRecordMapper.selectByCondition(new IssueRecord(issueNo,false));
        IssueRecord blueMinMissRecord = issueBlueRecordList.get(0);
        Set<Integer> redBaseForbiddenSet = new HashSet<>();
        redBaseForbiddenSet.add((int)redMaxMissRecord.getNum());
//        List<IssueRecord> issueRecordList = listByIssueNoAndCount(issueNo- 15,3,false);
        List<IssueRecord> blue15List = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(issueNo-15,issueNo,3,null,null,false));
        List<IssueRecord> blue25List = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(issueNo-25,issueNo,1,null,null,false));
        Set<Integer> blueSet = new HashSet<>();
        if(!CollectionUtils.isEmpty(blue15List)){
            blue15List.forEach(obj->{
                blueForbiddenSet.add((int)obj.getNum());
            });
        }
        if(!CollectionUtils.isEmpty(blue25List)){
            blue25List.forEach(obj->{
                blueSet.add((int)obj.getNum());
            });
        }
        for(int blue:blueBallPool){
            if(!blueSet.contains(blue)){
                blueForbiddenSet.add(blue);
            }
        }
        blueForbiddenSet.add((int)blueMaxMissRecord.getNum());
//        blueForbiddenSet.add((int)blueMinMissRecord.getNum());

        List<int[]> redResultList = new ArrayList<>();
        while(true){
            if(redResultList.size()>=groupNum){
                break;
            }
            int[] results = new int[7];
            int[] redResults = new int[6];
            Set<Integer> redForbiddenSet = new HashSet<>();
            redForbiddenSet.addAll(redBaseForbiddenSet);

            if(isAccurate){
                redResults = genRedBallByCase(issueNo,cases,redForbiddenSet);
            }else {
                redResults = genRedBallByProb(newIssueNo,redForbiddenSet,newRedPool);
            }
            Arrays.sort(redResults);
            if(checkRedSame(redResults,issue.getIssueNo(),true)
                    && checkRedContinuous(redResults)
                    && checkAreaCount(redResults)){
                results = Arrays.copyOf(redResults,7);
                Set<Integer> newBlueForbiddenSet = new HashSet<>();
                newBlueForbiddenSet.addAll(blueForbiddenSet);
                while (true){
                    int blueResult = genBlueBallsByTrend(issueNo,newBluePool,newBlueForbiddenSet);
                    if(checkBlue(blueResult)){
                        results[6] = blueResult;
                        break;
                    }
                }
                redResultList.add(results);
            }
        }


        for(int[] r: redResultList){
            for(int i=0;i<r.length;i++){
                boolean red = true;
                if(i==r.length-1){
                    red = false;
                }
                IssuePredictionRecord issuePredictionRecord = new IssuePredictionRecord(newIssueNo,(byte)r[i],red,null);
                issuePredictionRecordMapper.insertSelective(issuePredictionRecord);
            }
        }
        //随机组生成
        Set<Integer> set = new HashSet<>();
        Random random = new Random();
        int []randomRecord = new int[6];
        for(int i=0;i<redResultList.get(0).length-1;i++){

            int num = redResultList.get(0)[i];
            while (true){
                int temp = num;
                int rNum = random.nextInt(3);
                int isPlus = random.nextInt(2);
                if(isPlus == 1){
                    temp = num + rNum;
                }else {
                    if(num - rNum > 0){
                        temp = num - rNum;
                    }
                }
                if(set.contains(temp)){
                   continue;
                }else {
                    num = temp;
                    set.add(num);
                    break;
                }
            }

            randomRecord[i] = num;
        }
        Arrays.sort(randomRecord);
        randomRecord = Arrays.copyOf(randomRecord,7);
        randomRecord[6] = redResultList.get(0)[6];
        for(int i=0;i<randomRecord.length;i++){
            boolean red = true;
            if(i==redResultList.get(0).length-1){
                red = false;
            }
            IssuePredictionRecord issuePredictionRecord = new IssuePredictionRecord(newIssueNo,(byte)randomRecord[i],red,null);
            issuePredictionRecordMapper.insertSelective(issuePredictionRecord);
        }
        return redResultList.toArray(new int[0][]);
//        return new int[0][];
    }


    @Override
    public int[][] predict(int count) throws Exception {
        return new int[0][];
    }


    /**
     * 区域预测
     * @param issueNo
     * @param count
     * @return
     * @throws Exception
     */
    @Override
    public int[][] areaPredict(int issueNo, int count,boolean isAccurate,int cases[]) throws Exception {
        int [][]result = new int[][]{};

        Issue issue = get(issueNo);
        int newIssueNo = issue.getIssueNo()+1;
        List<AreaPredictionRecord> areaPredictionRecordList = areaPredictionRecordMapper.selectByCondition(new AreaPredictionRecord(newIssueNo));
        if(CollectionUtils.isEmpty(areaPredictionRecordList)){
           //获取预测号码组
            List<IssuePredictionRecord> issuePredictionRecordList = issuePredictionRecordMapper.selectByCondition(new IssuePredictionRecord(newIssueNo));
            int[][] predictResult = new int[][]{};
            List<int[]> predictResultList = new ArrayList<>();
            if(CollectionUtils.isEmpty(issuePredictionRecordList)){
                predictResult = predict(issueNo,count,isAccurate,cases);
            }else{
                for(int i=0;i<issuePredictionRecordList.size();i=i+7){
                    int[] predictionGroup = new int[]{issuePredictionRecordList.get(i).getNum(),
                            issuePredictionRecordList.get(i+1).getNum(),issuePredictionRecordList.get(i+2).getNum(),
                            issuePredictionRecordList.get(i+3).getNum(),issuePredictionRecordList.get(i+4).getNum(),
                            issuePredictionRecordList.get(i+5).getNum(),issuePredictionRecordList.get(i+6).getNum()};
                    predictResultList.add(predictionGroup);
                }
                predictResult = predictResultList.toArray(new int[][]{});
            }
//            String[] issueStrResult = issue.getResult().split(",");
//            Set<Integer> redBallResultSet = new HashSet<>();
//            Set<Integer> blueBallResultSet = new HashSet<>();
//            int[] issueResult = new int[7];
//            for (int i=0;i<issueStrResult.length;i++){
//                int num = Integer.valueOf(issueStrResult[i]);
//                issueResult[i] = num;
//                if(i == 6){
//                    blueBallResultSet.add(num);
//                }else {
//                    redBallResultSet.add(num);
//                }
//            }


            //case 1  生成5注
            //区间1/2/3各取一个 剩下按照概率取
            for(int i=0;i<5;i++){
                int[] redBalls = genAreaRedBalls(predictResult,1);
                System.out.println(Arrays.toString(redBalls));
                for(int r:redBalls){
                    areaPredictionRecordMapper.insertSelective(new AreaPredictionRecord(newIssueNo,(byte)r,true));
                }
            }

            //case 2  生成5注
            //全部按照概率取
            for(int i=0;i<5;i++){
                int[] redBalls = genAreaRedBalls(predictResult,2);
                System.out.println(Arrays.toString(redBalls));
            }
        }else{

        }
        return new int[0][];
    }

    /**
     * 按照区域生成红球
     * @param predictionResults
     * @return
     */
    public int[] genAreaRedBalls(int[][]predictionResults,int cas){
        Map<Integer,Integer> resultMap = new HashMap<>();
        Map<Integer,List<Integer>> countMap = new HashMap<>();
        for(int[] r: predictionResults){
            for(int i=0;i<r.length;i++){
                if(i == r.length-1){
                    continue;
                }
                if(resultMap.get(r[i])!=null){
                    resultMap.put(r[i],resultMap.get(r[i])+1);
                }else{
                    resultMap.put(r[i],1);
                }
            }
        }
        resultMap.forEach((k,v)->{
            if(countMap.get(v)!=null && countMap.get(v).size()>0){
                countMap.get(v).add(k);
            }else{
                List<Integer> list = new ArrayList<>();
                list.add(k);
                countMap.put(v,list);
            }
        });
        Set<Integer> ballSet = new HashSet<>();
        Random random = new Random();
        int[] result = new int[6];
        int ind = 0;
        if(cas == 1){
            result[0] = countMap.get(1).get(random.nextInt(countMap.get(1).size()));
            result[1] = countMap.get(2).get(random.nextInt(countMap.get(2).size()));
            result[2] = countMap.get(3).get(random.nextInt(countMap.get(3).size()));
            ballSet.add(result[0]);
            ballSet.add(result[1]);
            ballSet.add(result[2]);
            ind = 3;
        }
        int[] redPool = getAreaRedPool(countMap,ballSet);
        for(int i=ind;i<6;i++){
            while (true){
                int num = redPool[random.nextInt(redPool.length)];
                if(!ballSet.contains(num)){
                    result[i] = num;
                    ballSet.add(num);
                    break;
                }
            }
        }
        Arrays.sort(result);
        return result;
    }

    private int[] getHotRedBalls(int[] hotRedPools,int size,Set<Integer> redForbiddenSet){
        int[]redResults = new int[size];
        int index = 0;
        Random random = new Random();
        while (true){
            if(index>size-1){
                break;
            }
            int hotNum = hotRedPools[random.nextInt(hotRedPools.length)];
            if(!redForbiddenSet.contains(hotNum)){
                redResults[index] = hotNum;
                redForbiddenSet.add(hotNum);
                index++;
            }
        }
        return redResults;
    }

    public int[] getAreaRedPool(Map<Integer,List<Integer>> countMap,Set<Integer> ballSet){
        List<Integer> resultList = new ArrayList<>();
        Set<Integer> total1234Set = new HashSet<>();
        List<Integer> count0List = new ArrayList<>();
        for(Integer count:countMap.keySet()){
            if(count == 1 || count == 2 || count == 3 || count == 4){
                countMap.get(count).forEach(obj->{

                    total1234Set.add(obj);
                });
            }
        }
        for(int r : redBallPool){
            if(!total1234Set.contains(r)){
                count0List.add(r);
            }
        }

        for(int count = 0;count<5;count++){
            int size = 0;
            List<Integer> countList = new ArrayList<>();
            switch (count){
                case 1:
                    size = AREA1_SEED_NUM;
                    if(!ballSet.isEmpty()){
                        size = AREA1_SEED_NUM - 10;
                    }
                    countList = countMap.get(1);
                    break;
                case 2:
                    size = AREA2_SEED_NUM;
                    if(!ballSet.isEmpty()){
                        size = AREA2_SEED_NUM - 10;
                    }
                    countList = countMap.get(2);
                    break;
                case 3:
                    size = AREA3_SEED_NUM;
                    if(!ballSet.isEmpty()){
                        size = AREA3_SEED_NUM - 10;
                    }
                    countList = countMap.get(3);
                    break;
                case 4:
                    size = AREA4_SEED_NUM;
                    countList = countMap.get(4);
                    break;
                default:
                    size = AREA0_SEED_NUM;
                    countList = count0List;
            }

            for(int i=0;i<countList.size();i++){
                if(ballSet.contains(countList.get(i))){
                    continue;
                }
                for(int j=0;j<size;j++){
                    resultList.add(countList.get(i));
                }
            }
        }
        int[]result = new int[resultList.size()];
        for(int i=0;i<resultList.size();i++){
            result[i] = resultList.get(i);
        }
        return result;
    }

    private List<IssueRecord> listByIssueNoAndCount(int issueNo, int count, boolean red){
        return issueRecordMapper.selectNumByIssueNoAndCount(issueNo,count,red);
    }


    @Override
    public List<IssueRecord> list(IssueRecord issueRecord) throws Exception {
        return issueRecordMapper.selectByCondition(issueRecord);
    }

    /**
     * 生成红球池
     * @param newIssueNo
     * @return
     * @throws Exception
     */
    private int[]getRedPool(int newIssueNo)throws Exception{
        int[] newPool = Arrays.copyOf(redBallPool, redBallPool.length);

        //TODO 近5期取3-4个
        //遗漏值10以内数量+1 不包含近5期
//        List<IssueRecord> issueRecordList5 = listByIssueNoAndCount(newIssueNo- MIN_ISSUE_VALUE,MAX_ISSUE_HOT_NUM,true);
//        if(CollectionUtils.isEmpty(issueRecordList5)){
//            throw new BusiException("listByIssueNoAndCount 数量为空");
//        }
//        //近5期
//        int[]temp = Arrays.copyOf(newPool,newPool.length+issueRecordList5.size());
//        for(int i=0;i<issueRecordList5.size();i++){
//            temp[i+newPool.length] = issueRecordList5.get(i).getNum();
//        }
//        newPool = temp;

        //近15期
        List<IssueRecord> issueRecordList15 = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(newIssueNo-1-15,newIssueNo-1,1,null,null,true));
        //listByIssueNoAndCount(newIssueNo- MAX_MISS_VALUE,MIN_ISSUE_HOT_NUM,true);
        if(CollectionUtils.isEmpty(issueRecordList15)){
            throw new BusiException("listByIssueNoAndCount 数量为空");
        }
        int[]temp = Arrays.copyOf(newPool,newPool.length+issueRecordList15.size());
        for(int i=0;i<issueRecordList15.size();i++){
            temp[i+newPool.length] = issueRecordList15.get(i).getNum();
        }
        newPool = temp;
        return newPool;
    }

    private int[] getHotRedPool(int newIssueNo,int interval)throws Exception{
//        List<IssueRecord> issueRecordList5 = listByIssueNoAndCount(newIssueNo- MIN_ISSUE_VALUE,MIN_ISSUE_HOT_NUM,true);
        List<IssueRecord> issueRecordList5 = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(newIssueNo-1-interval,newIssueNo-1,1,null,null,true));
        if(CollectionUtils.isEmpty(issueRecordList5)){
            throw new BusiException("listByIssueNoAndCount 数量为空");
        }
        int[] result = new int[issueRecordList5.size()];
        int ind = 0;
        for(IssueRecord issueRecord : issueRecordList5){
            result[ind] = issueRecord.getNum();
            ind++;
        }

        return result;
    }

    private void randomRemoveHot(int hotPool[], int removeSize, Set<Integer> forbiddenSet){
        Random random = new Random();
        int index = 0;
        while (true){
            if(index>removeSize-1){
                break;
            }
            int num = hotPool[random.nextInt(hotPool.length)];
            if(!forbiddenSet.contains(num)){
                forbiddenSet.add(num);
                index++;
            }
        }
    }

    /**
     * 生成篮球池
     * @param newIssueNo
     * @return
     * @throws Exception
     */
    private int[] getBluePool(int newIssueNo,Set<Integer> forbiddenSet)throws Exception{
        int[] newPool = Arrays.copyOf(blueBallPool, blueBallPool.length);
        List<IssueRecord> issueRecordList = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(newIssueNo-15,newIssueNo-1,MIN_ISSUE_HOT_NUM,2,null,false));
//                List<IssueRecord> issueRecordList = listByIssueNoAndCount(newIssueNo- 15,MIN_ISSUE_HOT_NUM,false);
        if(CollectionUtils.isEmpty(issueRecordList)){
            throw new BusiException("listByIssueNoAndCount 返回空");
        }
//        Random random = new Random();
//        forbiddenSet.add((int)issueRecordList.get(random.nextInt(issueRecordList.size())).getNum());
        int[]temp = Arrays.copyOf(newPool,newPool.length+issueRecordList.size()*2);
        for(int i=0;i<issueRecordList.size();i++){
            temp[i+newPool.length] = issueRecordList.get(i).getNum();
            temp[i+newPool.length+issueRecordList.size()] = issueRecordList.get(i).getNum();
        }
        newPool = temp;
        return newPool;
    }

    /**
     * 近5期相同号码不超过3个
     * @param redResults
     * @return
     */
    public boolean checkRedSame(int []redResults, int issueNo, boolean red)throws Exception{
        List<IssueRecord> issueRecordList = issueRecordMapper.selectByRange(issueNo-5,true);
//        List<IssueRecord> issueRecordList = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(issueNo-5,issueNo,MIN_ISSUE_HOT_NUM,null,null,true));
        if(CollectionUtils.isEmpty(issueRecordList)){
            throw new BusiException("selectByRange数据为空");
        }
        Map<Integer,List<Byte>> issueRecordMap = new HashMap<>();
        issueRecordList.forEach(obj->{
            if(issueRecordMap.get(obj.getIssueNo()) != null){
                issueRecordMap.get(obj.getIssueNo()).add(obj.getNum());
            }else{
                List<Byte> list = new ArrayList<>();
                list.add(obj.getNum());
                issueRecordMap.put(obj.getIssueNo(),list);
            }

        });

        Set<Integer> set = new HashSet<>();
        for(int i=0;i<redResults.length;i++){
            set.add(redResults[i]);
        }
        for(int issueNoKey:issueRecordMap.keySet()){
            int count = 0;
            for(int i=0;i<issueRecordMap.get(issueNoKey).size();i++){

                if(count>=4){
                    return false;
                }
                if(set.contains((int)issueRecordMap.get(issueNoKey).get(i))){
                    count++;
                }
            }
        }
        return true;
    }

    /**
     * 检查连续红球
     * @param redResults
     * @return
     */
    private boolean checkRedContinuous(int []redResults){
        int count = 1;
        for(int i=0;i<redResults.length-1;i++){
            //检查超过连续3个以上的红球
            if(count>3){
                return false;
            }
            if(redResults[i] + 1 == redResults[i+1]){
                count++;
            }else{
                count = 1;
            }
        }
        return true;
    }

    private boolean checkAreaCount(int[]redResults){
        Map<Integer,Integer> countMap = new HashMap<>();
        for(int r:redResults){
            int po = r/10;
            if(countMap.get(po)==null){
                countMap.put(po,1);
            }else{
                countMap.put(po,countMap.get(po)+1);
                if(po==3){
                    if(countMap.get(po)>2){
                        return false;
                    }
                }else{
                    if(countMap.get(po)>3){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int[] genRedBalls(int[] newPool,Set<Integer> redForbiddenSet,int size){
        Set<Integer> redSet = new HashSet<>();

        int redResults[] = new int[size];
        //生成2个数
        int genCount = 0;
        Map<Integer,Integer> areaCountMap = new HashMap<>();
        int resultNum = 0;
        while(genCount < size){
            Random random = new Random();
            final int i = random.nextInt(newPool.length);
//            int i = (int)l%newPool.length;
            int num = newPool[i];
            int k = num/10;
            if(redForbiddenSet.contains(num)){
                continue;
            }
            if(redSet.contains(num)){
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
            redSet.add(num);
            genCount++;

            redResults[resultNum] = num;
            resultNum++;
        }
        Arrays.sort(redResults);
        return redResults;
    }

    /**
     * 普通随机生成
     * @param newPool
     * @param blueForbiddenSet
     * @return
     */
    private int genBlueBalls(int []newPool,Set<Integer> blueForbiddenSet){
        int result = 0;
        while (true){
            Random random = new Random();
            final int l = random.nextInt(newPool.length);
            final int r = random.nextInt(newPool.length-16);
            if(blueForbiddenSet.contains(newPool[r+16])){
               continue;
            }
            blueForbiddenSet.add(newPool[r+16]);
            result = newPool[l];
            if(blueForbiddenSet.contains(result)){
                continue;
            }
            break;
        }
        return result;
    }

    /**
     * 按照趋势规律生成
     * @param newPool
     * @param blueForbiddenSet
     * @return
     */
    private int genBlueBallsByTrend(int issueNo,int []newPool,Set<Integer> blueForbiddenSet){
        int result = 0;
        int[] bluePool = new int[]{};
        Random random = new Random();
        while (true){
            List<IssueRecord> recordList = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(issueNo-1,issueNo,1,null,null,false));
            if(isBlueMiddle(recordList.get(0).getNum()) && isBlueMiddle(recordList.get(1).getNum())){
                issueNo--;
                continue;
            }
            boolean isBigTrend = false;
            if(recordList.get(1).getNum()>recordList.get(0).getNum()){
                if(isBlueBig(recordList.get(1).getNum())){
                    isBigTrend = false;
                }else{
                    isBigTrend = true;
                }
            }else if(recordList.get(1).getNum().equals(recordList.get(0).getNum())){
                if(isBlueBig(recordList.get(1).getNum())){
                    isBigTrend = false;
                }else {
                    isBigTrend = true;
                }
            }else {
                if(isBlueBig(recordList.get(1).getNum())){
                    isBigTrend = false;
                }else if(isBlueMiddle(recordList.get(1).getNum())){
                    isBigTrend = false;
                }else {
                    isBigTrend = true;
                }
            }
            if(!isBigTrend){
                bluePool = new int[]{1,2,3,4,5,6,7};
            }else{
                bluePool = new int[]{8,9,10,11,12,13,14,15,16};
            }
            while (true){

                final int l = random.nextInt(bluePool.length);
                if(!blueForbiddenSet.contains(bluePool[l])){
                    result = bluePool[l];
                    break;
                }
            }
            break;
        }
        return result;
    }

    private boolean isBlueMiddle(int num){
        return num == 7 || num == 8;
    }

    private boolean checkBlue(int blueNum){

        return true;
    }

    private boolean isBlueBig(int num){
//        if(num>8){
//            return 1;
//        }else if(num<7){
//            return -1;
//        }else {
//            return 0;
//        }
        return num>8;
    }

    /**
     *
     * @param issueNo
     * @param cases  预测方案 例如321 即1区3个 2区2个 3区1个
     * @param redForbiddenSet
     * @return
     */
    private int[] genRedBallByCase(int issueNo, int[]cases, Set<Integer> redForbiddenSet){

        Random random = new Random();
        int []result = new int[6];
        int start = 0;
        Set<Integer> newSet = new HashSet<>();
        Set<Integer> set5 = new HashSet<>();
        Set<Integer> set10 = new HashSet<>();

        if(cases[0]>0){
            int index = 0;
            List<IssueRecord> recordList = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(issueNo-5,issueNo,1,null,null,true));
            recordList.forEach(obj->{
                set5.add((int)obj.getNum());
            });
            while (true){
                if(index>cases[0]-1){
                    break;
                }
                int num = recordList.get(random.nextInt(recordList.size())).getNum();
                if(redForbiddenSet.contains(num) || newSet.contains(num)){
                    continue;
                }
                newSet.add(num);
                result[start+index] = num;
                index++;
            }
            start += cases[0];
        }
        if(cases[1]>0){
            int index = 0;
            List<IssueRecord> newRecordList = new ArrayList<>();
            List<IssueRecord> recordList = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(issueNo-10,issueNo-6,1,null,null,true));
            recordList.forEach(obj->{
                if(!set5.contains((int)obj.getNum())){
                    set10.add((int)obj.getNum());
                    newRecordList.add(obj);
                }
            });
            while (true){
                if(index>cases[1]-1){
                    break;
                }
                int num = newRecordList.get(random.nextInt(newRecordList.size())).getNum();
                if(redForbiddenSet.contains(num) || newSet.contains(num) ){
                    continue;
                }
                newSet.add(num);
                result[start+index] = num;
                index++;
            }
            start += cases[1];
        }
        if(cases[2]>0){
            int index = 0;
            List<IssueRecord> newRecordList = new ArrayList<>();
            List<IssueRecord> recordList = issueRecordMapper.selectNumByAreaCondition(new IssueRecordDTO(issueNo-15,issueNo-11,1,null,null,true));
            recordList.forEach(obj->{
                if(!set5.contains((int)obj.getNum()) && !set10.contains((int)obj.getNum())){
                    newRecordList.add(obj);
                }
            });
            while (true){
                if(index>cases[2]-1){
                    break;
                }
                int num = newRecordList.get(random.nextInt(newRecordList.size())).getNum();
                if(redForbiddenSet.contains(num) || newSet.contains(num) ){
                    continue;
                }
                newSet.add(num);
                result[start+index] = num;
                index++;
            }
            start += cases[2];
        }

        return result;
    }

    private int[] genRedBallByProb(int newIssueNo, Set<Integer> redForbiddenSet, int[]newRedPool)throws Exception{
        int redResults[] = new int[6];
        int[] hotRed5Pools = getHotRedPool(newIssueNo,5);
        if(hotRed5Pools.length>20){
            randomRemoveHot(hotRed5Pools,1,redForbiddenSet);
        }
        //6-10
        int[] hotRed10Pools = getHotRedPool(newIssueNo-6,5);
        if(hotRed10Pools.length>20){
            randomRemoveHot(hotRed10Pools,3,redForbiddenSet);
        }
        Random random = new Random();
        int hotSize = random.nextInt(2)+3;
        int hostMax = 5;
        //获取hotSize 个hot5红球
        int[]hot5Results = getHotRedBalls(hotRed5Pools,hotSize,redForbiddenSet);
        for(int i=0;i<hot5Results.length;i++){
            redResults[i] = hot5Results[i];
        }
        //获取1个hot10红球
        int[]hot10Results = getHotRedBalls(hotRed10Pools,1,redForbiddenSet);
        redResults[hotSize] = hot10Results[0];

        //如果此时红球数量不足5球从hot10与hot5集合中取1个红球
        if(hotSize+1<hostMax){
            int[]hotRedPools = new int[hotRed5Pools.length+hotRed10Pools.length];
            System.arraycopy(hotRed5Pools,0,hotRedPools,0,hotRed5Pools.length);
            System.arraycopy(hotRed10Pools,0,hotRedPools,hotRed5Pools.length,hotRed10Pools.length);
            int[]hotOtherResults = getHotRedBalls(hotRedPools,hostMax-hotSize-1,redForbiddenSet);
            for(int i=0;i<hotOtherResults.length;i++){
                redResults[i+hotSize+1] = hotOtherResults[i];
            }
        }

        int[] redCommonResults = genRedBalls(newRedPool,redForbiddenSet,6-hostMax);
        for(int i=0;i<redCommonResults.length;i++){
            redResults[hostMax+i] = redCommonResults[i];
        }
        return redResults;
    }
}
