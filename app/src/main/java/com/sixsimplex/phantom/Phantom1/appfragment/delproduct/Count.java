package com.sixsimplex.phantom.Phantom1.appfragment.delproduct;

public class Count {
    int number=0;
    public Count(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void minusOne() {
        if(number != 0 && number>0){
            number--;
        }
    }

    public void plusOne() {
        if( number<20){
            number++;
        }
    }
}
