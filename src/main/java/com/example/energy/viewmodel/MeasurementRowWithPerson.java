package com.example.energy.viewmodel;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class MeasurementRowWithPerson {
    private String hepMbr;
    private int value;
    private String personName;
    private int lastMonthSum;
    private int thisMonthSum;
    private int diffLastMonthSum;
    private int diffThisMonthSum;
    private String adresa;
    private String grad;
    private String meterCode;


    public MeasurementRowWithPerson(String hepMbr, int value, String personName) {
        this.hepMbr = hepMbr;
        this.value = value;
        this.personName = personName;
    }

    public MeasurementRowWithPerson(String hepMbr, String personName,int lastMonthSum,int thisMonthSum, int diffLastMonthSum,int diffThisMonthSum,String grad,String adresa) {
        this.hepMbr = hepMbr;
        this.personName = personName;
        this.lastMonthSum = lastMonthSum;
        this.thisMonthSum = thisMonthSum;
        this.diffLastMonthSum = diffLastMonthSum;
        this.diffThisMonthSum = diffThisMonthSum;
        this.adresa = adresa;
        this.grad = grad;
    }
    public MeasurementRowWithPerson(String hepMbr, String personName,int lastMonthSum,int thisMonthSum, int diffLastMonthSum,int diffThisMonthSum,String grad,String adresa,String meterCode) {
        this.hepMbr = hepMbr;
        this.personName = personName;
        this.lastMonthSum = lastMonthSum;
        this.thisMonthSum = thisMonthSum;
        this.diffLastMonthSum = diffLastMonthSum;
        this.diffThisMonthSum = diffThisMonthSum;
        this.adresa = adresa;
        this.grad = grad;
        this.meterCode = meterCode;
    }



}
