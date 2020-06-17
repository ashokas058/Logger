package nss.support.ashokas.nsslog.RecyclerView;

/**
 * Created by DARK-DEVIL on 5/7/2020.
 */

public class CLS_callsModel  {
    String callerName,callDate,callType,callernumber;

    public CLS_callsModel(String callerName, String callDate, String callType, String callernumber) {
        this.callerName = callerName;
        this.callDate = callDate;
        this.callType = callType;
        this.callernumber = callernumber;
    }
    public CLS_callsModel(){}

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallDate() {
        return callDate;
    }

    public void setCallDate(String callDate) {
        this.callDate = callDate;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallernumber() {
        return callernumber;
    }

    public void setCallernumber(String callernumber) {
        this.callernumber = callernumber;
    }
}
