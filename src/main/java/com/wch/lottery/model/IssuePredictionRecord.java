package com.wch.lottery.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * issue_prediction_record
 * @author 
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class IssuePredictionRecord implements Serializable {

    public IssuePredictionRecord(Integer issueNo){
        this.issueNo = issueNo;
    }

    public IssuePredictionRecord(Integer issueNo,Byte num,Boolean red,Boolean hit){
        this.issueNo = issueNo;
        this.num = num;
        this.red = red;
        this.hit = hit;
    }

    private Long id;

    /**
     * 期号
     */
    private Integer issueNo;

    /**
     * 号码
     */
    private Byte num;

    /**
     * 是否为红球 1红球 0蓝球
     */
    private Boolean red;

    /**
     * 是否命中 1命中 0未命中
     */
    private Boolean hit;

    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        IssuePredictionRecord other = (IssuePredictionRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getIssueNo() == null ? other.getIssueNo() == null : this.getIssueNo().equals(other.getIssueNo()))
            && (this.getNum() == null ? other.getNum() == null : this.getNum().equals(other.getNum()))
            && (this.getRed() == null ? other.getRed() == null : this.getRed().equals(other.getRed()))
            && (this.getHit() == null ? other.getHit() == null : this.getHit().equals(other.getHit()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getIssueNo() == null) ? 0 : getIssueNo().hashCode());
        result = prime * result + ((getNum() == null) ? 0 : getNum().hashCode());
        result = prime * result + ((getRed() == null) ? 0 : getRed().hashCode());
        result = prime * result + ((getHit() == null) ? 0 : getHit().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", issueNo=").append(issueNo);
        sb.append(", num=").append(num);
        sb.append(", red=").append(red);
        sb.append(", hit=").append(hit);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}