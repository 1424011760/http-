package utils;

/*
@Author zd
@Date 2020/12/17 15:29
*/

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class MyData {
    @ExcelHeader(value = "推送时间")
    private  String time;
    @ExcelHeader(value = "项目名称")
    private  String name;
    @ExcelHeader(value = "采购单位")
    private String unit;
    @ExcelHeader(value = "送货地址")
    private String addr;
    @ExcelHeader(value = "成交价")
    private String priceSuccess;
    @ExcelHeader(value = "预算价")
    private String budgeting;
    @ExcelHeader(value = "数量")
    private String count;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getPriceSuccess() {
        return priceSuccess;
    }

    public void setPriceSuccess(String priceSuccess) {
        this.priceSuccess = priceSuccess;
    }

    public String getBudgeting() {
        return budgeting;
    }

    public void setBudgeting(String budgeting) {
        this.budgeting = budgeting;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "MyData{" +
                "time='" + time + '\'' +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", addr='" + addr + '\'' +
                ", priceSuccess='" + priceSuccess + '\'' +
                ", budgeting='" + budgeting + '\'' +
                ", count='" + count + '\'' +
                '}';
    }
}
