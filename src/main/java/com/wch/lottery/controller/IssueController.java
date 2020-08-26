package com.wch.lottery.controller;

import com.wch.lottery.model.IssueRecord;
import com.wch.lottery.model.body.IssueBody;
import com.wch.lottery.model.vo.ResponseVO;
import com.wch.lottery.service.IssueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RequestMapping("/lottery")
@RestController
public class IssueController {

    @Autowired
    private IssueService issueService;


    @RequestMapping(value = "/issues",method = RequestMethod.POST)
    @ResponseBody
    public ResponseVO add(@RequestBody IssueBody issueBody){
        ResponseVO vo = new ResponseVO(0,"success",null);
        try {

            issueService.add(issueBody);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return new ResponseVO(1,e.getMessage(),null);
        }

        return vo;


    }

    @RequestMapping(value = "/issues" ,method = RequestMethod.GET)
    public ResponseVO list(IssueRecord issueRecord){
        try {

            List<IssueRecord> list = issueService.list(issueRecord);
            return new ResponseVO(0,"",list);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return new ResponseVO();
    }

    @RequestMapping(value = "/issues/predict" ,method = RequestMethod.GET)
    public ResponseVO predict(Integer groupNum,Integer issueNo,boolean isAccurate,int[]cases){
        try {
            ResponseVO vo = new ResponseVO(0,"success",null);
            int[][]result = issueService.predict(issueNo,groupNum,isAccurate,cases);
            Set<Integer> resultSet = new HashSet<>();
            Map<Integer,Integer> resultMap = new HashMap<>();
            Map<Integer,List<Integer>> countMap = new HashMap<>();
            for(int[] r: result){
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
            System.out.println(resultMap.size());
            System.out.println(resultMap);
            System.out.println(countMap);

            return new ResponseVO(0,"",result);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return new ResponseVO();
    }

    @RequestMapping(value = "/issues/areaPredict" ,method = RequestMethod.GET)
    public ResponseVO areaPredict(Integer groupNum,Integer issueNo ,boolean isAccurate,int[]cases) {
        ResponseVO vo = new ResponseVO(0, "success", null);
        try {
            int[][] result = issueService.areaPredict(issueNo, groupNum,isAccurate,cases);
            vo.setResult(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return vo;
    }

}
