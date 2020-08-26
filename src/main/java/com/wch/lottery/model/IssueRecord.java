package com.wch.lottery.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * issue_record
 * @author 
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class IssueRecord implements Comparable<IssueRecord> {

    public IssueRecord(Integer issueNo,Boolean red){
        this.issueNo = issueNo;
        this.red = red;
    }

    public IssueRecord(Integer issueNo,Byte num,Boolean red){
        this.issueNo = issueNo;
        this.num = num;
        this.red = red;
    }

    private Long id;

    private Integer issueNo;

    private Byte num;

    /**
     * 是否为红球 1红球 0蓝球
     */
    private Boolean red;

    private static final long serialVersionUID = 1L;

    @Override
    public int compareTo(IssueRecord o) {
        if(o == null){
            return 0;
        }else if(o.red != null && !o.red){
            return 1;
        }else {
            if(o.num>this.num){
                return 1;
            }else {
                return 0;
            }
        }
    }

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
        IssueRecord other = (IssueRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getIssueNo() == null ? other.getIssueNo() == null : this.getIssueNo().equals(other.getIssueNo()))
            && (this.getNum() == null ? other.getNum() == null : this.getNum().equals(other.getNum()))
            && (this.getRed() == null ? other.getRed() == null : this.getRed().equals(other.getRed()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getIssueNo() == null) ? 0 : getIssueNo().hashCode());
        result = prime * result + ((getNum() == null) ? 0 : getNum().hashCode());
        result = prime * result + ((getRed() == null) ? 0 : getRed().hashCode());
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
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}