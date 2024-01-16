package com.nex.request;

import lombok.Data;

@Data
public class ReqNotice {
    private int tsiUno;
    private String tsiKeyword;
    private int tsrUno;
    private int page;

    public int getPage(){
        if(this.page == 0){
            return 1;
        }else{
            return this.page;
        }
    }
}
